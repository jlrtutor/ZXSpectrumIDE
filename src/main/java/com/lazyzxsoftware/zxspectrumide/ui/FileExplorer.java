package com.lazyzxsoftware.zxspectrumide.ui;

import com.lazyzxsoftware.zxspectrumide.editor.FileManager;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.File;
import java.util.Arrays;

public class FileExplorer extends VBox {

    private final TreeView<File> treeView;
    private final FileManager fileManager;

    public FileExplorer(FileManager fileManager) {
        this.fileManager = fileManager;

        treeView = new TreeView<>();
        // AHORA SÍ mostramos la raíz (la carpeta del proyecto)
        treeView.setShowRoot(true);

        VBox.setVgrow(treeView, Priority.ALWAYS);
        treeView.setMaxHeight(Double.MAX_VALUE);

        // --- PERSONALIZACIÓN DE CELDAS ---
        treeView.setCellFactory(new Callback<>() {
            @Override
            public TreeCell<File> call(TreeView<File> param) {
                return new TreeCell<>() {
                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            String name = item.getName();
                            if (name == null || name.isEmpty()) name = item.getAbsolutePath();

                            String icon = item.isDirectory() ? "📁 " : "📄 ";
                            setText(icon + name);
                        }
                    }
                };
            }
        });

        treeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<File> selectedItem = treeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.getValue() != null && selectedItem.getValue().isFile()) {
                    fileManager.openFile(selectedItem.getValue());
                }
            }
        });

        this.getChildren().add(treeView);

        // NO cargamos nada al inicio (vacío)
    }

    /**
     * Establece la carpeta raíz del proyecto y restringe la vista a ella.
     */
    public void setProjectRoot(File projectDir) {
        if (projectDir == null || !projectDir.exists() || !projectDir.isDirectory()) return;

        // Creamos el nodo raíz con la carpeta del proyecto
        TreeItem<File> rootItem = createNode(projectDir);
        rootItem.setExpanded(true); // Lo expandimos para ver el contenido inmediatamente

        treeView.setRoot(rootItem);
    }

    private TreeItem<File> createNode(File file) {
        TreeItem<File> item = new TreeItem<>(file);

        if (file.isDirectory()) {
            item.getChildren().add(new TreeItem<>()); // Dummy para lazy loading

            item.expandedProperty().addListener((obs, wasExpanded, isExpanded) -> {
                if (isExpanded) {
                    boolean isFirstTime = false;
                    if (item.getChildren().size() == 1 && item.getChildren().get(0).getValue() == null) {
                        isFirstTime = true;
                    }

                    if (isFirstTime) {
                        populateNode(item, file);
                    }
                }
            });
        }
        return item;
    }

    private void populateNode(TreeItem<File> parentItem, File directory) {
        parentItem.getChildren().clear();
        File[] files = directory.listFiles();
        if (files != null) {
            Arrays.sort(files, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareToIgnoreCase(f2.getName());
            });

            for (File file : files) {
                if (file.isHidden() || file.getName().startsWith(".")) continue;
                // Excluir carpeta build si quieres
                if (file.getName().equals("build") || file.getName().equals("target")) continue;

                TreeItem<File> childNode = createNode(file);
                parentItem.getChildren().add(childNode);
            }
        }
    }
}