package assembler.parser;

import assembler.core.Instruction;
import assembler.core.Label;

/**
 * Represents a single line of assembly code after parsing.
 * Contains the original source line, parsed components, and generated machine code.
 */
public class AssemblyLine {
    private final int lineNumber;
    private final String sourceLine;
    private Label label;
    private Instruction instruction;
    private String comment;
    private int address;
    private byte[] machineCode;    private String pseudoInstruction;
    private Object pseudoOperand;
    private boolean isComment;
    private boolean isEmpty;
    
    /**
     * Creates a new AssemblyLine with the given line number and source text.
     * 
     * @param lineNumber The line number in the source file (1-based)
     * @param sourceLine The original source line text
     */
    public AssemblyLine(int lineNumber, String sourceLine) {
        this.lineNumber = lineNumber;
        this.sourceLine = sourceLine;
        this.address = -1; // Will be set during first pass
        this.machineCode = new byte[0];
        this.isComment = false;
        this.isEmpty = false;
    }
    
    // Getters
    public int getLineNumber() {
        return lineNumber;
    }
    
    public String getSourceLine() {
        return sourceLine;
    }
    
    public Label getLabel() {
        return label;
    }
    
    public Instruction getInstruction() {
        return instruction;
    }
    
    public String getComment() {
        return comment;
    }
    
    public int getAddress() {
        return address;
    }
    
    public byte[] getMachineCode() {
        return machineCode;
    }
    
    public String getPseudoInstruction() {
        return pseudoInstruction;
    }
    
    public boolean isComment() {
        return isComment;
    }
    
    public boolean isEmpty() {
        return isEmpty;
    }
    
    public boolean hasLabel() {
        return label != null;
    }
    
    public boolean hasInstruction() {
        return instruction != null;
    }
    
    public boolean isPseudoInstruction() {
        return pseudoInstruction != null;
    }
    
    // Setters
    public void setLabel(Label label) {
        this.label = label;
    }
    
    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public void setAddress(int address) {
        this.address = address;
        if (label != null) {
            label.setAddress(address);
        }
    }
    
    public void setMachineCode(byte[] machineCode) {
        this.machineCode = machineCode != null ? machineCode : new byte[0];
    }
    
    public void setPseudoInstruction(String pseudoInstruction) {
        this.pseudoInstruction = pseudoInstruction;
    }
    
    public void setIsComment(boolean isComment) {
        this.isComment = isComment;
    }
      /**
     * Sets a pseudo-instruction with an integer operand.
     * @param pseudoOp The pseudo-instruction name
     * @param operand The integer operand
     */
    public void setPseudoOp(String pseudoOp, int operand) {
        this.pseudoInstruction = pseudoOp;
        this.pseudoOperand = operand;
    }
    
    /**
     * Sets a pseudo-instruction with a list operand.
     * @param pseudoOp The pseudo-instruction name
     * @param operand The list operand
     */
    public void setPseudoOp(String pseudoOp, java.util.List<Integer> operand) {
        this.pseudoInstruction = pseudoOp;
        this.pseudoOperand = operand;
    }
    
    /**
     * Gets the pseudo-instruction operand.
     * @return The operand object
     */
    public Object getPseudoOperand() {
        return pseudoOperand;
    }
    
    /**
     * Checks if this line has unresolved label references.
     * @return true if there are unresolved references
     */
    public boolean hasUnresolvedReference() {
        if (instruction != null && instruction.getOperand() != null) {
            String operand = instruction.getOperand();
            // Check if operand looks like a label reference (not a numeric value)
            return operand.matches(".*[a-zA-Z_][a-zA-Z0-9_]*.*") && 
                   !operand.startsWith("$") && !operand.startsWith("%") && 
                   !operand.matches("\\d+");
        }
        return false;
    }
    
    public void setIsEmpty(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }
    
    /**
     * Returns the size in bytes that this line will occupy in memory.
     * For instructions, this is the instruction size.
     * For pseudo-instructions like FCB/FDB, this is the data size.
     * For labels only or comments, this is 0.
     */
    public int getSize() {
        if (instruction != null) {
            return instruction.getSize();
        } else if (isPseudoInstruction()) {
            // Handle pseudo-instruction sizes
            String pseudo = pseudoInstruction.toUpperCase();
            switch (pseudo) {
                case "FCB":
                    return 1; // Form Constant Byte
                case "FDB":
                    return 2; // Form Double Byte
                case "FCC":
                    // Form Constant Character - size depends on string length
                    // This would need to be calculated during parsing
                    return 1; // Default, should be updated during parsing
                default:
                    return 0;
            }
        }
        return 0;
    }
    
    /**
     * Returns a string representation of the machine code in hexadecimal format.
     */
    public String getMachineCodeHex() {
        if (machineCode.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (byte b : machineCode) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Line %d: ", lineNumber));
        
        if (isEmpty) {
            sb.append("(empty)");
        } else if (isComment) {
            sb.append("(comment) ").append(sourceLine);
        } else {
            if (hasLabel()) {
                sb.append(label.getName()).append(": ");
            }
            if (hasInstruction()) {
                sb.append(instruction.toString());
            } else if (isPseudoInstruction()) {
                sb.append(pseudoInstruction);
            }
            if (comment != null && !comment.isEmpty()) {
                sb.append(" ; ").append(comment);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Checks if this line contains a pseudo-instruction
     * @return true if this line is a pseudo-instruction
     */
    public boolean isPseudoOp() {
        return isPseudoInstruction();
    }
    
    /**
     * Gets the pseudo-instruction name
     * @return the pseudo-instruction name, or null if not a pseudo-instruction
     */
    public String getPseudoOp() {
        return pseudoInstruction;
    }
    
    /**
     * Sets the machine code for this line
     * @param machineCode the machine code bytes
     */
    public void setMachineCode(java.util.List<Integer> machineCode) {
        this.machineCode = new byte[machineCode.size()];
        for (int i = 0; i < machineCode.size(); i++) {
            this.machineCode[i] = machineCode.get(i).byteValue();
        }
    }
}
