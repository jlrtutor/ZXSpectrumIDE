package com.lazyzxsoftware.zxspectrumide.editor;

import com.lazyzxsoftware.zxspectrumide.i18n.I18nManager;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Gestor de operaciones de archivo
 */
public class FileManager {

    private final Stage owner;
    private final I18nManager i18n;
    private File lastDirectory;

    public FileManager(Stage owner) {
        this.owner = owner;
        this.i18n = I18nManager.getInstance();

        // Directorio inicial (home del usuario)
        this.lastDirectory = new File(System.getProperty("user.home"));
    }

    /**
     * Muestra el diálogo para abrir un archivo
     */
    public File showOpenDialog() {
        FileChooser fileChooser = createFileChooser(i18n.get("file.open_dialog.title"));
        File file = fileChooser.showOpenDialog(owner);

        if (file != null) {
            lastDirectory = file.getParentFile();
        }

        return file;
    }

    /**
     * Muestra el diálogo para guardar un archivo
     */
    public File showSaveDialog() {
        FileChooser fileChooser = createFileChooser(i18n.get("file.save_dialog.title"));
        File file = fileChooser.showSaveDialog(owner);

        if (file != null) {
            lastDirectory = file.getParentFile();

            // Asegurar extensión .asm
            if (!file.getName().toLowerCase().endsWith(".asm")) {
                file = new File(file.getAbsolutePath() + ".asm");
            }
        }

        return file;
    }

    /**
     * Crea un FileChooser configurado
     */
    private FileChooser createFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialDirectory(lastDirectory);

        // Filtros de archivo
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(i18n.get("file.asm_files"), "*.asm"),
                new FileChooser.ExtensionFilter(i18n.get("file.all_files"), "*.*")
        );

        return fileChooser;
    }

    /**
     * Obtiene el último directorio usado
     */
    public File getLastDirectory() {
        return lastDirectory;
    }

    /**
     * Establece el último directorio usado
     */
    public void setLastDirectory(File directory) {
        if (directory != null && directory.isDirectory()) {
            this.lastDirectory = directory;
        }
    }
}