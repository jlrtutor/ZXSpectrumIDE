package com.lazyzxsoftware.zxspectrumide.utils;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreen {

    private final Stage splashStage;
    private final Label statusLabel;
    private final ProgressBar progressBar; // Útil tener referencia

    public SplashScreen() {
        splashStage = new Stage(StageStyle.UNDECORATED);

        // Layout principal
        VBox root = new VBox(20); // Usamos variable local, no hace falta campo de clase
        root.setAlignment(Pos.CENTER);

        // CORRECCIÓN CSS: Añadir 'px' al padding y asegurar sintaxis
        root.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #1a1e2e, #0f111a);" +
                        "-fx-padding: 60px;" +  // Importante: añadir unidades 'px'
                        "-fx-border-color: #80CBC4;" +
                        "-fx-border-width: 2px;"
        );

        // Logo/Título
        Label title = new Label("ZX SPECTRUM IDE");
        title.setFont(Font.font("System", FontWeight.BOLD, 36));
        title.setStyle("-fx-text-fill: #80CBC4;");

        // Subtítulo
        Label subtitle = new Label("Z80 Assembly Development Environment");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitle.setStyle("-fx-text-fill: #B0BEC5;");

        // Versión
        Label version = new Label("v0.0.4-alpha");
        version.setFont(Font.font("System", FontWeight.LIGHT, 12));
        version.setStyle("-fx-text-fill: #697098;");

        // Copyright
        Label copyright = new Label("© 2025 Lazy ZX Software");
        copyright.setFont(Font.font("System", FontWeight.LIGHT, 10));
        copyright.setStyle("-fx-text-fill: #546E7A;");

        // Barra de progreso
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        // Estilo CSS para que la barra se vea bien sobre fondo oscuro
        progressBar.setStyle("-fx-accent: #80CBC4; -fx-control-inner-background: #2b3040; -fx-text-box-border: transparent;");

        // Estado
        statusLabel = new Label("Iniciando...");
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        statusLabel.setStyle("-fx-text-fill: #B0BEC5;");

        // Añadir todo
        root.getChildren().addAll(title, subtitle, version, copyright, progressBar, statusLabel);

        // Crear escena
        Scene scene = new Scene(root, 500, 400);

        // SEGURIDAD: Poner el fondo de la escena del mismo color oscuro
        // Así, si el CSS del VBox falla, no se verá un recuadro blanco cegador.
        scene.setFill(Color.web("#1a1e2e"));

        splashStage.setScene(scene);
        splashStage.centerOnScreen();
    }

    public void show() {
        splashStage.show();
        // Forzar que se pinte inmediatamente (a veces ayuda en cargas rápidas)
        splashStage.toFront();
    }

    // Aseguramos que la actualización de UI ocurra siempre en el hilo correcto
    public void updateStatus(String status) {
        if (Platform.isFxApplicationThread()) {
            statusLabel.setText(status);
        } else {
            Platform.runLater(() -> statusLabel.setText(status));
        }
    }

    public void close(Runnable onFinished) {
        // Ejecutar el cierre en el hilo de FX
        Platform.runLater(() -> {
            splashStage.close();
            if (onFinished != null) {
                onFinished.run();
            }
        });
    }

    public void updateProgress(double progress) {
        // Aseguramos que el cambio visual se haga en el hilo de JavaFX
        if (Platform.isFxApplicationThread()) {
            progressBar.setProgress(progress);
        } else {
            Platform.runLater(() -> progressBar.setProgress(progress));
        }
    }
}