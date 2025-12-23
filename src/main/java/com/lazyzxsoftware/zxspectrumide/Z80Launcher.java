package com.lazyzxsoftware.zxspectrumide;

import com.lazyzxsoftware.zxspectrumide.config.AppConfig;
import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Z80Launcher {

    public static void launch(String tapPath) {
        // 1. Obtener configuración
        AppConfig config = ConfigManager.getInstance().getConfig();
        String emulatorPath = config.getEmulatorPath();
        String machineModel = config.getEmulatorMachine(); // Ej: "Spectrum 48k"

        if (emulatorPath == null || emulatorPath.isBlank()) {
            System.err.println("ERROR: No hay emulador configurado.");
            return;
        }

        File emulator = new File(emulatorPath);
        if (!emulator.exists()) {
            System.err.println("ERROR: No encuentro el ejecutable del emulador en: " + emulatorPath);
            return;
        }

        try {
            // 2. Construir el comando
            List<String> command = new ArrayList<>();
            command.add(emulatorPath);

            // Argumentos específicos para ZEsarUX (el más común y el que pusimos por defecto)
            // Si el nombre del ejecutable contiene "zesarux", asumimos sus flags.
            // Si usas otro emulador, aquí habría que adaptar la lógica.
            if (emulator.getName().toLowerCase().contains("zesarux")) {
                command.add("--noconfigfile"); // Para evitar conflictos con configs previas

                // Mapear el modelo del combo a parámetro de ZEsarUX
                if (machineModel != null) {
                    command.add("--machine");
                    if (machineModel.contains("128k")) command.add("128k");
                    else if (machineModel.contains("+3")) command.add("Plus3");
                    else command.add("48k"); // Default
                }

                command.add(tapPath); // ZEsarUX detecta automáticamente que es una cinta
            } else {
                // Lógica genérica para otros emuladores (Fuse, RetroArch, etc.)
                // La mayoría aceptan la ruta del archivo como último argumento
                command.add(tapPath);
            }

            // 3. Lanzar el proceso
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO(); // Para ver la salida del emulador en la consola del IDE (si la hubiera)
            pb.start();

            System.out.println("🚀 Emulador lanzado: " + emulatorPath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al lanzar el emulador: " + e.getMessage());
        }
    }
}