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
            pasmoPath = "pasmo";
        }

        // 1. Calcular nombres base
        String baseName = sourceFile.getName().replaceFirst("[.][^.]+$", "");

        // 2. Calcular nombre "limpio" para la cabecera del Spectrum (Max 8 chars)
        // Ejemplo: "Mi Juego.asm" -> "MIJUEGO"
        String tapeName = baseName.toUpperCase()
                .replace(" ", "")  // Quitamos espacios
                .replace("_", ""); // Quitamos guiones bajos

        if (tapeName.length() > 8) {
            tapeName = tapeName.substring(0, 8); // Cortamos a 8 caracteres
        }
        if (tapeName.isEmpty()) {
            tapeName = "NONAME";
        }

        String format = config.getPasmoFormat();
        if (format == null) format = "tapbas";

        String extension = ".tap";
        if ("hex".equals(format)) extension = ".hex";
        if ("bin".equals(format)) extension = ".bin";
        File outputFile = new File(outputDir, tapeName + extension);

        List<String> command = new ArrayList<>();
        command.add(pasmoPath);

        // 3. Añadir parámetro --name con el nombre limpio
        command.add("--name");
        command.add(tapeName);

        switch (format) {
            case "tapbas": command.add("--tapbas"); break;
            case "tap":    command.add("--tap"); break;
            case "bin":    command.add("--bin"); break;
            case "hex":    command.add("--hex"); break;
            default:       command.add("--tapbas");
        }

        if (config.isPasmoDebug()) {
            command.add("-d");
            command.add("--public");
            command.add(new File(outputDir, baseName + ".symbols").getAbsolutePath());
        }

        command.add("-I");
        command.add(sourceFile.getParent());

        command.add(sourceFile.getAbsolutePath());
        command.add(outputFile.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(outputDir);
        pb.redirectErrorStream(true);

        return pb.start();
    }
}