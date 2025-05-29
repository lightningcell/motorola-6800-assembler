package assembler;

import assembler.parser.*;
import assembler.assembler.*;
import assembler.simulator.*;
import assembler.ui.*;
import assembler.ui.gui.*;
import assembler.util.*;
import assembler.ai.*;
import javafx.application.Application;
import java.util.*;

/**
 * Main application class for the Motorola 6800 Assembler.
 * Supports both GUI and console interfaces for assembling and simulating 6800 programs.
 * 
 * Features:
 * - Interactive assembly code input/editing
 * - Full 6800 instruction set support (72 instructions, 197 opcodes)
 * - Label resolution and pseudo-instruction support
 * - Machine code generation with multiple output formats
 * - CPU simulation with step-by-step debugging
 * - Memory and register visualization
 * - File I/O for loading/saving programs
 * - Modern JavaFX GUI with syntax highlighting
 * 
 * @author Motorola 6800 Assembler Team
 */
public class App {
      private UserInterface ui;
    private final AssemblyParser parser;
    private final CodeGenerator codeGenerator;
    private final ExecutionEngine simulator;
    private final AIAssemblyGenerator aiGenerator;
    
    private List<AssemblyLine> currentProgram;
    private Map<Integer, List<Integer>> machineCode;
    private String sourceCode;
    
    public App() {
        this(false); // Default to console mode
    }
    
    public App(boolean useGUI) {
        this.parser = new AssemblyParser();
        this.codeGenerator = new CodeGenerator();
        this.simulator = new ExecutionEngine();
        this.aiGenerator = new AIAssemblyGenerator(null);
        this.currentProgram = new ArrayList<>();
        this.machineCode = new HashMap<>();
        this.sourceCode = "";
        
        if (useGUI) {
            // UI will be set when JavaFX application starts
            this.ui = null;
        } else {
            this.ui = new ConsoleUI();
        }
    }
    
    /**
     * Set the user interface (used by GUI mode)
     */
    public void setUserInterface(UserInterface ui) {
        this.ui = ui;
    }
    
    /**
     * Get the current user interface
     */
    public UserInterface getUserInterface() {
        return ui;
    }
    
    /**
     * Main entry point for the application.
     */
    public static void main(String[] args) {
        // Check command line arguments for GUI mode
        boolean useGUI = args.length > 0 && "--gui".equals(args[0]);
        
        if (useGUI) {
            // Launch JavaFX application
            Application.launch(AssemblerGUI.class, args);
        } else {
            // Run in console mode
            App app = new App(false);
            app.run();
        }
    }
    
    /**
     * Main application loop.
     */
    public void run() {
        ui.showWelcome();
        
        boolean running = true;
        while (running) {
            try {
                int choice = ui.showMainMenu();
                  switch (choice) {
                    case 1:
                        inputAssemblyCode();
                        break;
                    case 2:
                        loadProgramFromFile();
                        break;
                    case 3:
                        assembleProgram();
                        break;
                    case 4:
                        viewMachineCode();
                        break;
                    case 5:
                        simulateProgram();
                        break;
                    case 6:
                        saveProgram();
                        break;
                    case 7:
                        showInstructionSet();
                        break;
                    case 8:
                        createExampleProgram();
                        break;
                    case 9:
                        handleAIMenu();
                        break;
                    case 0:
                        running = false;
                        break;
                    default:
                        ui.showError("Invalid choice. Please try again.");
                }
                
            } catch (Exception e) {
                ui.showError("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        ui.showMessage("Thank you for using the Motorola 6800 Assembler!");
    }
    
    /**
     * Handle assembly code input from user.
     */
    private void inputAssemblyCode() {
        ui.showMessage("Enter assembly code (type 'END' on a line by itself to finish):");
        sourceCode = ui.getMultiLineInput();
        ui.showMessage("Assembly code entered successfully.");
    }
    
    /**
     * Load program from file.
     */
    private void loadProgramFromFile() {
        try {
            String filename = ui.getInput("Enter filename to load: ");
            sourceCode = FileManager.loadSourceFile(filename);
            ui.showMessage("Program loaded successfully from: " + filename);
        } catch (Exception e) {
            ui.showError("Failed to load file: " + e.getMessage());
        }
    }
      /**
     * Assemble the current program.
     */
    private void assembleProgram() {
        if (sourceCode.isEmpty()) {
            ui.showError("No source code to assemble. Please input or load a program first.");
            return;
        }

        try {
            // Parse source code (first pass - no label resolution yet)
            ui.showMessage("Parsing source code...");
            currentProgram = parser.parseSource(sourceCode);
            
            // Calculate addresses for each line (needed for label resolution)
            ui.showMessage("Calculating addresses...");
            calculateAddresses(currentProgram);
            
            // Resolve label references (second pass)
            ui.showMessage("Resolving labels...");
            parser.resolveLabelReferences(currentProgram);

            // Generate machine code
            ui.showMessage("Generating machine code...");
            machineCode = codeGenerator.generateCode(currentProgram);

            ui.showMessage("Assembly completed successfully!");
            ui.showMessage("Program contains " + currentProgram.size() + " lines");
            ui.showMessage("Generated " + getTotalBytes() + " bytes of machine code");

        } catch (ParseException e) {
            ui.showError("Parse error: " + e.getMessage());
        } catch (CodeGenerationException e) {
            ui.showError("Code generation error: " + e.getMessage());
        }
    }
    
    /**
     * Display machine code listing.
     */
    private void viewMachineCode() {
        if (machineCode.isEmpty()) {
            ui.showError("No machine code available. Please assemble a program first.");
            return;
        }
        
        ui.showMachineCode(currentProgram, machineCode);
    }
    
    /**
     * Enter simulation mode.
     */
    private void simulateProgram() {
        if (currentProgram.isEmpty()) {
            ui.showError("No program loaded. Please assemble a program first.");
            return;
        }
        
        try {
            // Load program into simulator
            simulator.loadProgram(currentProgram);
            ui.showMessage("Program loaded into simulator.");
            
            // Simulation loop
            boolean simulating = true;
            while (simulating) {
                int choice = ui.showSimulatorMenu();
                
                switch (choice) {
                    case 1:
                        stepExecution();
                        break;
                    case 2:
                        runProgram();
                        break;
                    case 3:
                        viewRegisters();
                        break;
                    case 4:
                        viewMemory();
                        break;
                    case 5:
                        manageBreakpoints();
                        break;
                    case 6:
                        resetSimulation();
                        break;
                    case 0:
                        simulating = false;
                        break;
                    default:
                        ui.showError("Invalid choice.");
                }
            }
            
        } catch (CodeGenerationException e) {
            ui.showError("Failed to load program: " + e.getMessage());
        }
    }
    
    /**
     * Execute one instruction step.
     */
    private void stepExecution() {
        if (simulator.isHalted()) {
            ui.showMessage("Program is halted. Use Reset to restart.");
            return;
        }
        
        ExecutionResult result = simulator.step();
        ui.showExecutionResult(result);
        
        if (result.getAssemblyLine() != null) {
            ui.showMessage("Executed: " + result.getAssemblyLine().getSourceLine());
        }
    }
    
    /**
     * Run program until completion or breakpoint.
     */
    private void runProgram() {
        if (simulator.isHalted()) {
            ui.showMessage("Program is halted. Use Reset to restart.");
            return;
        }
        
        ui.showMessage("Running program...");
        ExecutionResult result = simulator.run();
        ui.showExecutionResult(result);
        
        ExecutionStatistics stats = simulator.getStatistics();
        ui.showMessage("Execution completed: " + stats.getFormattedStats());
    }
    
    /**
     * Display current register values.
     */
    private void viewRegisters() {
        ui.showRegisters(simulator.getRegisters());
    }
    
    /**
     * Display memory contents.
     */
    private void viewMemory() {
        String input = ui.getInput("Enter start address (hex, e.g., 0100): ");
        try {
            int startAddr = Integer.parseInt(input, 16);
            ui.showMemory(simulator.getMemory(), startAddr, 16);
        } catch (NumberFormatException e) {
            ui.showError("Invalid address format.");
        }
    }
    
    /**
     * Manage breakpoints.
     */
    private void manageBreakpoints() {
        boolean managing = true;
        while (managing) {
            int choice = ui.showBreakpointMenu();
            
            switch (choice) {
                case 1:
                    addBreakpoint();
                    break;
                case 2:
                    removeBreakpoint();
                    break;
                case 3:
                    listBreakpoints();
                    break;
                case 4:
                    simulator.clearBreakpoints();
                    ui.showMessage("All breakpoints cleared.");
                    break;
                case 0:
                    managing = false;
                    break;
                default:
                    ui.showError("Invalid choice.");
            }
        }
    }
    
    /**
     * Add a breakpoint.
     */
    private void addBreakpoint() {
        String input = ui.getInput("Enter breakpoint address (hex): ");
        try {
            int address = Integer.parseInt(input, 16);
            simulator.addBreakpoint(address);
            ui.showMessage("Breakpoint added at address 0x" + Integer.toHexString(address).toUpperCase());
        } catch (NumberFormatException e) {
            ui.showError("Invalid address format.");
        }
    }
    
    /**
     * Remove a breakpoint.
     */
    private void removeBreakpoint() {
        String input = ui.getInput("Enter breakpoint address to remove (hex): ");
        try {
            int address = Integer.parseInt(input, 16);
            simulator.removeBreakpoint(address);
            ui.showMessage("Breakpoint removed from address 0x" + Integer.toHexString(address).toUpperCase());
        } catch (NumberFormatException e) {
            ui.showError("Invalid address format.");
        }
    }
    
    /**
     * List all breakpoints.
     */
    private void listBreakpoints() {
        Set<Integer> breakpoints = simulator.getBreakpoints();
        if (breakpoints.isEmpty()) {
            ui.showMessage("No breakpoints set.");
        } else {
            ui.showMessage("Active breakpoints:");
            for (Integer addr : breakpoints) {
                ui.showMessage("  0x" + Integer.toHexString(addr).toUpperCase());
            }
        }
    }
    
    /**
     * Reset simulation.
     */
    private void resetSimulation() {
        simulator.reset();
        ui.showMessage("Simulation reset.");
    }
    
    /**
     * Save program to file.
     */
    private void saveProgram() {
        if (sourceCode.isEmpty()) {
            ui.showError("No program to save.");
            return;
        }
        
        try {
            String filename = ui.getInput("Enter filename to save: ");
            FileManager.saveSourceFile(filename, sourceCode);
            ui.showMessage("Program saved to: " + filename);
            
            // Also save machine code if available
            if (!machineCode.isEmpty()) {
                String hexFile = filename.replaceFirst("\\.[^.]*$", "") + ".hex";
                FileManager.saveHexFile(hexFile, machineCode);
                ui.showMessage("Machine code saved to: " + hexFile);
            }
            
        } catch (Exception e) {
            ui.showError("Failed to save file: " + e.getMessage());
        }
    }
    
    /**
     * Show available instruction set.
     */
    private void showInstructionSet() {
        ui.showInstructionSet();
    }
    
    /**
     * Create example program.
     */
    private void createExampleProgram() {
        try {
            String filename = ui.getInput("Enter filename for example program: ");
            FileManager.createExampleProgram(filename);
            ui.showMessage("Example program created: " + filename);
            
            // Ask if user wants to load it
            String load = ui.getInput("Load this program now? (y/n): ");
            if (load.toLowerCase().startsWith("y")) {
                sourceCode = FileManager.loadSourceFile(filename);
                ui.showMessage("Example program loaded.");
            }
            
        } catch (Exception e) {
            ui.showError("Failed to create example: " + e.getMessage());
        }
    }
    
    /**
     * Calculate total bytes in machine code.
     */
    private int getTotalBytes() {
        return machineCode.values().stream()
                         .mapToInt(List::size)
                         .sum();
    }
      /**
     * Calculate addresses for each assembly line.
     * This is needed before label resolution can take place.
     */
    private void calculateAddresses(List<AssemblyLine> assemblyLines) {
        int currentAddress = 0;
        
        for (AssemblyLine line : assemblyLines) {
            // Handle ORG pseudo-instruction which changes the current address
            if (line.isPseudoOp() && "ORG".equals(line.getPseudoOp())) {
                currentAddress = (Integer) line.getPseudoOperand();
            }
            
            line.setAddress(currentAddress);
            
            // Calculate size of this line for address advancement
            int lineSize = calculateLineSize(line);
            currentAddress += lineSize;
        }
    }
    
    /**
     * Calculate the size in bytes for a single assembly line.
     */
    private int calculateLineSize(AssemblyLine line) {
        // Handle pseudo-instructions
        if (line.isPseudoOp()) {
            String pseudoOp = line.getPseudoOp();
            Object operand = line.getPseudoOperand();
            
            switch (pseudoOp) {
                case "ORG":
                case "END":
                case "EQU":
                    return 0; // No bytes generated
                    
                case "FCB":
                    if (operand instanceof List) {
                        return ((List<?>) operand).size();
                    } else {
                        return 1;
                    }
                    
                case "FDB":
                    if (operand instanceof List) {
                        return ((List<?>) operand).size() * 2;
                    } else {
                        return 2;
                    }
                    
                default:
                    return 0;
            }
        }
        
        // Handle regular instructions
        if (line.getInstruction() != null) {
            switch (line.getInstruction().getAddressingMode()) {
                case INHERENT:
                    return 1; // Just opcode
                case IMMEDIATE:
                case DIRECT:
                case INDEXED:
                case RELATIVE:
                    return 2; // Opcode + 1 byte operand
                case EXTENDED:
                    return 3; // Opcode + 2 byte operand
                default:
                    return 1;
            }
        }
        
        return 0; // Empty line or comment
    }
    
    /**
     * Handle AI menu interactions.
     */
    private void handleAIMenu() {
        boolean inAIMenu = true;
        while (inAIMenu) {
            try {
                int choice = ui.showAIMenu();
                
                switch (choice) {
                    case 1:
                        setApiKey();
                        break;
                    case 2:
                        generateCodeWithAI();
                        break;
                    case 3:
                        showApiKeyStatus();
                        break;
                    case 0:
                        inAIMenu = false;
                        break;
                    default:
                        ui.showError("Invalid choice. Please try again.");
                }
                
            } catch (Exception e) {
                ui.showError("An error occurred in AI menu: " + e.getMessage());
            }
        }
    }
    
    /**
     * Set OpenAI API key.
     */
    private void setApiKey() {
        try {
            String apiKey = ui.getApiKey();
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                aiGenerator.setApiKey(apiKey);
                ui.showMessage("API key set successfully!");
            } else {
                ui.showError("Invalid API key provided.");
            }
        } catch (Exception e) {
            ui.showError("Failed to set API key: " + e.getMessage());
        }
    }
      /**
     * Generate assembly code using AI.
     */
    private void generateCodeWithAI() {
        try {
            if (!aiGenerator.isInitialized()) {
                ui.showError("Please set your OpenAI API key first (option 1).");
                return;
            }
            
            String description = ui.getAIPrompt();
            if (description == null || description.trim().isEmpty()) {
                ui.showError("Please provide a description for the assembly program.");
                return;
            }
            
            ui.showMessage("Generating assembly code... Please wait.");
            
            String generatedCode = aiGenerator.generateAssemblyCode(description);
            
            if (generatedCode != null && !generatedCode.trim().isEmpty()) {
                // Show the generated code to the user
                ui.showGeneratedCode(generatedCode);
                
                // Ask for confirmation before loading it
                if (ui.confirmGeneratedCode()) {
                    sourceCode = generatedCode;
                    ui.showMessage("✓ AI generated code loaded successfully!");
                    ui.showMessage("You can now use option 3 to assemble the code.");
                } else {
                    ui.showMessage("Code generation cancelled. Try again with a different description.");
                }
            } else {
                ui.showError("Failed to generate assembly code. Please try again.");
            }
            
        } catch (Exception e) {
            ui.showError("Error generating code: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show API key status.
     */
    private void showApiKeyStatus() {
        ui.showApiKeyStatus(aiGenerator.isInitialized());
    }
    
    /**
     * Get the assembly parser
     */
    public AssemblyParser getParser() {
        return parser;
    }
    
    /**
     * Get the code generator
     */
    public CodeGenerator getCodeGenerator() {
        return codeGenerator;
    }
    
    /**
     * Get the execution engine (simulator)
     */
    public ExecutionEngine getSimulator() {
        return simulator;
    }
    
    /**
     * Get the AI assembly generator
     */
    public AIAssemblyGenerator getAIGenerator() {
        return aiGenerator;
    }
    
    /**
     * Get the current program (assembly lines)
     */
    public List<AssemblyLine> getCurrentProgram() {
        return currentProgram;
    }
    
    /**
     * Set the current program
     */
    public void setCurrentProgram(List<AssemblyLine> program) {
        this.currentProgram = program;
    }
    
    /**
     * Get the machine code
     */
    public Map<Integer, List<Integer>> getMachineCode() {
        return machineCode;
    }
    
    /**
     * Set the machine code
     */
    public void setMachineCode(Map<Integer, List<Integer>> machineCode) {
        this.machineCode = machineCode;
    }
    
    /**
     * Get the source code
     */
    public String getSourceCode() {
        return sourceCode;
    }
    
    /**
     * Set the source code
     */
    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
}
