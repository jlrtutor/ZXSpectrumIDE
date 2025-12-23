package com.lazyzxsoftware.zxspectrumide.settings;

import javafx.scene.layout.VBox;

/**
 * Clase base para todos los paneles de configuración específicos (Pasmo, ZEsarUX, etc.)
 */
public abstract class SettingsPanel extends VBox {

    public SettingsPanel() {
        this.setSpacing(10);
    }

    /**
     * Carga los valores actuales de AppConfig a los controles de la UI viaja
     */
    public abstract void loadSettings();

    /**
     * Guarda los valores de los controles de la UI en AppConfig
     */
    public abstract void saveSettings();
}