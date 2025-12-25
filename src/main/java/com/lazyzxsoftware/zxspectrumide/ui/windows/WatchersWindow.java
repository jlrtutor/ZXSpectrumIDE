package com.lazyzxsoftware.zxspectrumide.ui.windows;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

public class WatchersWindow extends BorderPane {

    public WatchersWindow() {
        initUI();
    }

    private void initUI() {
        setPadding(new Insets(10));

        Label lblPlaceholder = new Label("Variables Observadas\n(Aquí podrás añadir variables para ver su valor)");
        lblPlaceholder.setFont(Font.font("System", 14));
        lblPlaceholder.setStyle("-fx-text-fill: #888888;");

        setCenter(lblPlaceholder);

        // Aquí añadiremos una Tabla (TableView) en el futuro
    }
}