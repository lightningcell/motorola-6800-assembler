package assembler.simulator;

/**
 * Enumeration of execution states for the simulator.
 * 
 * @author Motorola 6800 Assembler Team
 */
public enum ExecutionStatus {
    /** Program is running normally */
    RUNNING,
    
    /** Program has halted (SWI, end of program, error) */
    HALTED,
    
    /** Execution stopped at a breakpoint */
    BREAKPOINT,
    
    /** Execution stopped by user request */
    STOPPED,
    
    /** Error occurred during execution */
    ERROR
}
