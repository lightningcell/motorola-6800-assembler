# UI Paketi

`assembler.ui` paketi, kullanıcı ile uygulamanın etkileşimini sağlayan kullanıcı arayüzü bileşenlerini içerir. Şu anda console-based interface sunar, gelecekte GUI desteği planlanıyor.

## 📦 Paket İçeriği

| Sınıf | Açıklama | Sorumluluk |
|-------|----------|------------|
| **ConsoleUI** | Console-based kullanıcı arayüzü | Menü sistemi, input/output handling, formatting |

## 🖼️ UI Mimarisi

### 1. UI Katmanları

```
┌─────────────────────────────────────┐
│              App.java               │  ← Business Logic
│  - Main application flow            │
│  - Command processing               │
└─────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│            ConsoleUI                │  ← Presentation Layer
│  - Menu display                     │
│  - User input handling              │
│  - Output formatting                │
│  - Error display                    │
└─────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│           System.in/out             │  ← I/O Layer
│  - Console input/output             │
│  - Scanner for input                │
└─────────────────────────────────────┘
```

### 2. UI Design Principles

- **Separation of Concerns:** UI logic vs business logic ayrımı
- **User-Friendly:** Clear prompts ve meaningful error messages
- **Consistent:** Uniform formatting ve menu structure
- **Extensible:** Future GUI implementation için interface hazır

## 🔧 ConsoleUI Sınıf Detayları

### Ana Menü Sistemi

```java
public class ConsoleUI {
    private final Scanner scanner;
    
    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
    }
    
    // Ana menü gösterimi
    public int showMainMenu();
    
    // Input handling
    public String getInput(String prompt);
    public String getMultiLineInput();
    
    // Output formatting
    public void showMessage(String message);
    public void showError(String error);
    public void showWelcome();
    
    // Specialized displays
    public void showMachineCode(List<AssemblyLine> program, Map<Integer, List<Integer>> machineCode);
    public void showRegisters(Registers registers);
    public void showMemory(Memory memory, int startAddress, int length);
    public void showExecutionResult(ExecutionResult result);
}
```

### Ana Menü Implementation

#### `showMainMenu()` - Ana Menü
```java
public int showMainMenu() {
    System.out.println("\n" + "=".repeat(50));
    System.out.println("         MOTOROLA 6800 ASSEMBLER");
    System.out.println("=".repeat(50));
    System.out.println();
    System.out.println("Main Menu:");
    System.out.println("1. Input Assembly Code");
    System.out.println("2. Load Program from File");
    System.out.println("3. Assemble Program");
    System.out.println("4. View Machine Code");
    System.out.println("5. Simulate Program");
    System.out.println("6. Save Program");
    System.out.println("7. Show Instruction Set");
    System.out.println("8. Create Example Program");
    System.out.println("0. Exit");
    System.out.println();
    
    return getIntInput("Enter your choice: ", 0, 8);
}
```

#### `showWelcome()` - Karşılama Mesajı
```java
public void showWelcome() {
    System.out.println();
    System.out.println("█".repeat(55));
    System.out.println("█                                                   █");
    System.out.println("█      Motorola 6800 Assembler & Simulator         █");
    System.out.println("█                   Version 1.0                    █");
    System.out.println("█                                                   █");
    System.out.println("█".repeat(55));
    System.out.println();
    System.out.println("Welcome to the Motorola 6800 development environment!");
    System.out.println("This tool provides:");
    System.out.println("• Complete 6800 instruction set support (72 instructions)");
    System.out.println("• Two-pass assembler with label resolution");
    System.out.println("• Full CPU simulation with step-by-step debugging");
    System.out.println("• Memory and register visualization");
    System.out.println("• File I/O for loading and saving programs");
    System.out.println();
}
```

### Input Handling

#### `getInput()` - Basic Input
```java
public String getInput(String prompt) {
    System.out.print(prompt);
    String input = scanner.nextLine().trim();
    return input;
}
```

#### `getMultiLineInput()` - Assembly Code Input
```java
public String getMultiLineInput() {
    StringBuilder code = new StringBuilder();
    String line;
    
    System.out.println("Enter assembly code (type 'END' on a line by itself to finish):");
    System.out.println("Tip: Use labels, comments, and proper syntax.");
    System.out.println("Example:");
    System.out.println("        ORG $0200");
    System.out.println("START   LDA #$FF");
    System.out.println("        STA $0300");
    System.out.println("        SWI");
    System.out.println("        END");
    System.out.println();
    
    int lineNumber = 1;
    do {
        System.out.printf("%3d: ", lineNumber);
        line = scanner.nextLine();
        
        if (!"END".equalsIgnoreCase(line.trim())) {
            code.append(line).append("\n");
            lineNumber++;
        }
    } while (!"END".equalsIgnoreCase(line.trim()));
    
    return code.toString();
}
```

#### `getIntInput()` - Validated Integer Input
```java
public int getIntInput(String prompt, int min, int max) {
    while (true) {
        try {
            System.out.print(prompt);
            int value = Integer.parseInt(scanner.nextLine().trim());
            
            if (value >= min && value <= max) {
                return value;
            } else {
                showError("Please enter a number between " + min + " and " + max);
            }
        } catch (NumberFormatException e) {
            showError("Invalid number format. Please try again.");
        }
    }
}
```

### Output Formatting

#### `showMachineCode()` - Machine Code Display
```java
public void showMachineCode(List<AssemblyLine> program, Map<Integer, List<Integer>> machineCode) {
    System.out.println("\n" + "=".repeat(80));
    System.out.println("                           MACHINE CODE LISTING");
    System.out.println("=".repeat(80));
    System.out.printf("%-8s %-12s %-20s %s%n", "Address", "Machine Code", "Assembly", "Comments");
    System.out.println("-".repeat(80));
    
    for (AssemblyLine line : program) {
        if (line.isEmpty() || line.isComment()) {
            // Show source line without address/machine code
            System.out.printf("%-8s %-12s %-20s %s%n", 
                            "", "", "", line.getSourceLine());
            continue;
        }
        
        int address = line.getAddress();
        List<Integer> bytes = machineCode.get(address);
        
        // Format address
        String addressStr = String.format("$%04X", address);
        
        // Format machine code bytes
        String machineCodeStr = "";
        if (bytes != null && !bytes.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Integer b : bytes) {
                sb.append(String.format("%02X ", b));
            }
            machineCodeStr = sb.toString().trim();
        }
        
        // Assembly instruction
        String assemblyStr = "";
        if (line.hasLabel()) {
            assemblyStr += line.getLabel().getName() + " ";
        }
        if (line.getInstruction() != null) {
            assemblyStr += line.getInstruction().getMnemonic();
            if (line.getInstruction().getOperand() != null) {
                assemblyStr += " " + line.getInstruction().getOperand();
            }
        }
        
        // Comment
        String commentStr = line.getComment() != null ? line.getComment() : "";
        
        System.out.printf("%-8s %-12s %-20s %s%n", 
                         addressStr, machineCodeStr, assemblyStr, commentStr);
    }
    
    System.out.println("-".repeat(80));
    System.out.println("Total bytes: " + getTotalBytes(machineCode));
}
```

#### `showRegisters()` - CPU Register Display
```java
public void showRegisters(Registers registers) {
    System.out.println("\n" + "=".repeat(50));
    System.out.println("              CPU REGISTERS");
    System.out.println("=".repeat(50));
    
    // 8-bit registers
    System.out.printf("Accumulator A: $%02X (%3d) [%s]%n", 
                     registers.getAccumulatorA(), 
                     registers.getAccumulatorA(),
                     toBinary8(registers.getAccumulatorA()));
    
    System.out.printf("Accumulator B: $%02X (%3d) [%s]%n", 
                     registers.getAccumulatorB(), 
                     registers.getAccumulatorB(),
                     toBinary8(registers.getAccumulatorB()));
    
    // 16-bit registers
    System.out.printf("Index Register: $%04X (%5d)%n", 
                     registers.getIndexRegister(), 
                     registers.getIndexRegister());
    
    System.out.printf("Stack Pointer:  $%04X (%5d)%n", 
                     registers.getStackPointer(), 
                     registers.getStackPointer());
    
    System.out.printf("Program Counter: $%04X (%5d)%n", 
                     registers.getProgramCounter(), 
                     registers.getProgramCounter());
    
    // Condition Code Register
    System.out.println();
    System.out.println("Condition Code Register (CCR):");
    System.out.printf("  H I N Z V C%n");
    System.out.printf("  %s %s %s %s %s %s%n",
                     registers.isHalfCarryFlag() ? "1" : "0",
                     registers.isInterruptFlag() ? "1" : "0", 
                     registers.isNegativeFlag() ? "1" : "0",
                     registers.isZeroFlag() ? "1" : "0",
                     registers.isOverflowFlag() ? "1" : "0",
                     registers.isCarryFlag() ? "1" : "0");
    
    System.out.println();
    System.out.println("  H = Half Carry, I = Interrupt Mask");
    System.out.println("  N = Negative,   Z = Zero");
    System.out.println("  V = Overflow,   C = Carry");
}
```

#### `showMemory()` - Memory Dump Display
```java
public void showMemory(Memory memory, int startAddress, int length) {
    System.out.println("\n" + "=".repeat(70));
    System.out.printf("                    MEMORY DUMP ($%04X - $%04X)%n", 
                     startAddress, startAddress + length - 1);
    System.out.println("=".repeat(70));
    
    System.out.print("       ");
    for (int i = 0; i < 16; i++) {
        System.out.printf(" %02X", i);
    }
    System.out.println();
    
    for (int row = 0; row < (length + 15) / 16; row++) {
        int rowAddress = startAddress + (row * 16);
        System.out.printf("$%04X:", rowAddress);
        
        // Hex values
        for (int col = 0; col < 16; col++) {
            int address = rowAddress + col;
            if (address < startAddress + length && address < Memory.MEMORY_SIZE) {
                int value = memory.readByte(address);
                System.out.printf(" %02X", value);
            } else {
                System.out.print("   ");
            }
        }
        
        System.out.print("  ");
        
        // ASCII representation
        for (int col = 0; col < 16; col++) {
            int address = rowAddress + col;
            if (address < startAddress + length && address < Memory.MEMORY_SIZE) {
                int value = memory.readByte(address);
                char c = (value >= 32 && value <= 126) ? (char) value : '.';
                System.out.print(c);
            } else {
                System.out.print(" ");
            }
        }
        
        System.out.println();
    }
}
```

### Simulator UI

#### `showSimulatorMenu()` - Simulator Menü
```java
public int showSimulatorMenu() {
    System.out.println("\n" + "-".repeat(40));
    System.out.println("          SIMULATION MODE");
    System.out.println("-".repeat(40));
    System.out.println("1. Step Execution (single instruction)");
    System.out.println("2. Run Program (until halt/breakpoint)");
    System.out.println("3. View Registers");
    System.out.println("4. View Memory");
    System.out.println("5. Manage Breakpoints");
    System.out.println("6. Reset Simulation");
    System.out.println("0. Return to Main Menu");
    System.out.println();
    
    return getIntInput("Enter your choice: ", 0, 6);
}
```

#### `showExecutionResult()` - Execution Status Display
```java
public void showExecutionResult(ExecutionResult result) {
    ExecutionStatus status = result.getStatus();
    int pc = result.getProgramCounter();
    String message = result.getMessage();
    
    System.out.println();
    
    switch (status) {
        case RUNNING:
            System.out.printf("✓ [PC=$%04X] %s%n", pc, message);
            break;
            
        case HALTED:
            System.out.printf("⏹ [PC=$%04X] %s%n", pc, message);
            break;
            
        case BREAKPOINT:
            System.out.printf("⏸ [PC=$%04X] %s%n", pc, message);
            break;
            
        case ERROR:
            System.out.printf("❌ [PC=$%04X] %s%n", pc, message);
            break;
            
        case STOPPED:
            System.out.printf("⏹ [PC=$%04X] %s%n", pc, message);
            break;
    }
    
    // Show source line if available
    if (result.getAssemblyLine() != null) {
        AssemblyLine line = result.getAssemblyLine();
        System.out.printf("   Source: %s%n", line.getSourceLine());
    }
}
```

### Error Handling ve Messaging

#### `showError()` - Error Display
```java
public void showError(String error) {
    System.err.println();
    System.err.println("❌ ERROR: " + error);
    System.err.println();
}
```

#### `showMessage()` - General Message Display
```java
public void showMessage(String message) {
    System.out.println("ℹ️  " + message);
}
```

### Utility Methods

#### Binary Format Helper
```java
private String toBinary8(int value) {
    return String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0');
}
```

#### Total Bytes Calculator
```java
private int getTotalBytes(Map<Integer, List<Integer>> machineCode) {
    return machineCode.values().stream()
                     .mapToInt(List::size)
                     .sum();
}
```

## 🎨 UI Design Patterns

### 1. Menu-Driven Interface
```
Main Menu → Sub Menu → Action → Result Display → Return to Menu
```

### 2. Input Validation
```java
// Consistent input validation across all prompts
public String getValidatedInput(String prompt, Predicate<String> validator, String errorMessage) {
    while (true) {
        String input = getInput(prompt);
        if (validator.test(input)) {
            return input;
        }
        showError(errorMessage);
    }
}
```

### 3. Formatted Output
```java
// Consistent formatting for data display
public void showFormattedData(String title, Map<String, String> data) {
    System.out.println("\n" + "=".repeat(title.length() + 4));
    System.out.println("  " + title);
    System.out.println("=".repeat(title.length() + 4));
    
    data.forEach((key, value) -> 
        System.out.printf("%-20s: %s%n", key, value));
}
```

## 🧪 UI Test Scenarios

### 1. Menu Navigation Test
```java
// Simulate user input sequence
String[] inputs = {"1", "ORG $0200", "LDA #$FF", "SWI", "END", "3", "0"};
// Test: Input code → Assemble → Exit
```

### 2. Error Handling Test
```java
// Test invalid menu choices
String[] invalidInputs = {"-1", "99", "abc", ""};
// Should show error and prompt again
```

### 3. Output Formatting Test
```java
// Verify machine code display format
// Check register display alignment  
// Validate memory dump layout
```

## 🚀 Future UI Enhancements

### 1. GUI Implementation (JavaFX)
```java
public interface UserInterface {
    void showMessage(String message);
    void showError(String error);
    int showMainMenu();
    // ... other methods
}

public class ConsoleUI implements UserInterface { ... }
public class GraphicalUI implements UserInterface { ... }  // Future
```

### 2. Enhanced Debugging
```java
// Source-level debugging
public void showSourceWithCurrentLine(List<AssemblyLine> program, int currentPC);

// Call stack display
public void showCallStack(List<Integer> callStack);

// Variable watch window
public void showWatchedVariables(Map<String, Integer> variables);
```

### 3. Customizable Display
```java
// User preferences
public class UISettings {
    private boolean showMachineCodeInHex;
    private boolean showBinaryValues;
    private int memoryDisplayWidth;
    private boolean colorOutput;
}
```

---

UI paketi, kullanıcı deneyimini yöneten kritik bir bileşendir. Clear, consistent ve user-friendly interface sağlar. Future GUI implementation için uygun foundation hazırlanmıştır.
