package com.lazyzxsoftware.zxspectrumide.emulator.core;

import com.lazyzxsoftware.zxspectrumide.emulator.impl.Spectrum48k;

public class Z80 {
    private final Memory mem;
    private final Spectrum48k machine;

    // Registros
    public int A, F, B, C, D, E, H, L;
    public int A_, F_, B_, C_, D_, E_, H_, L_;
    public int IX, IY, SP, PC;
    public int I, R;
    public boolean IFF1, IFF2, Halted;
    public int IM;
    private long tStates;

    // Flags
    private static final int SF=0x80, ZF=0x40, YF=0x20, HF=0x10, XF=0x08, PF=0x04, NF=0x02, CF=0x01;
    private static final boolean[] P = new boolean[256];
    private static final int[] SZ53 = new int[256];

    static {
        for (int i=0; i<256; i++) {
            boolean p=true; int b=i;
            for(int j=0; j<8; j++){ if((b&1)!=0) p=!p; b>>=1; }
            P[i]=p; SZ53[i]=i&(SF|YF|XF);
            if(i==0) SZ53[i]|=ZF;
        }
    }

    public Z80(Memory memory, Spectrum48k machine) {
        this.mem = memory;
        this.machine = machine;
        reset();
    }

    public void reset() {
        PC=0; SP=0xFFFF; A=F=B=C=D=E=H=L=0;
        IX=IY=0; I=R=0; IFF1=IFF2=false; IM=0; Halted=false; tStates=0;
    }

    public long getTStates() { return tStates; }

    public int getPC() { return PC; }

    public int step() {
        if (Halted) { tStates+=4; return 4; }

        int op = mem.read(PC);
        PC = (PC + 1) & 0xFFFF;
        R = (R & 0x80) | ((R + 1) & 0x7F);

        int cycles = 4;

        switch (op) {
            case 0x00: break; // NOP

            // --- GRUPO DE CARGA (LOAD) DE 8 BITS ---
            // LD r, r' (Cargas básicas entre registros)
            case 0x7F: break; // LD A,A
            case 0x78: A=B; break; case 0x79: A=C; break; case 0x7A: A=D; break; case 0x7B: A=E; break;
            case 0x7C: A=H; break; case 0x7D: A=L; break;
            case 0x47: B=A; break; case 0x40: break;      case 0x41: B=C; break; case 0x42: B=D; break; case 0x43: B=E; break; case 0x44: B=H; break; case 0x45: B=L; break;
            case 0x4F: C=A; break; case 0x48: C=B; break; case 0x49: break;      case 0x4A: C=D; break; case 0x4B: C=E; break; case 0x4C: C=H; break; case 0x4D: C=L; break;
            case 0x57: D=A; break; case 0x50: D=B; break; case 0x51: D=C; break; case 0x52: break;      case 0x53: D=E; break; case 0x54: D=H; break; case 0x55: D=L; break;
            case 0x5F: E=A; break; case 0x58: E=B; break; case 0x59: E=C; break; case 0x5A: E=D; break; case 0x5B: break;      case 0x5C: E=H; break; case 0x5D: E=L; break;
            case 0x67: H=A; break; case 0x60: H=B; break; case 0x61: H=C; break; case 0x62: H=D; break; case 0x63: H=E; break; case 0x64: break;      case 0x65: H=L; break;
            case 0x6F: L=A; break; case 0x68: L=B; break; case 0x69: L=C; break; case 0x6A: L=D; break; case 0x6B: L=E; break; case 0x6C: L=H; break; case 0x6D: break;

            // LD r, n (Carga inmediata)
            case 0x3E: A=mem.read(PC++); cycles=7; break;
            case 0x06: B=mem.read(PC++); cycles=7; break;
            case 0x0E: C=mem.read(PC++); cycles=7; break;
            case 0x16: D=mem.read(PC++); cycles=7; break;
            case 0x1E: E=mem.read(PC++); cycles=7; break;
            case 0x26: H=mem.read(PC++); cycles=7; break;
            case 0x2E: L=mem.read(PC++); cycles=7; break;

            // LD r, (HL) -> LEER DE MEMORIA (Faltaba gran parte de esto)
            case 0x7E: A=mem.read(H<<8|L); cycles=7; break;
            case 0x46: B=mem.read(H<<8|L); cycles=7; break;
            case 0x4E: C=mem.read(H<<8|L); cycles=7; break;
            case 0x56: D=mem.read(H<<8|L); cycles=7; break;
            case 0x5E: E=mem.read(H<<8|L); cycles=7; break;
            case 0x66: H=mem.read(H<<8|L); cycles=7; break;
            case 0x6E: L=mem.read(H<<8|L); cycles=7; break;

            // LD (HL), r -> ESCRIBIR EN MEMORIA (¡IMPORTANTE PARA PANTALLA!)
            case 0x77: mem.write(H<<8|L, A); cycles=7; break;
            case 0x70: mem.write(H<<8|L, B); cycles=7; break;
            case 0x71: mem.write(H<<8|L, C); cycles=7; break;
            case 0x72: mem.write(H<<8|L, D); cycles=7; break;
            case 0x73: mem.write(H<<8|L, E); cycles=7; break;
            case 0x74: mem.write(H<<8|L, H); cycles=7; break;
            case 0x75: mem.write(H<<8|L, L); cycles=7; break;
            case 0x36: mem.write(H<<8|L, mem.read(PC++)); cycles=10; break; // LD (HL), n

            // Otras cargas indirectas
            case 0x0A: A=mem.read(B<<8|C); cycles=7; break; // LD A,(BC)
            case 0x1A: A=mem.read(D<<8|E); cycles=7; break; // LD A,(DE)
            case 0x02: mem.write(B<<8|C, A); cycles=7; break; // LD (BC),A
            case 0x12: mem.write(D<<8|E, A); cycles=7; break; // LD (DE),A
            case 0x32: mem.write(mem.read(PC++)|(mem.read(PC++)<<8), A); cycles=13; break; // LD (nn),A
            case 0x3A: A=mem.read(mem.read(PC++)|(mem.read(PC++)<<8)); cycles=13; break; // LD A,(nn)

            // --- 16-BIT LOAD ---
            case 0x21: L=mem.read(PC++); H=mem.read(PC++); cycles=10; break; // LD HL,nn
            case 0x11: E=mem.read(PC++); D=mem.read(PC++); cycles=10; break; // LD DE,nn
            case 0x01: C=mem.read(PC++); B=mem.read(PC++); cycles=10; break; // LD BC,nn
            case 0x31: SP=mem.read(PC++)|(mem.read(PC++)<<8); cycles=10; break; // LD SP,nn

            // --- 16-BIT ALU ---
            case 0x03: C++; if(C>255){C=0; B=(B+1)&0xFF;} cycles=6; break; // INC BC
            case 0x13: E++; if(E>255){E=0; D=(D+1)&0xFF;} cycles=6; break; // INC DE
            case 0x23: L++; if(L>255){L=0; H=(H+1)&0xFF;} cycles=6; break; // INC HL
            case 0x33: SP=(SP+1)&0xFFFF; cycles=6; break;
            case 0x0B: C--; if(C<0){C=255; B=(B-1)&0xFF;} cycles=6; break; // DEC BC
            case 0x1B: E--; if(E<0){E=255; D=(D-1)&0xFF;} cycles=6; break; // DEC DE
            case 0x2B: L--; if(L<0){L=255; H=(H-1)&0xFF;} cycles=6; break; // DEC HL
            case 0x3B: SP=(SP-1)&0xFFFF; cycles=6; break;

            // --- 8-BIT ALU ---
            case 0x3C: A=inc(A); break; case 0x3D: A=dec(A); break;
            case 0x04: B=inc(B); break; case 0x05: B=dec(B); break;
            case 0x0C: C=inc(C); break; case 0x0D: C=dec(C); break;
            case 0x14: D=inc(D); break; case 0x15: D=dec(D); break;
            case 0x1C: E=inc(E); break; case 0x1D: E=dec(E); break;
            case 0x24: H=inc(H); break; case 0x25: H=dec(H); break;
            case 0x2C: L=inc(L); break; case 0x2D: L=dec(L); break;

            case 0xAF: A^=A; F=SZ53[0]|ZF|PF|NF; break; // XOR A
            case 0xB7: or(A); break; // OR A
            case 0xB0: or(B); break; case 0xB1: or(C); break;

            case 0xFE: cp(mem.read(PC++)); cycles=7; break; // CP n

            // --- JUMPS ---
            case 0xC3: PC=mem.read(PC)|(mem.read(PC+1)<<8); cycles=10; break; // JP nn
            case 0x18: jr(); cycles=12; break; // JR e
            case 0x20: if((F&ZF)==0) {jr(); cycles=12;} else {PC++; cycles=7;} break; // JR NZ,e
            case 0x28: if((F&ZF)!=0) {jr(); cycles=12;} else {PC++; cycles=7;} break; // JR Z,e
            case 0x30: if((F&CF)==0) {jr(); cycles=12;} else {PC++; cycles=7;} break; // JR NC,e
            case 0x38: if((F&CF)!=0) {jr(); cycles=12;} else {PC++; cycles=7;} break; // JR C,e
            case 0x10: B=(B-1)&0xFF; if(B!=0){jr(); cycles=13;} else {PC++; cycles=8;} break; // DJNZ

            // --- CALL/RET ---
            case 0xCD: push(PC+2); PC=mem.read(PC)|(mem.read(PC+1)<<8); cycles=17; break; // CALL nn
            case 0xC9: PC=pop(); cycles=10; break; // RET
            case 0xC0: if((F&ZF)==0) {PC=pop(); cycles=11;} else cycles=5; break; // RET NZ
            case 0xC8: if((F&ZF)!=0) {PC=pop(); cycles=11;} else cycles=5; break; // RET Z

            // --- STACK ---
            case 0xC5: push(B<<8|C); cycles=11; break; // PUSH BC
            case 0xD5: push(D<<8|E); cycles=11; break; // PUSH DE
            case 0xE5: push(H<<8|L); cycles=11; break; // PUSH HL
            case 0xF5: push(A<<8|F); cycles=11; break; // PUSH AF
            case 0xC1: int v=pop(); C=v&0xFF; B=v>>8; cycles=10; break; // POP BC
            case 0xD1: v=pop(); E=v&0xFF; D=v>>8; cycles=10; break; // POP DE
            case 0xE1: v=pop(); L=v&0xFF; H=v>>8; cycles=10; break; // POP HL
            case 0xF1: v=pop(); F=v&0xFF; A=v>>8; cycles=10; break; // POP AF

            // --- I/O & CTRL ---
            case 0xD3: if(machine!=null) machine.output(mem.read(PC++)|(A<<8), A); PC++; cycles=11; break; // OUT
            case 0xDB: if(machine!=null) A=machine.input(mem.read(PC++)|(A<<8)); PC++; cycles=11; break; // IN
            case 0xF3: IFF1=IFF2=false; cycles=4; break; // DI
            case 0xFB: IFF1=IFF2=true; cycles=4; break; // EI

            // --- EX ---
            case 0xEB: int t=D; D=H; H=t; t=E; E=L; L=t; cycles=4; break; // EX DE,HL
            case 0x08: t=A; A=A_; A_=t; t=F; F=F_; F_=t; cycles=4; break; // EX AF,AF'
            case 0xD9: t=B;B=B_;B_=t; t=C;C=C_;C_=t; t=D;D=D_;D_=t; t=E;E=E_;E_=t; t=H;H=H_;H_=t; t=L;L=L_;L_=t; cycles=4; break; // EXX

            // --- PREFIXES ---
            case 0xCB: decodeCB(); cycles=8; break;
            case 0xED: decodeED(); cycles=8; break;
            case 0xDD: PC++; step(); break; // IX (Simplificado)
            case 0xFD: PC++; step(); break; // IY (Simplificado)

            default:
                // Ignorar opcode desconocido
                break;
        }

        tStates += cycles;
        return cycles;
    }

    // --- Helpers ---
    private void jr() { int o=(byte)mem.read(PC++); PC=(PC+o)&0xFFFF; }
    private void push(int v) { SP=(SP-1)&0xFFFF; mem.write(SP,v>>8); SP=(SP-1)&0xFFFF; mem.write(SP,v&0xFF); }
    private int pop() { int l=mem.read(SP); SP=(SP+1)&0xFFFF; int h=mem.read(SP); SP=(SP+1)&0xFFFF; return h<<8|l; }

    private int inc(int v) { v=(v+1)&0xFF; updateFlagsInc(v); return v; }
    private int dec(int v) { updateFlagsDec(v); return (v-1)&0xFF; }
    private void or(int v) {
        A |= v;
        F = SZ53[A] | (P[A] ? PF : 0);
        F &= ~HF;
        F &= ~NF;
        F &= ~CF;
    }
    private void cp(int v) { int r=A-v; F=(r&SF)|(r&ZF)|(v&XF)|(v&YF)|NF; if((r&0xFF00)!=0)F|=CF; if(((A^v^r)&HF)!=0)F|=HF; if(((v^A)&(v^r)&0x80)!=0)F|=PF; }

    private void updateFlagsInc(int v) { F=(F&CF)|SZ53[v]|(v==0x80?PF:0)|((v&0x0F)==0?HF:0); }
    private void updateFlagsDec(int v) { F=(F&CF)|NF|SZ53[(v-1)&0xFF]|(v==0x80?PF:0)|((v&0x0F)==0?HF:0); }

    // Prefijo CB (Bits)
    private void decodeCB() {
        int op = mem.read(PC++);
        int r = op & 0x07;
        int bit = (op >> 3) & 0x07;
        int val = (r==0?B:r==1?C:r==2?D:r==3?E:r==4?H:r==5?L:r==7?A:mem.read(H<<8|L));

        if ((op & 0xC0) == 0x40) { // BIT
            boolean z = (val & (1 << bit)) == 0;
            F = (F & CF) | HF | (z ? ZF : 0);
        }
    }

    // Prefijo ED
    private void decodeED() {
        int op = mem.read(PC++);
        if (op == 0xB0) { // LDIR
            int v = mem.read(H<<8|L);
            mem.write(D<<8|E, v);
            E++; if(E>255){E=0;D=(D+1)&0xFF;} // DE++
            L++; if(L>255){L=0;H=(H+1)&0xFF;} // HL++
            C--; if(C<0){C=255;B=(B-1)&0xFF;} // BC--
            if (B!=0 || C!=0) { PC=(PC-2)&0xFFFF; tStates+=21; } else tStates+=16;
            F&=~(HF|NF|PF);
        }
    }

    public void interrupt() {
        if (IFF1) {
            IFF1=IFF2=false; Halted=false;
            push(PC); PC=0x0038; tStates+=13;
        }
    }
}