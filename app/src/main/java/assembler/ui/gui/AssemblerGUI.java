package assembler.ui.gui;

import assembler.App;
import assembler.core.Memory;
import assembler.core.Registers;
import assembler.parser.AssemblyLine;
import assembler.simulator.ExecutionResult;
import assembler.ui.UserInterface;
import assembler.util.FileManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Modern JavaFX-based graphical user interface for the Motorola 6800 Assembler.
 * 
 * Features:
 * - Syntax-highlighted code editor with line numbers
 * - Real-time machine code display with assembly mapping
 * - Interactive CPU simulation and debugging
 * - Memory and register visualization
 * - AI-powered code generation
 * - Modern dark theme with professional styling
 * - File operations and project management
 * 
 * @author Motorola 6800 Assembler Team
 */
public class AssemblerGUI extends Application implements UserInterface {
    
    // === Main Layout Components ===
    private Stage primaryStage;
    private BorderPane rootLayout;
    private SplitPane mainSplitPane;
    private SplitPane horizontalSplit;
    
    // === Menu and Toolbar ===
    private MenuBar menuBar;
    private ToolBar toolBar;
    
    // === Code Editor (Left Panel) ===
    private TabPane leftTabPane;
    private Tab codeTab;
    private Tab fileExplorerTab;
    private CodeArea codeEditor;
    private TreeView<String> fileExplorer;
    
    // === Machine Code Display (Center Panel) ===
    private TabPane centerTabPane;
    private Tab machineCodeTab;
    private Tab consoleTab;
    private TableView<MachineCodeEntry> machineCodeTable;
    private TextArea consoleOutput;
    
    // === CPU State and Controls (Right Panel) ===
    private TabPane rightTabPane;
    private Tab registersTab;
    private Tab memoryTab;
    private Tab breakpointsTab;
    private Tab aiTab;
    private Tab simulatorControlTab;
    
    private GridPane registersPanel;
    private TableView<MemoryEntry> memoryTable;
    private VBox breakpointsPanel;
    private VBox aiPanel;
    private VBox simulatorPanel;
    
    // === AI Assistant Components ===
    private TextArea aiPrompt;
    private TextArea aiResponse;
    private Button generateButton;
    private Button useCodeButton;
    private ProgressIndicator aiProgressIndicator;
      // === Simulator Controls ===
    private Button runButton;
    private Button stepButton;
    private Button pauseButton;
    private Button resetButton;
    private Button stopButton;
    private Slider speedSlider;
    private Label speedLabel;
    
    // === Status Bar ===
    private HBox statusBar;
    private Label statusLabel;
    private Label positionLabel;
    private Label memoryUsageLabel;
    private ProgressBar progressBar;
    
    // === Data Models ===
    private ObservableList<MachineCodeEntry> machineCodeData = FXCollections.observableArrayList();
    private ObservableList<MemoryEntry> memoryData = FXCollections.observableArrayList();
    
    // === Business Logic ===
    private App app;
    
    // === UI State ===
    private CompletableFuture<Integer> menuChoiceFuture;
    private CompletableFuture<String> inputFuture;
    private File currentFile;
    private boolean isSimulationRunning = false;
    private boolean hasUnsavedChanges = false;
    
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        
        // Initialize business logic
        this.app = new App(true);
        this.app.setUserInterface(this);
        
        // Build the GUI
        initializeGUI();
        setupEventHandlers();
        
        // Create and configure the scene
        Scene scene = new Scene(rootLayout, 1600, 1000);
        scene.getStylesheets().add(getClass().getResource("/styles/assembler-theme.css").toExternalForm());
        
        // Configure the primary stage
        primaryStage.setTitle("Motorola 6800 Assembler & Simulator - Professional Edition");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setOnCloseRequest(e -> {
            if (hasUnsavedChanges()) {
                e.consume();
                handleExit();
            }
        });
        
        primaryStage.show();
        
        // Show welcome message
        Platform.runLater(this::showWelcomeDialog);
    }
    
    /**
     * Initialize all GUI components and layout structure.
     */
    private void initializeGUI() {
        createRootLayout();
        createMenuBar();
        createToolBar();
        createMainPanels();
        createStatusBar();
        assembleLayout();
    }
      /**
     * Create the root layout structure.
     */
    private void createRootLayout() {
        rootLayout = new BorderPane();
        rootLayout.getStyleClass().add("root-layout");
        
        mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.HORIZONTAL);
        mainSplitPane.getStyleClass().add("main-split-pane");
        
        // Initialize horizontalSplit as the same as mainSplitPane
        horizontalSplit = mainSplitPane;
    }
    
    /**
     * Create the menu bar with all menus.
     */
    private void createMenuBar() {
        menuBar = new MenuBar();
        
        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem newFile = new MenuItem("New");
        MenuItem openFile = new MenuItem("Open...");
        MenuItem saveFile = new MenuItem("Save");
        MenuItem saveAsFile = new MenuItem("Save As...");
        MenuItem exitApp = new MenuItem("Exit");
        
        newFile.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+N"));
        openFile.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+O"));
        saveFile.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+S"));
        
        fileMenu.getItems().addAll(newFile, openFile, new SeparatorMenuItem(),
                saveFile, saveAsFile, new SeparatorMenuItem(), exitApp);
        
        // Edit menu
        Menu editMenu = new Menu("Edit");
        MenuItem undo = new MenuItem("Undo");
        MenuItem redo = new MenuItem("Redo");
        MenuItem cut = new MenuItem("Cut");
        MenuItem copy = new MenuItem("Copy");
        MenuItem paste = new MenuItem("Paste");
        MenuItem selectAll = new MenuItem("Select All");
        
        undo.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+Z"));
        redo.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+Y"));
        cut.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+X"));
        copy.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+C"));
        paste.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+V"));
        selectAll.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+A"));
        
        editMenu.getItems().addAll(undo, redo, new SeparatorMenuItem(),
                cut, copy, paste, new SeparatorMenuItem(), selectAll);
        
        // Build menu
        Menu buildMenu = new Menu("Build");
        MenuItem assemble = new MenuItem("Assemble");
        MenuItem assembleRun = new MenuItem("Assemble & Run");
        MenuItem clean = new MenuItem("Clean");
        
        assemble.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F7"));
        assembleRun.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F5"));
        
        buildMenu.getItems().addAll(assemble, assembleRun, new SeparatorMenuItem(), clean);
        
        // Debug menu
        Menu debugMenu = new Menu("Debug");
        MenuItem startDebug = new MenuItem("Start Debugging");
        MenuItem stepInto = new MenuItem("Step Into");
        MenuItem stepOver = new MenuItem("Step Over");
        MenuItem stepOut = new MenuItem("Step Out");
        MenuItem toggleBreakpoint = new MenuItem("Toggle Breakpoint");
        MenuItem clearBreakpoints = new MenuItem("Clear All Breakpoints");
        
        startDebug.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F9"));
        stepInto.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F11"));
        stepOver.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F10"));
        toggleBreakpoint.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F8"));
        
        debugMenu.getItems().addAll(startDebug, new SeparatorMenuItem(),
                stepInto, stepOver, stepOut, new SeparatorMenuItem(),
                toggleBreakpoint, clearBreakpoints);
        
        // AI menu
        Menu aiMenu = new Menu("AI Assistant");
        MenuItem generateCode = new MenuItem("Generate Code...");
        MenuItem configureAPI = new MenuItem("Configure API Key...");
        MenuItem showExamples = new MenuItem("Show Examples");
        
        aiMenu.getItems().addAll(generateCode, configureAPI, new SeparatorMenuItem(), showExamples);
        
        // Help menu
        Menu helpMenu = new Menu("Help");
        MenuItem instructionSet = new MenuItem("Instruction Set Reference");
        MenuItem userGuide = new MenuItem("User Guide");
        MenuItem about = new MenuItem("About");
        
        helpMenu.getItems().addAll(instructionSet, userGuide, new SeparatorMenuItem(), about);
        
        menuBar.getMenus().addAll(fileMenu, editMenu, buildMenu, debugMenu, aiMenu, helpMenu);
    }
    
    /**
     * Create the toolbar with commonly used actions.
     */
    private void createToolBar() {
        toolBar = new ToolBar();
        
        Button newBtn = new Button("New");
        Button openBtn = new Button("Open");
        Button saveBtn = new Button("Save");
        
        Separator sep1 = new Separator();
        
        Button assembleBtn = new Button("Assemble");
        Button runBtn = new Button("Run");
        Button debugBtn = new Button("Debug");
        Button stopBtn = new Button("Stop");
        
        Separator sep2 = new Separator();
        
        Button stepBtn = new Button("Step");
        Button breakpointBtn = new Button("Breakpoint");
        
        Separator sep3 = new Separator();
        
        Button aiBtn = new Button("AI Assistant");
        
        // Style buttons
        newBtn.getStyleClass().add("toolbar-button");
        openBtn.getStyleClass().add("toolbar-button");
        saveBtn.getStyleClass().add("toolbar-button");
        assembleBtn.getStyleClass().add("toolbar-button");
        runBtn.getStyleClass().add("toolbar-button");
        debugBtn.getStyleClass().add("toolbar-button");
        stopBtn.getStyleClass().add("toolbar-button");
        stepBtn.getStyleClass().add("toolbar-button");
        breakpointBtn.getStyleClass().add("toolbar-button");
        aiBtn.getStyleClass().addAll("toolbar-button", "ai-button");
        
        toolBar.getItems().addAll(
                newBtn, openBtn, saveBtn, sep1,
                assembleBtn, runBtn, debugBtn, stopBtn, sep2,
                stepBtn, breakpointBtn, sep3,
                aiBtn
        );
    }
    
    /**
     * Create the left panel with code editor and file explorer.
     */
    private void createLeftPanel() {
        leftTabPane = new TabPane();
        leftTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
          // Code editor tab
        codeTab = new Tab("Assembly Code");
        codeEditor = new CodeArea();
        codeEditor.setParagraphGraphicFactory(LineNumberFactory.get(codeEditor));
        codeEditor.getStyleClass().add("code-editor");
        
        // Add syntax highlighting for assembly
        codeEditor.textProperty().addListener((obs, oldText, newText) -> {
            codeEditor.setStyleSpans(0, AssemblyHighlighter.computeHighlighting(newText));
        });
        
        codeTab.setContent(new VBox(codeEditor));
        
        // File explorer tab
        fileExplorerTab = new Tab("Files");
        fileExplorer = new TreeView<>();
        fileExplorer.setRoot(new TreeItem<>("Project"));
        fileExplorerTab.setContent(fileExplorer);
        
        leftTabPane.getTabs().addAll(codeTab, fileExplorerTab);
        leftTabPane.setPrefWidth(400);
    }
    
    /**
     * Create the center panel with machine code and console output.
     */
    private void createCenterPanel() {
        centerTabPane = new TabPane();
        centerTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Machine code tab
        machineCodeTab = new Tab("Machine Code");
        machineCodeTable = new TableView<>();
        
        TableColumn<MachineCodeEntry, String> addressCol = new TableColumn<>("Address");
        TableColumn<MachineCodeEntry, String> bytesCol = new TableColumn<>("Machine Code");
        TableColumn<MachineCodeEntry, String> asmCol = new TableColumn<>("Assembly");
        
        addressCol.setPrefWidth(80);
        bytesCol.setPrefWidth(120);
        asmCol.setPrefWidth(200);
        
        machineCodeTable.getColumns().addAll(addressCol, bytesCol, asmCol);
        machineCodeTab.setContent(new VBox(machineCodeTable));
        
        // Console output tab
        consoleTab = new Tab("Console");
        consoleOutput = new TextArea();
        consoleOutput.setEditable(false);
        consoleOutput.getStyleClass().add("console-output");
        consoleTab.setContent(new VBox(consoleOutput));
        
        centerTabPane.getTabs().addAll(machineCodeTab, consoleTab);
        centerTabPane.setPrefWidth(500);
    }
    
    /**
     * Create the right panel with CPU state, memory, and AI assistant.
     */
    private void createRightPanel() {
        rightTabPane = new TabPane();
        rightTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Registers tab
        registersTab = new Tab("Registers");
        registersPanel = new GridPane();
        registersPanel.setHgap(10);
        registersPanel.setVgap(5);
        registersPanel.setPadding(new Insets(10));
        registersTab.setContent(new VBox(registersPanel));
        
        // Memory tab
        memoryTab = new Tab("Memory");
        memoryTable = new TableView<>();
        
        TableColumn<MemoryEntry, String> memAddrCol = new TableColumn<>("Address");
        TableColumn<MemoryEntry, String> memValueCol = new TableColumn<>("Value");
        TableColumn<MemoryEntry, String> memAsciiCol = new TableColumn<>("ASCII");
        
        memAddrCol.setPrefWidth(80);
        memValueCol.setPrefWidth(60);
        memAsciiCol.setPrefWidth(50);
        
        memoryTable.getColumns().addAll(memAddrCol, memValueCol, memAsciiCol);
        memoryTab.setContent(new VBox(memoryTable));
        
        // Breakpoints tab
        breakpointsTab = new Tab("Breakpoints");
        breakpointsPanel = new VBox(10);
        breakpointsPanel.setPadding(new Insets(10));
        breakpointsTab.setContent(breakpointsPanel);
        
        // AI Assistant tab
        aiTab = new Tab("AI Assistant");
        createAIPanel();
        aiTab.setContent(aiPanel);
        
        rightTabPane.getTabs().addAll(registersTab, memoryTab, breakpointsTab, aiTab);
        rightTabPane.setPrefWidth(350);
        
        // Assemble the horizontal split
        horizontalSplit.getItems().addAll(leftTabPane, centerTabPane, rightTabPane);
        horizontalSplit.setDividerPositions(0.35, 0.65);
    }
    
    /**
     * Create the AI assistant panel.
     */
    private void createAIPanel() {
        aiPanel = new VBox(10);
        aiPanel.setPadding(new Insets(10));
        
        Label promptLabel = new Label("Describe your assembly program:");
        promptLabel.getStyleClass().add("section-header");
        
        aiPrompt = new TextArea();
        aiPrompt.setPromptText("Example: Create a program that adds two numbers...");
        aiPrompt.setPrefRowCount(4);
        
        generateButton = new Button("Generate Code");
        generateButton.getStyleClass().addAll("primary-button", "ai-button");
        generateButton.setPrefWidth(150);
        
        Label responseLabel = new Label("Generated Code:");
        responseLabel.getStyleClass().add("section-header");
        
        aiResponse = new TextArea();
        aiResponse.setEditable(false);
        aiResponse.setPrefRowCount(8);
        aiResponse.getStyleClass().add("code-response");
        
        useCodeButton = new Button("Use This Code");
        useCodeButton.getStyleClass().add("secondary-button");
        useCodeButton.setPrefWidth(150);
        useCodeButton.setDisable(true);
        
        aiPanel.getChildren().addAll(
                promptLabel, aiPrompt, generateButton,
                new Separator(),
                responseLabel, aiResponse, useCodeButton
        );
    }
      /**
     * Create the status bar.
     */
    private void createStatusBar() {
        statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.getStyleClass().add("status-bar");
        
        statusLabel = new Label("Ready");
        progressBar = new ProgressBar();
        progressBar.setVisible(false);
        progressBar.setPrefWidth(100);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        memoryUsageLabel = new Label("Memory: 0 / 65536 bytes");
        
        statusBar.getChildren().addAll(statusLabel, progressBar, spacer, memoryUsageLabel);
    }
    
    /**
     * Set up event handlers for all interactive components.
     */
    private void setupEventHandlers() {
        // TODO: Add event handlers for all buttons and menu items
        // This will be implemented when we connect to the business logic
    }
    
    /**
     * Check if there are unsaved changes in the editor.
     */
    private boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }
    
    /**
     * Handle application exit with unsaved changes check.
     */
    private void handleExit() {
        if (hasUnsavedChanges()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unsaved Changes");
            alert.setHeaderText("You have unsaved changes");
            alert.setContentText("Do you want to save before exiting?");
            
            ButtonType saveButton = new ButtonType("Save");
            ButtonType dontSaveButton = new ButtonType("Don't Save");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == saveButton) {
                    // TODO: Implement save functionality
                    Platform.exit();
                } else if (result.get() == dontSaveButton) {
                    Platform.exit();
                }
                // If cancel, do nothing (don't exit)
            }
        } else {
            Platform.exit();
        }
    }
    
    /**
     * Show welcome dialog on startup.
     */
    private void showWelcomeDialog() {
        Alert welcome = new Alert(Alert.AlertType.INFORMATION);
        welcome.setTitle("Welcome");
        welcome.setHeaderText("Motorola 6800 Assembler & Simulator");
        welcome.setContentText("Welcome to the modern GUI version of the Motorola 6800 Assembler!\n\n" +
                "Features:\n" +
                "• Syntax-highlighted code editor\n" +
                "• Real-time assembly and simulation\n" +
                "• Interactive debugging\n" +
                "• AI-powered code generation\n" +
                "• Modern dark theme");
        welcome.showAndWait();
    }
    
    /**
     * Create all the main panels (left, center, right).
     */
    private void createMainPanels() {
        createLeftPanel();
        createCenterPanel();
        createRightPanel();
    }
    
    /**
     * Assemble the final layout structure.
     */
    private void assembleLayout() {
        rootLayout.setTop(new VBox(menuBar, toolBar));
        rootLayout.setCenter(mainSplitPane);
        rootLayout.setBottom(statusBar);
    }
    
    // ========== UserInterface Implementation ==========
    
    @Override
    public void initialize() {
        // Already initialized in start() method
    }
    
    @Override
    public void showWelcome() {
        Platform.runLater(() -> {
            Alert welcome = new Alert(Alert.AlertType.INFORMATION);
            welcome.setTitle("Welcome");
            welcome.setHeaderText("Motorola 6800 Assembler & Simulator");
            welcome.setContentText("Welcome to the modern GUI version of the Motorola 6800 Assembler!\n\n" +
                    "Features:\n" +
                    "• Syntax-highlighted code editor\n" +
                    "• Real-time assembly and simulation\n" +
                    "• Interactive debugging\n" +
                    "• AI-powered code generation\n" +
                    "• Modern dark theme");
            welcome.showAndWait();
        });
    }
    
    @Override
    public void close() {
        Platform.runLater(() -> {
            if (primaryStage != null) {
                primaryStage.close();
            }
        });
    }
    
    @Override
    public int showMainMenu() {
        // For GUI, we don't show a traditional menu - return a default or wait for user action
        menuChoiceFuture = new CompletableFuture<>();
        try {
            return menuChoiceFuture.get(); // This will be completed by button actions
        } catch (Exception e) {
            return 0; // Exit
        }
    }
    
    @Override
    public int showSimulatorMenu() {
        // Similar to main menu, GUI uses buttons instead of menu choices
        return 0;
    }
    
    @Override
    public int showBreakpointMenu() {
        return 0;
    }
    
    @Override
    public int showAIMenu() {
        // Switch to AI tab
        Platform.runLater(() -> rightTabPane.getSelectionModel().select(aiTab));
        return 1;
    }
    
    @Override
    public String getInput(String prompt) {
        inputFuture = new CompletableFuture<>();
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Input Required");
            dialog.setHeaderText(null);
            dialog.setContentText(prompt);
            
            Optional<String> result = dialog.showAndWait();
            inputFuture.complete(result.orElse(""));
        });
        
        try {
            return inputFuture.get();
        } catch (Exception e) {
            return "";
        }
    }
    
    @Override
    public String getMultiLineInput() {
        // For GUI, use the code editor content
        return codeEditor.getText();
    }
    
    @Override
    public int getIntInput(String prompt) {
        String input = getInput(prompt);
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    @Override
    public String getApiKey() {
        CompletableFuture<String> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("OpenAI API Key");
            dialog.setHeaderText("Enter your OpenAI API Key");
            dialog.setContentText("API Key:");
            
            // Make the text field show asterisks
            dialog.getEditor().textProperty().addListener((obs, oldText, newText) -> {
                if (!newText.equals(oldText)) {
                    String masked = "*".repeat(newText.length());
                    if (!dialog.getEditor().getText().equals(masked)) {
                        Platform.runLater(() -> dialog.getEditor().setText(masked));
                    }
                }
            });
            
            Optional<String> result = dialog.showAndWait();
            future.complete(result.orElse(""));
        });
        
        try {
            return future.get();
        } catch (Exception e) {
            return "";
        }
    }
    
    @Override
    public String getAIPrompt() {
        return aiPrompt.getText();
    }
    
    @Override
    public void showMessage(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            consoleOutput.appendText(message + "\n");
        });
    }
    
    @Override
    public void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            
            statusLabel.setText("Error: " + message);
            consoleOutput.appendText("ERROR: " + message + "\n");
        });
    }
    
    @Override
    public void showWarning(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            
            statusLabel.setText("Warning: " + message);
            consoleOutput.appendText("WARNING: " + message + "\n");
        });
    }
    
    @Override
    public void showInfo(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            consoleOutput.appendText("INFO: " + message + "\n");
        });
    }
    
    @Override
    public void showMachineCode(Map<Integer, List<Integer>> machineCode) {
        Platform.runLater(() -> {
            centerTabPane.getSelectionModel().select(machineCodeTab);
            // TODO: Populate machine code table
        });
    }
    
    @Override
    public void showMachineCode(List<AssemblyLine> assemblyLines, Map<Integer, List<Integer>> machineCode) {
        Platform.runLater(() -> {
            centerTabPane.getSelectionModel().select(machineCodeTab);
            // TODO: Populate machine code table with assembly lines
        });
    }
    
    @Override
    public void showInstructionSet() {
        Platform.runLater(() -> {
            Alert help = new Alert(Alert.AlertType.INFORMATION);
            help.setTitle("Instruction Set Reference");
            help.setHeaderText("Motorola 6800 Instruction Set");
            help.setContentText("Instruction set reference will be displayed here...");
            help.showAndWait();
        });
    }
    
    @Override
    public void showGeneratedCode(String code) {
        Platform.runLater(() -> {
            aiResponse.setText(code);
            useCodeButton.setDisable(false);
            rightTabPane.getSelectionModel().select(aiTab);
        });
    }
    
    @Override
    public boolean confirmGeneratedCode() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Use Generated Code");
            confirm.setHeaderText("Use the AI-generated code?");
            confirm.setContentText("Do you want to load this code into the editor?");
            
            Optional<ButtonType> result = confirm.showAndWait();
            future.complete(result.isPresent() && result.get() == ButtonType.OK);
        });
        
        try {
            return future.get();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public void showRegisters(Registers registers) {
        Platform.runLater(() -> {
            rightTabPane.getSelectionModel().select(registersTab);
            // TODO: Update registers display
        });
    }
    
    @Override
    public void showMemory(Memory memory, int startAddr, int length) {
        Platform.runLater(() -> {
            rightTabPane.getSelectionModel().select(memoryTab);
            // TODO: Update memory table
        });
    }
    
    @Override
    public void showExecutionResult(ExecutionResult result) {
        Platform.runLater(() -> {
            showMessage("Execution: " + result.getStatus());
            // TODO: Update execution state display
        });
    }
    
    @Override
    public void showApiKeyStatus(boolean isSet) {
        Platform.runLater(() -> {
            String status = isSet ? "✓ API Key configured" : "✗ API Key not set";
            showMessage(status);
        });
    }
    
    // Helper classes for table data
    public static class MachineCodeEntry {
        private final String address;
        private final String machineCode;
        private final String assembly;
        
        public MachineCodeEntry(String address, String machineCode, String assembly) {
            this.address = address;
            this.machineCode = machineCode;
            this.assembly = assembly;
        }
        
        public String getAddress() { return address; }
        public String getMachineCode() { return machineCode; }
        public String getAssembly() { return assembly; }
    }
    
    public static class MemoryEntry {
        private final String address;
        private final String value;
        private final String ascii;
        
        public MemoryEntry(String address, String value, String ascii) {
            this.address = address;
            this.value = value;
            this.ascii = ascii;
        }
        
        public String getAddress() { return address; }
        public String getValue() { return value; }
        public String getAscii() { return ascii; }
    }
}
