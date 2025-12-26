package com.lazyzxsoftware.zxspectrumide.editor;

import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import com.lazyzxsoftware.zxspectrumide.i18n.I18nManager;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

public class FileManager {

    private final Stage stage;
    private final CodeEditor codeEditor;
    private final I18nManager i18n;

    public FileManager(Stage stage, CodeEditor codeEditor) {
        this.stage = stage;
        this.codeEditor = codeEditor;
        this.i18n = I18nManager.getInstance();

        // Conectamos el listener de cierre de pestañas
        this.codeEditor.setOnCloseRequestConsumer(this::handleTabCloseRequest);
    }

    /**
     * Intenta cerrar todos los archivos abiertos.
     * Verifica primero si hay cambios sin guardar.
     * * @return true si se cerraron los archivos, false si el usuario canceló la operación.
     */
    public boolean closeAllFiles() {
        // 1. Reutilizamos la lógica de verificación que ya creamos
        // Esto pondrá el foco en la pestaña sucia y mostrará el modal
        if (!checkUnsavedChanges()) {
            return false; // El usuario canceló
        }

        // 2. Si llegamos aquí, es seguro cerrar todo
        codeEditor.closeAllTabs();
        return true;
    }

    /**
     * Busca y abre automáticamente el archivo principal del proyecto.
     * Reglas:
     * 1. Si solo hay un .asm en la raíz, lo abre.
     * 2. Si hay varios, busca 'main.asm' y lo abre.
     */
    public void detectAndOpenMainFile(File projectDir) {
        if (projectDir == null || !projectDir.exists()) return;

        // Filtramos solo los archivos .asm en la raíz del proyecto
        File[] asmFiles = projectDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".asm"));

        if (asmFiles == null || asmFiles.length == 0) return;

        if (asmFiles.length == 1) {
            // CASO 1: Solo hay un fichero .asm -> Lo abrimos directamente
            openFile(asmFiles[0]);
        } else {
            // CASO 2: Hay varios -> Buscamos "main.asm" (ignorando mayúsculas/minúsculas)
            for (File file : asmFiles) {
                if (file.getName().equalsIgnoreCase("main.asm")) {
                    openFile(file);
                    break; // Encontrado y abierto, terminamos
                }
            }
        }
    }

    public void newFile() {
        codeEditor.openTab(null, "");
    }

    public void openFile() {
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

    public boolean saveFile() {
        File currentFile = codeEditor.getCurrentFile();
        if (currentFile == null) {
            return saveFileAs();
        } else {
            return writeToFile(currentFile, codeEditor.getText());
        }
    }

    public boolean saveFileAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(i18n.get("dialog.save.title"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Assembler Files (*.asm)", "*.asm")
        );

        File currentFile = codeEditor.getCurrentFile();
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
            return writeToFile(file, codeEditor.getText());
        }
        return false;
    }

    /**
     * Verifica si el archivo actual tiene cambios antes de compilar.
     * @return true si se puede continuar (se guardó o se eligió ignorar), false si se cancela.
     */
    public boolean ensureSavedForCompilation() {
        // Obtenemos la pestaña activa
        Tab currentTab = codeEditor.getTabPane().getSelectionModel().getSelectedItem();
        if (currentTab == null) return true;

        // Verificamos si está modificada
        boolean isModified = (boolean) currentTab.getProperties().getOrDefault("modified", false);

        if (isModified) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(i18n.get("dialog.save.title"));
            alert.setHeaderText("Archivo con cambios pendientes");
            alert.setContentText("El archivo tiene cambios no guardados.\n¿Quieres guardarlos antes de compilar?");

            ButtonType btnSave = new ButtonType(i18n.get("button.save"));
            ButtonType btnContinue = new ButtonType("Compilar sin guardar");
            ButtonType btnCancel = new ButtonType(i18n.get("button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(btnSave, btnContinue, btnCancel);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == btnSave) {
                    // Intenta guardar. Si es "Sin título", saveFile() abrirá el diálogo "Guardar Como" automáticamente.
                    return saveFile();
                } else if (result.get() == btnContinue) {
                    return true; // Seguimos adelante con la versión en memoria
                } else {
                    return false; // Cancelar todo
                }
            }
            return false; // Se cerró el diálogo
        }
        return true; // No había cambios, adelante
    }

    /**
     * Revisa TODAS las pestañas en busca de cambios no guardados.
     * Se usa al intentar cerrar la aplicación completa.
     */
    public boolean checkUnsavedChanges() {
        for (Tab tab : codeEditor.getTabPane().getTabs()) {
            boolean isModified = (boolean) tab.getProperties().getOrDefault("modified", false);
            if (isModified) {
                codeEditor.getTabPane().getSelectionModel().select(tab);
                if (!confirmClose(tab)) {
                    return false;
                }
            }
        }
        return true;
    }

    // --- MÉTODO AÑADIDO PARA SOLUCIONAR EL ERROR EN MAIN ---
    public File getCurrentFile() {
        return codeEditor.getCurrentFile();
    }

    private void loadFile(File file) {
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            codeEditor.openTab(file, content);
        } catch (IOException e) {
            showError("Error loading file", e.getMessage());
        }
    }

    private boolean writeToFile(File file, String content) {
        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
            codeEditor.markCurrentTabAsSaved(file);
            return true;
        } catch (IOException e) {
            showError("Error saving file", e.getMessage());
            return false;
        }
    }

    // --- Manejo de eventos de cierre ---

    private void handleTabCloseRequest(Tab tab, Event event) {
        boolean isModified = (boolean) tab.getProperties().getOrDefault("modified", false);
        if (isModified) {
            if (!confirmClose(tab)) {
                event.consume();
            }
        }
    }

    private boolean confirmClose(Tab tab) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(i18n.get("dialog.unsaved.title"));
        alert.setHeaderText(i18n.get("dialog.unsaved.header"));

        // CORRECCIÓN: Usamos la traducción dinámica con el nombre del archivo
        String fileName = tab.getText().replace("*", "");
        String content = java.text.MessageFormat.format(i18n.get("dialog.unsaved.content"), fileName);
        alert.setContentText(content);

        ButtonType btnSave = new ButtonType(i18n.get("button.save"));
        ButtonType btnDontSave = new ButtonType(i18n.get("button.dontsave"));
        ButtonType btnCancel = new ButtonType(i18n.get("button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnSave, btnDontSave, btnCancel);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == btnSave) {
                CodeArea area = (CodeArea) tab.getContent();
                File file = (File) tab.getUserData();
                boolean saved;
                if (file == null) {
                    saved = saveFileAs();
                } else {
                    saved = writeToFile(file, area.getText());
                }
                return saved;
            } else if (result.get() == btnDontSave) {
                return true;
            }
        }
        return false;
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

    // Este método permite abrir un archivo directamente sin cuadro de diálogo
    public void openFile(File file) {
        if (file != null && file.exists() && file.isFile()) {
            loadFile(file);
            updateLastDirectory(file);
        }
    }
}