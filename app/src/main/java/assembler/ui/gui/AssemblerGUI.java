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
import org.fxmisc.richtext.model.TwoDimensional;

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
    
    // === Menu Items ===
    private MenuItem newFile, openFile, saveFile, saveAsFile, exitApp;
    private MenuItem undo, redo, cut, copy, paste, selectAll;
    private MenuItem assemble, assembleRun, clean;
    private MenuItem startDebug, stepInto, stepOver, stepOut, toggleBreakpoint, clearBreakpoints;
    private MenuItem generateCode, configureAPI, showExamples;
    private MenuItem instructionSet, userGuide, about;
    
    // === Toolbar Buttons ===
    private Button newBtn, openBtn, saveBtn;
    private Button assembleBtn, runBtn, debugBtn, stopBtn;
    private Button stepBtn, breakpointBtn, aiBtn;
    
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
        newFile = new MenuItem("New");
        openFile = new MenuItem("Open...");
        saveFile = new MenuItem("Save");
        saveAsFile = new MenuItem("Save As...");
        exitApp = new MenuItem("Exit");
        
        newFile.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+N"));
        openFile.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+O"));
        saveFile.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+S"));
        
        fileMenu.getItems().addAll(newFile, openFile, new SeparatorMenuItem(),
                saveFile, saveAsFile, new SeparatorMenuItem(), exitApp);
        
        // Edit menu
        Menu editMenu = new Menu("Edit");
        undo = new MenuItem("Undo");
        redo = new MenuItem("Redo");
        cut = new MenuItem("Cut");
        copy = new MenuItem("Copy");
        paste = new MenuItem("Paste");
        selectAll = new MenuItem("Select All");
        
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
        assemble = new MenuItem("Assemble");
        assembleRun = new MenuItem("Assemble & Run");
        clean = new MenuItem("Clean");
        
        assemble.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F7"));
        assembleRun.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F5"));
        
        buildMenu.getItems().addAll(assemble, assembleRun, new SeparatorMenuItem(), clean);
        
        // Debug menu
        Menu debugMenu = new Menu("Debug");
        startDebug = new MenuItem("Start Debugging");
        stepInto = new MenuItem("Step Into");
        stepOver = new MenuItem("Step Over");
        stepOut = new MenuItem("Step Out");
        toggleBreakpoint = new MenuItem("Toggle Breakpoint");
        clearBreakpoints = new MenuItem("Clear All Breakpoints");
        
        startDebug.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F9"));
        stepInto.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F11"));
        stepOver.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F10"));
        toggleBreakpoint.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F8"));
        
        debugMenu.getItems().addAll(startDebug, new SeparatorMenuItem(),
                stepInto, stepOver, stepOut, new SeparatorMenuItem(),
                toggleBreakpoint, clearBreakpoints);
        
        // AI menu
        Menu aiMenu = new Menu("AI Assistant");
        generateCode = new MenuItem("Generate Code...");
        configureAPI = new MenuItem("Configure API Key...");
        showExamples = new MenuItem("Show Examples");
        
        aiMenu.getItems().addAll(generateCode, configureAPI, new SeparatorMenuItem(), showExamples);
        
        // Help menu
        Menu helpMenu = new Menu("Help");
        instructionSet = new MenuItem("Instruction Set Reference");
        userGuide = new MenuItem("User Guide");
        about = new MenuItem("About");
        
        helpMenu.getItems().addAll(instructionSet, userGuide, new SeparatorMenuItem(), about);
        
        menuBar.getMenus().addAll(fileMenu, editMenu, buildMenu, debugMenu, aiMenu, helpMenu);
    }
      /**
     * Create the toolbar with commonly used actions.
     */
    private void createToolBar() {
        toolBar = new ToolBar();
        
        newBtn = new Button("New");
        openBtn = new Button("Open");
        saveBtn = new Button("Save");
        
        Separator sep1 = new Separator();
        
        assembleBtn = new Button("Assemble");
        runBtn = new Button("Run");
        debugBtn = new Button("Debug");
        stopBtn = new Button("Stop");
        
        Separator sep2 = new Separator();
        
        stepBtn = new Button("Step");
        breakpointBtn = new Button("Breakpoint");
        
        Separator sep3 = new Separator();
        
        aiBtn = new Button("AI Assistant");
        
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

        // Simulator Controls tab
        simulatorControlTab = new Tab("Simulator Controls");
        createSimulatorControls();
        simulatorControlTab.setContent(simulatorPanel);
        
        rightTabPane.getTabs().addAll(registersTab, memoryTab, breakpointsTab, aiTab, simulatorControlTab);
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
     * Create the simulator controls panel.
     */
    private void createSimulatorControls() {
        simulatorPanel = new VBox(10);
        simulatorPanel.setPadding(new Insets(10));

        runButton = new Button("Run");
        stepButton = new Button("Step");
        pauseButton = new Button("Pause");
        resetButton = new Button("Reset");
        stopButton = new Button("Stop");

        speedLabel = new Label("Speed:");
        speedSlider = new Slider(0.1, 2.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(0.5);
        speedSlider.setMinorTickCount(4);
        speedSlider.setBlockIncrement(0.1);
        speedSlider.setPrefWidth(180);

        HBox speedBox = new HBox(10, speedLabel, speedSlider);
        speedBox.setPadding(new Insets(5, 0, 5, 0));
        speedBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        HBox buttonBox = new HBox(10, runButton, stepButton, pauseButton, resetButton, stopButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        simulatorPanel.getChildren().addAll(buttonBox, speedBox);
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
        // === File Menu Event Handlers ===
        newFile.setOnAction(e -> handleNewFile());
        openFile.setOnAction(e -> handleOpenFile());
        saveFile.setOnAction(e -> handleSaveFile());
        saveAsFile.setOnAction(e -> handleSaveAsFile());
        exitApp.setOnAction(e -> handleExit());
        
        // === Edit Menu Event Handlers ===
        undo.setOnAction(e -> codeEditor.undo());
        redo.setOnAction(e -> codeEditor.redo());
        cut.setOnAction(e -> codeEditor.cut());
        copy.setOnAction(e -> codeEditor.copy());
        paste.setOnAction(e -> codeEditor.paste());
        selectAll.setOnAction(e -> codeEditor.selectAll());
        
        // === Build Menu Event Handlers ===
        assemble.setOnAction(e -> handleAssemble());
        assembleRun.setOnAction(e -> handleAssembleAndRun());
        clean.setOnAction(e -> handleClean());
        
        // === Debug Menu Event Handlers ===
        startDebug.setOnAction(e -> handleStartDebug());
        stepInto.setOnAction(e -> handleStepInto());
        stepOver.setOnAction(e -> handleStepOver());
        stepOut.setOnAction(e -> handleStepOut());
        toggleBreakpoint.setOnAction(e -> handleToggleBreakpoint());
        clearBreakpoints.setOnAction(e -> handleClearBreakpoints());
        
        // === AI Menu Event Handlers ===
        generateCode.setOnAction(e -> handleGenerateCode());
        configureAPI.setOnAction(e -> handleConfigureAPI());
        showExamples.setOnAction(e -> handleShowExamples());
        
        // === Help Menu Event Handlers ===
        instructionSet.setOnAction(e -> handleInstructionSet());
        userGuide.setOnAction(e -> handleUserGuide());
        about.setOnAction(e -> handleAbout());
        
        // === Toolbar Button Event Handlers ===
        newBtn.setOnAction(e -> handleNewFile());
        openBtn.setOnAction(e -> handleOpenFile());
        saveBtn.setOnAction(e -> handleSaveFile());
        assembleBtn.setOnAction(e -> handleAssemble());
        runBtn.setOnAction(e -> handleRun());
        debugBtn.setOnAction(e -> handleStartDebug());
        stopBtn.setOnAction(e -> handleStop());
        stepBtn.setOnAction(e -> handleStepInto());
        breakpointBtn.setOnAction(e -> handleToggleBreakpoint());
        aiBtn.setOnAction(e -> handleShowAI());
        
        // === AI Panel Event Handlers ===
        generateButton.setOnAction(e -> handleAIGenerate());
        useCodeButton.setOnAction(e -> handleUseGeneratedCode());
        
        // === Simulator Controls Event Handlers ===
        runButton.setOnAction(e -> handleSimulatorRun());
        stepButton.setOnAction(e -> handleSimulatorStep());
        pauseButton.setOnAction(e -> handleSimulatorPause());
        resetButton.setOnAction(e -> handleSimulatorReset());
        stopButton.setOnAction(e -> handleSimulatorStop());
        
        // === Code Editor Event Handlers ===
        codeEditor.textProperty().addListener((obs, oldText, newText) -> {
            hasUnsavedChanges = true;
            updateStatusBar();
        });
        
        codeEditor.caretPositionProperty().addListener((obs, oldPos, newPos) -> {
            updateCaretPosition();
        });
        
        // === File Explorer Event Handlers ===
        fileExplorer.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                handleFileExplorerDoubleClick();
            }
        });
        
        // === Memory Table Event Handlers ===
        memoryTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                handleMemoryTableDoubleClick();
            }
        });
        
        // === Speed Slider Event Handler ===
        if (speedSlider != null) {
            speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateSimulationSpeed(newVal.doubleValue());
            });
        }    }
    
    /**
     * Check if there are unsaved changes in the editor.
     */
    private boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
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
        });    }
    
    // ========== Event Handler Methods ==========
    
    /**
     * Handle new file action.
     */
    private void handleNewFile() {
        if (hasUnsavedChanges) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Unsaved Changes");
            confirm.setHeaderText("You have unsaved changes. Do you want to continue?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }
        
        codeEditor.clear();
        currentFile = null;
        hasUnsavedChanges = false;
        updateStatusBar();
        showMessage("New file created");
    }
    
    /**
     * Handle open file action.
     */
    private void handleOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Assembly File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Assembly Files", "*.asm", "*.s"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                String content = FileManager.loadSourceFile(file.getAbsolutePath());
                codeEditor.replaceText(content);
                currentFile = file;
                hasUnsavedChanges = false;
                updateStatusBar();
                showMessage("File loaded: " + file.getName());
            } catch (Exception e) {
                showError("Failed to load file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle save file action.
     */
    private void handleSaveFile() {
        if (currentFile == null) {
            handleSaveAsFile();
        } else {
            try {
                FileManager.saveSourceFile(currentFile.getAbsolutePath(), codeEditor.getText());
                hasUnsavedChanges = false;
                updateStatusBar();
                showMessage("File saved: " + currentFile.getName());
            } catch (Exception e) {
                showError("Failed to save file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle save as file action.
     */
    private void handleSaveAsFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Assembly File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Assembly Files", "*.asm"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                FileManager.saveSourceFile(file.getAbsolutePath(), codeEditor.getText());
                currentFile = file;
                hasUnsavedChanges = false;
                updateStatusBar();
                showMessage("File saved: " + file.getName());
            } catch (Exception e) {
                showError("Failed to save file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle application exit.
     */
    private void handleExit() {
        if (hasUnsavedChanges) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Unsaved Changes");
            confirm.setHeaderText("You have unsaved changes. Do you want to save before exiting?");
            confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent()) {
                if (result.get() == ButtonType.YES) {
                    handleSaveFile();
                } else if (result.get() == ButtonType.CANCEL) {
                    return;
                }
            }
        }
        Platform.exit();
    }
    
    /**
     * Handle assemble action.
     */
    private void handleAssemble() {
        try {
            String sourceCode = codeEditor.getText();
            if (sourceCode.trim().isEmpty()) {
                showError("No source code to assemble.");
                return;
            }
            
            app.setSourceCode(sourceCode);
            
            // Parse source code
            showMessage("Parsing source code...");
            List<AssemblyLine> program = app.getParser().parseSource(sourceCode);
            app.setCurrentProgram(program);
            
            // Generate machine code
            showMessage("Generating machine code...");
            Map<Integer, List<Integer>> machineCode = app.getCodeGenerator().generateCode(program);
            app.setMachineCode(machineCode);
            
            showMessage("Assembly completed successfully! Generated " + 
                       machineCode.values().stream().mapToInt(List::size).sum() + " bytes of machine code.");
            
            // Update machine code display
            updateMachineCodeTable(program, machineCode);
            centerTabPane.getSelectionModel().select(machineCodeTab);
            
        } catch (Exception e) {
            showError("Assembly failed: " + e.getMessage());
        }
    }
    
    /**
     * Handle assemble and run action.
     */
    private void handleAssembleAndRun() {
        handleAssemble();
        if (!app.getCurrentProgram().isEmpty()) {
            handleRun();
        }
    }
    
    /**
     * Handle clean action.
     */
    private void handleClean() {
        machineCodeData.clear();
        consoleOutput.clear();
        showMessage("Workspace cleaned.");
    }
    
    /**
     * Handle run action.
     */
    private void handleRun() {
        try {
            if (app.getCurrentProgram().isEmpty()) {
                showError("No program loaded. Please assemble first.");
                return;
            }
            
            app.getSimulator().loadProgram(app.getCurrentProgram());
            showMessage("Program loaded into simulator. Use debug controls to step through execution.");
            rightTabPane.getSelectionModel().select(registersTab);
            
        } catch (Exception e) {
            showError("Failed to load program: " + e.getMessage());
        }
    }
    
    /**
     * Handle start debugging action.
     */
    private void handleStartDebug() {
        handleRun();
        rightTabPane.getSelectionModel().select(registersTab);
        isSimulationRunning = true;
        updateSimulatorControls();
    }
    
    /**
     * Handle step into action.
     */
    private void handleStepInto() {
        try {
            ExecutionResult result = app.getSimulator().step();
            showExecutionResult(result);
            updateRegisterDisplay();
            updateMemoryDisplay();
        } catch (Exception e) {
            showError("Step execution failed: " + e.getMessage());
        }
    }
    
    /**
     * Handle step over action.
     */
    private void handleStepOver() {
        // For simplicity, treat step over same as step into for now
        handleStepInto();
    }
    
    /**
     * Handle step out action.
     */
    private void handleStepOut() {
        // For simplicity, treat step out same as step into for now
        handleStepInto();
    }
    
    /**
     * Handle stop action.
     */
    private void handleStop() {
        isSimulationRunning = false;
        app.getSimulator().reset();
        updateSimulatorControls();
        updateRegisterDisplay();
        updateMemoryDisplay();
        showMessage("Simulation stopped and reset.");
    }
    
    /**
     * Handle toggle breakpoint action.
     */
    private void handleToggleBreakpoint() {
        // Get current line in code editor
        int caretPos = codeEditor.getCaretPosition();
        int currentLine = codeEditor.offsetToPosition(caretPos, TwoDimensional.Bias.Forward).getMajor();
        
        showMessage("Breakpoint toggled at line " + (currentLine + 1));
        // TODO: Implement actual breakpoint logic
    }
    
    /**
     * Handle clear breakpoints action.
     */
    private void handleClearBreakpoints() {
        app.getSimulator().clearBreakpoints();
        showMessage("All breakpoints cleared.");
    }
    
    /**
     * Handle generate code action.
     */
    private void handleGenerateCode() {
        rightTabPane.getSelectionModel().select(aiTab);
        aiPrompt.requestFocus();
    }
    
    /**
     * Handle configure API action.
     */
    private void handleConfigureAPI() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Configure OpenAI API Key");
        dialog.setHeaderText("Enter your OpenAI API Key:");
        dialog.setContentText("API Key:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            app.getAIGenerator().setApiKey(result.get().trim());
            showMessage("API Key configured successfully.");
        }
    }
    
    /**
     * Handle show examples action.
     */
    private void handleShowExamples() {
        String example = "; Motorola 6800 Assembly Example\n" +
                        "; Add two numbers and store result\n\n" +
                        "        ORG     $1000    ; Set origin address\n\n" +
                        "START:  LDA     #$05     ; Load 5 into accumulator A\n" +
                        "        ADD     #$03     ; Add 3\n" +
                        "        STA     $2000    ; Store result at $2000\n" +
                        "        HLT              ; Halt execution\n\n" +
                        "        END     START    ; End with start label\n";
        
        codeEditor.replaceText(example);
        hasUnsavedChanges = true;
        updateStatusBar();
        showMessage("Example code loaded into editor.");
    }
    
    /**
     * Handle instruction set action.
     */
    private void handleInstructionSet() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Motorola 6800 Instruction Set");
        info.setHeaderText("Motorola 6800 Instruction Reference");
        info.setContentText("Load/Store: LDA, STA, LDX, STX\n" +
                           "Arithmetic: ADD, SUB, INC, DEC\n" +
                           "Logic: AND, ORA, EOR\n" +
                           "Branch: BRA, BEQ, BNE, BMI, BPL\n" +
                           "Jump/Call: JMP, JSR, RTS\n" +
                           "Control: NOP, HLT\n" +
                           "Pseudo: ORG, END, EQU");
        info.getDialogPane().setPrefWidth(400);
        info.showAndWait();
    }
    
    /**
     * Handle user guide action.
     */
    private void handleUserGuide() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("User Guide");
        info.setHeaderText("How to use the Motorola 6800 Assembler");
        info.setContentText("1. Write assembly code in the editor\n" +
                           "2. Press F7 or click Assemble to compile\n" +
                           "3. Use Debug menu to step through execution\n" +
                           "4. View registers and memory on the right panel\n" +
                           "5. Use AI Assistant to generate code");
        info.showAndWait();
    }
    
    /**
     * Handle about action.
     */
    private void handleAbout() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("About");
        info.setHeaderText("Motorola 6800 Assembler & Simulator");
        info.setContentText("Professional Edition\n\n" +
                           "A complete development environment for Motorola 6800 assembly programming.\n\n" +
                           "Features:\n" +
                           "• Syntax highlighting\n" +
                           "• Real-time assembly\n" +
                           "• CPU simulation\n" +
                           "• AI code generation\n\n" +
                           "Version 1.0\n" +
                           "© 2024 Motorola 6800 Assembler Team");
        info.showAndWait();
    }
    
    /**
     * Handle show AI assistant action.
     */
    private void handleShowAI() {
        rightTabPane.getSelectionModel().select(aiTab);
    }
    
    /**
     * Handle AI generate action.
     */
    private void handleAIGenerate() {
        String prompt = aiPrompt.getText().trim();
        if (prompt.isEmpty()) {
            showError("Please enter a description for the assembly program.");
            return;
        }
        
        if (!app.getAIGenerator().isInitialized()) {
            showError("Please configure your OpenAI API key first.");
            handleConfigureAPI();
            return;
        }
        
        // Show progress
        generateButton.setDisable(true);
        aiProgressIndicator.setVisible(true);
        aiResponse.setText("Generating code...");
        
        // Run AI generation in background
        Task<String> aiTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return app.getAIGenerator().generateAssemblyCode(prompt);
            }
        };
        
        aiTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                String generatedCode = aiTask.getValue();
                if (generatedCode != null && !generatedCode.trim().isEmpty()) {
                    aiResponse.setText(generatedCode);
                    useCodeButton.setDisable(false);
                    showMessage("Code generated successfully!");
                } else {
                    aiResponse.setText("Failed to generate code. Please try again.");
                }
                generateButton.setDisable(false);
                aiProgressIndicator.setVisible(false);
            });
        });
        
        aiTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                aiResponse.setText("Error generating code: " + aiTask.getException().getMessage());
                generateButton.setDisable(false);
                aiProgressIndicator.setVisible(false);
            });
        });
        
        new Thread(aiTask).start();
    }
    
    /**
     * Handle use generated code action.
     */
    private void handleUseGeneratedCode() {
        String generatedCode = aiResponse.getText();
        if (!generatedCode.isEmpty() && !generatedCode.startsWith("Generating") && 
            !generatedCode.startsWith("Error") && !generatedCode.startsWith("Failed")) {
            
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Use Generated Code");
            confirm.setHeaderText("Replace current code with AI generated code?");
            confirm.setContentText("This will replace the current content in the editor.");
            
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                codeEditor.replaceText(generatedCode);
                hasUnsavedChanges = true;
                updateStatusBar();
                showMessage("AI generated code loaded into editor.");
                leftTabPane.getSelectionModel().select(codeTab);
            }
        }
    }
    
    /**
     * Handle simulator run action.
     */
    private void handleSimulatorRun() {
        if (!isSimulationRunning) {
            handleRun();
        } else {
            // Continue running
            try {
                while (isSimulationRunning) {
                    ExecutionResult result = app.getSimulator().step();                    if (result.getStatus() == assembler.simulator.ExecutionStatus.HALTED ||
                        result.getStatus() == assembler.simulator.ExecutionStatus.ERROR) {
                        isSimulationRunning = false;
                        break;
                    }
                    // Add small delay to prevent UI freezing
                    Thread.sleep(100);
                }
                updateRegisterDisplay();
                updateMemoryDisplay();
                updateSimulatorControls();
            } catch (Exception e) {
                showError("Execution error: " + e.getMessage());
                isSimulationRunning = false;
                updateSimulatorControls();
            }
        }
    }
    
    /**
     * Handle simulator step action.
     */
    private void handleSimulatorStep() {
        handleStepInto();
    }
    
    /**
     * Handle simulator pause action.
     */
    private void handleSimulatorPause() {
        isSimulationRunning = false;
        updateSimulatorControls();
        showMessage("Simulation paused.");
    }
    
    /**
     * Handle simulator reset action.
     */
    private void handleSimulatorReset() {
        handleStop();
    }
    
    /**
     * Handle simulator stop action.
     */
    private void handleSimulatorStop() {
        handleStop();
    }
    
    /**
     * Handle file explorer double click.
     */
    private void handleFileExplorerDoubleClick() {
        TreeItem<String> selectedItem = fileExplorer.getSelectionModel().getSelectedItem();
        if (selectedItem != null && !selectedItem.getValue().endsWith("/")) {
            // It's a file, try to open it
            showMessage("Opening file: " + selectedItem.getValue());
            // TODO: Implement file opening from explorer
        }
    }
    
    /**
     * Handle memory table double click.
     */
    private void handleMemoryTableDoubleClick() {
        MemoryEntry selectedEntry = memoryTable.getSelectionModel().getSelectedItem();
        if (selectedEntry != null) {
            // Allow editing memory value
            TextInputDialog dialog = new TextInputDialog(selectedEntry.getValue());
            dialog.setTitle("Edit Memory");
            dialog.setHeaderText("Edit memory at address " + selectedEntry.getAddress());
            dialog.setContentText("Value (hex):");
            
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                try {
                    int address = Integer.parseInt(selectedEntry.getAddress().substring(2), 16);
                    int value = Integer.parseInt(result.get(), 16);
                    app.getSimulator().getMemory().writeByte(address, value);
                    updateMemoryDisplay();
                    showMessage("Memory updated at " + selectedEntry.getAddress());
                } catch (NumberFormatException e) {
                    showError("Invalid hexadecimal value.");
                }
            }
        }
    }
    
    /**
     * Update simulation speed.
     */
    private void updateSimulationSpeed(double speed) {
        if (speedLabel != null) {
            speedLabel.setText("Speed: " + String.format("%.1f", speed));
        }
    }
    
    /**
     * Update status bar with current information.
     */
    private void updateStatusBar() {
        if (statusLabel != null) {
            String status = currentFile != null ? currentFile.getName() : "Untitled";
            if (hasUnsavedChanges) {
                status += " *";
            }
            statusLabel.setText(status);
        }
        
        updateCaretPosition();
    }
    
    /**
     * Update caret position display.
     */
    private void updateCaretPosition() {
        if (positionLabel != null) {
            int caretPos = codeEditor.getCaretPosition();
            var position = codeEditor.offsetToPosition(caretPos, TwoDimensional.Bias.Forward);
            positionLabel.setText("Line: " + (position.getMajor() + 1) + 
                                 ", Col: " + (position.getMinor() + 1));
        }
    }
    
    /**
     * Update machine code table with assembly and machine code.
     */
    private void updateMachineCodeTable(List<AssemblyLine> assemblyLines, Map<Integer, List<Integer>> machineCode) {
        machineCodeData.clear();
        
        for (AssemblyLine line : assemblyLines) {
            if (line.getAddress() >= 0 && machineCode.containsKey(line.getAddress())) {
                List<Integer> bytes = machineCode.get(line.getAddress());
                StringBuilder bytesStr = new StringBuilder();
                for (int b : bytes) {
                    bytesStr.append(String.format("%02X ", b));
                }
                
                MachineCodeEntry entry = new MachineCodeEntry(
                    String.format("$%04X", line.getAddress()),
                    bytesStr.toString().trim(),
                    line.getSourceLine()
                );
                machineCodeData.add(entry);
            }
        }
    }
    
    /**
     * Update register display.
     */
    private void updateRegisterDisplay() {
        if (registersPanel != null) {
            // Clear existing children
            registersPanel.getChildren().clear();
            
            // Get current register values
            Registers regs = app.getSimulator().getRegisters();
            
            // Add register labels and values
            int row = 0;
            addRegisterRow("A:", String.format("$%02X", regs.getAccumulatorA()), row++);
            addRegisterRow("B:", String.format("$%02X", regs.getAccumulatorB()), row++);
            addRegisterRow("X:", String.format("$%04X", regs.getIndexRegister()), row++);
            addRegisterRow("SP:", String.format("$%04X", regs.getStackPointer()), row++);
            addRegisterRow("PC:", String.format("$%04X", regs.getProgramCounter()), row++);
            addRegisterRow("CC:", String.format("$%02X", regs.getConditionCodeRegister()), row++);
        }
    }
    
    /**
     * Add a register row to the display.
     */
    private void addRegisterRow(String name, String value, int row) {
        Label nameLabel = new Label(name);
        Label valueLabel = new Label(value);
        nameLabel.getStyleClass().add("register-name");
        valueLabel.getStyleClass().add("register-value");
        
        registersPanel.add(nameLabel, 0, row);
        registersPanel.add(valueLabel, 1, row);
    }
    
    /**
     * Update memory display.
     */
    private void updateMemoryDisplay() {
        if (memoryTable != null) {
            memoryData.clear();
            
            Memory memory = app.getSimulator().getMemory();
            
            // Show memory from $0000 to $00FF for now
            for (int addr = 0; addr < 256; addr += 16) {
                StringBuilder hexStr = new StringBuilder();
                StringBuilder asciiStr = new StringBuilder();
                
                for (int i = 0; i < 16; i++) {
                    int b = memory.readByte(addr + i);
                    hexStr.append(String.format("%02X ", b & 0xFF));
                    char c = (char) (b & 0xFF);
                    asciiStr.append(Character.isISOControl(c) ? '.' : c);
                }
                
                MemoryEntry entry = new MemoryEntry(
                    String.format("$%04X", addr),
                    hexStr.toString().trim(),
                    asciiStr.toString()
                );
                memoryData.add(entry);
            }
        }
    }
    
    /**
     * Update simulator control buttons.
     */
    private void updateSimulatorControls() {
        if (runButton != null) {
            runButton.setText(isSimulationRunning ? "Continue" : "Run");
        }
        if (pauseButton != null) {
            pauseButton.setDisable(!isSimulationRunning);
        }
        if (stopButton != null) {
            stopButton.setDisable(!isSimulationRunning);
        }
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
