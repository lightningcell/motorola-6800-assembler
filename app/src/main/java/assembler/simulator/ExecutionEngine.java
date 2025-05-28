package assembler.simulator;

import assembler.core.*;
import assembler.parser.*;
import assembler.assembler.*;
import java.util.*;

/**
 * High-level execution engine for the Motorola 6800 simulator.
 * Manages program loading, execution control, and state monitoring.
 * 
 * Features:
 * - Program loading from assembled code
 * - Step-by-step execution
 * - Continuous execution with breakpoints
 * - Register and memory inspection
 * - Execution statistics
 * 
 * @author Motorola 6800 Assembler Team
 */
public class ExecutionEngine {
    
    private final CPU6800 cpu;
    private final CodeGenerator codeGenerator;
    private List<AssemblyLine> currentProgram;
    private Map<Integer, AssemblyLine> addressToLineMap;
    private int instructionsExecuted;
    private long startTime;
    
    public ExecutionEngine() {
        this.cpu = new CPU6800();
        this.codeGenerator = new CodeGenerator();
        this.currentProgram = new ArrayList<>();
        this.addressToLineMap = new HashMap<>();
        this.instructionsExecuted = 0;
    }
    
    /**
     * Load and assemble program for execution.
     * 
     * @param assemblyLines Parsed assembly lines
     * @throws CodeGenerationException if code generation fails
     */
    public void loadProgram(List<AssemblyLine> assemblyLines) throws CodeGenerationException {
        // Generate machine code
        Map<Integer, List<Integer>> machineCode = codeGenerator.generateCode(assemblyLines);
        
        // Store program information
        this.currentProgram = new ArrayList<>(assemblyLines);
        this.addressToLineMap = new HashMap<>();
        
        // Build address to line mapping
        for (AssemblyLine line : assemblyLines) {
            if (line.getAddress() >= 0) {
                addressToLineMap.put(line.getAddress(), line);
            }
        }
        
        // Load into CPU
        int startAddress = findStartAddress(assemblyLines);
        cpu.loadProgram(startAddress, machineCode);
        
        // Reset execution statistics
        instructionsExecuted = 0;
        startTime = System.currentTimeMillis();
    }
    
    /**
     * Find program start address (first ORG or address 0).
     */
    private int findStartAddress(List<AssemblyLine> assemblyLines) {
        for (AssemblyLine line : assemblyLines) {
            if (line.isPseudoOp() && "ORG".equals(line.getPseudoOp())) {
                return (Integer) line.getPseudoOperand();
            }
        }
        return 0; // Default start address
    }
    
    /**
     * Execute one instruction.
     * 
     * @return ExecutionResult containing status and information
     */
    public ExecutionResult step() {
        if (cpu.isHalted()) {
            return new ExecutionResult(ExecutionStatus.HALTED, getCurrentPC(), 
                                     "Program halted", getCurrentLine());
        }
        
        int pc = getCurrentPC();
        AssemblyLine currentLine = addressToLineMap.get(pc);
        
        boolean success = cpu.step();
        instructionsExecuted++;
        
        if (!success) {
            if (cpu.isHalted()) {
                return new ExecutionResult(ExecutionStatus.HALTED, pc, 
                                         "Program halted", currentLine);
            } else {
                return new ExecutionResult(ExecutionStatus.BREAKPOINT, pc, 
                                         "Breakpoint hit", currentLine);
            }
        }
        
        return new ExecutionResult(ExecutionStatus.RUNNING, pc, 
                                 "Instruction executed", currentLine);
    }
    
    /**
     * Run program until halted or breakpoint.
     * 
     * @return ExecutionResult with final status
     */
    public ExecutionResult run() {
        startTime = System.currentTimeMillis();
        
        while (!cpu.isHalted()) {
            ExecutionResult result = step();
            
            if (result.getStatus() != ExecutionStatus.RUNNING) {
                return result;
            }
        }
        
        return new ExecutionResult(ExecutionStatus.HALTED, getCurrentPC(), 
                                 "Program completed", getCurrentLine());
    }
    
    /**
     * Stop program execution.
     */
    public void stop() {
        cpu.stop();
    }
    
    /**
     * Reset CPU and program state.
     */
    public void reset() {
        cpu.reset();
        instructionsExecuted = 0;
        
        if (!currentProgram.isEmpty()) {
            try {
                loadProgram(currentProgram);
            } catch (CodeGenerationException e) {
                // Should not happen on reset
                System.err.println("Error resetting program: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get current program counter value.
     */
    public int getCurrentPC() {
        return cpu.getRegisters().getPC();
    }
    
    /**
     * Get assembly line at current PC.
     */
    public AssemblyLine getCurrentLine() {
        return addressToLineMap.get(getCurrentPC());
    }
    
    /**
     * Get assembly line at specified address.
     */
    public AssemblyLine getLineAtAddress(int address) {
        return addressToLineMap.get(address);
    }
    
    /**
     * Get all loaded assembly lines.
     */
    public List<AssemblyLine> getProgram() {
        return new ArrayList<>(currentProgram);
    }
    
    /**
     * Get CPU registers.
     */
    public Registers getRegisters() {
        return cpu.getRegisters();
    }
    
    /**
     * Get CPU memory.
     */
    public Memory getMemory() {
        return cpu.getMemory();
    }
    
    /**
     * Get execution statistics.
     */
    public ExecutionStatistics getStatistics() {
        long runtime = System.currentTimeMillis() - startTime;
        return new ExecutionStatistics(instructionsExecuted, runtime);
    }
    
    // Breakpoint management
    public void addBreakpoint(int address) {
        cpu.addBreakpoint(address);
    }
    
    public void removeBreakpoint(int address) {
        cpu.removeBreakpoint(address);
    }
    
    public void clearBreakpoints() {
        cpu.clearBreakpoints();
    }
    
    public Set<Integer> getBreakpoints() {
        return cpu.getBreakpoints();
    }
    
    /**
     * Check if program is loaded.
     */
    public boolean isProgramLoaded() {
        return !currentProgram.isEmpty();
    }
    
    /**
     * Check if CPU is running.
     */
    public boolean isRunning() {
        return cpu.isRunning();
    }
    
    /**
     * Check if program is halted.
     */
    public boolean isHalted() {
        return cpu.isHalted();
    }
}
