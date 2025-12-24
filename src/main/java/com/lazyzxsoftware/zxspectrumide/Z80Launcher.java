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
        String machineModel = config.getEmulatorMachine();

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

            if (emulator.getName().toLowerCase().contains("zesarux")) {
                //command.add("--noconfigfile");

                // --- NUEVO: ACTIVAR MODO DEBUG REMOTO (ZRCP) ---
                //command.add("--enable-remotectrl");
                // command.add("--enable-zrcp"); // Probaremos esto si la config manual falla.
                // -----------------------------------------------

                if (machineModel != null) {
                    command.add("--machine");
                    if (machineModel.contains("128k")) command.add("128k");
                    else if (machineModel.contains("+3")) command.add("Plus3");
                    else command.add("48k");
                }

                command.add(tapPath);
            } else {
                command.add(tapPath);
            }

            // 3. Lanzar el proceso
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO();
            pb.start();

            System.out.println("🚀 Emulador lanzado con soporte Debug: " + emulatorPath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al lanzar el emulador: " + e.getMessage());
        }
    }
}