package com.lazyzxsoftware.zxspectrumide.ui.windows;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

public class RegistersWindow extends BorderPane {

    // Registros Principales
    public Label lblAF, lblBC, lblDE, lblHL;
    // Registros Alternativos (Shadow)
    public Label lblAF_Alt, lblBC_Alt, lblDE_Alt, lblHL_Alt;
    // Índices y Control
    public Label lblIX, lblIY, lblSP, lblPC;
    public Label lblI, lblR, lblIM;
    public Label lblFlags;

    public RegistersWindow() {
        initUI();
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

        // Estilo 'display' (verde fósforo o blanco brillante)
        String labelStyle = "-fx-text-fill: #00ff00; -fx-font-family: 'Monospaced'; -fx-font-weight: bold; -fx-font-size: 13px;";
        String shadowStyle = "-fx-text-fill: #008800; -fx-font-family: 'Monospaced'; -fx-font-size: 13px;"; // Un poco más oscuro para los shadow

        // --- Inicialización de Etiquetas ---

        // Principales
        lblAF = createLabel("AF: 0000", labelStyle);
        lblBC = createLabel("BC: 0000", labelStyle);
        lblDE = createLabel("DE: 0000", labelStyle);
        lblHL = createLabel("HL: 0000", labelStyle);

        // Alternativos (Shadow)
        lblAF_Alt = createLabel("AF': 0000", shadowStyle);
        lblBC_Alt = createLabel("BC': 0000", shadowStyle);
        lblDE_Alt = createLabel("DE': 0000", shadowStyle);
        lblHL_Alt = createLabel("HL': 0000", shadowStyle);

        // Índices
        lblIX = createLabel("IX: 0000", labelStyle);
        lblIY = createLabel("IY: 0000", labelStyle);
        lblSP = createLabel("SP: 0000", labelStyle);
        lblPC = createLabel("PC: 0000", labelStyle);

        lblFlags = new Label("Flags: S Z 5 H 3 P N C");
        lblFlags.setStyle("-fx-text-fill: #aaa; -fx-font-family: 'Monospaced'; -fx-font-size: 11px;");

        // --- Construcción de la Rejilla ---
        // Columna 0: Principales | Columna 1: Alternativos

        grid.addRow(0, lblAF, lblAF_Alt);
        grid.addRow(1, lblBC, lblBC_Alt);
        grid.addRow(2, lblDE, lblDE_Alt);
        grid.addRow(3, lblHL, lblHL_Alt);

        // Separador visual (podríamos poner un Separator aquí)

        grid.addRow(4, lblIX, lblSP);
        grid.addRow(5, lblIY, lblPC);

        grid.add(lblFlags, 0, 7, 2, 1);

        return grid;
    }

    private Label createLabel(String text, String style) {
        Label l = new Label(text);
        l.setStyle(style);
        return l;
    }

    // Método para actualizar todos los registros de golpe
    public void updateRegisters(int af, int bc, int de, int hl,
                                int af_, int bc_, int de_, int hl_,
                                int ix, int iy, int sp, int pc) {

        // Helper para formatear a Hexadecimal de 4 dígitos (ej: 3F4A)
        // Usamos Platform.runLater por seguridad si venimos de otro hilo
        javafx.application.Platform.runLater(() -> {
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

            // Actualizar Flags (Desglosamos F, que es la parte baja de AF)
            updateFlags(af & 0xFF);
        });
    }

    private void updateFlags(int f) {
        // S Z 5 H 3 P N C
        // Bit 76543210
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