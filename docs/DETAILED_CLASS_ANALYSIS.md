# Sınıf Bazlı Detaylı Kod Analizi

Bu döküman her Java sınıfını ayrı ayrı analiz eder ve responsivite'lerini, metodlarını ve kullanım amaçlarını detaylandırır.

## 📁 assembler (Root Package)

### App.java - Ana Orkestrasyon Sınıfı

**Sorumluluk**: Uygulamanın kalbi olan sınıf. Tüm bileşenleri orchestrate eder.

**Temel Alanlar**:
```java
private final ConsoleUI ui;                    // Kullanıcı arayüzü
private final AssemblyParser parser;           // Assembly parser
private final CodeGenerator codeGenerator;     // Makine kod üretici
private final ExecutionEngine simulator;       // Simülasyon motoru
private List<AssemblyLine> currentProgram;     // Mevcut program
private Map<Integer, List<Integer>> machineCode; // Üretilen makine kodu
private String sourceCode;                     // Kaynak kod
```

**Ana Metotlar**:

#### `public static void main(String[] args)`
- **Amaç**: Program entry point'i
- **İşlem**: App instance'ı oluşturur ve run() metodu çağırır

#### `public void run()`
- **Amaç**: Ana kullanıcı döngüsü
- **İşlem**: Menü gösterme, kullanıcı seçimi alma, ilgili işlemi çağırma
- **Döngü**: `while(running)` ile sürekli çalışır

#### `private void assembleProgram()`
- **Amaç**: İki geçişli assembly işleminin koordinasyonu
- **İşlem Sırası**:
  1. `parser.parseSource(sourceCode)` - Parsing
  2. `calculateAddresses(currentProgram)` - Adres hesaplama
  3. `parser.resolveLabelReferences(currentProgram)` - Label çözümü
  4. `codeGenerator.generateCode(currentProgram)` - Kod üretimi

#### `private void simulateProgram()`
- **Amaç**: Simülasyon oturum yönetimi
- **İşlem**: Simülatör menü döngüsü, step/run/inspect operasyonları

#### `private void calculateAddresses(List<AssemblyLine> assemblyLines)`
- **Amaç**: First pass için adres hesaplaması
- **Algoritma**: ORG direktifleri işleme, instruction size hesaplama

**Kullanım Senaryosu**:
```java
App app = new App();  // Bileşenler initialize edilir
app.run();           // Ana döngü başlar
// Kullanıcı menüden seçim yapar → ilgili metot çağrılır
```

---

## 📁 assembler.core (Veri Modelleri)

### AddressingMode.java - Adreslenme Modları Enum

**Sorumluluk**: 6800'ün 7 adreslenme türünü tanımlar ve yönetir.

**Enum Değerleri**:
```java
INHERENT("Inherent", 0, "No operand")          // NOP, TAB
IMMEDIATE("Immediate", 1, "#data")             // LDA #$FF
DIRECT("Direct", 1, "addr (0-255)")           // LDA $30
EXTENDED("Extended", 2, "addr (0-65535)")     // LDA $1234
INDEXED("Indexed", 1, "offset,X")             // LDA $30,X
RELATIVE("Relative", 1, "relative offset")    // BEQ LOOP
PSEUDO("Pseudo", 0, "assembler directive")    // ORG, END
```

**Ana Metotlar**:
- `getOperandSize()`: Operand byte sayısı
- `getInstructionSize()`: Toplam instruction boyutu (opcode + operand)
- `getFormat()`: Display formatı

### Instruction.java - Komut Modeli

**Sorumluluk**: Tek bir assembly komutunu temsil eder.

**Alanlar**:
```java
private final String mnemonic;           // Komut adı (LDA, STA)
private final AddressingMode addressingMode; // Adreslenme türü
private String operand;                  // Operand string'i
private int resolvedOperand;             // Çözümlenmiş operand değeri
private final int opcode;                // Makine kodu opcode'u
private final int cycles;                // CPU cycle sayısı
private final String description;        // Açıklama
```

**Constructor'lar**:
1. **Parsing Constructor**: `Instruction(mnemonic, addressingMode, operand)`
2. **Opcode Table Constructor**: `Instruction(mnemonic, addressingMode, opcode, cycles, description)`

**Kullanım Alanları**:
- Parser tarafından parsing sırasında oluşturulur
- CodeGenerator tarafından makine koduna çevrilir
- Simulator tarafından execution sırasında kullanılır

### Label.java - Sembolik Adres Modeli

**Sorumluluk**: Assembly label'larını temsil eder.

**Alanlar**:
```java
private final String name;         // Label adı
private int value;                 // Label değeri (adresi)
private boolean defined;           // Tanımlanmış mı?
```

**Ana Metotlar**:
- `setValue(int value)`: Label değeri atar
- `isDefined()`: Tanımlanma durumu kontrolü
- `getValue()`: Label değerini döner

### Memory.java - Bellek Simülasyonu

**Sorumluluk**: 6800'ün 64KB bellek alanını simüle eder.

**Alanlar**:
```java
private final byte[] memory;       // 64KB bellek array'i
private static final int SIZE = 65536; // 64KB
```

**Ana Metotlar**:
```java
public void writeByte(int address, int value)     // Byte yazma
public int readByte(int address)                  // Byte okuma
public void writeWord(int address, int value)     // Word yazma (16-bit)
public int readWord(int address)                  // Word okuma
public void loadProgram(int startAddress, Map<Integer, List<Integer>> code) // Program yükleme
```

### Registers.java - CPU Register Seti

**Sorumluluk**: 6800 CPU registerlerini model eder.

**Register Alanları**:
```java
private int regA;          // Accumulator A (8-bit)
private int regB;          // Accumulator B (8-bit)
private int regX;          // Index Register (16-bit)
private int stackPointer;  // Stack Pointer (16-bit)
private int programCounter; // Program Counter (16-bit)
private int conditionCode; // Condition Code Register (8-bit)
```

**Flag Bit'leri (CCR)**:
```java
public static final int FLAG_CARRY = 0x01;      // C flag
public static final int FLAG_OVERFLOW = 0x02;   // V flag
public static final int FLAG_ZERO = 0x04;       // Z flag
public static final int FLAG_NEGATIVE = 0x08;   // N flag
public static final int FLAG_INTERRUPT = 0x10;  // I flag
public static final int FLAG_HALF_CARRY = 0x20; // H flag
```

---

## 📁 assembler.parser (Parsing Katmanı)

### AssemblyParser.java - Ana Parser Sınıfı

**Sorumluluk**: Assembly kaynak kodunu parse eder ve doğrular.

**Bileşenler**:
```java
private final SyntaxValidator validator;    // Sözdizimi doğrulayıcı
private final LabelResolver labelResolver;  // Label çözümleyici
private final TokenParser tokenParser;     // Token ayırıcı
```

**Ana İşlem Akışı**:

#### `public List<AssemblyLine> parseSource(String sourceCode)`
1. Kaynak kodu satırlara böl
2. Her satır için `parseLine()` çağır
3. Parse edilmiş satırları listele

#### `public AssemblyLine parseLine(String line, int lineNumber)`
1. Comment'ları kaldır
2. Token'lara ayır (`tokenParser.tokenize()`)
3. Label kontrolü (`:` ile bitenler)
4. Instruction/pseudo-op parsing
5. Addressing mode tespiti

#### `private AddressingMode determineAddressingMode(String mnemonic, String operand)`
- `#` ile başlayan → IMMEDIATE
- `,X` ile biten → INDEXED  
- `$XX` formatı → DIRECT veya EXTENDED (değere göre)
- Operand yok → INHERENT

### AssemblyLine.java - Parse Edilmiş Satır

**Sorumluluk**: Tek bir assembly satırının tüm parse bilgilerini tutar.

**Alanlar**:
```java
private final int lineNumber;          // Satır numarası
private final String sourceLine;       // Orijinal kaynak satır
private Label label;                   // Varsa label
private Instruction instruction;       // Varsa instruction
private String pseudoOp;              // Varsa pseudo-operation
private Object pseudoOperand;         // Pseudo-op operandı
private int address;                  // Bu satırın adresi
private List<Integer> machineCode;    // Üretilen makine kodu
```

**Pseudo-Op Desteği**:
- `ORG address`: Origin address belirleme
- `END`: Program sonu
- `EQU value`: Equ directive (label = value)
- `FCB value1,value2,...`: Form Constant Byte
- `FDB value1,value2,...`: Form Double Byte (word)

### LabelResolver.java - Label Çözümleyici

**Sorumluluk**: İki geçişli label resolution algoritması uygular.

**İç Sınıf**: `LabelReference`
```java
private static class LabelReference {
    String labelName;           // Referans edilen label adı
    int lineNumber;            // Referansın olduğu satır
    int address;               // Referansın adresi
}
```

**Ana Metotlar**:

#### `public void resolveLabelReferences(List<AssemblyLine> lines)`
1. **First Pass**: Tüm label tanımlarını topla
2. **Second Pass**: Tüm label referanslarını çöz
3. Error handling: Undefined/multiply defined labels

### SyntaxValidator.java - Sözdizimi Doğrulayıcı

**Sorumluluk**: Assembly kod sözdizimi kontrolü yapar.

**Doğrulama Türleri**:

#### `public void validateLabelName(String name)`
- Alfabetik karakter ile başlamalı
- Alphanumeric + underscore
- Reserved keyword kontrolü

#### `public void validateInstruction(String mnemonic, AddressingMode mode)`
- Instruction'ın var olup olmadığı
- Addressing mode'un desteklenip desteklenmediği
- OpcodeTable ile cross-check

### TokenParser.java - Lexical Analyzer

**Sorumluluk**: Kaynak kodu anlamlı token'lara ayırır.

**Token Türleri**:
- **Label**: `:` ile biten string'ler
- **Mnemonic**: Instruction adları
- **Operand**: Komut operandları
- **Numeric**: Sayısal değerler (decimal, hex, binary)

**Numeric Parsing**:
```java
$FF   → Hexadecimal (255)
%1010 → Binary (10)  
123   → Decimal (123)
```

---

## 📁 assembler.assembler (Code Generation)

### CodeGenerator.java - Makine Kodu Üretici

**Sorumluluk**: Parse edilmiş assembly'den makine kodu üretir.

**Bileşenler**:
```java
private final OpcodeTable opcodeTable;  // Opcode lookup tablosu
private int currentAddress;             // Mevcut adres pointer'ı
```

**Ana İşlem Akışı**:

#### `public Map<Integer, List<Integer>> generateCode(List<AssemblyLine> lines)`
1. Her satır için adres ata
2. `generateLineCode()` ile byte'ları üret
3. Address → byte map'i oluştur

#### `private List<Integer> generateInstructionCode(Instruction instruction)`
1. OpcodeTable'dan opcode bul
2. Operand encode et (addressing mode'a göre)
3. Byte array oluştur

**Operand Encoding**:
- **IMMEDIATE**: Değer direkt
- **DIRECT**: Low byte only
- **EXTENDED**: High byte + Low byte
- **INDEXED**: Offset value
- **RELATIVE**: Relative offset hesapla

### OpcodeTable.java - Opcode Tablosu

**Sorumluluk**: 6800'ün 197 opcode'unu yönetir.

**Veri Yapısı**:
```java
private final Map<String, Instruction> instructionMap;
// Key: "MNEMONIC_ADDRESSING_MODE" 
// Value: Instruction with opcode, cycles, description
```

**Örnek Girişler**:
```java
addInstruction("LDA", IMMEDIATE, 0x86, 2, "Load accumulator A immediate");
addInstruction("LDA", DIRECT, 0x96, 3, "Load accumulator A direct");
addInstruction("BEQ", RELATIVE, 0x27, 4, "Branch if equal (Z=1)");
```

**Komut Kategorileri**:
- **Load/Store**: LDA, LDB, STA, STB, LDX, STX
- **Arithmetic**: ADD, SUB, CMP, INC, DEC
- **Logic**: AND, OR, EOR, COM
- **Branch**: BEQ, BNE, BCC, BCS, BPL, BMI
- **Jump**: JMP, JSR, RTS
- **Stack**: PSH, PUL
- **Control**: NOP, SWI, RTI

---

## 📁 assembler.simulator (CPU Simülasyonu)

### ExecutionEngine.java - Simülasyon Kontrolcüsü

**Sorumluluk**: Yüksek seviye simülasyon operasyonlarını yönetir.

**Bileşenler**:
```java
private final CPU6800 cpu;                     // Düşük seviye CPU
private final CodeGenerator codeGenerator;     // Kod üretici
private List<AssemblyLine> currentProgram;     // Mevcut program
private Map<Integer, AssemblyLine> addressToLineMap; // Adres → satır map'i
```

**Ana Metotlar**:

#### `public void loadProgram(List<AssemblyLine> assemblyLines)`
1. Machine code üret
2. Address → line mapping yap
3. CPU'ya program yükle
4. İstatistikleri reset et

#### `public ExecutionResult step()`
- Tek instruction çalıştır
- Execution result döner (status, PC, line info)
- İstatistikleri güncelle

#### `public ExecutionResult run()`
- Program bitene veya breakpoint'e kadar çalıştır
- Her step'te breakpoint kontrolü
- Final statistics döner

### CPU6800.java - Düşük Seviye CPU Simülatörü

**Sorumluluk**: 6800 CPU'nun cycle-accurate simülasyonu.

**CPU State**:
```java
private final Registers registers;     // CPU register seti
private final Memory memory;          // Bellek
private boolean halted;               // CPU durumu
private Set<Integer> breakpoints;     // Breakpoint'ler
```

**Execution Cycle**:

#### `public boolean step()`
1. **Fetch**: PC'den instruction fetch et
2. **Decode**: Opcode'u decode et
3. **Execute**: Instruction'ı çalıştır
4. **Update**: PC ve flag'lari güncelle

**Instruction Implementation Örnekleri**:
```java
private void executeLDA_IMM() {
    int value = fetchByte();
    registers.setRegA(value);
    updateFlags(value);
}

private void executeBEQ_REL() {
    int offset = fetchByte();
    if (registers.getFlag(FLAG_ZERO)) {
        int pc = registers.getProgramCounter();
        registers.setProgramCounter(pc + offset);
    }
}
```

### ExecutionResult.java - Çalıştırma Sonucu

**Sorumluluk**: Tek bir instruction execution sonucunu tutar.

**Alanlar**:
```java
private final ExecutionStatus status;      // RUNNING, HALTED, BREAKPOINT, ERROR
private final int programCounter;          // Execution anındaki PC
private final String message;              // Status mesajı
private final AssemblyLine assemblyLine;   // Çalıştırılan satır
```

### ExecutionStatistics.java - İstatistik Toplayıcı

**Sorumluluk**: Simülasyon performance metriklerini toplar.

**Metrikler**:
```java
private int instructionsExecuted;      // Toplam instruction sayısı
private long executionTime;            // Çalışma süresi (ms)
private int cyclesExecuted;            // Toplam CPU cycle
private double averageSpeed;           // Ortalama hız (instructions/sec)
```

---

## 📁 assembler.ui (Kullanıcı Arayüzü)

### ConsoleUI.java - Konsol Arayüzü

**Sorumluluk**: Tüm kullanıcı etkileşimlerini yönetir.

**Scanner Management**:
```java
private final Scanner scanner;  // Kullanıcı input'u için
```

**Menü Sistemleri**:

#### `public int showMainMenu()`
```
1. Enter assembly code manually
2. Load program from file  
3. Assemble current program
4. View machine code
5. Simulate program
6. Save program to file
7. Show instruction set
8. Create example program
0. Exit
```

#### `public int showSimulatorMenu()`
```
1. Step execution (single instruction)
2. Run program
3. View registers
4. View memory
5. Manage breakpoints
6. Reset simulation
0. Exit simulator
```

**Display Metotları**:

#### `public void showMachineCode(List<AssemblyLine> lines, Map<Integer, List<Integer>> code)`
```
Address  Source Line                 Machine Code
-------- --------------------------- ------------
0100     ORG $0100                  
0100     LDA #$FF                   86 FF
0102     STA $1000                  B7 10 00
```

#### `public void showRegisters(Registers registers)`
```
A=$FF B=$00 X=$0000 SP=$01FF PC=$0105
Flags: N=1 Z=0 V=0 C=0 I=0 H=0
```

#### `public void showMemory(Memory memory, int start, int length)`
```
Address  +0 +1 +2 +3 +4 +5 +6 +7  +8 +9 +A +B +C +D +E +F
0100     86 FF B7 10 00 4F 20 FB  96 30 97 31 7E 01 06 00
```

---

## 📁 assembler.util (Yardımcı Sınıflar)

### FileManager.java - Dosya Operasyonları

**Sorumluluk**: Tüm dosya I/O işlemlerini yönetir.

**Ana Metotlar**:

#### `public static String loadSourceFile(String filePath)`
- Assembly kaynak dosyasını yükler
- UTF-8 encoding desteği
- IOException handling

#### `public static void saveBinaryFile(String filePath, Map<Integer, List<Integer>> machineCode)`
- Raw binary format kaydetme
- Contiguous memory image oluşturma
- Gap'leri sıfır ile doldurma

#### `public static void saveHexFile(String filePath, Map<Integer, List<Integer>> machineCode)`
- Intel HEX format çıktı
- 16-byte chunk'lar halinde
- Checksum hesaplama

**Intel HEX Format Örneği**:
```
:10010000862FB710004F20FB96309731016E0100D5
:10011000C6FFD73086FFB7100001000000000000A6
:00000001FF
```

#### `public static void createExampleProgram(String filename)`
Örnek program oluşturur:
```assembly
; Simple example program
        ORG $0100
START   LDA #$FF        ; Load 255 into A
        STA $1000       ; Store A to memory
LOOP    DEC $1000       ; Decrement memory
        BNE LOOP        ; Branch if not zero
        HLT             ; Halt processor
        END
```

---

## 🔗 Sınıflar Arası İlişkiler

### Dependency Graph
```
App
├── ConsoleUI (composition)
├── AssemblyParser (composition)
│   ├── SyntaxValidator (composition)
│   ├── LabelResolver (composition)
│   └── TokenParser (composition)
├── CodeGenerator (composition)
│   └── OpcodeTable (composition)
├── ExecutionEngine (composition)
│   └── CPU6800 (composition)
│       ├── Registers (composition)
│       └── Memory (composition)
└── FileManager (static usage)

Data Flow:
AssemblyLine ←→ Label, Instruction
Instruction ←→ AddressingMode
ExecutionResult ←→ ExecutionStatus, ExecutionStatistics
```

### Communication Patterns

1. **App → All Components**: Orchestration ve koordinasyon
2. **Parser → Core Models**: AssemblyLine, Instruction, Label oluşturma
3. **CodeGenerator → OpcodeTable**: Opcode lookup
4. **ExecutionEngine → CPU6800**: Low-level execution delegation
5. **ConsoleUI → All**: User interaction ve display formatting

Bu detaylı analiz, her sınıfın sorumluluklarını, metodlarını ve diğer sınıflarla ilişkilerini kapsamlı bir şekilde açıklamaktadır.
