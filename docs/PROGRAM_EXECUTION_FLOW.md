# Program Akışı ve Execution Flow

Bu dokümant, Motorola 6800 Assembler uygulamasının nasıl çalıştığını, hangi metotların hangi sırada çağrıldığını ve veri akışının nasıl gerçekleştiğini detaylı olarak açıklar.

## 🎯 Genel Program Akışı Diyagramı

```
[main()] 
    │
    ▼
[App Constructor]
    │
    ├── new ConsoleUI()
    ├── new AssemblyParser()  
    ├── new CodeGenerator()
    └── new ExecutionEngine()
    │
    ▼
[App.run()] ◄──────────────────────┐
    │                              │
    ▼                              │
[ui.showMainMenu()] ────► [Kullanıcı Seçimi] ──┤
    │                              │
    ├─1─► [inputAssemblyCode()]     │
    ├─2─► [loadProgramFromFile()]   │
    ├─3─► [assembleProgram()]       │ 
    ├─4─► [viewMachineCode()]       │
    ├─5─► [simulateProgram()]       │
    ├─6─► [saveProgram()]           │
    ├─7─► [showInstructionSet()]    │
    ├─8─► [createExampleProgram()]  │
    └─0─► [exit] ───────────────────┘
```

## 🔧 Uygulama Başlangıcı (Application Startup)

### 1. main() Method Execution

```java
public static void main(String[] args) {
    App app = new App();  // Constructor chain başlar
    app.run();           // Ana döngü başlatılır
}
```

### 2. App Constructor Initialization

```java
public App() {
    // UI bileşeni initialize edilir
    this.ui = new ConsoleUI();           
    
    // Parser ve alt bileşenleri initialize edilir
    this.parser = new AssemblyParser();  
    // └── new SyntaxValidator()
    // └── new LabelResolver()  
    // └── new TokenParser()
    
    // Code generation bileşeni initialize edilir
    this.codeGenerator = new CodeGenerator();
    // └── new OpcodeTable() (197 opcode yüklenir)
    
    // Simulation engine initialize edilir  
    this.simulator = new ExecutionEngine();
    // └── new CPU6800()
    //     ├── new Registers()
    //     └── new Memory() (64KB allocate edilir)
    
    // Data structures initialize edilir
    this.currentProgram = new ArrayList<>();
    this.machineCode = new HashMap<>();
    this.sourceCode = "";
}
```

### 3. Ana Döngü Başlatma

```java
public void run() {
    ui.showWelcome();  // Hoşgeldin mesajı
    
    boolean running = true;
    while (running) {
        try {
            int choice = ui.showMainMenu();  // Menü göster ve seçim al
            
            switch (choice) {
                case 1: inputAssemblyCode(); break;
                case 2: loadProgramFromFile(); break;
                case 3: assembleProgram(); break;    // ★ Ana işlem
                case 4: viewMachineCode(); break;
                case 5: simulateProgram(); break;    // ★ Simülasyon
                case 6: saveProgram(); break;
                case 7: showInstructionSet(); break;
                case 8: createExampleProgram(); break;
                case 0: running = false; break;
            }
        } catch (Exception e) {
            ui.showError("An error occurred: " + e.getMessage());
        }
    }
}
```

## 📝 Assembly İşlem Süreci (Seçenek 3)

Bu, uygulamanın en kritik işlevidir. İki geçişli assembly algoritmasını uygular.

### Assembly İşlem Akışı

```
[assembleProgram()] 
    │
    ├── Validation: sourceCode boş mu?
    │
    ▼
[1. PARSING PHASE]
    │
    ├── parser.parseSource(sourceCode)
    │   │
    │   ├── String[] lines = sourceCode.split("\n")
    │   │
    │   └── For each line:
    │       ├── parseLine(line, lineNumber)
    │       │   ├── Remove comments (;'den sonrası)
    │       │   ├── tokenParser.tokenize(line)
    │       │   ├── Check for label (: ile bitenler)
    │       │   ├── Detect pseudo-op vs instruction
    │       │   └── Create AssemblyLine object
    │       │
    │       └── Add to currentProgram list
    │
    ▼
[2. ADDRESS CALCULATION]
    │
    ├── calculateAddresses(currentProgram)
    │   │
    │   └── For each AssemblyLine:
    │       ├── Handle ORG pseudo-instruction
    │       ├── Set line.address = currentAddress
    │       ├── Calculate instruction size
    │       └── currentAddress += size
    │
    ▼
[3. LABEL RESOLUTION]
    │
    ├── parser.resolveLabelReferences(currentProgram)
    │   │
    │   ├── First Pass: Collect all label definitions
    │   │   └── For each line with label:
    │   │       └── labelMap.put(labelName, address)
    │   │
    │   └── Second Pass: Resolve all references
    │       └── For each unresolved reference:
    │           ├── Find label in labelMap
    │           ├── Calculate relative/absolute address
    │           └── Update instruction operand
    │
    ▼
[4. CODE GENERATION]
    │
    ├── codeGenerator.generateCode(currentProgram)
    │   │
    │   └── For each AssemblyLine:
    │       ├── generateLineCode(line)
    │       │   ├── Handle pseudo-ops (ORG, FCB, FDB, etc.)
    │       │   └── Handle instructions:
    │       │       ├── opcodeTable.getInstruction(mnemonic, mode)
    │       │       ├── Encode operand based on addressing mode
    │       │       └── Create byte array [opcode, operand_bytes...]
    │       │
    │       └── Add to machineCode map
    │
    ▼
[SUCCESS OUTPUT]
    │
    ├── ui.showMessage("Assembly completed successfully!")
    ├── ui.showMessage("Program contains " + currentProgram.size() + " lines")
    └── ui.showMessage("Generated " + getTotalBytes() + " bytes of machine code")
```

### Detaylı Parsing Süreci

#### tokenParser.tokenize() İşlemi

```java
Input: "LOOP    LDA $30,X    ; Load from memory"

Tokenization:
├── Remove comments: "LOOP    LDA $30,X"
├── Split by whitespace: ["LOOP", "LDA", "$30,X"]
└── Return token list

Output: ["LOOP", "LDA", "$30,X"]
```

#### parseLine() Label Detection

```java
Input tokens: ["LOOP:", "LDA", "$30,X"]

Processing:
├── tokens[0].endsWith(":") → true
├── labelName = "LOOP" (remove :)
├── validator.validateLabelName("LOOP")
├── assemblyLine.setLabel(new Label("LOOP"))
└── tokenIndex = 1 (move to instruction)
```

#### Addressing Mode Detection

```java
Input: mnemonic="LDA", operand="$30,X"

determineAddressingMode():
├── operand.endsWith(",X") → true
├── return AddressingMode.INDEXED
└── Validate: opcodeTable.hasInstruction("LDA", INDEXED)
```

## 🎮 Simülasyon Süreci (Seçenek 5)

### Simülasyon Başlatma

```
[simulateProgram()]
    │
    ├── Validation: currentProgram boş mu?
    │
    ▼
[PROGRAM LOADING]
    │
    ├── simulator.loadProgram(currentProgram)
    │   │
    │   ├── codeGenerator.generateCode(assemblyLines)
    │   ├── Build addressToLineMap
    │   ├── Find program start address (first ORG)
    │   ├── cpu.loadProgram(startAddress, machineCode)
    │   │   └── memory.loadProgram() → bytes to memory
    │   └── Reset execution statistics
    │
    ▼
[SIMULATION LOOP]
    │
    └── while (simulating):
        ├── ui.showSimulatorMenu()
        └── Process choice:
            ├─1─► stepExecution()
            ├─2─► runProgram()  
            ├─3─► viewRegisters()
            ├─4─► viewMemory()
            ├─5─► manageBreakpoints()
            ├─6─► resetSimulation()
            └─0─► exit simulation
```

### Step Execution Detayı

```
[stepExecution()]
    │
    ├── Check: simulator.isHalted() ?
    │
    ▼
[simulator.step()]
    │
    ├── Get current PC
    ├── Find current AssemblyLine from addressToLineMap
    │
    ▼
[cpu.step()] ─────► [6800 CPU CYCLE]
    │                   │
    │                   ├── FETCH: memory.readByte(PC)
    │                   ├── DECODE: lookup instruction
    │                   ├── EXECUTE: perform operation
    │                   │   ├── Update registers
    │                   │   ├── Update flags
    │                   │   └── Update PC
    │                   └── Return success/failure
    │
    ▼
[ExecutionResult]
    │
    ├── Create ExecutionResult object
    │   ├── status (RUNNING/HALTED/BREAKPOINT)
    │   ├── current PC value  
    │   ├── executed AssemblyLine
    │   └── status message
    │
    ▼
[ui.showExecutionResult()]
    │
    └── Display execution status and line info
```

### Run Program (Continuous Execution)

```
[runProgram()]
    │
    ▼
[simulator.run()]
    │
    └── while (!cpu.isHalted()):
        ├── Check breakpoint at current PC
        ├── If breakpoint found:
        │   ├── status = BREAKPOINT
        │   └── break loop
        ├── cpu.step()
        ├── instructionsExecuted++
        └── Update statistics
    │
    ▼
[Display Results]
    │
    ├── ui.showExecutionResult(finalResult)
    ├── ExecutionStatistics stats = simulator.getStatistics()
    └── ui.showMessage(stats.getFormattedStats())
```

## 💾 Dosya İşlemleri

### Program Yükleme (Seçenek 2)

```
[loadProgramFromFile()]
    │
    ├── filename = ui.getInput("Enter filename: ")
    │
    ▼
[FileManager.loadSourceFile(filename)]
    │
    ├── Path path = Paths.get(filename)
    ├── Check file exists
    ├── sourceCode = Files.readString(path)
    └── Return source code string
    │
    ▼
[Success]
    │
    ├── this.sourceCode = loaded content
    └── ui.showMessage("Program loaded successfully")
```

### Program Kaydetme (Seçenek 6)

```
[saveProgram()]
    │
    ├── Validation: sourceCode boş mu?
    ├── filename = ui.getInput("Enter filename: ")
    │
    ▼
[FileManager.saveSourceFile(filename, sourceCode)]
    │
    ├── Save assembly source (.asm)
    │
    ▼
[Additional: Save machine code if available]
    │
    ├── If machineCode not empty:
    │   ├── hexFile = filename.replace(".asm", ".hex")
    │   └── FileManager.saveHexFile(hexFile, machineCode)
    │       │
    │       └── Intel HEX Format:
    │           ├── :10010000862FB710004F20FB96309731...
    │           ├── :10011000C6FFD73086FFB7100001000...
    │           └── :00000001FF
    │
    ▼
[Success Messages]
    │
    ├── ui.showMessage("Program saved to: " + filename)
    └── ui.showMessage("Machine code saved to: " + hexFile)
```

## 🔍 Memory ve Register Inspection

### Register Görüntüleme

```
[viewRegisters()]
    │
    ├── Registers registers = simulator.getRegisters()
    │
    ▼
[ui.showRegisters(registers)]
    │
    └── Format and display:
        ├── "A=$FF B=$00 X=$0000 SP=$01FF PC=$0105"
        ├── "Flags: N=1 Z=0 V=0 C=0 I=0 H=0"
        └── Additional register details
```

### Memory Dump

```
[viewMemory()]
    │
    ├── input = ui.getInput("Enter start address (hex): ")
    ├── startAddr = Integer.parseInt(input, 16)
    │
    ▼
[ui.showMemory(memory, startAddr, 16)]
    │
    └── Display hex dump:
        "Address  +0 +1 +2 +3 +4 +5 +6 +7  +8 +9 +A +B +C +D +E +F"
        "0100     86 FF B7 10 00 4F 20 FB  96 30 97 31 7E 01 06 00"
```

## 🐛 Breakpoint Yönetimi

### Breakpoint Ekleme

```
[addBreakpoint()]
    │
    ├── input = ui.getInput("Enter breakpoint address (hex): ")
    ├── address = Integer.parseInt(input, 16)
    │
    ▼
[simulator.addBreakpoint(address)]
    │
    ├── cpu.addBreakpoint(address)
    │   └── breakpoints.add(address)
    │
    └── ui.showMessage("Breakpoint added at 0x" + address)
```

### Breakpoint Kontrolü (Run sırasında)

```
[During simulator.run()]
    │
    └── Each step:
        ├── int currentPC = cpu.getProgramCounter()
        ├── if (breakpoints.contains(currentPC)):
        │   ├── status = BREAKPOINT
        │   ├── message = "Breakpoint hit at 0x" + currentPC
        │   └── break execution loop
        └── continue execution
```

## 📊 Execution Statistics

### İstatistik Toplama

```
[During execution]
    │
    ├── instructionsExecuted++
    ├── cyclesExecuted += instruction.getCycles()
    ├── currentTime = System.currentTimeMillis()
    ├── executionTime = currentTime - startTime
    └── averageSpeed = instructionsExecuted / (executionTime / 1000.0)
```

### İstatistik Görüntüleme

```
[stats.getFormattedStats()]
    │
    └── Return formatted string:
        "Instructions: 1,234"
        "Cycles: 5,678"  
        "Time: 1.23 seconds"
        "Speed: 1,002 instructions/sec"
```

## 🔧 Error Handling Flow

### Parse Error Handling

```
[During assembleProgram()]
    │
    └── try {
        ├── parsing operations...
        } catch (ParseException e) {
        ├── ui.showError("Parse error: " + e.getMessage())
        └── return to main menu
        }
```

### Simulation Error Handling

```
[During simulation]
    │
    └── try {
        ├── simulation operations...
        } catch (CodeGenerationException e) {
        ├── ui.showError("Failed to load program: " + e.getMessage())
        └── return to main menu
        }
```

## 🎯 Veri Akışı Özeti

```
User Input → UI → App → Parser → Core Models → CodeGenerator → Machine Code
                    ↓
                Simulator ← Memory ← CPU ← Execution Results → UI → User
```

Bu detaylı program akışı analizi, uygulamanın her adımında ne olduğunu, hangi metotların çağrıldığını ve verilerin nasıl işlendiğini göstermektedir. Yeni geliştiriciler bu dokümantı kullanarak kodun nasıl çalıştığını hızlı bir şekilde anlayabilirler.
