package com.lazyzxsoftware.zxspectrumide.utils;

import java.util.ArrayList;
import java.util.List;

public class Z80Disassembler {

    public static class Instruction {
        public int address;
        public String bytesStr;
        public String mnemonic;
        public String args;
        public int size;

        public Instruction(int address, String bytesStr, String mnemonic, String args, int size) {
            this.address = address;
            this.bytesStr = bytesStr;
            this.mnemonic = mnemonic;
            this.args = args;
            this.size = size;
        }

        @Override
        public String toString() {
            return String.format("%04X  %-10s  %-4s %s", address, bytesStr, mnemonic, args);
        }
    }

    public static List<Instruction> disassemble(byte[] memory, int startAddress, int count) {
        List<Instruction> instructions = new ArrayList<>();
        int pc = startAddress;

        for (int i = 0; i < count; i++) {
            if (pc >= 65536) break;
            Instruction instr = decode(memory, pc);
            instructions.add(instr);
            pc = (pc + instr.size) & 0xFFFF;
        }
        return instructions;
    }

    private static Instruction decode(byte[] mem, int pc) {
        int b = peek(mem, pc);
        int next1 = peek(mem, pc + 1);
        int next2 = peek(mem, pc + 2);
        int next3 = peek(mem, pc + 3); // Para instrucciones largas (IX+d)

        // Lógica simplificada pero extensible para detectar prefijos
        if (b == 0xED) return decodeED(mem, pc, next1);
        if (b == 0xCB) return decodeCB(mem, pc, next1);
        if (b == 0xDD) return decodeIndex(mem, pc, "IX");
        if (b == 0xFD) return decodeIndex(mem, pc, "IY");

        // Instrucciones base (Ejemplos comunes, habría que rellenar la tabla completa)
        String mnem = "NOP";
        String args = "";
        int size = 1;

        switch (b) {
            case 0x00: mnem = "NOP"; break;
            case 0x76: mnem = "HALT"; break;
            case 0xF3: mnem = "DI"; break;
            case 0xFB: mnem = "EI"; break;
            case 0xC9: mnem = "RET"; break;

            // Cargas inmediatas 8-bit
            case 0x3E: mnem = "LD"; args = "A," + hex8(next1); size = 2; break;
            case 0x06: mnem = "LD"; args = "B," + hex8(next1); size = 2; break;
            // ... (añadir resto de LD r,n)

            // Saltos relativos
            case 0x18: mnem = "JR"; args = rel(pc, 2, next1); size = 2; break;
            case 0x20: mnem = "JR"; args = "NZ," + rel(pc, 2, next1); size = 2; break;
            case 0x28: mnem = "JR"; args = "Z," + rel(pc, 2, next1); size = 2; break;
            case 0x10: mnem = "DJNZ"; args = rel(pc, 2, next1); size = 2; break;

            // Saltos absolutos
            case 0xC3: mnem = "JP"; args = hex16(next2, next1); size = 3; break;
            case 0xCD: mnem = "CALL"; args = hex16(next2, next1); size = 3; break;

            // ALU A, n
            case 0xC6: mnem = "ADD"; args = "A," + hex8(next1); size = 2; break;
            case 0xD6: mnem = "SUB"; args = hex8(next1); size = 2; break;
            case 0xE6: mnem = "AND"; args = hex8(next1); size = 2; break;
            case 0xF6: mnem = "OR"; args = hex8(next1); size = 2; break;
            case 0xFE: mnem = "CP"; args = hex8(next1); size = 2; break;

            // Salidas
            case 0xD3: mnem = "OUT"; args = "(" + hex8(next1) + "),A"; size = 2; break;
            case 0xDB: mnem = "IN"; args = "A,(" + hex8(next1) + ")"; size = 2; break;

            default:
                mnem = "DB";
                args = hex8(b);
                break;
        }

        // Construir string de bytes para display
        StringBuilder bytesStr = new StringBuilder(String.format("%02X", b));
        for(int k=1; k<size; k++) bytesStr.append(String.format(" %02X", peek(mem, pc+k)));

        return new Instruction(pc, bytesStr.toString(), mnem, args, size);
    }

    // --- Helpers de decodificación ---

    private static Instruction decodeED(byte[] mem, int pc, int opcode) {
        String mnem = "NEG"; // Placeholder
        String args = "";
        int size = 2;

        if (opcode == 0xB0) mnem = "LDIR";
        else if (opcode == 0xB8) mnem = "LDDR";
        else if (opcode == 0x46) { mnem = "IM"; args = "0"; }
        else if (opcode == 0x56) { mnem = "IM"; args = "1"; }
        else if (opcode == 0x5E) { mnem = "IM"; args = "2"; }
        else { mnem = "???"; args = "(ED " + String.format("%02X", opcode) + ")"; }

        return new Instruction(pc, "ED " + String.format("%02X", opcode), mnem, args, size);
    }

    private static Instruction decodeCB(byte[] mem, int pc, int opcode) {
        // Los opcodes CB son bits/shifts. Ej: CB C7 = SET 0,A
        return new Instruction(pc, "CB " + String.format("%02X", opcode), "BIT/ROT", "...", 2);
    }

    private static Instruction decodeIndex(byte[] mem, int pc, String reg) {
        // Manejo básico de IX/IY
        int op = peek(mem, pc + 1);
        int disp = peek(mem, pc + 2); // Desplazamiento d
        int size = 2; // Mínimo
        String mnem = "???";
        String args = "";

        if (op == 0x21) { // LD IX, nn
            mnem = "LD";
            args = reg + "," + hex16(peek(mem, pc+3), disp);
            size = 4;
        } else if (op == 0xE9) { // JP (IX)
            mnem = "JP"; args = "(" + reg + ")";
        }
        // ... (Faltaría implementar la lógica completa de prefijos) ...

        // Construir bytes
        StringBuilder bytesStr = new StringBuilder(String.format("%02X %02X", peek(mem, pc), op));
        if (size > 2) bytesStr.append(" ...");

        return new Instruction(pc, bytesStr.toString(), mnem, args, size);
    }

    private static int peek(byte[] mem, int addr) {
        if (mem == null || addr >= mem.length) return 0;
        return mem[addr & 0xFFFF] & 0xFF;
    }

    private static String hex8(int v) { return String.format("#%02X", v); }
    private static String hex16(int h, int l) { return String.format("#%04X", (h << 8) | l); }
    private static String rel(int pc, int size, int offset) {
        int target = pc + size + (byte)offset; // Casting a byte maneja el signo
        return String.format("#%04X", target & 0xFFFF);
    }
}