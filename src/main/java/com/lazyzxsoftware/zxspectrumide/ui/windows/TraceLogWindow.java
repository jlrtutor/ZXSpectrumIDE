package com.lazyzxsoftware.zxspectrumide.ui.windows;

import com.lazyzxsoftware.zxspectrumide.utils.Z80Disassembler;
import com.lazyzxsoftware.zxspectrumide.utils.Z80Disassembler.Instruction;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class TraceLogWindow extends BorderPane {

    private final TableView<TraceStep> table;
    private byte[] lastTraceData = null;

    public TraceLogWindow() {
        setPadding(new Insets(10));

        table = new TableView<>();
        table.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px; -fx-base: #2b2b2b; -fx-control-inner-background: #2b2b2b; -fx-background-color: #2b2b2b;");

        // --- DEFINICIÓN DE COLUMNAS ---
        TableColumn<TraceStep, String> colCount = createColumn("#", 50, "gray");
        colCount.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().countStr));

        TableColumn<TraceStep, String> colPC = createColumn("Addr", 60, "#4EC9B0");
        colPC.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().pcStr));

        TableColumn<TraceStep, String> colInstr = createColumn("Instruction", 140, "#CE9178");
        colInstr.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().instructionStr));
        colInstr.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #CE9178;");

        TableColumn<TraceStep, String> colA = createColumn("A", 40, "white");
        colA.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().aStr));

        TableColumn<TraceStep, String> colFlags = createColumn("S Z Y H X P N C", 160, "#DCDCAA");
        colFlags.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().flagsStr));

        TableColumn<TraceStep, String> colB = createColumn("B", 35, "#D4D4D4");
        colB.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().bStr));
        TableColumn<TraceStep, String> colC = createColumn("C", 35, "#D4D4D4");
        colC.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().cStr));
        TableColumn<TraceStep, String> colD = createColumn("D", 35, "#D4D4D4");
        colD.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().dStr));
        TableColumn<TraceStep, String> colE = createColumn("E", 35, "#D4D4D4");
        colE.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().eStr));
        TableColumn<TraceStep, String> colH = createColumn("H", 35, "#D4D4D4");
        colH.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().hStr));
        TableColumn<TraceStep, String> colL = createColumn("L", 35, "#D4D4D4");
        colL.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().lStr));

        TableColumn<TraceStep, String> colIX = createColumn("IX", 60, "#9CDCFE");
        colIX.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().ixStr));
        TableColumn<TraceStep, String> colIY = createColumn("IY", 60, "#9CDCFE");
        colIY.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().iyStr));
        TableColumn<TraceStep, String> colSP = createColumn("SP", 60, "#C586C0");
        colSP.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().spStr));

        table.getColumns().addAll(colCount, colPC, colInstr, colA, colFlags, colB, colC, colD, colE, colH, colL, colIX, colIY, colSP);
        setCenter(table);
    }

    private TableColumn<TraceStep, String> createColumn(String title, double width, String color) {
        TableColumn<TraceStep, String> col = new TableColumn<>(title);
        col.setPrefWidth(width);
        col.setStyle("-fx-alignment: CENTER; -fx-text-fill: " + color + ";");
        return col;
    }

    public void updateTrace(byte[] traceData, byte[] memory) {
        if (traceData == null || traceData.length == 0) return;

        if (lastTraceData != null && java.util.Arrays.equals(traceData, lastTraceData)) {
            return;
        }
        lastTraceData = traceData;

        ObservableList<TraceStep> steps = FXCollections.observableArrayList();
        ByteBuffer buffer = ByteBuffer.wrap(traceData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        int totalSteps = traceData.length / 16;
        TraceStep lastStep = null;
        int stepCounter = 1;

        for (int i = 0; i < totalSteps; i++) {
            int pc = Short.toUnsignedInt(buffer.getShort());
            int sp = Short.toUnsignedInt(buffer.getShort());
            int af = Short.toUnsignedInt(buffer.getShort());
            int bc = Short.toUnsignedInt(buffer.getShort());
            int de = Short.toUnsignedInt(buffer.getShort());
            int hl = Short.toUnsignedInt(buffer.getShort());
            int ix = Short.toUnsignedInt(buffer.getShort());
            int iy = Short.toUnsignedInt(buffer.getShort());

            if (pc == 0 && sp == 0 && af == 0) continue;

            if (lastStep != null && lastStep.isSameState(pc, af, bc, de, hl, sp, ix, iy)) {
                lastStep.incrementCount();
            } else {
                String instructionText = "???";
                if (memory != null && pc < memory.length) {
                    try {
                        // CORRECCIÓN 1: Usar 'disassembleMemory' en lugar de 'disassemble'
                        // Pedimos solo 1 instrucción
                        List<Instruction> instructions = Z80Disassembler.disassembleMemory(memory, pc, 1);

                        if (!instructions.isEmpty()) {
                            Instruction instr = instructions.get(0);
                            // CORRECCIÓN 2: Usar 'instr.opcode' que ahora contiene todo el texto (mnemónico + args)
                            instructionText = instr.opcode;
                        }
                    } catch (Exception e) {
                        instructionText = "ERR";
                    }
                }

                TraceStep newStep = new TraceStep(stepCounter++, pc, af, bc, de, hl, ix, iy, sp, instructionText);
                steps.add(newStep);
                lastStep = newStep;
            }
        }

        table.setItems(steps);
        table.scrollTo(steps.size() - 1);
    }

    public static class TraceStep {
        int rawPC, rawAF, rawBC, rawDE, rawHL, rawSP, rawIX, rawIY;
        int repeatCount = 1;
        String countStr, pcStr, instructionStr, aStr, flagsStr;
        String bStr, cStr, dStr, eStr, hStr, lStr, ixStr, iyStr, spStr;

        public TraceStep(int count, int pc, int af, int bc, int de, int hl, int ix, int iy, int sp, String mnemonic) {
            this.rawPC = pc; this.rawAF = af; this.rawBC = bc;
            this.rawDE = de; this.rawHL = hl; this.rawSP = sp;
            this.rawIX = ix; this.rawIY = iy;

            this.countStr = String.valueOf(count);
            this.pcStr = String.format("%04X", pc);
            this.instructionStr = mnemonic;

            int a = (af >> 8) & 0xFF;
            int f = af & 0xFF;
            this.aStr = String.format("%02X", a);

            this.flagsStr = formatFlags(f);

            this.bStr = String.format("%02X", (bc >> 8) & 0xFF);
            this.cStr = String.format("%02X", bc & 0xFF);
            this.dStr = String.format("%02X", (de >> 8) & 0xFF);
            this.eStr = String.format("%02X", de & 0xFF);
            this.hStr = String.format("%02X", (hl >> 8) & 0xFF);
            this.lStr = String.format("%02X", hl & 0xFF);
            this.ixStr = String.format("%04X", ix);
            this.iyStr = String.format("%04X", iy);
            this.spStr = String.format("%04X", sp);
        }

        private String formatFlags(int f) {
            char[] names = {'S', 'Z', 'Y', 'H', 'X', 'P', 'N', 'C'};
            StringBuilder sb = new StringBuilder();

            for (int i = 7; i >= 0; i--) {
                boolean bitSet = ((f >> i) & 1) == 1;
                sb.append(bitSet ? names[7-i] : "-");
                if (i > 0) sb.append(" ");
            }
            return sb.toString();
        }

        public boolean isSameState(int pc, int af, int bc, int de, int hl, int sp, int ix, int iy) {
            return rawPC == pc && rawAF == af && rawBC == bc && rawDE == de &&
                    rawHL == hl && rawSP == sp && rawIX == ix && rawIY == iy;
        }

        public void incrementCount() {
            this.repeatCount++;
            this.countStr = "(" + repeatCount + ")";
        }
    }
}