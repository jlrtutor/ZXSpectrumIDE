package com.lazyzxsoftware.zxspectrumide.ui.windows;

import com.lazyzxsoftware.zxspectrumide.ui.webview.EmulatorWebView;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class EmulatorStage extends Stage {

    private final EmulatorWebView emulatorView;

    public EmulatorStage() {
        setTitle("ZX Spectrum Emulation (Web Core)");

        // Instanciamos nuestro componente web
        emulatorView = new EmulatorWebView();

        Scene scene = new Scene(emulatorView, 700, 550);
        setScene(scene);

        // Icono
        try {
            // Nota: Ajusta la ruta si es necesario según tu estructura de carpetas
            getIcons().add(new Image(getClass().getResourceAsStream("/com/lazyzxsoftware/zxspectrumide/icons/app_icon.png")));
        } catch (Exception ignored) {}

        // 1. Al cerrar: Solo ocultamos la ventana (no cerramos la app)
        setOnCloseRequest(event -> {
            event.consume();
            hide();
        });

        // 2. NUEVO: Al abrir la ventana, quitamos el PAUSE automáticamente
        setOnShown(event -> {
            System.out.println("🔄 Ventana visible. Reactivando emulador...");
            if (emulatorView != null) {
                // Esto llama a 'emulator.start()' en JS, que quita el botón PLAY
                emulatorView.resumeEmulator();
                // Opcional: Pedir foco para asegurar que las teclas funcionen
                emulatorView.requestFocus();
            }
        });
    }

    public EmulatorWebView getEmulatorView() {
        return emulatorView;
    }
}