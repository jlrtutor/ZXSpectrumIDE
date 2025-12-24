package com.lazyzxsoftware.zxspectrumide.settings.panels;

import com.lazyzxsoftware.zxspectrumide.config.AppConfig;
import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import com.lazyzxsoftware.zxspectrumide.settings.SettingsPanel;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.scene.layout.HBox;
import java.io.File;

public class PasmoSettingsPanel extends SettingsPanel {

    private final TextField pathField;
    private final TextField buildPathField;
    private final ComboBox<String> formatCombo;
    private final CheckBox debugCheck;

    public PasmoSettingsPanel() {
        Label pathLabel = new Label("Ruta del ejecutable (pasmo):");
        pathField = new TextField();
        pathField.setPromptText("Ej: /usr/bin/pasmo o C:\\Tools\\pasmo.exe");

        pathField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(pathField, Priority.ALWAYS);

        Button browseBtn = new Button("...");
        browseBtn.setOnAction(e -> browsePasmo());
        HBox pathBox = new HBox(10, pathField, browseBtn);

        // --- NUEVO SECCIÓN: CARPETA DE SALIDA ---
        Label buildLabel = new Label("Carpeta de Salida (Opcional, por defecto 'build/'):");
        buildPathField = new TextField();
        buildPathField.setPromptText("Dejar vacío para usar carpeta relativa 'build'");
        Button browseBuildBtn = new Button("...");
        browseBuildBtn.setOnAction(e -> browseBuildDir());
        HBox buildBox = new HBox(10, buildPathField, browseBuildBtn);

        Label formatLabel = new Label("Formato de Salida:");
        formatCombo = new ComboBox<>();
        // CAMBIO AQUÍ: TAP -> TAPBAS
        formatCombo.getItems().addAll("TAPBAS (Cargador BASIC)", "TAP (Solo Código)", "Binario Raw", "Hex");

        debugCheck = new CheckBox("Generar información de depuración (--public, -d)");

        this.getChildren().addAll(pathLabel, pathBox, buildLabel, buildBox, formatLabel, formatCombo, debugCheck);
    }

    private void browsePasmo() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(this.getScene().getWindow());
        if (file != null) pathField.setText(file.getAbsolutePath());
    }

    // Nuevo método
    private void browseBuildDir() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Seleccionar carpeta de salida");
        File dir = dirChooser.showDialog(this.getScene().getWindow());
        if (dir != null) buildPathField.setText(dir.getAbsolutePath());
    }

    @Override
    public void loadSettings() {
        AppConfig config = ConfigManager.getInstance().getConfig();

        pathField.setText(config.getPasmoPath());
        buildPathField.setText(config.getBuildPath());
        debugCheck.setSelected(config.isPasmoDebug());

        String savedFormat = config.getPasmoFormat();
        if (savedFormat == null) savedFormat = "tapbas";

        switch (savedFormat) {
            case "tap": formatCombo.getSelectionModel().select(1); break;
            case "bin": formatCombo.getSelectionModel().select(2); break;
            case "hex": formatCombo.getSelectionModel().select(3); break;
            default: formatCombo.getSelectionModel().select(0); // Selecciona TAPBAS por defecto
        }
    }

    @Override
    public void saveSettings() {
        AppConfig config = ConfigManager.getInstance().getConfig();

        config.setPasmoPath(pathField.getText());
        config.setBuildPath(buildPathField.getText());
        config.setPasmoDebug(debugCheck.isSelected());

        String selected = formatCombo.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // La lógica de detección sigue funcionando porque usamos "contains"
            // o cae en el else (tapbas)
            if (selected.contains("Solo Código")) config.setPasmoFormat("tap");
            else if (selected.contains("Binario")) config.setPasmoFormat("bin");
            else if (selected.contains("Hex")) config.setPasmoFormat("hex");
            else config.setPasmoFormat("tapbas");
        }
    }
}