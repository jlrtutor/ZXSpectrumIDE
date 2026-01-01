package com.lazyzxsoftware.zxspectrumide.ui.windows;

import com.lazyzxsoftware.zxspectrumide.emulator.interfaces.SpectrumEmulator;
import com.lazyzxsoftware.zxspectrumide.managers.WindowManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

public class RegistersWindow extends BorderPane { // <--- DEBE EXTENDER BORDERPANE

    // Registros Principales
    private Label lblAF, lblBC, lblDE, lblHL;
    // Registros Alternativos
    private Label lblAF_Alt, lblBC_Alt, lblDE_Alt, lblHL_Alt;
    // Índices y Control
    private Label lblIX, lblIY, lblSP, lblPC;
    // Flags
    private Label lblFlags;

    public RegistersWindow() {
        initUI();
        update(); // Actualizar datos al abrir
    }

    private void initUI() {
        setPadding(new Insets(10));
        setCenter(createGrid());
        setStyle("-fx-background-color: #2b2b2b;");
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(5);

        String labelStyle = "-fx-text-fill: #00ff00; -fx-font-family: 'Monospaced'; -fx-font-weight: bold; -fx-font-size: 13px;";
        String shadowStyle = "-fx-text-fill: #008800; -fx-font-family: 'Monospaced'; -fx-font-size: 13px;";

        lblAF = createLabel("AF: 0000", labelStyle);
        lblBC = createLabel("BC: 0000", labelStyle);
        lblDE = createLabel("DE: 0000", labelStyle);
        lblHL = createLabel("HL: 0000", labelStyle);

        lblAF_Alt = createLabel("AF': 0000", shadowStyle);
        lblBC_Alt = createLabel("BC': 0000", shadowStyle);
        lblDE_Alt = createLabel("DE': 0000", shadowStyle);
        lblHL_Alt = createLabel("HL': 0000", shadowStyle);

        lblIX = createLabel("IX: 0000", labelStyle);
        lblIY = createLabel("IY: 0000", labelStyle);
        lblSP = createLabel("SP: 0000", labelStyle);
        lblPC = createLabel("PC: 0000", labelStyle);

        lblFlags = new Label("Flags: S Z 5 H 3 P N C");
        lblFlags.setStyle("-fx-text-fill: #aaa; -fx-font-family: 'Monospaced'; -fx-font-size: 11px;");

        grid.addRow(0, lblAF, lblAF_Alt);
        grid.addRow(1, lblBC, lblBC_Alt);
        grid.addRow(2, lblDE, lblDE_Alt);
        grid.addRow(3, lblHL, lblHL_Alt);
        grid.add(new Label(" "), 0, 4);
        grid.addRow(5, lblIX, lblSP);
        grid.addRow(6, lblIY, lblPC);
        grid.add(lblFlags, 0, 8, 2, 1);

        return grid;
    }

    private Label createLabel(String text, String style) {
        Label l = new Label(text);
        l.setStyle(style);
        return l;
    }

    // --- MÉTODO PULL: Lee del emulador ---
    public void update() {
        SpectrumEmulator emu = WindowManager.getInstance().getEmulator();
        if (emu == null) return;

        // Leer valores directamente de la interfaz
        int af = emu.getRegister("AF");
        int bc = emu.getRegister("BC");
        int de = emu.getRegister("DE");
        int hl = emu.getRegister("HL");
        int ix = emu.getRegister("IX");
        int iy = emu.getRegister("IY");
        int sp = emu.getRegister("SP");
        int pc = emu.getRegister("PC");

        // Valores Dummy para Shadow (hasta que los expongamos en la interfaz)
        int af_ = 0, bc_ = 0, de_ = 0, hl_ = 0;

        Platform.runLater(() -> {
            lblAF.setText(String.format("AF: %04X", af));
            lblBC.setText(String.format("BC: %04X", bc));
            lblDE.setText(String.format("DE: %04X", de));
            lblHL.setText(String.format("HL: %04X", hl));

            lblAF_Alt.setText(String.format("AF': %04X", af_));
            lblBC_Alt.setText(String.format("BC': %04X", bc_));
            lblDE_Alt.setText(String.format("DE': %04X", de_));
            lblHL_Alt.setText(String.format("HL': %04X", hl_));

            lblIX.setText(String.format("IX: %04X", ix));
            lblIY.setText(String.format("IY: %04X", iy));
            lblSP.setText(String.format("SP: %04X", sp));
            lblPC.setText(String.format("PC: %04X", pc));

            updateFlags(af & 0xFF);
        });
    }

    private void updateFlags(int f) {
        StringBuilder sb = new StringBuilder("Flags: ");
        sb.append((f & 0x80) != 0 ? "S " : "- ");
        sb.append((f & 0x40) != 0 ? "Z " : "- ");
        sb.append((f & 0x20) != 0 ? "5 " : "- ");
        sb.append((f & 0x10) != 0 ? "H " : "- ");
        sb.append((f & 0x08) != 0 ? "3 " : "- ");
        sb.append((f & 0x04) != 0 ? "P " : "- ");
        sb.append((f & 0x02) != 0 ? "N " : "- ");
        sb.append((f & 0x01) != 0 ? "C"  : "-");
        lblFlags.setText(sb.toString());
    }
}