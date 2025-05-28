package assembler.parser;

import assembler.core.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Main parser for Motorola 6800 assembly language source code.
 * Handles tokenization, syntax validation, and conversion to internal representation.
 * 
 * Supports:
 * - Labels (with colon terminator)
 * - Instructions with various addressing modes
 * - Pseudo-instructions (ORG, END, EQU, FCB, FDB)
 * - Comments (semicolon to end of line)
 * - Numeric literals (decimal, hex $xx, binary %xxxxxxxx)
 * 
 * @author Motorola 6800 Assembler Team
 */
public class AssemblyParser {
    
    private final SyntaxValidator validator;
    private final LabelResolver labelResolver;
    private final TokenParser tokenParser;
      // Patterns for different token types  
    private static final Pattern HEX_PATTERN = Pattern.compile("^\\$[0-9A-Fa-f]+$");
    private static final Pattern BINARY_PATTERN = Pattern.compile("^%[01]+$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^[0-9]+$");
    
    public AssemblyParser() {
        this.validator = new SyntaxValidator();
        this.labelResolver = new LabelResolver();
        this.tokenParser = new TokenParser();
    }
    
    /**
     * Parse complete assembly source code into list of assembly lines.
     * 
     * @param sourceCode The complete assembly source code
     * @return List of parsed assembly lines
     * @throws ParseException if syntax errors are found
     */    public List<AssemblyLine> parseSource(String sourceCode) throws ParseException {
        List<AssemblyLine> assemblyLines = new ArrayList<>();
        String[] lines = sourceCode.split("\n");

        // First pass: parse lines only (no label resolution yet)
        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            String line = lines[lineNumber].trim();

            // Skip empty lines
            if (line.isEmpty()) {
                continue;
            }

            try {
                AssemblyLine assemblyLine = parseLine(line, lineNumber + 1);
                if (assemblyLine != null) {
                    assemblyLines.add(assemblyLine);
                }
            } catch (ParseException e) {
                throw new ParseException(String.format("Line %d: %s", lineNumber + 1, e.getMessage()));
            }
        }

        return assemblyLines;
    }
    
    /**
     * Parse a single line of assembly code.
     * 
     * @param line The source line to parse
     * @param lineNumber The line number (1-based)
     * @return Parsed assembly line or null for empty/comment lines
     * @throws ParseException if syntax errors are found
     */
    public AssemblyLine parseLine(String line, int lineNumber) throws ParseException {
        // Remove comments
        int commentPos = line.indexOf(';');
        if (commentPos >= 0) {
            line = line.substring(0, commentPos).trim();
        }
        
        // Skip empty lines after comment removal
        if (line.isEmpty()) {
            return null;
        }
        
        // Tokenize the line
        List<String> tokens = tokenParser.tokenize(line);
        if (tokens.isEmpty()) {
            return null;
        }
        
        AssemblyLine assemblyLine = new AssemblyLine(lineNumber, line);
        int tokenIndex = 0;
        
        // Check for label
        if (tokens.get(0).endsWith(":")) {
            String labelName = tokens.get(0).substring(0, tokens.get(0).length() - 1);
            validator.validateLabelName(labelName);
            assemblyLine.setLabel(new Label(labelName));
            tokenIndex++;
        }
        
        // Must have at least instruction/pseudo-op after label
        if (tokenIndex >= tokens.size()) {
            throw new ParseException("Missing instruction or pseudo-operation");
        }
        
        String mnemonic = tokens.get(tokenIndex++).toUpperCase();
        
        // Handle pseudo-instructions
        if (isPseudoInstruction(mnemonic)) {
            return parsePseudoInstruction(assemblyLine, mnemonic, tokens, tokenIndex);
        }
        
        // Parse regular instruction
        return parseInstruction(assemblyLine, mnemonic, tokens, tokenIndex);
    }
    
    /**
     * Parse a pseudo-instruction (ORG, END, EQU, FCB, FDB).
     */
    private AssemblyLine parsePseudoInstruction(AssemblyLine assemblyLine, String mnemonic, 
                                               List<String> tokens, int tokenIndex) throws ParseException {
        switch (mnemonic) {
            case "ORG":
                if (tokenIndex >= tokens.size()) {
                    throw new ParseException("ORG requires an address operand");
                }
                int address = parseNumericOperand(tokens.get(tokenIndex));
                assemblyLine.setPseudoOp("ORG", address);
                break;
                
            case "END":
                assemblyLine.setPseudoOp("END", 0);
                break;
                
            case "EQU":
                if (tokenIndex >= tokens.size()) {
                    throw new ParseException("EQU requires a value operand");
                }
                if (assemblyLine.getLabel() == null) {
                    throw new ParseException("EQU requires a label");
                }
                int value = parseNumericOperand(tokens.get(tokenIndex));
                assemblyLine.setPseudoOp("EQU", value);
                break;
                
            case "FCB":
                if (tokenIndex >= tokens.size()) {
                    throw new ParseException("FCB requires byte value(s)");
                }
                // Handle multiple comma-separated values
                List<Integer> bytes = parseByteList(tokens, tokenIndex);
                assemblyLine.setPseudoOp("FCB", bytes);
                break;
                
            case "FDB":
                if (tokenIndex >= tokens.size()) {
                    throw new ParseException("FDB requires word value(s)");
                }
                // Handle multiple comma-separated values
                List<Integer> words = parseWordList(tokens, tokenIndex);
                assemblyLine.setPseudoOp("FDB", words);
                break;
                
            default:
                throw new ParseException("Unknown pseudo-instruction: " + mnemonic);
        }
        
        return assemblyLine;
    }
    
    /**
     * Parse a regular 6800 instruction.
     */
    private AssemblyLine parseInstruction(AssemblyLine assemblyLine, String mnemonic, 
                                         List<String> tokens, int tokenIndex) throws ParseException {
        
        // Determine addressing mode and operand
        AddressingMode addressingMode;
        String operand = null;
        
        if (tokenIndex >= tokens.size()) {
            // No operand - inherent addressing
            addressingMode = AddressingMode.INHERENT;        } else {
            // Join remaining tokens as operand (handles expressions)
            operand = String.join(" ", tokens.subList(tokenIndex, tokens.size()));
            addressingMode = determineAddressingMode(mnemonic, operand);
        }
        
        // Validate instruction exists and addressing mode is supported
        validator.validateInstruction(mnemonic, addressingMode);
        
        // Create instruction object
        Instruction instruction = new Instruction(mnemonic, addressingMode, operand);
        assemblyLine.setInstruction(instruction);
        
        return assemblyLine;
    }
      /**
     * Determine addressing mode from operand syntax.
     */    private AddressingMode determineAddressingMode(String mnemonic, String operand) throws ParseException {
        if (operand == null || operand.trim().isEmpty()) {
            return AddressingMode.INHERENT;
        }
        
        operand = operand.trim();
        String upperMnemonic = mnemonic.toUpperCase();
        
        // Branch instructions always use relative addressing
        if (isBranchInstruction(upperMnemonic)) {
            return AddressingMode.RELATIVE;
        }
        
        // Immediate: #value
        if (operand.startsWith("#")) {
            return AddressingMode.IMMEDIATE;
        }
        
        // Indexed: value,X
        if (operand.toUpperCase().endsWith(",X")) {
            return AddressingMode.INDEXED;
        }
        
        // For numeric values, determine if direct (zero page) or extended
        if (isNumericOperand(operand)) {
            int value = parseNumericOperand(operand);
            if (value >= 0 && value <= 255) {
                return AddressingMode.DIRECT;
            } else {
                return AddressingMode.EXTENDED;
            }
        }
        
        // Label reference - assume extended for now, resolve later
        return AddressingMode.EXTENDED;
    }
    
    /**
     * Check if operand is a numeric literal.
     */
    private boolean isNumericOperand(String operand) {
        return HEX_PATTERN.matcher(operand).matches() ||
               BINARY_PATTERN.matcher(operand).matches() ||
               DECIMAL_PATTERN.matcher(operand).matches();
    }
    
    /**
     * Parse numeric operand (decimal, hex $xx, or binary %xxxxxxxx).
     */
    private int parseNumericOperand(String operand) throws ParseException {
        try {
            if (operand.startsWith("$")) {
                return Integer.parseInt(operand.substring(1), 16);
            } else if (operand.startsWith("%")) {
                return Integer.parseInt(operand.substring(1), 2);
            } else {
                return Integer.parseInt(operand);
            }
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid numeric operand: " + operand);
        }
    }
    
    /**
     * Parse comma-separated list of byte values for FCB.
     */
    private List<Integer> parseByteList(List<String> tokens, int startIndex) throws ParseException {
        List<Integer> bytes = new ArrayList<>();
        String operandStr = String.join(" ", tokens.subList(startIndex, tokens.size()));
        String[] values = operandStr.split(",");
        
        for (String value : values) {
            int byteValue = parseNumericOperand(value.trim());
            if (byteValue < 0 || byteValue > 255) {
                throw new ParseException("Byte value out of range (0-255): " + byteValue);
            }
            bytes.add(byteValue);
        }
        
        return bytes;
    }
    
    /**
     * Parse comma-separated list of word values for FDB.
     */
    private List<Integer> parseWordList(List<String> tokens, int startIndex) throws ParseException {
        List<Integer> words = new ArrayList<>();
        String operandStr = String.join(" ", tokens.subList(startIndex, tokens.size()));
        String[] values = operandStr.split(",");
        
        for (String value : values) {
            int wordValue = parseNumericOperand(value.trim());
            if (wordValue < 0 || wordValue > 65535) {
                throw new ParseException("Word value out of range (0-65535): " + wordValue);
            }
            words.add(wordValue);
        }
        
        return words;
    }
      /**
     * Check if mnemonic is a pseudo-instruction.
     */
    private boolean isPseudoInstruction(String mnemonic) {
        return Arrays.asList("ORG", "END", "EQU", "FCB", "FDB").contains(mnemonic);
    }
    
    /**
     * Check if mnemonic is a branch instruction.
     */
    private boolean isBranchInstruction(String mnemonic) {
        return Arrays.asList("BRA", "BEQ", "BNE", "BCC", "BCS", "BPL", "BMI", 
                           "BVC", "BVS", "BGE", "BLT", "BGT", "BLE", "BHI", "BLS").contains(mnemonic);
    }
    
    /**
     * Resolve label references in assembly lines.
     * This should be called after addresses have been calculated.
     */
    public void resolveLabelReferences(List<AssemblyLine> assemblyLines) throws ParseException {
        // First, collect all labels with their addresses
        for (AssemblyLine line : assemblyLines) {
            if (line.getLabel() != null) {
                labelResolver.addLabel(line.getLabel().getName(), line.getAddress());
            }
        }
        
        // Then resolve all label references
        for (AssemblyLine line : assemblyLines) {
            labelResolver.resolveReferences(line);
        }
    }
}
