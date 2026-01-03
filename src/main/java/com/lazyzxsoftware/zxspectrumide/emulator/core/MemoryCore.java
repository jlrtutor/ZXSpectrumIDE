package com.lazyzxsoftware.zxspectrumide.emulator.core;

import java.util.Arrays;

/**
 * MemoryCore: Implementación limpia y simple de la memoria de 64KB.
 * Sin bancos, sin contención y sin protección de ROM por ahora.
 * Ideal para Unit Testing y validación de la CPU.
 */
public class MemoryCore {
    // 64KB de RAM pura
    private final byte[] data = new byte[65536];

    public MemoryCore() {
        reset();
    }

    /**
     * Reinicia la memoria a ceros.
     */
    public void reset() {
        Arrays.fill(data, (byte) 0);
    }

    /**
     * Lee un byte de la dirección especificada.
     * @return Valor entre 0 y 255 (importante: devuelve int sin signo)
     */
    public int read(int address) {
        return data[address & 0xFFFF] & 0xFF;
    }

    /**
     * Escribe un byte en la dirección especificada.
     * En este núcleo básico, NO hay protección contra escritura.
     */
    public void write(int address, int value) {
        data[address & 0xFFFF] = (byte) (value & 0xFF);
    }

    // --- MÉTODOS AUXILIARES DE 16 BITS (Little Endian) ---

    /**
     * Lee una palabra de 16 bits (Word).
     * El byte bajo está en 'address', el alto en 'address + 1'.
     */
    public int readWord(int address) {
        int low = read(address);
        int high = read(address + 1);
        return (high << 8) | low;
    }

    /**
     * Escribe una palabra de 16 bits.
     */
    public void writeWord(int address, int value) {
        write(address, value & 0xFF);         // Byte bajo
        write(address + 1, (value >> 8) & 0xFF); // Byte alto
    }

    // --- UTILIDADES PARA TESTS ---

    /**
     * Carga un array de bytes en una dirección específica.
     * Esencial para inyectar los micro-tests.
     */
    public void loadData(int address, byte[] content) {
        for (int i = 0; i < content.length; i++) {
            write(address + i, content[i]);
        }
    }
}