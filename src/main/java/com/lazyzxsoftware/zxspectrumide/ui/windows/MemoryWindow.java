package com.lazyzxsoftware.zxspectrumide.ui.windows;

import com.lazyzxsoftware.zxspectrumide.emulator.interfaces.SpectrumEmulator;
import com.lazyzxsoftware.zxspectrumide.managers.WindowManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class MemoryWindow extends BorderPane {

    private TextArea hexArea;
    private TextField addressField;
    private final int TOTAL_MEMORY = 65536; // 64KB completos
    private int targetScrollAddress = -1; // Para saltar a una dirección tras cargar

    public MemoryWindow() {
        initUI();
        // Cargar toda la memoria al abrir (ahora es síncrono)
        refreshMemory();
    }

    // Método público para forzar actualización desde fuera
    public void update() {
        refreshMemory();
    }

    private void initUI() {
        // --- BARRA SUPERIOR ---
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(5));
        topBar.setStyle("-fx-background-color: #3c3f41;");

        Label lblAddr = new Label("Ir a Dirección (Hex):");
        lblAddr.setStyle("-fx-text-fill: white;");

        // Campo de texto para dirección (ej: 4000)
        addressField = new TextField("0000");
        addressField.setPrefWidth(80);
        addressField.setOnAction(e -> jumpToAddress());

        Button btnGo = new Button("Ir");
        btnGo.setOnAction(e -> jumpToAddress());

        Button btnRefresh = new Button("↻ Recargar");
        btnRefresh.setStyle("-fx-font-weight: bold;");
        btnRefresh.setTooltip(new Tooltip("Traer los 64KB de memoria actualizados"));
        btnRefresh.setOnAction(e -> refreshMemory());

        topBar.getChildren().addAll(lblAddr, addressField, btnGo, new Separator(), btnRefresh);

        // --- ÁREA CENTRAL (Visor Hex) ---
        hexArea = new TextArea();
        hexArea.setEditable(false);
        // Fuente monoespaciada esencial para que alineen las columnas
        hexArea.setFont(Font.font("Monospaced", 14));
        // Estilo oscuro tipo "Matrix"
        hexArea.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #00ff00; -fx-font-family: 'Monospaced';");

        setTop(topBar);
        setCenter(hexArea);
    }

    /**
     * Lee la memoria directamente del emulador nativo y actualiza el visor.
     */
    private void refreshMemory() {
        SpectrumEmulator emulator = WindowManager.getInstance().getEmulator();

        if (emulator != null) {
            // Guardamos la posición del scroll para intentar restaurarla
            double oldScroll = hexArea.getScrollTop();
            int currentCaret = hexArea.getCaretPosition();

            // 1. Lectura Síncrona (Instantánea)
            byte[] data = new byte[TOTAL_MEMORY];
            for (int i = 0; i < TOTAL_MEMORY; i++) {
                data[i] = (byte) emulator.peek(i);
            }

            // 2. Actualizar UI
            Platform.runLater(() -> {
                updateMemoryText(0, data);

                // Restaurar scroll si no hay salto pendiente
                if (targetScrollAddress == -1) {
                    hexArea.setScrollTop(oldScroll);
                    if (currentCaret < hexArea.getText().length()) {
                        hexArea.positionCaret(currentCaret);
                    }
                }
            });

        } else {
            hexArea.setText("Emulador no conectado o no iniciado.");
        }
    }

    /**
     * Calcula la línea donde está la dirección y marca el salto.
     */
    private void jumpToAddress() {
        try {
            String text = addressField.getText().trim().replace("$", "").replace("0x", "");
            int addr = Integer.parseInt(text, 16);
            if (addr >= 0 && addr <= 65535) {
                targetScrollAddress = addr;
                refreshMemory();
            }
        } catch (NumberFormatException e) {
            // Ignorar si escriben basura
        }
    }

    /**
     * Mueve el cursor del TextArea a la línea correspondiente a la dirección.
     */
    private void scrollToAddress(int address) {
        if (hexArea.getText().isEmpty()) return;

        try {
            // Buscamos el string de la dirección al principio de la línea (ej: "4000:")
            int alignedAddr = (address / 16) * 16;
            String searchStr = String.format("%04X: ", alignedAddr);

            int index = hexArea.getText().indexOf(searchStr);
            if (index != -1) {
                // Posicionar el caret hace que el TextArea haga scroll automático hasta él
                hexArea.positionCaret(index);
                hexArea.selectPositionCaret(index);
                hexArea.deselect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Genera el texto hexadecimal gigante y lo pone en el TextArea.
     */
    private void updateMemoryText(int startAddr, byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 5);

        for (int i = 0; i < data.length; i += 16) {
            sb.append(String.format("%04X: ", startAddr + i));

            StringBuilder ascii = new StringBuilder();
            for (int j = 0; j < 16; j++) {
                if (i + j < data.length) {
                    byte b = data[i + j];
                    sb.append(String.format("%02X ", b));

                    char c = (char) (b & 0xFF);
                    if (c >= 32 && c <= 126) {
                        ascii.append(c);
                    } else {
                        ascii.append('.');
                    }
                } else {
                    sb.append("   ");
                }
            }
            sb.append(" | ").append(ascii).append("\n");
        }

        hexArea.setText(sb.toString());

        if (targetScrollAddress >= 0) {
            scrollToAddress(targetScrollAddress);
            targetScrollAddress = -1;
        }
    }
}