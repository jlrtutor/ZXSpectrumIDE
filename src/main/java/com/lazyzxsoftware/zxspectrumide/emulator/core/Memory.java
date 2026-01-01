package com.lazyzxsoftware.zxspectrumide.emulator.core;

import java.io.InputStream;
import java.util.Arrays;

public class Memory {
    // 64KB de memoria lineal (0x0000 - 0xFFFF)
    // 0x0000 - 0x3FFF: ROM (16K)
    // 0x4000 - 0xFFFF: RAM (48K)
    private final int[] data = new int[65536];
    private boolean isRomProtected = true;

    public Memory() {
        reset(); // Llamamos a reset para inicializar correctamente
    }

    // Leer byte (0-255)
    public int read(int address) {
        return data[address & 0xFFFF];
    }

    // Escribir byte
    public void write(int address, int value) {
        address &= 0xFFFF;
        // Si intentamos escribir en ROM y está protegida, ignorar
        if (address < 0x4000 && isRomProtected) {
            return;
        }
        data[address] = value & 0xFF;
    }

    public void reset() {
        // 1. Borramos toda la RAM (llenar con 0)
        Arrays.fill(data, 0);

        // 2. TRUCO VISUAL: Inicializar atributos de pantalla a BLANCO (Paper 7) sobre NEGRO (Ink 0)
        // La zona de atributos va de 0x5800 a 0x5AFF (768 bytes)
        // El valor 0x38 en binario es 00 111 000 (Flash 0, Bright 0, Paper 7, Ink 0)
        for (int i = 0x5800; i <= 0x5AFF; i++) {
            data[i] = 0x38;
        }
    }

    // Carga de ROM desde resources
    public void loadRom(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) throw new RuntimeException("ROM no encontrada: " + resourcePath);
            byte[] buffer = is.readAllBytes();
            for (int i = 0; i < Math.min(buffer.length, 16384); i++) {
                data[i] = buffer[i] & 0xFF; // Convertir byte signado a int unsigned
            }
            System.out.println("✅ ROM cargada: " + resourcePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Para depuración: volcado de memoria
    public int[] getDump(int start, int length) {
        int[] dump = new int[length];
        for (int i = 0; i < length; i++) {
            dump[i] = data[(start + i) & 0xFFFF];
        }
        return dump;
    }
}