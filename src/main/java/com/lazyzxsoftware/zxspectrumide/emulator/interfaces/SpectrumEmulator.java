package com.lazyzxsoftware.zxspectrumide.emulator.interfaces;

import com.lazyzxsoftware.zxspectrumide.emulator.core.Memory;
import java.util.function.Consumer;

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

    // CAMBIO: Ahora obtenemos el buffer de video completo ya pintado por la ULA
    int[] getScreenBuffer();

    Memory getMemory(); // Lo mantenemos para el depurador de memoria
    int getBorderColor();

    void loadRom(String path);
    void loadSnapshot(String path);
    void loadProgram(byte[] data, String name);
    void setOnCpuStop(Runnable callback);
}