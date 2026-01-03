package com.lazyzxsoftware.zxspectrumide.emulator.impl;

import com.lazyzxsoftware.zxspectrumide.emulator.core.Memory;
import com.lazyzxsoftware.zxspectrumide.emulator.core.Z80;
import com.lazyzxsoftware.zxspectrumide.emulator.interfaces.SpectrumEmulator;
import javafx.application.Platform;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Spectrum48k implements SpectrumEmulator {

    private final Memory memory;
    private final Z80 cpu;

    private volatile boolean running = false;
    private volatile boolean paused = true;
    private Thread emulatorThread;

    // --- VIDEO (Double Buffer) ---
    private final int[] drawingBuffer = new int[320 * 240];
    private final int[] displayBuffer = new int[320 * 240];
    private final Object videoLock = new Object();

    // Paleta estándar Sinclair
    private static final int[] PALETTE = {
            0xFF000000, 0xFF0000CD, 0xFFCD0000, 0xFFCD00CD,
            0xFF00CD00, 0xFF00CDCD, 0xFFCDCD00, 0xFFCDCDCD,
            0xFF000000, 0xFF0000FF, 0xFFFF0000, 0xFFFF00FF,
            0xFF00FF00, 0xFF00FFFF, 0xFFFFFF00, 0xFFFFFFFF
    };

    private int currentBorderColor = 7;
    private int flashCounter = 0;
    private boolean flashState = false;

    // --- INTERRUPCIONES Y TIMING ---
    private static final int TSTATES_PER_FRAME = 69888; // JSpeccy standard for 48k
    private long frameCount = 0;

    // --- LOADER ---
    private List<byte[]> tapeBlocks = new ArrayList<>();
    private int tapeBlockIndex = 0;
    private boolean tapePlaying = false;
    private int autoLoadStep = 0;
    private int autoLoadDelay = 0;

    private final int[] keyboardMatrix = new int[8];
    private final Set<Integer> breakpoints = new HashSet<>();
    private Runnable onStopCallback;

    private int debugFrameCounter = 0;
    private boolean hasDumped = false;

    public Spectrum48k() {
        this.memory = new Memory();
        this.cpu = new Z80(this.memory, this);
        Arrays.fill(keyboardMatrix, 0xFF);
        loadRom("/com/lazyzxsoftware/zxspectrumide/roms/48k.rom");
    }

    @Override
    public void start() {
        if (running && !paused) return;
        paused = false;
        if (running) return;

        running = true;
        emulatorThread = new Thread(this::runLoop, "SpectrumThread");
        emulatorThread.setPriority(Thread.MAX_PRIORITY);
        emulatorThread.start();
    }

    private void runLoop() {
        long nextFrameTime = System.nanoTime();
        final long nsPerFrame = 20_000_000; // 50 Hz

        while (running) {
            if (paused) {
                try { Thread.sleep(20); } catch (InterruptedException e) {}
                nextFrameTime = System.nanoTime();
                continue;
            }

            executeFrame();
            frameCount++;

            nextFrameTime += nsPerFrame;
            long sleepTime = (nextFrameTime - System.nanoTime()) / 1_000_000;

            if (sleepTime > 0) {
                try { Thread.sleep(sleepTime); } catch (InterruptedException e) {}
            } else if (sleepTime < -100) {
                // Si vamos muy lentos, reseteamos el tiempo para evitar saltos
                nextFrameTime = System.nanoTime();
            }
        }
    }

    private void executeFrame() {
        cpu.resetTStates();

        while (cpu.getTStates() < 69888) {
            if (tapePlaying && cpu.getPC() == 0x0556) {
                performFastLoad();
            }
            cpu.step();
        }

        cpu.interrupt();

        // LÓGICA DE DUMP AUTOMÁTICO
        debugFrameCounter++;
        // 250 frames son aprox 5 segundos de ejecución real
        if (debugFrameCounter > 250 && !hasDumped) {
            cpu.forceLogDump("TIMEOUT - PANTALLA NEGRA");
            hasDumped = true;
        }

        // Resto de la lógica de renderizado...
        renderFullFrame();
    }

    private void renderFullFrame() {
        synchronized (videoLock) {
            // 1. Pintar el borde (Simplificado: todo el fondo del color del borde)
            int borderColor = PALETTE[currentBorderColor];
            Arrays.fill(drawingBuffer, borderColor);

            // 2. Pintar la zona de píxeles (256x192 centrada en 320x240)
            // Offset X = 32, Offset Y = 24
            for (int y = 0; y < 192; y++) {
                int screenY = y;
                // Fórmula de direcciones de pantalla del Spectrum
                int vramAddr = 0x4000 | ((screenY & 0xC0) << 5) | ((screenY & 0x07) << 8) | ((screenY & 0x38) << 2);
                int attrAddr = 0x5800 | ((screenY >> 3) << 5);

                for (int x = 0; x < 32; x++) {
                    int pixels = memory.read(vramAddr + x);
                    int attr = memory.read(attrAddr + x);

                    int inkIdx = attr & 0x07;
                    int paperIdx = (attr >> 3) & 0x07;
                    boolean bright = (attr & 0x40) != 0;
                    boolean flash = (attr & 0x80) != 0;

                    if (bright) { inkIdx += 8; paperIdx += 8; }
                    if (flash && flashState) {
                        int temp = inkIdx; inkIdx = paperIdx; paperIdx = temp;
                    }

                    int ink = PALETTE[inkIdx];
                    int paper = PALETTE[paperIdx];

                    int bufferOffset = (y + 24) * 320 + (x * 8 + 32);
                    for (int bit = 0; bit < 8; bit++) {
                        drawingBuffer[bufferOffset + bit] = ((pixels & (0x80 >> bit)) != 0) ? ink : paper;
                    }
                }
            }
            System.arraycopy(drawingBuffer, 0, displayBuffer, 0, drawingBuffer.length);
        }
    }

    // En el método de inicialización o constructor
    public void powerOn() {
        memory.reset();

        // Intentamos cargar ZEXALL. Si no existe, cargamos la ROM normal.
        InputStream testFile = getClass().getResourceAsStream("/com/lazyzxsoftware/zxspectrumide/roms/ZEXALL.bin");

        if (testFile != null) {
            memory.loadBinary("/com/lazyzxsoftware/zxspectrumide/roms/ZEXALL.bin", 0x0000);
            cpu.PC = 0x0000; // ZEXALL suele empezar en 0000 o 0100 según versión
        } else {
            memory.loadRom("/com/lazyzxsoftware/zxspectrumide/roms/48k.rom");
            cpu.PC = 0x0000;
        }
    }

    // --- I/O PORTS ---
    public void output(int port, int value) {
        if ((port & 0x01) == 0) { // Puerto 0xFE
            currentBorderColor = value & 0x07;
            // Aquí iría el sonido (beeper)
        }
    }

    public int input(int port) {
        if ((port & 0x01) == 0) {
            int result = 0xFF;
            for (int i = 0; i < 8; i++) {
                if ((port & (1 << (i + 8))) == 0) result &= keyboardMatrix[i];
            }
            return result;
        }
        return 0xFF;
    }

    // --- TAPE MANAGEMENT ---
    private void performFastLoad() {
        if (tapeBlockIndex >= tapeBlocks.size()) {
            tapePlaying = false;
            return;
        }
        byte[] block = tapeBlocks.get(tapeBlockIndex++);
        int start = cpu.IX;
        int length = (cpu.D << 8) | cpu.E;

        // Copiar bloque omitiendo el flag byte y el checksum
        for (int i = 0; i < length && (i + 1) < block.length; i++) {
            memory.write(start + i, block[i + 1] & 0xFF);
        }

        cpu.F |= 0x01; // Carry flag = Success
        cpu.PC = pop(); // Simular RET
    }

    private int pop() {
        int l = memory.read(cpu.SP++);
        int h = memory.read(cpu.SP++);
        cpu.SP &= 0xFFFF;
        return (h << 8) | l;
    }

    private void processSmartLoader() {
        if (autoLoadStep == 0) return;

        // Esperar a que el sistema esté estable (ERR_NR = 0xFF en 0x5C3A)
        if (autoLoadStep == 1) {
            if (memory.read(0x5C3A) != 0xFF) return;
            autoLoadStep++;
            autoLoadDelay = 50; // Esperar un segundo tras el arranque
            return;
        }

        if (autoLoadDelay > 0) { autoLoadDelay--; return; }

        Arrays.fill(keyboardMatrix, 0xFF);
        switch (autoLoadStep) {
            case 2: setKey(6, 3); nextStep(10); break; // J
            case 4: setKey(7, 1); setKey(5, 0); nextStep(10); break; // "
            case 6: setKey(7, 1); setKey(5, 0); nextStep(10); break; // "
            case 8: setKey(6, 0); nextStep(10); break; // Enter
            case 10: autoLoadStep = 0; break;
            default: autoLoadStep++; break;
        }
    }

    private void setKey(int row, int bit) { keyboardMatrix[row] &= ~(1 << bit); }
    private void nextStep(int delay) { autoLoadDelay = delay; autoLoadStep++; }

    // --- OVERRIDES INTERFACE ---
    @Override public void reset() {
        cpu.reset();
        memory.reset();
        currentBorderColor = 7;
        autoLoadStep = 0;
        synchronized (videoLock) {
            Arrays.fill(drawingBuffer, 0xFFFFFFFF);
            Arrays.fill(displayBuffer, 0xFFFFFFFF);
        }
    }

    @Override public void pause() { paused = true; }
    @Override public void stop() { running = false; cpu.closeLogger(); System.out.println("✅ Emulador detenido y log cerrado.");}
    @Override public boolean isPaused() { return paused; }
    @Override public long getTStates() { return cpu.getTStates(); }
    @Override public Memory getMemory() { return memory; }
    @Override public int getBorderColor() { return currentBorderColor; }
    @Override public int[] getScreenBuffer() { synchronized (videoLock) { return displayBuffer; } }
    @Override public void step() { cpu.step(); }
    @Override public void stepOver() { step(); }
    @Override public void toggleBreakpoint(int address) { if(breakpoints.contains(address)) breakpoints.remove(address); else breakpoints.add(address); }
    @Override public void loadRom(String path) { memory.loadRom(path); }
    @Override public void loadSnapshot(String path) {}
    @Override public void setOnCpuStop(Runnable callback) { this.onStopCallback = callback; }
    @Override public int peek(int address) { return memory.read(address); }
    @Override public void poke(int address, int value) { memory.write(address, value); }
    @Override public int getRegister(String name) { return 0; }
    @Override public void loadProgram(File file) { /* Implementado igual que antes */ }
}