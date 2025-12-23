package com.lazyzxsoftware.zxspectrumide.ui;

import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import com.lazyzxsoftware.zxspectrumide.settings.SettingsPanel;
import com.lazyzxsoftware.zxspectrumide.settings.panels.PasmoSettingsPanel;
import com.lazyzxsoftware.zxspectrumide.settings.panels.ZesaruxSettingsPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SettingsDialog {

    // Método estático como espera tu Main.java
    public static void show(Scene ownerScene) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Configuración");
        if (ownerScene != null) stage.initOwner(ownerScene.getWindow());

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // --- Pestaña 1: Compilador ---
        Tab compilerTab = new Tab("Compilador");
        VBox compilerContainer = new VBox(10);
        compilerContainer.setPadding(new Insets(15));

        // Selector de compilador (Futuro: SjASMPlus)
        Label selectCompLabel = new Label("Seleccionar Compilador:");
        ComboBox<String> compilerSelector = new ComboBox<>();
        compilerSelector.getItems().add("Pasmo");
        compilerSelector.getSelectionModel().selectFirst();

        BorderPane compilerDynamicArea = new BorderPane();
        SettingsPanel pasmoPanel = new PasmoSettingsPanel();
        pasmoPanel.loadSettings(); // Cargar datos actuales
        compilerDynamicArea.setCenter(pasmoPanel);

        compilerContainer.getChildren().addAll(selectCompLabel, compilerSelector, new Separator(), compilerDynamicArea);
        compilerTab.setContent(compilerContainer);

        // --- Pestaña 2: Emulador ---
        Tab emulatorTab = new Tab("Emulador");
        VBox emulatorContainer = new VBox(10);
        emulatorContainer.setPadding(new Insets(15));

        BorderPane emulatorDynamicArea = new BorderPane();
        SettingsPanel zesaruxPanel = new ZesaruxSettingsPanel();
        zesaruxPanel.loadSettings(); // Cargar datos actuales
        emulatorDynamicArea.setCenter(zesaruxPanel);

        emulatorContainer.getChildren().addAll(new Label("Configuración ZEsarUX:"), new Separator(), emulatorDynamicArea);
        emulatorTab.setContent(emulatorContainer);

        tabPane.getTabs().addAll(compilerTab, emulatorTab);

        // --- Botón Guardar ---
        Button btnSave = new Button("Guardar y Cerrar");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setOnAction(e -> {
            // 1. Guardar cambios de UI a Memoria
            pasmoPanel.saveSettings();
            zesaruxPanel.saveSettings();

            // 2. Guardar Memoria a Disco (CRÍTICO)
            ConfigManager.getInstance().saveConfig();

            stage.close();
        });

        VBox root = new VBox(10, tabPane, new Separator(), btnSave);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 600, 500);
        // Intentar cargar estilos
        if (ownerScene != null && !ownerScene.getStylesheets().isEmpty()) {
            scene.getStylesheets().add(ownerScene.getStylesheets().get(0));
        }

        stage.setScene(scene);
        stage.showAndWait();
    }
}