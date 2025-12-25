package com.lazyzxsoftware.zxspectrumide.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Gestor de configuración de la aplicación.
 * Guarda y carga la configuración en formato JSON usando GSON.
 */
public class ConfigManager {

    private static ConfigManager instance;
    private AppConfig config;
    private final Gson gson;
    private final Path configPath;

    private ConfigManager() {
        // Inicializamos GSON con formato "bonito" para que el JSON sea legible
        gson = new GsonBuilder().setPrettyPrinting().create();

        // Definimos la ruta de configuración: ~/.zxide/config.json
        String userHome = System.getProperty("user.home");
        Path zxideDir = Paths.get(userHome, ".zxide");
        configPath = zxideDir.resolve("config.json");

        // Crear directorio .zxide si no existe
        try {
            if (!Files.exists(zxideDir)) {
                Files.createDirectories(zxideDir);
            }
        } catch (IOException e) {
            System.err.println("Error al crear directorio de configuración: " + e.getMessage());
        }

        // Cargar configuración al iniciar
        loadConfig();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Obtiene el objeto de configuración actual.
     * Si por algún motivo es null, crea uno nuevo para evitar NullPointerException.
     */
    public AppConfig getConfig() {
        if (config == null) {
            System.err.println("ADVERTENCIA: config era null en getConfig(). Restaurando valores por defecto.");
            config = new AppConfig();
            saveConfig();
        }
        return config;
    }

    /**
     * Carga la configuración desde el disco.
     */
    private void loadConfig() {
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                AppConfig loadedConfig = gson.fromJson(reader, AppConfig.class);

                if (loadedConfig != null) {
                    config = loadedConfig;
                    System.out.println("Configuración cargada desde: " + configPath);
                } else {
                    System.err.println("Error: archivo de configuración vacío o corrupto. Usando defaults.");
                    config = new AppConfig();
                    saveConfig();
                }
            } catch (IOException e) {
                System.err.println("Error al leer configuración: " + e.getMessage());
                config = new AppConfig(); // Fallback
            }
        } else {
            System.out.println("Archivo de configuración no encontrado. Creando nuevo.");
            config = new AppConfig();
            saveConfig();
        }

        // Doble verificación de seguridad
        if (config == null) {
            config = new AppConfig();
        }
    }

    /**
     * Guarda la configuración actual en el archivo JSON.
     */
    public void saveConfig() {
        if (config == null) return;

        try (Writer writer = Files.newBufferedWriter(configPath)) {
            gson.toJson(config, writer);
            // System.out.println("Configuración guardada."); // Comentado para no saturar logs
        } catch (IOException e) {
            System.err.println("Error grave al guardar configuración: " + e.getMessage());
        }
    }

    /**
     * Restaura la configuración de fábrica.
     */
    public void resetToDefaults() {
        config = new AppConfig();
        saveConfig();
        System.out.println("Configuración restablecida a valores por defecto.");
    }
}