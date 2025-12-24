package com.lazyzxsoftware.zxspectrumide.ui;

import com.lazyzxsoftware.zxspectrumide.integration.ZesaruxBridge;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.CompletableFuture;

public class DebugPanel extends BorderPane {

    // Componentes UI
    private TextArea disassemblyArea;
    private ListView<String> stackList;

    // --- BOTONES (Variables de clase para controlar su estado) ---
    private Button btnConnect;
    private Button btnPause;
    private Button btnStep;
    private Button btnContinue;
    private Button btnGoToStart;

    // Etiquetas de Registros
    private Label regAF, regBC, regDE, regHL, regPC, regSP, regIX, regIY;
    private Label flagsLabel;

    // Estado interno
    private String currentPC = "";
    private String currentSP = "";

    // Lógica
    private final ZesaruxBridge bridge;
    private ScheduledExecutorService poller;
    private volatile boolean isPollingActive = false;

    public DebugPanel() {
        this.bridge = ZesaruxBridge.getInstance();
        initUI();
    }

    private void initUI() {
        ToolBar toolbar = new ToolBar();

        // Inicialización de botones
        btnConnect = new Button("Conectar");

        btnPause = new Button("Pausar (F8)");
        btnPause.setStyle("-fx-base: #ffdddd; -fx-text-fill: #8a0000;");

        btnStep = new Button("Paso (F10)");
        btnStep.setDisable(true); // Desactivado por defecto (hasta que pausamos)

        btnContinue = new Button("Continuar (F5)");

        btnGoToStart = new Button("Ir a Inicio ($8000)");
        btnGoToStart.setStyle("-fx-base: #ddffdd; -fx-text-fill: #005500;");

        // --- ACCIONES DE LOS BOTONES ---

        btnConnect.setOnAction(e -> toggleConnection());

        // PAUSAR: Parar y actualizar UNA VEZ -> ACTIVAR BOTÓN PASO
        btnPause.setOnAction(e -> {
            stopPolling();
            sendCommand("enter-cpu-step").thenRun(() -> {
                Platform.runLater(() -> {
                    btnStep.setDisable(false); // ACTIVAR PASO
                    refreshState();
                });
            });
        });

        // PASO: Avanzar uno y actualizar -> EL BOTÓN SIGUE ACTIVO
        btnStep.setOnAction(e -> {
            stopPolling();
            sendCommand("cpu-step").thenRun(this::refreshState);
        });

        // CONTINUAR: Correr y esperar -> DESACTIVAR BOTÓN PASO
        btnContinue.setOnAction(e -> {
            stopPolling();
            disassemblyArea.deselect();

            // Desactivamos Paso inmediatamente porque va a correr
            btnStep.setDisable(true);

            // Enviamos 'run' y esperamos (la promesa no volverá hasta que pare)
            bridge.sendCommand("run").thenRun(() -> {
                // Cuando el 'run' termina (se ha parado por breakpoint/usuario),
                // volvemos a activar el botón Paso y actualizamos la UI
                Platform.runLater(() -> {
                    btnStep.setDisable(false); // REACTIVAR PASO
                    refreshState();
                });
            });
        });

        // IR A INICIO: Corre hasta 8000 -> DESACTIVAR y LUEGO REACTIVAR
        btnGoToStart.setOnAction(e -> {
            stopPolling();
            disassemblyArea.deselect();
            btnStep.setDisable(true); // DESACTIVAR (Corriendo...)

            bridge.sendCommand("run 8000").thenRun(() -> {
                Platform.runLater(() -> {
                    btnStep.setDisable(false); // REACTIVAR (Llegó a destino)
                    refreshState();
                });
            });
        });

        toolbar.getItems().addAll(btnConnect, new Separator(), btnPause, btnStep, btnContinue, new Separator(), btnGoToStart);
        setTop(toolbar);

        // --- 2. PANELES (SPLIT) ---
        SplitPane mainSplit = new SplitPane();

        // Panel Izquierdo: Desensamblado
        disassemblyArea = new TextArea();
        disassemblyArea.setFont(Font.font("Monospaced", 12));
        disassemblyArea.setEditable(false);
        disassemblyArea.setText("Esperando conexión... \n(Pulsa Conectar y luego Pausar)");

        // Estilo CSS para selección visible (azul intenso)
        disassemblyArea.setStyle(
                "-fx-highlight-fill: #0078d7; " +
                        "-fx-highlight-text-fill: white; " +
                        "-fx-display-caret: false;"
        );

        VBox leftPane = new VBox(new Label(" Desensamblado (PC)"), disassemblyArea);
        VBox.setVgrow(disassemblyArea, Priority.ALWAYS);

        // Panel Derecho: Registros y Stack
        SplitPane rightSplit = new SplitPane();
        rightSplit.setOrientation(javafx.geometry.Orientation.VERTICAL);

        VBox regsPane = createRegistersPane();
        VBox stackPane = createStackPane();

        rightSplit.getItems().addAll(regsPane, stackPane);
        rightSplit.setDividerPositions(0.45);

        mainSplit.getItems().addAll(leftPane, rightSplit);
        mainSplit.setDividerPositions(0.65);
        setCenter(mainSplit);

        // --- 3. ATAJOS DE TECLADO ---
        this.setFocusTraversable(true);
        this.setOnMouseClicked(e -> this.requestFocus());
        this.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case F10: case PERIOD:
                    if (!btnStep.isDisabled()) { // Solo si está habilitado
                        stopPolling();
                        sendCommand("cpu-step").thenRun(this::refreshState);
                    }
                    break;

                case F5: // Continuar
                    stopPolling();
                    disassemblyArea.deselect();
                    btnStep.setDisable(true);
                    bridge.sendCommand("run").thenRun(() -> Platform.runLater(() -> {
                        btnStep.setDisable(false);
                        refreshState();
                    }));
                    break;

                case F8: // Pausar
                    stopPolling();
                    sendCommand("enter-cpu-step").thenRun(() -> Platform.runLater(() -> {
                        btnStep.setDisable(false);
                        refreshState();
                    }));
                    break;
            }
            event.consume();
        });
    }

    private VBox createRegistersPane() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #2b2b2b;");

        Label title = new Label("Registros");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(5);

        regAF = createRegLabel("AF: 0000");
        regBC = createRegLabel("BC: 0000");
        regDE = createRegLabel("DE: 0000");
        regHL = createRegLabel("HL: 0000");
        regPC = createRegLabel("PC: 0000");
        regSP = createRegLabel("SP: 0000");
        regIX = createRegLabel("IX: 0000");
        regIY = createRegLabel("IY: 0000");
        flagsLabel = createRegLabel("Flags: ........");

        grid.addRow(0, regAF, regSP);
        grid.addRow(1, regBC, regDE);
        grid.addRow(2, regHL, regPC);
        grid.addRow(3, regIX, regIY);

        box.getChildren().addAll(title, grid, new Separator(), flagsLabel);
        return box;
    }

    private VBox createStackPane() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(5));

        Label title = new Label(" Stack (Pila)");
        title.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 12));

        stackList = new ListView<>();
        stackList.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 11px;");
        VBox.setVgrow(stackList, Priority.ALWAYS);

        box.getChildren().addAll(title, stackList);
        return box;
    }

    private Label createRegLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Monospaced", 13));
        l.setStyle("-fx-text-fill: #00ff00;");
        return l;
    }

    // --- GESTIÓN DE CONEXIÓN ---

    private void toggleConnection() {
        if (!bridge.isConnected()) {
            // --- CONECTAR ---
            if (bridge.connect()) {
                btnConnect.setText("Desconectar");
                btnStep.setDisable(true); // Al conectar asumimos que corre

                disassemblyArea.setText("✅ Conexión establecida.\n\n" +
                        "El emulador está en ejecución.\n" +
                        "Pulsa PAUSAR (F8) para detener la CPU y ver el código.");
            }
        } else {
            // --- DESCONECTAR (Salida Limpia) ---
            stopPolling();
            btnStep.setDisable(true);

            // 1. Mandamos 'run' para liberar al emulador (SIN ESPERAR / Fire & Forget)
            // Esto asegura que el emulador no se quede 'tonto' esperando.
            bridge.sendCommandNoWait("run");

            // 2. Esperamos un instante (200ms) para que el mensaje salga y cortamos
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override public void run() {
                    bridge.disconnect();

                    Platform.runLater(() -> {
                        btnConnect.setText("Conectar");
                        disassemblyArea.setText("Desconectado.\nDale a Conectar para empezar.");
                        System.out.println("[DEBUG] Desconexión limpia completada.");
                    });
                }
            }, 200);
        }
    }

    // Este es el método que te daba error: Asegúrate de tenerlo aquí
    private void stopPolling() {
        if (poller != null) {
            poller.shutdownNow();
            poller = null;
        }
        isPollingActive = false;
    }

    // --- COMUNICACIÓN Y PARSEO ---

    private void refreshState() {
        if (!bridge.isConnected()) return;

        bridge.sendCommand("get-registers")
                .thenCompose(regsResponse -> {
                    String pc = extractValue(regsResponse, "PC");
                    this.currentPC = pc;
                    this.currentSP = extractValue(regsResponse, "SP");

                    Platform.runLater(() -> parseRegisters(regsResponse));

                    String disasmCmd = "disassemble";
                    return bridge.sendCommand(disasmCmd);
                })
                .thenCompose(disasmResponse -> {
                    Platform.runLater(() -> updateDisassembly(disasmResponse));
                    String sp = (currentSP != null && !currentSP.isEmpty()) ? currentSP : "0000";
                    return bridge.sendCommand("hexdump " + sp + " 16");
                })
                .thenAccept(stackDump -> {
                    Platform.runLater(() -> parseStack(stackDump));
                });
    }

    private String extractValue(String raw, String key) {
        if (raw == null || raw.contains("Error")) return "";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(key + "=([0-9A-Fa-f]+)");
        java.util.regex.Matcher m = p.matcher(raw);
        if (m.find()) return m.group(1);
        return "";
    }

    private void parseRegisters(String raw) {
        if (raw == null || raw.isEmpty() || raw.startsWith("Error")) return;

        String[] parts = raw.split("\\s+");
        for (String part : parts) {
            String[] kv = part.split("=");
            if (kv.length == 2) {
                String key = kv[0];
                String val = kv[1];
                switch (key) {
                    case "AF": regAF.setText("AF: " + val); break;
                    case "BC": regBC.setText("BC: " + val); break;
                    case "DE": regDE.setText("DE: " + val); break;
                    case "HL": regHL.setText("HL: " + val); break;
                    case "PC":
                        regPC.setText("PC: " + val);
                        this.currentPC = val;
                        break;
                    case "SP":
                        regSP.setText("SP: " + val);
                        this.currentSP = val;
                        break;
                    case "IX": regIX.setText("IX: " + val); break;
                    case "IY": regIY.setText("IY: " + val); break;
                }
            }
        }
    }

    private void updateDisassembly(String text) {
        if (text == null || text.startsWith("Error")) return;

        String cleanText = text.replace("command@cpu-step>", "")
                .replace("command@running>", "")
                .trim();

        disassemblyArea.setText(cleanText);

        if (currentPC == null || currentPC.isEmpty()) return;

        System.out.println("[DEBUG UI] Buscando PC: " + currentPC);

        String[] lines = cleanText.split("\n");
        int charIndex = 0;
        boolean found = false;

        for (String line : lines) {
            int lineLength = line.length() + 1;
            String trimmed = line.trim();
            String pcUpper = currentPC.toUpperCase();

            if (trimmed.toUpperCase().startsWith(pcUpper)) {
                boolean exactMatch = false;
                if (trimmed.length() == pcUpper.length()) {
                    exactMatch = true;
                }
                else if (trimmed.length() > pcUpper.length()) {
                    char nextChar = trimmed.charAt(pcUpper.length());
                    if (Character.isWhitespace(nextChar)) exactMatch = true;
                }

                if (exactMatch) {
                    System.out.println("[DEBUG UI] ¡ENCONTRADO! en línea: " + line);
                    int start = disassemblyArea.getText().indexOf(line, charIndex);
                    if (start == -1) start = charIndex;

                    disassemblyArea.selectRange(start, start + line.length());
                    found = true;
                    break;
                }
            }
            charIndex += lineLength;
        }

        if (!found) disassemblyArea.deselect();
    }

    private void parseStack(String rawDump) {
        if (rawDump == null || rawDump.startsWith("Error")) return;
        stackList.getItems().clear();
        String[] lines = rawDump.split("\n");
        for (String line : lines) {
            if (!line.isBlank()) stackList.getItems().add(line.trim());
        }
    }

    // Helper para enviar comandos (devuelve Future)
    private CompletableFuture<Void> sendCommand(String cmd) {
        return bridge.sendCommand(cmd).thenAccept(res -> {
            System.out.println("[DEBUG CMD] " + cmd + " -> " + res);
            if (res.startsWith("Error")) {
                Platform.runLater(() -> System.err.println("Error ZEsarUX: " + res));
            }
        });
    }
}