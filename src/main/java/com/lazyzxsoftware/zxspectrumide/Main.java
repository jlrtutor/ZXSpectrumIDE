package com.lazyzxsoftware.zxspectrumide;

import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import com.lazyzxsoftware.zxspectrumide.editor.CodeEditor;
import com.lazyzxsoftware.zxspectrumide.editor.FileManager;
import com.lazyzxsoftware.zxspectrumide.i18n.I18nManager;
import com.lazyzxsoftware.zxspectrumide.theme.ThemeManager;
import com.lazyzxsoftware.zxspectrumide.utils.SplashScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    private Stage primaryStage;
    private ThemeManager themeManager;
    private ConfigManager configManager;
    private I18nManager i18nManager;
    private Label statusLabel;
    private CodeEditor codeEditor;
    private FileManager fileManager;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Mostrar splash screen
        SplashScreen splash = new SplashScreen();
        splash.show();

        // Inicializar en background
        Platform.runLater(() -> {
            try {
                splash.updateStatus("Cargando configuración...");
                Thread.sleep(300);

                configManager = ConfigManager.getInstance();

                splash.updateStatus("Cargando idioma...");
                Thread.sleep(300);
                i18nManager = I18nManager.getInstance();

                splash.updateStatus("Cargando temas...");
                Thread.sleep(300);
                themeManager = ThemeManager.getInstance();

                splash.updateStatus("Inicializando gestor de archivos...");
                Thread.sleep(300);
                fileManager = new FileManager(primaryStage);

                primaryStage.setTitle(i18nManager.get("app.title"));

                BorderPane root = new BorderPane();

                splash.updateStatus("Creando interfaz...");
                Thread.sleep(300);

                MenuBar menuBar = createMenuBar();
                root.setTop(menuBar);

                splash.updateStatus("Inicializando editor...");
                Thread.sleep(400);

                codeEditor = new CodeEditor();
                codeEditor.setOnModifiedChanged(this::updateWindowTitle);
                root.setCenter(codeEditor);

                HBox statusBar = createStatusBar();
                root.setBottom(statusBar);

                Scene scene = new Scene(root, 1200, 800);
                primaryStage.setScene(scene);

                splash.updateStatus("Aplicando tema...");
                Thread.sleep(300);

                themeManager.registerScene(scene);

                splash.updateStatus("Finalizando...");
                Thread.sleep(300);

                updateStatus(i18nManager.get("app.ready"));

                System.out.println(i18nManager.get("message.ide_started"));
                System.out.println(i18nManager.get("message.current_theme",
                        themeManager.getCurrentTheme().getDisplayName()));

                // Cerrar splash y mostrar ventana principal
                splash.close(() -> {
                    primaryStage.show();
                    updateWindowTitle();
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
                splash.close(() -> primaryStage.show());
            }
        });
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
        exit.setOnAction(e -> {
            System.out.println("Cerrando ZX Spectrum IDE...");
            primaryStage.close();
        });

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
        // MENÚ EDITAR
        // ============================================
        Menu menuEdit = new Menu(i18nManager.get("menu.edit"));

        MenuItem undo = new MenuItem(i18nManager.get("menu.edit.undo"));
        undo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN));
        undo.setOnAction(e -> {
            if (codeEditor != null && codeEditor.canUndo()) {
                codeEditor.undo();
                updateStatus(i18nManager.get("menu.edit.undo"));
            }
        });

        MenuItem redo = new MenuItem(i18nManager.get("menu.edit.redo"));
        redo.setAccelerator(new KeyCodeCombination(KeyCode.Z,
                KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        redo.setOnAction(e -> {
            if (codeEditor != null && codeEditor.canRedo()) {
                codeEditor.redo();
                updateStatus(i18nManager.get("menu.edit.redo"));
            }
        });

        MenuItem cut = new MenuItem(i18nManager.get("menu.edit.cut"));
        cut.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN));
        cut.setOnAction(e -> {
            if (codeEditor != null) {
                codeEditor.cut();
            }
        });

        MenuItem copy = new MenuItem(i18nManager.get("menu.edit.copy"));
        copy.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        copy.setOnAction(e -> {
            if (codeEditor != null) {
                codeEditor.copy();
            }
        });

        MenuItem paste = new MenuItem(i18nManager.get("menu.edit.paste"));
        paste.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN));
        paste.setOnAction(e -> {
            if (codeEditor != null) {
                codeEditor.paste();
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
            showAlert(
                    i18nManager.get("dialog.compile.title"),
                    i18nManager.get("dialog.compile.message")
            );
        });

        MenuItem run = new MenuItem(i18nManager.get("menu.tools.run"));
        run.setAccelerator(new KeyCodeCombination(KeyCode.F6));
        run.setOnAction(e -> {
            updateStatus(i18nManager.get("app.running"));
            showAlert(
                    i18nManager.get("dialog.run.title"),
                    i18nManager.get("dialog.run.message")
            );
        });

        MenuItem spriteEditor = new MenuItem(i18nManager.get("menu.tools.sprite_editor"));
        MenuItem mapEditor = new MenuItem(i18nManager.get("menu.tools.map_editor"));
        MenuItem musicEditor = new MenuItem(i18nManager.get("menu.tools.music_editor"));

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
        HBox statusBar = new HBox(10);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(5, 10, 5, 10));

        statusLabel = new Label(i18nManager.get("app.ready"));
        statusLabel.setStyle("-fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        statusBar.getChildren().addAll(statusLabel, spacer);

        return statusBar;
    }

    private void newFile() {
        if (checkUnsavedChanges()) {
            codeEditor.clear();
            updateWindowTitle();
            updateStatus(i18nManager.get("app.ready"));
        }
    }

    private void openFile() {
        if (checkUnsavedChanges()) {
            File file = fileManager.showOpenDialog();
            if (file != null) {
                try {
                    codeEditor.openFile(file);
                    updateWindowTitle();
                    updateStatus(i18nManager.get("file.opened", file.getName()));
                } catch (Exception ex) {
                    showAlert(i18nManager.get("file.error.open"), ex.getMessage());
                }
            }
        }
    }

    private void saveFile() {
        if (codeEditor.getCurrentFile() == null) {
            saveFileAs();
        } else {
            try {
                codeEditor.saveFile();
                updateWindowTitle();
                updateStatus(i18nManager.get("file.saved",
                        codeEditor.getCurrentFile().getName()));
            } catch (Exception ex) {
                showAlert(i18nManager.get("file.error.save"), ex.getMessage());
            }
        }
    }

    private void saveFileAs() {
        File file = fileManager.showSaveDialog();
        if (file != null) {
            try {
                codeEditor.saveFile(file);
                updateWindowTitle();
                updateStatus(i18nManager.get("file.saved", file.getName()));
            } catch (Exception ex) {
                showAlert(i18nManager.get("file.error.save"), ex.getMessage());
            }
        }
    }

    private boolean checkUnsavedChanges() {
        if (codeEditor.isModified()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(i18nManager.get("file.unsaved.title"));
            alert.setHeaderText(null);
            alert.setContentText(i18nManager.get("file.unsaved.message"));

            ButtonType saveButton = new ButtonType(i18nManager.get("file.unsaved.save"));
            ButtonType discardButton = new ButtonType(i18nManager.get("file.unsaved.discard"));
            ButtonType cancelButton = new ButtonType(i18nManager.get("file.unsaved.cancel"),
                    ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setMinWidth(500);
            themeManager.registerScene(dialogPane.getScene());

            var result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == saveButton) {
                    saveFile();
                    return !codeEditor.isModified();
                } else if (result.get() == discardButton) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private void updateWindowTitle() {
        String title = i18nManager.get("app.title");

        if (codeEditor != null && codeEditor.getCurrentFile() != null) {
            title += " - " + codeEditor.getCurrentFile().getName();
        } else {
            title += " - " + i18nManager.get("file.untitled");
        }

        if (codeEditor != null && codeEditor.isModified()) {
            title += " " + i18nManager.get("file.modified");
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
}