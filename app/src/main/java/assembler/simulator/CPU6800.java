package assembler.simulator;

import assembler.core.*;
import java.util.*;

/**
 * Motorola 6800 CPU simulator.
 * Provides execution engine for running assembled 6800 machine code.
 * 
 * Features:
 * - Full register set simulation (A, B, X, SP, PC, CC)
 * - 64KB memory space
 * - Instruction execution with proper flag updates
 * - Step-by-step debugging support
 * - Breakpoint management
 * 
 * @author Motorola 6800 Assembler Team
 */
public class CPU6800 {
    
    private final Registers registers;
    private final Memory memory;
    private final Set<Integer> breakpoints;
    private boolean running;
    private boolean halted;
    
    public CPU6800() {
        this.registers = new Registers();
        this.memory = new Memory();
        this.breakpoints = new HashSet<>();
        this.running = false;
        this.halted = false;
    }
    
    /**
     * Load machine code into memory at specified address.
     * 
     * @param address Starting address
     * @param code Map of addresses to machine code bytes
     */
    public void loadProgram(int address, Map<Integer, List<Integer>> code) {
        memory.clear();
        
        for (Map.Entry<Integer, List<Integer>> entry : code.entrySet()) {
            int addr = entry.getKey();
            List<Integer> bytes = entry.getValue();
            
            for (int i = 0; i < bytes.size(); i++) {
                memory.writeByte(addr + i, bytes.get(i));
            }
        }
        
        // Set program counter to start address
        registers.setPC(address);
        halted = false;
    }
    
    /**
     * Execute one instruction and update CPU state.
     * 
     * @return true if instruction executed successfully, false if halted
     */
    public boolean step() {
        if (halted) {
            return false;
        }
        
        int pc = registers.getPC();
        
        // Check for breakpoint
        if (breakpoints.contains(pc)) {
            running = false;
            return false;
        }
        
        // Fetch instruction opcode
        int opcode = memory.readByte(pc);
        registers.setPC(pc + 1);
        
        // Execute instruction based on opcode
        return executeInstruction(opcode);
    }
    
    /**
     * Run program until halted or breakpoint hit.
     */
    public void run() {
        running = true;
        while (running && !halted) {
            if (!step()) {
                break;
            }
        }
    }
    
    /**
     * Stop program execution.
     */
    public void stop() {
        running = false;
    }
    
    /**
     * Reset CPU to initial state.
     */
    public void reset() {
        registers.reset();
        halted = false;
        running = false;
    }
    
    /**
     * Execute instruction based on opcode.
     * This is a simplified implementation covering key instructions.
     */
    private boolean executeInstruction(int opcode) {
        int pc = registers.getPC();
        
        switch (opcode) {
            // LDA (Load Accumulator A)
            case 0x86: // LDA Immediate
                registers.setA(memory.readByte(pc));
                registers.setPC(pc + 1);
                updateNZFlags(registers.getA());
                break;
                
            case 0x96: // LDA Direct
                int directAddr = memory.readByte(pc);
                registers.setA(memory.readByte(directAddr));
                registers.setPC(pc + 1);
                updateNZFlags(registers.getA());
                break;
                
            case 0xB6: // LDA Extended
                int extAddr = (memory.readByte(pc) << 8) | memory.readByte(pc + 1);
                registers.setA(memory.readByte(extAddr));
                registers.setPC(pc + 2);
                updateNZFlags(registers.getA());
                break;
                
            case 0xA6: // LDA Indexed
                int indexedOffset = memory.readByte(pc);
                int indexedAddr = (registers.getX() + indexedOffset) & 0xFFFF;
                registers.setA(memory.readByte(indexedAddr));
                registers.setPC(pc + 1);
                updateNZFlags(registers.getA());
                break;
                
            // STA (Store Accumulator A)
            case 0x97: // STA Direct
                int staDirectAddr = memory.readByte(pc);
                memory.writeByte(staDirectAddr, registers.getA());
                registers.setPC(pc + 1);
                updateNZFlags(registers.getA());
                break;
                
            case 0xB7: // STA Extended
                int staExtAddr = (memory.readByte(pc) << 8) | memory.readByte(pc + 1);
                memory.writeByte(staExtAddr, registers.getA());
                registers.setPC(pc + 2);
                updateNZFlags(registers.getA());
                break;
                
            case 0xA7: // STA Indexed
                int staIndexedOffset = memory.readByte(pc);
                int staIndexedAddr = (registers.getX() + staIndexedOffset) & 0xFFFF;
                memory.writeByte(staIndexedAddr, registers.getA());
                registers.setPC(pc + 1);
                updateNZFlags(registers.getA());
                break;
                
            // ADD (Add to Accumulator A)
            case 0x8B: // ADDA Immediate
                int addImm = memory.readByte(pc);
                int addResult = registers.getA() + addImm;
                updateArithmeticFlags(registers.getA(), addImm, addResult);
                registers.setA(addResult & 0xFF);
                registers.setPC(pc + 1);
                break;
                
            // SUB (Subtract from Accumulator A)
            case 0x80: // SUBA Immediate
                int subImm = memory.readByte(pc);
                int subResult = registers.getA() - subImm;
                updateSubtractionFlags(registers.getA(), subImm, subResult);
                registers.setA(subResult & 0xFF);
                registers.setPC(pc + 1);
                break;
                
            // CMP (Compare Accumulator A)
            case 0x81: // CMPA Immediate
                int cmpImm = memory.readByte(pc);
                int cmpResult = registers.getA() - cmpImm;
                updateSubtractionFlags(registers.getA(), cmpImm, cmpResult);
                registers.setPC(pc + 1);
                break;
                
            // Branch Instructions
            case 0x20: // BRA (Branch Always)
                int braOffset = (byte) memory.readByte(pc); // Sign extend
                registers.setPC((pc + 1 + braOffset) & 0xFFFF);
                break;
                
            case 0x27: // BEQ (Branch if Equal)
                int beqOffset = (byte) memory.readByte(pc);
                if (registers.isZeroFlag()) {
                    registers.setPC((pc + 1 + beqOffset) & 0xFFFF);
                } else {
                    registers.setPC(pc + 1);
                }
                break;
                
            case 0x26: // BNE (Branch if Not Equal)
                int bneOffset = (byte) memory.readByte(pc);
                if (!registers.isZeroFlag()) {
                    registers.setPC((pc + 1 + bneOffset) & 0xFFFF);
                } else {
                    registers.setPC(pc + 1);
                }
                break;
                
            // Jump Instructions
            case 0x7E: // JMP Extended
                int jmpAddr = (memory.readByte(pc) << 8) | memory.readByte(pc + 1);
                registers.setPC(jmpAddr);
                break;
                
            case 0x6E: // JMP Indexed
                int jmpOffset = memory.readByte(pc);
                int jmpIndexedAddr = (registers.getX() + jmpOffset) & 0xFFFF;
                registers.setPC(jmpIndexedAddr);
                break;
                
            // Stack Operations
            case 0x36: // PSH A (Push Accumulator A)
                pushByte(registers.getA());
                break;
                
            case 0x32: // PUL A (Pull Accumulator A)
                registers.setA(pullByte());
                updateNZFlags(registers.getA());
                break;
                
            // Miscellaneous
            case 0x01: // NOP (No Operation)
                // Do nothing
                break;
                
            case 0x3F: // SWI (Software Interrupt) - treat as halt
                halted = true;
                return false;
                
            case 0x39: // RTS (Return from Subroutine)
                int returnAddr = (pullByte() << 8) | pullByte();
                registers.setPC(returnAddr);
                break;
                
            default:
                // Unknown instruction - halt
                System.err.println("Unknown opcode: 0x" + Integer.toHexString(opcode) + " at PC=0x" + Integer.toHexString(pc - 1));
                halted = true;
                return false;
        }
        
        return true;
    }
    
    /**
     * Update N and Z flags based on result.
     */
    private void updateNZFlags(int result) {
        result &= 0xFF; // Ensure 8-bit result
        registers.setNegativeFlag((result & 0x80) != 0);
        registers.setZeroFlag(result == 0);
    }
    
    /**
     * Update flags after arithmetic operation (addition).
     */
    private void updateArithmeticFlags(int operand1, int operand2, int result) {
        // Carry flag
        registers.setCarryFlag((result & 0x100) != 0);
        
        // Overflow flag (signed arithmetic)
        boolean overflow = ((operand1 & 0x80) == (operand2 & 0x80)) && 
                          ((operand1 & 0x80) != (result & 0x80));
        registers.setOverflowFlag(overflow);
        
        // N and Z flags
        updateNZFlags(result);
    }
    
    /**
     * Update flags after subtraction operation.
     */
    private void updateSubtractionFlags(int operand1, int operand2, int result) {
        // Carry flag (borrow)
        registers.setCarryFlag(result < 0);
        
        // Overflow flag
        boolean overflow = ((operand1 & 0x80) != (operand2 & 0x80)) && 
                          ((operand1 & 0x80) != (result & 0x80));
        registers.setOverflowFlag(overflow);
        
        // N and Z flags
        updateNZFlags(result);
    }
    
    /**
     * Push byte onto stack.
     */
    private void pushByte(int value) {
        int sp = registers.getSP();
        memory.writeByte(sp, value & 0xFF);
        registers.setSP((sp - 1) & 0xFFFF);
    }
    
    /**
     * Pull byte from stack.
     */
    private int pullByte() {
        int sp = (registers.getSP() + 1) & 0xFFFF;
        registers.setSP(sp);
        return memory.readByte(sp);
    }
    
    // Breakpoint management
    public void addBreakpoint(int address) {
        breakpoints.add(address);
    }
    
    public void removeBreakpoint(int address) {
        breakpoints.remove(address);
    }
    
    public void clearBreakpoints() {
        breakpoints.clear();
    }
    
    public Set<Integer> getBreakpoints() {
        return new HashSet<>(breakpoints);
    }
    
    // Getters
    public Registers getRegisters() {
        return registers;
    }
    
    public Memory getMemory() {
        return memory;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public boolean isHalted() {
        return halted;
    }
}
