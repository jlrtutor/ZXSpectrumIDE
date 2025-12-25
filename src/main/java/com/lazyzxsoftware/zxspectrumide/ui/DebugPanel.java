package com.lazyzxsoftware.zxspectrumide.ui;

import com.lazyzxsoftware.zxspectrumide.integration.ZesaruxBridge;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.CompletableFuture;

public class DebugPanel extends BorderPane {

    // Componentes UI
    private TextArea disassemblyArea;
    private ListView<String> stackList;

    // Etiquetas de Registros
    private Label regAF, regBC, regDE, regHL, regPC, regSP, regIX, regIY;
    private Label flagsLabel;

    // Estado interno
    private String currentPC = "";
    private String currentSP = "";

    // Lógica
    // (Mantenemos el bridge por si quieres reutilizar la lógica de lectura de registros más adelante,
    // aunque ahora no haya botones para accionarlo desde aquí)
    private final ZesaruxBridge bridge;
    private ScheduledExecutorService poller;
    private volatile boolean isPollingActive = false;

    public DebugPanel() {
        this.bridge = ZesaruxBridge.getInstance();
        initUI();
    }

    private void initUI() {
        // --- 1. SE HAN ELIMINADO LOS BOTONES DE CONTROL (ZESARUX) Y EL TOOLBAR ---
        // (Conectar, Pausar, Continuar, Paso, Ir a Inicio 'run 8000')

        // --- 2. PANELES (SPLIT) ---
        SplitPane mainSplit = new SplitPane();

        // Panel Izquierdo: Desensamblado
        disassemblyArea = new TextArea();
        disassemblyArea.setFont(Font.font("Monospaced", 12));
        disassemblyArea.setEditable(false);
        disassemblyArea.setText("Panel de Depuración\n(Controles manuales desactivados)");

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
        // Eliminados los atajos F5, F8, F10 que controlaban el puente ZEsarUX directamente.
        this.setFocusTraversable(true);
        this.setOnMouseClicked(e -> this.requestFocus());
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

    // --- MÉTODOS DE ACTUALIZACIÓN (Se mantienen por si se reutilizan externamente) ---

    private void stopPolling() {
        if (poller != null) {
            poller.shutdownNow();
            poller = null;
        }
        isPollingActive = false;
    }

    // Nota: Este método ya no es llamado por botones internos, pero podría ser útil
    // si decides actualizar la vista desde fuera.
    public void refreshState() {
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
            if (res.startsWith("Error")) {
                Platform.runLater(() -> System.err.println("Error ZEsarUX: " + res));
            }
        });
    }
}