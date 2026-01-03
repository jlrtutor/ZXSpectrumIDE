package com.lazyzxsoftware.zxspectrumide.emulator.interfaces;

import com.lazyzxsoftware.zxspectrumide.emulator.core.Memory;
import java.io.File;

public interface SpectrumEmulator {
    void start();
    void pause();
    void stop();
    void reset();
    void step();
    void stepOver();
    void toggleBreakpoint(int address);
    boolean isPaused();
    long getTStates();

    int peek(int address);
    void poke(int address, int value);
    int getRegister(String name);

    // VIDEO (Double Buffer)
    int[] getScreenBuffer();
    int getBorderColor();

    Memory getMemory();

    void loadRom(String path);
    void loadSnapshot(String path);

    // CAMBIO: Inyectamos el archivo real para imitar a JSpeccy
    void loadProgram(File tapFile);

    void setOnCpuStop(Runnable callback);
}