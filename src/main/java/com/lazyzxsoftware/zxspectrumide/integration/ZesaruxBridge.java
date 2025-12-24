package com.lazyzxsoftware.zxspectrumide.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Gestiona la comunicación con ZEsarUX a través del protocolo ZRCP (TCP).
 * Puerto por defecto: 10000
 * Implementa Singleton, Timeout Dinámico y Doble Cerrojo (Split Locking).
 */
public class ZesaruxBridge {

    private static final String HOST = "localhost";
    private static final int PORT = 10000;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private final AtomicBoolean connected = new AtomicBoolean(false);
    private static ZesaruxBridge instance;

    // --- DOBLE CERROJO (SPLIT LOCKING) ---
    private final Object sendLock = new Object();
    private final Object receiveLock = new Object();

    private ZesaruxBridge() { }

    public static synchronized ZesaruxBridge getInstance() {
        if (instance == null) {
            instance = new ZesaruxBridge();
        }
        return instance;
    }

    public boolean connect() {
        if (connected.get()) return true;

        try {
            socket = new Socket(HOST, PORT);
            socket.setSoTimeout(3000); // 3s para el handshake inicial

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("[ZesaruxBridge] Socket abierto. Sincronizando...");

            // 1. Limpieza inicial estricta
            if (!readUntilPrompt()) {
                System.err.println("[ZesaruxBridge] Error: No se detectó el prompt de bienvenida.");
                disconnect();
                return false;
            }

            // 2. Apagar Eco (para que las respuestas sean limpias)
            synchronized (sendLock) {
                out.print("set-debug-settings echo off\n");
                out.flush();
            }
            // Consumir la respuesta del comando echo off
            synchronized (receiveLock) {
                readUntilPrompt();
            }

            // Timeout operativo estándar (1s)
            socket.setSoTimeout(1000);

            connected.set(true);
            System.out.println("[ZesaruxBridge] Conectado y Sincronizado.");
            return true;

        } catch (IOException e) {
            System.err.println("[ZesaruxBridge] Error de conexión: " + e.getMessage());
            disconnect();
            return false;
        }
    }

    public void disconnect() {
        connected.set(false);
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("[ZesaruxBridge] Desconectado.");
    }

    /**
     * Envía un comando. Si es 'run', espera indefinidamente hasta que pare.
     */
    public CompletableFuture<String> sendCommand(String command) {
        return CompletableFuture.supplyAsync(() -> {
            if (!connected.get()) return "Error: No conectado";

            boolean isRunCommand = command.trim().startsWith("run");

            try {
                if (isRunCommand) {
                    // --- MODO RUN (ESPECIAL) ---
                    synchronized (sendLock) {
                        out.print(command + "\n");
                        out.flush();
                    }

                    // Esperar con TIMEOUT INFINITO hasta que el emulador pare
                    synchronized (receiveLock) {
                        int oldTimeout = socket.getSoTimeout();
                        socket.setSoTimeout(0); // Infinito
                        try {
                            return readResponse();
                        } finally {
                            socket.setSoTimeout(oldTimeout); // Restaurar
                        }
                    }

                } else {
                    // --- MODO NORMAL (ATÓMICO) ---
                    synchronized (sendLock) {
                        out.print(command + "\n");
                        out.flush();

                        synchronized (receiveLock) {
                            // CORRECCIÓN: Quitada la limpieza "while (in.ready())".
                            // El buffer ya debe estar sincronizado. Si limpiamos aquí,
                            // borramos la respuesta que acaba de llegar si ZEsarUX fue rápido.

                            return readResponse();
                        }
                    }
                }

            } catch (SocketTimeoutException e) {
                System.out.println("[ZesaruxBridge] Timeout para: " + command);
                return "";
            } catch (IOException e) {
                System.err.println("[ZesaruxBridge] Error IO: " + e.getMessage());
                disconnect();
                return "Error: IO";
            }
        });
    }

    /**
     * Envía un comando sin esperar respuesta (Fire & Forget).
     * Útil para 'run' justo antes de desconectar.
     */
    public void sendCommandNoWait(String command) {
        if (!connected.get()) return;

        CompletableFuture.runAsync(() -> {
            synchronized (sendLock) {
                try {
                    out.print(command + "\n");
                    out.flush();
                } catch (Exception e) {
                    System.err.println("Error enviando no-wait: " + e.getMessage());
                }
            }
        });
    }

    private boolean readUntilPrompt() {
        try {
            int c;
            while ((c = in.read()) != -1) {
                if ((char) c == '>') return true;
            }
        } catch (IOException e) { return false; }
        return false;
    }

    private String readResponse() throws IOException {
        StringBuilder response = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            char ch = (char) c;
            if (ch == '>') break;
            response.append(ch);
        }
        return response.toString().replace("command", "").trim();
    }

    public boolean isConnected() { return connected.get(); }
}