package com.lazyzxsoftware.zxspectrumide.ui;

import com.lazyzxsoftware.zxspectrumide.editor.CodeEditor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.fxmisc.richtext.CodeArea;

public class FindReplaceDialog {

    private final Stage stage;
    private final CodeEditor codeEditor;
    private final TextField txtFind;
    private final TextField txtReplace;
    private final CheckBox chkCaseSensitive;
    private final Label lblStatus;

    public FindReplaceDialog(CodeEditor codeEditor) {
        this.codeEditor = codeEditor;

        stage = new Stage();
        stage.initStyle(StageStyle.UTILITY); // Ventana pequeña tipo herramienta
        stage.setAlwaysOnTop(true);
        stage.setTitle("Buscar / Reemplazar");

        // Componentes
        txtFind = new TextField();
        txtFind.setPromptText("Texto a buscar...");

        txtReplace = new TextField();
        txtReplace.setPromptText("Reemplazar con...");

        chkCaseSensitive = new CheckBox("Coincidir mayúsculas");

        lblStatus = new Label("");
        lblStatus.setStyle("-fx-text-fill: red;");

        // Botones
        Button btnFindNext = new Button("Buscar Siguiente");
        btnFindNext.setDefaultButton(true); // Se activa con ENTER
        btnFindNext.setOnAction(e -> findNext());

        Button btnReplace = new Button("Reemplazar");
        btnReplace.setOnAction(e -> replace());

        Button btnReplaceAll = new Button("Reemplazar Todo");
        btnReplaceAll.setOnAction(e -> replaceAll());

        Button btnClose = new Button("Cerrar");
        btnClose.setOnAction(e -> stage.close());

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.add(new Label("Buscar:"), 0, 0);
        grid.add(txtFind, 1, 0);
        grid.add(new Label("Reemplazar:"), 0, 1);
        grid.add(txtReplace, 1, 1);
        grid.add(chkCaseSensitive, 1, 2);

        HBox buttonBox = new HBox(10, btnFindNext, btnReplace, btnReplaceAll, btnClose);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(10, grid, lblStatus, buttonBox);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root);
        stage.setScene(scene);

        // Atajos de teclado dentro del diálogo
        root.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) stage.close();
        });
    }

    public void show(boolean replaceMode) {
        txtReplace.setDisable(!replaceMode);
        if (replaceMode) {
            stage.setTitle("Buscar y Reemplazar");
        } else {
            stage.setTitle("Buscar");
        }

        // Intentar pre-rellenar con la selección actual del editor
        CodeArea area = codeEditor.getActiveCodeArea();
        if (area != null) {
            String selected = area.getSelectedText();
            if (selected != null && !selected.isEmpty()) {
                txtFind.setText(selected);
            }
        }

        stage.show();
        txtFind.requestFocus();
    }

    private void findNext() {
        CodeArea area = codeEditor.getActiveCodeArea();
        if (area == null) return;

        String text = area.getText();
        String query = txtFind.getText();
        if (query.isEmpty()) return;

        if (!chkCaseSensitive.isSelected()) {
            text = text.toLowerCase();
            query = query.toLowerCase();
        }

        int currentCaret = area.getCaretPosition();
        int index = text.indexOf(query, currentCaret);

        // Si no lo encuentra, buscar desde el principio (Wrap around)
        if (index == -1) {
            index = text.indexOf(query);
            if (index != -1) {
                lblStatus.setText("Búsqueda reiniciada desde el principio.");
            } else {
                lblStatus.setText("No se encontraron coincidencias.");
            }
        } else {
            lblStatus.setText("");
        }

        if (index != -1) {
            area.moveTo(index);
            area.selectRange(index, index + txtFind.getText().length());
            area.requestFollowCaret();
        }
    }

    private void replace() {
        CodeArea area = codeEditor.getActiveCodeArea();
        if (area == null) return;

        // Verificar si lo que está seleccionado coincide con la búsqueda
        String currentSelection = area.getSelectedText();
        String query = txtFind.getText();

        if (!chkCaseSensitive.isSelected()) {
            currentSelection = currentSelection.toLowerCase();
            query = query.toLowerCase();
        }

        if (currentSelection.equals(query)) {
            // Reemplazar selección
            area.replaceSelection(txtReplace.getText());
            // Buscar el siguiente automáticamente
            findNext();
        } else {
            // Si no está seleccionado, buscar primero
            findNext();
        }
    }

    private void replaceAll() {
        CodeArea area = codeEditor.getActiveCodeArea();
        if (area == null) return;

        String text = area.getText();
        String query = txtFind.getText();
        String replacement = txtReplace.getText();

        if (query.isEmpty()) return;

        // Nota: Para hacerlo robusto con Case Insensitive en replaceAll sería más complejo,
        // aquí hacemos la versión estándar simple.
        String newText = text.replace(query, replacement);

        if (!newText.equals(text)) {
            area.replaceText(newText);
            lblStatus.setText("Reemplazo masivo completado.");
        } else {
            lblStatus.setText("Nada que reemplazar.");
        }
    }
}