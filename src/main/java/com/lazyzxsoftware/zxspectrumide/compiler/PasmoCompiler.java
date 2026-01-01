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

        // 1. Calcular nombres base
        String baseName = sourceFile.getName().replaceFirst("[.][^.]+$", "");

        // 2. Calcular nombre "limpio" para la cabecera del Spectrum (Max 8 chars)
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
        File outputFile = new File(outputDir, tapeName + extension);

        List<String> command = new ArrayList<>();
        command.add(pasmoPath);

        // Opciones de formato
        // --tapbas añade un cargador BASIC automáticamente (Recomendado para juegos)
        // --tap crea solo el bloque de bytes (requiere LOAD "" CODE manual)
        switch (format) {
            case "tapbas": command.add("--tapbas"); break;
            case "tap":    command.add("--tap"); break;
            case "bin":    command.add("--bin"); break;
            case "hex":    command.add("--hex"); break;
            default:       command.add("--tapbas");
        }

        command.add("--name");
        command.add(tapeName);

        if (config.isPasmoDebug()) {
            command.add("-d");
        }

        // --alocal: Autolocales (etiquetas que empiezan por _ son locales)
        // Muy útil para no llenar la tabla de símbolos de basura
        command.add("--alocal");

        command.add("-I");
        command.add(sourceFile.getParent());

        // ARGUMENTOS POSICIONALES (IMPORTANTE EL ORDEN)
        // 1. INPUT
        command.add(sourceFile.getAbsolutePath());

        // 2. OUTPUT
        command.add(outputFile.getAbsolutePath());

        // 3. SYMBOL FILE (.symbols)
        // Pasmo solo acepta UN archivo de símbolos al final
        File symbolFile = new File(outputDir, baseName + ".symbols");
        command.add(symbolFile.getAbsolutePath());

        // --- ERROR ANTERIOR: NO AÑADIR .publics AQUÍ ---
        // Pasmo no soporta un 4º argumento de archivo.

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(outputDir);

        // IMPORTANTE: NO redirigir error stream a input stream
        // Queremos leer stdout y stderr por separado en Main.java
        pb.redirectErrorStream(false);

        return pb.start();
    }
}