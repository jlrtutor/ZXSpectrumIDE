package com.lazyzxsoftware.zxspectrumide.emulator.impl;

import com.lazyzxsoftware.zxspectrumide.emulator.core.Memory;
import com.lazyzxsoftware.zxspectrumide.emulator.core.Z80;
import com.lazyzxsoftware.zxspectrumide.emulator.interfaces.SpectrumEmulator;
import javafx.application.Platform;

import java.io.ByteArrayInputStream;
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

    // Debug
    private final Set<Integer> breakpoints = new HashSet<>();
    private Runnable onStopCallback;

    // --- ULA / VIDEO ---
    private final int[] videoBuffer = new int[320 * 240];
    private static final int[] PALETTE = {
            0xFF000000, 0xFF0000CD, 0xFFCD0000, 0xFFCD00CD,
            0xFF00CD00, 0xFF00CDCD, 0xFFCDCD00, 0xFFCDCDCD,
            0xFF000000, 0xFF0000FF, 0xFFFF0000, 0xFFFF00FF,
            0xFF00FF00, 0xFF00FFFF, 0xFFFFFF00, 0xFFFFFFFF
    };
    private int currentBorderColor = 7;
    private int flashCounter = 0;
    private boolean flashState = false;

    // --- TAPE / FAST LOADER ---
    private List<byte[]> tapeBlocks = new ArrayList<>();
    private int tapeBlockIndex = 0;
    private boolean tapePlaying = false;
    private int autoLoadStep = 0;
    private int autoLoadDelay = 0;

    // Keyboard Matrix
    private final int[] keyboardMatrix = new int[8];

    private static final String ROM_PATH = "/com/lazyzxsoftware/zxspectrumide/roms/48.rom";

    public Spectrum48k() {
        this.memory = new Memory();
        this.cpu = new Z80(this.memory, this);
        Arrays.fill(keyboardMatrix, 0xFF);
        loadRom(ROM_PATH);
    }

    @Override
    public Memory getMemory() { return this.memory; }

    @Override
    public int getBorderColor() { return currentBorderColor; }

    @Override
    public int[] getScreenBuffer() { return videoBuffer; }

    @Override
    public void start() {
        if (running && !paused) return;
        if (running && paused) { paused = false; return; }
        running = true;
        paused = false;

        emulatorThread = new Thread(this::runLoop);
        emulatorThread.setName("Spectrum-Thread");
        emulatorThread.setDaemon(true);
        emulatorThread.start();
    }

    private void runLoop() {
        final int linesTotal = 312;
        final int tStatesPerLine = 224;
        final long nsPerFrame = 20_000_000;

        while (running) {
            long frameStartNano = System.nanoTime();

            if (paused) {
                try { Thread.sleep(10); } catch (InterruptedException e) {}
                continue;
            }

            flashCounter++;
            if (flashCounter >= 16) {
                flashState = !flashState;
                flashCounter = 0;
            }

            processAutoLoad();

            for (int line = 0; line < linesTotal; line++) {
                long lineEndT = cpu.getTStates() + tStatesPerLine;
                while (cpu.getTStates() < lineEndT && !paused) {

                    // 1. Breakpoints Check
                    if (breakpoints.contains(cpu.getPC())) {
                        pause();
                        if (onStopCallback != null) Platform.runLater(onStopCallback);
                        break;
                    }

                    // 2. Tape Trap at 0x0556 (LD-BYTES)
                    if (tapePlaying && cpu.getPC() == 0x0556) {
                        performFastLoad();
                    }

                    cpu.step();
                }
                drawScanline(line);
                if (paused) break;
            }

            if (!paused) cpu.interrupt();

            long executionTime = System.nanoTime() - frameStartNano;
            long waitNs = nsPerFrame - executionTime;
            if (waitNs > 0) {
                try {
                    long ms = waitNs / 1_000_000;
                    int ns = (int) (waitNs % 1_000_000);
                    Thread.sleep(ms, ns);
                } catch (InterruptedException e) {}
            }
        }
    }

    private void performFastLoad() {
        if (tapeBlockIndex >= tapeBlocks.size()) {
            tapePlaying = false;
            return;
        }

        int startAddr = cpu.IX;
        int length = (cpu.D << 8) | cpu.E;
        int expectedFlag = cpu.A;
        boolean isVerify = (cpu.F & 0x01) == 0;

        byte[] block = tapeBlocks.get(tapeBlockIndex);
        int actualFlag = block[0] & 0xFF;

        if (actualFlag == expectedFlag) {
            if (!isVerify) {
                for (int i = 0; i < length; i++) {
                    if (i + 1 < block.length) {
                        memory.write(startAddr + i, block[i + 1] & 0xFF);
                    }
                }
            }
            tapeBlockIndex++;
            cpu.F |= 0x01; // Success (Carry Set)
        } else {
            cpu.F &= ~0x01; // Error (Carry Clear)
        }

        int retAddr = memory.read(cpu.SP) | (memory.read(cpu.SP + 1) << 8);
        cpu.SP = (cpu.SP + 2) & 0xFFFF;
        cpu.PC = retAddr;
    }

    @Override
    public void loadProgram(byte[] data, String name) {
        System.out.println("Montando cinta TAP: " + name);
        tapeBlocks.clear();
        tapeBlockIndex = 0;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
            while (bis.available() > 0) {
                int lenLow = bis.read();
                int lenHigh = bis.read();
                if (lenLow == -1 || lenHigh == -1) break;
                int blockLen = (lenHigh << 8) | lenLow;

                byte[] block = new byte[blockLen];
                if (bis.read(block) != blockLen) break;

                tapeBlocks.add(block);
            }
        } catch (Exception e) { e.printStackTrace(); }

        // --- CORRECCIÓN CRÍTICA ---
        // 1. Reseteamos PRIMERO (Esto apaga tapePlaying)
        reset();

        // 2. Activamos la cinta y el robot DESPUÉS
        tapePlaying = true;
        autoLoadStep = 1;
        autoLoadDelay = 50;
    }

    private void processAutoLoad() {
        if (autoLoadStep == 0) return;
        if (autoLoadDelay > 0) { autoLoadDelay--; return; }

        Arrays.fill(keyboardMatrix, 0xFF);

        switch (autoLoadStep) {
            case 1: setKey(6, 3); nextStep(5); break; // J
            case 2: nextStep(5); break;
            case 3: setKey(7, 1); setKey(5, 0); nextStep(5); break; // "
            case 4: nextStep(5); break;
            case 5: setKey(7, 1); setKey(5, 0); nextStep(5); break; // "
            case 6: nextStep(5); break;
            case 7: setKey(6, 0); nextStep(5); break; // Enter
            case 8: autoLoadStep = 0; break;
            default: autoLoadStep++; break;
        }
    }

    private void setKey(int row, int bit) {
        keyboardMatrix[row] &= ~(1 << bit);
    }

    private void nextStep(int delay) {
        autoLoadDelay = delay;
        autoLoadStep++;
    }

    public int input(int port) {
        if ((port & 1) == 0) {
            int result = 0xFF;
            for (int i = 0; i < 8; i++) {
                if ((port & (1 << (i + 8))) == 0) {
                    result &= keyboardMatrix[i];
                }
            }
            return result;
        }
        return 0xFF;
    }

    public void output(int port, int value) {
        if ((port & 1) == 0) currentBorderColor = value & 0x07;
    }

    @Override
    public void reset() {
        running = false;
        try { if (emulatorThread != null) emulatorThread.join(100); } catch (Exception e) {}

        cpu.reset();
        memory.reset();
        Arrays.fill(keyboardMatrix, 0xFF); // Soltamos teclas al resetear
        currentBorderColor = 7;
        tapePlaying = false; // El reset apaga la cinta (¡OJO aquí!)

        running = true;
        paused = false;
        emulatorThread = new Thread(this::runLoop);
        emulatorThread.setName("Spectrum-Thread");
        emulatorThread.setDaemon(true);
        emulatorThread.start();
    }

    private void drawScanline(int line) {
        int bufferY = line - 32;
        if (bufferY < 0 || bufferY >= 240) return;
        int width = 320;
        int offset = bufferY * width;
        int borderColor = PALETTE[currentBorderColor];
        Arrays.fill(videoBuffer, offset, offset + width, borderColor);
        if (bufferY >= 24 && bufferY < 216) {
            int screenY = bufferY - 24;
            int vramAddr = 0x4000 | ((screenY & 0xC0) << 5) | ((screenY & 0x07) << 8) | ((screenY & 0x38) << 2);
            int attrAddr = 0x5800 | ((screenY >> 3) << 5);
            for (int col = 0; col < 32; col++) {
                int pixels = memory.read(vramAddr + col);
                int attr = memory.read(attrAddr + col);
                int ink = PALETTE[attr & 0x07];
                int paper = PALETTE[(attr >> 3) & 0x07];
                if ((attr & 0x40) != 0) { ink = PALETTE[(attr & 0x07) + 8]; paper = PALETTE[((attr >> 3) & 0x07) + 8]; }
                if ((attr & 0x80) != 0 && flashState) { int t = ink; ink = paper; paper = t; }
                int pixelOffset = offset + 32 + (col * 8);
                for (int bit = 0; bit < 8; bit++) {
                    videoBuffer[pixelOffset + bit] = ((pixels & (0x80 >> bit)) != 0) ? ink : paper;
                }
            }
        }
    }

    // --- IMPLEMENTACIÓN DE MÉTODOS DE LA INTERFAZ ---

    @Override
    public void pause() { paused = true; }

    @Override
    public void stop() { running = false; paused = true; }

    @Override
    public boolean isPaused() { return paused; }

    @Override
    public long getTStates() { return cpu.getTStates(); }

    @Override
    public void step() { if (cpu != null) cpu.step(); }

    @Override
    public void stepOver() { step(); }

    @Override
    public void toggleBreakpoint(int address) {
        if (breakpoints.contains(address)) breakpoints.remove(address);
        else breakpoints.add(address);
    }

    @Override
    public void loadRom(String path) {
        try { memory.loadRom(path); cpu.reset(); } catch(Exception e){}
    }

    @Override
    public void loadSnapshot(String path) {}

    @Override
    public void setOnCpuStop(Runnable callback) { this.onStopCallback = callback; }

    @Override
    public int peek(int address) { return memory.read(address); }

    @Override
    public void poke(int address, int value) { memory.write(address, value); }

    @Override
    public int getRegister(String name) {
        if (cpu == null) return 0;
        switch (name.toUpperCase()) {
            case "AF": return (cpu.A << 8) | cpu.F;
            case "BC": return (cpu.B << 8) | cpu.C;
            case "DE": return (cpu.D << 8) | cpu.E;
            case "HL": return (cpu.H << 8) | cpu.L;
            case "PC": return cpu.PC;
            case "SP": return cpu.SP;
            case "IX": return cpu.IX;
            case "IY": return cpu.IY;
            case "I": return cpu.I;
            case "R": return cpu.R;
            case "A": return cpu.A;
            case "F": return cpu.F;
            case "B": return cpu.B;
            case "C": return cpu.C;
            case "D": return cpu.D;
            case "E": return cpu.E;
            case "H": return cpu.H;
            case "L": return cpu.L;
            default: return 0;
        }
    }
}