package com.lazyzxsoftware.zxspectrumide.managers;

import com.lazyzxsoftware.zxspectrumide.ui.webview.EmulatorWebView;
import com.lazyzxsoftware.zxspectrumide.ui.windows.EmulatorStage;

/**
 * Gestor centralizado de ventanas para la arquitectura JavaFX.
 * Se asegura de que solo exista una instancia de las ventanas flotantes.
 */
public class WindowManager {

    private static WindowManager instance;

    // Referencia a la ventana del emulador (WebView)
    private EmulatorStage emulatorStage;

    private EmulatorWebView emulatorWebView;

    private WindowManager() {
        // Constructor privado para Singleton
    }

    public static synchronized WindowManager getInstance() {
        if (instance == null) {
            instance = new WindowManager();
        }
        return instance;
    }

    /**
     * Muestra la ventana del emulador. Si no existe, la crea.
     * Si ya existe pero está oculta, la muestra y la trae al frente.
     */
    public void showEmulator() {
        // Lazy initialization: solo se crea la primera vez que se pide
        if (emulatorStage == null) {
            emulatorStage = new EmulatorStage();
            // CAPTURAMOS LA REFERENCIA AQUÍ
        }

        // Si se cerró o estaba oculta, la mostramos
        if (!emulatorStage.isShowing()) {
            emulatorStage.show();
        }

        // Traer al frente por si estaba tapada
        emulatorStage.toFront();
    }

    /**
     * Cierra definitivamente la ventana del emulador (útil al salir de la app).
     */
    public void closeEmulator() {
        if (emulatorStage != null) {
            emulatorStage.close();
            emulatorStage = null;
        }
    }

    // Setter (llámalo desde donde crees el emulador)
    public void setEmulatorWebView(EmulatorWebView view) {
        this.emulatorWebView = view;
    }

    /**
     * Permite acceder al controlador del emulador (WebView) desde otras partes
     * de la aplicación (como el Debugger).
     */
    public EmulatorWebView getEmulatorWebView() {
        // Si la ventana del emulador no se ha creado todavía, devolvemos null
        if (emulatorStage == null) {
            return null;
        }
        // Delegamos en el getter que ya creaste en EmulatorStage
        return emulatorStage.getEmulatorView();
    }
}