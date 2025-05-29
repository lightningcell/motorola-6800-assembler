# Simulator Paketi

`assembler.simulator` paketi, Motorola 6800 CPU'nun tam simülasyonunu gerçekleştirir. Machine code'u execute eder, register ve memory state'ini takip eder, ve debugging özellikleri sunar.

## 📦 Paket İçeriği

| Sınıf | Açıklama | Sorumluluk |
|-------|----------|------------|
| **ExecutionEngine** | Yüksek seviye execution kontrol | Program loading, execution management |
| **CPU6800** | Düşük seviye CPU simülasyonu | Instruction execution, state management |
| **ExecutionResult** | Execution sonuç bilgisi | Step execution results |
| **ExecutionStatistics** | Performance metrikleri | Execution statistics tracking |
| **ExecutionStatus** | Execution durumu enum | Running, halted, breakpoint, error states |

## 🖥️ CPU Simülasyon Mimarisi

### 1. Simülasyon Katmanları

```
┌─────────────────────────────────────┐
│        ExecutionEngine              │  ← High-level control
│  - Program loading                  │
│  - Breakpoint management            │  
│  - Statistics tracking              │
└─────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│            CPU6800                  │  ← Low-level simulation
│  - Instruction fetch/decode/execute │
│  - Register management              │
│  - Memory management                │
│  - Flag calculations                │
└─────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│      Memory + Registers             │  ← Hardware state
│  - 64KB memory space               │
│  - CPU register set                 │
│  - Condition flags                  │
└─────────────────────────────────────┘
```

### 2. Execution Cycle

```
1. FETCH    → Read instruction from PC address
2. DECODE   → Determine instruction and addressing mode  
3. EXECUTE  → Perform instruction operation
4. UPDATE   → Update registers, flags, PC
5. CHECK    → Breakpoint and halt conditions
```

## 🔧 Sınıf Detayları

### 1. ExecutionEngine (Yüksek Seviye Kontrol)

Program execution'ın orchestration'ını yapar:

```java
public class ExecutionEngine {
    private final CPU6800 cpu;
    private final CodeGenerator codeGenerator;
    private List<AssemblyLine> currentProgram;
    private Map<Integer, AssemblyLine> addressToLineMap;
    private int instructionsExecuted;
    private long startTime;
    
    // Program loading
    public void loadProgram(List<AssemblyLine> assemblyLines) throws CodeGenerationException;
    
    // Execution control
    public ExecutionResult step();                    // Single step
    public ExecutionResult run();                     // Run until halt/breakpoint
    public void reset();                              // Reset to initial state
    
    // State inspection
    public boolean isHalted();
    public Registers getRegisters();
    public Memory getMemory();
    public ExecutionStatistics getStatistics();
    
    // Debugging
    public void addBreakpoint(int address);
    public void removeBreakpoint(int address);
    public Set<Integer> getBreakpoints();
    public void clearBreakpoints();
}
```

**Key Methods:**

#### `loadProgram()` - Program Loading
```java
public void loadProgram(List<AssemblyLine> assemblyLines) throws CodeGenerationException {
    this.currentProgram = assemblyLines;
    
    // Generate machine code
    Map<Integer, List<Integer>> machineCode = codeGenerator.generateCode(assemblyLines);
    
    // Create address-to-line mapping for debugging
    addressToLineMap = new HashMap<>();
    for (AssemblyLine line : assemblyLines) {
        if (line.getAddress() > 0) {
            addressToLineMap.put(line.getAddress(), line);
        }
    }
    
    // Find program start address
    int startAddress = findProgramStart(assemblyLines);
    
    // Load into CPU
    cpu.loadProgram(startAddress, machineCode);
    cpu.getRegisters().setProgramCounter(startAddress);
    
    // Reset statistics
    instructionsExecuted = 0;
    startTime = System.currentTimeMillis();
}
```

#### `step()` - Single Step Execution
```java
public ExecutionResult step() {
    if (cpu.isHalted()) {
        return new ExecutionResult(ExecutionStatus.HALTED, 
                                 cpu.getRegisters().getProgramCounter(),
                                 "CPU is halted", null);
    }
    
    int pc = cpu.getRegisters().getProgramCounter();
    
    // Check for breakpoint
    if (cpu.getBreakpoints().contains(pc)) {
        return new ExecutionResult(ExecutionStatus.BREAKPOINT, pc,
                                 "Breakpoint at 0x" + Integer.toHexString(pc),
                                 addressToLineMap.get(pc));
    }
    
    // Execute one instruction
    boolean success = cpu.step();
    instructionsExecuted++;
    
    if (!success) {
        return new ExecutionResult(ExecutionStatus.HALTED, pc,
                                 "Program halted", 
                                 addressToLineMap.get(pc));
    }
    
    return new ExecutionResult(ExecutionStatus.RUNNING, pc,
                             "Instruction executed",
                             addressToLineMap.get(pc));
}
```

#### `run()` - Continuous Execution
```java
public ExecutionResult run() {
    ExecutionResult result;
    
    do {
        result = step();
        
        // Stop on breakpoint, halt, or error
        if (result.getStatus() != ExecutionStatus.RUNNING) {
            break;
        }
        
        // Safety check - prevent infinite loops
        if (instructionsExecuted > 1000000) {
            return new ExecutionResult(ExecutionStatus.ERROR,
                                     cpu.getRegisters().getProgramCounter(),
                                     "Execution limit exceeded (possible infinite loop)",
                                     null);
        }
        
    } while (result.getStatus() == ExecutionStatus.RUNNING);
    
    return result;
}
```

### 2. CPU6800 (Düşük Seviye CPU Simülasyonu)

Gerçek 6800 CPU'nun behavior'ını simüle eder:

```java
public class CPU6800 {
    private final Registers registers;
    private final Memory memory;
    private final Set<Integer> breakpoints;
    private boolean running;
    private boolean halted;
    
    // Core execution
    public boolean step();                           // Execute one instruction
    public void reset();                             // Reset CPU state
    
    // Program loading
    public void loadProgram(int address, Map<Integer, List<Integer>> code);
    
    // State management
    public boolean isHalted();
    public boolean isRunning();
    
    // Hardware access
    public Registers getRegisters();
    public Memory getMemory();
    public Set<Integer> getBreakpoints();
}
```

**Instruction Execution Engine:**

#### `step()` - Core Execution Loop
```java
public boolean step() {
    if (halted) return false;
    
    try {
        // 1. FETCH
        int pc = registers.getProgramCounter();
        int opcode = memory.readByte(pc);
        
        // 2. DECODE  
        InstructionInfo instruction = decodeInstruction(opcode);
        if (instruction == null) {
            halt("Unknown opcode: 0x" + Integer.toHexString(opcode));
            return false;
        }
        
        // 3. FETCH OPERANDS
        int operand = fetchOperand(pc + 1, instruction.getAddressingMode());
        
        // 4. EXECUTE
        executeInstruction(instruction, operand);
        
        // 5. UPDATE PC
        registers.setProgramCounter(pc + instruction.getSize());
        
        return true;
        
    } catch (Exception e) {
        halt("Execution error: " + e.getMessage());
        return false;
    }
}
```

#### `executeInstruction()` - Instruction Implementation
```java
private void executeInstruction(InstructionInfo instruction, int operand) {
    String mnemonic = instruction.getMnemonic();
    AddressingMode mode = instruction.getAddressingMode();
    
    switch (mnemonic) {
        case "LDA":  // Load Accumulator A
            executeLDA(operand, mode);
            break;
            
        case "STA":  // Store Accumulator A  
            executeSTA(operand, mode);
            break;
            
        case "ABA":  // Add B to A
            executeABA();
            break;
            
        case "JMP":  // Jump
            executeJMP(operand, mode);
            break;
            
        case "BEQ":  // Branch if Equal
            executeBEQ(operand);
            break;
            
        case "SWI":  // Software Interrupt
            executeSWI();
            break;
            
        // ... 72 total instructions
            
        default:
            throw new IllegalArgumentException("Unimplemented instruction: " + mnemonic);
    }
}
```

**Instruction Implementation Örnekleri:**

#### Load Accumulator A (LDA)
```java
private void executeLDA(int operand, AddressingMode mode) {
    int value;
    
    switch (mode) {
        case IMMEDIATE:
            value = operand;
            break;
            
        case DIRECT:
            value = memory.readByte(operand);
            break;
            
        case EXTENDED:
            value = memory.readByte(operand);
            break;
            
        case INDEXED:
            int effectiveAddress = (registers.getIndexRegister() + operand) & 0xFFFF;
            value = memory.readByte(effectiveAddress);
            break;
            
        default:
            throw new IllegalArgumentException("Invalid addressing mode for LDA: " + mode);
    }
    
    registers.setAccumulatorA(value & 0xFF);
    registers.updateFlags(value);  // Update N and Z flags
}
```

#### Add B to A (ABA)
```java
private void executeABA() {
    int a = registers.getAccumulatorA();
    int b = registers.getAccumulatorB();
    int result = a + b;
    
    // Update accumulator A
    registers.setAccumulatorA(result & 0xFF);
    
    // Update flags
    registers.setCarryFlag((result & 0x100) != 0);        // Carry
    registers.setOverflowFlag(isOverflow(a, b, result));   // Overflow
    registers.updateFlags(result & 0xFF);                  // N, Z flags
}
```

#### Branch if Equal (BEQ)
```java
private void executeBEQ(int offset) {
    if (registers.isZeroFlag()) {
        // Convert to signed 8-bit offset
        int signedOffset = (offset > 127) ? offset - 256 : offset;
        int newPC = (registers.getProgramCounter() + signedOffset) & 0xFFFF;
        registers.setProgramCounter(newPC);
    }
}
```

#### Software Interrupt (SWI)
```java
private void executeSWI() {
    // Push registers to stack (6800 behavior)
    pushToStack(registers.getProgramCounter() & 0xFF);        // PC low
    pushToStack((registers.getProgramCounter() >> 8) & 0xFF); // PC high
    pushToStack(registers.getIndexRegister() & 0xFF);         // X low
    pushToStack((registers.getIndexRegister() >> 8) & 0xFF);  // X high
    pushToStack(registers.getAccumulatorA());                 // A
    pushToStack(registers.getAccumulatorB());                 // B
    pushToStack(getConditionCodeRegister());                  // CCR
    
    // Set interrupt flag
    registers.setInterruptFlag(true);
    
    // For simulator, halt execution
    halt("SWI executed");
}
```

### 3. ExecutionResult (Execution Sonucu)

Step execution'ın sonucunu taşır:

```java
public class ExecutionResult {
    private final ExecutionStatus status;    // RUNNING, HALTED, BREAKPOINT, ERROR
    private final int programCounter;        // PC value
    private final String message;            // Descriptive message
    private final AssemblyLine assemblyLine; // Associated source line
    
    // Constructor ve getters
    public ExecutionResult(ExecutionStatus status, int programCounter, 
                          String message, AssemblyLine assemblyLine);
    
    // Status checking
    public boolean isRunning();
    public boolean isHalted();
    public boolean isBreakpoint();
    public boolean isError();
}
```

### 4. ExecutionStatistics (Performance Tracking)

Execution performance'ını takip eder:

```java
public class ExecutionStatistics {
    private final int instructionsExecuted;
    private final long executionTimeMs;
    
    public ExecutionStatistics(int instructionsExecuted, long executionTimeMs);
    
    // Metrics
    public int getInstructionsExecuted();
    public long getExecutionTimeMs();
    public double getInstructionsPerSecond();
    
    // Formatted output
    public String getFormattedStats();
}
```

**Statistics Example:**
```
Execution Statistics:
- Instructions executed: 1,250
- Execution time: 15 ms
- Instructions per second: 83,333
- Average instruction time: 12 μs
```

### 5. ExecutionStatus (Execution Durumu)

```java
public enum ExecutionStatus {
    RUNNING,     // Normal execution
    HALTED,      // Program halted (SWI, end)
    BREAKPOINT,  // Stopped at breakpoint
    STOPPED,     // User stopped
    ERROR        // Execution error
}
```

## 🧪 Simulation Örnekleri

### 1. Basic Program Execution

```assembly
        ORG $0200
        LDA #$FF         ; Load 255 into A
        LDB #$01         ; Load 1 into B  
        ABA              ; Add B to A (A = 255 + 1 = 0, Carry = 1)
        SWI              ; Halt
```

**Execution Trace:**
```
Step 1: PC=$0200, LDA #$FF → A=$FF, Flags: N=1, Z=0
Step 2: PC=$0202, LDB #$01 → B=$01
Step 3: PC=$0204, ABA → A=$00, Flags: N=0, Z=1, C=1, V=0
Step 4: PC=$0205, SWI → HALTED
```

### 2. Loop Example

```assembly
        ORG $0200
        LDA #$05         ; Counter = 5
LOOP    DEC A            ; Decrement A
        BNE LOOP         ; Branch if not zero
        SWI              ; Done
```

**Execution Trace:**
```
Step 1: PC=$0200, LDA #$05 → A=$05
Step 2: PC=$0202, DEC A → A=$04, Z=0
Step 3: PC=$0203, BNE LOOP → PC=$0202 (branch taken)
Step 4: PC=$0202, DEC A → A=$03, Z=0  
Step 5: PC=$0203, BNE LOOP → PC=$0202 (branch taken)
...
Step 10: PC=$0202, DEC A → A=$00, Z=1
Step 11: PC=$0203, BNE LOOP → PC=$0204 (branch not taken)
Step 12: PC=$0204, SWI → HALTED
```

### 3. Memory Operations

```assembly
        ORG $0200
        LDA #$AA         ; Load test pattern
        STA $0300        ; Store to memory
        LDB $0300        ; Load from memory to B
        CMP B            ; Compare A with B (should be equal)
        SWI
```

**Memory State:**
```
Before: Memory[$0300] = $00
After:  Memory[$0300] = $AA
Final:  A=$AA, B=$AA, Z=1 (equal)
```

## 🔧 Advanced Features

### 1. Breakpoint Management

```java
// Set breakpoint
cpu.addBreakpoint(0x0202);

// Execution stops when PC reaches breakpoint
ExecutionResult result = engine.run();
if (result.getStatus() == ExecutionStatus.BREAKPOINT) {
    System.out.println("Stopped at breakpoint: " + 
                      Integer.toHexString(result.getProgramCounter()));
}
```

### 2. Memory Watchpoints (Future Feature)

```java
// Watch memory location for changes
engine.addMemoryWatch(0x0300);

// Execution stops when memory changes
if (memory.readByte(0x0300) != previousValue) {
    return ExecutionResult.memoryWatchpoint(0x0300);
}
```

### 3. Step-by-Step Debugging

```java
// Single instruction execution
ExecutionResult result = engine.step();
System.out.println("Executed: " + result.getAssemblyLine().getSourceLine());
System.out.println("PC: 0x" + Integer.toHexString(result.getProgramCounter()));

// Register state
Registers regs = engine.getRegisters();
System.out.printf("A=%02X B=%02X X=%04X SP=%04X%n", 
                  regs.getAccumulatorA(), regs.getAccumulatorB(),
                  regs.getIndexRegister(), regs.getStackPointer());
```

## 🧪 Test Scenarios

### 1. Basic Instruction Test
```java
@Test
public void testLDAImmediate() {
    // Setup
    cpu.getMemory().writeByte(0x0200, 0x86);  // LDA #$FF opcode
    cpu.getMemory().writeByte(0x0201, 0xFF);  // operand
    cpu.getRegisters().setProgramCounter(0x0200);
    
    // Execute
    boolean success = cpu.step();
    
    // Verify
    assertTrue(success);
    assertEquals(0xFF, cpu.getRegisters().getAccumulatorA());
    assertEquals(0x0202, cpu.getRegisters().getProgramCounter());
    assertTrue(cpu.getRegisters().isNegativeFlag());
    assertFalse(cpu.getRegisters().isZeroFlag());
}
```

### 2. Branch Instruction Test
```java
@Test  
public void testBranchTaken() {
    // Setup BEQ with zero flag set
    cpu.getRegisters().setZeroFlag(true);
    cpu.getMemory().writeByte(0x0200, 0x27);  // BEQ opcode
    cpu.getMemory().writeByte(0x0201, 0x10);  // offset +16
    cpu.getRegisters().setProgramCounter(0x0200);
    
    // Execute
    cpu.step();
    
    // Verify branch taken
    assertEquals(0x0212, cpu.getRegisters().getProgramCounter()); // 0x0202 + 16
}
```

### 3. Full Program Test
```java
@Test
public void testCompleteProgram() throws Exception {
    String source = """
        ORG $0200
        LDA #$05
        STA $0300
        DEC $0300
        LDA $0300
        SWI
        """;
    
    List<AssemblyLine> program = parser.parseSource(source);
    engine.loadProgram(program);
    
    ExecutionResult result = engine.run();
    
    assertEquals(ExecutionStatus.HALTED, result.getStatus());
    assertEquals(0x04, engine.getMemory().readByte(0x0300));
    assertEquals(0x04, engine.getRegisters().getAccumulatorA());
}
```

## 🚀 Performance Considerations

### 1. Instruction Dispatch
```java
// Use lookup table instead of large switch statement
private final Map<Integer, InstructionHandler> instructionHandlers;

// Fast opcode dispatch
InstructionHandler handler = instructionHandlers.get(opcode);
if (handler != null) {
    handler.execute(operand);
}
```

### 2. Memory Access Optimization
```java
// Bounds checking only in debug mode
public int readByte(int address) {
    if (DEBUG_MODE && (address < 0 || address >= MEMORY_SIZE)) {
        throw new IndexOutOfBoundsException();
    }
    return memory[address];
}
```

### 3. Statistics Collection
```java
// Minimal overhead statistics
private long instructionCount;
private long startTime = System.nanoTime();

// Only calculate when requested
public double getInstructionsPerSecond() {
    long elapsed = System.nanoTime() - startTime;
    return (instructionCount * 1_000_000_000.0) / elapsed;
}
```

---

Simulator paketi, assembly programlarının gerçekçi execution'ı için kritik bir bileşendir. 6800 CPU'nun tüm özelliklerini doğru şekilde simüle eder.
