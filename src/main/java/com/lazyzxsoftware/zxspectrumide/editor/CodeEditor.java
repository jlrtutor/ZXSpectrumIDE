package com.lazyzxsoftware.zxspectrumide.editor;

import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import com.lazyzxsoftware.zxspectrumide.i18n.I18nManager;
import javafx.event.Event;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import javafx.scene.Node;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.File;
import java.time.Duration;
import java.util.function.BiConsumer;

/**
 * Editor de código con soporte para múltiples pestañas.
 */
public class CodeEditor extends BorderPane {

    private final TabPane tabPane;
    private final ConfigManager configManager;
    private BiConsumer<Tab, Event> onCloseRequestConsumer; // Callback para el FileManager

    public CodeEditor() {
        configManager = ConfigManager.getInstance();

        // 1. Inicializamos el TabPane
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        // Añadimos el TabPane al centro del layout
        setCenter(tabPane);

        System.out.println("Editor de código (Multi-Tab) inicializado");
    }

    /**
     * Crea una nueva pestaña con un archivo y contenido.
     */
    public void openTab(File file, String content) {
        // Verificar si ya está abierto para no duplicar
        for (Tab t : tabPane.getTabs()) {
            File tabFile = (File) t.getUserData();
            if (tabFile != null && tabFile.equals(file)) {
                tabPane.getSelectionModel().select(t);
                return;
            }
        }

        // Crear el nombre de la pestaña
        String title = (file != null) ? file.getName() : I18nManager.getInstance().get("label.untitled");
        Tab tab = new Tab(title);
        tab.setUserData(file); // Guardamos la referencia al archivo

        // Crear el CodeArea para esta pestaña
        CodeArea codeArea = createCodeArea();
        codeArea.replaceText(content);
        codeArea.getUndoManager().forgetHistory(); // Limpiamos historial al abrir

        // Listener para el asterisco (*) de modificado
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            if (!tab.getText().endsWith("*")) {
                tab.setText(tab.getText() + "*");
                // Marcar propiedad interna para saber que está sucio
                tab.getProperties().put("modified", true);
            }
        });

        // Configurar el comportamiento al intentar cerrar la pestaña
        tab.setOnCloseRequest(event -> {
            if (onCloseRequestConsumer != null) {
                // Delegamos la decisión al FileManager
                onCloseRequestConsumer.accept(tab, event);
            }
        });

        // Configurar el resaltado inicial
        if (!content.isEmpty()) {
            codeArea.setStyleSpans(0, Z80Lexer.computeHighlighting(content));
        }

        tab.setContent(codeArea);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    /**
     * Método auxiliar para configurar un nuevo CodeArea con los estilos
     */
    private CodeArea createCodeArea() {
        CodeArea area = new CodeArea();

        // Configuración visual (fuente, tamaño, números de línea)
        if (configManager.getConfig().isShowLineNumbers()) {
            area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        }

        String fontFamily = configManager.getConfig().getEditorFontFamily();
        double fontSize = configManager.getConfig().getEditorFontSize();
        area.setStyle(String.format("-fx-font-family: '%s'; -fx-font-size: %.0fpx;", fontFamily, fontSize));

        HBox.setHgrow(area, Priority.ALWAYS);

        // Resaltado de sintaxis dinámico
        area.multiPlainChanges()
                .successionEnds(Duration.ofMillis(50))
                .subscribe(ignore -> area.setStyleSpans(0, Z80Lexer.computeHighlighting(area.getText())));

        return area;
    }

    // --- Métodos de utilidad para interactuar con la pestaña activa ---

    public CodeArea getActiveCodeArea() {
        Tab selected = tabPane.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getContent() instanceof CodeArea) {
            return (CodeArea) selected.getContent();
        }
        return null;
    }

    public String getText() {
        CodeArea area = getActiveCodeArea();
        return area != null ? area.getText() : "";
    }

    public File getCurrentFile() {
        Tab selected = tabPane.getSelectionModel().getSelectedItem();
        if (selected != null) {
            return (File) selected.getUserData();
        }
        return null;
    }

    /**
     * Marca la pestaña actual como guardada (quita el asterisco)
     */
    public void markCurrentTabAsSaved(File file) {
        Tab selected = tabPane.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setText(file.getName());
            selected.setUserData(file); // Actualizamos el archivo por si era "Save As"
            selected.getProperties().put("modified", false);
        }
    }

    public void setOnCloseRequestConsumer(BiConsumer<Tab, Event> consumer) {
        this.onCloseRequestConsumer = consumer;
    }

    /**
     * Método getter necesario para que el FileManager pueda acceder a las pestañas.
     */
    public TabPane getTabPane() {
        return tabPane;
    }

    // --- Métodos de Edición (Delegados a la pestaña activa) ---

    public void undo() {
        CodeArea area = getActiveCodeArea();
        if (area != null) area.undo();
    }

    public void redo() {
        CodeArea area = getActiveCodeArea();
        if (area != null) area.redo();
    }

    public void cut() {
        CodeArea area = getActiveCodeArea();
        if (area != null) area.cut();
    }

    public void copy() {
        CodeArea area = getActiveCodeArea();
        if (area != null) area.copy();
    }

    public void paste() {
        CodeArea area = getActiveCodeArea();
        if (area != null) area.paste();
    }

    public void selectAll() {
        CodeArea area = getActiveCodeArea();
        if (area != null) area.selectAll();
    }

    /**
     * Mueve el cursor a la línea especificada en la pestaña ACTIVA.
     */
    public void goToLine(int lineNumber) {
        // 1. Obtener la pestaña seleccionada actualmente
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab == null) return;

        // 2. Extraer el CodeArea de dentro de la pestaña
        // (Recordemos que el CodeArea suele estar dentro de un VirtualizedScrollPane)
        Node content = currentTab.getContent();
        CodeArea activeCodeArea = null;

        if (content instanceof VirtualizedScrollPane) {
            // El caso normal: sacamos el contenido del scroll pane
            var scrollPane = (VirtualizedScrollPane<?>) content;
            if (scrollPane.getContent() instanceof CodeArea) {
                activeCodeArea = (CodeArea) scrollPane.getContent();
            }
        } else if (content instanceof CodeArea) {
            // Caso raro: si estuviera el editor directo sin scroll
            activeCodeArea = (CodeArea) content;
        }

        // Si no encontramos un editor válido, salimos
        if (activeCodeArea == null) return;

        // 3. Lógica de navegación sobre el editor activo
        int index = lineNumber - 1; // RichTextFX usa base-0

        if (index < 0) index = 0;

        // Evitar ir más allá del final
        int totalLines = activeCodeArea.getParagraphs().size();
        if (index >= totalLines) index = totalLines - 1;

        try {
            activeCodeArea.moveTo(index, 0);       // Mover cursor
            activeCodeArea.requestFollowCaret();   // Scroll hasta el cursor
            activeCodeArea.requestFocus();         // Dar foco
            activeCodeArea.selectLine();           // Resaltar línea
        } catch (Exception e) {
            System.err.println("Error navegando a línea: " + lineNumber);
        }
    }

    /**
     * Cierra todas las pestañas abiertas sin preguntar (la validación se hace antes).
     */
    public void closeAllTabs() {
        tabPane.getTabs().clear();
    }
}