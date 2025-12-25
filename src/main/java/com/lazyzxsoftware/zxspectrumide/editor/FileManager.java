package com.lazyzxsoftware.zxspectrumide.editor;

import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import com.lazyzxsoftware.zxspectrumide.i18n.I18nManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

public class FileManager {

    private final Stage stage;
    private final CodeEditor codeEditor; // CAMBIO: Usamos CodeEditor, no CodeArea
    private File currentFile;
    private boolean modified = false;
    private final I18nManager i18n;

    // CAMBIO: Constructor recibe CodeEditor
    public FileManager(Stage stage, CodeEditor codeEditor) {
        this.stage = stage;
        this.codeEditor = codeEditor;
        this.i18n = I18nManager.getInstance();

        // Detectar cambios a través del CodeEditor
        this.codeEditor.setOnModifiedChanged(() -> {
            if (!modified) setModified(true);
        });
    }

    public void newFile() {
        if (checkUnsavedChanges()) {
            codeEditor.clear(); // Usamos el método del editor
            setCurrentFile(null);
            setModified(false);
        }
    }

    public void openFile() {
        if (checkUnsavedChanges()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(i18n.get("dialog.open.title"));
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Assembler Files (*.asm, *.z80)", "*.asm", "*.z80")
            );

            String lastDir = ConfigManager.getInstance().getConfig().getLastDirectory();
            if (lastDir != null && !lastDir.isEmpty()) {
                File dir = new File(lastDir);
                if (dir.exists()) fileChooser.setInitialDirectory(dir);
            }

            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                loadFile(file);
                updateLastDirectory(file);
            }
        }
    }

    public boolean saveFile() {
        if (currentFile == null) {
            return saveFileAs();
        } else {
            return writeToFile(currentFile);
        }
    }

    public boolean saveFileAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(i18n.get("dialog.save.title"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Assembler Files (*.asm)", "*.asm")
        );

        if (currentFile != null) {
            fileChooser.setInitialDirectory(currentFile.getParentFile());
            fileChooser.setInitialFileName(currentFile.getName());
        } else {
            String lastDir = ConfigManager.getInstance().getConfig().getLastDirectory();
            if (lastDir != null) {
                File dir = new File(lastDir);
                if (dir.exists()) fileChooser.setInitialDirectory(dir);
            }
        }

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            updateLastDirectory(file);
            return writeToFile(file);
        }
        return false;
    }

    private void loadFile(File file) {
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);

            // CAMBIO CLAVE: Usamos setText del CodeEditor.
            // Esto fuerza el repintado de sintaxis inmediatamente.
            codeEditor.setText(content);

            // Limpiamos historial de deshacer (accediendo al área interna si es necesario)
            codeEditor.getCodeArea().getUndoManager().forgetHistory();

            setCurrentFile(file);
            setModified(false);
        } catch (IOException e) {
            showError("Error loading file", e.getMessage());
        }
    }

    private boolean writeToFile(File file) {
        try {
            Files.writeString(file.toPath(), codeEditor.getText(), StandardCharsets.UTF_8);
            setCurrentFile(file);
            setModified(false);
            return true;
        } catch (IOException e) {
            showError("Error saving file", e.getMessage());
            return false;
        }
    }

    public boolean checkUnsavedChanges() {
        if (!modified) return true;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(i18n.get("dialog.unsaved.title"));
        alert.setHeaderText(i18n.get("dialog.unsaved.header"));
        alert.setContentText(i18n.get("dialog.unsaved.content"));

        ButtonType btnSave = new ButtonType(i18n.get("button.save"));
        ButtonType btnDontSave = new ButtonType(i18n.get("button.dontsave"));
        ButtonType btnCancel = new ButtonType(i18n.get("button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnSave, btnDontSave, btnCancel);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == btnSave) {
                return saveFile();
            } else if (result.get() == btnDontSave) {
                return true;
            }
        }
        return false;
    }

    public File getCurrentFile() {
        return currentFile;
    }

    private void setCurrentFile(File file) {
        this.currentFile = file;
        updateTitle();
    }

    private void setModified(boolean modified) {
        this.modified = modified;
        updateTitle();
    }

    private void updateTitle() {
        String title = "ZX Spectrum IDE";
        if (currentFile != null) {
            title += " - " + currentFile.getName();
        } else {
            title += " - " + i18n.get("label.untitled");
        }
        if (modified) {
            title += " *";
        }
        stage.setTitle(title);
    }

    private void updateLastDirectory(File file) {
        if (file != null && file.getParent() != null) {
            ConfigManager.getInstance().getConfig().setLastDirectory(file.getParent());
            ConfigManager.getInstance().saveConfig();
        }
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}