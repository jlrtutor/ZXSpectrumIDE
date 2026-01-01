package com.lazyzxsoftware.zxspectrumide.emulator.core;

public interface Bus {
    int read(int address);
    void write(int address, int value);

    int input(int port);
    void output(int port, int value);
}