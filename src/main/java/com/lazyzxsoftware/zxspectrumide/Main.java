package com.lazyzxsoftware.zxspectrumide;

import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import com.lazyzxsoftware.zxspectrumide.theme.ThemeManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage primaryStage;
    private ThemeManager themeManager;
    private ConfigManager configManager;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Inicializar managers
        configManager = ConfigManager.getInstance();
        themeManager = ThemeManager.getInstance();

        // Configuración de la ventana principal
        primaryStage.setTitle("ZX Spectrum IDE - v0.0.1");

        // Layout principal
        BorderPane root = new BorderPane();

        // Barra de menú
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        // Área central (temporal)
        Label placeholder = new Label("ZX Spectrum IDE");
        placeholder.setStyle("-fx-font-size: 32px; -fx-opacity: 0.5;");
        root.setCenter(placeholder);

        // Barra de estado
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);

        // Crear la escena
        Scene scene = new Scene(root, 1200, 800);

        // Registrar escena en ThemeManager y aplicar tema
        themeManager.registerScene(scene);

        // Aplicar a la ventana
        primaryStage.setScene(scene);
        primaryStage.show();

        // Actualizar estado
        updateStatus("Listo");

        System.out.println("ZX Spectrum IDE iniciado correctamente");
        System.out.println("Tema actual: " + themeManager.getCurrentTheme().getDisplayName());
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // ============================================
        // MENÚ ARCHIVO
        // ============================================
        Menu menuFile = new Menu("Archivo");

        MenuItem newProject = new MenuItem("Nuevo Proyecto...");
        newProject.setOnAction(e -> showAlert("Nuevo Proyecto", "Funcionalidad en desarrollo"));

        MenuItem openProject = new MenuItem("Abrir Proyecto...");
        openProject.setOnAction(e -> showAlert("Abrir Proyecto", "Funcionalidad en desarrollo"));

        MenuItem saveFile = new MenuItem("Guardar");
        saveFile.setOnAction(e -> updateStatus("Archivo guardado"));

        MenuItem saveAsFile = new MenuItem("Guardar como...");

        MenuItem exit = new MenuItem("Salir");
        exit.setOnAction(e -> {
            System.out.println("Cerrando ZX Spectrum IDE...");
            primaryStage.close();
        });

        menuFile.getItems().addAll(
                newProject,
                openProject,
                new SeparatorMenuItem(),
                saveFile,
                saveAsFile,
                new SeparatorMenuItem(),
                exit
        );

        // ============================================
        // MENÚ EDITAR
        // ============================================
        Menu menuEdit = new Menu("Editar");

        MenuItem undo = new MenuItem("Deshacer");
        undo.setOnAction(e -> updateStatus("Deshacer"));

        MenuItem redo = new MenuItem("Rehacer");
        redo.setOnAction(e -> updateStatus("Rehacer"));

        MenuItem cut = new MenuItem("Cortar");
        MenuItem copy = new MenuItem("Copiar");
        MenuItem paste = new MenuItem("Pegar");

        MenuItem find = new MenuItem("Buscar...");
        MenuItem replace = new MenuItem("Reemplazar...");

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
        Menu menuView = new Menu("Ver");

        // Submenú de temas
        Menu themeMenu = new Menu("Tema");
        ToggleGroup themeGroup = new ToggleGroup();

        for (ThemeManager.Theme theme : themeManager.getAvailableThemes()) {
            RadioMenuItem themeItem = new RadioMenuItem(theme.getDisplayName());
            themeItem.setToggleGroup(themeGroup);
            themeItem.setSelected(theme == themeManager.getCurrentTheme());
            themeItem.setOnAction(e -> {
                themeManager.setTheme(theme);
                updateStatus("Tema cambiado a: " + theme.getDisplayName());
            });
            themeMenu.getItems().add(themeItem);
        }

        MenuItem toggleTheme = new MenuItem("Alternar Tema");
        toggleTheme.setOnAction(e -> {
            themeManager.toggleTheme();
            updateStatus("Tema alternado");
            // Actualizar radio buttons
            updateThemeRadioButtons(themeGroup);
        });

        CheckMenuItem showLineNumbers = new CheckMenuItem("Mostrar números de línea");
        showLineNumbers.setSelected(configManager.getConfig().isShowLineNumbers());
        showLineNumbers.setOnAction(e -> {
            configManager.getConfig().setShowLineNumbers(showLineNumbers.isSelected());
            configManager.saveConfig();
            updateStatus("Números de línea: " + (showLineNumbers.isSelected() ? "activados" : "desactivados"));
        });

        CheckMenuItem showWhitespace = new CheckMenuItem("Mostrar espacios en blanco");
        showWhitespace.setSelected(configManager.getConfig().isShowWhitespace());
        showWhitespace.setOnAction(e -> {
            configManager.getConfig().setShowWhitespace(showWhitespace.isSelected());
            configManager.saveConfig();
        });

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
        Menu menuTools = new Menu("Herramientas");

        MenuItem compile = new MenuItem("Compilar (F5)");
        compile.setOnAction(e -> {
            updateStatus("Compilando...");
            showAlert("Compilar", "PASMO no configurado todavía");
        });

        MenuItem run = new MenuItem("Ejecutar (F6)");
        run.setOnAction(e -> {
            updateStatus("Ejecutando...");
            showAlert("Ejecutar", "ZEsarUX no configurado todavía");
        });

        MenuItem spriteEditor = new MenuItem("Editor de Sprites");
        spriteEditor.setOnAction(e -> showAlert("Editor de Sprites", "Funcionalidad en desarrollo"));

        MenuItem mapEditor = new MenuItem("Editor de Mapas");
        mapEditor.setOnAction(e -> showAlert("Editor de Mapas", "Funcionalidad en desarrollo"));

        MenuItem musicEditor = new MenuItem("Editor de Música");
        musicEditor.setOnAction(e -> showAlert("Editor de Música", "Funcionalidad en desarrollo"));

        menuTools.getItems().addAll(
                compile,
                run,
                new SeparatorMenuItem(),
                spriteEditor,
                mapEditor,
                musicEditor
        );

        // ============================================
        // MENÚ CONFIGURACIÓN
        // ============================================
        Menu menuSettings = new Menu("Configuración");

        MenuItem preferences = new MenuItem("Preferencias...");
        preferences.setOnAction(e -> showAlert("Preferencias", "Panel de configuración en desarrollo"));

        menuSettings.getItems().add(preferences);

        // ============================================
        // MENÚ AYUDA
        // ============================================
        Menu menuHelp = new Menu("Ayuda");

        MenuItem documentation = new MenuItem("Documentación");
        documentation.setOnAction(e -> showAlert("Documentación", "Ver README.md y TODO.md en el repositorio"));

        MenuItem about = new MenuItem("Acerca de...");
        about.setOnAction(e -> showAboutDialog());

        menuHelp.getItems().addAll(documentation, about);

        menuBar.getMenus().addAll(menuFile, menuEdit, menuView, menuTools, menuSettings, menuHelp);

        return menuBar;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(5, 10, 5, 10));

        statusLabel = new Label("Listo");
        statusLabel.setStyle("-fx-font-size: 12px;");

        // Información adicional
        Label themeLabel = new Label("Tema: " + themeManager.getCurrentTheme().getDisplayName());
        themeLabel.setStyle("-fx-font-size: 12px; -fx-opacity: 0.7;");

        Label configLabel = new Label("Tab: " + configManager.getConfig().getTabSize() + " espacios");
        configLabel.setStyle("-fx-font-size: 12px; -fx-opacity: 0.7;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        statusBar.getChildren().addAll(statusLabel, spacer, themeLabel, configLabel);

        return statusBar;
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
        System.out.println("Status: " + message);
    }

    private void updateThemeRadioButtons(ToggleGroup group) {
        // Actualizar la selección de los radio buttons después de cambiar tema
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

        // Aplicar tema al diálogo
        DialogPane dialogPane = alert.getDialogPane();
        themeManager.registerScene(dialogPane.getScene());

        alert.showAndWait();
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de ZX Spectrum IDE");
        alert.setHeaderText("ZX Spectrum IDE v0.0.1");
        alert.setContentText(
                "IDE moderno para desarrollo de juegos ZX Spectrum\n\n" +
                        "Desarrollado por: Lazy ZX Software\n" +
                        "Licencia: Apache 2.0\n" +
                        "GitHub: https://github.com/jlrtutor/ZXSpectrumIDE\n\n" +
                        "Tecnologías:\n" +
                        "- Java 17\n" +
                        "- JavaFX 21\n" +
                        "- RichTextFX\n\n" +
                        "© 2024 Lazy ZX Software"
        );

        // Aplicar tema al diálogo
        DialogPane dialogPane = alert.getDialogPane();
        themeManager.registerScene(dialogPane.getScene());

        alert.showAndWait();
    }

    @Override
    public void stop() {
        // Guardar configuración al cerrar
        configManager.saveConfig();
        System.out.println("Configuración guardada. ZX Spectrum IDE cerrado.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}