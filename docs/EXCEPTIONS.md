# Exception Handling Guide

This document explains the error handling strategy and exception hierarchy used throughout the Motorola 6800 Assembler project.

## Overview

The assembler uses a comprehensive exception handling strategy to provide meaningful error messages and graceful failure handling. Each major component has its own exception types for specific error categories.

## Exception Hierarchy

```
java.lang.Exception
├── assembler.parser.ParseException
├── assembler.assembler.CodeGenerationException
├── assembler.simulator.SimulationException
└── java.io.IOException (standard Java)
```

## Core Exception Classes

### ParseException
**Package**: `assembler.parser`  
**Purpose**: Handles all parsing-related errors during source code analysis.

**Common Scenarios**:
- Invalid instruction mnemonics
- Malformed operands
- Undefined labels
- Syntax errors
- Circular label references
- Duplicate label definitions

**Key Fields**:
```java
private String message;         // Error description
private int lineNumber;         // Source line number
private String sourceLine;      // The problematic line
private ErrorType errorType;    // Category of error
```

**Usage Example**:
```java
try {
    List<AssemblyLine> lines = parser.parseProgram(sourceCode);
} catch (ParseException e) {
    System.err.printf("Parse error on line %d: %s%n", 
                     e.getLineNumber(), e.getMessage());
    System.err.printf("Source: %s%n", e.getSourceLine());
}
```

**Error Types**:
```java
public enum ErrorType {
    SYNTAX_ERROR,           // General syntax problems
    INVALID_MNEMONIC,       // Unknown instruction
    INVALID_OPERAND,        // Bad operand format
    UNDEFINED_LABEL,        // Reference to undefined label
    DUPLICATE_LABEL,        // Label defined multiple times
    INVALID_ADDRESSING_MODE,// Addressing mode not supported
    MISSING_OPERAND,        // Required operand missing
    CIRCULAR_REFERENCE      // Labels reference each other
}
```

---

### CodeGenerationException
**Package**: `assembler.assembler`  
**Purpose**: Handles errors during machine code generation.

**Common Scenarios**:
- Invalid opcode combinations
- Address calculation errors
- Operand encoding failures
- Memory range violations

**Key Fields**:
```java
private String instruction;     // Problematic instruction
private String reason;          // Why generation failed
private int address;           // Memory address
private AddressingMode mode;   // Addressing mode used
```

**Usage Example**:
```java
try {
    Map<Integer, List<Integer>> machineCode = generator.generateCode(lines);
} catch (CodeGenerationException e) {
    System.err.printf("Code generation error at address $%04X: %s%n",
                     e.getAddress(), e.getMessage());
    System.err.printf("Instruction: %s%n", e.getInstruction());
}
```

---

### SimulationException
**Package**: `assembler.simulator`  
**Purpose**: Handles errors during program execution simulation.

**Common Scenarios**:
- Invalid memory access
- Unknown opcodes
- Stack overflow/underflow
- Execution timeout
- Invalid register operations

**Key Fields**:
```java
private String operation;       // Operation being performed
private int programCounter;     // PC when error occurred
private String reason;          // Error description
private SimulationError type;   // Error category
```

**Error Types**:
```java
public enum SimulationError {
    MEMORY_FAULT,           // Invalid memory access
    INVALID_OPCODE,         // Unknown instruction
    STACK_OVERFLOW,         // Stack pointer error
    STACK_UNDERFLOW,        // Stack underflow
    EXECUTION_TIMEOUT,      // Program ran too long
    DIVISION_BY_ZERO,       // Arithmetic error
    BREAKPOINT_HIT         // Debug breakpoint
}
```

---

## Error Handling Strategies

### 1. Parser Error Handling

The parser uses a multi-phase error detection strategy:

**Phase 1: Lexical Analysis**
- Detect invalid characters
- Identify malformed tokens
- Report syntax errors with specific positions

**Phase 2: Syntax Validation**
- Validate instruction mnemonics
- Check operand formats
- Verify addressing mode compatibility

**Phase 3: Semantic Analysis**
- Resolve label references
- Check for circular dependencies
- Validate memory layout

**Example Implementation**:
```java
public class AssemblyParser {
    private List<String> errors = new ArrayList<>();
    
    public List<AssemblyLine> parseProgram(String sourceCode) throws ParseException {
        try {
            // Phase 1: Tokenize
            List<String> lines = tokenizeSource(sourceCode);
            
            // Phase 2: Parse each line
            List<AssemblyLine> assemblyLines = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                try {
                    AssemblyLine line = parseLine(lines.get(i), i + 1);
                    assemblyLines.add(line);
                } catch (ParseException e) {
                    errors.add(String.format("Line %d: %s", i + 1, e.getMessage()));
                }
            }
            
            // Phase 3: Resolve labels
            if (!errors.isEmpty()) {
                throw new ParseException("Multiple parse errors occurred", errors);
            }
            
            resolveLabels(assemblyLines);
            return assemblyLines;
            
        } catch (Exception e) {
            throw new ParseException("Parse failed: " + e.getMessage(), -1);
        }
    }
}
```

### 2. Code Generation Error Handling

The code generator validates instructions before generating machine code:

```java
public class CodeGenerator {
    public Map<Integer, List<Integer>> generateCode(List<AssemblyLine> lines) 
            throws CodeGenerationException {
        
        Map<Integer, List<Integer>> machineCode = new HashMap<>();
        
        for (AssemblyLine line : lines) {
            if (line.hasInstruction()) {
                try {
                    List<Integer> bytes = generateInstruction(line);
                    machineCode.put(line.getAddress(), bytes);
                } catch (Exception e) {
                    throw new CodeGenerationException(
                        String.format("Failed to generate code for '%s'", line.getOriginalLine()),
                        line.getAddress(),
                        e.getMessage()
                    );
                }
            }
        }
        
        return machineCode;
    }
}
```

### 3. Simulation Error Handling

The simulator provides detailed execution state when errors occur:

```java
public class ExecutionEngine {
    public ExecutionResult executeStep() throws SimulationException {
        try {
            int opcode = memory.read(registers.getProgramCounter());
            cpu.executeInstruction(opcode);
            
            return new ExecutionResult(ExecutionStatus.RUNNING, "OK");
            
        } catch (InvalidOpcodeException e) {
            throw new SimulationException(
                SimulationError.INVALID_OPCODE,
                registers.getProgramCounter(),
                String.format("Unknown opcode: $%02X", e.getOpcode())
            );
        } catch (MemoryAccessException e) {
            throw new SimulationException(
                SimulationError.MEMORY_FAULT,
                registers.getProgramCounter(),
                String.format("Invalid memory access: $%04X", e.getAddress())
            );
        }
    }
}
```

## Error Recovery Strategies

### 1. Graceful Degradation
When errors occur, the assembler attempts to continue processing when possible:

```java
// Continue parsing after syntax errors
try {
    AssemblyLine line = parseLine(sourceLine, lineNumber);
    assemblyLines.add(line);
} catch (ParseException e) {
    // Log error but continue with next line
    errors.add(e);
    assemblyLines.add(new AssemblyLine(lineNumber, sourceLine, true)); // Mark as error
}
```

### 2. Error Accumulation
Collect multiple errors before reporting to give complete feedback:

```java
public class ValidationResult {
    private List<ParseException> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    
    public void addError(ParseException error) {
        errors.add(error);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public void throwIfErrors() throws ParseException {
        if (hasErrors()) {
            throw new ParseException("Multiple errors found", errors);
        }
    }
}
```

### 3. Context Preservation
Maintain context information for better error reporting:

```java
public class ParserContext {
    private String currentFile;
    private int currentLine;
    private String currentFunction;
    private Map<String, Label> labels;
    
    public ParseException createException(String message) {
        return new ParseException(
            String.format("%s in %s at line %d", message, currentFile, currentLine),
            currentLine,
            getCurrentSourceLine()
        );
    }
}
```

## User-Friendly Error Messages

### 1. Specific Error Descriptions
Instead of generic messages, provide specific guidance:

```java
// Bad
throw new ParseException("Invalid operand");

// Good
throw new ParseException(
    "Invalid immediate operand '#$XYZ' - expected hexadecimal value like '#$FF'",
    lineNumber
);
```

### 2. Suggested Fixes
When possible, suggest how to fix the error:

```java
public class ErrorMessages {
    public static String getInvalidMnemonicMessage(String mnemonic) {
        String closest = findClosestMnemonic(mnemonic);
        if (closest != null) {
            return String.format("Unknown instruction '%s'. Did you mean '%s'?", 
                               mnemonic, closest);
        }
        return String.format("Unknown instruction '%s'", mnemonic);
    }
}
```

### 3. Error Context
Provide surrounding context for better understanding:

```java
public class ParseException extends Exception {
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage()).append("\n");
        sb.append("Line ").append(lineNumber).append(": ").append(sourceLine).append("\n");
        
        // Add pointer to error location
        if (columnNumber > 0) {
            sb.append(" ".repeat(columnNumber + 7)).append("^\n");
        }
        
        return sb.toString();
    }
}
```

## Error Logging and Debugging

### 1. Structured Logging
Use consistent logging format for debugging:

```java
public class AssemblerLogger {
    private static final Logger logger = LoggerFactory.getLogger(AssemblerLogger.class);
    
    public static void logParseError(ParseException e) {
        logger.error("Parse error at line {}: {} in file {}", 
                    e.getLineNumber(), e.getMessage(), e.getSourceFile());
    }
    
    public static void logCodeGenError(CodeGenerationException e) {
        logger.error("Code generation error at address ${:04X}: {}", 
                    e.getAddress(), e.getMessage());
    }
}
```

### 2. Error Statistics
Track error patterns for improvement:

```java
public class ErrorStatistics {
    private Map<ErrorType, Integer> errorCounts = new HashMap<>();
    
    public void recordError(ParseException e) {
        errorCounts.merge(e.getErrorType(), 1, Integer::sum);
    }
    
    public void printSummary() {
        System.out.println("Error Summary:");
        errorCounts.forEach((type, count) -> 
            System.out.printf("  %s: %d%n", type, count));
    }
}
```

## Testing Exception Handling

### Unit Tests for Error Conditions
```java
@Test
public void testInvalidMnemonic() {
    String source = "INVALID #$FF";
    
    ParseException exception = assertThrows(ParseException.class, () -> {
        parser.parseProgram(source);
    });
    
    assertEquals(ErrorType.INVALID_MNEMONIC, exception.getErrorType());
    assertEquals(1, exception.getLineNumber());
    assertTrue(exception.getMessage().contains("INVALID"));
}

@Test
public void testUndefinedLabel() {
    String source = "JMP UNDEFINED";
    
    ParseException exception = assertThrows(ParseException.class, () -> {
        parser.parseProgram(source);
    });
    
    assertEquals(ErrorType.UNDEFINED_LABEL, exception.getErrorType());
}
```

## Best Practices

### 1. Fail Fast
Detect errors as early as possible in the process.

### 2. Provide Context
Include relevant information about the error location and state.

### 3. Be Specific
Give precise error descriptions rather than generic messages.

### 4. Suggest Solutions
When possible, tell the user how to fix the problem.

### 5. Log for Debugging
Maintain detailed logs for developers while showing user-friendly messages to end users.

### 6. Validate Early
Check inputs and constraints before processing begins.

### 7. Handle Gracefully
Don't crash the entire application for recoverable errors.

This comprehensive error handling strategy ensures that users receive helpful feedback while developers get the detailed information needed for debugging and maintenance.
