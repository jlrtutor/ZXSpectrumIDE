package com.lazyzxsoftware.zxspectrumide.managers;

import com.lazyzxsoftware.zxspectrumide.emulator.interfaces.SpectrumEmulator;
import com.lazyzxsoftware.zxspectrumide.ui.windows.DebugWindowManager;
import javafx.application.Platform;
import java.util.HashSet;
import java.util.Set;

public class BreakpointManager {

    private static BreakpointManager instance;
    private final Set<Integer> breakpoints;

    private BreakpointManager() {
        this.breakpoints = new HashSet<>();
    }

    public static synchronized BreakpointManager getInstance() {
        if (instance == null) {
            instance = new BreakpointManager();
        }
        return instance;
    }

    // --- Gestión de Breakpoints ---

    public void toggleBreakpoint(int address) {
        if (breakpoints.contains(address)) {
            breakpoints.remove(address);
        } else {
            breakpoints.add(address);
        }
        // Sincronizar cambios con el emulador y la UI
        updateEmulator(address);
        notifyUI();
    }

    public void addBreakpoint(int address) {
        if (breakpoints.add(address)) {
            // Solo notificamos si realmente es nuevo
            updateEmulator(address);
            notifyUI();
        }
    }

    public void removeBreakpoint(int address) {
        if (breakpoints.remove(address)) {
            // Solo notificamos si realmente existía
            updateEmulator(address);
            notifyUI();
        }
    }

    public boolean isBreakpoint(int address) {
        return breakpoints.contains(address);
    }

    public void clearAll() {
        // Para borrar todo del emulador (que solo tiene toggle),
        // recorremos los breakpoints activos y los "toggleamos" para quitarlos.
        SpectrumEmulator emu = WindowManager.getInstance().getEmulator();
        if (emu != null) {
            for (Integer addr : breakpoints) {
                emu.toggleBreakpoint(addr);
            }
        }
        breakpoints.clear();
        notifyUI();
    }

    public Set<Integer> getBreakpoints() {
        return new HashSet<>(breakpoints); // Copia defensiva
    }

    // --- Comunicación con el Emulador ---

    private void updateEmulator(int address) {
        SpectrumEmulator emu = WindowManager.getInstance().getEmulator();
        if (emu != null) {
            // Como mantenemos sincronizados los estados, un toggle aquí
            // refleja la acción de añadir/quitar que acabamos de hacer en la lista.
            emu.toggleBreakpoint(address);
        }
    }

    private void notifyUI() {
        Platform.runLater(() -> {
            DebugWindowManager.getInstance().refreshDebuggerUI();
        });
    }
}