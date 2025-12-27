package com.lazyzxsoftware.zxspectrumide.utils;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Ahora el Splash es un Panel (StackPane) que se superpone a la UI principal.
 * No es una ventana separada.
 */
public class SplashScreen extends StackPane {

    private final ProgressBar progressBar;
    private final Label statusLabel;

    public SplashScreen() {
        // Estilo de fondo oscuro y borde (igual que tenías)
        this.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1e2e, #0f111a);");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setMaxSize(500, 400); // Tamaño fijo visual para el "cuadro" del splash
        content.setStyle(
                "-fx-padding: 60px;" +
                        "-fx-border-color: #80CBC4;" +
                        "-fx-border-width: 2px;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-color: #1a1e2e;" // Fondo interior
        );

        // Logo/Título
        Label title = new Label("ZX SPECTRUM IDE");
        title.setFont(Font.font("System", FontWeight.BOLD, 36));
        title.setStyle("-fx-text-fill: #80CBC4;");

        Label subtitle = new Label("Z80 Assembly Development Environment");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitle.setStyle("-fx-text-fill: #B0BEC5;");

        Label version = new Label("v0.0.9");
        version.setFont(Font.font("System", FontWeight.LIGHT, 12));
        version.setStyle("-fx-text-fill: #697098;");

        Label copyright = new Label("© 2025 Lazy ZX Software");
        copyright.setFont(Font.font("System", FontWeight.LIGHT, 10));
        copyright.setStyle("-fx-text-fill: #546E7A;");

        // Barra de progreso
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        progressBar.setStyle("-fx-accent: #80CBC4; -fx-control-inner-background: #2b3040; -fx-text-box-border: transparent;");

        statusLabel = new Label("Iniciando...");
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        statusLabel.setStyle("-fx-text-fill: #B0BEC5;");

        content.getChildren().addAll(title, subtitle, version, copyright, progressBar, statusLabel);

        this.getChildren().add(content);
    }

    public void updateStatus(String status) {
        Platform.runLater(() -> statusLabel.setText(status));
    }

    public void updateProgress(double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress));
    }

    /**
     * Hace desaparecer el splash con una animación suave y ejecuta la acción final.
     */
    public void dismiss(Runnable onFinished) {
        FadeTransition fade = new FadeTransition(Duration.millis(500), this);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            setVisible(false);
            if (onFinished != null) onFinished.run();
        });
        fade.play();
    }
}