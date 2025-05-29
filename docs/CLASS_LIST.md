# Class Reference Guide

This document provides a comprehensive reference for all classes in the Motorola 6800 Assembler project, organized by package and functionality.

## Quick Navigation

- [Core Package Classes](#core-package-classes)
- [Parser Package Classes](#parser-package-classes)
- [Assembler Package Classes](#assembler-package-classes)
- [Simulator Package Classes](#simulator-package-classes)
- [UI Package Classes](#ui-package-classes)
- [Util Package Classes](#util-package-classes)
- [Main Application](#main-application)

---

## Core Package Classes

### AddressingMode
**Package**: `assembler.core`  
**Purpose**: Enumeration defining the different addressing modes supported by the Motorola 6800 processor.

**Key Elements**:
```java
IMMEDIATE      // #$FF - Immediate addressing
DIRECT         // $FF - Direct/Zero page addressing  
EXTENDED       // $FFFF - Extended/Absolute addressing
INDEXED        // $FF,X - Indexed addressing
INHERENT       // No operand - Inherent addressing
RELATIVE       // $FF - Relative addressing (for branches)
```

**Usage**: Used throughout the assembler to determine how to encode operands and calculate instruction sizes.

---

### Instruction
**Package**: `assembler.core`  
**Purpose**: Represents a single 6800 assembly instruction with its components.

**Key Fields**:
- `String mnemonic`: Instruction name (LDA, STA, etc.)
- `AddressingMode addressingMode`: How operand is addressed
- `String operand`: The operand string
- `int address`: Memory address where instruction is located
- `List<Integer> machineCode`: Generated machine code bytes

**Key Methods**:
```java
public Instruction(String mnemonic, AddressingMode mode, String operand)
public int getSize()                    // Returns instruction size in bytes
public boolean isValid()                // Validates instruction format
public String toString()                // Human-readable representation
```

---

### Label
**Package**: `assembler.core`  
**Purpose**: Represents assembly labels with their names and memory addresses.

**Key Fields**:
- `String name`: Label identifier
- `int address`: Memory address the label points to
- `boolean isDefined`: Whether the label has been resolved

**Key Methods**:
```java
public Label(String name)
public void setAddress(int address)
public boolean isResolved()
public int getValue()                   // Returns resolved address
```

---

### Memory
**Package**: `assembler.core`  
**Purpose**: Models the 6800's memory space with read/write operations.

**Key Fields**:
- `byte[] memory`: 64KB memory array
- `Map<Integer, String> labels`: Memory location labels
- `Set<Integer> breakpoints`: Debug breakpoints

**Key Methods**:
```java
public byte read(int address)
public void write(int address, byte value)
public void loadProgram(Map<Integer, List<Integer>> machineCode)
public void setBreakpoint(int address)
public void reset()                     // Clear all memory
```

---

### Registers
**Package**: `assembler.core`  
**Purpose**: Models all 6800 processor registers and status flags.

**Key Fields**:
- `int accumulator`: 8-bit accumulator register
- `int indexRegister`: 16-bit index register
- `int programCounter`: 16-bit program counter
- `int stackPointer`: 16-bit stack pointer
- `StatusFlags flags`: Processor status flags

**Key Methods**:
```java
public void setAccumulator(int value)
public int getAccumulator()
public void setProgramCounter(int address)
public void updateFlags(int result)     // Update status flags
public void reset()                     // Reset all registers
```

---

## Parser Package Classes

### AssemblyParser
**Package**: `assembler.parser`  
**Purpose**: Main parser class that coordinates the parsing process using a two-pass algorithm.

**Key Methods**:
```java
public List<AssemblyLine> parseProgram(String sourceCode) throws ParseException
public Map<String, Label> getLabels()
public List<String> getErrors()
public void setOrigin(int address)      // Set ORG address
```

**Key Features**:
- Two-pass parsing algorithm
- Label resolution and forward reference handling
- Syntax validation and error reporting
- Support for pseudo-instructions (ORG, END, FCB, etc.)

---

### AssemblyLine
**Package**: `assembler.parser`  
**Purpose**: Represents a single line of assembly source code with all parsed components.

**Key Fields**:
- `int lineNumber`: Source line number
- `String originalLine`: Raw source text
- `String label`: Label on this line (if any)
- `String mnemonic`: Instruction mnemonic
- `String operand`: Instruction operand
- `String comment`: Line comment
- `int address`: Memory address for this line
- `List<Integer> machineCode`: Generated machine code

**Key Methods**:
```java
public boolean hasLabel()
public boolean hasInstruction()
public boolean isComment()
public boolean isPseudoOp()
public int getSize()                    // Size in bytes
```

---

### LabelResolver
**Package**: `assembler.parser`  
**Purpose**: Handles label definition, forward references, and address resolution.

**Key Methods**:
```java
public void defineLabel(String name, int address) throws ParseException
public int resolveLabel(String name) throws ParseException
public boolean isLabelDefined(String name)
public Map<String, Label> getAllLabels()
public List<String> getUnresolvedLabels()
```

**Key Features**:
- Two-pass label resolution
- Forward reference handling
- Duplicate label detection
- Circular reference detection

---

### ParseException
**Package**: `assembler.parser`  
**Purpose**: Custom exception for parser errors with detailed error information.

**Key Fields**:
- `String message`: Error description
- `int lineNumber`: Line where error occurred
- `String sourceLine`: The problematic source line
- `ErrorType type`: Category of error

**Key Methods**:
```java
public ParseException(String message, int lineNumber)
public String getDetailedMessage()
public ErrorType getErrorType()
```

---

### SyntaxValidator
**Package**: `assembler.parser`  
**Purpose**: Validates assembly syntax including mnemonics, operands, and addressing modes.

**Key Methods**:
```java
public boolean isValidMnemonic(String mnemonic)
public boolean isValidOperand(String operand, AddressingMode mode)
public boolean isValidLabel(String label)
public AddressingMode determineAddressingMode(String operand)
public List<String> validateInstruction(String mnemonic, String operand)
```

---

### TokenParser
**Package**: `assembler.parser`  
**Purpose**: Breaks down assembly lines into tokens (label, mnemonic, operand, comment).

**Key Methods**:
```java
public static Map<String, String> parseLine(String line)
public static String extractLabel(String line)
public static String extractMnemonic(String line)
public static String extractOperand(String line)
public static String extractComment(String line)
```

---

## Assembler Package Classes

### CodeGenerator
**Package**: `assembler.assembler`  
**Purpose**: Generates machine code from parsed assembly instructions.

**Key Methods**:
```java
public Map<Integer, List<Integer>> generateCode(List<AssemblyLine> lines) throws CodeGenerationException
public List<Integer> generateInstruction(Instruction instruction)
public int calculateInstructionSize(String mnemonic, AddressingMode mode)
```

**Key Features**:
- Machine code generation for all 6800 instructions
- Address calculation and encoding
- Operand encoding for different addressing modes
- Size optimization

---

### OpcodeTable
**Package**: `assembler.assembler`  
**Purpose**: Contains the complete opcode lookup table for Motorola 6800 instructions.

**Key Methods**:
```java
public static int getOpcode(String mnemonic, AddressingMode mode)
public static boolean isValidInstruction(String mnemonic, AddressingMode mode)
public static AddressingMode[] getSupportedModes(String mnemonic)
public static int getInstructionSize(String mnemonic, AddressingMode mode)
```

**Key Data**:
- Complete 6800 instruction set opcodes
- Addressing mode combinations
- Instruction timing information
- Size calculations

---

### CodeGenerationException
**Package**: `assembler.assembler`  
**Purpose**: Exception thrown during machine code generation.

**Key Fields**:
- `String instruction`: The problematic instruction
- `String reason`: Why code generation failed
- `int address`: Memory address where error occurred

---

## Simulator Package Classes

### ExecutionEngine
**Package**: `assembler.simulator`  
**Purpose**: Main simulation engine that executes 6800 machine code instruction by instruction.

**Key Methods**:
```java
public ExecutionResult executeStep() throws SimulationException
public void reset()
public void loadProgram(Map<Integer, List<Integer>> machineCode)
public ExecutionStatistics getStatistics()
public void setBreakpoint(int address)
```

**Key Features**:
- Step-by-step execution
- Breakpoint support
- Instruction execution statistics
- Register and memory state tracking

---

### CPU6800
**Package**: `assembler.simulator`  
**Purpose**: Complete implementation of the Motorola 6800 CPU instruction set.

**Key Methods**:
```java
public void executeInstruction(int opcode)
public void updateFlags(int result)
public int fetchByte()
public int fetchWord()
public void push(int value)
public int pull()
```

**Key Features**:
- Full 6800 instruction set implementation
- Accurate flag handling
- Stack operations
- Memory access through addressing modes

---

### ExecutionResult
**Package**: `assembler.simulator`  
**Purpose**: Contains the result of executing a single instruction.

**Key Fields**:
- `ExecutionStatus status`: Success, error, or halted
- `String message`: Status description
- `int instructionCount`: Instructions executed
- `long cycleCount`: CPU cycles consumed
- `Registers registerState`: Register snapshot
- `List<Integer> memoryChanges`: Modified memory locations

---

### ExecutionStatistics
**Package**: `assembler.simulator`  
**Purpose**: Tracks execution statistics and performance metrics.

**Key Fields**:
- `long totalInstructions`: Total instructions executed
- `long totalCycles`: Total CPU cycles
- `Map<String, Integer> instructionCounts`: Count per instruction type
- `long executionTime`: Wall clock time
- `double instructionsPerSecond`: Performance metric

---

### ExecutionStatus
**Package**: `assembler.simulator`  
**Purpose**: Enumeration defining possible execution states.

**Values**:
```java
RUNNING         // Normal execution
HALTED          // Program terminated (SWI, etc.)
BREAKPOINT      // Hit a breakpoint
ERROR           // Execution error
MEMORY_FAULT    // Invalid memory access
INVALID_OPCODE  // Unknown instruction
```

---

## AI Package Classes

### 47. AIAssemblyGenerator

**File**: `AIAssemblyGenerator.java`  
**Package**: `assembler.ai`  
**Purpose**: AI-powered assembly code generator using OpenAI API

**Core Functionality**:
- Natural language to Motorola 6800 assembly translation
- OpenAI API integration and communication
- JSON request/response handling
- Code generation with proper syntax validation

**Key Fields**:
```java
private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions"
private static final String MODEL = "gpt-4o"
private OkHttpClient client
private ObjectMapper objectMapper
private String apiKey
```

**Key Methods**:
```java
public void setApiKey(String apiKey)
public boolean isInitialized()
public String generateAssemblyCode(String description) throws Exception
private String createRequestBody(String description)
private String extractAssemblyCodeFromResponse(String responseBody)
private String createSystemPrompt()
private String createUserPrompt(String description)
```

**Key Features**:
- OpenAI GPT-4o model integration
- Comprehensive error handling and timeout management
- Prompt engineering for optimal assembly generation
- Markdown formatting cleanup and code extraction
- HTTP client configuration with proper timeouts

**AI Integration**:
- System prompt optimization for Motorola 6800 expertise
- Temperature control for consistent code generation
- Token limit management for efficient API usage
- Response validation and formatting

---

## UI Package Classes

### ConsoleUI
**Package**: `assembler.ui`  
**Purpose**: Command-line interface for the assembler.

**Key Methods**:
```java
public static void main(String[] args)
public void displayHelp()
public void assembleFile(String filename)
public void runSimulator(String filename)
public void showMemoryDump(int start, int end)
public void showRegisters()
```

**Key Features**:
- Command-line argument processing
- File assembly and output generation
- Interactive simulation mode
- Memory and register display
- Help and usage information

---

## Util Package Classes

### FileManager
**Package**: `assembler.util`  
**Purpose**: Utility class for all file I/O operations.

**Key Methods**:
```java
public static String loadSourceFile(String filePath) throws IOException
public static void saveSourceFile(String filePath, String sourceCode)
public static void saveBinaryFile(String filePath, Map<Integer, List<Integer>> machineCode)
public static void saveHexFile(String filePath, Map<Integer, List<Integer>> machineCode)
public static void saveListingFile(String filePath, List<Object> assemblyLines)
public static void createExampleProgram(String filePath)
```

**Key Features**:
- Multiple output formats (binary, Intel HEX, listing)
- Example program generation
- File utility functions
- Backup and safety features

---

## Main Application

### App
**Package**: `assembler`  
**Purpose**: Main application entry point and high-level coordination.

**Key Methods**:
```java
public static void main(String[] args)
public void processCommandLine(String[] args)
public void assembleProgram(String filename)
public void runInteractiveMode()
```

**Key Features**:
- Command-line interface
- Coordinates parser, assembler, and simulator
- Error handling and user feedback
- Interactive and batch modes

---

## Class Relationships

### Core Data Flow
1. **App** → **ConsoleUI** → User interaction
2. **FileManager** → Load source → **AssemblyParser**
3. **AssemblyParser** → **LabelResolver** + **SyntaxValidator** + **TokenParser**
4. **CodeGenerator** → **OpcodeTable** → Machine code
5. **ExecutionEngine** → **CPU6800** → **Memory** + **Registers**

### Dependencies
- **Parser classes** depend on **Core classes** for data models
- **Assembler classes** depend on **Parser output** and **Core classes**
- **Simulator classes** depend on **Core classes** and **Assembler output**
- **UI classes** coordinate all other packages
- **Util classes** are independent utilities

### Error Handling
- **ParseException** for parser errors
- **CodeGenerationException** for assembler errors
- **IOException** for file operations
- **SimulationException** for execution errors

This reference provides a complete overview of all classes and their roles in the Motorola 6800 Assembler project. Each class is designed with clear responsibilities and well-defined interfaces to ensure maintainability and extensibility.
