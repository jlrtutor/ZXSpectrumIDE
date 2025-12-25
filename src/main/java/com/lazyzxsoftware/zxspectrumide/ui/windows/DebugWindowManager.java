package com.lazyzxsoftware.zxspectrumide.ui.windows;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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

    // --- MÉTODOS PÚBLICOS PARA ABRIR VENTANAS ---

    public void showMainDebugger() {
        showWindow("Debugger Principal", new MainDebuggerWindow(), 800, 600);
    }

    public void showRegisters() {
        showWindow("Registros CPU", new RegistersWindow(), 300, 400);
    }

    public void showMemory() {
        showWindow("Visor de Memoria", new MemoryWindow(), 600, 400);
    }

    public void showStack() {
        showWindow("Call Stack", new StackWindow(), 250, 400);
    }

    public void showBreakpoints() {
        showWindow("Breakpoints", new BreakpointsWindow(), 400, 300);
    }

    public void showWatchers() {
        showWindow("Watchers (Variables)", new WatchersWindow(), 300, 400);
    }

    public void closeAll() {
        openWindows.values().forEach(Stage::close);
        openWindows.clear();
    }

    // --- LÓGICA INTERNA ---

    private void showWindow(String title, BorderPane content, double width, double height) {
        if (openWindows.containsKey(title)) {
            Stage stage = openWindows.get(title);
            stage.show();
            stage.toFront();
        } else {
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(content, width, height));
            // stage.initStyle(StageStyle.UTILITY); // Descomenta si prefieres estilo "paleta flotante"

            // Aplicar tema oscuro si existe
            try {
                stage.getScene().getStylesheets().add(getClass().getResource("/com/lazyzxsoftware/zxspectrumide/themes/deep-ocean.css").toExternalForm());
                content.setStyle("-fx-background-color: #2b2b2b;");
            } catch (Exception e) {
                System.err.println("No se pudo cargar el tema oscuro: " + e.getMessage());
            }

            stage.setOnHidden(e -> openWindows.remove(title));
            openWindows.put(title, stage);
            stage.show();
        }
    }

    // Método puente para actualizar la UI si la ventana de registros está visible
    public void updateRegisterInfo(int af, int bc, int de, int hl,
                                   int af_, int bc_, int de_, int hl_,
                                   int ix, int iy, int sp, int pc) {

        if (openWindows.containsKey("Registros CPU")) {
            RegistersWindow win = (RegistersWindow) openWindows.get("Registros CPU").getScene().getRoot();
            win.updateRegisters(af, bc, de, hl, af_, bc_, de_, hl_, ix, iy, sp, pc);
        }
    }
}