package com.lazyzxsoftware.zxspectrumide;

import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import com.lazyzxsoftware.zxspectrumide.editor.CodeEditor;
import com.lazyzxsoftware.zxspectrumide.i18n.I18nManager;
import com.lazyzxsoftware.zxspectrumide.theme.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private Stage primaryStage;
    private ThemeManager themeManager;
    private ConfigManager configManager;
    private I18nManager i18nManager;
    private Label statusLabel;
    private CodeEditor codeEditor;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Inicializar managers
        configManager = ConfigManager.getInstance();
        i18nManager = I18nManager.getInstance();
        themeManager = ThemeManager.getInstance();

        // Configuración de la ventana principal
        primaryStage.setTitle(i18nManager.get("app.title"));

        // Hacer la ventana invisible inicialmente para evitar parpadeo
        primaryStage.setOpacity(0);

        // Layout principal
        BorderPane root = new BorderPane();

        // Barra de menú
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        // Área central - Editor de código
        codeEditor = new CodeEditor();
        root.setCenter(codeEditor);

        // Barra de estado
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);

        // Crear la escena
        Scene scene = new Scene(root, 1200, 800);

        // Aplicar a la ventana
        primaryStage.setScene(scene);

        // Registrar escena en ThemeManager y aplicar tema
        themeManager.registerScene(scene);

        // Mostrar ventana (todavía invisible)
        primaryStage.show();

        // Hacer visible después de que todo esté renderizado
        Platform.runLater(() -> {
            primaryStage.setOpacity(1);
        });

        // Actualizar estado
        updateStatus(i18nManager.get("app.ready"));

        System.out.println(i18nManager.get("message.ide_started"));
        System.out.println(i18nManager.get("message.current_theme",
                themeManager.getCurrentTheme().getDisplayName()));
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // ============================================
        // MENÚ ARCHIVO
        // ============================================
        Menu menuFile = new Menu(i18nManager.get("menu.file"));

        MenuItem newProject = new MenuItem(i18nManager.get("menu.file.new_project"));
        newProject.setOnAction(e -> showAlert(
                i18nManager.get("dialog.new_project.title"),
                i18nManager.get("dialog.new_project.message")
        ));

        MenuItem openProject = new MenuItem(i18nManager.get("menu.file.open_project"));
        openProject.setOnAction(e -> showAlert(
                i18nManager.get("dialog.open_project.title"),
                i18nManager.get("dialog.open_project.message")
        ));

        MenuItem saveFile = new MenuItem(i18nManager.get("menu.file.save"));
        saveFile.setOnAction(e -> {
            if (codeEditor != null) {
                try {
                    codeEditor.saveFile();
                    updateStatus(i18nManager.get("status.file_saved"));
                } catch (IOException ex) {
                    showAlert("Error", "No se pudo guardar el archivo: " + ex.getMessage());
                }
            }
        });

        MenuItem saveAsFile = new MenuItem(i18nManager.get("menu.file.save_as"));

        MenuItem exit = new MenuItem(i18nManager.get("menu.file.exit"));
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
        Menu menuEdit = new Menu(i18nManager.get("menu.edit"));

        MenuItem undo = new MenuItem(i18nManager.get("menu.edit.undo"));
        undo.setOnAction(e -> {
            if (codeEditor != null && codeEditor.canUndo()) {
                codeEditor.undo();
                updateStatus(i18nManager.get("menu.edit.undo"));
            }
        });

        MenuItem redo = new MenuItem(i18nManager.get("menu.edit.redo"));
        redo.setOnAction(e -> {
            if (codeEditor != null && codeEditor.canRedo()) {
                codeEditor.redo();
                updateStatus(i18nManager.get("menu.edit.redo"));
            }
        });

        MenuItem cut = new MenuItem(i18nManager.get("menu.edit.cut"));
        cut.setOnAction(e -> {
            if (codeEditor != null) {
                codeEditor.cut();
            }
        });

        MenuItem copy = new MenuItem(i18nManager.get("menu.edit.copy"));
        copy.setOnAction(e -> {
            if (codeEditor != null) {
                codeEditor.copy();
            }
        });

        MenuItem paste = new MenuItem(i18nManager.get("menu.edit.paste"));
        paste.setOnAction(e -> {
            if (codeEditor != null) {
                codeEditor.paste();
            }
        });

        MenuItem find = new MenuItem(i18nManager.get("menu.edit.find"));
        MenuItem replace = new MenuItem(i18nManager.get("menu.edit.replace"));

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

        // Submenú de temas
        Menu themeMenu = new Menu(i18nManager.get("menu.view.theme"));
        ToggleGroup themeGroup = new ToggleGroup();

        for (ThemeManager.Theme theme : themeManager.getAvailableThemes()) {
            RadioMenuItem themeItem = new RadioMenuItem(theme.getDisplayName());
            themeItem.setToggleGroup(themeGroup);
            themeItem.setSelected(theme == themeManager.getCurrentTheme());
            themeItem.setOnAction(e -> {
                themeManager.setTheme(theme);
                updateStatus(i18nManager.get("theme.changed", theme.getDisplayName()));
                updateStatusBar(); // Actualizar barra de estado
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
        showLineNumbers.setOnAction(e -> {
            configManager.getConfig().setShowLineNumbers(showLineNumbers.isSelected());
            configManager.saveConfig();
            updateStatus(showLineNumbers.isSelected() ?
                    i18nManager.get("status.line_numbers.enabled") :
                    i18nManager.get("status.line_numbers.disabled")
            );
        });

        CheckMenuItem showWhitespace = new CheckMenuItem(
                i18nManager.get("menu.view.show_whitespace")
        );
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
        Menu menuTools = new Menu(i18nManager.get("menu.tools"));

        MenuItem compile = new MenuItem(i18nManager.get("menu.tools.compile"));
        compile.setOnAction(e -> {
            updateStatus(i18nManager.get("app.compiling"));
            showAlert(
                    i18nManager.get("dialog.compile.title"),
                    i18nManager.get("dialog.compile.message")
            );
        });

        MenuItem run = new MenuItem(i18nManager.get("menu.tools.run"));
        run.setOnAction(e -> {
            updateStatus(i18nManager.get("app.running"));
            showAlert(
                    i18nManager.get("dialog.run.title"),
                    i18nManager.get("dialog.run.message")
            );
        });

        MenuItem spriteEditor = new MenuItem(i18nManager.get("menu.tools.sprite_editor"));
        spriteEditor.setOnAction(e -> showAlert(
                i18nManager.get("dialog.sprite_editor.title"),
                i18nManager.get("dialog.sprite_editor.message")
        ));

        MenuItem mapEditor = new MenuItem(i18nManager.get("menu.tools.map_editor"));
        mapEditor.setOnAction(e -> showAlert(
                i18nManager.get("dialog.map_editor.title"),
                i18nManager.get("dialog.map_editor.message")
        ));

        MenuItem musicEditor = new MenuItem(i18nManager.get("menu.tools.music_editor"));
        musicEditor.setOnAction(e -> showAlert(
                i18nManager.get("dialog.music_editor.title"),
                i18nManager.get("dialog.music_editor.message")
        ));

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
        Menu menuSettings = new Menu(i18nManager.get("menu.settings"));

        MenuItem preferences = new MenuItem(i18nManager.get("menu.settings.preferences"));
        preferences.setOnAction(e -> showAlert(
                i18nManager.get("dialog.preferences.title"),
                i18nManager.get("dialog.preferences.message")
        ));

        // Submenú de idiomas
        Menu languageMenu = new Menu(i18nManager.get("menu.settings.language"));
        ToggleGroup languageGroup = new ToggleGroup();

        for (I18nManager.Language lang : i18nManager.getAvailableLanguages()) {
            RadioMenuItem langItem = new RadioMenuItem(lang.getDisplayName());
            langItem.setToggleGroup(languageGroup);
            langItem.setSelected(lang == i18nManager.getCurrentLanguage());
            langItem.setOnAction(e -> {
                i18nManager.loadLanguage(lang);
                showAlert("Idioma", "Reinicia la aplicación para aplicar el cambio de idioma.");
            });
            languageMenu.getItems().add(langItem);
        }

        menuSettings.getItems().addAll(preferences, languageMenu);

        // ============================================
        // MENÚ AYUDA
        // ============================================
        Menu menuHelp = new Menu(i18nManager.get("menu.help"));

        MenuItem documentation = new MenuItem(i18nManager.get("menu.help.documentation"));
        documentation.setOnAction(e -> showAlert(
                i18nManager.get("dialog.documentation.title"),
                i18nManager.get("dialog.documentation.message")
        ));

        MenuItem about = new MenuItem(i18nManager.get("menu.help.about"));
        about.setOnAction(e -> showAboutDialog());

        menuHelp.getItems().addAll(documentation, about);

        menuBar.getMenus().addAll(menuFile, menuEdit, menuView, menuTools, menuSettings, menuHelp);

        return menuBar;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(5, 10, 5, 10));

        statusLabel = new Label(i18nManager.get("app.ready"));
        statusLabel.setStyle("-fx-font-size: 12px;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        statusBar.getChildren().addAll(statusLabel, spacer);

        return statusBar;
    }

    private void updateStatusBar() {
        // Recrear la barra de estado con información actualizada
        HBox statusBar = (HBox) ((BorderPane) primaryStage.getScene().getRoot()).getBottom();
        statusBar.getChildren().clear();

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

        statusBar.getChildren().addAll(statusLabel, spacer, themeLabel, configLabel);
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
        System.out.println("Status: " + message);
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
        themeManager.registerScene(dialogPane.getScene());

        alert.showAndWait();
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(i18nManager.get("dialog.about.title"));
        alert.setHeaderText(i18nManager.get("dialog.about.header"));
        alert.setContentText(i18nManager.get("dialog.about.content"));

        DialogPane dialogPane = alert.getDialogPane();
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
}