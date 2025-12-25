package com.lazyzxsoftware.zxspectrumide.utils;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.Taskbar;
import java.awt.Toolkit;
import java.net.URL;

public class PlatformUtils {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isMac() {
        return OS.contains("mac");
    }

    public static boolean isWindows() {
        return OS.contains("win");
    }

    /**
     * Establece el icono de la aplicación de forma compatible con todos los sistemas operativos.
     * @param stage La ventana principal (para Windows/Linux).
     * @param iconPath La ruta absoluta al recurso del icono (ej: "/com/miApp/icon.png").
     */
    public static void setAppIcon(Stage stage, String iconPath) {
        try {
            // 1. Cargar el icono como recurso de JavaFX
            var iconStream = PlatformUtils.class.getResourceAsStream(iconPath);
            if (iconStream == null) {
                System.err.println("❌ PlatformUtils: No se encuentra el icono en: " + iconPath);
                return;
            }

            // Establecer icono en la ventana (Estándar para Windows y Linux)
            // En Mac esto no tiene efecto visual en la ventana, pero es buena práctica.
            Image fxIcon = new Image(iconStream);
            stage.getIcons().add(fxIcon);

            // 2. Tratamiento especial para macOS (Icono del Dock)
            if (isMac()) {
                setMacDockIcon(iconPath);
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error estableciendo el icono de la app: " + e.getMessage());
        }
    }

    private static void setMacDockIcon(String iconPath) {
        try {
            // macOS requiere usar java.awt.Taskbar
            // Necesitamos la URL, no el Stream, para AWT
            URL iconUrl = PlatformUtils.class.getResource(iconPath);

            if (iconUrl != null && Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();
                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    java.awt.Image awtIcon = Toolkit.getDefaultToolkit().getImage(iconUrl);
                    taskbar.setIconImage(awtIcon);
                    System.out.println("🍎 macOS: Icono del Dock actualizado.");
                }
            }
        } catch (Exception e) {
            // A veces falla en entornos de desarrollo sin firma, no es crítico
            System.err.println("⚠️ No se pudo establecer el icono del Dock en Mac: " + e.getMessage());
        }
    }
}