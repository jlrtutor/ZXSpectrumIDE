package com.lazyzxsoftware.zxspectrumide.compiler;

import java.io.File;
import java.util.List;

public interface Compiler {
    /**
     * Compila el archivo fuente y genera el ejecutable.
     * @param sourceFile Archivo .asm de entrada
     * @param outputDir Directorio donde dejar el resultado
     * @return El proceso ejecutado (para poder leer su salida)
     * @throws Exception Si falla la ejecución
     */
    Process compile(File sourceFile, File outputDir) throws Exception;

    /**
     * Devuelve el nombre del compilador (ej: "Pasmo v0.5.5")
     */
    String getName();

    /**
     * Devuelve la lista de formatos de salida soportados (ej: .tap, .bin)
     */
    List<String> getSupportedFormats();
}