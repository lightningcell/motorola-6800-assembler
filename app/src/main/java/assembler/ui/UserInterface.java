package assembler.ui;

import assembler.core.Registers;
import assembler.parser.AssemblyLine;
import assembler.core.Memory;
import assembler.simulator.ExecutionResult;
import java.util.List;
import java.util.Map;

/**
 * Common interface for all user interface implementations.
 * This allows the application to work with both console and graphical interfaces.
 * 
 * @author Motorola 6800 Assembler Team
 */
public interface UserInterface {
    
    // === Initialization and Lifecycle ===
    
    /**
     * Initialize the user interface.
     */
    void initialize();
    
    /**
     * Show welcome message/screen.
     */
    void showWelcome();
    
    /**
     * Close the user interface and cleanup resources.
     */
    void close();
    
    // === Menu and Navigation ===
    
    /**
     * Show main menu and return user choice.
     * @return Selected menu option
     */
    int showMainMenu();
    
    /**
     * Show simulator menu and return user choice.
     * @return Selected menu option
     */
    int showSimulatorMenu();
    
    /**
     * Show breakpoint management menu and return user choice.
     * @return Selected menu option
     */
    int showBreakpointMenu();
    
    /**
     * Show AI assistant menu and return user choice.
     * @return Selected menu option
     */
    int showAIMenu();
    
    // === Input Methods ===
    
    /**
     * Get string input from user with prompt.
     * @param prompt The prompt to display
     * @return User input
     */
    String getInput(String prompt);
    
    /**
     * Get multi-line input from user (for assembly code).
     * @return Multi-line input
     */
    String getMultiLineInput();
    
    /**
     * Get integer input from user with prompt.
     * @param prompt The prompt to display
     * @return User input as integer
     */
    int getIntInput(String prompt);
    
    /**
     * Get OpenAI API key from user.
     * @return API key
     */
    String getApiKey();
    
    /**
     * Get AI prompt description from user.
     * @return AI prompt
     */
    String getAIPrompt();
    
    // === Output and Display Methods ===
    
    /**
     * Show a general message to the user.
     * @param message The message to display
     */
    void showMessage(String message);
    
    /**
     * Show an error message to the user.
     * @param message The error message
     */
    void showError(String message);
    
    /**
     * Show a warning message to the user.
     * @param message The warning message
     */
    void showWarning(String message);
    
    /**
     * Show an information message to the user.
     * @param message The info message
     */
    void showInfo(String message);
    
    // === Assembly and Code Display ===
    
    /**
     * Show machine code mapping.
     * @param machineCode Map of addresses to machine code bytes
     */
    void showMachineCode(Map<Integer, List<Integer>> machineCode);
    
    /**
     * Show machine code with assembly line mapping.
     * @param assemblyLines List of assembly lines
     * @param machineCode Map of addresses to machine code bytes
     */
    void showMachineCode(List<AssemblyLine> assemblyLines, Map<Integer, List<Integer>> machineCode);
    
    /**
     * Show the available instruction set.
     */
    void showInstructionSet();
    
    /**
     * Show generated assembly code from AI.
     * @param code The generated code
     */
    void showGeneratedCode(String code);
    
    /**
     * Ask user if they want to use the generated code.
     * @return true if user confirms, false otherwise
     */
    boolean confirmGeneratedCode();
    
    // === Simulation Display ===
    
    /**
     * Show processor registers.
     * @param registers Current register state
     */
    void showRegisters(Registers registers);
    
    /**
     * Show memory contents.
     * @param memory Memory object
     * @param startAddr Starting address
     * @param length Number of bytes to show
     */
    void showMemory(Memory memory, int startAddr, int length);
    
    /**
     * Show execution result.
     * @param result Execution result
     */
    void showExecutionResult(ExecutionResult result);
    
    // === API Status ===
    
    /**
     * Show API key status.
     * @param isSet Whether API key is configured
     */
    void showApiKeyStatus(boolean isSet);
}
