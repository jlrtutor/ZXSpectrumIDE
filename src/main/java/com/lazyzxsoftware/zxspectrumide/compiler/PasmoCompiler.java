package com.lazyzxsoftware.zxspectrumide.compiler;

import com.lazyzxsoftware.zxspectrumide.config.AppConfig;
import com.lazyzxsoftware.zxspectrumide.config.ConfigManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PasmoCompiler implements Compiler {

    public PasmoCompiler() {
    }

    @Override
    public String getName() {
        return "Pasmo";
    }

    @Override
    public List<String> getSupportedFormats() {
        return Arrays.asList("tap", "tapbas", "bin", "hex");
    }

    @Override
    public Process compile(File sourceFile, File outputDir) throws Exception {
        AppConfig config = ConfigManager.getInstance().getConfig();

        String pasmoPath = config.getPasmoPath();
        if (pasmoPath == null || pasmoPath.isEmpty()) {
            pasmoPath = "pasmo"; // Asume que está en el PATH del sistema
        }

        // 1. Calcular nombre base
        String baseName = sourceFile.getName().replaceFirst("[.][^.]+$", "");

        // 2. Calcular nombre "limpio" para la cabecera del Spectrum (MSDOS 8.3)
        // Max 8 caracteres, Mayúsculas, Sin espacios.
        String tapeName = baseName.toUpperCase()
                .replace(" ", "")
                .replace("_", "");

        if (tapeName.length() > 8) {
            tapeName = tapeName.substring(0, 8);
        }
        if (tapeName.isEmpty()) {
            tapeName = "NONAME";
        }

        String format = config.getPasmoFormat();
        if (format == null) format = "tapbas";

        String extension = ".tap";
        if ("hex".equals(format)) extension = ".hex";
        if ("bin".equals(format)) extension = ".bin";

        // --- CAMBIO: Usar 'tapeName' (MSDOS 8.3) para el archivo físico ---
        // Esto asegura compatibilidad total con emuladores y FAT16/32.
        // Ej: "Mi Juego Increible.asm" -> "MIJUEGOI.tap"
        File outputFile = new File(outputDir, tapeName + extension);

        List<String> command = new ArrayList<>();
        command.add(pasmoPath);

        // Opciones de formato
        switch (format) {
            case "tapbas": command.add("--tapbas"); break;
            case "tap":    command.add("--tap"); break;
            case "bin":    command.add("--bin"); break;
            case "hex":    command.add("--hex"); break;
            default:       command.add("--tapbas");
        }

        // Nombre interno (Cabecera Spectrum)
        command.add("--name");
        command.add(tapeName);

        if (config.isPasmoDebug()) {
            command.add("-d");
        }

        command.add("--alocal");

        command.add("-I");
        command.add(sourceFile.getParent());

        // ARGUMENTOS POSICIONALES
        // 1. INPUT
        command.add(sourceFile.getAbsolutePath());

        // 2. OUTPUT (Nombre físico MSDOS)
        command.add(outputFile.getAbsolutePath());

        // 3. SYMBOL FILE
        // Los símbolos sí pueden conservar el nombre largo para facilitar el debug en el IDE
        File symbolFile = new File(outputDir, baseName + ".symbols");
        command.add(symbolFile.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(outputDir);

        pb.redirectErrorStream(false);

        return pb.start();
    }
}