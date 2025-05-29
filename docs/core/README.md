# Core Paketi

`assembler.core` paketi, projenin temel veri yapılarını ve modellerini içerir. Bu paket, projenin foundation'ını oluşturur ve diğer tüm paketler tarafından kullanılır.

## 📦 Paket İçeriği

| Sınıf | Açıklama | Sorumluluk |
|-------|----------|------------|
| **AddressingMode** | Enum - 7 addressing mode | 6800'ün addressing mode'larını tanımlar |
| **Instruction** | Model - Assembly instruction | Instruction bilgilerini saklar |
| **Label** | Model - Symbolic address | Label tanımları ve referansları |
| **Memory** | Model - 64KB memory space | CPU memory simülasyonu |
| **Registers** | Model - CPU register set | CPU register'larını saklar |

## 🎯 Temel Kavramlar

### 1. AddressingMode (Enum)

Motorola 6800'ün 7 addressing mode'unu tanımlar:

```java
public enum AddressingMode {
    INHERENT,    // NOP, RTS - operand yok
    IMMEDIATE,   // LDA #$FF - sabit değer
    DIRECT,      // LDA $80 - zero page (0-255)
    EXTENDED,    // LDA $1234 - 16-bit address  
    INDEXED,     // LDA $10,X - X register + offset
    RELATIVE,    // BRA LOOP - relative branching
    PSEUDO       // ORG, END - assembler directives
}
```

**Kullanım Örneği:**
```java
// Parser'da addressing mode belirleme
if (operand.startsWith("#")) {
    return AddressingMode.IMMEDIATE;
} else if (operand.matches("\\$[0-9A-F]{1,2}")) {
    return AddressingMode.DIRECT;
}
```

### 2. Instruction (Model)

Assembly instruction'ları temsil eder:

```java
public class Instruction {
    private final String mnemonic;           // "LDA", "STA", etc.
    private final AddressingMode addressingMode;
    private String operand;                  // "$FF", "LOOP", etc.
    private int resolvedOperand;             // Numeric değer
    private final int opcode;                // Machine code
    private final int cycles;                // Execution cycle count
    private final String description;        // İnsan okunabilir açıklama
}
```

**Yaşam Döngüsü:**
1. **Parse Time:** Mnemonic ve operand string ile oluşturulur
2. **Address Calculation:** Address'i hesaplanır
3. **Label Resolution:** Operand resolve edilir  
4. **Code Generation:** Machine code'a çevrilir

**Kullanım Örnekleri:**
```java
// Parser'da instruction oluşturma
Instruction inst = new Instruction("LDA", AddressingMode.IMMEDIATE, "#$FF");

// Code generator'da machine code alma
int opcode = opcodeTable.getInstruction("LDA", AddressingMode.IMMEDIATE).getOpcode();
```

### 3. Label (Model)

Symbolic address'leri temsil eder:

```java
public class Label {
    private final String name;      // "START", "LOOP", etc.
    private int address;            // Resolved address
    private final int lineNumber;   // Source line number
    private boolean resolved;       // Resolution durumu
}
```

**Label Lifecycle:**
```
Definition: "LOOP:" → Label("LOOP", lineNumber)
    ↓
Address Calculation: label.setAddress(0x0200)  
    ↓
Resolution: "JMP LOOP" → resolvedOperand = 0x0200
```

**Kullanım Örnekleri:**
```java
// Label tanımlama
Label startLabel = new Label("START", 10);
startLabel.setAddress(0x0200);

// Label referansı
if (!label.isResolved()) {
    throw new ParseException("Unresolved label: " + label.getName());
}
```

### 4. Memory (Model)

64KB memory space simülasyonu:

```java
public class Memory {
    public static final int MEMORY_SIZE = 65536;    // 64KB
    public static final int ZERO_PAGE_SIZE = 256;   // 0x00-0xFF
    public static final int RESET_VECTOR = 0xFFFE;  // Reset vector
    
    private final int[] memory;  // Memory array
}
```

**Memory Layout:**
```
$0000-$00FF: Zero Page (Direct addressing)
$0100-$01FF: Stack Area (default)  
$0200-$FFFD: User Program Area
$FFFE-$FFFF: Reset Vector
```

**Key Methods:**
```java
// Byte operations
public int readByte(int address);
public void writeByte(int address, int value);

// Word operations (16-bit)
public int readWord(int address);
public void writeWord(int address, int value);

// Bulk operations
public void loadProgram(int startAddress, byte[] program);
public void clear();
```

**Kullanım Örnekleri:**
```java
// Program loading
memory.writeWord(Memory.RESET_VECTOR, 0x0200);  // Set reset vector
memory.loadProgram(0x0200, machineCode);         // Load program

// Runtime memory access
int value = memory.readByte(0x0300);
memory.writeByte(0x0300, 0xFF);
```

### 5. Registers (Model)

Motorola 6800 CPU register seti:

```java
public class Registers {
    // 8-bit registers
    private int accumulatorA;    // A register
    private int accumulatorB;    // B register
    
    // 16-bit registers
    private int indexRegister;   // X register
    private int stackPointer;    // SP register  
    private int programCounter;  // PC register
    
    // Condition Code flags (8-bit CCR)
    private boolean carryFlag;      // C
    private boolean overflowFlag;   // V
    private boolean zeroFlag;       // Z
    private boolean negativeFlag;   // N
    private boolean interruptFlag;  // I
    private boolean halfCarryFlag;  // H
}
```

**Register Functions:**
```java
// Accumulator operations
public int getAccumulatorA();
public void setAccumulatorA(int value);
public int getAccumulatorB();  
public void setAccumulatorB(int value);

// 16-bit register operations
public int getProgramCounter();
public void setProgramCounter(int value);
public int getStackPointer();
public void setStackPointer(int value);

// Flag operations
public boolean isZeroFlag();
public void setZeroFlag(boolean flag);
public void updateFlags(int result);  // Auto flag update
```

**Flag Update Logic:**
```java
public void updateFlags(int result) {
    setZeroFlag((result & 0xFF) == 0);
    setNegativeFlag((result & 0x80) != 0);
    // Carry ve Overflow context'e göre set edilir
}
```

## 🔗 Sınıflar Arası İlişkiler

### Dependency Graph
```
Memory ←─── CPU6800 ───→ Registers
  ↑                         ↑
  │                         │
ExecutionEngine ←─── Instruction
  ↑                         ↑
  │                         │
App ←─────────────── AssemblyLine
                           ↑
                         Label
```

### Data Flow
```
Assembly Source
    ↓
AssemblyLine (contains Instruction + Label)
    ↓  
Address Resolution
    ↓
Machine Code Generation
    ↓
Memory Loading (Memory class)
    ↓
CPU Execution (Registers class)
```

## 🎨 Design Patterns

### 1. **Value Objects**
- **AddressingMode, Instruction, Label** - Immutable değerler
- **Memory, Registers** - Mutable state holders

### 2. **Encapsulation**
```java
// Memory bounds checking
public int readByte(int address) {
    if (address < 0 || address >= MEMORY_SIZE) {
        throw new IndexOutOfBoundsException("Invalid memory address: " + address);
    }
    return memory[address];
}
```

### 3. **Builder Pattern (gelecek)**
```java
// Complex instruction building için
Instruction inst = new InstructionBuilder()
    .mnemonic("LDA")
    .addressingMode(AddressingMode.EXTENDED)
    .operand("$1234")
    .build();
```

## 🧪 Test Örnekleri

### Memory Test
```java
Memory memory = new Memory();
memory.writeByte(0x0200, 0x86);  // LDA #$FF opcode
memory.writeByte(0x0201, 0xFF);  // operand
assertEquals(0x86, memory.readByte(0x0200));
```

### Registers Test
```java
Registers regs = new Registers();
regs.setAccumulatorA(0xFF);
regs.updateFlags(0xFF);
assertTrue(regs.isNegativeFlag());
assertFalse(regs.isZeroFlag());
```

### Label Resolution Test
```java
Label label = new Label("START", 10);
label.setAddress(0x0200);
assertTrue(label.isResolved());
assertEquals(0x0200, label.getAddress());
```

## 🚀 Genişletme Önerileri

### 1. **Memory Management**
```java
// Memory mapping için
public class MemoryMapper {
    private final Map<Range, MemoryDevice> mappings;
    // ROM/RAM/IO mapping desteği
}
```

### 2. **Register Extensions**
```java
// 6802/6809 desteği için
public class ExtendedRegisters extends Registers {
    private int directPageRegister;  // 6809 DP register
    // Extended register set
}
```

### 3. **Debugging Support**
```java
// Debug bilgileri için
public class DebuggingInfo {
    private final Map<Integer, String> addressLabels;
    private final Map<Integer, AssemblyLine> sourceMapping;
}
```

---

Bu core paket, tüm projenin temelini oluşturur. Değişiklik yaparken backward compatibility'yi korumaya dikkat edin.
