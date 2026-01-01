package com.lazyzxsoftware.zxspectrumide.ui.windows;

import com.lazyzxsoftware.zxspectrumide.emulator.interfaces.SpectrumEmulator;
import com.lazyzxsoftware.zxspectrumide.managers.BreakpointManager;
import com.lazyzxsoftware.zxspectrumide.managers.WindowManager;
import com.lazyzxsoftware.zxspectrumide.utils.Z80Disassembler;
import com.lazyzxsoftware.zxspectrumide.utils.Z80Disassembler.Instruction;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

public class MainDebuggerWindow extends BorderPane {

    private final ListView<Instruction> listView;
    private final CheckBox chkFollowPC;
    private int currentPC = -1;

    public MainDebuggerWindow() {
        setPadding(new Insets(10));

        // --- TOOLBAR ---
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button btnResume = new Button("▶ Continuar");
        btnResume.setStyle("-fx-base: #2E7D32; -fx-text-fill: white;");
        btnResume.setOnAction(e -> {
            SpectrumEmulator emu = WindowManager.getInstance().getEmulator();
            if (emu != null) emu.start();
        });

        Button btnPause = new Button("⏸ Pausar");
        btnPause.setStyle("-fx-base: #C62828; -fx-text-fill: white;");
        btnPause.setOnAction(e -> {
            SpectrumEmulator emu = WindowManager.getInstance().getEmulator();
            if (emu != null) {
                emu.pause();
                refreshAll();
            }
        });

        Button btnStep = new Button("⏯ Paso");
        btnStep.setStyle("-fx-base: #1565C0; -fx-text-fill: white;");
        btnStep.setOnAction(e -> {
            SpectrumEmulator emu = WindowManager.getInstance().getEmulator();
            if (emu != null) {
                emu.step();
                refreshAll();
            }
        });

        chkFollowPC = new CheckBox("Seguir PC");
        chkFollowPC.setSelected(true);
        chkFollowPC.setStyle("-fx-text-fill: #D4D4D4;");

        toolbar.getChildren().addAll(btnResume, btnPause, btnStep, new Separator(javafx.geometry.Orientation.VERTICAL), chkFollowPC);
        setTop(toolbar);

        // --- LISTA ---
        listView = new ListView<>();
        listView.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 13px;");
        setCenter(listView);

        listView.setCellFactory(lv -> new ListCell<>() {
            private final Circle breakpointNode = new Circle(4, Color.RED);
            private final Circle emptyNode = new Circle(4, Color.TRANSPARENT);
            private final HBox graphicContainer = new HBox();

            {
                graphicContainer.setAlignment(Pos.CENTER);
                graphicContainer.setPrefWidth(25);
                graphicContainer.setMinWidth(25);
                graphicContainer.setStyle("-fx-background-color: #333333;");
                graphicContainer.setCursor(Cursor.HAND);

                graphicContainer.setOnMouseClicked(e -> toggleBp());
                this.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2) toggleBp();
                });
            }

            private void toggleBp() {
                if (getItem() != null) {
                    int addr = getItem().address;
                    // Solo llamamos al Manager. Él se encarga de actualizar el Emulador.
                    BreakpointManager.getInstance().toggleBreakpoint(addr);
                    listView.refresh();
                }
            }

            @Override
            protected void updateItem(Instruction item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    boolean isBp = BreakpointManager.getInstance().isBreakpoint(item.address);
                    graphicContainer.getChildren().setAll(isBp ? breakpointNode : emptyNode);
                    setGraphic(graphicContainer);

                    if (item.address == currentPC) {
                        setStyle("-fx-background-color: #2d5a88; -fx-text-fill: white;");
                    } else {
                        setStyle("-fx-text-fill: #cccccc; -fx-background-color: transparent;");
                    }
                }
            }
        });
    }

    public void refreshAll() {
        SpectrumEmulator emu = WindowManager.getInstance().getEmulator();
        if (emu == null) return;

        this.currentPC = emu.getRegister("PC");
        byte[] memoryDump = new byte[65536];
        // Lectura rápida de memoria
        for (int i = 0; i < 65536; i++) {
            memoryDump[i] = (byte) emu.peek(i);
        }
        updateDisassembly(currentPC, memoryDump);
    }

    private void updateDisassembly(int pc, byte[] memory) {
        List<Instruction> fullList = Z80Disassembler.disassembleMemory(memory, 0, 0x8000);
        List<Instruction> highMem = Z80Disassembler.disassembleMemory(memory, 0x8000, 0x8000);
        fullList.addAll(highMem);

        listView.setItems(FXCollections.observableArrayList(fullList));

        if (chkFollowPC.isSelected()) {
            scrollToPC(fullList, pc);
        }
        listView.refresh();
    }

    private void scrollToPC(List<Instruction> instructions, int pc) {
        int pcIndex = -1;
        for (int i = 0; i < instructions.size(); i++) {
            if (instructions.get(i).address == pc) {
                pcIndex = i;
                break;
            }
        }
        if (pcIndex != -1) {
            listView.getSelectionModel().select(pcIndex);
            int targetIndex = Math.max(0, pcIndex - 12);
            Platform.runLater(() -> listView.scrollTo(targetIndex));
        }
    }

    // Método auxiliar para refrescar solo la lista visualmente
    public void refreshList() {
        listView.refresh();
    }

    // Método auxiliar para actualizar PC y scroll
    public void setPC(int pc) {
        this.currentPC = pc;
        listView.refresh();
        if (chkFollowPC.isSelected()) {
            scrollToPC(listView.getItems(), pc);
        }
    }
}