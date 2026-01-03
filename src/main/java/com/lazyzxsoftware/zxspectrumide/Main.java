package com.lazyzxsoftware.zxspectrumide;

import com.lazyzxsoftware.zxspectrumide.ui.windows.Z80TestWindow;
import javafx.scene.layout.StackPane;
import com.lazyzxsoftware.zxspectrumide.editor.CodeEditor;
import com.lazyzxsoftware.zxspectrumide.editor.FileManager;
import com.lazyzxsoftware.zxspectrumide.i18n.I18nManager;
import com.lazyzxsoftware.zxspectrumide.managers.WindowManager;
import com.lazyzxsoftware.zxspectrumide.theme.ThemeManager;
import com.lazyzxsoftware.zxspectrumide.ui.FileExplorer;
import com.lazyzxsoftware.zxspectrumide.ui.OutputConsole;
import com.lazyzxsoftware.zxspectrumide.ui.SettingsDialog;
import com.lazyzxsoftware.zxspectrumide.utils.SplashScreen;
import com.lazyzxsoftware.zxspectrumide.utils.PlatformUtils;
import com.lazyzxsoftware.zxspectrumide.ui.windows.DebugWindowManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    private SplitPane splitPaneHorizontal;
    private SplitPane splitPaneVertical;
    private OutputConsole outputConsole;

    private Label lblProject;
    private Button btnCollapse;
    private Stage primaryStage;
    private BorderPane rootLayout;
    private CodeEditor codeEditor;
    private FileManager fileManager;
    private I18nManager i18n;
    private FileExplorer fileExplorer;
    private VBox sidebarContainer;
    private boolean sidebarVisible = true;
    private double lastDividerPosition = 0.2;
    private javafx.scene.layout.HBox sidebarHeader;
    private javafx.scene.layout.Region spacer;

    @Override
    public void start(Stage primaryStage) {
        initMainStageComponents(primaryStage);

        SplashScreen splash = new SplashScreen();
        StackPane rootStack = new StackPane(rootLayout, splash);
        Scene scene = new Scene(rootStack, 1100, 800);

        ThemeManager.getInstance().registerScene(scene);

        primaryStage.setScene(scene);
        primaryStage.setTitle(i18n.get("app.title") + " (Develop)");
        PlatformUtils.setAppIcon(primaryStage, "/com/lazyzxsoftware/zxspectrumide/icons/app_icon.png");

        primaryStage.show();
        primaryStage.toFront();
        primaryStage.requestFocus();

        new Thread(() -> {
            try {
                splash.updateStatus("Inicializando núcleo...");
                Thread.sleep(300);

                splash.updateProgress(0.4);
                splash.updateStatus("Cargando configuración...");
                Thread.sleep(300);

                splash.updateProgress(0.8);
                splash.updateStatus("Preparando interfaz...");

                Platform.runLater(() -> rootLayout.requestLayout());
                Thread.sleep(400);

                splash.updateProgress(1.0);

                Platform.runLater(() -> {
                    splash.dismiss(() -> {
                        rootStack.getChildren().remove(splash);
                        primaryStage.setMaximized(true);
                        if (codeEditor != null) codeEditor.requestFocus();
                    });
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void initMainStageComponents(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.i18n = I18nManager.getInstance();

        codeEditor = new CodeEditor();
        fileManager = new FileManager(primaryStage, codeEditor);

        initRootLayout();

        primaryStage.setOnCloseRequest(event -> {
            if (fileManager.checkUnsavedChanges()) {
                WindowManager.getInstance().closeEmulator();
                Platform.exit();
                System.exit(0);
            } else {
                event.consume();
            }
        });
    }

    private void initRootLayout() {
        rootLayout = new BorderPane();
        MenuBar menuBar = createMenuBar();
        rootLayout.setTop(menuBar);

        fileExplorer = new FileExplorer(fileManager);
        sidebarHeader = new javafx.scene.layout.HBox();
        sidebarHeader.getStyleClass().add("sidebar-header");
        sidebarHeader.setStyle("-fx-padding: 5; -fx-alignment: center-left;");
        lblProject = new Label("PROYECTO");
        lblProject.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        btnCollapse = new Button("<<");
        btnCollapse.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 2; -fx-min-width: 25;");
        btnCollapse.setOnAction(e -> toggleSidebar());
        spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        sidebarHeader.getChildren().addAll(lblProject, spacer, btnCollapse);

        sidebarContainer = new javafx.scene.layout.VBox(sidebarHeader, fileExplorer);
        javafx.scene.layout.VBox.setVgrow(fileExplorer, javafx.scene.layout.Priority.ALWAYS);
        sidebarContainer.setMinWidth(0);

        splitPaneHorizontal = new SplitPane();
        splitPaneHorizontal.getItems().add(codeEditor);

        outputConsole = OutputConsole.getInstance();
        outputConsole.setOnLineRequest(lineNumber -> {
            codeEditor.requestFocus();
            codeEditor.goToLine(lineNumber);
        });

        splitPaneVertical = new SplitPane();
        splitPaneVertical.setOrientation(javafx.geometry.Orientation.VERTICAL);
        splitPaneVertical.getItems().addAll(splitPaneHorizontal, outputConsole);
        splitPaneVertical.setDividerPositions(0.8);

        rootLayout.setCenter(splitPaneVertical);
        toggleSidebar();
    }

    private void toggleSidebar() {
        if (sidebarVisible) {
            double[] dividers = splitPaneHorizontal.getDividerPositions();
            if (dividers.length > 0) lastDividerPosition = dividers[0];

            fileExplorer.setVisible(false); fileExplorer.setManaged(false);
            lblProject.setVisible(false); lblProject.setManaged(false);
            spacer.setVisible(false); spacer.setManaged(false);

            btnCollapse.setText(">>");
            sidebarHeader.setStyle("-fx-padding: 5 0 5 0; -fx-alignment: center;");
            sidebarHeader.setAlignment(javafx.geometry.Pos.CENTER);
            sidebarContainer.setMinWidth(50); sidebarContainer.setMaxWidth(50);

            sidebarVisible = false;
        } else {
            sidebarContainer.setMinWidth(0); sidebarContainer.setMaxWidth(Double.MAX_VALUE);
            fileExplorer.setVisible(true); fileExplorer.setManaged(true);
            lblProject.setVisible(true); lblProject.setManaged(true);
            spacer.setVisible(true); spacer.setManaged(true);

            btnCollapse.setText("<<");
            sidebarHeader.setStyle("-fx-padding: 5; -fx-alignment: center-left;");
            sidebarHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            if (splitPaneHorizontal.getItems().contains(sidebarContainer)) {
                splitPaneHorizontal.setDividerPositions(lastDividerPosition);
            }
            sidebarVisible = true;
        }
    }

    private void openProjectAction() {
        if (!fileManager.closeAllFiles()) return;

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta del proyecto");
        File selectedDirectory = directoryChooser.showDialog(primaryStage);

        if (selectedDirectory != null) {
            fileExplorer.setProjectRoot(selectedDirectory);
            lblProject.setText(selectedDirectory.getName().toUpperCase());
            ensureSidebarExpanded();

            if (!splitPaneHorizontal.getItems().contains(sidebarContainer)) {
                splitPaneHorizontal.getItems().add(0, sidebarContainer);
                splitPaneHorizontal.setDividerPositions(0.2);
            }
            fileManager.detectAndOpenMainFile(selectedDirectory);
        }
    }

    private void ensureSidebarExpanded() {
        sidebarContainer.setMinWidth(0); sidebarContainer.setMaxWidth(Double.MAX_VALUE);
        fileExplorer.setVisible(true); fileExplorer.setManaged(true);
        lblProject.setVisible(true); lblProject.setManaged(true);
        spacer.setVisible(true); spacer.setManaged(true);
        btnCollapse.setText("<<");
        sidebarHeader.setStyle("-fx-padding: 5; -fx-alignment: center-left;");
        sidebarHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        sidebarVisible = true;
    }

    private void closeProjectAction() {
        if (!fileManager.checkUnsavedChanges()) return;
        fileManager.closeAllFiles();
        fileExplorer.setProjectRoot(null);
        lblProject.setText("PROYECTO");
        if (splitPaneHorizontal.getItems().contains(sidebarContainer)) {
            splitPaneHorizontal.getItems().remove(sidebarContainer);
        }
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu menuFile = new Menu(i18n.get("menu.file"));
        MenuItem itemNew = new MenuItem(i18n.get("menu.file.new"));
        itemNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
        itemNew.setOnAction(e -> fileManager.newFile());

        MenuItem itemOpen = new MenuItem(i18n.get("menu.file.open"));
        itemOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        itemOpen.setOnAction(e -> fileManager.openFile());

        MenuItem itemSave = new MenuItem(i18n.get("menu.file.save"));
        itemSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        itemSave.setOnAction(e -> fileManager.saveFile());

        MenuItem itemSaveAs = new MenuItem(i18n.get("menu.file.saveas"));
        itemSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        itemSaveAs.setOnAction(e -> fileManager.saveFileAs());

        MenuItem itemExit = new MenuItem(i18n.get("menu.file.exit"));
        itemExit.setOnAction(e -> {
            if (fileManager.checkUnsavedChanges()) {
                WindowManager.getInstance().closeEmulator();
                Platform.exit();
            }
        });

        MenuItem itemNewProject = new MenuItem(i18n.get("menu.file.new_project"));
        itemNewProject.setOnAction(e -> openProjectAction());
        MenuItem itemOpenProject = new MenuItem(i18n.get("menu.file.open_project"));
        itemOpenProject.setOnAction(e -> openProjectAction());
        MenuItem itemCloseProject = new MenuItem("Cerrar Proyecto");
        itemCloseProject.setOnAction(e -> closeProjectAction());

        menuFile.getItems().addAll(itemNew, itemOpen, itemSave, itemSaveAs, new SeparatorMenuItem(), itemNewProject, itemOpenProject, itemCloseProject, new SeparatorMenuItem(), itemExit);

        Menu menuEdit = new Menu(i18n.get("menu.edit"));
        MenuItem itemUndo = new MenuItem(i18n.get("menu.edit.undo"));
        itemUndo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN));
        itemUndo.setOnAction(e -> codeEditor.undo());

        MenuItem itemRedo = new MenuItem(i18n.get("menu.edit.redo"));
        itemRedo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        itemRedo.setOnAction(e -> codeEditor.redo());

        MenuItem itemCut = new MenuItem(i18n.get("menu.edit.cut"));
        itemCut.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN));
        itemCut.setOnAction(e -> codeEditor.cut());

        MenuItem itemCopy = new MenuItem(i18n.get("menu.edit.copy"));
        itemCopy.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        itemCopy.setOnAction(e -> codeEditor.copy());

        MenuItem itemPaste = new MenuItem(i18n.get("menu.edit.paste"));
        itemPaste.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN));
        itemPaste.setOnAction(e -> codeEditor.paste());

        Menu menuFind = new Menu("Buscar");
        MenuItem itemFind = new MenuItem("Buscar...");
        itemFind.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN));
        itemFind.setOnAction(e -> new com.lazyzxsoftware.zxspectrumide.ui.FindReplaceDialog(codeEditor).show(false));
        MenuItem itemReplace = new MenuItem("Reemplazar...");
        itemReplace.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));
        itemReplace.setOnAction(e -> new com.lazyzxsoftware.zxspectrumide.ui.FindReplaceDialog(codeEditor).show(true));
        menuFind.getItems().addAll(itemFind, itemReplace);

        MenuItem itemSelectAll = new MenuItem(i18n.get("menu.edit.select_all"));
        itemSelectAll.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.SHORTCUT_DOWN));
        itemSelectAll.setOnAction(e -> codeEditor.selectAll());

        menuEdit.getItems().addAll(itemUndo, itemRedo, new SeparatorMenuItem(), itemCut, itemCopy, itemPaste, new SeparatorMenuItem(), menuFind, new SeparatorMenuItem(), itemSelectAll);

        Menu menuView = new Menu(i18n.get("menu.view"));
        MenuItem itemToggleSidebar = new MenuItem("Alternar Explorador de Archivos");
        itemToggleSidebar.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN));
        itemToggleSidebar.setOnAction(e -> toggleSidebar());
        menuView.getItems().add(itemToggleSidebar);

        Menu menuTools = new Menu(i18n.get("menu.tools"));
        MenuItem itemCompile = new MenuItem("Compilar (Pasmo)");
        itemCompile.setAccelerator(new KeyCodeCombination(KeyCode.F7));
        itemCompile.setOnAction(e -> compileCode());
        menuTools.getItems().add(itemCompile);

        MenuItem itemRun = new MenuItem("Compilar y Ejecutar");
        itemRun.setAccelerator(new KeyCodeCombination(KeyCode.F5));
        itemRun.setOnAction(e -> compileAndRunCode());
        menuTools.getItems().add(itemRun);

        MenuItem spriteEditor = new MenuItem("Editor de Sprites");
        MenuItem mapEditor = new MenuItem("Editor de mapas");
        MenuItem soundEditor = new MenuItem("Editor de sonido");
        menuTools.getItems().addAll(new SeparatorMenuItem(), spriteEditor, mapEditor, soundEditor);

        Menu menuWindows = new Menu("Ventanas");
        MenuItem itemEmulator = new MenuItem("Pantalla Emulador");
        itemEmulator.setAccelerator(new KeyCodeCombination(KeyCode.F6));
        itemEmulator.setOnAction(e -> WindowManager.getInstance().showEmulator());

        Menu menuDebug = new Menu("Debug");
        MenuItem itemMainDebug = new MenuItem("Debugger Principal");
        itemMainDebug.setAccelerator(new KeyCodeCombination(KeyCode.F5, KeyCombination.SHIFT_DOWN));
        itemMainDebug.setOnAction(e -> DebugWindowManager.getInstance().showMainDebugger());
        MenuItem itemMemory = new MenuItem("Visor de Memoria");
        itemMemory.setOnAction(e -> DebugWindowManager.getInstance().showMemory());
        MenuItem itemRegisters = new MenuItem("Registros CPU");
        itemRegisters.setOnAction(e -> DebugWindowManager.getInstance().showRegisters());
        MenuItem itemStack = new MenuItem("Call Stack");
        itemStack.setOnAction(e -> DebugWindowManager.getInstance().showStack());
        MenuItem itemBreakpoints = new MenuItem("Breakpoints");
        itemBreakpoints.setOnAction(e -> DebugWindowManager.getInstance().showBreakpoints());
        MenuItem itemWatchers = new MenuItem("Watchers (Variables)");
        itemWatchers.setOnAction(e -> DebugWindowManager.getInstance().showWatchers());

        menuDebug.getItems().addAll(itemMainDebug, new SeparatorMenuItem(), itemRegisters, itemMemory, itemStack, itemBreakpoints, itemWatchers);
        menuWindows.getItems().addAll(itemEmulator, new SeparatorMenuItem(), menuDebug);

        Menu debugMenu = new Menu("Depurador");
        MenuItem testZ80Item = new MenuItem("🧪 Test Z80 (Esqueleto)");
        testZ80Item.setOnAction(e -> {
            new Z80TestWindow().show();
        });
        debugMenu.getItems().add(testZ80Item);

        MenuItem itemLog = new MenuItem("Ver Historial de Ejecución (Trace Log)");
        itemLog.setOnAction(e -> DebugWindowManager.getInstance().showTraceLog());
        debugMenu.getItems().add(itemLog);

        Menu menuSettings = new Menu(i18n.get("menu.settings"));
        MenuItem itemPreferences = new MenuItem(i18n.get("menu.settings.preferences"));
        itemPreferences.setOnAction(e -> SettingsDialog.show(primaryStage.getScene()));
        MenuItem itemTheme = new MenuItem("Cambiar Tema");
        itemTheme.setOnAction(e -> ThemeManager.getInstance().toggleTheme());
        menuSettings.getItems().addAll(itemPreferences, itemTheme);

        Menu menuHelp = new Menu(i18n.get("menu.help"));
        MenuItem itemAbout = new MenuItem(i18n.get("menu.help.about"));
        menuHelp.getItems().add(itemAbout);

        menuBar.getMenus().addAll(menuFile, menuEdit, menuView, debugMenu, menuTools, menuWindows, menuSettings, menuHelp);

        return menuBar;
    }

    private void compileAndRunCode() {
        if (!fileManager.ensureSavedForCompilation()) return;

        outputConsole.clear();
        outputConsole.logSuccess("🚀 Iniciando Compilación y Ejecución...");

        String sourceCode = codeEditor.getText();
        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            outputConsole.logError("No hay código para compilar.");
            return;
        }

        File currentFile = fileManager.getCurrentFile();
        String fileName = "NONAME.asm";
        File buildDir;

        if (currentFile != null) {
            fileName = currentFile.getName();
            buildDir = new File(currentFile.getParentFile(), "build");
        } else {
            buildDir = new File("build");
        }

        if (!buildDir.exists()) buildDir.mkdirs();

        // --- LÓGICA DE NOMBRE IDENTICA A PASMOCOMPILER ---
        String baseName = fileName.replaceFirst("[.][^.]+$", "");
        String tapName = baseName.toUpperCase().replace(" ", "").replace("_", "");
        if (tapName.length() > 8) tapName = tapName.substring(0, 8);
        if (tapName.isEmpty()) tapName = "NONAME";
        tapName += ".tap";
        // ------------------------------------------------

        final String finalSourceFileName = fileName;
        final String finalTapFileName = tapName;
        final File finalBuildDir = buildDir;

        // --- DEBUG EXTRA ---
        outputConsole.println("DEBUG: Archivo fuente: " + finalSourceFileName);
        outputConsole.println("DEBUG: Archivo TAP destino: " + finalTapFileName);

        try {
            File sourceFile = new File(finalBuildDir, finalSourceFileName);
            java.nio.file.Files.writeString(sourceFile.toPath(), sourceCode);

            com.lazyzxsoftware.zxspectrumide.compiler.PasmoCompiler compiler = new com.lazyzxsoftware.zxspectrumide.compiler.PasmoCompiler();
            Process process = compiler.compile(sourceFile, finalBuildDir);

            String output = new String(process.getInputStream().readAllBytes());
            String error = new String(process.getErrorStream().readAllBytes());
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                File tapFile = new File(finalBuildDir, finalTapFileName);

                String symbolsName = finalSourceFileName.replaceFirst("[.][^.]+$", "") + ".symbols";
                File symbolsFile = new File(finalBuildDir, symbolsName);
                if (symbolsFile.exists()) {
                    try {
                        String symContent = java.nio.file.Files.readString(symbolsFile.toPath());
                        outputConsole.showSymbols(symContent);
                    } catch (Exception e) {
                        outputConsole.logError("No se pudo leer .symbols");
                    }
                }

                if (tapFile.exists()) {
                    outputConsole.logSuccess("✅ Compilación OK.");
                    if (!output.trim().isEmpty()) outputConsole.println(output);

                    // --- MENSAJE CONFIRMACIÓN DE CARGA ---
                    outputConsole.logSuccess("📂 Cargando fichero: " + tapFile.getAbsolutePath() + " (" + tapFile.length() + " bytes)");
                    // -------------------------------------

                    byte[] binaryData = java.nio.file.Files.readAllBytes(tapFile.toPath());
                    Platform.runLater(() -> {
                        WindowManager.getInstance().showEmulator();
                        var emulator = WindowManager.getInstance().getEmulator();
                        if (emulator != null) {
                            // CAMBIO: Pasar el OBJETO FILE, no los bytes
                            emulator.loadProgram(tapFile);
                        }
                    });
                } else {
                    outputConsole.logError("Pasmo terminó bien, pero no se generó: " + finalTapFileName);
                    outputConsole.logError("Verifique la lógica de nombres.");
                }
            } else {
                outputConsole.logError("❌ Fallo de compilación:");
                outputConsole.println(error);
                outputConsole.println(output);
            }

        } catch (Exception e) {
            e.printStackTrace();
            outputConsole.logError("Error Crítico del IDE: " + e.getMessage());
        }
    }

    private void compileCode() {
        if (!fileManager.ensureSavedForCompilation()) return;

        outputConsole.clear();
        outputConsole.logSuccess("Iniciando compilación...");

        String sourceCode = codeEditor.getText();
        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            showAlert("Aviso", "No hay código para compilar.");
            return;
        }

        File currentFile = fileManager.getCurrentFile();
        File buildDir;
        String fileName = "NONAME.asm";

        if (currentFile != null) {
            fileName = currentFile.getName();
            buildDir = new File(currentFile.getParentFile(), "build");
        } else {
            buildDir = new File("build");
        }

        if (!buildDir.exists()) buildDir.mkdirs();

        String baseName = fileName.replaceFirst("[.][^.]+$", "");
        String tapName = baseName.toUpperCase().replace(" ", "").replace("_", "");
        if (tapName.length() > 8) tapName = tapName.substring(0, 8);
        if (tapName.isEmpty()) tapName = "NONAME";
        tapName += ".tap";

        System.out.println("🔨 Compilando " + fileName + " en " + buildDir.getAbsolutePath());

        final String finalSourceFileName = fileName;
        final String finalTapFileName = tapName;
        final File finalBuildDir = buildDir;

        new Thread(() -> {
            try {
                File sourceFile = new File(finalBuildDir, finalSourceFileName);
                java.nio.file.Files.writeString(sourceFile.toPath(), sourceCode);

                com.lazyzxsoftware.zxspectrumide.compiler.PasmoCompiler compiler = new com.lazyzxsoftware.zxspectrumide.compiler.PasmoCompiler();
                Process process = compiler.compile(sourceFile, finalBuildDir);

                String output = new String(process.getInputStream().readAllBytes());
                String error = new String(process.getErrorStream().readAllBytes());
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    outputConsole.logSuccess("✅ Compilación OK: " + finalTapFileName);

                    if (!output.trim().isEmpty()) {
                        outputConsole.println(output);
                    }

                    String symbolsName = baseName + ".symbols";
                    File symbolsFile = new File(finalBuildDir, symbolsName);

                    if (symbolsFile.exists()) {
                        try {
                            String symContent = java.nio.file.Files.readString(symbolsFile.toPath());
                            outputConsole.showSymbols(symContent);
                        } catch (Exception e) {
                            outputConsole.logError("No se pudo leer .symbols: " + e.getMessage());
                        }
                    } else {
                        outputConsole.logError("Advertencia: No se generó archivo de símbolos.");
                    }
                } else {
                    outputConsole.logError("Error de compilación:");
                    outputConsole.println(error);
                    outputConsole.println(output);
                }

            } catch (Exception e) {
                e.printStackTrace();
                outputConsole.logError("Error crítico del sistema: " + e.getMessage());
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}