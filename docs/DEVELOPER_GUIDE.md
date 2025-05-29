# Geliştiriciler İçin Teknik Rehber

Bu dokümant, projeye yeni katılan geliştiricilerin kodbase'i anlaması ve etkili bir şekilde katkıda bulunması için kapsamlı bir teknik rehber sağlar.

## 🎯 Hızlı Başlangıç Rehberi

### 1. Projeyi Anlamak İçin İzlenecek Yol

```
Adım 1: Ana Mimariyi Anla
├── ARCHITECTURE.md okuyun (mimari genel bakış)
├── PACKAGE_ARCHITECTURE.md okuyun (paket detayları)
└── PROGRAM_EXECUTION_FLOW.md okuyun (akış analizi)

Adım 2: Kodu Çalıştır ve Test Et  
├── ./gradlew run (uygulamayı çalıştır)
├── Menüdeki tüm seçenekleri dene
└── Örnek program oluştur ve simüle et

Adım 3: Kaynak Kodu İncele
├── App.java ile başla (ana orkestrasyon)
├── assembler.core paketini incele (veri modelleri)
├── assembler.parser paketini incele (parsing logic)
└── assembler.simulator paketini incele (CPU simülasyonu)

Adım 4: İlk Değişiklik Yap
├── Yeni bir instruction ekle (OpcodeTable.java)
├── UI'da küçük bir iyileştirme yap
└── Test et ve doğrula
```

### 2. Geliştirme Ortamı Kurulumu

#### Gereksinimler
```bash
# Java 11+ (proje Java 11 ile test edilmiştir)
java -version

# Gradle (wrapper included)
./gradlew --version

# IDE (IntelliJ IDEA önerilir)
# VS Code + Java Extension Pack alternatif
```

#### Proje Build ve Run
```bash
# Proje build
./gradlew build

# Uygulamayı çalıştır
./gradlew run

# Test'leri çalıştır  
./gradlew test

# JAR dosyası oluştur
./gradlew jar
```

## 🧩 Kod Yapısı ve Conventions

### Java Coding Standards

#### Naming Conventions
```java
// Classes: PascalCase
public class AssemblyParser { }
public class ExecutionEngine { }

// Methods: camelCase
public void parseSource() { }
public ExecutionResult step() { }

// Constants: UPPER_SNAKE_CASE
public static final int FLAG_ZERO = 0x04;
public static final String DEFAULT_ORG = "0100";

// Variables: camelCase
private List<AssemblyLine> currentProgram;
private Map<Integer, List<Integer>> machineCode;

// Packages: lowercase
package assembler.parser;
package assembler.simulator;
```

#### Code Organization Patterns

```java
// Class structure template
public class ExampleClass {
    // 1. Constants (static final)
    private static final int MAX_SIZE = 1000;
    
    // 2. Instance fields
    private final DependencyClass dependency;
    private StateClass currentState;
    
    // 3. Constructor(s)
    public ExampleClass(DependencyClass dependency) {
        this.dependency = dependency;
    }
    
    // 4. Public methods (interface)
    public ResultType publicMethod() { }
    
    // 5. Package-private methods (if needed)
    void packageMethod() { }
    
    // 6. Private methods (implementation)
    private void privateHelper() { }
    
    // 7. Inner classes (if needed)
    private static class InnerHelper { }
}
```

### JavaDoc Standards

```java
/**
 * Ana parser sınıfı. Motorola 6800 assembly kodunu parse eder.
 * 
 * <p>İki geçişli algoritma kullanır:
 * <ol>
 *   <li>Tokenization ve syntax validation</li>
 *   <li>Label resolution ve final validation</li>
 * </ol>
 * 
 * <p>Desteklenen özellikler:
 * <ul>
 *   <li>Tüm 6800 instruction'ları</li>
 *   <li>Label tanımları ve referansları</li>
 *   <li>Pseudo-instruction'lar (ORG, END, EQU, FCB, FDB)</li>
 *   <li>Comment desteği (semicolon)</li>
 * </ul>
 * 
 * @author lightningcell
 * @since 1.0
 * @see AssemblyLine
 * @see LabelResolver
 */
public class AssemblyParser {
    
    /**
     * Assembly kaynak kodunu parse eder.
     * 
     * @param sourceCode Parse edilecek assembly kaynak kodu
     * @return Parse edilmiş assembly satırları
     * @throws ParseException Syntax hatası durumunda
     * @throws IllegalArgumentException sourceCode null ise
     */
    public List<AssemblyLine> parseSource(String sourceCode) throws ParseException {
        // Implementation
    }
}
```

## 🔧 Yeni Özellik Ekleme Rehberi

### Yeni Instruction Ekleme

#### Adım 1: OpcodeTable.java'ya Ekle
```java
// OpcodeTable.java - initializeInstructions() metodu içinde
private void initializeInstructions() {
    // Mevcut instruction'lar...
    
    // Yeni instruction ekleme
    addInstruction("NEG", AddressingMode.DIRECT, 0x60, 6, "Negate memory (direct)");
    addInstruction("NEG", AddressingMode.INDEXED, 0x70, 7, "Negate memory (indexed)");
    addInstruction("NEG", AddressingMode.EXTENDED, 0x80, 7, "Negate memory (extended)");
}
```

#### Adım 2: CPU6800.java'ya Execution Logic Ekle
```java
// CPU6800.java - executeInstruction() metodu içinde
private boolean executeInstruction(int opcode) {
    switch (opcode) {
        // Mevcut case'ler...
        
        case 0x60: executeNEG_DIR(); break;   // NEG direct
        case 0x70: executeNEG_IDX(); break;   // NEG indexed  
        case 0x80: executeNEG_EXT(); break;   // NEG extended
        
        default: return false;
    }
    return true;
}

// Yeni instruction implementation'ı
private void executeNEG_DIR() {
    int address = fetchByte();                    // Get direct address
    int value = memory.readByte(address);         // Read current value
    int result = (-value) & 0xFF;                 // Negate (two's complement)
    
    memory.writeByte(address, result);            // Write back
    updateNZVC(result, value, 0);                 // Update flags
}

private void executeNEG_IDX() {
    int offset = fetchByte();                     // Get offset
    int address = (registers.getRegX() + offset) & 0xFFFF;
    int value = memory.readByte(address);
    int result = (-value) & 0xFF;
    
    memory.writeByte(address, result);
    updateNZVC(result, value, 0);
}

private void executeNEG_EXT() {
    int address = fetchWord();                    // Get 16-bit address
    int value = memory.readByte(address);
    int result = (-value) & 0xFF;
    
    memory.writeByte(address, result);
    updateNZVC(result, value, 0);
}
```

#### Adım 3: Test Ekleme
```java
// Test dosyası oluştur: src/test/java/assembler/assembler/OpcodeTableTest.java
@Test
public void testNegInstructionExists() {
    OpcodeTable table = new OpcodeTable();
    
    // Test all NEG addressing modes
    assertNotNull(table.getInstruction("NEG", AddressingMode.DIRECT));
    assertNotNull(table.getInstruction("NEG", AddressingMode.INDEXED)); 
    assertNotNull(table.getInstruction("NEG", AddressingMode.EXTENDED));
    
    // Verify opcodes
    assertEquals(0x60, table.getInstruction("NEG", AddressingMode.DIRECT).getOpcode());
    assertEquals(0x70, table.getInstruction("NEG", AddressingMode.INDEXED).getOpcode());
    assertEquals(0x80, table.getInstruction("NEG", AddressingMode.EXTENDED).getOpcode());
}

// CPU test ekleme
@Test
public void testNegInstructionExecution() {
    CPU6800 cpu = new CPU6800();
    
    // Test NEG direct
    cpu.getMemory().writeByte(0x30, 0x05);        // Write test value
    cpu.getMemory().writeByte(0x0000, 0x60);      // NEG $30 instruction
    cpu.getMemory().writeByte(0x0001, 0x30);      // Direct address
    
    cpu.step();                                   // Execute
    
    assertEquals(0xFB, cpu.getMemory().readByte(0x30)); // -5 = 0xFB
    assertTrue(cpu.getRegisters().getFlag(Registers.FLAG_NEGATIVE)); // N flag set
}
```

### Yeni Pseudo-Instruction Ekleme

#### Örnek: RMB (Reserve Memory Bytes) Ekleme

#### Adım 1: AssemblyParser.java'da Recognition
```java
// AssemblyParser.java - isPseudoInstruction() metodu
private boolean isPseudoInstruction(String mnemonic) {
    return mnemonic.equals("ORG") || mnemonic.equals("END") || 
           mnemonic.equals("EQU") || mnemonic.equals("FCB") || 
           mnemonic.equals("FDB") || mnemonic.equals("RMB");  // Yeni eklenen
}

// parsePseudoInstruction() metodu içinde
private AssemblyLine parsePseudoInstruction(AssemblyLine assemblyLine, String mnemonic, 
                                           List<String> tokens, int tokenIndex) throws ParseException {
    switch (mnemonic) {
        // Mevcut case'ler...
        
        case "RMB":
            if (tokenIndex >= tokens.size()) {
                throw new ParseException("RMB requires a byte count operand");
            }
            int byteCount = parseNumericOperand(tokens.get(tokenIndex));
            if (byteCount < 0 || byteCount > 65536) {
                throw new ParseException("RMB byte count must be 0-65536");
            }
            assemblyLine.setPseudoOp("RMB", byteCount);
            break;
            
        default:
            throw new ParseException("Unknown pseudo-instruction: " + mnemonic);
    }
    
    return assemblyLine;
}
```

#### Adım 2: CodeGenerator.java'da Handling
```java
// CodeGenerator.java - generatePseudoOpCode() metodu
private List<Integer> generatePseudoOpCode(AssemblyLine line) throws CodeGenerationException {
    List<Integer> bytes = new ArrayList<>();
    String pseudoOp = line.getPseudoOp();
    Object operand = line.getPseudoOperand();
    
    switch (pseudoOp) {
        // Mevcut case'ler...
        
        case "RMB":
            int byteCount = (Integer) operand;
            // RMB doesn't generate bytes, just advances address
            // Address advancement handled by caller
            return bytes; // Empty list - no bytes generated
            
        default:
            throw new CodeGenerationException("Unknown pseudo-operation: " + pseudoOp);
    }
}

// calculateLineSize() metodu güncelle
private int calculateLineSize(AssemblyLine line) {
    if (line.isPseudoOp()) {
        String pseudoOp = line.getPseudoOp();
        Object operand = line.getPseudoOperand();
        
        switch (pseudoOp) {
            // Mevcut case'ler...
            
            case "RMB":
                return (Integer) operand; // Reserve specified number of bytes
                
            default:
                return 0;
        }
    }
    // Mevcut instruction size calculation...
}
```

### Yeni Output Format Ekleme

#### Örnek: Motorola S-Record Format

#### Adım 1: FileManager.java'ya Metot Ekle
```java
/**
 * Save machine code to Motorola S-Record format.
 * 
 * @param filePath Path to save the S-Record file
 * @param machineCode Map of addresses to machine code bytes
 * @throws IOException if file cannot be written
 */
public static void saveSRecordFile(String filePath, Map<Integer, List<Integer>> machineCode) throws IOException {
    try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
        // S0 record (header)
        writer.println(createS0Record("ASSEMBLER"));
        
        // S1 records (data)
        for (Map.Entry<Integer, List<Integer>> entry : machineCode.entrySet()) {
            int address = entry.getKey();
            List<Integer> bytes = entry.getValue();
            
            // Write in chunks of 16 bytes max
            for (int i = 0; i < bytes.size(); i += 16) {
                int chunkSize = Math.min(16, bytes.size() - i);
                List<Integer> chunk = bytes.subList(i, i + chunkSize);
                String record = createS1Record(address + i, chunk);
                writer.println(record);
            }
        }
        
        // S9 record (termination)
        writer.println(createS9Record());
    }
}

private static String createS1Record(int address, List<Integer> data) {
    StringBuilder record = new StringBuilder();
    
    int length = data.size() + 3; // data + 2 byte address + 1 byte checksum
    record.append(String.format("S1%02X%04X", length, address));
    
    int checksum = length + (address >> 8) + (address & 0xFF);
    for (int b : data) {
        record.append(String.format("%02X", b));
        checksum += b;
    }
    
    checksum = (~checksum) & 0xFF; // One's complement
    record.append(String.format("%02X", checksum));
    
    return record.toString();
}
```

#### Adım 2: App.java'da Kullanım Ekleme
```java
// saveProgram() metodu güncellemesi
private void saveProgram() {
    if (sourceCode.isEmpty()) {
        ui.showError("No program to save.");
        return;
    }
    
    try {
        String filename = ui.getInput("Enter filename to save: ");
        FileManager.saveSourceFile(filename, sourceCode);
        ui.showMessage("Program saved to: " + filename);
        
        if (!machineCode.isEmpty()) {
            // Existing formats
            String hexFile = filename.replaceFirst("\\.[^.]*$", "") + ".hex";
            FileManager.saveHexFile(hexFile, machineCode);
            ui.showMessage("Intel HEX saved to: " + hexFile);
            
            // New format
            String srecFile = filename.replaceFirst("\\.[^.]*$", "") + ".s19";
            FileManager.saveSRecordFile(srecFile, machineCode);
            ui.showMessage("S-Record saved to: " + srecFile);
        }
        
    } catch (Exception e) {
        ui.showError("Failed to save file: " + e.getMessage());
    }
}
```

## 🧪 Test Yazma Rehberi

### Unit Test Patterns

#### Core Model Testing
```java
// Test class örneği
public class InstructionTest {
    
    @Test
    public void testInstructionCreation() {
        // Given
        String mnemonic = "LDA";
        AddressingMode mode = AddressingMode.IMMEDIATE;
        int opcode = 0x86;
        int cycles = 2;
        String description = "Load accumulator A immediate";
        
        // When
        Instruction instruction = new Instruction(mnemonic, mode, opcode, cycles, description);
        
        // Then
        assertEquals("LDA", instruction.getMnemonic());
        assertEquals(AddressingMode.IMMEDIATE, instruction.getAddressingMode());
        assertEquals(0x86, instruction.getOpcode());
        assertEquals(2, instruction.getCycles());
        assertEquals(description, instruction.getDescription());
    }
    
    @Test
    public void testInstructionImmutability() {
        // Instruction'ın immutable alanlarının değiştirilemediğini test et
        Instruction instruction = new Instruction("LDA", AddressingMode.IMMEDIATE, 0x86, 2, "Test");
        
        // Bu alanlar final olduğu için değiştirilemez
        assertThrows(/* Compilation error expected */);
    }
}
```

#### Parser Testing
```java
public class AssemblyParserTest {
    
    private AssemblyParser parser;
    
    @BeforeEach
    public void setUp() {
        parser = new AssemblyParser();
    }
    
    @Test
    public void testParseSimpleInstruction() throws ParseException {
        // Given
        String sourceCode = "LDA #$FF";
        
        // When
        List<AssemblyLine> result = parser.parseSource(sourceCode);
        
        // Then
        assertEquals(1, result.size());
        AssemblyLine line = result.get(0);
        assertNotNull(line.getInstruction());
        assertEquals("LDA", line.getInstruction().getMnemonic());
        assertEquals(AddressingMode.IMMEDIATE, line.getInstruction().getAddressingMode());
        assertEquals("#$FF", line.getInstruction().getOperand());
    }
    
    @Test
    public void testParseLabeledInstruction() throws ParseException {
        // Given
        String sourceCode = "LOOP: DEC $30";
        
        // When
        List<AssemblyLine> result = parser.parseSource(sourceCode);
        
        // Then
        AssemblyLine line = result.get(0);
        assertNotNull(line.getLabel());
        assertEquals("LOOP", line.getLabel().getName());
        assertNotNull(line.getInstruction());
        assertEquals("DEC", line.getInstruction().getMnemonic());
    }
    
    @Test 
    public void testParseError() {
        // Given
        String invalidCode = "INVALID_INSTRUCTION #$FF";
        
        // When & Then
        assertThrows(ParseException.class, () -> {
            parser.parseSource(invalidCode);
        });
    }
}
```

#### Simulator Testing
```java
public class CPU6800Test {
    
    private CPU6800 cpu;
    
    @BeforeEach
    public void setUp() {
        cpu = new CPU6800();
    }
    
    @Test
    public void testLDAImmediate() {
        // Given - Setup LDA #$FF instruction
        cpu.getMemory().writeByte(0x0000, 0x86); // LDA immediate opcode
        cpu.getMemory().writeByte(0x0001, 0xFF); // Immediate value
        cpu.getRegisters().setProgramCounter(0x0000);
        
        // When
        boolean success = cpu.step();
        
        // Then
        assertTrue(success);
        assertEquals(0xFF, cpu.getRegisters().getRegA());
        assertEquals(0x0002, cpu.getRegisters().getProgramCounter());
        assertTrue(cpu.getRegisters().getFlag(Registers.FLAG_NEGATIVE)); // N flag set
        assertFalse(cpu.getRegisters().getFlag(Registers.FLAG_ZERO));   // Z flag clear
    }
    
    @Test
    public void testBranchTaken() {
        // Given - Setup BEQ with Z flag set
        cpu.getMemory().writeByte(0x0000, 0x27); // BEQ opcode
        cpu.getMemory().writeByte(0x0001, 0x10); // Relative offset (+16)
        cpu.getRegisters().setProgramCounter(0x0000);
        cpu.getRegisters().setFlag(Registers.FLAG_ZERO, true); // Set Z flag
        
        // When
        cpu.step();
        
        // Then
        assertEquals(0x0012, cpu.getRegisters().getProgramCounter()); // 0x0002 + 0x10
    }
}
```

### Integration Test Patterns

```java
public class AssemblyIntegrationTest {
    
    @Test
    public void testCompleteAssemblyProcess() throws Exception {
        // Given - Complete assembly program
        String sourceCode = 
            "        ORG $0100\n" +
            "START   LDA #$FF\n" +
            "        STA $1000\n" +
            "LOOP    DEC $1000\n" +
            "        BNE LOOP\n" +
            "        SWI\n" +
            "        END\n";
        
        // When - Full assembly process
        App app = new App();
        // Simulate complete assembly process
        // (Bu test daha karmaşık ve App sınıfının refactor edilmesini gerektirebilir)
        
        // Then - Verify results
        // Machine code generated correctly
        // Label resolution successful
        // No errors occurred
    }
}
```

## 🐛 Debugging Rehberi

### Common Issues ve Solutions

#### Issue 1: Label Resolution Hatası
```
Error: "Undefined label: LOOP"

Neden:
- Label tanımlanmamış
- Label yanlış yazılmış
- Label scope problemi

Debug Yöntemi:
1. LabelResolver.java'da breakpoint koy
2. labelDefinitions map'ini incele
3. Label tanımının doğru collected olduğunu kontrol et
4. Label reference'ının doğru resolve edildiğini kontrol et

Fix:
- Label tanımının `:` ile bittiğini kontrol et
- Case sensitivity kontrolü
- Label scope'unu doğrula
```

#### Issue 2: Opcode Bulunamıyor
```
Error: "No opcode found for instruction: LDA EXTENDED"

Neden:
- OpcodeTable'da opcode eksik
- Addressing mode yanlış tespit edilmiş
- Mnemonic yanlış

Debug Yöntemi:
1. OpcodeTable.java'da getInstruction() metoduna breakpoint
2. Key oluşturma logic'ini incele
3. instructionMap'in doğru populate edildiğini kontrol et

Fix:
- OpcodeTable.initializeInstructions()'a eksik opcode'u ekle
- AddressingMode detection logic'ini düzelt
```

#### Issue 3: CPU Execution Hatası
```
Error: CPU halted unexpectedly

Neden:
- Invalid opcode execute edilmeye çalışılıyor
- Stack overflow/underflow
- Invalid memory access

Debug Yöntemi:
1. CPU6800.step() metoduna breakpoint
2. Program Counter değerini incele
3. Fetch edilen opcode'u kontrol et
4. Memory state'ini incele

Fix:
- Opcode table'ını kontrol et
- Memory initialization'ı doğrula
- Stack pointer management'ı incele
```

### Debug Tools Usage

#### IntelliJ IDEA Debugging
```java
// Conditional breakpoint örneği
// CPU6800.step() metodunda:
// Condition: registers.getProgramCounter() == 0x0105

// Watch expressions
registers.getProgramCounter()
memory.readByte(registers.getProgramCounter())
Integer.toHexString(opcode)

// Evaluate expressions
memory.readWord(0x1000)
registers.getFlag(Registers.FLAG_ZERO)
```

#### Logging Ekleme
```java
// Temporary debugging logs
public boolean step() {
    int pc = registers.getProgramCounter();
    int opcode = memory.readByte(pc);
    
    System.out.printf("DEBUG: PC=0x%04X, Opcode=0x%02X%n", pc, opcode);
    
    boolean result = executeInstruction(opcode);
    
    System.out.printf("DEBUG: After execution - A=0x%02X, Flags=0x%02X%n",
                     registers.getRegA(), registers.getConditionCode());
    
    return result;
}
```

## 📝 Code Review Checklist

### Pre-commit Checklist
```
□ Kod compile oluyor mu?
□ Mevcut testler geçiyor mu?
□ Yeni feature için test yazıldı mı?
□ JavaDoc eksiksiz mi?
□ Naming conventions uygulanmış mı?
□ Error handling adequate mi?
□ Performance impact değerlendirildi mi?
□ Security implications düşünüldü mü?
```

### Code Quality Checks
```
□ Single Responsibility Principle uygulanmış mı?
□ Dependencies minimize edilmiş mi?
□ Magic numbers/strings constant'a çevrilmiş mi?
□ Exception handling meaningful mi?
□ Method ve class boyutları reasonable mi?
□ Comments ve documentation güncel mi?
```

Bu teknik rehber, geliştiricilerin projeye hızlı entegre olması ve etkili katkıda bulunması için gerekli tüm bilgileri içermektedir.
