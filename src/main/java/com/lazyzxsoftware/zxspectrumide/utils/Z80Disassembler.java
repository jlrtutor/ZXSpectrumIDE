package com.lazyzxsoftware.zxspectrumide.utils;

import java.util.ArrayList;
import java.util.List;

public class Z80Disassembler {

    public static class Instruction {
        public int address;
        public String bytesStr;
        public String mnemonic;
        public int size;

        public Instruction(int address, String bytesStr, String mnemonic, int size) {
            this.address = address;
            this.bytesStr = bytesStr;
            this.mnemonic = mnemonic;
            this.size = size;
        }
    }

    /**
     * Desensambla un bloque de bytes comenzando en una dirección dada.
     * @param memory Array con los bytes de la memoria RAM.
     * @param startAddress Dirección de memoria donde empieza este bloque (ej: el PC actual).
     * @param count Número de instrucciones a decodificar.
     */
    public static List<Instruction> disassemble(byte[] memory, int startAddress, int count) {
        List<Instruction> instructions = new ArrayList<>();
        int currentAddr = startAddress;
        int maxBytes = memory.length;

        for (int i = 0; i < count; i++) {
            if (currentAddr >= 65536) break; // Fin de memoria

            // Decodificar una instrucción
            Instruction instr = decodeInstruction(memory, currentAddr, maxBytes);
            instructions.add(instr);

            currentAddr += instr.size;
        }
        return instructions;
    }

    private static Instruction decodeInstruction(byte[] mem, int addr, int maxLen) {
        int b = peek(mem, addr) & 0xFF;
        int size = 1;
        String mnemonic = "NOP";
        String bytesStr = String.format("%02X", b);

        // --- LÓGICA DE DECODIFICACIÓN SIMPLIFICADA ---
        // Aquí añadiremos los opcodes. Esto es un subconjunto básico.
        // Una implementación completa requiere miles de líneas (tablas de opcodes).
        // Usaremos un "fallback" inteligente para lo que no reconozcamos.

        switch (b) {
            case 0x00: mnemonic = "NOP"; break;
            case 0x76: mnemonic = "HALT"; break;
            case 0xC9: mnemonic = "RET"; break;
            case 0xF3: mnemonic = "DI"; break;
            case 0xFB: mnemonic = "EI"; break;

            // Cargas de 8 bits (LD r, n)
            case 0x3E: mnemonic = "LD A, " + arg8(mem, addr+1); size=2; break;
            case 0x06: mnemonic = "LD B, " + arg8(mem, addr+1); size=2; break;
            case 0x0E: mnemonic = "LD C, " + arg8(mem, addr+1); size=2; break;

            // Saltos relativos (JR)
            case 0x18: mnemonic = "JR " + relAddr(addr, peek(mem, addr+1)); size=2; break;
            case 0x20: mnemonic = "JR NZ, " + relAddr(addr, peek(mem, addr+1)); size=2; break;
            case 0x28: mnemonic = "JR Z, " + relAddr(addr, peek(mem, addr+1)); size=2; break;
            case 0x10: mnemonic = "DJNZ " + relAddr(addr, peek(mem, addr+1)); size=2; break;

            // Saltos absolutos (JP, CALL)
            case 0xC3: mnemonic = "JP " + arg16(mem, addr+1); size=3; break;
            case 0xCD: mnemonic = "CALL " + arg16(mem, addr+1); size=3; break;

            // Salidas (OUT)
            case 0xD3: mnemonic = "OUT (" + arg8(mem, addr+1) + "), A"; size=2; break;

            // Prefijo extendido ED (ejemplos)
            case 0xED:
                int b2 = peek(mem, addr+1) & 0xFF;
                size = 2;
                bytesStr += String.format(" %02X", b2);
                if (b2 == 0xB0) mnemonic = "LDIR";
                else if (b2 == 0xB8) mnemonic = "LDDR";
                else mnemonic = "??? (ED " + String.format("%02X", b2) + ")";
                break;

            // Por defecto: mostrar DB (Define Byte) si no lo conocemos
            default:
                mnemonic = "DB " + String.format("#%02X", b);
                break;
        }

        // Actualizar string de bytes si la instrucción ocupaba más
        if (size == 2) bytesStr = String.format("%02X %02X", b, peek(mem, addr+1));
        if (size == 3) bytesStr = String.format("%02X %02X %02X", b, peek(mem, addr+1), peek(mem, addr+2));

        return new Instruction(addr, bytesStr, mnemonic, size);
    }

    // --- Helpers de lectura ---

    private static byte peek(byte[] mem, int addr) {
        if (addr >= mem.length) return 0;
        return mem[addr]; // Asumiendo que 'mem' es un bloque que empieza en 0 o PC relativo
    }

    private static String arg8(byte[] mem, int addr) {
        return String.format("#%02X", peek(mem, addr));
    }

    private static String arg16(byte[] mem, int addr) {
        int low = peek(mem, addr) & 0xFF;
        int high = peek(mem, addr+1) & 0xFF;
        return String.format("#%04X", (high << 8) | low);
    }

    private static String relAddr(int pcAddr, byte offset) {
        int target = pcAddr + 2 + offset; // PC + 2 bytes de la instr + offset
        return String.format("#%04X", target & 0xFFFF);
    }
}