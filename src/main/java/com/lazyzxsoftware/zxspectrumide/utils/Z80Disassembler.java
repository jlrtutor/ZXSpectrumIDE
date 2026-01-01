package com.lazyzxsoftware.zxspectrumide.utils;

import java.util.ArrayList;
import java.util.List;

public class Z80Disassembler {

    // Clase para representar una instrucción decodificada
    public static class Instruction {
        public int address;
        public String opcode;   // Ej: "LD A, (HL)"
        public String bytesStr; // Ej: "7E" o "DD 7E 05"
        public int size;        // Número de bytes

        public Instruction(int address, String opcode, String bytesStr, int size) {
            this.address = address;
            this.opcode = opcode;
            this.bytesStr = bytesStr;
            this.size = size;
        }

        @Override
        public String toString() {
            return String.format("%04X  %-12s  %s", address, bytesStr, opcode);
        }
    }

    /**
     * Desensambla un bloque de memoria comenzando desde startPC.
     * @param memory Array completo de 64k (o el fragmento disponible)
     * @param startPC Dirección de inicio (PC)
     * @param instructionCount Cuántas instrucciones queremos decodificar hacia adelante
     */
    public static List<Instruction> disassembleMemory(byte[] memory, int startPC, int instructionCount) {
        List<Instruction> instructions = new ArrayList<>();
        int currentPC = startPC;

        for (int i = 0; i < instructionCount; i++) {
            if (currentPC >= 65536) break;

            Instruction instr = decodeInstruction(memory, currentPC);
            instructions.add(instr);
            currentPC = (currentPC + instr.size) & 0xFFFF;
        }
        return instructions;
    }

    // =========================================================================
    // NÚCLEO DE DECODIFICACIÓN
    // =========================================================================

    private static Instruction decodeInstruction(byte[] memory, int pc) {
        int b = peek(memory, pc);

        // --- PREFIJOS DE INDICE (IX/IY) ---
        if (b == 0xDD || b == 0xFD) {
            String indexReg = (b == 0xDD) ? "IX" : "IY";
            int nextB = peek(memory, pc + 1);

            // Caso especial: DD CB d op (Bitwise sobre índice)
            if (nextB == 0xCB) {
                int d = peek(memory, pc + 2); // Desplazamiento
                int op = peek(memory, pc + 3); // Opcode real al final
                String disp = formatDisplacement(d);
                String bitInstr = decodeCB(op, "(" + indexReg + disp + ")");

                // Hack: DD CB d 46 -> BIT 0, (IX+d).
                // Pero algunas inst. copian resultado a reg (undocumented), las ignoramos por simplicidad estándar.

                String bytes = String.format("%02X CB %02X %02X", b, d, op);
                return new Instruction(pc, bitInstr, bytes, 4);
            }

            // Caso IX/IY normal: Tratamos de decodificar la instrucción base
            // Si la instrucción base usa (HL), se reemplaza por (IX+d).
            // Si la instrucción base usa HL, se reemplaza por IX.
            // Si no usa HL, el prefijo no tiene efecto (o actúa como NOP), pero lo mostramos.

            Instruction base = decodeMain(memory, pc + 1, true, pc); // Pasamos flag isIndex=true

            // Ajustamos el tamaño y los bytes
            // OJO: Si la instrucción base usa (HL), ha consumido un byte extra 'd' que decodeMain habrá leído
            // decodeMain retorna size relativo al opcode base.

            // Recalculamos manualmente para indexados complejos
            return decodeIndexMain(memory, pc, indexReg);
        }

        // --- PREFIJO EXTENDIDO (ED) ---
        if (b == 0xED) {
            int op = peek(memory, pc + 1);
            String txt = decodeED(op, memory, pc);
            String bytes = String.format("ED %02X", op);
            // Algunas ED tienen operandos (LD (nn), BC etc), ajustamos si necesario
            // Por simplicidad, la mayoría de ED son 2 bytes, salvo las de memoria
            int size = 2;
            if ((op & 0xC7) == 0x43) size = 4; // LD (nn), rr (16 bit)
            if (size == 4) bytes += String.format(" %02X %02X", peek(memory, pc+2), peek(memory, pc+3));

            return new Instruction(pc, txt, bytes, size);
        }

        // --- PREFIJO BITS (CB) ---
        if (b == 0xCB) {
            int op = peek(memory, pc + 1);
            String txt = decodeCB(op, null); // null = usar registro por defecto del opcode
            return new Instruction(pc, txt, String.format("CB %02X", op), 2);
        }

        // --- INSTRUCCIÓN ESTÁNDAR ---
        return decodeMain(memory, pc, false, pc);
    }

    // Lógica principal para opcodes sin prefijo (o base para IX/IY)
    private static Instruction decodeMain(byte[] memory, int pc, boolean isIndex, int originalPC) {
        int op = peek(memory, pc);
        int d = 0;
        int nn = 0;
        int size = 1;
        String bytes = String.format("%02X", op);

        // Si es modo indice y la instrucción accede a memoria (HL), necesitamos leer el desplazamiento 'd'
        // Pero cuidado, el 'd' viene DESPUÉS del opcode.
        // Ej: ADD A, (IX+d) -> DD 86 d.  El opcode es 86.
        String indexName = (peek(memory, originalPC) == 0xDD) ? "IX" : "IY";
        String hlReplace = isIndex ? indexName : "HL";
        String memReplace = isIndex ? "(" + indexName + "+d)" : "(HL)";

        // Tablas de registros
        String[] r = {"B", "C", "D", "E", "H", "L", "(HL)", "A"};
        String[] rp = {"BC", "DE", "HL", "SP"};
        String[] rp2 = {"BC", "DE", "HL", "AF"};
        String[] cc = {"NZ", "Z", "NC", "C", "PO", "PE", "P", "M"};
        String[] alu = {"ADD A,", "ADC A,", "SUB", "SBC A,", "AND", "XOR", "OR", "CP"};

        // --- DECODIFICACIÓN POR PATRONES DE BITS ---

        int x = (op >> 6) & 0x03;
        int y = (op >> 3) & 0x07;
        int z = op & 0x07;
        int p = (y >> 1);
        int q = y & 1;

        String mnemonic = "NOP";

        // BLOQUE 0 (x=0) : Control, Carga, ALU Inmediata
        if (x == 0) {
            if (z == 0) {
                if (y == 0) mnemonic = "NOP";
                else if (y == 1) { mnemonic = "EX AF, AF'"; }
                else if (y == 2) { mnemonic = "DJNZ " + getRelAddr(memory, pc); size = 2; }
                else if (y == 3) { mnemonic = "JR " + getRelAddr(memory, pc); size = 2; }
                else if (y >= 4) { mnemonic = "JR " + cc[y-4] + ", " + getRelAddr(memory, pc); size = 2; }
            } else if (z == 1) {
                if (q == 0) { mnemonic = "LD " + rp[p] + ", " + getNN(memory, pc); size = 3; } // LD rr, nn
                else        { mnemonic = "ADD " + hlReplace + ", " + rp[p]; } // ADD HL, rr (o IX, rr)
            } else if (z == 2) {
                if (q == 0) {
                    if (p == 0) mnemonic = "LD (BC), A";
                    else if (p == 1) mnemonic = "LD (DE), A";
                    else if (p == 2) { mnemonic = "LD (" + getNN(memory, pc) + "), " + hlReplace; size = 3; } // LD (nn), HL
                    else if (p == 3) { mnemonic = "LD (" + getNN(memory, pc) + "), A"; size = 3; }
                } else {
                    if (p == 0) mnemonic = "LD A, (BC)";
                    else if (p == 1) mnemonic = "LD A, (DE)";
                    else if (p == 2) { mnemonic = "LD " + hlReplace + ", (" + getNN(memory, pc) + ")"; size = 3; } // LD HL, (nn)
                    else if (p == 3) { mnemonic = "LD A, (" + getNN(memory, pc) + ")"; size = 3; }
                }
            } else if (z == 3) {
                if (q == 0) mnemonic = "INC " + rp[p]; // INC BC/DE/HL/SP (IX/IY)
                else        mnemonic = "DEC " + rp[p];
            } else if (z == 4) {
                mnemonic = "INC " + getRegName(y, isIndex, indexName, memory, pc, false); // INC r / (HL)
                if (isIndex && y == 6) size++; // (IX+d) consume byte
            } else if (z == 5) {
                mnemonic = "DEC " + getRegName(y, isIndex, indexName, memory, pc, false); // DEC r
                if (isIndex && y == 6) size++;
            } else if (z == 6) {
                mnemonic = "LD " + getRegName(y, isIndex, indexName, memory, pc, false) + ", " + getN(memory, pc + (isIndex && y==6 ? 1 : 0));
                size = 2;
                if (isIndex && y == 6) size++; // LD (IX+d), n son 4 bytes en total (Prefijo + Op + d + n)
            } else if (z == 7) {
                String[] rot = {"RLCA", "RRCA", "RLA", "RRA", "DAA", "CPL", "SCF", "CCF"};
                mnemonic = rot[y];
            }
        }
        // BLOQUE 1 (x=1) : LD r, r (Cargas de 8 bits)
        else if (x == 1) {
            if (z == 6 && y == 6) mnemonic = "HALT";
            else {
                // LD dest, src
                // Si usamos indexados, tenemos que leer el desplazamiento SI alguno es (HL) -> (IX+d)
                // Pero Z80 no permite LD (IX+d), (IX+d). Solo uno puede ser memoria.
                String dest = getRegName(y, isIndex, indexName, memory, pc, false);
                String src = getRegName(z, isIndex, indexName, memory, pc, false);
                mnemonic = "LD " + dest + ", " + src;
                if (isIndex && (z == 6 || y == 6)) size++; // +d
            }
        }
        // BLOQUE 2 (x=2) : ALU A, r
        else if (x == 2) {
            String opAlu = alu[y];
            String operand = getRegName(z, isIndex, indexName, memory, pc, false);
            mnemonic = opAlu + " " + operand;
            if (isIndex && z == 6) size++; // +d
        }
        // BLOQUE 3 (x=3) : Control y ALU n
        else if (x == 3) {
            if (z == 0) { mnemonic = "RET " + cc[y]; }
            else if (z == 1) {
                if (q == 0) mnemonic = "POP " + (p==2 ? isIndex?indexName:"HL" : rp2[p]); // POP rr
                else {
                    if (p == 0) mnemonic = "RET";
                    else if (p == 1) mnemonic = "EXX";
                    else if (p == 2) mnemonic = "JP (" + hlReplace + ")";
                    else mnemonic = "LD SP, " + hlReplace;
                }
            } else if (z == 2) { mnemonic = "JP " + cc[y] + ", " + getNN(memory, pc); size = 3; }
            else if (z == 3) {
                if (y == 0) { mnemonic = "JP " + getNN(memory, pc); size = 3; }
                else if (y == 1) { mnemonic = "CB Prefix (Bug)"; } // No debería llegar aquí
                else if (y == 2) { mnemonic = "OUT (" + getN(memory, pc) + "), A"; size = 2; }
                else if (y == 3) { mnemonic = "IN A, (" + getN(memory, pc) + ")"; size = 2; }
                else if (y == 4) { mnemonic = "EX (SP), " + hlReplace; }
                else if (y == 5) { mnemonic = "EX DE, HL"; }
                else if (y == 6) { mnemonic = "DI"; }
                else if (y == 7) { mnemonic = "EI"; }
            } else if (z == 4) { mnemonic = "CALL " + cc[y] + ", " + getNN(memory, pc); size = 3; }
            else if (z == 5) {
                if (q == 0) { mnemonic = "PUSH " + (p==2 ? isIndex?indexName:"HL" : rp2[p]); }
                else {
                    if (p == 0) { mnemonic = "CALL " + getNN(memory, pc); size = 3; }
                    else if (p == 1) { mnemonic = "DD Prefix"; }
                    else if (p == 2) { mnemonic = "ED Prefix"; }
                    else if (p == 3) { mnemonic = "FD Prefix"; }
                }
            } else if (z == 6) {
                mnemonic = alu[y] + " " + getN(memory, pc); size = 2;
            } else if (z == 7) {
                mnemonic = "RST " + String.format("%02XH", y * 8);
            }
        }

        // --- GESTIÓN DE BYTES PARA VISUALIZACIÓN ---
        // Si es una instrucción indexada que usa desplazamiento, debemos leerlo e incluirlo en bytesStr
        // y ajustar el puntero del address.
        // Simplificamos: Si size > 1 y no es index, leemos normal.
        // Si es index, el prefijo ya se leyó fuera.

        if (isIndex) {
            // Reconstruimos los bytes reales para el string
            // El prefijo DD/FD ya está en decodeInstruction. Aquí retornamos el resto.
            // Si usamos d, está en PC+1 (el opcode es PC).
            StringBuilder bStr = new StringBuilder(String.format("%02X", op));
            if (size >= 2) { // Hay operando (d o n o nn)
                // Caso feo: LD (IX+d), n -> Opcode(1) + d(1) + n(1).
                // La lógica anterior incrementó size.
                for (int k=1; k<size; k++) bStr.append(String.format(" %02X", peek(memory, pc+k)));
            }
            return new Instruction(originalPC, mnemonic, bStr.toString(), size); // size aquí no incluye el prefijo
        } else {
            StringBuilder bStr = new StringBuilder();
            for (int k=0; k<size; k++) bStr.append(String.format("%02X ", peek(memory, pc+k)));
            return new Instruction(pc, mnemonic, bStr.toString().trim(), size);
        }
    }

    // Decodificador especial para re-encapsular lógica Indexada limpia
    private static Instruction decodeIndexMain(byte[] memory, int pc, String idx) {
        // Esta función es un wrapper para llamar a decodeMain pero gestionando bien el byte de desplazamiento
        // que en código máquina está DESPUÉS del opcode.
        // DD [Opcode] [d] [n...]
        int op = peek(memory, pc + 1);
        Instruction i = decodeMain(memory, pc + 1, true, pc);

        // Ajuste final de string de bytes (Prefijo + Instruccion base)
        String fullBytes = String.format("%02X %s", peek(memory, pc), i.bytesStr);
        return new Instruction(pc, i.opcode, fullBytes, i.size + 1); // +1 por el prefijo
    }

    private static String getRegName(int idx, boolean isIndex, String idxName, byte[] memory, int pc, boolean ignoreD) {
        String[] r = {"B", "C", "D", "E", "H", "L", "(HL)", "A"};
        if (!isIndex) return r[idx];

        if (idx == 4) return idxName + "H"; // IXH / IYH
        if (idx == 5) return idxName + "L"; // IXL / IYL
        if (idx == 6) {
            // (IX+d)
            int d = peek(memory, pc + 1);
            return "(" + idxName + formatDisplacement(d) + ")";
        }
        return r[idx];
    }

    // --- DECODIFICADORES ESPECÍFICOS (CB, ED) ---

    private static String decodeCB(int op, String overrideOperand) {
        int x = (op >> 6) & 0x03;
        int y = (op >> 3) & 0x07;
        int z = op & 0x07;

        String[] r = {"B", "C", "D", "E", "H", "L", "(HL)", "A"};
        String operand = (overrideOperand != null) ? overrideOperand : r[z];

        if (x == 0) {
            String[] rots = {"RLC", "RRC", "RL", "RR", "SLA", "SRA", "SLL", "SRL"};
            return rots[y] + " " + operand;
        } else if (x == 1) {
            return "BIT " + y + ", " + operand;
        } else if (x == 2) {
            return "RES " + y + ", " + operand;
        } else if (x == 3) {
            return "SET " + y + ", " + operand;
        }
        return "UNK CB";
    }

    private static String decodeED(int op, byte[] memory, int pc) {
        // Bloque ED es complejo, implementamos los comunes del Spectrum
        int x = (op >> 6) & 0x03;
        int y = (op >> 3) & 0x07;
        int z = op & 0x07;
        int p = (y >> 1);
        int q = y & 1;

        if (x == 1) {
            if (z == 0) return "IN " + (y!=6?"(C)":"(C)") + ", " + (y==6?"0":new String[]{"B","C","D","E","H","L","(HL)","A"}[y]);
            if (z == 1) return "OUT (C), " + (y==6?"0":new String[]{"B","C","D","E","H","L","(HL)","A"}[y]);
            if (z == 2) return (q==0 ? "SBC" : "ADC") + " HL, " + new String[]{"BC","DE","HL","SP"}[p];
            if (z == 3) return "LD (" + getNN(memory, pc) + "), " + new String[]{"BC","DE","HL","SP"}[p]; // 4 bytes
            if (z == 4) return "NEG";
            if (z == 5) return "RETN";
            if (z == 6) return "IM " + ((y==0||y==4)?0 : (y==2||y==6)?1 : 2);
            if (z == 7) return "LD I, A"; // Simplificado
        }
        if (x == 2) {
            if (z == 0 && y == 4) return "LDI";
            if (z == 0 && y == 5) return "LDD";
            if (z == 0 && y == 6) return "LDIR";
            if (z == 0 && y == 7) return "LDDR";
            // ... Otros bloques CPI, CPIR, etc.
            if (z == 1 && y == 6) return "CPIR";
            if (z == 1 && y == 7) return "CPDR";
        }
        return "ED " + String.format("%02X", op);
    }

    // --- UTILIDADES ---

    private static int peek(byte[] mem, int addr) {
        return mem[addr & 0xFFFF] & 0xFF;
    }

    private static String getNN(byte[] mem, int pc) {
        int low = peek(mem, pc + 1);
        int high = peek(mem, pc + 2);
        int val = (high << 8) | low;
        return String.format("0x%04X", val);
    }

    private static String getN(byte[] mem, int pc) {
        return String.format("0x%02X", peek(mem, pc + 1));
    }

    private static String getRelAddr(byte[] mem, int pc) {
        int offset = peek(mem, pc + 1);
        if (offset > 127) offset -= 256;
        int addr = (pc + 2 + offset) & 0xFFFF;
        return String.format("0x%04X", addr);
    }

    private static String formatDisplacement(int d) {
        if (d > 127) d -= 256;
        if (d >= 0) return "+" + d;
        return String.valueOf(d);
    }
}