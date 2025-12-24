package com.lazyzxsoftware.zxspectrumide;

import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import com.lazyzxsoftware.zxspectrumide.editor.CodeEditor;
import com.lazyzxsoftware.zxspectrumide.editor.FileManager;
import com.lazyzxsoftware.zxspectrumide.i18n.I18nManager;
import com.lazyzxsoftware.zxspectrumide.theme.ThemeManager;
import com.lazyzxsoftware.zxspectrumide.ui.SettingsDialog;
import com.lazyzxsoftware.zxspectrumide.ui.DebugPanel;
import com.lazyzxsoftware.zxspectrumide.utils.SplashScreen;
import com.lazyzxsoftware.zxspectrumide.compiler.PasmoCompiler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    private Stage primaryStage;
    private ThemeManager themeManager;
    private ConfigManager configManager;
    private I18nManager i18nManager;
    private Label statusLabel;
    private TextArea consoleArea;
    private TabPane tabPane;
    private FileManager fileManager;
    private HBox statusBarBox;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // 1. Mostrar Splash Screen
        SplashScreen splash = new SplashScreen();
        splash.show();

        // --- CÓDIGO NUEVO PARA EL ICONO ---
        loadAppIcon(primaryStage);
        // ----------------------------------

        // 2. Tarea de carga en segundo plano (Hilo separado)
        new Thread(() -> {
            try {
                long delay = 200;
                Platform.runLater(() -> {
                    splash.updateStatus("Iniciando núcleo del sistema...");
                    splash.updateProgress(0.1);
                });
                Thread.sleep(delay);

                // PASO 2: Configuración (25%)
                configManager = ConfigManager.getInstance();
                Platform.runLater(() -> {
                    splash.updateStatus("Cargando preferencias de usuario...");
                    splash.updateProgress(0.25);
                });
                Thread.sleep(delay);

                // PASO 3: Recursos Visuales (50%)
                loadCustomFont("RobotoMono-Regular.ttf");
                loadCustomFont("RobotoMono-Bold.ttf");
                loadCustomFont("RobotoMono-Italic.ttf");
                themeManager = ThemeManager.getInstance();

                Platform.runLater(() -> {
                    splash.updateStatus("Aplicando temas y estilos...");
                    splash.updateProgress(0.50);
                });
                Thread.sleep(delay);

                // PASO 4: Idioma y Sistema (75%)
                i18nManager = I18nManager.getInstance();
                fileManager = new FileManager(primaryStage);

                Platform.runLater(() -> {
                    splash.updateStatus("Cargando traducciones y sistema de archivos...");
                    splash.updateProgress(0.75);
                });
                Thread.sleep(delay);

                // PASO 5: Finalización (100%)
                Platform.runLater(() -> {
                    splash.updateStatus("Listo. Lanzando interfaz...");
                    splash.updateProgress(1.0);
                });
                Thread.sleep(delay); // Una última pausa para ver la barra llena

                // LANZAR LA UI PRINCIPAL
                Platform.runLater(() -> {
                    try {
                        initializeMainUI(splash);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> splash.close(null));
            }
        }).start();
    }

    private void loadAppIcon(Stage stage) {
        try {
            // CORRECCIÓN: Usar la ruta real de tu estructura de paquetes
            String iconPath = "/com/lazyzxsoftware/zxspectrumide/icons/app_icon.png";

            // 1. Cargar para la ventana (JavaFX)
            var iconStream = getClass().getResourceAsStream(iconPath);
            if (iconStream != null) {
                Image fxImage = new Image(iconStream);
                stage.getIcons().add(fxImage);
            } else {
                System.err.println("⚠️ No se encontró el icono en: " + iconPath);
            }

            // 2. Cargar para el DOCK de macOS (AWT)
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                java.net.URL iconURL = getClass().getResource(iconPath);

                if (iconURL != null) {
                    // Inicializar Toolkit de AWT (necesario a veces para que Taskbar despierte)
                    java.awt.Toolkit.getDefaultToolkit();

                    java.awt.Image awtImage = java.awt.Toolkit.getDefaultToolkit().getImage(iconURL);
                    try {
                        java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
                        if (taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                            taskbar.setIconImage(awtImage);
                            System.out.println("✅ Icono del Dock establecido correctamente.");
                        }
                    } catch (UnsupportedOperationException e) {
                        System.err.println("La API Taskbar no está soportada en este sistema.");
                    } catch (SecurityException e) {
                        System.err.println("Error de seguridad al acceder a Taskbar.");
                    }
                } else {
                    System.err.println("⚠️ URL del icono es NULL para AWT.");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error fatal cargando icono: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeMainUI(SplashScreen splash) {
        splash.updateStatus("Construyendo interfaz gráfica...");

        primaryStage.setTitle(i18nManager.get("app.title"));

        try {
            // Intenta cargar el icono. Asegúrate de que el nombre coincida (png o svg)
            String iconPath = "/com/lazyzxsoftware/zxspectrumide/icons/app_icon.png";
            var iconStream = getClass().getResourceAsStream(iconPath);
            if (iconStream != null) {
                primaryStage.getIcons().add(new javafx.scene.image.Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono de la aplicación: " + e.getMessage());
        }

        // 1. Crear el contenedor raíz
        BorderPane root = new BorderPane();

        // 2. Crear y añadir el MENÚ SUPERIOR (¡Crucial para que se vea!)
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        // 3. Sistema de pestañas (Editor)
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            updateWindowTitle();
        });
        createNewTab(null); // Pestaña inicial

        // 4. Área de Consola (Mantenemos tu configuración actual)
        consoleArea = new TextArea();
        consoleArea.setEditable(false);
        consoleArea.setWrapText(true);
        consoleArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");
        consoleArea.setPromptText("Salida del compilador...");

        // --- NUEVO: SISTEMA DE PESTAÑAS INFERIOR ---
        TabPane bottomTabPane = new TabPane();
        bottomTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); // No permitir cerrar

        // Pestaña 1: Consola (Salida estándar)
        Tab consoleTab = new Tab("Terminal / Salida");
        consoleTab.setContent(consoleArea);

        // Pestaña 2: Depurador (Tu nuevo panel visual)
        Tab debugTab = new Tab("Debugger (ZEsarUX)");
        DebugPanel debugPanel = new DebugPanel(); // Instanciamos tu clase
        debugTab.setContent(debugPanel);

        // Añadimos las pestañas
        bottomTabPane.getTabs().addAll(consoleTab, debugTab);
        // --------------------------------------------

        // 5. Barra de Estado (Igual que antes)
        if (statusBarBox == null) createStatusBar();

        // 6. SplitPane (Divide Editor y EL NUEVO PANEL DE PESTAÑAS)
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);

        // CAMBIO: En vez de consoleArea, metemos bottomTabPane
        splitPane.getItems().addAll(tabPane, bottomTabPane);
        splitPane.setDividerPositions(0.75); // 75% para editor, 25% para herramientas

        // 7. Montar el layout principal
        root.setCenter(splitPane);      // Centro: Editor + Consola
        root.setBottom(statusBarBox);   // Abajo: Barra de estado fija

        // 8. Crear la escena con TAMAÑO EXPLÍCITO (Soluciona ventana pequeña)
        Scene scene = new Scene(root, 1200, 800);

        // 9. Configuración final del Stage
        primaryStage.setScene(scene);
        themeManager.registerScene(scene); // Aplicar tema

        updateStatus(i18nManager.get("app.ready"));
        primaryStage.setOnCloseRequest(e -> handleAppClose());

        // 10. Mostrar ventana principal y cerrar splash
        primaryStage.show();
        splash.close(null);

        // Actualizar título final (por si hay archivo abierto o modificado)
        updateWindowTitle();
    }

    private void createNewTab(File file) {
        CodeEditor editor = new CodeEditor();
        String title = (file != null) ? file.getName() : i18nManager.get("file.untitled");
        Tab tab = new Tab(title);

        // Asignar el editor al contenido de la pestaña
        tab.setContent(editor);

        // Listener para el asterisco de modificado
        editor.setOnModifiedChanged(() -> {
            String currentTitle = (editor.getCurrentFile() != null)
                    ? editor.getCurrentFile().getName()
                    : i18nManager.get("file.untitled");

            if (editor.isModified()) {
                tab.setText(currentTitle + "*");
            } else {
                tab.setText(currentTitle);
            }
            updateWindowTitle(); // Actualizar título global también
        });

        // Gestión del cierre de la pestaña individual
        tab.setOnCloseRequest(e -> {
            // Si hay cambios sin guardar, preguntamos. Si cancela, consumimos el evento (no cierra).
            if (!checkUnsavedChanges(editor)) {
                e.consume();
            }
        });

        // Si es un archivo existente, lo abrimos
        if (file != null) {
            try {
                editor.openFile(file);
                tab.setText(file.getName()); // Resetear nombre tras carga
            } catch (Exception ex) {
                showAlert(i18nManager.get("file.error.open"), ex.getMessage());
                return; // No añadir pestaña si falla
            }
        }

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        editor.getCodeArea().requestFocus();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // ============================================
        // MENÚ ARCHIVO
        // ============================================
        Menu menuFile = new Menu(i18nManager.get("menu.file"));

        MenuItem newFile = new MenuItem(i18nManager.get("menu.file.new"));
        newFile.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
        newFile.setOnAction(e -> newFile());

        MenuItem openFile = new MenuItem(i18nManager.get("menu.file.open"));
        openFile.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        openFile.setOnAction(e -> openFile());

        MenuItem saveFile = new MenuItem(i18nManager.get("menu.file.save"));
        saveFile.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        saveFile.setOnAction(e -> saveFile());

        MenuItem saveAsFile = new MenuItem(i18nManager.get("menu.file.save_as"));
        saveAsFile.setAccelerator(new KeyCodeCombination(KeyCode.S,
                KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        saveAsFile.setOnAction(e -> saveFileAs());

        MenuItem exit = new MenuItem(i18nManager.get("menu.file.exit"));
        exit.setOnAction(e -> handleAppClose());

        menuFile.getItems().addAll(
                newFile,
                openFile,
                new SeparatorMenuItem(),
                saveFile,
                saveAsFile,
                new SeparatorMenuItem(),
                exit
        );

        // ============================================
        // MENÚ EDITAR (Actualizado para pestañas)
        // ============================================
        Menu menuEdit = new Menu(i18nManager.get("menu.edit"));

        MenuItem undo = new MenuItem(i18nManager.get("menu.edit.undo"));
        undo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN));
        undo.setOnAction(e -> {
            CodeEditor editor = getCurrentEditor(); // <--- USAR ESTO
            if (editor != null && editor.canUndo()) {
                editor.undo();
                updateStatus(i18nManager.get("menu.edit.undo"));
            }
        });

        MenuItem redo = new MenuItem(i18nManager.get("menu.edit.redo"));
        redo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        redo.setOnAction(e -> {
            CodeEditor editor = getCurrentEditor(); // <--- USAR ESTO
            if (editor != null && editor.canRedo()) {
                editor.redo();
                updateStatus(i18nManager.get("menu.edit.redo"));
            }
        });

        MenuItem cut = new MenuItem(i18nManager.get("menu.edit.cut"));
        cut.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN));
        cut.setOnAction(e -> {
            CodeEditor editor = getCurrentEditor(); // <--- USAR ESTO
            if (editor != null) {
                editor.cut();
            }
        });

        MenuItem copy = new MenuItem(i18nManager.get("menu.edit.copy"));
        copy.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        copy.setOnAction(e -> {
            CodeEditor editor = getCurrentEditor(); // <--- USAR ESTO
            if (editor != null) {
                editor.copy();
            }
        });

        MenuItem paste = new MenuItem(i18nManager.get("menu.edit.paste"));
        paste.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN));
        paste.setOnAction(e -> {
            CodeEditor editor = getCurrentEditor(); // <--- USAR ESTO
            if (editor != null) {
                editor.paste();
            }
        });

        MenuItem find = new MenuItem(i18nManager.get("menu.edit.find"));
        find.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN));

        MenuItem replace = new MenuItem(i18nManager.get("menu.edit.replace"));
        replace.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.SHORTCUT_DOWN));

        menuEdit.getItems().addAll(
                undo,
                redo,
                new SeparatorMenuItem(),
                cut,
                copy,
                paste,
                new SeparatorMenuItem(),
                find,
                replace
        );

        // ============================================
        // MENÚ VER
        // ============================================
        Menu menuView = new Menu(i18nManager.get("menu.view"));

        Menu themeMenu = new Menu(i18nManager.get("menu.view.theme"));
        ToggleGroup themeGroup = new ToggleGroup();

        for (ThemeManager.Theme theme : themeManager.getAvailableThemes()) {
            RadioMenuItem themeItem = new RadioMenuItem(theme.getDisplayName());
            themeItem.setToggleGroup(themeGroup);
            themeItem.setSelected(theme == themeManager.getCurrentTheme());
            themeItem.setOnAction(e -> {
                themeManager.setTheme(theme);
                updateStatus(i18nManager.get("theme.changed", theme.getDisplayName()));
                updateStatusBar();
            });
            themeMenu.getItems().add(themeItem);
        }

        MenuItem toggleTheme = new MenuItem(i18nManager.get("menu.view.toggle_theme"));
        toggleTheme.setOnAction(e -> {
            themeManager.toggleTheme();
            updateStatus(i18nManager.get("theme.changed",
                    themeManager.getCurrentTheme().getDisplayName()));
            updateThemeRadioButtons(themeGroup);
            updateStatusBar();
        });

        CheckMenuItem showLineNumbers = new CheckMenuItem(
                i18nManager.get("menu.view.show_line_numbers")
        );
        showLineNumbers.setSelected(configManager.getConfig().isShowLineNumbers());

        CheckMenuItem showWhitespace = new CheckMenuItem(
                i18nManager.get("menu.view.show_whitespace")
        );
        showWhitespace.setSelected(configManager.getConfig().isShowWhitespace());

        menuView.getItems().addAll(
                themeMenu,
                toggleTheme,
                new SeparatorMenuItem(),
                showLineNumbers,
                showWhitespace
        );

        // ============================================
        // MENÚ HERRAMIENTAS
        // ============================================
        Menu menuTools = new Menu(i18nManager.get("menu.tools"));

        MenuItem compile = new MenuItem(i18nManager.get("menu.tools.compile"));
        compile.setAccelerator(new KeyCodeCombination(KeyCode.F5));
        compile.setOnAction(e -> {
            updateStatus(i18nManager.get("app.compiling"));
            compileCurrentFile();
        });

        MenuItem run = new MenuItem(i18nManager.get("menu.tools.run"));
        run.setAccelerator(new KeyCodeCombination(KeyCode.F6));
        run.setOnAction(e -> {
            CodeEditor editor = getCurrentEditor();

            if (editor == null || editor.getCurrentFile() == null) {
                updateStatus("⚠️ No hay archivo para ejecutar. Guarda primero.");
                return;
            }

            File sourceFile = editor.getCurrentFile();
            File parentDir = sourceFile.getParentFile();
            String fileName = sourceFile.getName();
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

            // --- CORRECCIÓN: Apuntar a la carpeta build ---
            // Construimos la ruta: carpeta_actual/build/archivo.tap
            File buildDir = new File(parentDir, "build");
            File tapFile = new File(buildDir, baseName + ".tap");
            // ----------------------------------------------

            System.out.println("[DEBUG] Buscando TAP en: " + tapFile.getAbsolutePath());

            if (tapFile.exists()) {
                updateStatus("🚀 Lanzando emulador con: " + tapFile.getName());
                Z80Launcher.launch(tapFile.getAbsolutePath());
            } else {
                updateStatus("⚠️ Archivo no encontrado en /build. Compila primero.");
            }
        });

        // +++ PRUEBA DE DEBUGGER +++
        MenuItem debugTestItem = new MenuItem("🛠 Probar Conexión Debugger");
        debugTestItem.setOnAction(e -> testDebuggerConnection());

        MenuItem spriteEditor = new MenuItem(i18nManager.get("menu.tools.sprite_editor"));
        MenuItem mapEditor = new MenuItem(i18nManager.get("menu.tools.map_editor"));
        MenuItem musicEditor = new MenuItem(i18nManager.get("menu.tools.music_editor"));

        menuTools.getItems().addAll(
                compile,
                run,
                debugTestItem,
                new SeparatorMenuItem(),
                spriteEditor,
                mapEditor,
                musicEditor
        );

        // ============================================
        // MENÚ CONFIGURACIÓN
        // ============================================
        Menu menuSettings = new Menu(i18nManager.get("menu.settings"));

        MenuItem preferences = new MenuItem(i18nManager.get("menu.settings.preferences"));
        preferences.setOnAction(e -> SettingsDialog.show(primaryStage.getScene()));

        Menu languageMenu = new Menu(i18nManager.get("menu.settings.language"));
        ToggleGroup languageGroup = new ToggleGroup();

        for (I18nManager.Language lang : i18nManager.getAvailableLanguages()) {
            RadioMenuItem langItem = new RadioMenuItem(lang.getDisplayName());
            langItem.setToggleGroup(languageGroup);
            langItem.setSelected(lang == i18nManager.getCurrentLanguage());
            languageMenu.getItems().add(langItem);
        }

        menuSettings.getItems().addAll(preferences, languageMenu);

        // ============================================
        // MENÚ AYUDA
        // ============================================
        Menu menuHelp = new Menu(i18nManager.get("menu.help"));

        MenuItem documentation = new MenuItem(i18nManager.get("menu.help.documentation"));
        MenuItem about = new MenuItem(i18nManager.get("menu.help.about"));
        about.setOnAction(e -> showAboutDialog());

        menuHelp.getItems().addAll(documentation, about);

        menuBar.getMenus().addAll(menuFile, menuEdit, menuView, menuTools, menuSettings, menuHelp);

        return menuBar;
    }

    private HBox createStatusBar() {
        // Asignamos a la variable de clase directamente
        statusBarBox = new HBox(10);
        statusBarBox.getStyleClass().add("status-bar");
        statusBarBox.setPadding(new Insets(5, 10, 5, 10));

        statusLabel = new Label(i18nManager.get("app.ready"));
        statusLabel.setStyle("-fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        statusBarBox.getChildren().addAll(statusLabel, spacer);

        return statusBarBox;
    }

    private void newFile() {
        // Ahora simplemente crea una nueva pestaña vacía
        createNewTab(null);
    }

    private void openFile() {
        File file = fileManager.showOpenDialog();
        if (file != null) {
            // Abre el archivo en una NUEVA pestaña
            createNewTab(file);
            updateStatus(i18nManager.get("file.opened", file.getName()));
        }
    }

    private void saveFile() {
        CodeEditor editor = getCurrentEditor();
        if (editor == null) return;

        if (editor.getCurrentFile() == null) {
            saveFileAs();
        } else {
            try {
                editor.saveFile();
                // El listener del asterisco se encarga de actualizar el texto del Tab
                updateStatus(i18nManager.get("file.saved", editor.getCurrentFile().getName()));
            } catch (Exception ex) {
                showAlert(i18nManager.get("file.error.save"), ex.getMessage());
            }
        }
    }

    private void saveFileAs() {
        CodeEditor editor = getCurrentEditor();
        if (editor == null) return;

        File file = fileManager.showSaveDialog();
        if (file != null) {
            try {
                editor.saveFile(file);
                // Actualizamos el título de la pestaña manualmente tras guardar como...
                Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
                if (currentTab != null) {
                    currentTab.setText(file.getName());
                }
                updateStatus(i18nManager.get("file.saved", file.getName()));
            } catch (Exception ex) {
                showAlert(i18nManager.get("file.error.save"), ex.getMessage());
            }
        }
    }

    // Devuelve TRUE si se puede continuar (se guardó o se descartó), FALSE si se canceló
    private boolean checkUnsavedChanges(CodeEditor editor) {
        if (editor != null && editor.isModified()) {
            // Recuperamos el nombre del archivo para el mensaje
            String fileName = (editor.getCurrentFile() != null) ? editor.getCurrentFile().getName() : i18nManager.get("file.untitled");

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(i18nManager.get("file.unsaved.title"));
            alert.setHeaderText(null);
            // Mensaje personalizado indicando qué archivo es
            alert.setContentText("El archivo '" + fileName + "' tiene cambios sin guardar.\n¿Deseas guardarlo?");

            ButtonType saveButton = new ButtonType(i18nManager.get("file.unsaved.save"));
            ButtonType discardButton = new ButtonType(i18nManager.get("file.unsaved.discard"));
            ButtonType cancelButton = new ButtonType(i18nManager.get("file.unsaved.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setMinWidth(500);
            themeManager.registerScene(dialogPane.getScene());

            var result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == saveButton) {
                    try {
                        // Lógica de guardado local
                        if (editor.getCurrentFile() == null) {
                            File file = fileManager.showSaveDialog();
                            if (file != null) {
                                editor.saveFile(file);
                                return true;
                            }
                            return false; // Cancelado en el diálogo de guardar
                        } else {
                            editor.saveFile();
                            return true;
                        }
                    } catch (Exception ex) {
                        showAlert("Error", "No se pudo guardar: " + ex.getMessage());
                        return false;
                    }
                } else if (result.get() == discardButton) {
                    return true; // Descartar cambios y continuar
                }
            }
            return false; // Cancelar operación
        }
        return true; // No estaba modificado
    }

    private void updateWindowTitle() {
        String title = i18nManager.get("app.title");

        // Obtenemos el editor de la pestaña activa
        CodeEditor editor = getCurrentEditor();

        if (editor != null) {
            if (editor.getCurrentFile() != null) {
                title += " - " + editor.getCurrentFile().getName();
            } else {
                title += " - " + i18nManager.get("file.untitled");
            }

            if (editor.isModified()) {
                title += " " + i18nManager.get("file.modified");
            }
        }

        primaryStage.setTitle(title);
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
        System.out.println("Status: " + message);
    }

    private void updateStatusBar() {
        // Verificación de seguridad simple
        if (statusBarBox == null) return;

        statusBarBox.getChildren().clear();

        statusLabel = new Label(statusLabel.getText());
        statusLabel.setStyle("-fx-font-size: 12px;");

        Label themeLabel = new Label(i18nManager.get("status.theme",
                themeManager.getCurrentTheme().getDisplayName()));
        themeLabel.setStyle("-fx-font-size: 12px; -fx-opacity: 0.7;");

        Label configLabel = new Label(i18nManager.get("status.tab_size",
                configManager.getConfig().getTabSize()));
        configLabel.setStyle("-fx-font-size: 12px; -fx-opacity: 0.7;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        statusBarBox.getChildren().addAll(statusLabel, spacer, themeLabel, configLabel);
    }

    private void updateThemeRadioButtons(ToggleGroup group) {
        for (Toggle toggle : group.getToggles()) {
            RadioMenuItem item = (RadioMenuItem) toggle;
            for (ThemeManager.Theme theme : themeManager.getAvailableThemes()) {
                if (item.getText().equals(theme.getDisplayName())) {
                    item.setSelected(theme == themeManager.getCurrentTheme());
                }
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setMinWidth(400);
        themeManager.registerScene(dialogPane.getScene());

        alert.showAndWait();
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(i18nManager.get("dialog.about.title"));
        alert.setHeaderText(i18nManager.get("dialog.about.header"));
        alert.setContentText(i18nManager.get("dialog.about.content"));

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setMinWidth(500);
        themeManager.registerScene(dialogPane.getScene());

        alert.showAndWait();
    }

    @Override
    public void stop() {
        configManager.saveConfig();
        System.out.println(i18nManager.get("message.config_saved"));
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Helper para obtener el editor de la pestaña activa
    private CodeEditor getCurrentEditor() {
        if (tabPane == null || tabPane.getSelectionModel().getSelectedItem() == null) {
            return null;
        }
        return (CodeEditor) tabPane.getSelectionModel().getSelectedItem().getContent();
    }

    private void handleAppClose() {
        // Verificar todas las pestañas abiertas
        for (Tab tab : tabPane.getTabs()) {
            CodeEditor editor = (CodeEditor) tab.getContent();
            if (!checkUnsavedChanges(editor)) {
                return; // Si el usuario cancela en algún archivo, abortamos el cierre
            }
        }
        System.out.println("Cerrando ZX Spectrum IDE...");
        primaryStage.close();
    }

    private void compileCurrentFile() {
        CodeEditor editor = getCurrentEditor();
        if (editor == null || editor.getCurrentFile() == null) {
            logToConsole("Error: No hay archivo seleccionado o no se ha guardado nunca.");
            return;
        }

        // 1. Guardar cambios antes de compilar
        if (editor.isModified()) {
            try {
                editor.saveFile();
                logToConsole("Archivo guardado automáticamente: " + editor.getCurrentFile().getName());
            } catch (Exception e) {
                logToConsole("Error al guardar: " + e.getMessage());
                return;
            }
        }

        // 2. Preparar compilación
        File sourceFile = editor.getCurrentFile();
        // Creamos carpeta 'build' junto al archivo fuente
        File outputDir = getBuildDirectory(sourceFile);

        if (!outputDir.exists()) outputDir.mkdirs();

        consoleArea.clear();
        logToConsole("Compilando en: " + outputDir.getAbsolutePath());

        // 3. Ejecutar PasmoCompiler en un hilo separado (para no congelar la UI)
        new Thread(() -> {
            try {
                PasmoCompiler compiler = new PasmoCompiler();
                Process process = compiler.compile(sourceFile, outputDir);

                // Leer la salida del proceso (stdout y stderr combinados en PasmoCompiler)
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String finalLine = line;
                        // Actualizar UI desde el hilo de JavaFX
                        Platform.runLater(() -> logToConsole(finalLine));
                    }
                }

                int exitCode = process.waitFor();
                Platform.runLater(() -> {
                    if (exitCode == 0) {
                        logToConsole("COMPILACIÓN EXITOSA");
                        updateStatus("Compilación finalizada con éxito.");
                    } else {
                        logToConsole("ERROR DE COMPILACIÓN");
                        updateStatus("Falló la compilación.");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> logToConsole("Error crítico al lanzar compilador: " + e.getMessage()));
            }
        }).start();
    }

    // Helper para escribir en la consola
    private void logToConsole(String text) {
        consoleArea.appendText(text + "\n");
        // Auto-scroll al final
        consoleArea.positionCaret(consoleArea.getLength());
    }

    private File getBuildDirectory(File sourceFile) {
        String customBuildPath = ConfigManager.getInstance().getConfig().getBuildPath();

        if (customBuildPath != null && !customBuildPath.isBlank()) {
            File customDir = new File(customBuildPath);
            if (!customDir.exists()) customDir.mkdirs();
            return customDir;
        } else {
            // Comportamiento por defecto: Carpeta 'build' relativa al fuente
            File defaultDir = new File(sourceFile.getParent(), "build");
            if (!defaultDir.exists()) defaultDir.mkdirs();
            return defaultDir;
        }
    }

    private void runCurrentFile() {
        CodeEditor editor = getCurrentEditor();
        if (editor == null || editor.getCurrentFile() == null) {
            showAlert("Error", "No hay archivo para ejecutar.");
            return;
        }

        File sourceFile = editor.getCurrentFile();
        File buildDir = getBuildDirectory(sourceFile);

        // Calculamos el nombre del TAP esperado
        // NOTA: PasmoCompiler usa esta misma lógica para nombrar el archivo
        String baseName = sourceFile.getName().replaceFirst("[.][^.]+$", "");

        // Asumimos .tap (o .hex/.bin según config, pero ZEsarUX suele querer .tap)
        // Si soportas múltiples formatos, deberías consultar AppConfig.getPasmoFormat()
        String extension = ".tap";
        if("hex".equals(ConfigManager.getInstance().getConfig().getPasmoFormat())) extension = ".hex";
        if("bin".equals(ConfigManager.getInstance().getConfig().getPasmoFormat())) extension = ".bin";

        File executableFile = new File(buildDir, baseName + extension);

        if (!executableFile.exists()) {
            logToConsole("Error: No se encuentra el ejecutable: " + executableFile.getAbsolutePath());
            logToConsole("¿Has compilado el proyecto (F5)?");
            showAlert("Archivo no encontrado", "Primero debes compilar el proyecto (F5).");
            return;
        }

        logToConsole("Lanzando emulador con: " + executableFile.getAbsolutePath());

        // Ejecutar en hilo aparte para no bloquear
        new Thread(() -> {
            Z80Launcher.launch(executableFile.getAbsolutePath());
        }).start();
    }

    private void loadCustomFont(String fontName) {
        try {
            // La ruta debe ser relativa a la carpeta 'resources'
            String fontPath = "/com/lazyzxsoftware/zxspectrumide/fonts/" + fontName;
            var fontStream = getClass().getResourceAsStream(fontPath);
            if (fontStream != null) {
                Font.loadFont(fontStream, 14); // El tamaño 14 es solo para cargarla, luego el CSS decide
                System.out.println("Fuente cargada: " + fontName);
            } else {
                System.err.println("No se pudo cargar la fuente: " + fontPath);
            }
        } catch (Exception e) {
            System.err.println("Error cargando fuente " + fontName + ": " + e.getMessage());
        }
    }

    private void testDebuggerConnection() {
        consoleArea.appendText("\n[DEBUG] Intentando conectar con ZEsarUX...\n");

        // Usamos un hilo para no congelar la UI al conectar
        new Thread(() -> {
            var bridge = com.lazyzxsoftware.zxspectrumide.integration.ZesaruxBridge.getInstance();

            if (bridge.connect()) {
                Platform.runLater(() -> consoleArea.appendText("[DEBUG] ¡CONECTADO! Enviando comando 'about'...\n"));

                // Enviar comando de prueba
                bridge.sendCommand("about").thenAccept(response -> {
                    Platform.runLater(() -> {
                        consoleArea.appendText("[DEBUG] Respuesta recibida:\n" + response + "\n");
                        consoleArea.appendText("------------------------------------------------\n");
                    });

                    // Desconectamos tras la prueba
                    bridge.disconnect();
                });

            } else {
                Platform.runLater(() -> {
                    consoleArea.appendText("[ERROR] No se pudo conectar.\n");
                    consoleArea.appendText("Asegúrate de que:\n");
                    consoleArea.appendText("1. ZEsarUX está abierto.\n");
                    consoleArea.appendText("2. Se lanzó con --enable-remotectrl (usa el botón Ejecutar del IDE).\n");
                });
            }
        }).start();
    }
}