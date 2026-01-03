package com.lazyzxsoftware.zxspectrumide.emulator.core;

import com.lazyzxsoftware.zxspectrumide.emulator.interfaces.SpectrumEmulator;

public class Z80Core {
    // --- REGISTROS PRINCIPALES (8 bits) ---
    // CAMBIO IMPORTANTE: Ahora son PUBLIC para que el TestWindow pueda leerlos
    public int A, F;
    public int B, C;
    public int D, E;
    public int H, L;

    // --- REGISTROS ALTERNATIVOS ---
    public int A_, F_, B_, C_, D_, E_, H_, L_;

    // --- REGISTROS DE 16 BITS Y CONTROL ---
    public int IX, IY;
    public int SP, PC;
    public int I, R;

    // --- ESTADO ---
    public boolean IFF1, IFF2;
    public boolean Halted;
    public int indexMode = 0; // 0=HL, 1=IX, 2=IY
    public long tStates;

    // --- INTERFAZ CON EL MUNDO EXTERIOR ---
    public final MemoryCore mem;
    public final SpectrumEmulator machine;

    // --- FLAGS (MÁSCARAS) ---
    public static final int SF=0x80, ZF=0x40, YF=0x20, HF=0x10, XF=0x08, PF=0x04, NF=0x02, CF=0x01;

    public Z80Core(MemoryCore memory, SpectrumEmulator machine) {
        this.mem = memory;
        this.machine = machine;
        reset();
    }

    public void reset() {
        PC = 0; SP = 0xFFFF;
        A = F = B = C = D = E = H = L = 0;
        IX = IY = 0xFFFF;
        I = R = 0;
        IFF1 = IFF2 = false;
        Halted = false;
        tStates = 0;
    }

    // --- MÉTODOS AUXILIARES SEGUROS ---
    // Ahora son public también por si los necesitas en los tests
    public int getHL() {
        return ((H & 0xFF) << 8) | (L & 0xFF);
    }

    // Necesitas estos getters para los tests de 16 bits (BC y DE)
    public int getBC() {
        return ((B & 0xFF) << 8) | (C & 0xFF);
    }

    public int getDE() {
        return ((D & 0xFF) << 8) | (E & 0xFF);
    }

    // Y HL que ya tenías
    public void setHL(int value) {
        this.H = (value >> 8) & 0xFF;
        this.L = value & 0xFF;
    }

    // --- CICLO PRINCIPAL ---
    public void execute() {
        int opcode = fetch();

        switch (opcode) {
            case 0x00: // NOP
                break;

            // --- CARGAS DE 8 BITS ---
            case 0x06: B = fetch(); break; // LD B, n
            case 0x0E: C = fetch(); break; // LD C, n
            case 0x16: D = fetch(); break; // LD D, n
            case 0x1E: E = fetch(); break; // LD E, n
            case 0x26: H = fetch(); break; // LD H, n
            case 0x2E: L = fetch(); break; // LD L, n
            case 0x3E: A = fetch(); break; // LD A, n

            // --- CARGAS DE 16 BITS (OJO AQUÍ) ---
            case 0x01: // LD BC, nn
                C = fetch(); // Bajo
                B = fetch(); // Alto
                break;
            case 0x11: // LD DE, nn
                E = fetch(); // Bajo
                D = fetch(); // Alto
                break;
            case 0x21: // LD HL, nn
                int low = fetch();  // Bajo
                int high = fetch(); // Alto
                L = low;
                H = high;
                break;

            // --- CARGAS INDIRECTAS (LD r, (HL)) ---
            case 0x7E: // LD A, (HL)
                A = mem.read(getHL());
                break;
            case 0x46: // LD B, (HL)
                B = mem.read(getHL()) & 0xFF;
                break;
            // (Añade el resto según las necesites: C, D, E, H, L)

            // --- ALU: INC (Incrementar) ---
            case 0x04: B = inc(B); break;
            case 0x0C: C = inc(C); break;
            case 0x14: D = inc(D); break;
            case 0x1C: E = inc(E); break;
            case 0x24: H = inc(H); break;
            case 0x2C: L = inc(L); break;
            case 0x3C: A = inc(A); break;

            // --- ALU: DEC (Decrementar) ---
            case 0x05: B = dec(B); break;
            case 0x3D: A = dec(A); break; // Usado a menudo en bucles

            // --- ALU: ADD (Sumar) ---
            case 0x80: add(B); break; // ADD A, B
            case 0x87: add(A); break; // ADD A, A

            // --- ALU: CP (Comparar) ---
            case 0xB8: cp(B); break; // CP B
            case 0xB9: cp(C); break; // CP C
            case 0xBF: cp(A); break; // CP A
            // Nota: Faltan D, E, H, L, etc., añádelos según necesites.

            // --- ESCRITURA EN MEMORIA (LD (HL), n) ---
            case 0x36: // LD (HL), n
                mem.write(getHL(), fetch());
                break;

            case 0x76: // HALT
                Halted = true;
                break;

            // --- CONTROL DE FLUJO: SALTOS RELATIVOS ---
            case 0x18: jr(); break;       // JR d (Salto incondicional)
            case 0x20:                    // JR NZ, d
                if ((F & ZF) == 0) jr();
                else { PC++; tStates += 7; } // Si no salta, consumimos el byte de desplazamiento y tiempo
                break;
            case 0x28:                    // JR Z, d
                if ((F & ZF) != 0) jr();
                else { PC++; tStates += 7; }
                break;
            case 0x30:                    // JR NC, d
                if ((F & CF) == 0) jr();
                else { PC++; tStates += 7; }
                break;
            case 0x38:                    // JR C, d
                if ((F & CF) != 0) jr();
                else { PC++; tStates += 7; }
                break;

            // --- CONTROL DE FLUJO: DJNZ (Decrement Jump Non-Zero) ---
            // ¡La instrucción favorita de los bucles del Spectrum!
            case 0x10:
                B = dec(B); // Decrementamos B (usando tu método dec existente)
                if (B != 0) {
                    jr();   // Si no es cero, saltamos
                    tStates += 13; // Tiempo si salta
                } else {
                    PC++;   // Si es cero, ignoramos el salto
                    tStates += 8;  // Tiempo si no salta
                }
                break;

            // --- INSTRUCCIONES EXTENDIDAS (ED) ---
            case 0xED:
                decodeED();
                break;

            // --- STACK: PUSH/POP ---
            case 0xC5: push(getBC()); break; // PUSH BC
            case 0xD5: push(getDE()); break; // PUSH DE
            case 0xE5: push(getHL()); break; // PUSH HL
            case 0xF5: push((A << 8) | F); break; // PUSH AF (Ojo: AF se guarda combinado)

            case 0xC1: setBC(pop()); break; // POP BC
            case 0xD1: setDE(pop()); break; // POP DE
            case 0xE1: setHL(pop()); break; // POP HL
            case 0xF1: // POP AF
                int af = pop();
                A = (af >> 8) & 0xFF;
                F = af & 0xFF;
                break;

            // --- SUBRUTINAS: CALL/RET ---
            case 0xCD: // CALL nn
                int target = fetchWord(); // Lee la dirección destino (nn)
                push(PC);                 // Guarda la dirección de retorno (siguiente instr)
                PC = target;              // Salta
                break;

            case 0xC9: // RET
                PC = pop();               // Recupera la dirección de retorno
                break;

            // --- ALU: SUB (Resta) ---
            case 0x90: sub(B); break;
            case 0x97: sub(A); break;
            case 0xD6: sub(fetch()); break; // SUB n (Inmediato)

            // --- ALU: LÓGICA (AND, XOR, OR) ---
            case 0xA0: and(B); break;
            case 0xA1: and(C); break;
            case 0xA7: and(A); break;
            case 0xE6: and(fetch()); break; // AND n

            case 0xA8: xor(B); break;
            case 0xAF:
                System.out.println("⚡ EJECUTANDO CASE 0xAF (XOR A)");
                xor(A);
                break;
            case 0xEE: xor(fetch()); break; // XOR n

            case 0xA9: xor(C); break;
            case 0xAA: xor(D); break;
            case 0xAB: xor(E); break;
            case 0xAC: xor(H); break;
            case 0xAD: xor(L); break;

            case 0xB0: or(B); break;
            case 0xB7: or(A); break;
            case 0xF6: or(fetch()); break; // OR n

            // ADD Inmediato (usado en el test)
            case 0xC6: add(fetch()); break;

            default:
                System.err.println(String.format("Opcode no implementado: %02X en PC:%04X", opcode, (PC-1)&0xFFFF));
                Halted = true;
                break;
        }
    }

    public int fetch() {
        int val = mem.read(PC);
        // --- DEBUG TEMPORAL ---
        if (PC == 0x0000) { // Solo espiamos la primera instrucción para no llenar el log
            System.out.println(String.format("🔍 DEBUG FETCH -> PC:%04X | Opcode leído: %02X", PC, val));
        }
        // ----------------------
        PC = (PC + 1) & 0xFFFF;
        return val & 0xFF;
    }

    // Salto Relativo (Jump Relative)
    // Lee el siguiente byte como un desplazamiento con signo (-128 a +127) y lo suma al PC.
    private void jr() {
        int offset = (byte) fetch(); // El cast a (byte) fuerza el signo en Java

        // PC ya ha avanzado 1 byte en fetch(), así que offset se aplica desde la siguiente instrucción.
        // Matemáticamente en Java:
        PC = (PC + offset) & 0xFFFF; // & 0xFFFF para mantenerlo en rango 64KB

        tStates += 12; // Los saltos suelen tardar 12 ciclos
    }

    // --- MÉTODOS ALU CON FLAGS ---

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

    // Decodificador de instrucciones extendidas
    private void decodeED() {
        int op = fetch(); // Leemos el siguiente byte tras el ED

        switch (op) {
            case 0xB0: // LDIR (Load, Increment, Repeat)
                ldir();
                break;

            default:
                System.err.println(String.format("Opcode ED %02X no implementado", op));
                break;
        }
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

    // --- MÉTODOS AUXILIARES QUE FALTABAN ---

    // Implementación canónica y segura
    public void setBC(int value) {
        // value se trata como int.
        // (value >> 8) mueve el byte alto a la posición baja.
        // & 0xFF asegura que limpiamos cualquier basura o extensión de signo.
        this.B = (value >> 8) & 0xFF;
        this.C = value & 0xFF;
    }

    public void setDE(int value) {
        this.D = (value >> 8) & 0xFF;
        this.E = value & 0xFF;
    }

    // PUSH: Guarda 16 bits en la pila
    // Orden Z80: Primero empuja el byte ALTO, luego el BAJO.
    // La pila crece hacia abajo (direcciones menores).
    public void push(int value) {
        // Byte Alto
        SP = (SP - 1) & 0xFFFF;
        mem.write(SP, (value >> 8) & 0xFF);

        // Byte Bajo
        SP = (SP - 1) & 0xFFFF;
        mem.write(SP, value & 0xFF);
    }

    // POP: Recupera 16 bits de la pila
    // Orden Z80: Primero lee el byte BAJO, luego el ALTO.
    public int pop() {
        // Byte Bajo
        int low = mem.read(SP) & 0xFF;
        SP = (SP + 1) & 0xFFFF;

        // Byte Alto
        int high = mem.read(SP) & 0xFF;
        SP = (SP + 1) & 0xFFFF;

        return (high << 8) | low;
    }

    // Lee 16 bits del PC actual (Little Endian)
    public int fetchWord() {
        int low = fetch();
        int high = fetch();
        return (high << 8) | low;
    }

    // --- LÓGICA MATEMÁTICA Y FLAGS ---

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

    private void and(int val) {
        A &= (val & 0xFF);
        // AND: C=0, N=0, H=1 (Estándar Z80), S, Z, P(Paridad)
        F = HF; // H siempre 1 en AND
        updateLogicFlags();
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

    // Auxiliar para flags lógicos (S, Z, P)
    private void updateLogicFlags() {
        if (A == 0) F |= ZF;
        if ((A & 0x80) != 0) F |= SF;

        // P/V en lógica actúa como PARIDAD (número par de bits a 1)
        int bits = Integer.bitCount(A);
        if ((bits % 2) == 0) F |= PF;
    }
}