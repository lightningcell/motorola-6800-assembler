package assembler;

/**
 * Represents a parsed line of assembly code.
 */
public class AssemblyLine {
    public final int lineNumber;
    public final String label;
    public final String mnemonic;
    public final String operand;
    public final String comment;

    public AssemblyLine(int lineNumber, String label, String mnemonic, String operand, String comment) {
        this.lineNumber = lineNumber;
        this.label = label;
        this.mnemonic = mnemonic;
        this.operand = operand;
        this.comment = comment;
    }
}
