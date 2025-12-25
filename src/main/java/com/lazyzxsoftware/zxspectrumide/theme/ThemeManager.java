package com.lazyzxsoftware.zxspectrumide.theme;

import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de temas de la aplicación.
 * Permite cambiar entre tema claro y oscuro y notifica a todas las escenas registradas.
 */
public class ThemeManager {

    private static ThemeManager instance;
    private final List<Scene> registeredScenes;
    private Theme currentTheme;

    // Rutas de los archivos CSS
    // Asegúrate de que estos archivos existen en src/main/resources/...
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

        // Cargar tema guardado en configuración (o usar default)
        String savedTheme = "light";
        try {
            // Intentamos leer de ConfigManager si existe y no es nulo
            if (ConfigManager.getInstance() != null && ConfigManager.getInstance().getConfig() != null) {
                savedTheme = ConfigManager.getInstance().getConfig().getTheme();
            }
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudo cargar configuración de tema: " + e.getMessage());
        }

        currentTheme = Theme.fromId(savedTheme);
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Método de conveniencia para aplicar tema a una escena (alias de registerScene).
     * Usado por Main.java.
     */
    public void applyTheme(Scene scene) {
        registerScene(scene);
    }

    /**
     * Registra una escena para que reciba actualizaciones de tema dinámicas.
     */
    public void registerScene(Scene scene) {
        if (!registeredScenes.contains(scene)) {
            registeredScenes.add(scene);
            // Aplicar el tema actual inmediatamente
            applyThemeToScene(scene, currentTheme);
        }
    }

    /**
     * Cambia el tema global de la aplicación.
     */
    public void setTheme(Theme theme) {
        if (this.currentTheme != theme) {
            this.currentTheme = theme;

            // Aplicar a todas las ventanas/escenas abiertas
            for (Scene scene : registeredScenes) {
                applyThemeToScene(scene, theme);
            }

            // Guardar preferencia
            try {
                if (ConfigManager.getInstance() != null) {
                    ConfigManager.getInstance().getConfig().setTheme(theme.getId());
                    ConfigManager.getInstance().saveConfig();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Tema cambiado a: " + theme.getDisplayName());
        }
    }

    private void applyThemeToScene(Scene scene, Theme theme) {
        // Limpiar estilos anteriores para no acumular basura
        scene.getStylesheets().clear();

        try {
            // Cargar CSS
            var resource = getClass().getResource(theme.getCssPath());

            if (resource == null) {
                System.err.println("❌ ERROR CRÍTICO: No se encuentra el CSS: " + theme.getCssPath());
                return;
            }

            String cssUrl = resource.toExternalForm();
            scene.getStylesheets().add(cssUrl);

        } catch (Exception e) {
            System.err.println("Error aplicando CSS: " + e.getMessage());
        }
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public void toggleTheme() {
        setTheme(currentTheme == Theme.LIGHT ? Theme.DARK : Theme.LIGHT);
    }
}