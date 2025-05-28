package assembler.simulator;

/**
 * Execution statistics for performance monitoring.
 * Tracks instruction count, execution time, and other metrics.
 * 
 * @author Motorola 6800 Assembler Team
 */
public class ExecutionStatistics {
    
    private final int instructionsExecuted;
    private final long executionTimeMs;
    
    /**
     * Create new execution statistics.
     * 
     * @param instructionsExecuted Number of instructions executed
     * @param executionTimeMs Execution time in milliseconds
     */
    public ExecutionStatistics(int instructionsExecuted, long executionTimeMs) {
        this.instructionsExecuted = instructionsExecuted;
        this.executionTimeMs = executionTimeMs;
    }
    
    /**
     * Get number of instructions executed.
     */
    public int getInstructionsExecuted() {
        return instructionsExecuted;
    }
    
    /**
     * Get execution time in milliseconds.
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    /**
     * Calculate instructions per second.
     */
    public double getInstructionsPerSecond() {
        if (executionTimeMs == 0) {
            return 0.0;
        }
        return (instructionsExecuted * 1000.0) / executionTimeMs;
    }
    
    /**
     * Get formatted statistics string.
     */
    public String getFormattedStats() {
        return String.format(
            "Instructions: %d, Time: %d ms, Rate: %.2f inst/sec",
            instructionsExecuted, executionTimeMs, getInstructionsPerSecond()
        );
    }
    
    @Override
    public String toString() {
        return getFormattedStats();
    }
}
