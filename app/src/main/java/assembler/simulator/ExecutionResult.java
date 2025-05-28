package assembler.simulator;

import assembler.parser.AssemblyLine;

/**
 * Result of an execution step in the simulator.
 * Contains status information and context about the executed instruction.
 * 
 * @author Motorola 6800 Assembler Team
 */
public class ExecutionResult {
    
    private final ExecutionStatus status;
    private final int programCounter;
    private final String message;
    private final AssemblyLine assemblyLine;
    
    /**
     * Create a new execution result.
     * 
     * @param status Execution status
     * @param programCounter Current PC value
     * @param message Descriptive message
     * @param assemblyLine Associated assembly line (may be null)
     */
    public ExecutionResult(ExecutionStatus status, int programCounter, 
                          String message, AssemblyLine assemblyLine) {
        this.status = status;
        this.programCounter = programCounter;
        this.message = message;
        this.assemblyLine = assemblyLine;
    }
    
    /**
     * Get execution status.
     */
    public ExecutionStatus getStatus() {
        return status;
    }
    
    /**
     * Get program counter value.
     */
    public int getProgramCounter() {
        return programCounter;
    }
    
    /**
     * Get descriptive message.
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Get associated assembly line.
     */
    public AssemblyLine getAssemblyLine() {
        return assemblyLine;
    }
    
    /**
     * Check if execution was successful.
     */
    public boolean isSuccess() {
        return status == ExecutionStatus.RUNNING;
    }
    
    /**
     * Check if execution should continue.
     */
    public boolean shouldContinue() {
        return status == ExecutionStatus.RUNNING;
    }
    
    @Override
    public String toString() {
        return String.format("ExecutionResult{status=%s, pc=0x%04X, message='%s'}", 
                           status, programCounter, message);
    }
}
