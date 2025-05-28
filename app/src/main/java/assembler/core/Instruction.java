package assembler.core;

/**
 * Represents a Motorola 6800 instruction with its mnemonic, addressing mode, and opcode
 * 
 * The 6800 has 72 base instructions that can be used with different addressing modes
 * to create a total of 197 opcodes. Each instruction has specific allowed addressing modes.
 */
public class Instruction {
      private final String mnemonic;
    private final AddressingMode addressingMode;
    private String operand;
    private int resolvedOperand;
    private final int opcode;
    private final int cycles;
    private final String description;
    
    /**
     * Creates a new instruction with mnemonic, addressing mode, and operand (used during parsing)
     * @param mnemonic The assembly language mnemonic
     * @param addressingMode The addressing mode
     * @param operand The operand string
     */
    public Instruction(String mnemonic, AddressingMode addressingMode, String operand) {
        this.mnemonic = mnemonic.toUpperCase();
        this.addressingMode = addressingMode;
        this.operand = operand;
        this.resolvedOperand = -1;
        this.opcode = 0; // Will be set during code generation
        this.cycles = 0; // Will be set during code generation
        this.description = "";
    }
    
    /**
     * Creates a new instruction
     * 
     * @param mnemonic The assembly language mnemonic (e.g., "LDA", "STA", "JMP")
     * @param addressingMode The addressing mode for this instruction variant
     * @param opcode The machine code opcode (0x00-0xFF)
     * @param cycles Number of CPU cycles required for execution
     * @param description Human-readable description of the instruction
     */
    public Instruction(String mnemonic, AddressingMode addressingMode, int opcode, int cycles, String description) {
        this.mnemonic = mnemonic.toUpperCase();
        this.addressingMode = addressingMode;
        this.opcode = opcode & 0xFF; // Ensure 8-bit value
        this.cycles = cycles;
        this.description = description;
    }
    
    /**
     * Gets the instruction mnemonic
     * @return The mnemonic (e.g., "LDA")
     */
    public String getMnemonic() {
        return mnemonic;
    }
    
    /**
     * Gets the addressing mode for this instruction variant
     * @return The addressing mode
     */
    public AddressingMode getAddressingMode() {
        return addressingMode;
    }
    
    /**
     * Gets the opcode value
     * @return The opcode (0x00-0xFF)
     */
    public int getOpcode() {
        return opcode;
    }
    
    /**
     * Gets the number of CPU cycles required
     * @return Number of cycles
     */
    public int getCycles() {
        return cycles;
    }
      /**
     * Gets the operand string
     * @return The operand string
     */
    public String getOperand() {
        return operand;
    }
    
    /**
     * Sets the operand string (used during label resolution)
     * @param operand The new operand string
     */
    public void setOperand(String operand) {
        this.operand = operand;
    }
    
    /**
     * Gets the resolved operand value
     * @return The resolved operand value
     */
    public int getResolvedOperand() {
        return resolvedOperand;
    }
    
    /**
     * Checks if the operand has been resolved to a numeric value
     * @return true if operand has been resolved
     */
    public boolean hasResolvedOperand() {
        return resolvedOperand != -1;
    }
    
    /**
     * Sets the resolved operand value
     * @param value the resolved numeric value
     */
    public void setResolvedOperand(int value) {
        this.resolvedOperand = value;
    }
    
    /**
     * Gets the instruction description
     * @return Description string
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the total size of this instruction in bytes (opcode + operand)
     * @return Instruction size in bytes
     */
    public int getSize() {
        return addressingMode.getInstructionSize();
    }
    
    /**
     * Creates a unique key for this instruction (mnemonic + addressing mode)
     * @return Unique key string
     */
    public String getKey() {
        return mnemonic + "_" + addressingMode.name();
    }
      @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mnemonic);
        if (operand != null && !operand.isEmpty()) {
            sb.append(" ").append(operand);
        }
        if (opcode != 0 || cycles != 0) {
            sb.append(String.format(" (%s) - Opcode: $%02X, Cycles: %d", 
                                  addressingMode.getName(), opcode, cycles));
        } else {
            sb.append(" (").append(addressingMode.getName()).append(")");
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Instruction that = (Instruction) obj;
        return opcode == that.opcode && 
               mnemonic.equals(that.mnemonic) && 
               addressingMode == that.addressingMode;
    }
    
    @Override
    public int hashCode() {
        return mnemonic.hashCode() * 31 + addressingMode.hashCode();
    }
}
