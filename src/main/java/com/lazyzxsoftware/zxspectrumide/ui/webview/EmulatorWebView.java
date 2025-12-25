package com.lazyzxsoftware.zxspectrumide.ui.webview;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmulatorWebView extends StackPane {

    private final WebView webView;
    private final WebEngine webEngine;
    private ScheduledExecutorService debugPoller;

    // --- NUEVO: CONTROL DE ESTADO ---
    private boolean isEmulatorReady = false;
    private Runnable pendingTask = null;

    public EmulatorWebView() {
        webView = new WebView();
        webEngine = webView.getEngine();

        // Configuración para permitir logs y alertas
        webEngine.setOnAlert(event -> System.out.println("JS Alert: " + event.getData()));
        webEngine.setOnError(event -> System.err.println("JS Error: " + event.getMessage()));

        // Cargar el HTML
        loadEmulator();

        // Configurar el Puente (Bridge)
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaApp", this);
                System.out.println("✅ Puente Java-JS inicializado.");
                // Nota: isEmulatorReady se pondrá a true cuando JS nos avise, no aquí.
            }
        });

        this.getChildren().add(webView);
    }

    private void loadEmulator() {
        try {
            String path = "/com/lazyzxsoftware/zxspectrumide/webview/emulator.html";
            URL url = getClass().getResource(path);
            if (url != null) {
                webEngine.load(url.toExternalForm());
            } else {
                System.err.println("❌ ERROR CRÍTICO: No se encuentra emulator.html");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =================================================================
    // MÉTODOS PÚBLICOS (BRIDGE)
    // =================================================================

    public void log(String message) {
        System.out.println("🖥️ [JS]: " + message);
    }

    /**
     * JS nos llama cuando ha terminado de arrancar y está listo para recibir comandos.
     */
    public void onEmulatorReady() {
        System.out.println("✅ Emulador confirma que está LISTO.");
        isEmulatorReady = true;

        // Si teníamos una carga pendiente (lo que fallaba a la primera), la lanzamos ahora
        if (pendingTask != null) {
            System.out.println("🔄 Ejecutando tarea pendiente...");
            Platform.runLater(pendingTask);
            pendingTask = null;
        }
    }

    public void requestRomLoad() {
        System.out.println("⚡ JS pide ROM...");
        new Thread(() -> {
            try {
                String romPath = "/com/lazyzxsoftware/zxspectrumide/webview/roms/48.rom";
                InputStream is = getClass().getResourceAsStream(romPath);
                if (is == null) {
                    System.err.println("❌ Error: 48.rom no encontrada.");
                    return;
                }
                byte[] romBytes = is.readAllBytes();
                String romBase64 = Base64.getEncoder().encodeToString(romBytes);
                is.close();

                Platform.runLater(() -> webEngine.executeScript("startEmulatorWithRom('" + romBase64 + "')"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // =================================================================
    // CONTROL DE CARGA Y EJECUCIÓN
    // =================================================================

    public void loadProgram(byte[] data, String filename) {
        // --- FIX: SISTEMA DE COLA ---
        if (!isEmulatorReady) {
            System.out.println("⏳ Emulador ocupado/iniciando. Encolando carga de: " + filename);
            // Guardamos la tarea para luego
            pendingTask = () -> loadProgram(data, filename);

            // Si la ventana estaba oculta/cerrada y se vuelve a abrir, aseguramos que se muestre
            // (La lógica de mostrar la ventana está en Main, aquí solo gestionamos la carga)
            return;
        }

        System.out.println("📤 Enviando programa: " + filename);
        startMonitoring(); // Aseguramos que el debug se reactiva

        String base64 = Base64.getEncoder().encodeToString(data);
        Platform.runLater(() -> {
            String safeName = filename.replace("'", "\\'");
            // Llamamos a loadProgramFromJava. Si el emulador estaba pausado,
            // la función JS se encargará de reactivarlo (autoload:true).
            webEngine.executeScript("loadProgramFromJava('" + base64 + "', '" + safeName + "')");
        });
    }

    // =================================================================
    // DEBUG & CONTROL
    // =================================================================

    public void startMonitoring() {
        if (debugPoller != null && !debugPoller.isShutdown()) {
            debugPoller.shutdownNow();
        }
        debugPoller = Executors.newSingleThreadScheduledExecutor();
        debugPoller.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                if (webEngine != null && isEmulatorReady) {
                    try { webEngine.executeScript("debugTest()"); } catch (Exception e) {}
                }
            });
        }, 500, 200, TimeUnit.MILLISECONDS);
    }

    public void stopMonitoring() {
        if (debugPoller != null) debugPoller.shutdownNow();
    }

    // Controles manuales
    public void pauseEmulator() { Platform.runLater(() -> webEngine.executeScript("if(emulator) emulator.stop();")); }
    public void resumeEmulator() { Platform.runLater(() -> webEngine.executeScript("if(emulator) emulator.start();")); }
    public void stepInto() {
        Platform.runLater(() -> {
            webEngine.executeScript("if(emulator) { emulator.stop(); emulator.getSpectrum().runFrame(1); debugTest(); }");
        });
    }
    public void resetEmulator() { Platform.runLater(() -> webEngine.executeScript("if(emulator) emulator.reset();")); }

    // Método para recibir datos de debug (el que ya tenías)
    public void updateDebugData(int af, int bc, int de, int hl, int af_, int bc_, int de_, int hl_, int ix, int iy, int sp, int pc) {
        Platform.runLater(() -> {
            com.lazyzxsoftware.zxspectrumide.ui.windows.DebugWindowManager.getInstance()
                    .updateRegisterInfo(af, bc, de, hl, af_, bc_, de_, hl_, ix, iy, sp, pc);
        });
    }
}