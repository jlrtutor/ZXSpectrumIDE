package com.lazyzxsoftware.zxspectrumide.ui;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutputConsole extends VBox {

    private static OutputConsole instance;
    private final StyleClassedTextArea logArea;       // Izquierda: Logs
    private final StyleClassedTextArea symbolsArea;   // Derecha: Símbolos

    private Consumer<Integer> onLineRequest;

    // Regex para detectar errores de Pasmo y números de línea
    private final Pattern PASMO_ERROR_PATTERN = Pattern.compile("(?i).*error:.*line\\s+(\\d+).*");
    private final Pattern EXTRACT_LINE_PATTERN = Pattern.compile("(?i)line\\s+(\\d+)");

    private OutputConsole() {
        // --- 1. ÁREA DE LOGS (Izquierda) ---
        logArea = new StyleClassedTextArea();
        logArea.setEditable(false);
        logArea.getStyleClass().add("output-console");

        configureLogArea();

        // --- 2. ÁREA DE SÍMBOLOS (Derecha) ---
        symbolsArea = new StyleClassedTextArea();
        symbolsArea.setEditable(false);
        symbolsArea.getStyleClass().add("output-console");

        // --- 3. SPLIT PANE ---
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(logArea, symbolsArea);
        splitPane.setDividerPositions(0.75); // 75% Log, 25% Símbolos por defecto

        VBox.setVgrow(splitPane, Priority.ALWAYS);
        this.getChildren().add(splitPane);
    }

    public static OutputConsole getInstance() {
        if (instance == null) {
            instance = new OutputConsole();
        }
        return instance;
    }

    private void configureLogArea() {
        // Cursor de mano sobre errores
        logArea.setOnMouseMoved(e -> {
            var hit = logArea.hit(e.getX(), e.getY());
            int charIndex = hit.getInsertionIndex();
            if (charIndex >= 0 && charIndex < logArea.getLength()) {
                StyleSpans<Collection<String>> styles = logArea.getStyleSpans(charIndex, charIndex + 1);
                boolean isLink = styles.styleStream().anyMatch(s -> s.contains("compilation-error"));
                logArea.setCursor(isLink ? Cursor.HAND : Cursor.TEXT);
            } else {
                logArea.setCursor(Cursor.DEFAULT);
            }
        });

        // Clic para saltar a línea
        logArea.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                try {
                    int index = logArea.getCurrentParagraph();
                    String text = logArea.getParagraph(index).getText();
                    checkForLineNumber(text);
                } catch (Exception ex) { /* Ignorar */ }
            }
        });
    }

    public void setOnLineRequest(Consumer<Integer> action) {
        this.onLineRequest = action;
    }

    private void checkForLineNumber(String lineText) {
        Matcher matcher = EXTRACT_LINE_PATTERN.matcher(lineText);
        if (matcher.find()) {
            try {
                int line = Integer.parseInt(matcher.group(1));
                if (onLineRequest != null) onLineRequest.accept(line);
            } catch (NumberFormatException e) { /* Ignorar */ }
        }
    }

    // --- MÉTODOS PÚBLICOS ---

    public void clear() {
        Platform.runLater(() -> {
            logArea.clear();
            symbolsArea.clear();
        });
    }

    // Muestra el contenido del archivo .symbols en el panel derecho
    public void showSymbols(String symbolsContent) {
        Platform.runLater(() -> {
            symbolsArea.clear();
            symbolsArea.append("--- SYMBOLS TABLE ---\n\n", "success-text");
            symbolsArea.append(symbolsContent, "normal-text");
            symbolsArea.moveTo(0);
        });
    }

    public void println(String text) {
        Matcher matcher = PASMO_ERROR_PATTERN.matcher(text);
        if (matcher.find()) {
            print(text + "\n", "compilation-error");
        } else {
            print(text + "\n", "normal-text");
        }
    }

    public void logSuccess(String message) {
        print("[INFO] " + message + "\n", "success-text");
    }

    public void logError(String message) {
        print("[ERROR] " + message + "\n", "error-text");
    }

    private void print(String text, String styleClass) {
        Platform.runLater(() -> {
            logArea.append(text, styleClass);
            logArea.requestFollowCaret();
            logArea.moveTo(logArea.getLength());
        });
    }
}