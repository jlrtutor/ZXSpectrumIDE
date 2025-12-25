package com.lazyzxsoftware.zxspectrumide.ui.windows;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class MainDebuggerWindow extends BorderPane {

    private TextArea disassemblyArea;
    private ToolBar toolBar;

    public MainDebuggerWindow() {
        initUI();
    }

    private void initUI() {
        // --- 1. BARRA DE HERRAMIENTAS (TOOLBAR) ---
        toolBar = new ToolBar();

        // Botones basados en Spectaculator
        Button btnResume = createButton("▶", "Continuar (F5)", "resume");
        Button btnPause = createButton("⏸", "Pausar (Break)", "pause");
        Button btnStepInto = createButton("⤵", "Paso a paso (F11)", "step-into");
        Button btnStepOver = createButton("↷", "Paso sobre (F10)", "step-over");
        Button btnStepOut = createButton("⬆", "Salir de rutina (Shift+F11)", "step-out");

        Separator sep1 = new Separator();
        Button btnRunToCursor = createButton("🏃", "Ejecutar hasta cursor (F9)", "run-cursor");

        toolBar.getItems().addAll(
                btnResume, btnPause, new Separator(),
                btnStepInto, btnStepOver, btnStepOut, new Separator(),
                btnRunToCursor
        );

        // --- 2. ÁREA DE DESENSAMBLADO ---
        disassemblyArea = new TextArea();
        disassemblyArea.setEditable(false);
        disassemblyArea.setFont(javafx.scene.text.Font.font("Monospaced", 13));
        disassemblyArea.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #d4d4d4;");
        disassemblyArea.setText("; Esperando conexión con el emulador...\n; (Aquí aparecerá el código desensamblado)");

        // Layout
        setTop(toolBar);
        setCenter(disassemblyArea);
    }

    private Button createButton(String text, String tooltip, String id) {
        Button btn = new Button(text);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setId(id);

        // Lógica real
        btn.setOnAction(e -> {
            // Obtenemos el emulador a través del gestor
            var emulator = com.lazyzxsoftware.zxspectrumide.managers.WindowManager.getInstance().getEmulatorWebView();

            if (emulator != null) {
                switch (id) {
                    case "resume":
                        emulator.resumeEmulator();
                        break;
                    case "pause":
                        emulator.pauseEmulator();
                        break;
                    case "step-into":
                        emulator.stepInto();
                        break;
                    // TODO: Implementar Step Over y Step Out más adelante (requieren lógica compleja de breakpoints)
                    case "run-cursor":
                        System.out.println("Run to cursor no implementado aún");
                        break;
                }
            } else {
                System.out.println("❌ Error: No hay conexión con el emulador");
            }
        });

        return btn;
    }
}