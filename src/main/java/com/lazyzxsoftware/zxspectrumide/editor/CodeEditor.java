package com.lazyzxsoftware.zxspectrumide.editor;

import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import com.lazyzxsoftware.zxspectrumide.i18n.I18nManager;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Editor de código basado en RichTextFX
 * Para edición de código Z80 Assembly
 */
public class CodeEditor extends BorderPane {

    private final CodeArea codeArea;
    private final ConfigManager configManager;
    private File currentFile;
    private boolean modified;

    public CodeEditor() {
        configManager = ConfigManager.getInstance();

        // Crear el área de código
        codeArea = new CodeArea();

        // Configurar el editor
        setupEditor();

        // Añadir al layout
        setCenter(codeArea);

        // Listener para detectar cambios
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            setModified(true);
        });

        System.out.println("Editor de código inicializado");
    }

    private void setupEditor() {
        // Numeración de líneas
        if (configManager.getConfig().isShowLineNumbers()) {
            codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        }

        // Fuente del editor
        String fontFamily = configManager.getConfig().getEditorFontFamily();
        double fontSize = configManager.getConfig().getEditorFontSize();
        codeArea.setStyle(String.format(
                "-fx-font-family: '%s'; -fx-font-size: %.0fpx;",
                fontFamily, fontSize
        ));

        // Configurar tabulación
        int tabSize = configManager.getConfig().getTabSize();
        // RichTextFX no tiene setTabSize directo, lo manejaremos con eventos después

        // Hacer el área expandible
        HBox.setHgrow(codeArea, Priority.ALWAYS);

        // Placeholder cuando está vacío
        //codeArea.setPromptText("; Escribe tu código Z80 Assembly aquí...");

        Label placeholder = new Label(I18nManager.getInstance().get("editor.placeholder"));
        placeholder.getStyleClass().add("code-placeholder");

        codeArea.setPlaceholder(placeholder);
    }

    /**
     * Obtiene el texto del editor
     */
    public String getText() {
        return codeArea.getText();
    }

    /**
     * Establece el texto del editor
     */
    public void setText(String text) {
        codeArea.replaceText(text);
        setModified(false);
    }

    /**
     * Limpia el editor
     */
    public void clear() {
        codeArea.clear();
        setModified(false);
        currentFile = null;
    }

    /**
     * Deshacer
     */
    public void undo() {
        codeArea.undo();
    }

    /**
     * Rehacer
     */
    public void redo() {
        codeArea.redo();
    }

    /**
     * Verifica si se puede deshacer
     */
    public boolean canUndo() {
        return codeArea.isUndoAvailable();
    }

    /**
     * Verifica si se puede rehacer
     */
    public boolean canRedo() {
        return codeArea.isRedoAvailable();
    }

    /**
     * Cortar
     */
    public void cut() {
        codeArea.cut();
    }

    /**
     * Copiar
     */
    public void copy() {
        codeArea.copy();
    }

    /**
     * Pegar
     */
    public void paste() {
        codeArea.paste();
    }

    /**
     * Seleccionar todo
     */
    public void selectAll() {
        codeArea.selectAll();
    }

    /**
     * Abre un archivo
     */
    public void openFile(File file) throws IOException {
        if (file != null && file.exists()) {
            String content = Files.readString(file.toPath());
            setText(content);
            this.currentFile = file;
            setModified(false);
            System.out.println("Archivo abierto: " + file.getName());
        }
    }

    /**
     * Guarda el archivo actual
     */
    public void saveFile() throws IOException {
        if (currentFile != null) {
            saveFile(currentFile);
        } else {
            throw new IOException("No hay archivo actual para guardar");
        }
    }

    /**
     * Guarda en un archivo específico
     */
    public void saveFile(File file) throws IOException {
        if (file != null) {
            Files.writeString(file.toPath(), getText());
            this.currentFile = file;
            setModified(false);
            System.out.println("Archivo guardado: " + file.getName());
        }
    }

    /**
     * Obtiene el archivo actual
     */
    public File getCurrentFile() {
        return currentFile;
    }

    /**
     * Verifica si el documento ha sido modificado
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Establece el estado de modificación
     */
    private void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * Obtiene el número de línea actual
     */
    public int getCurrentLine() {
        return codeArea.getCurrentParagraph() + 1; // +1 porque comienza en 0
    }

    /**
     * Obtiene el número de columna actual
     */
    public int getCurrentColumn() {
        return codeArea.getCaretColumn() + 1;
    }

    /**
     * Va a una línea específica
     */
    public void goToLine(int lineNumber) {
        if (lineNumber > 0 && lineNumber <= codeArea.getParagraphs().size()) {
            codeArea.moveTo(lineNumber - 1, 0);
            codeArea.requestFollowCaret();
        }
    }

    /**
     * Obtiene el CodeArea subyacente para personalizaciones avanzadas
     */
    public CodeArea getCodeArea() {
        return codeArea;
    }

    /**
     * Actualiza la configuración del editor
     */
    public void updateSettings() {
        setupEditor();
    }
}