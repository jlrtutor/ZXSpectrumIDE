package com.lazyzxsoftware.zxspectrumide.config;

/**
 * Clase que representa toda la configuración de la aplicación (POJO).
 * Centraliza: Editor, Compilador, Emulador y Preferencias Generales.
 */
public class AppConfig {

    // --- Apariencia ---
    private String theme = "light";
    private String language = "es";
    private double uiFontSize = 13.0;

    // --- Editor (Visual) ---
    private String editorFontFamily = "Roboto Mono";
    private double editorFontSize = 14.0;
    private boolean showLineNumbers = true;
    private boolean showWhitespace = false;
    private boolean highlightCurrentLine = true;
    private boolean showMinimap = false;

    // --- Editor (Comportamiento) ---
    private int tabSize = 4;
    private boolean useSpacesInsteadOfTabs = true;
    private boolean autoIndent = true;
    private boolean autoCloseBrackets = true;
    private boolean autocompleteEnabled = true;
    private boolean autoSave = true;
    private int autoSaveInterval = 30;

    // --- Configuración de PASMO (Compilador) ---
    private String pasmoPath = "";
    private String pasmoFormat = "tapbas"; // "tapbas", "tap", "bin", "hex"
    private boolean pasmoDebug = false;
    private String buildPath = ""; // Ruta de salida personalizada

    // --- Proyecto e Historial ---
    private String lastProjectPath = "";
    private boolean openLastProjectOnStartup = true;

    // Campo necesario para FileManager (recordar dónde abrir el FileChooser)
    private String lastDirectory = "";

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    // --- Apariencia ---
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public double getUiFontSize() { return uiFontSize; }
    public void setUiFontSize(double uiFontSize) { this.uiFontSize = uiFontSize; }

    // --- Editor Visual ---
    public String getEditorFontFamily() { return editorFontFamily; }
    public void setEditorFontFamily(String editorFontFamily) { this.editorFontFamily = editorFontFamily; }

    public double getEditorFontSize() { return editorFontSize; }
    public void setEditorFontSize(double editorFontSize) { this.editorFontSize = editorFontSize; }

    public boolean isShowLineNumbers() { return showLineNumbers; }
    public void setShowLineNumbers(boolean showLineNumbers) { this.showLineNumbers = showLineNumbers; }

    public boolean isShowWhitespace() { return showWhitespace; }
    public void setShowWhitespace(boolean showWhitespace) { this.showWhitespace = showWhitespace; }

    public boolean isHighlightCurrentLine() { return highlightCurrentLine; }
    public void setHighlightCurrentLine(boolean highlightCurrentLine) { this.highlightCurrentLine = highlightCurrentLine; }

    public boolean isShowMinimap() { return showMinimap; }
    public void setShowMinimap(boolean showMinimap) { this.showMinimap = showMinimap; }

    // --- Editor Comportamiento ---
    public int getTabSize() { return tabSize; }
    public void setTabSize(int tabSize) { this.tabSize = tabSize; }

    public boolean isUseSpacesInsteadOfTabs() { return useSpacesInsteadOfTabs; }
    public void setUseSpacesInsteadOfTabs(boolean useSpacesInsteadOfTabs) { this.useSpacesInsteadOfTabs = useSpacesInsteadOfTabs; }

    public boolean isAutoIndent() { return autoIndent; }
    public void setAutoIndent(boolean autoIndent) { this.autoIndent = autoIndent; }

    public boolean isAutoCloseBrackets() { return autoCloseBrackets; }
    public void setAutoCloseBrackets(boolean autoCloseBrackets) { this.autoCloseBrackets = autoCloseBrackets; }

    public boolean isAutocompleteEnabled() { return autocompleteEnabled; }
    public void setAutocompleteEnabled(boolean autocompleteEnabled) { this.autocompleteEnabled = autocompleteEnabled; }

    public boolean isAutoSave() { return autoSave; }
    public void setAutoSave(boolean autoSave) { this.autoSave = autoSave; }

    public int getAutoSaveInterval() { return autoSaveInterval; }
    public void setAutoSaveInterval(int autoSaveInterval) { this.autoSaveInterval = autoSaveInterval; }

    // --- PASMO ---
    public String getPasmoPath() { return pasmoPath; }
    public void setPasmoPath(String pasmoPath) { this.pasmoPath = pasmoPath; }

    public String getPasmoFormat() { return pasmoFormat; }
    public void setPasmoFormat(String pasmoFormat) { this.pasmoFormat = pasmoFormat; }

    public boolean isPasmoDebug() { return pasmoDebug; }
    public void setPasmoDebug(boolean pasmoDebug) { this.pasmoDebug = pasmoDebug; }

    public String getBuildPath() { return buildPath; }
    public void setBuildPath(String buildPath) { this.buildPath = buildPath; }

    // --- Proyecto ---
    public String getLastProjectPath() { return lastProjectPath; }
    public void setLastProjectPath(String lastProjectPath) { this.lastProjectPath = lastProjectPath; }

    public boolean isOpenLastProjectOnStartup() { return openLastProjectOnStartup; }
    public void setOpenLastProjectOnStartup(boolean openLastProjectOnStartup) { this.openLastProjectOnStartup = openLastProjectOnStartup; }

    // --- Historial de Directorios (Añadido) ---
    public String getLastDirectory() { return lastDirectory; }
    public void setLastDirectory(String lastDirectory) { this.lastDirectory = lastDirectory; }
}