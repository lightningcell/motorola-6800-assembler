package assembler.assembler;

import assembler.core.*;
import assembler.parser.*;
import java.util.*;

/**
 * Generates machine code from parsed assembly instructions.
 * Handles opcode lookup, operand encoding, and address calculation.
 * 
 * Supports all 7 addressing modes:
 * - Inherent: No operand (NOP, RTS, etc.)
 * - Immediate: #value (LDA #$FF)
 * - Direct: $xx (LDA $30 - zero page)
 * - Extended: $xxxx (LDA $1234)
 * - Indexed: $xx,X (LDA $30,X)
 * - Relative: label (BEQ label - branch instructions)
 * - Accumulator: A (ROL A)
 * 
 * @author Motorola 6800 Assembler Team
 */
public class CodeGenerator {
    
    private final OpcodeTable opcodeTable;
    private int currentAddress;
    
    public CodeGenerator() {
        this.opcodeTable = new OpcodeTable();
        this.currentAddress = 0;
    }
    
    /**
     * Generate machine code for a list of assembly lines.
     * 
     * @param assemblyLines List of parsed assembly lines
     * @return Map of addresses to machine code bytes
     * @throws CodeGenerationException if code generation fails
     */
    public Map<Integer, List<Integer>> generateCode(List<AssemblyLine> assemblyLines) 
            throws CodeGenerationException {
        
        Map<Integer, List<Integer>> machineCode = new LinkedHashMap<>();
        currentAddress = 0;
        
        for (AssemblyLine line : assemblyLines) {
            // Set address for this line
            line.setAddress(currentAddress);
            
            List<Integer> bytes = generateLineCode(line);
            if (!bytes.isEmpty()) {
                machineCode.put(currentAddress, bytes);
                line.setMachineCode(bytes);
            }
            
            // Advance address by number of bytes generated
            currentAddress += bytes.size();
        }
        
        return machineCode;
    }
    
    /**
     * Generate machine code for a single assembly line.
     */
    private List<Integer> generateLineCode(AssemblyLine line) throws CodeGenerationException {
        List<Integer> bytes = new ArrayList<>();
        
        // Handle pseudo-instructions
        if (line.isPseudoOp()) {
            return generatePseudoOpCode(line);
        }
        
        // Handle regular instructions
        if (line.getInstruction() != null) {
            return generateInstructionCode(line.getInstruction());
        }
        
        // Empty line or label-only line
        return bytes;
    }
    
    /**
     * Generate code for pseudo-instructions.
     */
    private List<Integer> generatePseudoOpCode(AssemblyLine line) throws CodeGenerationException {
        List<Integer> bytes = new ArrayList<>();
        String pseudoOp = line.getPseudoOp();
        Object operand = line.getPseudoOperand();
        
        switch (pseudoOp) {
            case "ORG":
                // Change current address
                currentAddress = (Integer) operand;
                return bytes; // No bytes generated
                
            case "END":
                return bytes; // No bytes generated
                
            case "EQU":
                return bytes; // No bytes generated (defines symbol value)
                
            case "FCB":
                // Form Constant Byte(s)
                if (operand instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Integer> values = (List<Integer>) operand;
                    bytes.addAll(values);
                } else {
                    bytes.add((Integer) operand);
                }
                break;
                
            case "FDB":
                // Form Double Byte (word) - high byte first (big endian)
                if (operand instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Integer> values = (List<Integer>) operand;
                    for (Integer value : values) {
                        bytes.add((value >> 8) & 0xFF); // High byte
                        bytes.add(value & 0xFF);        // Low byte
                    }
                } else {
                    int value = (Integer) operand;
                    bytes.add((value >> 8) & 0xFF); // High byte
                    bytes.add(value & 0xFF);        // Low byte
                }
                break;
                
            default:
                throw new CodeGenerationException("Unknown pseudo-instruction: " + pseudoOp);
        }
        
        return bytes;
    }
    
    /**
     * Generate machine code for a regular instruction.
     */
    private List<Integer> generateInstructionCode(Instruction instruction) throws CodeGenerationException {
        List<Integer> bytes = new ArrayList<>();
        
        String mnemonic = instruction.getMnemonic();
        AddressingMode mode = instruction.getAddressingMode();
        
        // Get opcode from table
        int opcode = opcodeTable.getOpcode(mnemonic, mode);
        bytes.add(opcode);
          // Add operand bytes based on addressing mode
        switch (mode) {
            case INHERENT:
                // No additional bytes
                break;
                
            case IMMEDIATE:
                // One byte operand
                int immediateValue = getOperandValue(instruction);
                if (immediateValue < 0 || immediateValue > 255) {
                    throw new CodeGenerationException("Immediate value out of range: " + immediateValue);
                }
                bytes.add(immediateValue);
                break;
                
            case DIRECT:
                // One byte address (zero page)
                int directAddr = getOperandValue(instruction);
                if (directAddr < 0 || directAddr > 255) {
                    throw new CodeGenerationException("Direct address out of range: " + directAddr);
                }
                bytes.add(directAddr);
                break;
                
            case EXTENDED:
                // Two byte address (high byte first)
                int extendedAddr = getOperandValue(instruction);
                if (extendedAddr < 0 || extendedAddr > 65535) {
                    throw new CodeGenerationException("Extended address out of range: " + extendedAddr);
                }
                bytes.add((extendedAddr >> 8) & 0xFF); // High byte
                bytes.add(extendedAddr & 0xFF);        // Low byte
                break;
                
            case INDEXED:
                // One byte offset for indexed addressing
                int indexedOffset = getIndexedOffset(instruction);
                if (indexedOffset < 0 || indexedOffset > 255) {
                    throw new CodeGenerationException("Indexed offset out of range: " + indexedOffset);
                }
                bytes.add(indexedOffset);
                break;
                
            case RELATIVE:
                // One byte signed offset for branch instructions
                int relativeOffset = getRelativeOffset(instruction);
                if (relativeOffset < -128 || relativeOffset > 127) {
                    throw new CodeGenerationException("Relative offset out of range: " + relativeOffset);
                }
                bytes.add(relativeOffset & 0xFF); // Convert to unsigned byte
                break;
                
            default:
                throw new CodeGenerationException("Unsupported addressing mode: " + mode);
        }
        
        return bytes;
    } 
    
    /**
     * Get numeric value from instruction operand.
     */
    private int getOperandValue(Instruction instruction) throws CodeGenerationException {
        String operand = instruction.getOperand();
        if (operand == null) {
            throw new CodeGenerationException("Missing operand for instruction: " + instruction.getMnemonic());
        }
        
        // Remove addressing mode prefixes
        operand = operand.trim();
        if (operand.startsWith("#")) {
            operand = operand.substring(1); // Remove immediate prefix
        }
        
        // Use resolved operand if available
        if (instruction.hasResolvedOperand()) {
            return instruction.getResolvedOperand();
        }
        
        return parseNumeric(operand);
    }
    
    /**
     * Get offset for indexed addressing (value before ,X).
     */
    private int getIndexedOffset(Instruction instruction) throws CodeGenerationException {
        String operand = instruction.getOperand();
        if (operand == null || !operand.toUpperCase().endsWith(",X")) {
            throw new CodeGenerationException("Invalid indexed operand: " + operand);
        }
        
        // Remove ",X" suffix
        String offsetStr = operand.substring(0, operand.length() - 2).trim();
        
        // Use resolved operand if available
        if (instruction.hasResolvedOperand()) {
            return instruction.getResolvedOperand();
        }
        
        return parseNumeric(offsetStr);
    }
    
    /**
     * Calculate relative offset for branch instructions.
     */
    private int getRelativeOffset(Instruction instruction) throws CodeGenerationException {
        // Use resolved operand if available (should contain target address)
        if (instruction.hasResolvedOperand()) {
            int targetAddress = instruction.getResolvedOperand();
            int offset = targetAddress - (currentAddress + 2); // +2 for instruction size
            return offset;
        }
        
        // Direct numeric offset
        String operand = instruction.getOperand();
        if (operand != null) {
            return parseNumeric(operand);
        }
        
        throw new CodeGenerationException("Cannot resolve relative offset for: " + instruction.getOperand());
    }
    
    /**
     * Parse numeric value from string (decimal, hex $xx, binary %xx).
     */
    private int parseNumeric(String value) throws CodeGenerationException {
        try {
            value = value.trim();
            if (value.startsWith("$")) {
                return Integer.parseInt(value.substring(1), 16);
            } else if (value.startsWith("%")) {
                return Integer.parseInt(value.substring(1), 2);
            } else {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            throw new CodeGenerationException("Invalid numeric value: " + value);
        }
    }
    
    /**
     * Set current address (used by ORG pseudo-op).
     */
    public void setCurrentAddress(int address) {
        this.currentAddress = address;
    }
    
    /**
     * Get current address.
     */
    public int getCurrentAddress() {
        return currentAddress;
    }
}
