package assembler.parser;

import assembler.core.AddressingMode;
import assembler.assembler.OpcodeTable;
import java.util.*;
import java.util.Arrays;

/**
 * Syntax validator for Motorola 6800 assembly language.
 * Validates instruction mnemonics, addressing modes, labels, and operands.
 * 
 * @author Motorola 6800 Assembler Team
 */
public class SyntaxValidator {
    
    private final OpcodeTable opcodeTable;
    private final Set<String> reservedWords;
    
    public SyntaxValidator() {
        this.opcodeTable = new OpcodeTable();
        this.reservedWords = initializeReservedWords();
    }
    
    /**
     * Initialize set of reserved words that cannot be used as labels.
     */
    private Set<String> initializeReservedWords() {
        Set<String> reserved = new HashSet<>();
        
        // CPU registers
        reserved.add("A");
        reserved.add("B");
        reserved.add("X");
        reserved.add("S");
        reserved.add("PC");
        reserved.add("CCR");
        
        // Pseudo-instructions
        reserved.add("ORG");
        reserved.add("END");
        reserved.add("EQU");
        reserved.add("FCB");
        reserved.add("FDB");
        
        // All instruction mnemonics from opcode table
        reserved.addAll(opcodeTable.getAllMnemonics());
        
        return reserved;
    }
    
    /**
     * Validate a label name.
     * 
     * @param labelName The label name to validate
     * @throws IllegalArgumentException if the label is invalid
     */
    public void validateLabelName(String labelName) {
        if (labelName == null || labelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Label name cannot be empty");
        }
        
        // Check format: must start with letter or underscore, followed by alphanumeric or underscore
        if (!labelName.matches("^[A-Za-z_][A-Za-z0-9_]*$")) {
            throw new IllegalArgumentException("Invalid label name format: " + labelName + 
                ". Labels must start with a letter or underscore, followed by letters, digits, or underscores.");
        }
        
        // Check length (reasonable limit)
        if (labelName.length() > 31) {
            throw new IllegalArgumentException("Label name too long (max 31 characters): " + labelName);
        }
        
        // Check reserved words (case-insensitive)
        if (reservedWords.contains(labelName.toUpperCase())) {
            throw new IllegalArgumentException("Label name conflicts with reserved word: " + labelName);
        }
    }
    
    /**
     * Validate an instruction mnemonic and addressing mode combination.
     * 
     * @param mnemonic The instruction mnemonic
     * @param addressingMode The addressing mode
     * @throws IllegalArgumentException if the combination is invalid
     */
    public void validateInstruction(String mnemonic, AddressingMode addressingMode) {
        if (mnemonic == null || mnemonic.trim().isEmpty()) {
            throw new IllegalArgumentException("Instruction mnemonic cannot be empty");
        }
        
        String upperMnemonic = mnemonic.toUpperCase();
        
        // Check if instruction exists
        if (!opcodeTable.hasInstruction(upperMnemonic)) {
            throw new IllegalArgumentException("Unknown instruction: " + mnemonic);
        }
        
        // Check if addressing mode is supported for this instruction
        if (!opcodeTable.hasInstructionWithMode(upperMnemonic, addressingMode)) {
            throw new IllegalArgumentException(String.format(
                "Instruction %s does not support %s addressing mode", 
                mnemonic, addressingMode.name().toLowerCase()));
        }
    }
    
    /**
     * Validate an immediate operand value.
     * 
     * @param value The immediate value
     * @param is16Bit Whether this is a 16-bit instruction (true) or 8-bit (false)
     * @throws IllegalArgumentException if the value is out of range
     */
    public void validateImmediateValue(int value, boolean is16Bit) {
        if (is16Bit) {
            if (value < 0 || value > 65535) {
                throw new IllegalArgumentException(String.format(
                    "16-bit immediate value out of range (0-65535): %d", value));
            }
        } else {
            if (value < 0 || value > 255) {
                throw new IllegalArgumentException(String.format(
                    "8-bit immediate value out of range (0-255): %d", value));
            }
        }
    }
    
    /**
     * Validate a direct (zero page) address.
     * 
     * @param address The address value
     * @throws IllegalArgumentException if the address is not in zero page
     */
    public void validateDirectAddress(int address) {
        if (address < 0 || address > 255) {
            throw new IllegalArgumentException(String.format(
                "Direct addressing requires zero page address (0-255): %d", address));
        }
    }
    
    /**
     * Validate an extended (16-bit) address.
     * 
     * @param address The address value
     * @throws IllegalArgumentException if the address is out of range
     */
    public void validateExtendedAddress(int address) {
        if (address < 0 || address > 65535) {
            throw new IllegalArgumentException(String.format(
                "Extended address out of range (0-65535): %d", address));
            }
    }
    
    /**
     * Validate an indexed offset.
     * 
     * @param offset The indexed offset value
     * @throws IllegalArgumentException if the offset is out of range
     */
    public void validateIndexedOffset(int offset) {
        if (offset < 0 || offset > 255) {
            throw new IllegalArgumentException(String.format(
                "Indexed offset out of range (0-255): %d", offset));
        }
    }
    
    /**
     * Validate a relative branch offset.
     * 
     * @param offset The relative offset (-128 to +127)
     * @throws IllegalArgumentException if the offset is out of range
     */
    public void validateRelativeOffset(int offset) {
        if (offset < -128 || offset > 127) {
            throw new IllegalArgumentException(String.format(
                "Relative branch offset out of range (-128 to +127): %d", offset));
        }
    }
    
    /**
     * Validate operand format for specific addressing mode.
     * 
     * @param operand The operand string
     * @param addressingMode The addressing mode
     * @throws IllegalArgumentException if the operand format is invalid
     */
    public void validateOperandFormat(String operand, AddressingMode addressingMode) {
        if (operand == null) {
            operand = "";
        }
        
        switch (addressingMode) {
            case INHERENT:
                if (!operand.trim().isEmpty()) {
                    throw new IllegalArgumentException("Inherent addressing mode requires no operand");
                }
                break;
                
            case IMMEDIATE:
                if (!operand.startsWith("#")) {
                    throw new IllegalArgumentException("Immediate addressing mode requires # prefix");
                }
                break;
                
            case INDEXED:
                if (!operand.toUpperCase().endsWith(",X")) {
                    throw new IllegalArgumentException("Indexed addressing mode requires ,X suffix");
                }
                break;
                
            case DIRECT:
            case EXTENDED:
            case RELATIVE:
                // These are validated based on the resolved address value
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported addressing mode: " + addressingMode);
        }
    }
    
    /**
     * Validate a pseudo-instruction and its operands.
     * 
     * @param pseudoOp The pseudo-instruction name
     * @param operands The operand list
     * @throws IllegalArgumentException if the pseudo-instruction is invalid
     */
    public void validatePseudoInstruction(String pseudoOp, List<String> operands) {
        switch (pseudoOp.toUpperCase()) {
            case "ORG":
                if (operands.size() != 1) {
                    throw new IllegalArgumentException("ORG requires exactly one address operand");
                }
                break;
                
            case "END":
                if (!operands.isEmpty()) {
                    throw new IllegalArgumentException("END takes no operands");
                }
                break;
                
            case "EQU":
                if (operands.size() != 1) {
                    throw new IllegalArgumentException("EQU requires exactly one value operand");
                }
                break;
                
            case "FCB":
                if (operands.isEmpty()) {
                    throw new IllegalArgumentException("FCB requires at least one byte value");
                }
                break;
                
            case "FDB":
                if (operands.isEmpty()) {
                    throw new IllegalArgumentException("FDB requires at least one word value");
                }
                break;
                
            default:
                throw new IllegalArgumentException("Unknown pseudo-instruction: " + pseudoOp);
        }
    }
    
    /**
     * Check if a string is a valid mnemonic.
     */
    public boolean isValidMnemonic(String mnemonic) {
        return mnemonic != null && opcodeTable.hasInstruction(mnemonic.toUpperCase());
    }
    
    /**
     * Check if a string is a pseudo-instruction.
     */
    public boolean isPseudoInstruction(String word) {
        if (word == null) return false;
        String upper = word.toUpperCase();
        return Arrays.asList("ORG", "END", "EQU", "FCB", "FDB").contains(upper);
    }
    
    /**
     * Get all supported addressing modes for an instruction.
     */
    public Set<AddressingMode> getSupportedAddressingModes(String mnemonic) {
        return opcodeTable.getSupportedAddressingModes(mnemonic.toUpperCase());
    }
}
