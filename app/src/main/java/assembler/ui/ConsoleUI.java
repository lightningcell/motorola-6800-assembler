package assembler.ui;

import assembler.core.Registers;
import assembler.parser.AssemblyLine;
import assembler.core.Memory;
import assembler.simulator.ExecutionResult;
import java.util.Scanner;
import java.util.List;
import java.util.Map;

/**
 * Console-based user interface for the Motorola 6800 Assembler
 */
public class ConsoleUI {
    
    private final Scanner scanner;
    
    public ConsoleUI() {
        scanner = new Scanner(System.in);
    }

    /**
     * Shows the welcome message
     */
    public void showWelcome() {
        System.out.println("===============================================");
        System.out.println("       Motorola 6800 Assembler & Simulator");
        System.out.println("===============================================");
        System.out.println("Features:");
        System.out.println("• Full 6800 instruction set support");
        System.out.println("• Assembly to machine code translation");
        System.out.println("• Interactive simulation");
        System.out.println("• File I/O operations");
        System.out.println();
    }    /**
     * Shows the main menu and returns user choice
     */
    public int showMainMenu() {
        System.out.println("=== MAIN MENU ===");
        System.out.println("1. Enter assembly code manually");
        System.out.println("2. Load program from file");
        System.out.println("3. Assemble current program");
        System.out.println("4. View machine code");
        System.out.println("5. Simulate program");
        System.out.println("6. Save program to file");
        System.out.println("7. Show instruction set");
        System.out.println("8. Create example program");
        System.out.println("0. Exit");
        return getIntInput("Enter your choice: ");
    }

    /**
     * Shows a message to the user
     */
    public void showMessage(String message) {
        System.out.println(message);
    }

    /**
     * Gets input from user with prompt
     */
    public String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /**
     * Gets multi-line input from user
     */
    public String getMultiLineInput() {
        StringBuilder code = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            code.append(line).append("\n");
        }
        return code.toString();
    }

    /**
     * Shows machine code mapping
     */
    public void showMachineCode(Map<Integer, List<Integer>> machineCode) {
        System.out.println("\n=== MACHINE CODE ===");
        System.out.println("Address  Machine Code");
        System.out.println("-------- ------------");
        
        for (Map.Entry<Integer, List<Integer>> entry : machineCode.entrySet()) {
            System.out.printf("%04X     ", entry.getKey());
            for (Integer b : entry.getValue()) {
                System.out.printf("%02X ", b);
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Shows processor registers
     */
    public void showRegisters(Registers registers) {
        System.out.println("\n=== PROCESSOR STATE ===");
        System.out.printf("A: %02X  B: %02X\n", registers.getA(), registers.getB());
        System.out.printf("X: %04X  SP: %04X  PC: %04X\n", 
                         registers.getX(), registers.getSP(), registers.getPC());
        System.out.print("Flags: ");
        System.out.print(registers.getCarryFlag() ? "C" : "-");
        System.out.print(registers.getZeroFlag() ? "Z" : "-");
        System.out.print(registers.getNegativeFlag() ? "N" : "-");
        System.out.print(registers.getOverflowFlag() ? "V" : "-");
        System.out.print(registers.getInterruptFlag() ? "I" : "-");
        System.out.print(registers.getHalfCarryFlag() ? "H" : "-");
        System.out.println();
        System.out.println();
    }

    /**
     * Shows breakpoint management menu
     */
    public int showBreakpointMenu() {
        System.out.println("\n=== BREAKPOINT MENU ===");
        System.out.println("1. Add breakpoint");
        System.out.println("2. Remove breakpoint");
        System.out.println("3. List breakpoints");
        System.out.println("4. Clear all breakpoints");
        System.out.println("0. Return to main menu");
        return getIntInput("Enter choice: ");
    }

    /**
     * Shows simulator menu and returns user choice
     */
    public int showSimulatorMenu() {
        System.out.println("\n=== SIMULATOR MENU ===");
        System.out.println("1. Step execution");
        System.out.println("2. Run program");
        System.out.println("3. View registers");
        System.out.println("4. View memory");
        System.out.println("5. Manage breakpoints");
        System.out.println("6. Reset simulation");
        System.out.println("0. Exit simulator");
        return getIntInput("Enter choice: ");
    }    /**
     * Shows execution result
     */
    public void showExecutionResult(ExecutionResult result) {
        System.out.println("\n=== EXECUTION RESULT ===");
        System.out.println("Status: " + result.getStatus());
        System.out.println("Program Counter: " + String.format("0x%04X", result.getProgramCounter()));
        
        if (result.getMessage() != null && !result.getMessage().isEmpty()) {
            System.out.println("Message: " + result.getMessage());
        }
        
        if (result.getAssemblyLine() != null) {
            System.out.println("Current line: " + result.getAssemblyLine().getSourceLine());
        }
        System.out.println();
    }

    /**
     * Shows memory contents
     */
    public void showMemory(Memory memory, int startAddr, int length) {
        System.out.println("\n=== MEMORY CONTENTS ===");
        System.out.println("Address  Data");
        System.out.println("-------  ----");
          for (int i = 0; i < length; i++) {
            int addr = startAddr + i;
            int value = memory.readByte(addr);
            System.out.printf("%04X     %02X\n", addr, value);
        }
        System.out.println();
    }

    /**
     * Shows available instruction set
     */
    public void showInstructionSet() {
        System.out.println("\n=== MOTOROLA 6800 INSTRUCTION SET ===");
        System.out.println("Data Movement:");
        System.out.println("  LDA  - Load Accumulator A");
        System.out.println("  LDB  - Load Accumulator B");
        System.out.println("  LDX  - Load Index Register");
        System.out.println("  STA  - Store Accumulator A");
        System.out.println("  STB  - Store Accumulator B");
        System.out.println("  STX  - Store Index Register");
        System.out.println();
        
        System.out.println("Arithmetic:");
        System.out.println("  ADDA - Add to Accumulator A");
        System.out.println("  ADDB - Add to Accumulator B");
        System.out.println("  SUBA - Subtract from Accumulator A");
        System.out.println("  SUBB - Subtract from Accumulator B");
        System.out.println("  INCA - Increment Accumulator A");
        System.out.println("  INCB - Increment Accumulator B");
        System.out.println("  DECA - Decrement Accumulator A");
        System.out.println("  DECB - Decrement Accumulator B");
        System.out.println();
        
        System.out.println("Logic:");
        System.out.println("  ANDA - AND with Accumulator A");
        System.out.println("  ANDB - AND with Accumulator B");
        System.out.println("  ORAA - OR with Accumulator A");
        System.out.println("  ORAB - OR with Accumulator B");
        System.out.println("  EORA - Exclusive OR with Accumulator A");
        System.out.println("  EORB - Exclusive OR with Accumulator B");
        System.out.println("  COMA - Complement Accumulator A");
        System.out.println("  COMB - Complement Accumulator B");
        System.out.println();
        
        System.out.println("Branching:");
        System.out.println("  JMP  - Jump");
        System.out.println("  JSR  - Jump to Subroutine");
        System.out.println("  RTS  - Return from Subroutine");
        System.out.println("  BEQ  - Branch if Equal");
        System.out.println("  BNE  - Branch if Not Equal");
        System.out.println("  BCC  - Branch if Carry Clear");
        System.out.println("  BCS  - Branch if Carry Set");
        System.out.println("  BRA  - Branch Always");
        System.out.println();
        
        System.out.println("Test/Compare:");
        System.out.println("  CMPA - Compare Accumulator A");
        System.out.println("  CMPB - Compare Accumulator B");
        System.out.println("  TSTA - Test Accumulator A");
        System.out.println("  TSTB - Test Accumulator B");
        System.out.println();
        
        System.out.println("Stack Operations:");
        System.out.println("  PSHA - Push Accumulator A");
        System.out.println("  PSHB - Push Accumulator B");
        System.out.println("  PULA - Pull Accumulator A");
        System.out.println("  PULB - Pull Accumulator B");
        System.out.println();
        
        System.out.println("Control:");
        System.out.println("  NOP  - No Operation");
        System.out.println("  HLT  - Halt");
        System.out.println("  SWI  - Software Interrupt");
        System.out.println("  CLC  - Clear Carry");
        System.out.println("  SEC  - Set Carry");
        System.out.println();
        
        System.out.println("Addressing Modes:");
        System.out.println("  #$nn    - Immediate (8-bit)");
        System.out.println("  #$nnnn  - Immediate (16-bit)");
        System.out.println("  $nn     - Direct/Zero Page");
        System.out.println("  $nnnn   - Extended/Absolute");
        System.out.println("  nn,X    - Indexed");
        System.out.println("  (none)  - Inherent");
        System.out.println();
    }

    /**
     * Shows machine code with assembly line mapping (overloaded version)
     */
    public void showMachineCode(List<AssemblyLine> assemblyLines, Map<Integer, List<Integer>> machineCode) {
        System.out.println("\n=== ASSEMBLY/MACHINE CODE MAPPING ===");
        System.out.println("Line Address  Machine Code  Assembly");
        System.out.println("---- -------  ------------  --------");
        
        for (AssemblyLine line : assemblyLines) {
            if (line.isPseudoOp()) {
                // Show pseudo-operations without machine code
                System.out.printf("%3d  -------- ------------- %s\n", 
                    line.getLineNumber(), line.getSourceLine());            } else if (line.getMachineCode() != null && line.getMachineCode().length > 0) {
                // Show instructions with machine code
                StringBuilder hexCode = new StringBuilder();
                for (byte b : line.getMachineCode()) {
                    hexCode.append(String.format("%02X ", b & 0xFF));
                }
                System.out.printf("%3d  %04X     %-12s %s\n", 
                    line.getLineNumber(), 
                    line.getAddress(), 
                    hexCode.toString().trim(),
                    line.getSourceLine());
            }
        }
        System.out.println();
    }

    /**
     * Gets integer input
     */
    public int getIntInput(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(getInput(prompt).trim());
            } catch (NumberFormatException e) {
                showMessage("Please enter a valid number.");
            }
        }
    }

    /**
     * Shows error message
     */
    public void showError(String message) {
        System.out.println("ERROR: " + message);
    }

    /**
     * Shows warning message
     */
    public void showWarning(String message) {
        System.out.println("WARNING: " + message);
    }

    /**
     * Shows information message
     */
    public void showInfo(String message) {
        System.out.println("INFO: " + message);
    }

    /**
     * Closes the UI
     */
    public void close() {
        scanner.close();
    }
}
