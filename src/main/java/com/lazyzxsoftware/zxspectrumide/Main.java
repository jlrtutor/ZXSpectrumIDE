package com.lazyzxsoftware.zxspectrumide;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Configuración de la ventana principal
        primaryStage.setTitle("ZX Spectrum IDE - v0.0.1");

        // Layout principal
        BorderPane root = new BorderPane();

        // Barra de menú (temporal)
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        // Área central (temporal)
        Label placeholder = new Label("ZX Spectrum IDE - Iniciando...");
        placeholder.setStyle("-fx-font-size: 24px; -fx-text-fill: gray;");
        root.setCenter(placeholder);

        // Crear la escena
        Scene scene = new Scene(root, 1200, 800);

        // Aplicar a la ventana
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Menú Archivo
        Menu menuFile = new Menu("Archivo");
        MenuItem newProject = new MenuItem("Nuevo Proyecto");
        MenuItem openProject = new MenuItem("Abrir Proyecto");
        MenuItem exit = new MenuItem("Salir");
        exit.setOnAction(e -> System.exit(0));
        menuFile.getItems().addAll(newProject, openProject, new SeparatorMenuItem(), exit);

        // Menú Editar
        Menu menuEdit = new Menu("Editar");
        MenuItem undo = new MenuItem("Deshacer");
        MenuItem redo = new MenuItem("Rehacer");
        menuEdit.getItems().addAll(undo, redo);

        // Menú Ver
        Menu menuView = new Menu("Ver");
        MenuItem theme = new MenuItem("Cambiar Tema");
        menuView.getItems().add(theme);

        // Menú Herramientas
        Menu menuTools = new Menu("Herramientas");
        MenuItem compile = new MenuItem("Compilar");
        MenuItem run = new MenuItem("Ejecutar");
        menuTools.getItems().addAll(compile, run);

        // Menú Configuración
        Menu menuSettings = new Menu("Configuración");
        MenuItem preferences = new MenuItem("Preferencias...");
        menuSettings.getItems().add(preferences);

        // Menú Ayuda
        Menu menuHelp = new Menu("Ayuda");
        MenuItem about = new MenuItem("Acerca de");
        menuHelp.getItems().add(about);

        menuBar.getMenus().addAll(menuFile, menuEdit, menuView, menuTools, menuSettings, menuHelp);

        return menuBar;
    }

    public static void main(String[] args) {
        launch(args);
    }
}