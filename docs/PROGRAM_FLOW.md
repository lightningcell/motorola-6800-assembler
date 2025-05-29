# Program Akışı

Bu dokümant, Motorola 6800 Assembler uygulamasının nasıl çalıştığını adım adım açıklar.

## 🎯 Ana Program Akışı

### 1. Uygulama Başlangıcı

```java
public static void main(String[] args) {
    App app = new App();
    app.run();  // Ana döngüye geç
}
```

**App Constructor'da yapılanlar:**
```java
public App() {
    this.ui = new ConsoleUI();           // UI bileşeni
    this.parser = new AssemblyParser();  // Parser bileşeni  
    this.codeGenerator = new CodeGenerator(); // Code generator
    this.simulator = new ExecutionEngine();  // Simülasyon motoru
    // Veri yapıları initialize
}
```

### 2. Ana Menü Döngüsü

```
┌─────────────────────────────────────┐
│             MAIN LOOP               │
│                                     │
│  ┌─────────────────────────────┐   │
│  │     showMainMenu()          │   │
│  │   1. Input Assembly Code    │   │
│  │   2. Load from File         │   │
│  │   3. Assemble Program       │   │
│  │   4. View Machine Code      │   │
│  │   5. Simulate Program       │   │
│  │   6. Save Program           │   │
│  │   7. Show Instruction Set   │   │
│  │   8. Create Example         │   │
│  │   0. Exit                   │   │
│  └─────────────────────────────┘   │
│              │                      │
│              ▼                      │
│  ┌─────────────────────────────┐   │
│  │    Process User Choice      │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

## 📝 Assembly İşlem Süreci

### Seçenek 1: Assembly Code Girişi

```java
private void inputAssemblyCode() {
    // Kullanıcıdan multi-line input al
    sourceCode = ui.getMultiLineInput();
    // "END" ile bitirmeli
}
```

### Seçenek 2: Dosyadan Yükleme

```java
private void loadProgramFromFile() {
    String filename = ui.getInput("Enter filename: ");
    sourceCode = FileManager.loadSourceFile(filename);
}
```

### Seçenek 3: Assembly İşlemi (EN ÖNEMLİ)

Bu, uygulamanın kalbi olan işlemdir:

```java
private void assembleProgram() {
    // 1. KAYNAK KODU PARSE ET
    currentProgram = parser.parseSource(sourceCode);
    
    // 2. ADRES HESAPLA (First Pass)
    calculateAddresses(currentProgram);
    
    // 3. LABEL'LARI RESOLVE ET (Second Pass)  
    parser.resolveLabelReferences(currentProgram);
    
    // 4. MACHINE CODE ÜRET
    machineCode = codeGenerator.generateCode(currentProgram);
}
```

## 🔍 Detaylı Assembly İşlem Akışı

### 1. Parse Source Code

```
Source Code (String)
    │
    ▼
AssemblyParser.parseSource()
    │
    ├─► TokenParser.tokenize()      # Lexical analysis
    ├─► SyntaxValidator.validate()  # Syntax check  
    └─► AssemblyLine objects oluştur
    │
    ▼
List<AssemblyLine>
```

**Örnek Parse İşlemi:**
```assembly
START   LDA #$FF    ; Load immediate value
```

Parse ediliyor:
- **Label:** "START"  
- **Instruction:** "LDA" 
- **Addressing Mode:** IMMEDIATE
- **Operand:** "$FF"
- **Comment:** "; Load immediate value"

### 2. Address Calculation

```java
private void calculateAddresses(List<AssemblyLine> assemblyLines) {
    int currentAddress = 0;
    
    for (AssemblyLine line : assemblyLines) {
        // ORG pseudo-instruction kontrolü
        if (line.isPseudoOp() && "ORG".equals(line.getPseudoOp())) {
            currentAddress = (Integer) line.getPseudoOperand();
        }
        
        line.setAddress(currentAddress);  // Satıra adres ata
        currentAddress += calculateLineSize(line); // Sonraki adres
    }
}
```

**Address Calculation Örneği:**
```
Address | Assembly Code      | Size | Next Address
--------|-------------------|------|-------------
$0200   | ORG $0200        | 0    | $0200
$0200   | START: LDA #$FF  | 2    | $0202  
$0202   | STA $0300        | 3    | $0205
$0205   | SWI              | 1    | $0206
```

### 3. Label Resolution

```java
// İki geçişli label resolution
// Pass 1: Label'ları topla
for (AssemblyLine line : assemblyLines) {
    if (line.hasLabel()) {
        labelResolver.addLabel(line.getLabel().getName(), line.getAddress());
    }
}

// Pass 2: Referansları resolve et
parser.resolveLabelReferences(currentProgram);
```

**Label Resolution Örneği:**
```assembly
        ORG $0200
START   LDA #$FF      ; START label'ı $0200 adresinde
        JMP START     ; JMP instruction'ı START'ı $0200 olarak resolve eder
```

### 4. Machine Code Generation

```java
public Map<Integer, List<Integer>> generateCode(List<AssemblyLine> assemblyLines) {
    Map<Integer, List<Integer>> result = new HashMap<>();
    
    for (AssemblyLine line : assemblyLines) {
        if (line.getInstruction() != null) {
            List<Integer> bytes = generateInstructionBytes(line);
            result.put(line.getAddress(), bytes);
        }
    }
    return result;
}
```

**Machine Code Generation Örneği:**
```
Assembly: LDA #$FF
Opcode Lookup: LDA + IMMEDIATE = $86
Result: [$86, $FF]

Assembly: STA $0300  
Opcode Lookup: STA + EXTENDED = $B7
Result: [$B7, $03, $00]
```

## 🖥️ Simülasyon İşlem Akışı

### Simulation Entry Point

```java
private void simulateProgram() {
    // Program'ı simülatöre yükle
    simulator.loadProgram(currentProgram);
    
    // Simülasyon menü döngüsü
    while (simulating) {
        int choice = ui.showSimulatorMenu();
        // 1. Step Execution
        // 2. Run Program  
        // 3. View Registers
        // 4. View Memory
        // 5. Manage Breakpoints
        // 6. Reset
    }
}
```

### Step Execution

```java
private void stepExecution() {
    if (simulator.isHalted()) return;
    
    ExecutionResult result = simulator.step();  // Bir instruction execute et
    ui.showExecutionResult(result);             // Sonucu göster
}
```

**Step Execution Süreci:**
```
1. PC register'dan instruction fetch et
2. Instruction'ı decode et  
3. Operand'ları al
4. Instruction'ı execute et
5. Register'ları ve flag'ları güncelle
6. PC'yi advance et
7. Breakpoint kontrolü yap
```

### CPU6800 Execution Cycle

```java
public boolean step() {
    // 1. FETCH
    int opcode = memory.readByte(registers.getProgramCounter());
    
    // 2. DECODE  
    Instruction instruction = opcodeTable.getInstructionByOpcode(opcode);
    
    // 3. EXECUTE
    executeInstruction(instruction);
    
    // 4. UPDATE PC
    registers.setProgramCounter(pc + instructionSize);
    
    return !halted;
}
```

## 📊 Veri Akışı Diyagramı

```
┌─────────────┐    parse     ┌─────────────┐    resolve   ┌─────────────┐
│ Source Code │─────────────►│AssemblyLine │─────────────►│ Resolved    │
│   (String)  │              │   Objects   │              │  Program    │  
└─────────────┘              └─────────────┘              └─────────────┘
       │                            │                            │
       │                            │                            │
       ▼                            ▼                            ▼
┌─────────────┐              ┌─────────────┐              ┌─────────────┐
│    User     │              │   Labels    │              │ Machine     │
│   Input     │              │   Table     │              │   Code      │
└─────────────┘              └─────────────┘              └─────────────┘
                                     │                            │
                                     │                            │
                                     ▼                            ▼
                              ┌─────────────┐              ┌─────────────┐
                              │Label        │              │CPU6800      │
                              │Resolution   │              │Simulator    │
                              └─────────────┘              └─────────────┘
```

## 🔧 Error Handling Akışı

### Parse Errors
```java
try {
    currentProgram = parser.parseSource(sourceCode);
} catch (ParseException e) {
    ui.showError("Parse error: " + e.getMessage());
    return; // İşlemi durdur
}
```

### Code Generation Errors
```java
try {
    machineCode = codeGenerator.generateCode(currentProgram);
} catch (CodeGenerationException e) {
    ui.showError("Code generation error: " + e.getMessage());
    return;
}
```

### Runtime Errors
```java
// Simülasyon sırasında
if (invalidMemoryAccess) {
    return new ExecutionResult(ExecutionStatus.ERROR, pc, 
                              "Invalid memory access", null);
}
```

## 🎮 User Interaction Flow

```
User Action → ConsoleUI → App.java → Core Logic → Result → ConsoleUI → User
```

**Örnek Flow:**
1. **User:** "3" (Assemble Program) tuşlar
2. **ConsoleUI:** Menu choice'ı App'e geçer
3. **App:** `assembleProgram()` çağırır
4. **Core Logic:** Parse → Resolve → Generate 
5. **Result:** Machine code oluşturulur
6. **ConsoleUI:** "Assembly completed!" mesajı
7. **User:** Sonucu görür

## 🔄 State Management

### Application State
```java
private List<AssemblyLine> currentProgram;     // Parse edilmiş program
private Map<Integer, List<Integer>> machineCode; // Üretilen machine code  
private String sourceCode;                      // Kaynak kod
```

### Simulator State
```java
private Registers registers;                    // CPU register'ları
private Memory memory;                         // 64KB memory
private Set<Integer> breakpoints;              // Debug breakpoint'ler
private boolean running, halted;               // Execution state
```

Bu akış, her assembly programının doğru şekilde parse, assemble ve execute edilmesini sağlar. Her adım hata kontrolü yapar ve kullanıcıya meaningful feedback verir.
