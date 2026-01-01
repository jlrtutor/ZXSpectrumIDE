package com.lazyzxsoftware.zxspectrumide.managers;

import com.lazyzxsoftware.zxspectrumide.emulator.interfaces.SpectrumEmulator;
import com.lazyzxsoftware.zxspectrumide.ui.windows.DebugWindowManager;
import com.lazyzxsoftware.zxspectrumide.ui.windows.EmulatorStage;

public class WindowManager {

    private static WindowManager instance;

    // Referencia a la interfaz del emulador (El motor nativo)
    private SpectrumEmulator emulator;

    // Referencia a la ventana gráfica principal del emulador
    private EmulatorStage emulatorStage;

    private WindowManager() {}

    public static synchronized WindowManager getInstance() {
        if (instance == null) {
            instance = new WindowManager();
        }
        return instance;
    }

    /**
     * Muestra la ventana del emulador. Si no existe, la crea.
     */
    public void showEmulator() {
        if (emulatorStage == null) {
            emulatorStage = new EmulatorStage();
        }

        if (!emulatorStage.isShowing()) {
            emulatorStage.show();
        }
        emulatorStage.toFront();
    }

    /**
     * Cierra la ventana del emulador y detiene la ejecución.
     */
    public void closeEmulator() {
        if (emulatorStage != null) {
            emulatorStage.close();
            if (emulator != null) {
                emulator.stop();
            }
            emulatorStage = null;
        }
    }

    // --- MÉTODOS DE ACCESO AL NÚCLEO (SpectrumEmulator) ---

    public void setEmulator(SpectrumEmulator emulator) {
        this.emulator = emulator;
    }

    public SpectrumEmulator getEmulator() {
        return emulator;
    }

    // --- DELEGACIÓN DE DEBUG ---

    /**
     * Avisa a las herramientas de depuración para que se actualicen.
     * Soluciona el error de "cannot find symbol registersWindow" delegando
     * la tarea al manager correcto.
     */
    public void refreshDebugger() {
        DebugWindowManager.getInstance().refreshAllOpenWindows();
    }
}