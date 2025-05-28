package assembler.core;

/**
 * Represents the addressing modes available in the Motorola 6800 processor
 * 
 * The 6800 supports 7 different addressing modes:
 * 1. INHERENT: No operand needed (e.g., NOP, TAB)
 * 2. IMMEDIATE: Operand is a constant value (e.g., LDA #$FF)
 * 3. DIRECT: Operand is an address in zero page (0-255) (e.g., LDA $80)
 * 4. EXTENDED: Operand is a 16-bit address (e.g., LDA $1000)
 * 5. INDEXED: Operand is calculated from index register + offset (e.g., LDA $10,X)
 * 6. RELATIVE: Used for branch instructions with relative offset (e.g., BRA LOOP)
 * 7. PSEUDO: Assembler directives (e.g., ORG, END)
 */
public enum AddressingMode {
    
    /** No operand required - instruction operates on registers only */
    INHERENT("Inherent", 0, "No operand"),
    
    /** Operand is an 8-bit or 16-bit immediate value */
    IMMEDIATE("Immediate", 1, "#data"),
    
    /** Operand is an 8-bit zero page address (0x00-0xFF) */
    DIRECT("Direct", 1, "addr (0-255)"),
    
    /** Operand is a 16-bit absolute address */
    EXTENDED("Extended", 2, "addr (0-65535)"),
    
    /** Operand is offset from index register X */
    INDEXED("Indexed", 1, "offset,X"),
    
    /** Operand is a relative offset for branch instructions */
    RELATIVE("Relative", 1, "relative offset"),
    
    /** Assembler pseudo-instructions like ORG, END */
    PSEUDO("Pseudo", 0, "assembler directive");
    
    private final String name;
    private final int operandSize; // Size in bytes
    private final String format;
    
    /**
     * Creates a new addressing mode
     * 
     * @param name Human-readable name
     * @param operandSize Size of operand in bytes
     * @param format Format description for display
     */
    AddressingMode(String name, int operandSize, String format) {
        this.name = name;
        this.operandSize = operandSize;
        this.format = format;
    }
    
    /**
     * Gets the human-readable name of this addressing mode
     * @return The name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the size of the operand in bytes
     * @return Operand size (0, 1, or 2 bytes)
     */
    public int getOperandSize() {
        return operandSize;
    }
    
    /**
     * Gets the format description for display purposes
     * @return Format string
     */
    public String getFormat() {
        return format;
    }
    
    /**
     * Gets the total instruction size including opcode and operand
     * @return Total instruction size in bytes
     */
    public int getInstructionSize() {
        return 1 + operandSize; // 1 byte opcode + operand
    }
    
    @Override
    public String toString() {
        return name + " (" + format + ")";
    }
}
