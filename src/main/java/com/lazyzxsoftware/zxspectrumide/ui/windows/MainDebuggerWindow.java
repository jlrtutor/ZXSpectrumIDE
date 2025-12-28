package com.lazyzxsoftware.zxspectrumide.ui.windows;

import com.lazyzxsoftware.zxspectrumide.managers.WindowManager;
import com.lazyzxsoftware.zxspectrumide.utils.Z80Disassembler;
import com.lazyzxsoftware.zxspectrumide.utils.Z80Disassembler.Instruction;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.List;

public class MainDebuggerWindow extends BorderPane {

    private final ListView<Instruction> listView;
    private final CheckBox chkFollowPC;
    private int currentPC = -1;

    public MainDebuggerWindow() {
        setPadding(new Insets(10));

        // --- TOOLBAR SUPERIOR (Botones + Checkbox) ---
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // 1. Botones de Control
        Button btnResume = new Button("▶ Continuar");
        btnResume.setStyle("-fx-base: #2E7D32; -fx-text-fill: white;"); // Verde
        btnResume.setOnAction(e -> WindowManager.getInstance().getEmulatorWebView().resumeEmulator());

        Button btnPause = new Button("⏸ Pausar");
        btnPause.setStyle("-fx-base: #C62828; -fx-text-fill: white;"); // Rojo
        btnPause.setOnAction(e -> WindowManager.getInstance().getEmulatorWebView().pauseEmulator());

        Button btnStep = new Button("⏯ Paso");
        btnStep.setStyle("-fx-base: #1565C0; -fx-text-fill: white;"); // Azul
        btnStep.setOnAction(e -> WindowManager.getInstance().getEmulatorWebView().stepInto());

        // Espaciador para empujar el checkbox a la derecha (opcional) o mantener todo junto
        Region spacer = new Region();
        // HBox.setHgrow(spacer, Priority.ALWAYS); // Descomentar si quieres el checkbox a la derecha del todo

        // 2. Checkbox "Seguir PC"
        chkFollowPC = new CheckBox("Seguir PC");
        chkFollowPC.setSelected(true);
        chkFollowPC.setStyle("-fx-text-fill: #D4D4D4;");

        // Añadir todo al toolbar
        toolbar.getChildren().addAll(btnResume, btnPause, btnStep, new Separator(javafx.geometry.Orientation.VERTICAL), chkFollowPC);
        setTop(toolbar);

        // --- LISTA CENTRAL ---
        listView = new ListView<>();
        listView.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 13px;");
        setCenter(listView);

        // Renderizado de celdas (Resaltado del PC)
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Instruction item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());

                    // Resaltado de la línea del PC
                    if (item.address == currentPC) {
                        // Azul estilo Visual Studio / IntelliJ para línea activa
                        setStyle("-fx-control-inner-background: #264F78; -fx-background-color: #264F78; -fx-text-fill: white; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #D4D4D4;"); // Gris claro para el resto
                    }
                }
            }
        });
    }

    public void updateDisassembly(int pc, int memStart, byte[] memory) {
        this.currentPC = pc;

        // 1. Desensamblar TODA la memoria (0-65535) para permitir scroll libre
        List<Instruction> instructions = Z80Disassembler.disassemble(memory, memStart, memory.length);

        listView.setItems(FXCollections.observableArrayList(instructions));

        // 2. Lógica de Centrado
        if (chkFollowPC.isSelected()) {
            scrollToPC(instructions, pc);
        }
    }

    private void scrollToPC(List<Instruction> instructions, int pc) {
        int pcIndex = -1;

        // Buscar la instrucción que coincide con el PC
        for (int i = 0; i < instructions.size(); i++) {
            if (instructions.get(i).address == pc) {
                pcIndex = i;
                break;
            }
        }

        if (pcIndex != -1) {
            listView.getSelectionModel().select(pcIndex);

            // CÁLCULO DE CENTRADO VERTICAL
            // Intentamos dejar el PC en medio de la vista
            int itemsInView = 24;
            int centerOffset = itemsInView / 2;
            final int targetIndex = Math.max(0, pcIndex - centerOffset);

            Platform.runLater(() -> {
                listView.scrollTo(targetIndex);
                listView.requestFocus();
            });
        }
    }
}