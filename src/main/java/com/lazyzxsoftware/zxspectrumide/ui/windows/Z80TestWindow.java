package com.lazyzxsoftware.zxspectrumide.ui.windows;

import com.lazyzxsoftware.zxspectrumide.emulator.core.MemoryCore;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import com.lazyzxsoftware.zxspectrumide.emulator.core.Z80Core;
import com.lazyzxsoftware.zxspectrumide.emulator.core.Memory;

public class Z80TestWindow extends Stage {

    private final TextArea logArea;
    private Z80Core testCpu;
    private MemoryCore testMem;

    public Z80TestWindow() {
        setTitle("Z80 Unit Tests Runner");
        setWidth(600);
        setHeight(400);

        BorderPane root = new BorderPane();
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px;");

        Button btnRun = new Button("▶ Ejecutar Tests Z80");
        btnRun.setOnAction(e -> runTests());

        ToolBar toolbar = new ToolBar(btnRun);
        root.setTop(toolbar);
        root.setCenter(logArea);

        setScene(new Scene(root));
    }

    private void runTests() {
        logArea.clear();
        log("🔵 INICIANDO BATERÍA DE TESTS (Validación Unitaria)");
        log("---------------------------------------------------");

        testMem = new MemoryCore();
        testCpu = new Z80Core(testMem, null); // Usamos el núcleo limpio

        // Ejecutamos los tests uno por uno
        boolean load8 = testLoad8BitImmediate();
        boolean load16 = testLoad16BitImmediate(); // <--- AQUÍ ES DONDE PROBABLEMENTE FALLE
        boolean loadMem = testLoadMemoryIndirect();
        boolean aluFlags = testALUFlags();
        boolean flow = testControlFlow();
        boolean blocks = testBlockTransfer();
        boolean registers = testRegisterIntegrity();
        boolean stack = testStackAndCall();
        boolean flags = testFlagsDetailed();

        log("---------------------------------------------------");
        if (load8 && load16 && loadMem && aluFlags && flow && blocks && registers && stack && flags) {
            log("✅ RESULTADO FINAL: ¡TODAS LAS CARGAS BÁSICAS FUNCIONAN!");
            log("🚀 Tu CPU ya sabe realizar cargas de memoria");
            log("🚀 Tu CPU ya sabe sumar y comparar.");
            log("🚀 Tu CPU ya sabe saltar y realizar condicionales.");
            log("🚀 Tu CPU ya sabe copiar con repetición");
            log("🚀 Tu CPU pasa el test de integridad de registros!");
            log("🚀 Tu CPU pasa sabe usar PUSH/POP y el CALL/RET ");
        } else {
            log("❌ RESULTADO FINAL: Hay errores críticos. Revisa los logs de arriba.");
        }
    }

    // --- TEST 1: Carga Inmediata de 8 bits (LD r, n) ---
    // Objetivo: Verificar que no hay "extensión de signo" (ej: 0xFF no se convierte en -1)
    private boolean testLoad8BitImmediate() {
        log("🧪 Test 1: LD r, n (8-bit immediate)...");
        testCpu.reset();

        // Programa:
        // LD A, 0x10
        // LD B, 0xFF (Prueba crítica de signo)
        // LD C, 0x00
        byte[] code = {
                (byte)0x3E, (byte)0x10, // LD A, 10
                (byte)0x06, (byte)0xFF, // LD B, FF
                (byte)0x0E, (byte)0x00, // LD C, 00
                (byte)0x76              // HALT
        };
        injectCode(code);

        runCpu(10); // Ejecutar

        boolean ok = true;
        ok &= assertReg("A", testCpu.A, 0x10);
        ok &= assertReg("B", testCpu.B, 0xFF); // Si aquí sale FFFFFFFF o -1, falta un & 0xFF
        ok &= assertReg("C", testCpu.C, 0x00);

        return printResult(ok);
    }

    // --- TEST 2: Carga Inmediata de 16 bits (LD rr, nn) ---
    // Objetivo: Verificar Little Endian (El byte bajo va primero)
    private boolean testLoad16BitImmediate() {
        log("🧪 Test 2: LD rr, nn (16-bit immediate - Endianness)...");
        testCpu.reset();

        // Programa:
        // LD HL, 0x1234  -> Opcode: 21 34 12 (34 es bajo, 12 es alto)
        // LD BC, 0xF00D  -> Opcode: 01 0D F0
        // LD DE, 0x00FF  -> Opcode: 11 FF 00
        byte[] code = {
                (byte)0x21, (byte)0x34, (byte)0x12,
                (byte)0x01, (byte)0x0D, (byte)0xF0,
                (byte)0x11, (byte)0xFF, (byte)0x00,
                (byte)0x76
        };
        injectCode(code);

        runCpu(15);

        boolean ok = true;
        // Verificación individual de H y L
        ok &= assertReg("H", testCpu.H, 0x12); // Si sale 34, están invertidos
        ok &= assertReg("L", testCpu.L, 0x34); // Si sale 12, están invertidos

        // Verificación del valor combinado (debe ser 1234, no 3412)
        ok &= assertReg("HL (16b)", testCpu.getHL(), 0x1234);
        ok &= assertReg("BC (16b)", testCpu.getBC(), 0xF00D);
        ok &= assertReg("DE (16b)", testCpu.getDE(), 0x00FF);

        return printResult(ok);
    }

    // --- TEST 3: Carga Indirecta de Memoria (LD r, (HL)) ---
    // Objetivo: Verificar que leemos de la dirección correcta y aplicamos máscaras
    private boolean testLoadMemoryIndirect() {
        log("🧪 Test 3: LD r, (HL) & LD (HL), n...");
        testCpu.reset();

        // 1. Preparamos la memoria manualmente
        // En la dirección 0x2000 escribimos el valor 0x55
        testMem.write(0x2000, 0x55);

        // Programa:
        // LD HL, 0x2000  (Apuntamos a la dirección)
        // LD A, (HL)     (Leemos lo que hay allí -> debería ser 55)
        // LD (HL), 0xAA  (Escribimos AA sobre el 55)
        // LD B, (HL)     (Leemos de nuevo -> debería ser AA)
        byte[] code = {
                (byte)0x21, (byte)0x00, (byte)0x20, // LD HL, 2000 (Endianness: 00 20)
                (byte)0x7E,                         // LD A, (HL)
                (byte)0x36, (byte)0xAA,             // LD (HL), AA
                (byte)0x46,                         // LD B, (HL)
                (byte)0x76                          // HALT
        };
        injectCode(code);

        runCpu(20);

        boolean ok = true;
        ok &= assertReg("HL", testCpu.getHL(), 0x2000);
        ok &= assertReg("A (Lectura)", testCpu.A, 0x55); // Si falla, HL apuntó mal
        ok &= assertReg("B (Escritura)", testCpu.B, 0xAA); // Si falla, LD (HL), n falló

        return printResult(ok);
    }

    // --- TEST 4: ALU Básica y Flags (INC, CP) ---
    // Objetivo: Verificar que CP activa el Flag Z solo cuando debe.
    private boolean testALUFlags() {
        log("🧪 Test 4: ALU Básica (INC, CP) y Flags...");
        testCpu.reset();

        // Programa de prueba:
        // 1. LD A, 0x05
        // 2. LD B, 0x05
        // 3. CP B        -> 5 - 5 = 0. El Flag Z (Zero) DEBE activarse (0x40).
        // 4. INC A       -> A se convierte en 6.
        // 5. CP B        -> 6 - 5 = 1. El Flag Z DEBE desactivarse.
        // 6. HALT
        byte[] code = {
                (byte)0x3E, (byte)0x05, // LD A, 5
                (byte)0x06, (byte)0x05, // LD B, 5
                (byte)0xB8,             // CP B (Verificaremos flags tras este paso)
                (byte)0x3C,             // INC A
                (byte)0xB8,             // CP B (Verificaremos flags tras este paso)
                (byte)0x76              // HALT
        };
        injectCode(code);

        // --- EJECUCIÓN PASO A PASO PARA VERIFICAR FLAGS INTERMEDIOS ---

        // Paso 1: Ejecutar hasta el primer CP (3 instrucciones: LD, LD, CP)
        runCpu(3);

        boolean ok = true;
        // Verificación CRÍTICA: 5 == 5, así que ZF (bit 6) debe ser 1
        if ((testCpu.F & Z80Core.ZF) != 0) {
            log("   ✅ CP 5==5: Flag Zero activado correctamente.");
        } else {
            log("   ❌ CP 5==5: FALLO. Flag Zero no se activó.");
            ok = false;
        }

        // Paso 2: Ejecutar el resto (INC A, CP B)
        runCpu(2); // 2 instrucciones más

        // Verificación: 6 != 5, así que ZF debe ser 0
        if ((testCpu.F & Z80Core.ZF) == 0) {
            log("   ✅ CP 6==5: Flag Zero desactivado correctamente.");
        } else {
            log("   ❌ CP 6==5: FALLO. Flag Zero se quedó activado (falso positivo).");
            ok = false;
        }

        ok &= assertReg("A", testCpu.A, 0x06);

        return printResult(ok);
    }

    // --- TEST 5: Control de Flujo (Saltos Relativos) ---
    // Objetivo: Verificar que la CPU puede hacer bucles y respetar condiciones.
    private boolean testControlFlow() {
        log("🧪 Test 5: Control de Flujo (JR, JR NZ, DJNZ)...");
        testCpu.reset();

        // Programa: Bucle simple que incrementa A tres veces.
        // Dirección | Opcode     | Mnemónico
        // ------------------------------------
        // 0000      | 3E 00      | LD A, 0    (Inicializamos A = 0)
        // 0002      | 06 03      | LD B, 3    (Contador del bucle = 3)
        // 0004      | 3C         | INC A      (<-- ETIQUETA 'BUCLE')
        // 0005      | 10 FD      | DJNZ -3    (Decrementa B. Si B!=0, salta atrás 3 bytes a 0004)
        // 0007      | 76         | HALT       (Fin)

        byte[] code = {
                (byte)0x3E, (byte)0x00,       // LD A, 0
                (byte)0x06, (byte)0x03,       // LD B, 3
                (byte)0x3C,                   // INC A (Dir 0004)
                (byte)0x10, (byte)0xFD,       // DJNZ -3 (0xFD es -3 en complemento a 2)
                (byte)0x76                    // HALT
        };
        injectCode(code);

        // Ejecutamos con límite de ciclos suficiente para las 3 vueltas
        // Vuelta 1: LD, LD, INC, DJNZ (salta)
        // Vuelta 2: INC, DJNZ (salta)
        // Vuelta 3: INC, DJNZ (no salta)
        // Fin: HALT
        runCpu(50);

        boolean ok = true;

        // Si el bucle funcionó, A debe valer 3
        if (testCpu.A == 3) {
            log("   ✅ Bucle DJNZ: Ejecutado 3 veces correctamente.");
        } else {
            log("   ❌ Bucle DJNZ: FALLO. A vale " + testCpu.A + " (Esperado: 3). El salto no funcionó.");
            ok = false;
        }

        ok &= assertReg("B", testCpu.B, 0x00); // B debe haber bajado a 0

        return printResult(ok);
    }

    // --- TEST 6: Transferencia de Bloques (LDIR) ---
    // Objetivo: Verificar que LDIR copia bytes, actualiza punteros y repite hasta BC=0.
    private boolean testBlockTransfer() {
        log("🧪 Test 6: Bloques (LDIR - ED B0)...");
        testCpu.reset();

        // 1. Preparar datos en memoria (Origen: 0x1000)
        // Escribimos 3 bytes: 0xAA, 0xBB, 0xCC
        testMem.write(0x1000, 0xAA);
        testMem.write(0x1001, 0xBB);
        testMem.write(0x1002, 0xCC);

        // 2. Programa: Copiar 3 bytes de 0x1000 a 0x2000
        // LD HL, 0x1000   (Origen)
        // LD DE, 0x2000   (Destino)
        // LD BC, 0x0003   (Longitud)
        // LDIR            (ED B0)
        // HALT
        byte[] code = {
                (byte)0x21, (byte)0x00, (byte)0x10, // LD HL, 1000
                (byte)0x11, (byte)0x00, (byte)0x20, // LD DE, 2000
                (byte)0x01, (byte)0x03, (byte)0x00, // LD BC, 3
                (byte)0xED, (byte)0xB0,             // LDIR
                (byte)0x76                          // HALT
        };
        injectCode(code);

        // Ejecutamos. LDIR tarda 21 ciclos por byte * 3 = 63 + setup. Ponemos 100 ciclos.
        runCpu(100);

        boolean ok = true;

        // Verificación 1: ¿Se copiaron los datos?
        if (testMem.read(0x2000) == 0xAA && testMem.read(0x2001) == 0xBB && testMem.read(0x2002) == 0xCC) {
            log("   ✅ Datos copiados correctamente (AA, BB, CC).");
        } else {
            log("   ❌ FALLO COPIA: Destino no tiene los datos esperados.");
            ok = false;
        }

        // Verificación 2: Punteros actualizados
        // Al terminar, HL debe ser 1003, DE debe ser 2003, BC debe ser 0
        ok &= assertReg("HL Final", testCpu.getHL(), 0x1003);
        ok &= assertReg("DE Final", testCpu.getDE(), 0x2003);
        ok &= assertReg("BC Final", testCpu.getBC(), 0x0000); // CRÍTICO: Si no es 0, el bucle falló

        return printResult(ok);
    }

    // --- TEST 7: Integridad Estructural de Registros ---
    // Objetivo: Verificar que BC, DE, HL son independientes y que
    // sus partes alta/baja (B/C, D/E, H/L) se mapean correctamente.
    private boolean testRegisterIntegrity() {
        log("🧪 Test 7: Integridad de Registros (Aislamiento)...");
        testCpu.reset();

        // 1. Llenamos TODOS los registros con patrones únicos
        // Así sabremos si al escribir en uno, machacamos otro por error.
        testCpu.A = 0x11;
        testCpu.B = 0x22; testCpu.C = 0x33; // BC debería ser 2233
        testCpu.D = 0x44; testCpu.E = 0x55; // DE debería ser 4455
        testCpu.H = 0x66; testCpu.L = 0x77; // HL debería ser 6677

        boolean ok = true;

        // VERIFICACIÓN 1: Lectura compuesta (getBC, getDE, getHL)
        ok &= assertReg("BC Inicial (2233)", testCpu.getBC(), 0x2233);
        ok &= assertReg("DE Inicial (4455)", testCpu.getDE(), 0x4455);
        ok &= assertReg("HL Inicial (6677)", testCpu.getHL(), 0x6677);

        if (!ok) log("   ❌ FALLO CRÍTICO: Los getters de 16 bits no combinan bien los de 8 bits.");

        // VERIFICACIÓN 2: Escritura compuesta (setBC) y efecto en 8 bits
        // Si tu setBC estaba mal, esto debería fallar.
        log("   > Escribiendo BC = 0x8899...");

        // Asumimos que has creado este método setBC en Z80Core como te dije antes
        // Si no lo tienes público, tendrás que exponerlo o usar una instrucción LD BC, nn
        testCpu.setBC(0x8899);

        ok &= assertReg("Nuevo B (debe ser 88)", testCpu.B, 0x88);
        ok &= assertReg("Nuevo C (debe ser 99)", testCpu.C, 0x99);

        // VERIFICACIÓN 3: Aislamiento (¿Se rompió algo más?)
        // Si al escribir BC, se cambió DE o HL, aquí saltará la alarma.
        ok &= assertReg("DE Intacto (4455)", testCpu.getDE(), 0x4455);
        ok &= assertReg("HL Intacto (6677)", testCpu.getHL(), 0x6677);
        ok &= assertReg("A Intacto (11)", testCpu.A, 0x11);

        // VERIFICACIÓN 4: Prueba de 'set' con máscaras (Signo)
        // Escribimos un valor que en Java podría dar problemas de signo (negativo en short)
        log("   > Prueba de fuego: Escribiendo DE = 0xFFEE...");
        testCpu.setDE(0xFFEE);

        ok &= assertReg("D (FF)", testCpu.D, 0xFF); // Si falta & 0xFF, esto podría fallar
        ok &= assertReg("E (EE)", testCpu.E, 0xEE);
        ok &= assertReg("DE Completo", testCpu.getDE(), 0xFFEE);

        return printResult(ok);
    }

    // --- TEST 7.5: La Pila (Stack) y Subrutinas ---
    // Objetivo: Verificar PUSH/POP y que CALL/RET saltan y vuelven correctamente.
    private boolean testStackAndCall() {
        log("🧪 Test 7: Pila (PUSH/POP) y Subrutinas (CALL/RET)...");
        testCpu.reset();

        // Inicializamos el Stack Pointer (SP) en una zona segura (ej. 0xFFFF)
        testCpu.SP = 0xFFFF;

        // Programa Principal:
        // 0000: LD BC, 0x1234
        // 0003: PUSH BC      (Guarda 1234 en la pila. SP baja 2 bytes)
        // 0004: POP DE       (Recupera en DE. DE debe ser 1234. SP sube 2 bytes)
        // 0005: CALL 0x0010  (Salta a la subrutina en 0010. Guarda PC=0008 en pila)
        // 0008: HALT         (Aquí debemos volver tras el RET)

        // Subrutina (en 0x0010):
        // 0010: LD A, 0x99   (Marca para saber que pasamos por aquí)
        // 0012: RET          (Vuelve a 0008)

        testMem.write(0x0000, 0x01); testMem.write(0x0001, 0x34); testMem.write(0x0002, 0x12); // LD BC, 1234
        testMem.write(0x0003, 0xC5); // PUSH BC
        testMem.write(0x0004, 0xD1); // POP DE
        testMem.write(0x0005, 0xCD); testMem.write(0x0006, 0x10); testMem.write(0x0007, 0x00); // CALL 0010
        testMem.write(0x0008, 0x76); // HALT

        // Código de la subrutina en 0010
        testMem.write(0x0010, 0x3E); testMem.write(0x0011, 0x99); // LD A, 99
        testMem.write(0x0012, 0xC9); // RET

        runCpu(100);

        boolean ok = true;

        // Verificación 1: Integridad de datos en Pila
        if (testCpu.getDE() == 0x1234) {
            log("   ✅ PUSH BC / POP DE: Datos preservados (1234).");
        } else {
            log("   ❌ FALLO PILA: Esperado 1234 en DE, obtenido " + String.format("%04X", testCpu.getDE()));
            ok = false;
        }

        // Verificación 2: Ejecución de subrutina
        if (testCpu.A == 0x99) {
            log("   ✅ Subrutina alcanzada (A=99).");
        } else {
            log("   ❌ FALLO CALL: No se ejecutó la subrutina.");
            ok = false;
        }

        // Verificación 3: Retorno correcto
        if (testCpu.PC == 0x0009) { // 0008 es el HALT, PC queda en 0009 tras ejecutarlo
            log("   ✅ RET correcto: Volvió al punto de llamada.");
        } else {
            log("   ❌ FALLO RET: PC acabó en " + String.format("%04X", testCpu.PC));
            ok = false;
        }

        // Verificación 4: Consistencia de SP
        if (testCpu.SP == 0xFFFF) {
            log("   ✅ SP equilibrado: Volvió a su valor original.");
        } else {
            log("   ❌ SP desequilibrado: " + String.format("%04X", testCpu.SP));
            ok = false;
        }

        return printResult(ok);
    }

    // --- TEST 8: Auditoría Completa de Flags (S, C, H, P/V) ---
    private boolean testFlagsDetailed() {
        log("🧪 Test 8: Auditoría de Flags (Signo, Carry, Overflow)...");
        testCpu.reset();
        boolean ok = true;

        // --- CASO 1: Flag de SIGNO (S) ---
        // Operación: 0 - 1 = -1 (0xFF). El bit 7 es 1, así que S debe activarse.
        // Usamos SUB A, 1 (Pero ojo, necesitamos implementar SUB o usar DEC A)
        // Usaremos DEC A partiendo de 0.
        testCpu.A = 0x00;
        testMem.write(0x0000, 0x3D); // DEC A
        testMem.write(0x0001, 0x76); // HALT
        testCpu.PC = 0;
        runCpu(2);

        if ((testCpu.F & Z80Core.SF) != 0) {
            log("   ✅ Sign Flag (S): Activado correctamente con resultado negativo (FF).");
        } else {
            log("   ❌ Sign Flag (S): FALLO. No se activó al bajar de 0.");
            ok = false;
        }

        // --- CASO 2: Flag de ACARREO (C) ---
        // Operación: 255 + 1 = 0 (con acarreo).
        // Usamos ADD A, 1 (Opcode C6 01)
        testCpu.reset();
        testCpu.A = 0xFF;
        testMem.write(0x0000, 0xC6); testMem.write(0x0001, 0x01); // ADD A, 1
        testMem.write(0x0002, 0x76);
        runCpu(2);

        if ((testCpu.F & Z80Core.CF) != 0) {
            log("   ✅ Carry Flag (C): Activado correctamente por desbordamiento (255+1).");
        } else {
            log("   ❌ Carry Flag (C): FALLO. No detectó el desbordamiento.");
            ok = false;
        }

        // --- CASO 3: Flag de HALF-CARRY (H) ---
        // Operación: 15 (0x0F) + 1 = 16 (0x10).
        // El bit 3 salta al bit 4. Esto activa H.
        testCpu.reset();
        testCpu.A = 0x0F;
        testMem.write(0x0000, 0xC6); testMem.write(0x0001, 0x01); // ADD A, 1
        testMem.write(0x0002, 0x76);
        runCpu(2);

        if ((testCpu.F & Z80Core.HF) != 0) {
            log("   ✅ Half-Carry (H): Detectado paso de bit 3 a 4.");
        } else {
            log("   ❌ Half-Carry (H): FALLO. Es vital para DAA.");
            ok = false;
        }

        // --- CASO 4: Flag de OVERFLOW (P/V) ---
        // Operación: 127 + 1 = 128.
        // En complemento a 2: (+127) + (+1) = (-128).
        // Pasamos de dos positivos a un negativo -> ¡Overflow!
        testCpu.reset();
        testCpu.A = 0x7F;
        testMem.write(0x0000, 0xC6); testMem.write(0x0001, 0x01); // ADD A, 1
        testMem.write(0x0002, 0x76);
        runCpu(2);

        if ((testCpu.F & Z80Core.PF) != 0) {
            log("   ✅ Overflow (P/V): Detectado cambio de signo inválido.");
        } else {
            log("   ❌ Overflow (P/V): FALLO. Matemática con signo rota.");
            ok = false;
        }

        // --- CASO 5: XOR A (Limpieza total) ---
        // XOR A contra sí mismo debe dar 0 y limpiar el Carry.
        testCpu.PC = 0;
        testCpu.Halted = false;

        testCpu.F = 0xFF; // Ensuciamos todos los flags
        testMem.write(0x0000, 0xAF); // XOR A
        testMem.write(0x0001, 0x76);
        runCpu(2);

        boolean xorOk = (testCpu.A == 0) && ((testCpu.F & Z80Core.CF) == 0) && ((testCpu.F & Z80Core.ZF) != 0);
        if (xorOk) log("   ✅ XOR A: Limpia A y Flags correctamente.");
        else {
            log("   ❌ XOR A: Falló. F=" + String.format("%02X", testCpu.F));
            ok = false;
        }

        return printResult(ok);
    }

    // --- MÉTODOS DE SOPORTE ---

    private void injectCode(byte[] code) {
        // ¡Mira qué simple ahora!
        testMem.loadData(0x0000, code);
    }

    private void runCpu(int cyclesLimit) {
        int steps = 0;
        try {
            while (!testCpu.Halted && steps < cyclesLimit) {
                testCpu.execute();
                steps++;
            }
        } catch (Exception e) {
            log("❌ EXCEPCIÓN: " + e.getMessage());
        }
    }

    private boolean assertReg(String name, int actual, int expected) {
        if (actual != expected) {
            log("   ❌ FALLO " + name + ": Esperado " + String.format("%04X", expected) +
                    " | Obtenido " + String.format("%04X", actual));
            return false;
        }
        return true;
    }

    private boolean printResult(boolean ok) {
        if (ok) log("   ✅ PASADO");
        else log("   ⚠️ FALLADO");
        return ok;
    }

    private void log(String msg) {
        logArea.appendText(msg + "\n");
    }
}