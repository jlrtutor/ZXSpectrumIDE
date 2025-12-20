package com.lazyzxsoftware.zxspectrumide.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Gestor de configuración de la aplicación
 * Guarda y carga la configuración en formato JSON
 */
public class ConfigManager {

    private static ConfigManager instance;
    private AppConfig config;
    private final Gson gson;
    private final Path configPath;

    private ConfigManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();

        // Ruta de configuración: ~/.zxide/config.json
        String userHome = System.getProperty("user.home");
        Path zxideDir = Paths.get(userHome, ".zxide");
        configPath = zxideDir.resolve("config.json");

        // Crear directorio si no existe
        try {
            Files.createDirectories(zxideDir);
        } catch (IOException e) {
            System.err.println("Error al crear directorio de configuración: " + e.getMessage());
        }

        // Cargar configuración
        loadConfig();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Obtiene la configuración actual
     */
    public AppConfig getConfig() {
        return config;
    }

    /**
     * Carga la configuración desde el archivo
     */
    private void loadConfig() {
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                config = gson.fromJson(reader, AppConfig.class);
                System.out.println("Configuración cargada desde: " + configPath);
            } catch (IOException e) {
                System.err.println("Error al cargar configuración: " + e.getMessage());
                config = new AppConfig(); // Usar configuración por defecto
            }
        } else {
            System.out.println("Archivo de configuración no encontrado. Usando valores por defecto.");
            config = new AppConfig();
            saveConfig(); // Guardar la configuración por defecto
        }
    }

    /**
     * Guarda la configuración actual en el archivo
     */
    public void saveConfig() {
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            gson.toJson(config, writer);
            System.out.println("Configuración guardada en: " + configPath);
        } catch (IOException e) {
            System.err.println("Error al guardar configuración: " + e.getMessage());
        }
    }

    /**
     * Restaura la configuración por defecto
     */
    public void resetToDefaults() {
        config = new AppConfig();
        saveConfig();
    }
}