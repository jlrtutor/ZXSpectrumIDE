package com.lazyzxsoftware.zxspectrumide;

import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import com.lazyzxsoftware.zxspectrumide.editor.CodeEditor;
import com.lazyzxsoftware.zxspectrumide.editor.FileManager;
import com.lazyzxsoftware.zxspectrumide.i18n.I18nManager;
import com.lazyzxsoftware.zxspectrumide.managers.WindowManager;
import com.lazyzxsoftware.zxspectrumide.theme.ThemeManager;
import com.lazyzxsoftware.zxspectrumide.ui.SettingsDialog;
import com.lazyzxsoftware.zxspectrumide.utils.SplashScreen;
import com.lazyzxsoftware.zxspectrumide.utils.PlatformUtils;
import com.lazyzxsoftware.zxspectrumide.ui.windows.DebugWindowManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

import java.awt.*;
import java.io.File;
import java.util.Objects;
import java.util.ResourceBundle;

public class Main extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private CodeEditor codeEditor;
    private FileManager fileManager;
    private I18nManager i18n;

    @Override
    public void start(Stage primaryStage) {
        // Para desarrollo rápido, iniciamos directo:
        initMainStage(primaryStage);
    }

    private void initMainStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.i18n = I18nManager.getInstance();

        // Configuración básica de la ventana
        primaryStage.setTitle(i18n.get("app.title") + " (Develop)");

        PlatformUtils.setAppIcon(primaryStage, "/com/lazyzxsoftware/zxspectrumide/icons/app_icon.png");

        // Inicializar componentes principales
        codeEditor = new CodeEditor();
        // fileManager necesita el CodeArea interno, no el editor completo
        fileManager = new FileManager(primaryStage, codeEditor);

        initRootLayout();

        // Aplicar tema guardado
        ThemeManager.getInstance().registerScene(primaryStage.getScene());

        // Manejo de cierre de la aplicación
        primaryStage.setOnCloseRequest(event -> {
            if (fileManager.checkUnsavedChanges()) {
                // Cerrar también el emulador al salir
                WindowManager.getInstance().closeEmulator();

                Platform.exit();
                System.exit(0);
            } else {
                event.consume(); // Cancelar cierre si el usuario cancela guardar
            }
        });

        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void initRootLayout() {
        rootLayout = new BorderPane();

        // Barra de Menú
        MenuBar menuBar = createMenuBar();
        rootLayout.setTop(menuBar);

        // --- CORRECCIÓN AQUÍ ---
        // Antes: rootLayout.setCenter(codeEditor.getView());
        // Ahora: Usamos codeEditor directamente porque CodeEditor extends BorderPane
        rootLayout.setCenter(codeEditor);

        // Crear escena
        Scene scene = new Scene(rootLayout, 1024, 768);
        primaryStage.setScene(scene);
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // --- 1. MENÚ ARCHIVO ---
        Menu menuFile = new Menu(i18n.get("menu.file"));

        MenuItem itemNew = new MenuItem(i18n.get("menu.file.new"));
        itemNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        itemNew.setOnAction(e -> fileManager.newFile());

        MenuItem itemOpen = new MenuItem(i18n.get("menu.file.open"));
        itemOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        itemOpen.setOnAction(e -> fileManager.openFile());

        MenuItem itemSave = new MenuItem(i18n.get("menu.file.save"));
        itemSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        itemSave.setOnAction(e -> fileManager.saveFile());

        MenuItem itemSaveAs = new MenuItem(i18n.get("menu.file.saveas"));
        itemSaveAs.setOnAction(e -> fileManager.saveFileAs());

        MenuItem itemExit = new MenuItem(i18n.get("menu.file.exit"));
        itemExit.setOnAction(e -> {
            if (fileManager.checkUnsavedChanges()) {
                WindowManager.getInstance().closeEmulator();
                Platform.exit();
            }
        });

        menuFile.getItems().addAll(itemNew, itemOpen, new SeparatorMenuItem(), itemSave, itemSaveAs, new SeparatorMenuItem(), itemExit);

        // --- 2. MENÚ EDITAR ---
        Menu menuEdit = new Menu(i18n.get("menu.edit"));

        // --- 3. MENÚ HERRAMIENTAS ---
        Menu menuTools = new Menu(i18n.get("menu.tools"));
        MenuItem itemCompile = new MenuItem("Compilar (Pasmo)");
        itemCompile.setAccelerator(new KeyCodeCombination(KeyCode.F7)); // Sugerencia: F7 para solo compilar

        itemCompile.setOnAction(e -> compileCode());
        menuTools.getItems().add(itemCompile);

        MenuItem itemRun = new MenuItem("Compilar y Ejecutar");
        itemRun.setAccelerator(new KeyCodeCombination(KeyCode.F5)); // Atajo F5
        itemRun.setOnAction(e -> runCode());
        menuTools.getItems().add(itemRun);

        // --- 4. MENÚ VENTANAS ---
        Menu menuWindows = new Menu("Ventanas");

        // 4.1 Item existente: Pantalla Emulador
        MenuItem itemEmulator = new MenuItem("Pantalla Emulador");
        itemEmulator.setAccelerator(new KeyCodeCombination(KeyCode.F6));
        itemEmulator.setOnAction(e -> WindowManager.getInstance().showEmulator());

        // 4.2 Nuevo Submenú: DEBUG
        Menu menuDebug = new Menu("Debug");

        // Ventana Principal (Desensamblado y controles)
        MenuItem itemMainDebug = new MenuItem("Debugger Principal");
        itemMainDebug.setAccelerator(new KeyCodeCombination(KeyCode.F5));
        itemMainDebug.setOnAction(e -> DebugWindowManager.getInstance().showMainDebugger());

        // Visor de Memoria
        MenuItem itemMemory = new MenuItem("Visor de Memoria");
        itemMemory.setOnAction(e -> DebugWindowManager.getInstance().showMemory());

        // Registros CPU
        MenuItem itemRegisters = new MenuItem("Registros CPU");
        itemRegisters.setOnAction(e -> DebugWindowManager.getInstance().showRegisters());

        // Pila (Stack)
        MenuItem itemStack = new MenuItem("Call Stack");
        itemStack.setOnAction(e -> DebugWindowManager.getInstance().showStack());

        // Breakpoints
        MenuItem itemBreakpoints = new MenuItem("Breakpoints");
        itemBreakpoints.setOnAction(e -> DebugWindowManager.getInstance().showBreakpoints());

        // Variables / Watchers
        MenuItem itemWatchers = new MenuItem("Watchers (Variables)");
        itemWatchers.setOnAction(e -> DebugWindowManager.getInstance().showWatchers());

        // Añadir items al submenú Debug
        menuDebug.getItems().addAll(
                itemMainDebug,
                new SeparatorMenuItem(),
                itemRegisters,
                itemMemory,
                itemStack,
                itemBreakpoints,
                itemWatchers
        );

        // Añadir todo al menú Ventanas
        menuWindows.getItems().addAll(itemEmulator, new SeparatorMenuItem(), menuDebug);

        // --- 5. MENÚ CONFIGURACIÓN ---
        Menu menuSettings = new Menu(i18n.get("menu.settings"));
        MenuItem itemPreferences = new MenuItem(i18n.get("menu.settings.preferences"));
        // Llamada estática directa, pasando la escena o el stage según pida tu método show
        itemPreferences.setOnAction(e -> SettingsDialog.show(primaryStage.getScene()));

        MenuItem itemTheme = new MenuItem("Cambiar Tema");
        itemTheme.setOnAction(e -> ThemeManager.getInstance().toggleTheme());

        menuSettings.getItems().addAll(itemPreferences, itemTheme);

        // --- 6. MENÚ AYUDA ---
        Menu menuHelp = new Menu(i18n.get("menu.help"));
        MenuItem itemAbout = new MenuItem(i18n.get("menu.help.about"));
        menuHelp.getItems().add(itemAbout);

        menuBar.getMenus().addAll(menuFile, menuEdit, menuTools, menuWindows, menuSettings, menuHelp);

        return menuBar;
    }

    private void runCode() {
        String sourceCode = codeEditor.getText();
        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            showAlert("Aviso", "No hay código para compilar.");
            return;
        }

        // 1. Detectar archivo y Directorio de compilación
        File currentFile = fileManager.getCurrentFile();
        String fileName = "NONAME.asm";
        File buildDir;

        if (currentFile != null) {
            fileName = currentFile.getName();
            // Creamos la carpeta 'build' JUNTO al archivo fuente
            buildDir = new File(currentFile.getParentFile(), "build");
        } else {
            // Fallback: Si no está guardado, usar carpeta build en la raíz del proyecto
            buildDir = new File("build");
        }

        // Crear directorio si no existe
        if (!buildDir.exists()) {
            buildDir.mkdirs();
        }

        // 2. Calcular nombre TAP (MSDOS 8.3)
        String baseName = fileName.replaceFirst("[.][^.]+$", "");
        String tapName = baseName.toUpperCase().replace(" ", "").replace("_", "");
        if (tapName.length() > 8) tapName = tapName.substring(0, 8);
        if (tapName.isEmpty()) tapName = "NONAME";
        tapName += ".tap";

        System.out.println("🚀 Compilando " + fileName + " en " + buildDir.getAbsolutePath());

        // Variables finales para el Thread
        final String finalSourceFileName = fileName;
        final String finalTapFileName = tapName;
        final File finalBuildDir = buildDir;

        new Thread(() -> {
            try {
                // Guardar código fuente temporal en la carpeta de destino
                File sourceFile = new File(finalBuildDir, finalSourceFileName);
                java.nio.file.Files.writeString(sourceFile.toPath(), sourceCode);

                // Instanciar compilador
                com.lazyzxsoftware.zxspectrumide.compiler.PasmoCompiler compiler = new com.lazyzxsoftware.zxspectrumide.compiler.PasmoCompiler();

                // Compilar en el directorio correcto
                Process process = compiler.compile(sourceFile, finalBuildDir);

                String output = new String(process.getInputStream().readAllBytes());
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    File tapFile = new File(finalBuildDir, finalTapFileName);

                    if (tapFile.exists()) {
                        byte[] binaryData = java.nio.file.Files.readAllBytes(tapFile.toPath());
                        System.out.println("✅ Compilado OK: " + tapFile.getAbsolutePath());

                        Platform.runLater(() -> {
                            WindowManager.getInstance().showEmulator();
                            var webView = WindowManager.getInstance().getEmulatorWebView();
                            if (webView != null) {
                                webView.loadProgram(binaryData, finalTapFileName);
                            }
                        });
                    } else {
                        Platform.runLater(() -> showAlert("Error Interno", "No se generó el archivo: " + finalTapFileName));
                    }
                } else {
                    Platform.runLater(() -> showAlert("Error de Compilación", output));
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error Crítico", e.getMessage()));
            }
        }).start();
    }

    // Helper para alertas rápidas (si no lo tienes ya)
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void compileCode() {
        String sourceCode = codeEditor.getText();
        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            showAlert("Aviso", "No hay código para compilar.");
            return;
        }

        // 1. Detectar archivo y determinar carpeta 'build' relativa
        java.io.File currentFile = fileManager.getCurrentFile();
        java.io.File buildDir;
        String fileName = "NONAME.asm";

        if (currentFile != null) {
            fileName = currentFile.getName();
            // AQUÍ ESTÁ LA CLAVE: Carpeta build al lado del archivo .asm
            buildDir = new java.io.File(currentFile.getParentFile(), "build");
        } else {
            // Si el archivo no se ha guardado nunca, usamos la raíz del proyecto por defecto
            buildDir = new java.io.File("build");
        }

        if (!buildDir.exists()) {
            buildDir.mkdirs();
        }

        // 2. Calcular nombre TAP (MSDOS 8.3)
        // Quitamos extensión, pasamos a mayúsculas, quitamos espacios y cortamos a 8 chars
        String baseName = fileName.replaceFirst("[.][^.]+$", "");
        String tapName = baseName.toUpperCase().replace(" ", "").replace("_", "");
        if (tapName.length() > 8) tapName = tapName.substring(0, 8);
        if (tapName.isEmpty()) tapName = "NONAME";
        tapName += ".tap";

        System.out.println("🔨 Compilando " + fileName + " en " + buildDir.getAbsolutePath());

        // Variables finales para el Thread
        final String finalSourceFileName = fileName;
        final String finalTapFileName = tapName;
        final java.io.File finalBuildDir = buildDir;

        new Thread(() -> {
            try {
                // Guardar una copia del código fuente en la carpeta build (opcional, pero útil)
                java.io.File sourceFile = new java.io.File(finalBuildDir, finalSourceFileName);
                java.nio.file.Files.writeString(sourceFile.toPath(), sourceCode);

                // Instanciar compilador
                com.lazyzxsoftware.zxspectrumide.compiler.PasmoCompiler compiler = new com.lazyzxsoftware.zxspectrumide.compiler.PasmoCompiler();

                // Compilar
                Process process = compiler.compile(sourceFile, finalBuildDir);

                String output = new String(process.getInputStream().readAllBytes());
                String error = new String(process.getErrorStream().readAllBytes());
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    java.io.File tapFile = new java.io.File(finalBuildDir, finalTapFileName);

                    Platform.runLater(() -> {
                        if (tapFile.exists()) {
                            // Mensaje de éxito en consola o barra de estado
                            System.out.println("✅ Generado correctamente: " + tapFile.getAbsolutePath());
                            // Opcional: showAlert("Éxito", "Compilado en: " + tapFile.getName());
                        } else {
                            showAlert("Error Interno", "Pasmo terminó bien pero no aparece el archivo: " + finalTapFileName);
                        }
                    });
                } else {
                    Platform.runLater(() -> showAlert("Error de Compilación", error + "\n" + output));
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error Crítico", e.getMessage()));
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}