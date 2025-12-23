package com.lazyzxsoftware.zxspectrumide.settings.panels;

import com.lazyzxsoftware.zxspectrumide.config.AppConfig;
import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import com.lazyzxsoftware.zxspectrumide.settings.SettingsPanel;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import java.io.File;

public class ZesaruxSettingsPanel extends SettingsPanel {
    private final TextField pathField;
    private final ComboBox<String> machineCombo;

    public ZesaruxSettingsPanel() {
        Label pathLabel = new Label("Ruta del ejecutable (zesarux):");
        pathField = new TextField();
        Button browseBtn = new Button("...");
        browseBtn.setOnAction(e -> browseZesarux());
        HBox pathBox = new HBox(10, pathField, browseBtn);

        Label machineLabel = new Label("Modelo de Spectrum:");
        machineCombo = new ComboBox<>();
        machineCombo.getItems().addAll("Spectrum 48k", "Spectrum 128k", "Spectrum +3");

        this.getChildren().addAll(pathLabel, pathBox, machineLabel, machineCombo);
    }

    private void browseZesarux() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) pathField.setText(file.getAbsolutePath());
    }

    @Override
    public void loadSettings() {
        AppConfig config = ConfigManager.getInstance().getConfig();

        // 1. Cargar Ruta
        pathField.setText(config.getEmulatorPath());

        // 2. Cargar Modelo (Seleccionar el valor guardado)
        String savedMachine = config.getEmulatorMachine();
        if (savedMachine != null && !savedMachine.isEmpty()) {
            machineCombo.setValue(savedMachine);
        } else {
            machineCombo.getSelectionModel().select(0); // Default 48k
        }
    }

    @Override
    public void saveSettings() {
        AppConfig config = ConfigManager.getInstance().getConfig();

        // 1. Guardar Ruta
        config.setEmulatorPath(pathField.getText());

        // 2. Guardar Modelo
        if (machineCombo.getValue() != null) {
            config.setEmulatorMachine(machineCombo.getValue());
        }
    }
}