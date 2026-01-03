package com.lazyzxsoftware.zxspectrumide.emulator.core;

import com.lazyzxsoftware.zxspectrumide.emulator.impl.Spectrum48k;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;


public class Z80 {
    private final Memory mem;
    private final Spectrum48k machine;

    // Registros
    public int A, F, B, C, D, E, H, L;
    public int A_, F_, B_, C_, D_, E_, H_, L_;
    public int IX, IY, SP, PC;
    public int I, R;
    public int IM;
    public boolean IFF1, IFF2, Halted;
    public int MEMPTR;

    // --- DEBUG: CAJA NEGRA DE PC ---
    private final int[] pcHistory = new int[10000];
    private int pcIdx = 0;
    private int instructionsExecuted = 0;
    private boolean logDumped = false;

    // --- NUEVO SISTEMA DE LOG A FICHERO ---
    private PrintWriter fileLogger;
    private long totalInstructions = 0;
    private static final String LOG_PATH = "build/initialize_emulator_log.txt";

    private static final int LOG_INTERVAL = 10000;
    private StringBuilder stepLog = new StringBuilder();

    public long tStates;
    private int indexMode = 0; // 0=HL, 1=IX, 2=IY

    // Flags Masks
    private static final int SF=0x80, ZF=0x40, YF=0x20, HF=0x10, XF=0x08, PF=0x04, NF=0x02, CF=0x01;
    private static final int[] SZ53P = new int[0x100];
    private static final int[] SZ53 = new int[0x100];

    // Tablas de Half-Carry y Overflow para sumas
    private static final int[] HC_ADD = {0, HF, HF, HF, 0, 0, 0, HF};
    private static final int[] OV_ADD = {0, 0, 0, PF, PF, 0, 0, 0};

    // Tablas de Half-Carry y Overflow para restas (usadas por CP)
    private static final int[] HC_SUB = {0, 0, HF, 0, HF, 0, HF, HF};
    private static final int[] OV_SUB = {0, PF, 0, 0, 0, 0, PF, 0};

    static {
        for (int i = 0; i < 256; i++) {
            SZ53[i] = i & (SF | YF | XF);
            if (i == 0) SZ53[i] |= ZF;

            int p = 0;
            for (int b = 0; b < 8; b++) { if (((i >> b) & 1) != 0) p++; }
            SZ53P[i] = SZ53[i] | ((p & 1) == 0 ? PF : 0);
        }
    }

    public Z80(Memory memory, Spectrum48k machine) {
        this.mem = memory;
        this.machine = machine;
        initFileLogger();
        reset();
    }

    private void initFileLogger() {
        try {
            File dir = new File("build");
            if (!dir.exists()) dir.mkdirs();
            // Append = false para que cada ejecución empiece de cero
            this.fileLogger = new PrintWriter(new FileWriter(LOG_PATH, false));
            System.out.println("📝 Log de CPU iniciado en: " + LOG_PATH);
        } catch (IOException e) {
            System.err.println("❌ No se pudo crear el fichero de log: " + e.getMessage());
        }
    }

    public void reset() {
        PC = 0; SP = 0xFFFF;
        A = 0; F = 0; // Valor real de hardware tras power-on
        B = C = D = E = H = L = 0;
        IX = 0xFFFF; IY = 0xFFFF;
        I = 0; R = 0;
        IM = 0;
        indexMode = 0; // <--- MUY IMPORTANTE
        IFF1 = IFF2 = false;
        Halted = false;
        tStates = 0;
    }

    public int getPC() { return PC; }
    public long getTStates() { return tStates; }
    public void resetTStates() { tStates = 0; }

    public void step() {
        int aPrevia = A;

        if (Halted) {
            tStates += 4;
            return;
        }

        if (fileLogger != null) {
            if (PC == 0x11DF) {
                // Cuando la ROM llega a CP H, imprimimos los valores
                fileLogger.printf("[%04X] CP H -> A:%02X | H:%02X | ZF:%b%n",
                        PC, A, getH(), (F & ZF) != 0);
            } else {
                // El resto de instrucciones las seguimos logueando normal
                fileLogger.printf("%04X -> ", PC);
                totalInstructions++;
                if (totalInstructions % 10 == 0) fileLogger.println();
            }

            // Sincronizar el fichero cada 1000 instrucciones para no perder el rastro
            if (totalInstructions % 1000 == 0) {
                fileLogger.flush();
                System.out.println("💾 Log guardado: " + totalInstructions + " instrucciones escritas.");
            }
        }

        if (PC == 0x0E5C) {
            fileLogger.printf("[LDIR DETECTADO] BC:%04X | HL:%04X | DE:%04X%n",
                    (B << 8 | C), (H << 8 | L), (D << 8 | E));
        }

        // --- AÑADE ESTO AQUÍ ---
        if (PC == 0x11DC) {
            // En 11DC la ROM hace LD (HL), 2. Leemos la memoria JUSTO después de que actúe.
            // Pero como estamos ANTES de ejecutar, el log será más útil si lo ponemos
            // también justo DESPUÉS de ejecutar ese opcode.
            fileLogger.printf("[DEBUG RAM] PC:11DC - HL:%04X - Intentando escribir valor 0x02%n", getHL());
        }

        if (PC == 0x11DE) {
            // En 11DE la ROM hace LD H, (HL). Queremos ver qué hay en la memoria antes de cargarlo en H.
            int valorEnMemoria = mem.read(getHL());
            fileLogger.printf("[DEBUG RAM] PC:11DE - HL:%04X - Memoria contiene: %02X - Registro H actual: %02X%n",
                    getHL(), valorEnMemoria, H);
        }

        int op = fetch();
        indexMode = 0; // Reset por defecto

        while (op == 0xDD || op == 0xFD) {
            indexMode = (op == 0xDD) ? 1 : 2;
            op = fetch();
        }

        if (op == 0xED) {
            indexMode = 0;
            decodeED();
        } else if (op == 0xCB) {
            decodeCB();
        } else {
            executeOpcode(op);
        }
        indexMode = 0; // <--- FORZAR RESET TRAS EJECUCIÓN

        if (aPrevia == 0 && A == 0x3F) {
            fileLogger.printf("‼️ ¡ALERTA! A ha cambiado de 00 a 3F en el PC: %04X ejecutando Opcode anterior%n", pcHistory[(pcIdx - 1 + 10000) % 10000]);
            fileLogger.flush();
        }
    }

    private void decodeCB() {
        int d = 0;
        // Si venimos de un prefijo DD o FD, el desplazamiento 'd' va ANTES del opcode
        if (indexMode != 0) {
            d = (byte) fetch();
        }

        int op = fetch();
        int bit = (op >> 3) & 0x07;
        int reg = op & 0x07;

        // Calcular la dirección de memoria si es necesario
        int addr = 0;
        if (indexMode != 0) {
            addr = ((indexMode == 1 ? IX : IY) + d) & 0xFFFF;
        } else if (reg == 6) {
            addr = (H << 8) | L;
        }

        // Leer el valor sobre el que vamos a operar
        int val = (indexMode != 0 || reg == 6) ? mem.read(addr) : getReg(reg);
        int res = val;

        // --- Identificar la operación ---
        if ((op & 0xC0) == 0x40) { // BIT n, r
            res = val & (1 << bit);
            F = (F & CF) | HF | (res == 0 ? ZF | PF : 0) | (res & SF);
            // Flags X e Y en modo indexado vienen del byte alto de la dirección calculada
            if (indexMode != 0) {
                F = (F & ~(YF | XF)) | ((addr >> 8) & (YF | XF));
            } else {
                F = (F & ~(YF | XF)) | (val & (YF | XF));
            }
        } else if ((op & 0xC0) == 0x80) { // RES n, r
            res = val & ~(1 << bit);
            storeResultCB(reg, addr, res);
        } else if ((op & 0xC0) == 0xC0) { // SET n, r
            res = val | (1 << bit);
            storeResultCB(reg, addr, res);
        } else { // Rotaciones y Desplazamientos (0x00 - 0x3F)
            int carry = (F & CF);
            switch ((op >> 3) & 0x07) {
                case 0: // RLC
                    F = (val >> 7); res = ((val << 1) | F) & 0xFF; break;
                case 1: // RRC
                    F = (val & 0x01); res = ((val >> 1) | (F << 7)) & 0xFF; break;
                case 2: // RL
                    res = ((val << 1) | carry) & 0xFF; F = (val >> 7); break;
                case 3: // RR
                    res = ((val >> 1) | (carry << 7)) & 0xFF; F = (val & 0x01); break;
                case 4: // SLA
                    F = (val >> 7); res = (val << 1) & 0xFF; break;
                case 5: // SRA
                    F = (val & 0x01); res = ((val >> 1) | (val & 0x80)) & 0xFF; break;
                case 6: // SLL (Instrucción no documentada pero usada)
                    F = (val >> 7); res = ((val << 1) | 0x01) & 0xFF; break;
                case 7: // SRL
                    F = (val & 0x01); res = (val >> 1) & 0xFF; break;
            }
            F = SZ53P[res] | (F & CF);
            storeResultCB(reg, addr, res);
        }

        // Ajuste de ciclos (JSpeccy precise)
        tStates += (indexMode != 0) ? 19 : (reg == 6 ? 11 : 4);
    }

    // Método auxiliar para guardar el resultado en CB
    private void storeResultCB(int reg, int addr, int res) {
        if (indexMode != 0) {
            mem.write(addr, res);
            // En instrucciones DDCB/FDCB, si el registro no es (HL), también se guarda en el registro
            if (reg != 6) storeReg(reg, res);
        } else if (reg == 6) {
            mem.write(addr, res);
        } else {
            storeReg(reg, res);
        }
    }

    public void closeLogger() {
        if (fileLogger != null) {
            fileLogger.flush();
            fileLogger.close();
        }
    }

    /**
     * Traduce el índice de 3 bits del opcode al valor del registro correspondiente.
     * 0=B, 1=C, 2=D, 3=E, 4=H, 5=L, 6=(HL) - gestionado fuera, 7=A
     */
    private int getReg(int reg) {
        switch (reg) {
            case 0: return B;
            case 1: return C;
            case 2: return D;
            case 3: return E;
            case 4: return getH(); // Usamos getH/L por si hay prefijos IX/IY
            case 5: return getL();
            case 7: return A;
            default: return 0;
        }
    }

    /**
     * Guarda un valor en el registro indicado por el índice de 3 bits.
     */
    private void storeReg(int reg, int val) {
        switch (reg) {
            case 0: B = val & 0xFF; break;
            case 1: C = val & 0xFF; break;
            case 2: D = val & 0xFF; break;
            case 3: E = val & 0xFF; break;
            case 4: setH(val & 0xFF); break; // Usamos setH/L por compatibilidad con IX/IY
            case 5: setL(val & 0xFF); break;
            case 7: A = val & 0xFF; break;
        }
    }

    public void dumpPCStack(String reason) {
        logDumped = true;
        System.out.println("\n=== 🔴 DEBUG CRASH DUMP: " + reason + " ===");
        System.out.println("Últimos 10,000 saltos de PC (orden cronológico):");

        for (int i = 0; i < 10000; i++) {
            int val = pcHistory[(pcIdx + i) % 10000];
            if (val == 0 && i < 9900) continue; // Saltar ceros iniciales si el buffer no está lleno
            System.out.printf("%04X -> ", val);
            if (i % 10 == 9) System.out.println();
        }
        System.out.println("\n=== END OF DUMP ===\n");
    }

    // En Z80.java
    public void forceLogDump(String reason) {
        System.out.println("\n=== 🔴 FORCED DEBUG DUMP: " + reason + " ===");
        // Imprimir los últimos 200 saltos (10,000 es demasiado para la consola de IntelliJ a veces)
        int start = (pcIdx - 200 + 10000) % 10000;
        for (int i = 0; i < 200; i++) {
            int val = pcHistory[(start + i) % 10000];
            System.out.printf("%04X -> ", val);
            if (i % 10 == 9) System.out.println();
        }
        System.out.println("\n=== END OF DUMP ===\n");
    }

    private int fetch() {
        int val = mem.read(PC);
        PC = (PC + 1) & 0xFFFF;

        // El registro R (Refresco) aumenta en cada fetch de opcode
        // Solo aumentan los 7 bits bajos, el bit 7 se mantiene
        R = (R & 0x80) | ((R + 1) & 0x7F);

        return val;
    }

    // --- MÉTODOS ALU (RESTAURADOS DE JSPECCY) ---
    private void add(int val) {
        int a8 = A & 0xFF;
        int v8 = val & 0xFF;
        int res = a8 + v8;
        int res8 = res & 0xFF;

        F = (res8 == 0 ? ZF : 0) | (res8 & SF); // N es 0 en suma
        if ((res & 0x100) != 0) F |= CF; // Carry

        // Half Carry: Si la suma de los nibbles bajos desborda
        if (((a8 & 0x0F) + (v8 & 0x0F)) > 0x0F) F |= HF;

        // Overflow
        if (((a8 ^ ~v8) & (a8 ^ res8) & 0x80) != 0) F |= PF;

        A = res8;
    }

    private void adc(int v) {
        int cy = (F & CF);
        int r = A + v + cy;
        int c = A ^ v ^ r;
        F = SZ53[r & 0xFF] | ((r & 0x100) != 0 ? CF : 0) | (c & HF) | (((c ^ 0x80) & (v ^ r) & 0x80) != 0 ? PF : 0);
        A = r & 0xFF;
    }
    private void sub(int val) {
        int a8 = A & 0xFF;
        int v8 = val & 0xFF;
        int res = a8 - v8;
        int res8 = res & 0xFF;

        F = NF; // N=1 en resta
        if (res8 == 0) F |= ZF;
        if ((res8 & 0x80) != 0) F |= SF;
        if ((res & 0x100) != 0) F |= CF; // Borrow

        // Half-Carry (Resta): Si bit 4 pide préstamo al bit 3
        // ((A & 0x0F) - (val & 0x0F)) < 0
        if (((a8 & 0x0F) - (v8 & 0x0F)) < 0) F |= HF;

        // Overflow (Resta): (Operando signos distintos) Y (Resultado signo cambio incorrecto)
        // (A^val)&0x80  AND  (A^res)&0x80
        if (((a8 ^ v8) & (a8 ^ res8) & 0x80) != 0) F |= PF;

        A = res8;
    }

    private void sbc(int v) {
        int cy = (F & CF);
        int r = A - v - cy;
        int c = A ^ v ^ r;
        F = SZ53[r & 0xFF] | NF | ((r & 0x100) != 0 ? CF : 0) | (c & HF) | (((v ^ A) & (A ^ r) & 0x80) != 0 ? PF : 0);
        A = r & 0xFF;
    }

    private void cp(int value) {
        int temp = A - (value & 0xFF); // Resta temporal
        F = (F & ~(SF | ZF | HF | PF | NF | CF)); // Limpiar flags afectados

        if ((temp & 0xFF) == 0) F |= ZF; // Zero: Si resultado es 0
        if ((temp & 0x80) != 0) F |= SF; // Signo
        if ((A & 0x0F) < (value & 0x0F)) F |= HF; // Half Carry
        if (((A ^ value) & (A ^ temp) & 0x80) != 0) F |= PF; // Overflow
        F |= NF; // Add/Sub: Siempre 1 en CP
        if (temp < 0) F |= CF; // Carry: Si hubo préstamo
        // IMPORTANTE: NO guardamos temp en A. CP solo compara.
    }

    private void and(int val) {
        A &= (val & 0xFF);
        // AND: C=0, N=0, H=1 (Estándar Z80), S, Z, P(Paridad)
        F = HF; // H siempre 1 en AND
        updateLogicFlags();
    }
    // Auxiliar para flags lógicos (S, Z, P)
    private void updateLogicFlags() {
        if (A == 0) F |= ZF;
        if ((A & 0x80) != 0) F |= SF;

        // P/V en lógica actúa como PARIDAD (número par de bits a 1)
        int bits = Integer.bitCount(A);
        if ((bits % 2) == 0) F |= PF;
    }

    private void or(int val) {
        A |= (val & 0xFF);
        // OR: C=0, N=0, H=0, S, Z, P(Paridad)
        F = 0;
        updateLogicFlags();
    }

    private void xor(int val) {
        A ^= (val & 0xFF);
        // XOR: C=0, N=0, H=0, S, Z, P(Paridad)
        F = 0;
        updateLogicFlags();
    }

    private void add16(int v) {
        int hl = getHL_val();
        int r = hl + v;
        int c = hl ^ r ^ v;
        F = (F & (SF | ZF | PF)) | ((r & 0x10000) != 0 ? CF : 0) | ((r >> 8) & (YF | XF)) | ((c >> 8) & HF);
        setHL(r & 0xFFFF);
    }

    private void executeOpcode(int op) {
        int cycles = 4;
        switch (op) {
            case 0x00: break;
            case 0x01: // LD BC, nn
                C = mem.read(PC++) & 0xFF; // Primero el bajo
                B = mem.read(PC++) & 0xFF; // Luego el alto
                cycles = 10;
                break;
            case 0x02:
                mem.write(((B & 0xFF) << 8) | (C & 0xFF), A);
                cycles = 7;
                break;
            case 0x03: { int v=(B<<8|C)+1; B=(v>>8)&0xFF; C=v&0xFF; cycles=6; break; }
            case 0x04: B=inc(B); break;
            case 0x05: B=dec(B); break;
            case 0x06: B=fetch(); cycles=7; break;
            case 0x07: { int b7=A>>7; A=((A<<1)|b7)&0xFF; F=(F&(SF|ZF|PF))|b7|(A&(YF|XF)); break; }
            case 0x08: { int t=A; A=A_; A_=t; t=F; F=F_; F_=t; break; }
            case 0x09: add16(B<<8|C); cycles=11; break;
            case 0x0A: A=mem.read(((B & 0xFF) << 8) | (C & 0xFF)); cycles=7; break;
            case 0x0B: { int v=(B<<8|C)-1; B=(v>>8)&0xFF; C=v&0xFF; cycles=6; break; }
            case 0x0C: C=inc(C); break;
            case 0x0D: C=dec(C); break;
            case 0x0E: C=fetch(); cycles=7; break;
            case 0x0F: { int b0=A&1; A=((A>>1)|(b0<<7))&0xFF; F=(F&(SF|ZF|PF))|b0|(A&(YF|XF)); break; }
            case 0x10: B=(B-1)&0xFF; if(B!=0) jr(); else {PC++; cycles=8;} break;
            case 0x11: // LD DE, nn
                E = mem.read(PC++) & 0xFF; // Primero el bajo
                D = mem.read(PC++) & 0xFF; // Luego el alto
                cycles = 10;
                break;
            case 0x12: mem.write(((D & 0xFF) << 8) | (E & 0xFF), A); cycles=7; break;
            case 0x13: { int v=(D<<8|E)+1; D=(v>>8)&0xFF; E=v&0xFF; cycles=6; break; }
            case 0x14: D=inc(D); break;
            case 0x15: D=dec(D); break;
            case 0x16: D=fetch(); cycles=7; break;
            case 0x17: { int b7=A>>7; int oldC=F&CF; A=((A<<1)|oldC)&0xFF; F=(F&(SF|ZF|PF))|b7|(A&(YF|XF)); break; }
            case 0x18: jr(); break;
            case 0x19: add16(D<<8|E); cycles=11; break;
            case 0x1A: A=mem.read(D<<8|E); cycles=7; break;
            case 0x1B: { int v=(D<<8|E)-1; D=(v>>8)&0xFF; E=v&0xFF; cycles=6; break; }
            case 0x1C: E=inc(E); break;
            case 0x1D: E=dec(E); break;
            case 0x1E: E=fetch(); cycles=7; break;
            case 0x1F: { int b0=A&1; int oldC=F&CF; A=((A>>1)|(oldC<<7))&0xFF; F=(F&(SF|ZF|PF))|b0|(A&(YF|XF)); break; }
            case 0x20: if ((F & ZF) == 0) jr(); else { PC++; } break;
            case 0x21: // LD HL, nn
                int low = fetch();  // PRIMERO el bajo
                int high = fetch(); // LUEGO el alto
                setHL((high << 8) | low);
                break;
            case 0x22: { int ad=fetch()|(fetch()<<8); mem.write(ad,getL()); mem.write(ad+1,getH()); cycles=16; break; }
            case 0x23: setHL((getHL_val()+1)&0xFFFF); cycles=6; break;
            case 0x24: setH(inc(getH())); break;
            case 0x25: setH(dec(getH())); break;
            case 0x26: setH(fetch()); cycles=7; break;
            case 0x27: daa(); break;
            case 0x28: if((F&ZF)!=0) jr(); else {PC++; cycles=7;} break;
            case 0x29: add16(getHL_val()); cycles=11; break;
            case 0x2A: { int ad=fetch()|(fetch()<<8); setL(mem.read(ad)); setH(mem.read(ad+1)); cycles=16; break; }
            case 0x2B: setHL((getHL_val()-1)&0xFFFF); cycles=6; break;
            case 0x2C: setL(inc(getL())); break;
            case 0x2D: setL(dec(getL())); break;
            case 0x2E: setL(fetch()); cycles=7; break;
            case 0x2F: A^=0xFF; F=(F&(SF|ZF|PF|CF))|HF|NF|(A&(YF|XF)); break;
            case 0x30: if((F&CF)==0) jr(); else {PC++; cycles=7;} break;
            case 0x31: // LD SP, nn
                int spLow = mem.read(PC++) & 0xFF;
                int spHigh = mem.read(PC++) & 0xFF;
                SP = ((spHigh << 8) | spLow) & 0xFFFF;
                cycles = 10;
                break;
            case 0x32: { int ad=fetch()|(fetch()<<8); mem.write(ad,A); cycles=13; break; }
            case 0x33: SP=(SP+1)&0xFFFF; cycles=6; break;
            case 0x34: writeMemHL(inc(readMemHL())); cycles=11; break;
            case 0x35: writeMemHL(dec(readMemHL())); cycles=11; break;
            case 0x36: writeMemHL(fetch()); cycles=10; break;
            case 0x37: F=(F&(SF|ZF|PF))|(A&(YF|XF))|CF; break;
            case 0x38: if((F&CF)!=0) jr(); else {PC++; cycles=7;} break;
            case 0x39: add16(SP); cycles=11; break;
            case 0x3A: { int ad=fetch()|(fetch()<<8); A=mem.read(ad); cycles=13; break; }
            case 0x3B: SP=(SP-1)&0xFFFF; cycles=6; break;
            case 0x3C: A=inc(A); break;
            case 0x3D: A=dec(A); break;
            case 0x3E: A=fetch(); cycles=7; break;
            case 0x3F: F=(F&(SF|ZF|PF|CF))|((F&CF)!=0?HF:0)|(A&(YF|XF)); F^=CF; F&=~NF; break;
            case 0x40: break;
            case 0x41: B=C; break;
            case 0x42: B=D; break;
            case 0x43: B=E; break;
            case 0x44: B=getH(); break;
            case 0x45: B=getL(); break;
            case 0x46: B=readMemHL(); cycles=7; break;
            case 0x47: B=A; break;
            case 0x48: C=B; break;
            case 0x49: break;
            case 0x4A: C=D; break;
            case 0x4B: C=E; break;
            case 0x4C: C=getH(); break;
            case 0x4D: C=getL(); break;
            case 0x4E: C=readMemHL(); cycles=7; break;
            case 0x4F: C=A; break;
            case 0x50: D=B; break;
            case 0x51: D=C; break;
            case 0x52: break;
            case 0x53: D=E; break;
            case 0x54: D=getH(); break;
            case 0x55: D=getL(); break;
            case 0x56: D=readMemHL(); cycles=7; break;
            case 0x57:
                A = I;
                // LD A, I afecta a los flags de una forma especial (copia IFF2 a PV)
                F = (F & CF) | SZ53[A] | (IFF2 ? PF : 0);
                tStates += 9;
                break;
            case 0x58: E=B; break;
            case 0x59: E=C; break;
            case 0x5A: E=D; break;
            case 0x5C: E=getH(); break;
            case 0x5D: E=getL(); break;
            case 0x5E: E=readMemHL(); cycles=7; break;
            case 0x5F: E=A; break;
            case 0x60: setH(B); break;
            case 0x61: setH(C); break;
            case 0x62: setH(D); break;
            case 0x63: setH(E); break;
            case 0x64: break;
            case 0x65: setH(getL()); break;
            case 0x66: // LD H, (HL)
                setH(mem.read(getHL_val()) & 0xFF);
                cycles = 7;
                break;
            case 0x67: setH(A); break;
            case 0x68: setL(B); break;
            case 0x69: setL(C); break;
            case 0x6A: setL(D); break;
            case 0x6B: setL(E); break;
            case 0x6C: setL(getH()); break;
            case 0x6D: break;
            case 0x6E: setL(readMemHL()); cycles=7; break;
            case 0x6F: setL(A); break;
            case 0x70: writeMemHL(B); cycles=7; break;
            case 0x71: writeMemHL(C); cycles=7; break;
            case 0x72: writeMemHL(D); cycles=7; break;
            case 0x73: writeMemHL(E); cycles=7; break;
            case 0x74: writeMemHL(getH()); cycles=7; break;
            case 0x75: writeMemHL(getL()); cycles=7; break;
            case 0x76: Halted=true; break;
            case 0x77: writeMemHL(A); cycles=7; break;
            case 0x78: A=B; break;
            case 0x79: A=C; break;
            case 0x7A: A=D; break;
            case 0x7B: A=E; break;
            case 0x7C: A=getH(); break;
            case 0x7D: A=getL(); break;
            case 0x7E: A = mem.read(getHL());
                break;
            case 0x7F: break;
            case 0x80: add(B); break;
            case 0x81: add(C); break;
            case 0x82: add(D); break;
            case 0x83: add(E); break;
            case 0x84: add(getH()); break;
            case 0x85: add(getL()); break;
            case 0x86: add(readMemHL()); cycles=7; break;
            case 0x87: add(A); break;
            case 0x88: adc(B); break;
            case 0x89: adc(C); break;
            case 0x8A: adc(D); break;
            case 0x8B: adc(E); break;
            case 0x8C: adc(getH()); break;
            case 0x8D: adc(getL()); break;
            case 0x8E: adc(readMemHL()); cycles=7; break;
            case 0x8F: adc(A); break;
            case 0x90: sub(B); break;
            case 0x91: sub(C); break;
            case 0x92: sub(D); break;
            case 0x93: sub(E); break;
            case 0x94: sub(getH()); break;
            case 0x95: sub(getL()); break;
            case 0x96: sub(readMemHL()); cycles=7; break;
            case 0x97: sub(A); break;
            case 0x98: sbc(B); break;
            case 0x99: sbc(C); break;
            case 0x9A: sbc(D); break;
            case 0x9B: sbc(E); break;
            case 0x9C: sbc(getH()); break;
            case 0x9D: sbc(getL()); break;
            case 0x9E: sbc(readMemHL()); cycles=7; break;
            case 0x9F: sbc(A); break;
            case 0xA0: and(B); break;
            case 0xA1: and(C); break;
            case 0xA2: and(D); break;
            case 0xA3: and(E); break;
            case 0xA4: and(getH()); break;
            case 0xA5: and(getL()); break;
            case 0xA6: and(readMemHL()); cycles=7; break;
            case 0xA7: and(A); break;
            case 0xA8: xor(B); break;
            case 0xA9: xor(C); break;
            case 0xAA: xor(D); break;
            case 0xAB: xor(E); break;
            case 0xAC: xor(getH()); break;
            case 0xAD: xor(getL()); break;
            case 0xAE: xor(readMemHL()); cycles=7; break;
            case 0xAF: xor(A); break;
            case 0xB0: or(B); break;
            case 0xB1: or(C); break;
            case 0xB2: or(D); break;
            case 0xB3: or(E); break;
            case 0xB4: or(getH()); break;
            case 0xB5: or(getL()); break;
            case 0xB6: or(readMemHL()); cycles=7; break;
            case 0xB7: or(A); break;
            case 0xB8: cp(B); break;
            case 0xB9: cp(C); break;
            case 0xBA: cp(D); break;
            case 0xBB: cp(E); break;
            case 0xBC:
                cp(getH());
                break;
            case 0xBD: cp(getL()); break;
            case 0xBE: cp(readMemHL()); cycles=7; break;
            case 0xBF: cp(A); break;
            case 0xC0: if((F&ZF)==0) ret(); else cycles=5; break;
            case 0xC1: { int v=pop(); C=v&0xFF; B=v>>8; cycles=10; break; }
            case 0xC2: if((F&ZF)==0) jp(); else {PC+=2; cycles=10;} break;
            case 0xC3: jp(); cycles=10; break;
            case 0xC4: if((F&ZF)==0) call(); else {PC+=2; cycles=10;} break;
            case 0xC5: push(B<<8|C); cycles=11; break;
            case 0xC6: add(fetch()); cycles=7; break;
            case 0xC7: rst(0x00); break;
            case 0xC8: if((F&ZF)!=0) ret(); else cycles=5; break;
            case 0xC9: ret(); break;
            case 0xCA: if((F&ZF)!=0) jp(); else {PC+=2; cycles=10;} break;
            case 0xCB: decodeCB(); break;
            case 0xCC: if((F&ZF)!=0) call(); else {PC+=2; cycles=10;} break;
            case 0xCD: call(); cycles=17; break;
            case 0xCE: adc(fetch()); cycles=7; break;
            case 0xCF: rst(0x08); break;
            case 0xD0: if((F&CF)==0) ret(); else cycles=5; break;
            case 0xD1: { int v=pop(); E=v&0xFF; D=v>>8; cycles=10; break; }
            case 0xD2: if((F&CF)==0) jp(); else {PC+=2; cycles=10;} break;
            case 0xD3: // OUT (n), A
                int port = fetch() | (A << 8);
                // ZEXALL a menudo usa el acumulador para enviar caracteres al terminal
                if ((port & 0xFF) == 0x01) { // Puerto común de salida en tests
                    System.out.print((char) A);
                }
                machine.output(port, A);
                break;
            case 0xD4: if((F&CF)==0) call(); else {PC+=2; cycles=10;} break;
            case 0xD5: push(D<<8|E); cycles=11; break;
            case 0xD6: sub(fetch()); cycles=7; break;
            case 0xD7: rst(0x10); break;
            case 0xD8: if((F&CF)!=0) ret(); else cycles=5; break;
            case 0xD9: { int t=B;B=B_;B_=t; t=C;C=C_;C_=t; t=D;D=D_;D_=t; t=E;E=E_;E_=t; t=H;H=H_;H_=t; t=L;L=L_;L_=t; break; }
            case 0xDA: if((F&CF)!=0) jp(); else {PC+=2; cycles=10;} break;
            case 0xDB: A=machine.input(fetch()|(A<<8)); cycles=11; break;
            case 0xDC: if((F&CF)!=0) call(); else {PC+=2; cycles=10;} break;
            case 0xDE: sbc(fetch()); cycles=7; break;
            case 0xDF: rst(0x18); break;
            case 0xE0: if((F&PF)==0) ret(); else cycles=5; break;
            case 0xE1: setHL(pop()); cycles=10; break;
            case 0xE2: if((F&PF)==0) jp(); else {PC+=2; cycles=10;} break;

            case 0xE3: { // EX (SP), HL
                int memlow = mem.read(SP);
                int memhigh = mem.read((SP + 1) & 0xFFFF);
                int valueAtStack = (memhigh << 8) | memlow;

                // Intercambiar con el valor actual de HL (considerando IX/IY)
                int currentHL = getHL_val();

                mem.write(SP, currentHL & 0xFF);
                mem.write((SP + 1) & 0xFFFF, (currentHL >> 8) & 0xFF);

                setHL(valueAtStack);
                cycles = 19;
                break;
            }

            case 0xE4: if((F&PF)==0) call(); else {PC+=2; cycles=10;} break;
            case 0xE5: push(getHL_val()); cycles=11; break;
            case 0xE6: and(fetch()); cycles=7; break;
            case 0xE7: rst(0x20); break;
            case 0xE8: if((F&PF)!=0) ret(); else cycles=5; break;
            case 0xE9: PC=getHL_val(); break;
            case 0xEA: if((F&PF)!=0) jp(); else {PC+=2; cycles=10;} break;
            case 0xEB: { int t=D;D=H;H=t; t=E;E=L;L=t; break; }
            case 0xEC: if((F&PF)!=0) call(); else {PC+=2; cycles=10;} break;
            case 0xED: decodeED(); break;
            case 0xEE: xor(fetch()); cycles=7; break;
            case 0xEF: rst(0x28); break;
            case 0xF0: if((F&SF)==0) ret(); else cycles=5; break;
            case 0xF1: { int v=pop(); F=v&0xFF; A=v>>8; cycles=10; break; }
            case 0xF2: if((F&SF)==0) jp(); else {PC+=2; cycles=10;} break;
            case 0xF3: IFF1=IFF2=false; break;
            case 0xF4: if((F&SF)==0) call(); else {PC+=2; cycles=10;} break;
            case 0xF5: push(A<<8|F); cycles=11; break;
            case 0xF6: or(fetch()); cycles=7; break;
            case 0xF7: rst(0x30); break;
            case 0xF8: if((F&SF)!=0) ret(); else cycles=5; break;
            case 0xF9: SP=getHL_val(); cycles=6; break;
            case 0xFA: if((F&SF)!=0) jp(); else {PC+=2; cycles=10;} break;
            case 0xFB: IFF1=IFF2=true; break;
            case 0xFC: if((F&SF)!=0) call(); else {PC+=2; cycles=10;} break;
            case 0xFE: cp(fetch()); cycles=7; break;
            case 0xFF: rst(0x38); break;
        }
        tStates += cycles;
    }

    private void ldir() {
        // 1. Transferir un byte: (DE) <- (HL)
        int val = mem.read(getHL());
        mem.write(getDE(), val);

        // 2. Incrementar HL y DE
        // Usamos & 0xFFFF para asegurar que si llega a FFFF dé la vuelta a 0000 correctamente
        setHL((getHL() + 1) & 0xFFFF);
        setDE((getDE() + 1) & 0xFFFF); // Necesitarás crear setDE() si no lo tienes

        // 3. Decrementar contador BC
        int bc = getBC() - 1;
        setBC(bc); // Necesitarás crear setBC()

        // 4. Flags (Específicos de LDIR)
        // H = 0, N = 0, P/V = 0
        F &= ~(HF | NF | PF);

        // 5. Bucle
        if (getBC() != 0) {
            // Si quedan bytes, retrocedemos el PC para repetir esta instrucción
            PC = (PC - 2) & 0xFFFF; // -2 porque la instrucción es ED B0 (2 bytes)
            tStates += 21;
        } else {
            tStates += 16;
        }
    }

    private void lddr() {
        // 1. Transferir un byte: (DE) <- (HL)
        int val = mem.read(getHL());
        mem.write(getDE(), val);

        // 2. Decrementar HL y DE (Hacia atrás)
        // Usamos & 0xFFFF para manejar el desbordamiento (0 - 1 = FFFF)
        setHL((getHL() - 1) & 0xFFFF);
        setDE((getDE() - 1) & 0xFFFF);

        // 3. Decrementar contador BC
        int bc = getBC() - 1;
        setBC(bc);

        // 4. Flags (Específicos de LDDR)
        // H = 0, N = 0, P/V = 0 (por defecto)
        F &= ~(HF | NF | PF);

        // 5. Bucle
        if (getBC() != 0) {
            F |= PF; // P/V a 1 si quedan bytes
            // Si quedan bytes, retrocedemos el PC para repetir esta instrucción
            PC = (PC - 2) & 0xFFFF;
            tStates += 21;
        } else {
            tStates += 16;
        }
    }

    private void jr() {
        int offset = (byte) fetch(); // El cast a (byte) fuerza el signo en Java

        // PC ya ha avanzado 1 byte en fetch(), así que offset se aplica desde la siguiente instrucción.
        // Matemáticamente en Java:
        PC = (PC + offset) & 0xFFFF; // & 0xFFFF para mantenerlo en rango 64KB

        tStates += 12; // Los saltos suelen tardar 12 ciclos
    }
    private void jp() { PC = fetch() | (fetch() << 8); }
    private void call() { int ad = fetch() | (fetch() << 8); push(PC); PC = ad; }
    private void ret() { PC = pop(); }

    private void rst(int addr) {
        push(PC); // Guardar la dirección de la siguiente instrucción
        PC = addr;
    }

    private void daa() {
        int t = A; int corr = 0;
        if ((F & HF) != 0 || (A & 0x0F) > 9) corr += 0x06;
        if ((F & CF) != 0 || A > 0x99) { corr += 0x60; F |= CF; }
        if ((F & NF) != 0) t -= corr; else t += corr;
        F = (F & CF) | SZ53P[t & 0xFF] | ((A ^ t) & HF);
        A = t & 0xFF;
    }

    private int getHL_val() {
        if (indexMode == 1) return IX & 0xFFFF;
        if (indexMode == 2) return IY & 0xFFFF;
        // IMPORTANTE: & 0xFF en ambos para evitar el error de HL:FFFF
        return ((H & 0xFF) << 8) | (L & 0xFF);
    }

    private int getH() {
        return indexMode == 0 ? (H & 0xFF) : (indexMode == 1 ? (IX >> 8) & 0xFF : (IY >> 8) & 0xFF);
    }

    private int getL() {
        return indexMode == 0 ? (L & 0xFF) : (indexMode == 1 ? IX & 0xFF : IY & 0xFF);
    }

    private void setH(int v) {
        v &= 0xFF; // Limpieza estricta
        if (indexMode == 0) H = v;
        else if (indexMode == 1) IX = (v << 8) | (IX & 0xFF);
        else IY = (v << 8) | (IY & 0xFF);
    }

    private void setL(int v) {
        if (indexMode == 0) L = v & 0xFF;
        else if (indexMode == 1) IX = (IX & 0xFF00) | (v & 0xFF);
        else IY = (IY & 0xFF00) | (v & 0xFF);
    }

    private int readMemHL() {
        int addr;
        if (indexMode == 0) {
            addr = (H << 8) | L;
        } else {
            // IMPORTANTE: El desplazamiento se lee de la memoria, NO se hace fetch (no avanza PC aún)
            int d = (byte) mem.read(PC);
            PC = (PC + 1) & 0xFFFF;
            addr = ((indexMode == 1 ? IX : IY) + d) & 0xFFFF;
            tStates += 5; // Las operaciones indexadas son más lentas
        }
        return mem.read(addr) & 0xFF; // Asegura siempre un valor 0-255
    }
    private void writeMemHL(int val) {
        int addr;
        if (indexMode == 0) {
            addr = (H << 8) | L;
        } else {
            int d = (byte) mem.read(PC);
            PC = (PC + 1) & 0xFFFF;
            addr = ((indexMode == 1 ? IX : IY) + d) & 0xFFFF;
            tStates += 5;
        }
        mem.write(addr, val);
    }

    private void decodeED() {
        int op = fetch();
        int cycles = 8; // Coste base aproximado de instrucciones ED

        switch (op) {
            // --- BLOQUE 1: ADC/SBC de 16 bits (Vitales para matemáticas) ---
            case 0x42: sbc16(B << 8 | C); break; // SBC HL, BC
            case 0x52: sbc16(D << 8 | E); break; // SBC HL, DE
            case 0x62: sbc16(getHL_val()); break; // SBC HL, HL
            case 0x72: sbc16(SP); break;         // SBC HL, SP

            case 0x4A: adc16(B << 8 | C); break; // ADC HL, BC
            case 0x5A: adc16(D << 8 | E); break; // ADC HL, DE
            case 0x6A: adc16(getHL_val()); break; // ADC HL, HL
            case 0x7A: adc16(SP); break;         // ADC HL, SP

            // --- BLOQUE 2: Cargas de 16 bits en memoria (Vitales para variables) ---
            case 0x43: // LD (nn), BC
                int ad = fetch() | (fetch() << 8);
                mem.write(ad, C); mem.write(ad + 1, B);
                cycles = 20; break;
            case 0x53: // LD (nn), DE
                ad = fetch() | (fetch() << 8);
                mem.write(ad, E); mem.write(ad + 1, D);
                cycles = 20; break;
            case 0x73: // LD (nn), SP
                ad = fetch() | (fetch() << 8);
                mem.write(ad, SP & 0xFF); mem.write(ad + 1, SP >> 8);
                cycles = 20; break;
            case 0x4B: // LD BC, (nn)
                int lowBC = fetch(); int highBC = fetch();
                int addrBC = (highBC << 8) | lowBC;
                C = mem.read(addrBC); B = mem.read((addrBC + 1) & 0xFFFF);
                cycles = 20; break;
            case 0x5B: // LD DE, (nn)
                int lowDE = fetch(); int highDE = fetch();
                int addrDE = (highDE << 8) | lowDE;
                E = mem.read(addrDE); D = mem.read((addrDE + 1) & 0xFFFF);
                cycles = 20; break;
            case 0x7B: // LD SP, (nn)
                ad = fetch() | (fetch() << 8);
                SP = mem.read(ad) | (mem.read(ad + 1) << 8);
                cycles = 20; break;

            // --- BLOQUE 3: Interrupciones y Estado (Vitales para la ROM) ---
            case 0x44: A = 0 - A; F = (A == 0 ? ZF : SF) | (A < 0 ? NF : 0); break; // NEG
            case 0x46: IM = 0; break; // IM 0
            case 0x56: IM = 1; break; // IM 1
            case 0x5E: IM = 2; break; // IM 2
            case 0x47: I = A; break; // LD I, A
            case 0x57: // LD A, I
                A = I;
                F = (F & CF) | SZ53[A] | (IFF2 ? PF : 0);
                tStates += 9;
                break;
            case 0x4D: // RETI
            case 0x45: // RETN
                PC = pop();
                IFF1 = IFF2;
                tStates += 14;
                break;

            // --- BLOQUE 4: Entrada/Salida ---
            case 0x78: // IN A, (C)
                int port = (B << 8) | C;
                int val = machine.input(port);
                A = val;
                F = (F & CF) | SZ53P[val]; // Actualizar flags
                cycles = 12;
                break;

            // --- BLOQUE 5: Transferencia de Bloques (LA NUEVA LÓGICA) ---
            // Asegúrate de tener los métodos ldir() y lddr() copiados en Z80.java
            case 0xB0: ldir(); break; // LDIR
            case 0xB8: lddr(); break; // LDDR

            // Repetir para CPIR, CPDR, INIR, OTIR si los tienes implementados
            // Si no, al menos LDIR y LDDR son obligatorios para que arranque.

            default:
                // Ignoramos instrucciones no soportadas (se comportan como NOP)
                System.err.println(String.format("Opcode ED %02X no implementado", op));
                break;
        }
        tStates += cycles;
    }

    // Método auxiliar para SBC HL, rr
    private void sbc16(int val) {
        int hl = getHL_val();
        int cy = (F & CF);
        int res = hl - val - cy;
        int lookup = (((hl & 0x0800) >> 11) | ((val & 0x0800) >> 10) | ((res & 0x0800) >> 9));
        F = NF | (res < 0 ? CF : 0) | (res == 0 ? ZF : 0) | ((res >> 8) & SF) |
                HC_SUB[lookup] | (((val ^ hl) & (hl ^ res) & 0x8000) != 0 ? PF : 0);
        setHL(res & 0xFFFF);
        tStates += 15;
    }

    public Memory getMem() {
        return mem; // O 'memory', según como hayas nombrado la variable en Z80
    }

    private void adc16(int val) {
        int hl = getHL_val();
        int cy = (F & CF);
        int res = hl + val + cy;

        // El secreto de los flags de 16 bits en JSpeccy:
        // Calculamos un lookup para el bit 11 (Half-Carry de 16 bits)
        int lookup = (((hl & 0x0800) >> 11) | ((val & 0x0800) >> 10) | ((res & 0x0800) >> 9));

        // Flags:
        // S, Z, Y, X se basan en el byte alto del resultado
        // H se basa en el acarreo del bit 11 (usando la tabla HC_ADD)
        // V (Overflow) se basa en el cambio de signo de los bits 15
        // N se pone a 0 (es una suma)
        // C es el acarreo del bit 15
        F = ((res >> 8) & (SF | YF | XF)) |
                ((res & 0xFFFF) == 0 ? ZF : 0) |
                ((res & 0x10000) != 0 ? CF : 0) |
                HC_ADD[lookup] |
                (((val ^ hl ^ 0x8000) & (val ^ res) & 0x8000) != 0 ? PF : 0);

        setHL(res & 0xFFFF);
        tStates += 15;
    }

    public void interrupt() {
        if (IFF1) {
            IFF1 = IFF2 = false;
            Halted = false;
            push(PC);
            PC = 0x0038;
            tStates += 13;
        }
    }

    // --- MÉTODOS DE REGISTROS SEGUROS ---
    public void setBC(int value) {
        B = (value >> 8) & 0xFF;
        C = value & 0xFF;
    }
    public void setDE(int value) {
        D = (value >> 8) & 0xFF;
        E = value & 0xFF;
    }
    public void setHL(int value) {
        H = (value >> 8) & 0xFF;
        L = value & 0xFF;
    }

    // Asegúrate de tener los getters correctos también
    public int getBC() { return ((B & 0xFF) << 8) | (C & 0xFF); }
    public int getDE() { return ((D & 0xFF) << 8) | (E & 0xFF); }
    public int getHL() { return ((H & 0xFF) << 8) | (L & 0xFF); }

    // --- STACK CORREGIDO ---
    public void push(int value) {
        SP = (SP - 1) & 0xFFFF;
        mem.write(SP, (value >> 8) & 0xFF); // Alto primero
        SP = (SP - 1) & 0xFFFF;
        mem.write(SP, value & 0xFF);        // Bajo después
    }

    public int pop() {
        int low = mem.read(SP) & 0xFF;      // Bajo primero
        SP = (SP + 1) & 0xFFFF;
        int high = mem.read(SP) & 0xFF;     // Alto después
        SP = (SP + 1) & 0xFFFF;
        return (high << 8) | low;
    }

    private int inc(int val) {
        int original = val & 0xFF;
        int res = (original + 1) & 0xFF;

        // INC afecta a todos los flags EXCEPTO Carry (CF se mantiene igual)
        int oldCarry = F & CF;
        F = (res == 0 ? ZF : 0) | (res & SF); // Z y S
        F |= oldCarry; // Restaurar Carry

        // Half-Carry: Si pasamos de xxxx1111 a xxxx0000 (0x0F -> 0x10)
        if ((original & 0x0F) == 0x0F) F |= HF;

        // Overflow (P/V): Solo ocurre si 0x7F -> 0x80
        if (original == 0x7F) F |= PF;

        return res;
    }

    private int dec(int val) {
        int original = val & 0xFF;
        int res = (original - 1) & 0xFF;

        int oldCarry = F & CF;
        F = (res == 0 ? ZF : 0) | (res & SF) | NF; // N se pone a 1 en DEC
        F |= oldCarry;

        // Half-Carry: Si pasamos de xxxx0000 a xxxx1111 (borrows from bit 4)
        if ((original & 0x0F) == 0x00) F |= HF;

        // Overflow: 0x80 -> 0x7F
        if (original == 0x80) F |= PF;

        return res;
    }
}