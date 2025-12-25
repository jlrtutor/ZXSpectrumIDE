package com.lazyzxsoftware.zxspectrumide.ui.windows;

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
        // Cargar toda la memoria al abrir
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

        Button btnRefresh = new Button("↻ Recargar Todo");
        btnRefresh.setStyle("-fx-font-weight: bold;");
        btnRefresh.setTooltip(new Tooltip("Traer los 64KB de memoria actualizados"));
        btnRefresh.setOnAction(e -> refreshMemory());

        // Eliminamos botones de paginación, ya no hacen falta
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
     * Pide al emulador que nos envíe TODO el bloque de memoria (0-65535).
     */
    private void refreshMemory() {
        var emulator = WindowManager.getInstance().getEmulatorWebView();
        if (emulator != null) {
            // Guardamos la posición actual del cursor/scroll para intentar restaurarla luego
            int currentCaret = hexArea.getCaretPosition();

            emulator.fetchMemory(0, TOTAL_MEMORY);

            // Si no hay salto pendiente, mantenemos el foco donde estaba
            if (targetScrollAddress == -1) {
                targetScrollAddress = -2; // Marca especial para "restaurar posición"
            }
        } else {
            hexArea.setText("Emulador no conectado o no iniciado.");
        }
    }

    /**
     * Calcula la línea donde está la dirección y mueve el scroll.
     */
    private void jumpToAddress() {
        try {
            String text = addressField.getText().trim().replace("$", "").replace("0x", "");
            int addr = Integer.parseInt(text, 16);
            if (addr >= 0 && addr <= 65535) {
                scrollToAddress(addr);
            }
        } catch (NumberFormatException e) {
            // Ignorar si escriben basura
        }
    }

    /**
     * Mueve el cursor del TextArea a la línea correspondiente a la dirección.
     */
    private void scrollToAddress(int address) {
        // Cada línea muestra 16 bytes.
        // Calculamos el índice de la línea (0 - 4095)
        int lineIndex = address / 16;

        // Estimación: Cada línea tiene unos 75 caracteres (depende del formato exacto)
        // FORMATO: "AAAA: 00 00 ... | ........\n" -> Aprox 74-75 chars
        // La forma más segura es usar las propiedades del TextArea si las tuviera,
        // pero selectRange o positionCaret funcionan bien.

        // Forzamos que se cargue primero si está vacío
        if (hexArea.getText().isEmpty()) {
            targetScrollAddress = address;
            refreshMemory();
            return;
        }

        // Navegación por fuerza bruta (pero efectiva en JavaFX)
        try {
            // Buscamos el string de la dirección al principio de la línea (ej: "4000:")
            // Ajustamos a múltiplo de 16 (ej: 4005 -> 4000)
            int alignedAddr = (address / 16) * 16;
            String searchStr = String.format("%04X:", alignedAddr);

            int index = hexArea.getText().indexOf(searchStr);
            if (index != -1) {
                hexArea.positionCaret(index);
                hexArea.selectPositionCaret(index); // Esto fuerza el scroll
                hexArea.deselect(); // Quitamos la selección azul fea
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Recibe los datos desde Javascript y rellena el TextArea.
     */
    public void updateMemory(int startAddr, byte[] data) {
        // Construimos un String gigante con los 64KB
        // StringBuilder es vital aquí para el rendimiento
        StringBuilder sb = new StringBuilder(data.length * 5); // Pre-reservar tamaño

        for (int i = 0; i < data.length; i += 16) {
            // 1. Dirección (Ej: "C000: ")
            sb.append(String.format("%04X: ", startAddr + i));

            // 2. Bytes Hexadecimales (16 bytes)
            StringBuilder ascii = new StringBuilder();
            for (int j = 0; j < 16; j++) {
                if (i + j < data.length) {
                    byte b = data[i + j];
                    sb.append(String.format("%02X ", b));

                    // Conversión ASCII segura (solo imprimibles)
                    // El Spectrum tiene su propio charset, pero esto ayuda a ver textos
                    char c = (char) (b & 0xFF);
                    if (c >= 32 && c <= 126) {
                        ascii.append(c);
                    } else {
                        ascii.append('.'); // Reemplazar control/gráficos por punto
                    }
                } else {
                    sb.append("   "); // Relleno si final de bloque
                }
            }

            // 3. Representación ASCII a la derecha
            sb.append(" | ").append(ascii).append("\n");
        }

        Platform.runLater(() -> {
            // Guardamos posición antigua si fuese necesario
            double oldScroll = hexArea.getScrollTop();

            hexArea.setText(sb.toString());

            // Gestión del scroll post-carga
            if (targetScrollAddress >= 0) {
                // Si había un "Ir a" pendiente
                scrollToAddress(targetScrollAddress);
                targetScrollAddress = -1;
            } else if (targetScrollAddress == -2) {
                // Restaurar posición anterior (Refresh)
                hexArea.setScrollTop(oldScroll);
                targetScrollAddress = -1;
            } else {
                // Por defecto: Ir al principio (0000)
                hexArea.positionCaret(0);
            }
        });
    }
}