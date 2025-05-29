# Assembler Paketi

`assembler.assembler` paketi, parse edilmiş assembly kodunu machine code'a çeviren code generation işlemlerinden sorumludur. İki geçişli assembly algoritmasının ikinci fazını gerçekleştirir.

## 📦 Paket İçeriği

| Sınıf | Açıklama | Sorumluluk |
|-------|----------|------------|
| **CodeGenerator** | Ana code generation sınıfı | Assembly'den machine code üretir |
| **OpcodeTable** | Motorola 6800 opcode tablosu | 72 instruction, 197 opcode desteği |
| **CodeGenerationException** | Code generation hataları | Machine code üretim hatalarını yönetir |

## ⚙️ Code Generation Süreci

### 1. Code Generation Akışı

```
Resolved AssemblyLine objects
    │
    ▼
CodeGenerator.generateCode()
    │
    ├─► OpcodeTable.getInstruction() → Opcode lookup
    ├─► Operand encoding → Binary format
    └─► Address calculation → Final machine code
    │
    ▼
Map<Integer, List<Integer>> machineCode
```

### 2. Machine Code Format

Her instruction için üretilen machine code:
```
[Opcode] [Operand Low] [Operand High]  // Extended addressing
[Opcode] [Operand]                     // Direct/Immediate  
[Opcode]                               // Inherent
```

## 🔧 Sınıf Detayları

### 1. CodeGenerator (Ana Code Generator)

Machine code üretiminin ana logic'ini içerir:

```java
public class CodeGenerator {
    private final OpcodeTable opcodeTable;
    private int currentAddress;
    
    // Ana code generation metodu
    public Map<Integer, List<Integer>> generateCode(List<AssemblyLine> assemblyLines) 
            throws CodeGenerationException;
    
    // Tek instruction için code generation
    private List<Integer> generateInstructionBytes(AssemblyLine line);
    
    // Operand encoding
    private List<Integer> encodeOperand(Instruction instruction);
}
```

**Key Methods:**

#### `generateCode()` - Ana Method
```java
public Map<Integer, List<Integer>> generateCode(List<AssemblyLine> assemblyLines) 
        throws CodeGenerationException {
    Map<Integer, List<Integer>> result = new HashMap<>();
    currentAddress = 0;
    
    for (AssemblyLine line : assemblyLines) {
        // Pseudo-instruction handling
        if (line.isPseudoOp()) {
            handlePseudoInstruction(line, result);
            continue;
        }
        
        // Regular instruction handling  
        if (line.getInstruction() != null) {
            List<Integer> bytes = generateInstructionBytes(line);
            result.put(line.getAddress(), bytes);
        }
        
        currentAddress = line.getAddress() + calculateLineSize(line);
    }
    
    return result;
}
```

#### `generateInstructionBytes()` - Instruction Encoding
```java
private List<Integer> generateInstructionBytes(AssemblyLine line) 
        throws CodeGenerationException {
    Instruction instruction = line.getInstruction();
    List<Integer> bytes = new ArrayList<>();
    
    // 1. Opcode lookup
    Instruction opcodeInfo = opcodeTable.getInstruction(
        instruction.getMnemonic(), 
        instruction.getAddressingMode()
    );
    
    if (opcodeInfo == null) {
        throw new CodeGenerationException(
            "No opcode found for " + instruction.getMnemonic() + 
            " with " + instruction.getAddressingMode() + " addressing"
        );
    }
    
    bytes.add(opcodeInfo.getOpcode());
    
    // 2. Operand encoding
    bytes.addAll(encodeOperand(instruction));
    
    return bytes;
}
```

#### `encodeOperand()` - Operand Encoding
```java
private List<Integer> encodeOperand(Instruction instruction) 
        throws CodeGenerationException {
    List<Integer> operandBytes = new ArrayList<>();
    AddressingMode mode = instruction.getAddressingMode();
    int operandValue = instruction.getResolvedOperand();
    
    switch (mode) {
        case INHERENT:
            // No operand bytes
            break;
            
        case IMMEDIATE:
        case DIRECT:
        case INDEXED:
        case RELATIVE:
            // 8-bit operand
            if (operandValue < 0 || operandValue > 255) {
                throw new CodeGenerationException(
                    "Operand value out of range for " + mode + ": " + operandValue
                );
            }
            operandBytes.add(operandValue & 0xFF);
            break;
            
        case EXTENDED:
            // 16-bit operand (high byte first - big endian)
            if (operandValue < 0 || operandValue > 65535) {
                throw new CodeGenerationException(
                    "Address out of range for extended addressing: " + operandValue
                );
            }
            operandBytes.add((operandValue >> 8) & 0xFF);  // High byte
            operandBytes.add(operandValue & 0xFF);         // Low byte
            break;
            
        default:
            throw new CodeGenerationException("Unsupported addressing mode: " + mode);
    }
    
    return operandBytes;
}
```

### 2. OpcodeTable (Opcode Lookup Tablosu)

Motorola 6800'ün tam instruction set'ini içerir:

```java
public class OpcodeTable {
    private final Map<String, Instruction> instructionMap;  // Key: "LDA_IMMEDIATE"
    
    // Constructor'da tüm instruction'ları initialize eder
    public OpcodeTable();
    
    // Instruction lookup
    public Instruction getInstruction(String mnemonic, AddressingMode mode);
    
    // Opcode'dan instruction bulma (simulation için)
    public Instruction getInstructionByOpcode(int opcode);
    
    // Debug/UI için
    public Collection<Instruction> getAllInstructions();
    public Set<String> getSupportedMnemonics();
}
```

**Instruction Initialize Örneği:**
```java
private void initializeInstructions() {
    // Load Accumulator A
    addInstruction("LDA", AddressingMode.IMMEDIATE, 0x86, 2, "Load Accumulator A immediate");
    addInstruction("LDA", AddressingMode.DIRECT,    0x96, 3, "Load Accumulator A direct");
    addInstruction("LDA", AddressingMode.INDEXED,   0xA6, 5, "Load Accumulator A indexed");
    addInstruction("LDA", AddressingMode.EXTENDED,  0xB6, 4, "Load Accumulator A extended");
    
    // Store Accumulator A
    addInstruction("STA", AddressingMode.DIRECT,    0x97, 4, "Store Accumulator A direct");
    addInstruction("STA", AddressingMode.INDEXED,   0xA7, 6, "Store Accumulator A indexed");
    addInstruction("STA", AddressingMode.EXTENDED,  0xB7, 5, "Store Accumulator A extended");
    
    // Add
    addInstruction("ABA", AddressingMode.INHERENT,  0x1B, 2, "Add Accumulator B to A");
    addInstruction("ADC", AddressingMode.IMMEDIATE, 0x89, 2, "Add with Carry immediate");
    // ... 197 total opcodes
}
```

**Full Motorola 6800 Instruction Set:**
```java
// Arithmetic Instructions (16)
ABA, ABX, ADC, ADD, ASL, ASR, CLC, CLI, CLR, CLV, CMP, COM, CPX, DAA, DAS, DEC

// Data Movement (8)  
LDA, LDB, LDS, LDX, STA, STB, STS, STX

// Jump/Branch (16)
BCC, BCS, BEQ, BGE, BGT, BHI, BLE, BLS, BLT, BMI, BNE, BPL, BRA, BSR, BVC, BVS

// Control (8)
JMP, JSR, NOP, RTI, RTS, SWI, TAB, TBA

// Logic (8)  
AND, BIT, EOR, INC, NEG, ORA, ROL, ROR

// Stack (6)
PSH, PUL, TSX, TXS

// Total: 72 base instructions → 197 opcodes with addressing modes
```

### 3. Pseudo-Instruction Handling

```java
private void handlePseudoInstruction(AssemblyLine line, Map<Integer, List<Integer>> result) 
        throws CodeGenerationException {
    String pseudoOp = line.getPseudoOp();
    Object operand = line.getPseudoOperand();
    
    switch (pseudoOp) {
        case "ORG":
            currentAddress = (Integer) operand;
            break;
            
        case "FCB":  // Form Constant Byte
            if (operand instanceof List) {
                List<Integer> bytes = (List<Integer>) operand;
                result.put(line.getAddress(), bytes);
            } else {
                result.put(line.getAddress(), Arrays.asList((Integer) operand));
            }
            break;
            
        case "FDB":  // Form Double Byte (16-bit)
            if (operand instanceof List) {
                List<Integer> words = (List<Integer>) operand;
                List<Integer> bytes = new ArrayList<>();
                for (Integer word : words) {
                    bytes.add((word >> 8) & 0xFF);  // High byte
                    bytes.add(word & 0xFF);         // Low byte
                }
                result.put(line.getAddress(), bytes);
            } else {
                Integer word = (Integer) operand;
                result.put(line.getAddress(), Arrays.asList(
                    (word >> 8) & 0xFF,  // High byte
                    word & 0xFF          // Low byte
                ));
            }
            break;
            
        case "EQU":  // Equate - no code generated
        case "END":  // End - no code generated
            break;
            
        default:
            throw new CodeGenerationException("Unknown pseudo-instruction: " + pseudoOp);
    }
}
```

## 📊 Code Generation Örnekleri

### 1. Basic Instructions

```assembly
Input Assembly:          Generated Machine Code:
LDA #$FF                → [0x86, 0xFF]         (2 bytes)
STA $0300               → [0xB7, 0x03, 0x00]   (3 bytes)  
NOP                     → [0x01]               (1 byte)
```

### 2. Addressing Mode Examples

```java
// Immediate addressing
LDA #$55  → opcode=0x86, operand=0x55 → [0x86, 0x55]

// Direct addressing (zero page)
LDA $80   → opcode=0x96, operand=0x80 → [0x96, 0x80]

// Extended addressing  
LDA $1234 → opcode=0xB6, operand=0x1234 → [0xB6, 0x12, 0x34]

// Indexed addressing
LDA $10,X → opcode=0xA6, operand=0x10 → [0xA6, 0x10]

// Inherent addressing
ABA       → opcode=0x1B → [0x1B]
```

### 3. Branch Instructions

```assembly
Assembly:               Machine Code:
BEQ LOOP               → [0x27, relative_offset]

// Relative offset calculation:
// If LOOP is at $0200 and BEQ is at $0210:
// offset = $0200 - ($0210 + 2) = $0200 - $0212 = -$12 = $EE (two's complement)
BEQ LOOP               → [0x27, 0xEE]
```

### 4. Pseudo-Instructions

```assembly
Assembly:               Generated Data:
ORG $0200              → (sets current address to $0200)
FCB $12,$34,$56        → [0x12, 0x34, 0x56]
FDB $1234,$5678        → [0x12, 0x34, 0x56, 0x78]  
MAXVAL EQU $FF         → (adds MAXVAL=255 to symbol table)
```

### 5. Complete Program Example

```assembly
        ORG $0200
START   LDA #$FF        ; $0200: 86 FF
        STA $0300       ; $0202: B7 03 00  
LOOP    DEC $0300       ; $0205: 7A 03 00
        BNE LOOP        ; $0208: 26 FB (FB = -5 offset)
        SWI             ; $020A: 3F
        END
```

**Generated Machine Code Map:**
```java
{
    0x0200 → [0x86, 0xFF],        // LDA #$FF
    0x0202 → [0xB7, 0x03, 0x00],  // STA $0300
    0x0205 → [0x7A, 0x03, 0x00],  // DEC $0300  
    0x0208 → [0x26, 0xFB],        // BNE LOOP (offset=-5)
    0x020A → [0x3F]               // SWI
}
```

## 🐛 Error Handling

### 1. Opcode Lookup Errors
```java
// Unknown instruction
"BADOP #$FF" → CodeGenerationException("No opcode found for BADOP")

// Invalid addressing mode combination
"SWI #$FF" → CodeGenerationException("SWI does not support IMMEDIATE addressing")
```

### 2. Operand Range Errors
```java
// Direct addressing out of range
"LDA $300" → CodeGenerationException("Operand value out of range for DIRECT: 768")

// Extended addressing out of range  
"LDA $10000" → CodeGenerationException("Address out of range for extended addressing: 65536")
```

### 3. Relative Branch Errors
```java
// Branch distance too far
"BEQ DISTANT" → CodeGenerationException("Branch offset out of range: 200")
// (6800 relative branches limited to -128 to +127)
```

## 🧪 Test Scenarios

### 1. Basic Code Generation Test
```java
@Test
public void testBasicCodeGeneration() throws Exception {
    List<AssemblyLine> lines = Arrays.asList(
        createAssemblyLine("LDA", AddressingMode.IMMEDIATE, "#$FF", 0x0200),
        createAssemblyLine("STA", AddressingMode.EXTENDED, "$0300", 0x0202)
    );
    
    Map<Integer, List<Integer>> result = codeGenerator.generateCode(lines);
    
    assertEquals(Arrays.asList(0x86, 0xFF), result.get(0x0200));
    assertEquals(Arrays.asList(0xB7, 0x03, 0x00), result.get(0x0202));
}
```

### 2. Pseudo-Instruction Test
```java
@Test
public void testPseudoInstructions() throws Exception {
    AssemblyLine fcbLine = createPseudoOp("FCB", Arrays.asList(0x12, 0x34), 0x0200);
    
    Map<Integer, List<Integer>> result = codeGenerator.generateCode(Arrays.asList(fcbLine));
    
    assertEquals(Arrays.asList(0x12, 0x34), result.get(0x0200));
}
```

### 3. Error Handling Test
```java
@Test
public void testInvalidAddressingMode() {
    AssemblyLine line = createAssemblyLine("SWI", AddressingMode.IMMEDIATE, "#$FF", 0x0200);
    
    assertThrows(CodeGenerationException.class, () -> {
        codeGenerator.generateCode(Arrays.asList(line));
    });
}
```

## 🚀 Performance Optimizations

### 1. Opcode Lookup Optimization
```java
// Hash-based lookup for O(1) performance
private final Map<String, Instruction> instructionMap;

// Key format: "MNEMONIC_ADDRESSINGMODE"
String key = mnemonic + "_" + addressingMode.toString();
return instructionMap.get(key);
```

### 2. Memory Efficiency
```java
// Reuse byte arrays where possible
private static final List<Integer> EMPTY_OPERAND = Collections.emptyList();

// Minimize object creation
if (mode == AddressingMode.INHERENT) {
    return EMPTY_OPERAND;
}
```

### 3. Batch Generation
```java
// Generate all instructions in single pass
// Avoid multiple iterations over assembly lines
// Pre-calculate sizes for memory allocation
```

---

Assembler paketi, parse edilmiş assembly kodunu executable machine code'a çeviren kritik bir bileşendir. Doğru opcode generation ve error handling oldukça önemlidir.
