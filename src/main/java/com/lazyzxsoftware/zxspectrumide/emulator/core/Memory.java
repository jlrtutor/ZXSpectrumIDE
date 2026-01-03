package com.lazyzxsoftware.zxspectrumide.emulator.core;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Random; // Importar Random

public class Memory {
    private final byte[] data = new byte[65536];
    private boolean isRomProtected = true;

    public Memory() {
        reset(); // Usar el reset para llenar con ruido
    }

    public int read(int address) {
        return this.data[address & 0xFFFF] & 0xFF;
    }

    public void loadBinary(String resourcePath, int startAddress) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.out.println("⚠️ Fichero no encontrado: " + resourcePath);
                return;
            }
            byte[] buffer = is.readAllBytes();
            for (int i = 0; i < buffer.length && (startAddress + i) < 65536; i++) {
                this.data[(startAddress + i) & 0xFFFF] = (byte) (buffer[i] & 0xFF);
            }
            System.out.println("✅ Binario cargado: " + resourcePath + " en " + String.format("%04X", startAddress));
        } catch (Exception e) {
            System.err.println("❌ Error cargando binario: " + e.getMessage());
        }
    }

    public void write(int address, int value) {
        int addr = address & 0xFFFF;
        if (addr >= 0x4000) {
            data[addr] = (byte) (value & 0xFF);

            // --- NUEVA ESTRATEGIA DE LOG ---
            int verify = data[addr] & 0xFF;
            if (verify != (value & 0xFF)) {
                System.out.println(String.format("!!! ERROR DE MEMORIA en %04X: Escribí %02X pero leo %02X",
                        addr, (value & 0xFF), verify));
            }
        }
    }

    public void reset() {
        // Estilo JSpeccy: Llenamos la RAM con ruido aleatorio en lugar de ceros.
        // Esto es fundamental para que el test de memoria de la ROM (RAM-CHECK)
        // detecte correctamente dónde termina la RAM física.
        Random rand = new Random();

        // La RAM del Spectrum 48K comienza en la dirección 0x4000 (16384).
        // No tocamos la zona de ROM (0x0000 - 0x3FFF) ya que se encarga loadRom.
        for (int i = 0x4000; i < data.length; i++) {
            // Usamos el cast (byte) para convertir el int (0-255) al tipo del array.
            // Esto evita el error de "lossy conversion" en Java.
            data[i] = (byte) rand.nextInt(256);
        }

        System.out.println("✅ Memoria RAM reiniciada con valores aleatorios.");
    }

    public void loadRom(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("ROM no encontrada: " + resourcePath);
            }

            // Leemos todos los bytes del archivo de la ROM
            byte[] buffer = is.readAllBytes();

            // El Spectrum 48K carga su ROM en los primeros 16KB (direcciones 0 a 16383)
            // Usamos Math.min para no intentar escribir más allá del espacio de la ROM
            for (int i = 0; i < Math.min(buffer.length, 16384); i++) {
                // Aplicamos cast (byte) para convertir el int resultante de la máscara
                // Esto guarda los 8 bits exactos en el array 'data'
                this.data[i] = (byte) (buffer[i] & 0xFF);
            }

            System.out.println("✅ ROM cargada correctamente en memoria: " + resourcePath);

        } catch (Exception e) {
            System.err.println("❌ Error crítico al cargar la ROM: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Debug
    public int[] getDump(int start, int length) {
        int[] dump = new int[length];
        for (int i = 0; i < length; i++) dump[i] = data[(start + i) & 0xFFFF];
        return dump;
    }
}