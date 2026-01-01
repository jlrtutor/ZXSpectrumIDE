package com.lazyzxsoftware.zxspectrumide.ui.windows;

import com.lazyzxsoftware.zxspectrumide.emulator.impl.Spectrum48k;
import com.lazyzxsoftware.zxspectrumide.emulator.interfaces.SpectrumEmulator;
import com.lazyzxsoftware.zxspectrumide.emulator.ui.SpectrumRenderer;
import com.lazyzxsoftware.zxspectrumide.managers.WindowManager;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

public class EmulatorStage extends Stage {

    private final SpectrumEmulator emulator;
    private final SpectrumRenderer renderer;
    private ImageView screenView;
    private AnimationTimer displayLoop;

    public EmulatorStage() {
        this.setTitle("ZX Spectrum Emulator (Nativo)");
        this.emulator = new Spectrum48k();
        this.renderer = new SpectrumRenderer();

        this.emulator.setOnCpuStop(this::updateDebugWindows);

        initUI();
        initGameLoop();
        WindowManager.getInstance().setEmulator(emulator);

        this.setOnCloseRequest(e -> {
            emulator.stop();
            if (displayLoop != null) displayLoop.stop();
        });
    }

    private void initUI() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #202020;");

        // Imagen (Ahora incluye el borde de 320x240)
        screenView = new ImageView(renderer.getImage());
        screenView.setFitWidth(640);  // x2 (320 * 2)
        screenView.setFitHeight(480); // x2 (240 * 2)
        screenView.setPreserveRatio(true);
        screenView.setSmooth(false);

        StackPane screenContainer = new StackPane(screenView);
        // Borde fino gris decorativo del IDE (no del Spectrum)
        screenContainer.setStyle("-fx-border-color: #444; -fx-border-width: 1;");

        root.setCenter(screenContainer);

        ToolBar toolbar = new ToolBar();
        Button btnPlay = createButton("mdi2p-play", "Ejecutar / Continuar");
        btnPlay.setOnAction(e -> emulator.start());
        Button btnPause = createButton("mdi2p-pause", "Pausar");
        btnPause.setOnAction(e -> { emulator.pause(); updateDebugWindows(); });
        Button btnReset = createButton("mdi2r-restart", "Reiniciar");
        btnReset.setOnAction(e -> { emulator.reset(); updateDebugWindows(); });
        Button btnStepInto = createButton("mdi2d-debug-step-into", "Paso a paso");
        btnStepInto.setOnAction(e -> { emulator.step(); updateDebugWindows(); });
        Button btnStepOver = createButton("mdi2d-debug-step-over", "Saltar instrucción");
        btnStepOver.setOnAction(e -> { emulator.stepOver(); updateDebugWindows(); });

        toolbar.getItems().addAll(btnPlay, btnPause, btnReset, new Separator(), btnStepInto, btnStepOver);
        root.setTop(toolbar);

        Scene scene = new Scene(root, 660, 550);
        this.setScene(scene);
    }

    private Button createButton(String iconCode, String tooltip) {
        Button btn = new Button();
        btn.setGraphic(new FontIcon(iconCode));
        btn.setTooltip(new Tooltip(tooltip));
        return btn;
    }

    private void initGameLoop() {
        displayLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Ya no llamamos a emulator.executeFrame() aquí porque el hilo del emulador va por libre

                // Solo repintamos la pantalla si hay datos
                if (emulator.getScreenBuffer() != null) {
                    renderer.paintBuffer(emulator.getScreenBuffer());
                }
            }
        };
        displayLoop.start();
    }

    private void updateDebugWindows() {
        WindowManager.getInstance().refreshDebugger();
    }
}