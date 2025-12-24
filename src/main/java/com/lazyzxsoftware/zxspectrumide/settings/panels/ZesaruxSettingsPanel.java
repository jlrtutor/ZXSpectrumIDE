package com.lazyzxsoftware.zxspectrumide.settings.panels;

import com.lazyzxsoftware.zxspectrumide.config.AppConfig;
import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import com.lazyzxsoftware.zxspectrumide.settings.SettingsPanel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// CORRECCIÓN: Ahora extiende SettingsPanel (que es un VBox)
public class ZesaruxSettingsPanel extends SettingsPanel {

    private final TextField pathField;
    private final ComboBox<String> machineCombo;
    private final Label versionLabel;

    public ZesaruxSettingsPanel() {
        setPadding(new Insets(10)); // Padding del VBox contenedor

        // Creamos el GridPane interno para el diseño avanzado
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        AppConfig config = ConfigManager.getInstance().getConfig();

        // --- FILA 0: Ruta del Ejecutable ---
        Label pathLabel = new Label("Ejecutable ZEsarUX:");
        pathField = new TextField(config.getEmulatorPath());
        pathField.setPromptText("Ruta al ejecutable de zesarux...");

        pathField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(pathField, Priority.ALWAYS);

        Button browseButton = new Button("...");
        browseButton.setOnAction(e -> browseFile());

        // --- FILA 1: Label de Versión ---
        versionLabel = new Label();
        versionLabel.setStyle("-fx-font-size: 11px; -fx-font-style: italic;");

        Button clearButton = new Button("X");
        clearButton.setTooltip(new Tooltip("Limpiar selección"));
        clearButton.setOnAction(e -> {
            pathField.clear();
            versionLabel.setText("");
            validateVersion(null);
        });

        HBox pathBox = new HBox(5, pathField, browseButton, clearButton);
        HBox.setHgrow(pathBox, Priority.ALWAYS);

        // --- FILA 2: Modelo de Máquina ---
        Label machineLabel = new Label("Modelo por defecto:");
        machineCombo = new ComboBox<>();
        machineCombo.getItems().add("");
        machineCombo.getItems().addAll("Spectrum 48k", "Spectrum 128k", "Spectrum +3", "ZX81");

        String currentMachine = config.getEmulatorMachine();
        if (currentMachine != null && !currentMachine.isEmpty()) {
            machineCombo.setValue(currentMachine);
        } else {
            machineCombo.getSelectionModel().selectFirst();
        }
        machineCombo.setMaxWidth(Double.MAX_VALUE);

        // --- COLOCACIÓN EN GRID ---
        grid.add(pathLabel, 0, 0);
        grid.add(pathBox, 1, 0);
        grid.add(versionLabel, 1, 1);
        grid.add(machineLabel, 0, 2);
        grid.add(machineCombo, 1, 2);

        // Añadimos el grid al VBox padre (SettingsPanel)
        this.getChildren().add(grid);

        // Validación inicial
        if (!pathField.getText().isEmpty()) {
            validateVersion(pathField.getText());
        }

        pathField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                versionLabel.setText("");
            } else {
                validateVersion(newVal);
            }
        });
    }

    private void browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar ZEsarUX");
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            pathField.setText(file.getAbsolutePath());
            validateVersion(file.getAbsolutePath());
        }
    }

    private void validateVersion(String path) {
        if (path == null || path.isBlank()) return;

        File file = new File(path);
        if (!file.exists() || !file.canExecute()) {
            versionLabel.setText("⚠️ El archivo no existe o no es ejecutable");
            versionLabel.setTextFill(Color.RED);
            return;
        }

        versionLabel.setText("⌛ Verificando versión...");
        versionLabel.setTextFill(Color.GRAY);

        CompletableFuture.runAsync(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(path, "--version");
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                String detectedVersion = null;

                Pattern pattern = Pattern.compile("ZEsarUX ([0-9]+\\.[0-9]+)");

                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        detectedVersion = matcher.group(1);
                        break;
                    }
                }

                String finalVersion = detectedVersion;
                Platform.runLater(() -> {
                    if (finalVersion != null) {
                        versionLabel.setText("✅ Detectado: ZEsarUX v" + finalVersion);
                        versionLabel.setTextFill(Color.GREEN);
                    } else {
                        versionLabel.setText("⚠️ No parece ser ZEsarUX (Output desconocido)");
                        versionLabel.setTextFill(Color.ORANGE);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    versionLabel.setText("❌ Error al ejecutar: " + e.getMessage());
                    versionLabel.setTextFill(Color.RED);
                });
            }
        });
    }

    // --- MÉTODOS OBLIGATORIOS DE SETTINGSPANEL ---

    @Override
    public void loadSettings() {
        // La carga ya se hace en el constructor, pero si necesitas recargar:
        AppConfig config = ConfigManager.getInstance().getConfig();
        pathField.setText(config.getEmulatorPath());
        machineCombo.setValue(config.getEmulatorMachine());
    }

    @Override
    public void saveSettings() {
        AppConfig config = ConfigManager.getInstance().getConfig();
        config.setEmulatorPath(pathField.getText());
        config.setEmulatorMachine(machineCombo.getValue());
    }
}