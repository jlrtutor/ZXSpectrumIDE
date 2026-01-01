package com.lazyzxsoftware.zxspectrumide.ui.windows;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

public class DebugWindowManager {

    private static DebugWindowManager instance;
    private final Map<String, Stage> openWindows = new HashMap<>();

    private DebugWindowManager() {}

    public static DebugWindowManager getInstance() {
        if (instance == null) instance = new DebugWindowManager();
        return instance;
    }

    // --- ABRIR VENTANAS ---
    // Nota: Como RegistersWindow ahora es BorderPane, showWindow lo aceptará sin problemas.
    public void showMainDebugger() { showWindow("Debugger Principal", new MainDebuggerWindow(), 800, 600); }
    public void showRegisters()    { showWindow("Registros CPU", new RegistersWindow(), 340, 250); }
    public void showMemory()       { showWindow("Visor de Memoria", new MemoryWindow(), 600, 400); }
    public void showStack()        { showWindow("Call Stack", new StackWindow(), 250, 400); }
    public void showBreakpoints()  { showWindow("Breakpoints", new BreakpointsWindow(), 400, 300); }
    public void showWatchers()     { showWindow("Watchers (Variables)", new WatchersWindow(), 300, 400); }
    public void showTraceLog()     { showWindow("Trace Log", new TraceLogWindow(), 600, 400); }

    public void closeAll() {
        openWindows.values().forEach(Stage::close);
        openWindows.clear();
    }

    // --- ACTUALIZACIÓN (ARQUITECTURA PULL) ---

    // Este método es llamado por WindowManager, BreakpointManager, etc.
    public void refreshDebuggerUI() {
        refreshAllOpenWindows();
    }

    // Notifica a todas las ventanas abiertas que lean los datos nuevos del emulador
    public void refreshAllOpenWindows() {
        Platform.runLater(() -> {

            // 1. Debugger Principal
            if (openWindows.containsKey("Debugger Principal")) {
                Stage stage = openWindows.get("Debugger Principal");
                if (stage.getScene().getRoot() instanceof MainDebuggerWindow) {
                    ((MainDebuggerWindow) stage.getScene().getRoot()).refreshAll();
                }
            }

            // 2. Registros (Llama al método update() que acabamos de crear)
            if (openWindows.containsKey("Registros CPU")) {
                Stage stage = openWindows.get("Registros CPU");
                if (stage.getScene().getRoot() instanceof RegistersWindow) {
                    ((RegistersWindow) stage.getScene().getRoot()).update();
                }
            }

            // 3. Memoria
            if (openWindows.containsKey("Visor de Memoria")) {
                Stage stage = openWindows.get("Visor de Memoria");
                if (stage.getScene().getRoot() instanceof MemoryWindow) {
                    ((MemoryWindow) stage.getScene().getRoot()).update();
                }
            }

            // 4. Trace Log (Si la ventana tuviera método de refresh, aquí iría)
            // Por ahora TraceLog suele ser Push por rendimiento, pero lo dejamos así para compilar.
        });
    }

    // --- MÉTODOS LEGACY (Mantenidos para compatibilidad pero redirigidos) ---

    public void updateDebugInfo(int af, int bc, int de, int hl, int af_, int bc_, int de_, int hl_,
                                int ix, int iy, int sp, int pc, int memStart, byte[] memory, byte[] trace) {
        refreshAllOpenWindows();
    }

    public void updateRegisterInfo(int af, int bc, int de, int hl, int af_, int bc_, int de_, int hl_,
                                   int ix, int iy, int sp, int pc) {
        refreshAllOpenWindows();
    }

    public void updateMemoryView(int addr, byte[] data) {
        refreshAllOpenWindows();
    }

    // --- HELPER PRIVADO ---
    private void showWindow(String title, BorderPane content, double width, double height) {
        if (openWindows.containsKey(title)) {
            Stage stage = openWindows.get(title);
            stage.show();
            stage.toFront();
        } else {
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(content, width, height));

            try {
                stage.getScene().getStylesheets().add(getClass().getResource("/com/lazyzxsoftware/zxspectrumide/themes/deep-ocean.css").toExternalForm());
                content.setStyle("-fx-background-color: #2b2b2b;");
            } catch (Exception e) {}

            stage.setOnHidden(e -> openWindows.remove(title));
            openWindows.put(title, stage);
            stage.show();
        }
    }
}