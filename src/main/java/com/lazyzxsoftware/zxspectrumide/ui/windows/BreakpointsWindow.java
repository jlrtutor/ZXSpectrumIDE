package com.lazyzxsoftware.zxspectrumide.ui.windows;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

public class BreakpointsWindow extends BorderPane {

    public BreakpointsWindow() {
        initUI();
    }

    private void initUI() {
        setPadding(new Insets(10));

        Label lblPlaceholder = new Label("Lista de Breakpoints\n(Aquí aparecerán los puntos de ruptura)");
        lblPlaceholder.setFont(Font.font("System", 14));
        lblPlaceholder.setStyle("-fx-text-fill: #888888;");

        setCenter(lblPlaceholder);
    }
}