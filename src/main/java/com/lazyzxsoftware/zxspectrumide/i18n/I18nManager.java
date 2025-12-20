package com.lazyzxsoftware.zxspectrumide.i18n;

import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;

import java.util.*;

/**
 * Gestor de internacionalización (i18n)
 * Maneja las traducciones de la aplicación
 */
public class I18nManager {

    private static I18nManager instance;
    private ResourceBundle currentBundle;
    private Locale currentLocale;

    // Idiomas disponibles
    public enum Language {
        SPANISH("es", "Español", new Locale("es")),
        ENGLISH("en", "English", Locale.ENGLISH);

        private final String code;
        private final String displayName;
        private final Locale locale;

        Language(String code, String displayName, Locale locale) {
            this.code = code;
            this.displayName = displayName;
            this.locale = locale;
        }

        public String getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Locale getLocale() {
            return locale;
        }

        public static Language fromCode(String code) {
            for (Language lang : values()) {
                if (lang.code.equals(code)) {
                    return lang;
                }
            }
            return SPANISH; // Por defecto
        }
    }

    private I18nManager() {
        // Cargar idioma desde configuración
        String savedLanguage = ConfigManager.getInstance().getConfig().getLanguage();
        Language language = Language.fromCode(savedLanguage);
        loadLanguage(language);
    }

    public static I18nManager getInstance() {
        if (instance == null) {
            instance = new I18nManager();
        }
        return instance;
    }

    /**
     * Carga un idioma
     */
    public void loadLanguage(Language language) {
        try {
            currentLocale = language.getLocale();
            currentBundle = ResourceBundle.getBundle(
                    "com.lazyzxsoftware.zxspectrumide.i18n.messages",
                    currentLocale
            );

            System.out.println("Idioma cargado: " + language.getDisplayName());

            // Guardar en configuración
            ConfigManager.getInstance().getConfig().setLanguage(language.getCode());
            ConfigManager.getInstance().saveConfig();

        } catch (MissingResourceException e) {
            System.err.println("Error al cargar idioma: " + e.getMessage());
            // Intentar cargar español por defecto
            if (language != Language.SPANISH) {
                loadLanguage(Language.SPANISH);
            }
        }
    }

    /**
     * Obtiene una traducción por su clave
     */
    public String get(String key) {
        try {
            return currentBundle.getString(key);
        } catch (MissingResourceException e) {
            System.err.println("Clave de traducción no encontrada: " + key);
            return "!" + key + "!"; // Retornar la clave con ! para facilitar debug
        }
    }

    /**
     * Obtiene una traducción con parámetros
     * Ejemplo: get("message.welcome", "Usuario")
     */
    public String get(String key, Object... args) {
        String pattern = get(key);
        return String.format(pattern, args);
    }

    /**
     * Obtiene el idioma actual
     */
    public Language getCurrentLanguage() {
        String code = ConfigManager.getInstance().getConfig().getLanguage();
        return Language.fromCode(code);
    }

    /**
     * Obtiene todos los idiomas disponibles
     */
    public Language[] getAvailableLanguages() {
        return Language.values();
    }

    /**
     * Obtiene el locale actual
     */
    public Locale getCurrentLocale() {
        return currentLocale;
    }
}