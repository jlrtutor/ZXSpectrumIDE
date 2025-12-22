package com.lazyzxsoftware.zxspectrumide.utils;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Splash screen que se muestra al inicio de la aplicación
 */
public class SplashScreen {

    private final Stage splashStage;
    private final VBox root;
    private final Label statusLabel;

    public SplashScreen() {
        splashStage = new Stage(StageStyle.UNDECORATED);

        // Layout principal
        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #1a1e2e, #0f111a);" +
                        "-fx-padding: 60;" +
                        "-fx-border-color: #80CBC4;" +
                        "-fx-border-width: 2;"
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
        Label version = new Label("v0.0.3-alpha");
        version.setFont(Font.font("System", FontWeight.LIGHT, 12));
        version.setStyle("-fx-text-fill: #697098;");

        // Copyright
        Label copyright = new Label("© 2025 Lazy ZX Software");
        copyright.setFont(Font.font("System", FontWeight.LIGHT, 10));
        copyright.setStyle("-fx-text-fill: #546E7A;");

        // Barra de progreso
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        progressBar.setStyle("-fx-accent: #80CBC4;");

        // Estado
        statusLabel = new Label("Iniciando...");
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        statusLabel.setStyle("-fx-text-fill: #B0BEC5;");

        // Añadir todo
        root.getChildren().addAll(title, subtitle, version, copyright, progressBar, statusLabel);

        // Crear escena
        Scene scene = new Scene(root, 500, 400);
        splashStage.setScene(scene);
        splashStage.centerOnScreen();
    }

    /**
     * Muestra el splash screen
     */
    public void show() {
        splashStage.show();
        splashStage.toFront();
    }

    /**
     * Actualiza el mensaje de estado
     */
    public void updateStatus(String status) {
        statusLabel.setText(status);
    }

    /**
     * Cierra el splash sin animación
     */
    public void close(Runnable onFinished) {
        splashStage.close();
        if (onFinished != null) {
            onFinished.run();
        }
    }

    /**
     * Obtiene el stage del splash
     */
    public Stage getStage() {
        return splashStage;
    }
}