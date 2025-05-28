package assembler.assembler;

/**
 * Exception thrown during machine code generation phase.
 * Indicates errors in converting parsed assembly to machine code.
 * 
 * Common causes:
 * - Invalid operand values or ranges
 * - Unsupported instruction/addressing mode combinations
 * - Unresolved label references
 * - Address overflow conditions
 * 
 * @author Motorola 6800 Assembler Team
 */
public class CodeGenerationException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Create a new code generation exception with message.
     * 
     * @param message Error description
     */
    public CodeGenerationException(String message) {
        super(message);
    }
    
    /**
     * Create a new code generation exception with message and cause.
     * 
     * @param message Error description
     * @param cause Underlying exception that caused this error
     */
    public CodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
