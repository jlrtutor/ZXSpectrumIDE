package com.lazyzxsoftware.zxspectrumide.ui.windows;

import com.lazyzxsoftware.zxspectrumide.managers.WindowManager;
import com.lazyzxsoftware.zxspectrumide.utils.Z80Disassembler;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MainDebuggerWindow extends BorderPane {

    private TableView<Z80Disassembler.Instruction> table;
    private ToolBar toolBar;

    // Cache para no desensamblar a lo loco
    private byte[] lastMemorySnapshot = new byte[0];
    private ObservableList<Z80Disassembler.Instruction> instructionsCache = FXCollections.observableArrayList();
    private boolean autoScroll = true; // Checkbox para "Seguir PC"

    public MainDebuggerWindow() {
        initUI();
    }

    private void initUI() {
        // --- 1. BARRA DE HERRAMIENTAS ---
        toolBar = new ToolBar();
        Button btnResume = createButton("▶", "Continuar (F5)", "resume");
        Button btnPause = createButton("⏸", "Pausar (Break)", "pause");
        Button btnStepInto = createButton("⤵", "Paso a paso (F11)", "step-into");

        CheckBox chkFollow = new CheckBox("Seguir PC");
        chkFollow.setSelected(true);
        chkFollow.selectedProperty().addListener((obs, old, val) -> autoScroll = val);

        toolBar.getItems().addAll(btnResume, btnPause, new Separator(), btnStepInto, new Separator(), chkFollow);

        // --- 2. TABLA ---
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 13px;");

        // Columna Address
        TableColumn<Z80Disassembler.Instruction, String> colAddr = new TableColumn<>("Addr");
        colAddr.setCellValueFactory(data -> new SimpleStringProperty(String.format("%04X", data.getValue().address)));
        colAddr.setPrefWidth(60);
        colAddr.setMaxWidth(80);
        colAddr.setStyle("-fx-text-fill: #569cd6; -fx-alignment: CENTER-RIGHT;");

        // Columna Bytes
        TableColumn<Z80Disassembler.Instruction, String> colBytes = new TableColumn<>("Bytes");
        colBytes.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().bytesStr));
        colBytes.setPrefWidth(100);
        colBytes.setMaxWidth(120);
        colBytes.setStyle("-fx-text-fill: #808080;");

        // Columna Mnemónico
        TableColumn<Z80Disassembler.Instruction, String> colInstr = new TableColumn<>("Instruction");
        colInstr.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().mnemonic));
        colInstr.setStyle("-fx-text-fill: #d4d4d4; -fx-font-weight: bold;");

        table.getColumns().addAll(colAddr, colBytes, colInstr);

        // Asignamos la lista observable vacía inicial
        table.setItems(instructionsCache);

        // --- ROW FACTORY: Resaltar PC ---
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Z80Disassembler.Instruction item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    // Verificamos si esta fila es el PC actual
                    // Usamos el "userdata" de la tabla para pasar el PC actual de forma chapucera pero efectiva
                    // O mejor: comparamos con una variable de clase, pero la RowFactory se refresca asíncrona.
                    // Para simplificar: La selección nativa de la tabla hará de cursor.

                    if (isSelected()) {
                        setStyle("-fx-background-color: #264f78; -fx-text-background-color: white;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        setTop(toolBar);
        setCenter(table);
    }

    /**
     * Recibe los datos completos (64KB) y el PC.
     */
    public void updateDisassembly(int pc, byte[] memory) {
        if (memory == null || memory.length == 0) return;

        Platform.runLater(() -> {
            boolean memoryChanged = !Arrays.equals(memory, lastMemorySnapshot);

            // 1. Si la memoria cambió, desensamblamos TODO (0000-FFFF)
            // Esto asegura que podemos hacer scroll hacia atrás.
            if (memoryChanged) {
                // Guardamos snapshot
                lastMemorySnapshot = memory.clone(); // Clonar es importante

                // Desensamblar los 64KB completos (Puede tardar unos ms, pero es aceptable en PC moderno)
                // Nota: Z80Disassembler.disassemble devuelve una lista.
                // Pedimos desde 0 hasta 65536 bytes.
                // OJO: El método original pedía "count" (número de instrucciones).
                // Vamos a usar una versión "inteligente" o simplemente un bucle grande.

                // Para no bloquear, desensamblamos "suficiente" o todo.
                // Vamos a desensamblar todo el bloque.
                List<Z80Disassembler.Instruction> fullList =
                        Z80Disassembler.disassemble(memory, 0, 32000); // ~32k instrucciones max para 64k bytes

                instructionsCache.setAll(fullList);
            }

            // 2. Buscar la instrucción que coincide con el PC y seleccionarla
            if (autoScroll) {
                scrollToPC(pc);
            }
        });
    }

    private void scrollToPC(int pc) {
        // Búsqueda lineal rápida (la lista está ordenada por dirección)
        // Podríamos usar búsqueda binaria para optimizar más.
        Optional<Z80Disassembler.Instruction> target = instructionsCache.stream()
                .filter(i -> i.address == pc)
                .findFirst();

        if (target.isPresent()) {
            Z80Disassembler.Instruction instr = target.get();
            table.getSelectionModel().select(instr);
            table.scrollTo(instr);
        } else {
            // Si no encontramos la instrucción exacta (desalineación), buscamos la más cercana
            // Esto pasa si desensamblamos desde 0000 y el código en 8000 está desalineado por datos.
            // Para la versión 0.0.7 es aceptable.
            table.getSelectionModel().clearSelection();
        }
    }

    private Button createButton(String text, String tooltip, String id) {
        Button btn = new Button(text);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setOnAction(e -> {
            var emulator = WindowManager.getInstance().getEmulatorWebView();
            if (emulator != null) {
                switch (id) {
                    case "resume": emulator.resumeEmulator(); break;
                    case "pause": emulator.pauseEmulator(); break;
                    case "step-into": emulator.stepInto(); break;
                }
            }
        });
        return btn;
    }
}