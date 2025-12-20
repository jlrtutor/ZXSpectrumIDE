package com.lazyzxsoftware.zxspectrumide.theme;

import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de temas de la aplicación
 * Permite cambiar entre tema claro y oscuro
 */
public class ThemeManager {

    private static ThemeManager instance;
    private final List<Scene> registeredScenes;
    private Theme currentTheme;

    // Rutas de los archivos CSS
    private static final String LIGHT_THEME_CSS = "/com/lazyzxsoftware/zxspectrumide/themes/light.css";
    private static final String DARK_THEME_CSS = "/com/lazyzxsoftware/zxspectrumide/themes/deep-ocean.css";


    public enum Theme {
        LIGHT("light", "Tema Claro", LIGHT_THEME_CSS),
        DARK("dark", "Tema Oscuro", DARK_THEME_CSS);

        private final String id;
        private final String displayName;
        private final String cssPath;

        Theme(String id, String displayName, String cssPath) {
            this.id = id;
            this.displayName = displayName;
            this.cssPath = cssPath;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getCssPath() {
            return cssPath;
        }

        public static Theme fromId(String id) {
            for (Theme theme : values()) {
                if (theme.id.equals(id)) {
                    return theme;
                }
            }
            return LIGHT; // Por defecto
        }
    }

    private ThemeManager() {
        registeredScenes = new ArrayList<>();

        // Cargar tema desde configuración
        String savedTheme = ConfigManager.getInstance().getConfig().getTheme();
        currentTheme = Theme.fromId(savedTheme);
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Registra una escena para aplicar temas
     */
    public void registerScene(Scene scene) {
        if (!registeredScenes.contains(scene)) {
            registeredScenes.add(scene);
            applyThemeToScene(scene, currentTheme);
        }
    }

    /**
     * Cambia el tema de la aplicación
     */
    public void setTheme(Theme theme) {
        if (this.currentTheme != theme) {
            this.currentTheme = theme;

            // Aplicar a todas las escenas registradas
            for (Scene scene : registeredScenes) {
                applyThemeToScene(scene, theme);
            }

            // Guardar en configuración
            ConfigManager.getInstance().getConfig().setTheme(theme.getId());
            ConfigManager.getInstance().saveConfig();

            System.out.println("Tema cambiado a: " + theme.getDisplayName());
        }
    }

    private void applyThemeToScene(Scene scene, Theme theme) {
        // Limpiar estilos anteriores
        scene.getStylesheets().clear();

        try {
            // Intentar cargar el recurso CSS
            var resource = getClass().getResource(theme.getCssPath());

            if (resource == null) {
                System.err.println("ERROR: No se encontró el archivo CSS: " + theme.getCssPath());
                System.err.println("Verifica que el archivo existe en src/main/resources");

                // Intentar con ruta alternativa
                String alternativePath = theme.getCssPath().replace("/com/lazyzxsoftware/zxspectrumide/", "/");
                resource = getClass().getResource(alternativePath);

                if (resource == null) {
                    System.err.println("Tampoco se encontró en: " + alternativePath);
                    return;
                } else {
                    System.out.println("Encontrado en ruta alternativa: " + alternativePath);
                }
            }

            String cssPath = resource.toExternalForm();
            scene.getStylesheets().add(cssPath);
            System.out.println("Tema aplicado a escena: " + theme.getDisplayName());
            System.out.println("Archivo CSS: " + cssPath);

        } catch (Exception e) {
            System.err.println("Error al aplicar tema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtiene el tema actual
     */
    public Theme getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Alterna entre tema claro y oscuro
     */
    public void toggleTheme() {
        Theme newTheme = (currentTheme == Theme.LIGHT) ? Theme.DARK : Theme.LIGHT;
        setTheme(newTheme);
    }

    /**
     * Obtiene todos los temas disponibles
     */
    public Theme[] getAvailableThemes() {
        return Theme.values();
    }
}