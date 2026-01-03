package com.lazyzxsoftware.zxspectrumide.ui.webview;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

public class EmulatorWebView extends StackPane {

    private final Gson gson = new Gson();
    private final WebView webView;
    private final WebEngine webEngine;
    private boolean isEmulatorReady = false;
    private Runnable pendingTask = null;

    public EmulatorWebView() {
        webView = new WebView();
        webEngine = webView.getEngine();

        // Logs de consola y alertas
        webEngine.setOnAlert(event -> System.out.println("JS Alert: " + event.getData()));
        webEngine.setOnError(event -> System.err.println("JS Error: " + event.getMessage()));

        loadEmulator();

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaApp", this);
            }
        });

        this.getChildren().add(webView);
    }

    private void loadEmulator() {
        try {
            String path = "/com/lazyzxsoftware/zxspectrumide/webview/emulator.html";
            URL url = getClass().getResource(path);
            if (url != null) webEngine.load(url.toExternalForm());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- PUENTE JS->JAVA ---

    public void log(String message) { System.out.println("🖥️ [JS]: " + message); }

    public void onEmulatorReady() {
        System.out.println("✅ Emulador confirma que está LISTO.");
        isEmulatorReady = true;
        if (pendingTask != null) {
            Platform.runLater(pendingTask);
            pendingTask = null;
        }
    }

    public void onEmulatorPaused(String jsonState) {
        Platform.runLater(() -> {
            try {
                JsonObject state = gson.fromJson(jsonState, JsonObject.class);

                // Extracción segura de campos
                int pc = state.get("pc").getAsInt();
                int memStart = state.has("previewMemStart") ? state.get("previewMemStart").getAsInt() : pc;

                int sp = state.get("sp").getAsInt();
                int af = state.get("af").getAsInt();
                int bc = state.get("bc").getAsInt();
                int de = state.get("de").getAsInt();
                int hl = state.get("hl").getAsInt();
                int ix = state.get("ix").getAsInt();
                int iy = state.get("iy").getAsInt();
                int af_ = state.get("af_").getAsInt();
                int bc_ = state.get("bc_").getAsInt();
                int de_ = state.get("de_").getAsInt();
                int hl_ = state.get("hl_").getAsInt();

                byte[] memoryData = state.has("previewMem") ?
                        Base64.getDecoder().decode(state.get("previewMem").getAsString()) : new byte[0];

                byte[] traceData = state.has("traceLog") ?
                        Base64.getDecoder().decode(state.get("traceLog").getAsString()) : null;

                // Actualizar UI
                com.lazyzxsoftware.zxspectrumide.ui.windows.DebugWindowManager.getInstance()
                        .updateDebugInfo(af, bc, de, hl, af_, bc_, de_, hl_, ix, iy, sp, pc, memStart, memoryData, traceData);

            } catch (Exception e) {
                System.err.println("❌ Error procesando datos de debug: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // --- COMANDOS JAVA->JS ---

    public void requestRomLoad() {
        new Thread(() -> {
            try {
                InputStream is = getClass().getResourceAsStream("/com/lazyzxsoftware/zxspectrumide/webview/roms/48k.rom");
                if (is != null) {
                    String romBase64 = Base64.getEncoder().encodeToString(is.readAllBytes());
                    is.close();
                    Platform.runLater(() -> webEngine.executeScript("startEmulatorWithRom('" + romBase64 + "')"));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    public void loadProgram(byte[] data, String filename) {
        if (!isEmulatorReady) {
            pendingTask = () -> loadProgram(data, filename);
            return;
        }
        String base64 = Base64.getEncoder().encodeToString(data);
        Platform.runLater(() -> {
            String safeName = filename.replace("'", "\\'");
            webEngine.executeScript("loadProgramFromJava('" + base64 + "', '" + safeName + "')");
        });
    }

    public void pauseEmulator() {
        Platform.runLater(() -> webEngine.executeScript("if(emulator) { emulator.stop(); notifyJavaPaused('manual_pause'); }"));
    }

    public void resumeEmulator() {
        Platform.runLater(() -> webEngine.executeScript("if(emulator) emulator.start();"));
    }

    public void stepInto() {
        Platform.runLater(() -> {
            // SOLUCIÓN TÉCNICA:
            // 1. Obtenemos el tiempo actual con z80.getTstates() (tStateCount NO existe).
            // 2. Ejecutamos hasta 'tiempo actual + 1'. Esto fuerza al emulador a entrar en su bucle
            //    y ejecutar la instrucción atómica completa (sea cual sea su duración).
            String script =
                    "try { " +
                            "  if(emulator && emulator.getZ80) { " +
                            "     emulator.stop(); " +
                            "     var z80 = emulator.getZ80(); " +
                            "     " +
                            "     if (typeof z80.getTstates === 'function') { " +
                            "         var currentT = z80.getTstates(); " +
                            "         z80.runFrame(currentT + 1); " +
                            "     } else { " +
                            "         console.error('ERROR: z80.getTstates no existe. Revisa jsspeccy-core.min.js'); " +
                            "     } " +
                            "     " +
                            "     notifyJavaPaused('step'); " +
                            "  } " +
                            "} catch(e) { console.error('Error JS en Step: ' + e); }";

            webEngine.executeScript(script);
        });
    }

    public void resetEmulator() { Platform.runLater(() -> webEngine.executeScript("if(emulator) emulator.reset();")); }

    public void fetchMemory(int address, int size) {
        if (isEmulatorReady) Platform.runLater(() -> webEngine.executeScript("readMemoryBlock(" + address + ", " + size + ")"));
    }

    public void onMemoryRead(int startAddress, String base64Data) {
        Platform.runLater(() -> {
            try {
                byte[] data = Base64.getDecoder().decode(base64Data);
                com.lazyzxsoftware.zxspectrumide.ui.windows.DebugWindowManager.getInstance()
                        .updateMemoryView(startAddress, data);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }
}