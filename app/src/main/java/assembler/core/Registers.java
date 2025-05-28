package assembler.core;

/**
 * Represents the register set of the Motorola 6800 processor
 * 
 * The 6800 has the following registers:
 * - A: 8-bit accumulator A
 * - B: 8-bit accumulator B  
 * - X: 16-bit index register
 * - S: 16-bit stack pointer
 * - PC: 16-bit program counter
 * - CCR: 8-bit condition code register (flags)
 */
public class Registers {
    
    // 8-bit accumulators
    private int accumulatorA;
    private int accumulatorB;
    
    // 16-bit registers  
    private int indexRegister;    // X
    private int stackPointer;     // S
    private int programCounter;   // PC
    
    // 8-bit condition code register flags
    private boolean carryFlag;        // C
    private boolean overflowFlag;     // V
    private boolean zeroFlag;         // Z
    private boolean negativeFlag;     // N
    private boolean interruptFlag;    // I
    private boolean halfCarryFlag;    // H
    
    /**
     * Creates a new register set with default values
     */
    public Registers() {
        reset();
    }
    
    /**
     * Resets all registers to their default power-on state
     */
    public void reset() {
        accumulatorA = 0x00;
        accumulatorB = 0x00;
        indexRegister = 0x0000;
        stackPointer = 0x00FF;     // Stack starts at top of zero page
        programCounter = 0x0000;   // Will be loaded from reset vector
        
        // Clear all flags
        carryFlag = false;
        overflowFlag = false;
        zeroFlag = false;
        negativeFlag = false;
        interruptFlag = true;      // Interrupts disabled on reset
        halfCarryFlag = false;
    }
    
    // Accumulator A methods
    public int getAccumulatorA() { return accumulatorA; }
    public void setAccumulatorA(int value) { accumulatorA = value & 0xFF; }
    
    // Accumulator B methods
    public int getAccumulatorB() { return accumulatorB; }
    public void setAccumulatorB(int value) { accumulatorB = value & 0xFF; }
    
    // Index register methods
    public int getIndexRegister() { return indexRegister; }
    public void setIndexRegister(int value) { indexRegister = value & 0xFFFF; }
    
    // Stack pointer methods
    public int getStackPointer() { return stackPointer; }
    public void setStackPointer(int value) { stackPointer = value & 0xFFFF; }
    
    // Program counter methods
    public int getProgramCounter() { return programCounter; }
    public void setProgramCounter(int value) { programCounter = value & 0xFFFF; }
    
    // Condition code register flag methods
    public boolean getCarryFlag() { return carryFlag; }
    public void setCarryFlag(boolean flag) { carryFlag = flag; }
    
    public boolean getOverflowFlag() { return overflowFlag; }
    public void setOverflowFlag(boolean flag) { overflowFlag = flag; }
    
    public boolean getZeroFlag() { return zeroFlag; }
    public void setZeroFlag(boolean flag) { zeroFlag = flag; }
    
    public boolean getNegativeFlag() { return negativeFlag; }
    public void setNegativeFlag(boolean flag) { negativeFlag = flag; }
    
    public boolean getInterruptFlag() { return interruptFlag; }
    public void setInterruptFlag(boolean flag) { interruptFlag = flag; }
    
    public boolean getHalfCarryFlag() { return halfCarryFlag; }
    public void setHalfCarryFlag(boolean flag) { halfCarryFlag = flag; }
    
    /**
     * Gets the condition code register as a single byte
     * Bit layout: [H I N Z V C] (bits 7-0)
     * @return CCR value as 8-bit integer
     */
    public int getConditionCodeRegister() {
        int ccr = 0;
        if (halfCarryFlag) ccr |= 0x20;    // Bit 5
        if (interruptFlag) ccr |= 0x10;    // Bit 4  
        if (negativeFlag) ccr |= 0x08;     // Bit 3
        if (zeroFlag) ccr |= 0x04;         // Bit 2
        if (overflowFlag) ccr |= 0x02;     // Bit 1
        if (carryFlag) ccr |= 0x01;        // Bit 0
        return ccr;
    }
    
    /**
     * Sets the condition code register from a single byte
     * @param ccr The CCR value to set
     */
    public void setConditionCodeRegister(int ccr) {
        halfCarryFlag = (ccr & 0x20) != 0;
        interruptFlag = (ccr & 0x10) != 0;
        negativeFlag = (ccr & 0x08) != 0;
        zeroFlag = (ccr & 0x04) != 0;
        overflowFlag = (ccr & 0x02) != 0;
        carryFlag = (ccr & 0x01) != 0;
    }
    
    /**
     * Updates the N and Z flags based on a result value
     * @param result The 8-bit result to test
     */
    public void updateNZFlags(int result) {
        result &= 0xFF;
        negativeFlag = (result & 0x80) != 0;  // Bit 7 set = negative
        zeroFlag = (result == 0);             // Result is zero
    }
    
    /**
     * Gets a formatted string representation of all registers
     * @return Formatted register display
     */
    @Override
    public String toString() {
        return String.format(
            "A:%02X B:%02X X:%04X S:%04X PC:%04X CCR:%02X [%s%s%s%s%s%s]",
            accumulatorA, accumulatorB, indexRegister, stackPointer, programCounter,
            getConditionCodeRegister(),
            halfCarryFlag ? "H" : "h",
            interruptFlag ? "I" : "i", 
            negativeFlag ? "N" : "n",
            zeroFlag ? "Z" : "z",
            overflowFlag ? "V" : "v",
            carryFlag ? "C" : "c"
        );
    }
    
    // Short alias methods for convenience (used by CPU simulation)
    public int getA() { return getAccumulatorA(); }
    public void setA(int value) { setAccumulatorA(value); }
    
    public int getB() { return getAccumulatorB(); }
    public void setB(int value) { setAccumulatorB(value); }
    
    public int getX() { return getIndexRegister(); }
    public void setX(int value) { setIndexRegister(value); }
    
    public int getSP() { return getStackPointer(); }
    public void setSP(int value) { setStackPointer(value); }
    
    public int getPC() { return getProgramCounter(); }
    public void setPC(int value) { setProgramCounter(value); }
    
    // Flag alias methods
    public boolean isCarryFlag() { return getCarryFlag(); }
    public boolean isOverflowFlag() { return getOverflowFlag(); }
    public boolean isZeroFlag() { return getZeroFlag(); }
    public boolean isNegativeFlag() { return getNegativeFlag(); }
    public boolean isInterruptFlag() { return getInterruptFlag(); }
    public boolean isHalfCarryFlag() { return getHalfCarryFlag(); }
}
