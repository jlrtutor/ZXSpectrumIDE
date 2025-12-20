package com.lazyzxsoftware.zxspectrumide.config;

/**
 * Clase que representa toda la configuración de la aplicación
 */
public class AppConfig {

    // Apariencia
    private String theme = "light"; // "light" o "dark"
    private String language = "es"; // "es", "en", etc.
    private double uiFontSize = 13.0;

    // Editor
    private int tabSize = 4;
    private boolean useSpacesInsteadOfTabs = true;
    private boolean showLineNumbers = true;
    private boolean showTabLines = true;
    private boolean showWhitespace = false;
    private boolean highlightCurrentLine = true;
    private boolean showMinimap = false;
    private String editorFontFamily = "Consolas";
    private double editorFontSize = 14.0;
    private int lineWidthGuide = 80; // 0 = desactivado

    // Comportamiento del editor
    private boolean autoSave = true;
    private int autoSaveInterval = 30; // segundos
    private boolean autoIndent = true;
    private boolean autoCloseBrackets = true;
    private boolean autocompleteEnabled = true;

    // Compilador (PASMO)
    private String pasmoPath = "";
    private String outputFormat = "tap"; // tap, tzx, bin
    private String buildDirectory = "build";
    private boolean generateSymbols = true;
    private boolean generateListing = true;
    private String additionalArgs = "";

    // Emulador (ZEsarUX)
    private String zesaruxPath = "";
    private String spectrumModel = "48k"; // 48k, 128k, +2, +3
    private boolean fullscreenOnRun = false;
    private boolean debugMode = false;

    // Proyecto
    private String lastProjectPath = "";
    private boolean openLastProjectOnStartup = true;
    private boolean rememberOpenFiles = true;
    private int maxBackups = 5;

    // Getters y Setters

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public double getUiFontSize() {
        return uiFontSize;
    }

    public void setUiFontSize(double uiFontSize) {
        this.uiFontSize = uiFontSize;
    }

    public int getTabSize() {
        return tabSize;
    }

    public void setTabSize(int tabSize) {
        this.tabSize = tabSize;
    }

    public boolean isUseSpacesInsteadOfTabs() {
        return useSpacesInsteadOfTabs;
    }

    public void setUseSpacesInsteadOfTabs(boolean useSpacesInsteadOfTabs) {
        this.useSpacesInsteadOfTabs = useSpacesInsteadOfTabs;
    }

    public boolean isShowLineNumbers() {
        return showLineNumbers;
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        this.showLineNumbers = showLineNumbers;
    }

    public boolean isShowTabLines() {
        return showTabLines;
    }

    public void setShowTabLines(boolean showTabLines) {
        this.showTabLines = showTabLines;
    }

    public boolean isShowWhitespace() {
        return showWhitespace;
    }

    public void setShowWhitespace(boolean showWhitespace) {
        this.showWhitespace = showWhitespace;
    }

    public boolean isHighlightCurrentLine() {
        return highlightCurrentLine;
    }

    public void setHighlightCurrentLine(boolean highlightCurrentLine) {
        this.highlightCurrentLine = highlightCurrentLine;
    }

    public boolean isShowMinimap() {
        return showMinimap;
    }

    public void setShowMinimap(boolean showMinimap) {
        this.showMinimap = showMinimap;
    }

    public String getEditorFontFamily() {
        return editorFontFamily;
    }

    public void setEditorFontFamily(String editorFontFamily) {
        this.editorFontFamily = editorFontFamily;
    }

    public double getEditorFontSize() {
        return editorFontSize;
    }

    public void setEditorFontSize(double editorFontSize) {
        this.editorFontSize = editorFontSize;
    }

    public int getLineWidthGuide() {
        return lineWidthGuide;
    }

    public void setLineWidthGuide(int lineWidthGuide) {
        this.lineWidthGuide = lineWidthGuide;
    }

    public boolean isAutoSave() {
        return autoSave;
    }

    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    public int getAutoSaveInterval() {
        return autoSaveInterval;
    }

    public void setAutoSaveInterval(int autoSaveInterval) {
        this.autoSaveInterval = autoSaveInterval;
    }

    public boolean isAutoIndent() {
        return autoIndent;
    }

    public void setAutoIndent(boolean autoIndent) {
        this.autoIndent = autoIndent;
    }

    public boolean isAutoCloseBrackets() {
        return autoCloseBrackets;
    }

    public void setAutoCloseBrackets(boolean autoCloseBrackets) {
        this.autoCloseBrackets = autoCloseBrackets;
    }

    public boolean isAutocompleteEnabled() {
        return autocompleteEnabled;
    }

    public void setAutocompleteEnabled(boolean autocompleteEnabled) {
        this.autocompleteEnabled = autocompleteEnabled;
    }

    public String getPasmoPath() {
        return pasmoPath;
    }

    public void setPasmoPath(String pasmoPath) {
        this.pasmoPath = pasmoPath;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getBuildDirectory() {
        return buildDirectory;
    }

    public void setBuildDirectory(String buildDirectory) {
        this.buildDirectory = buildDirectory;
    }

    public boolean isGenerateSymbols() {
        return generateSymbols;
    }

    public void setGenerateSymbols(boolean generateSymbols) {
        this.generateSymbols = generateSymbols;
    }

    public boolean isGenerateListing() {
        return generateListing;
    }

    public void setGenerateListing(boolean generateListing) {
        this.generateListing = generateListing;
    }

    public String getAdditionalArgs() {
        return additionalArgs;
    }

    public void setAdditionalArgs(String additionalArgs) {
        this.additionalArgs = additionalArgs;
    }

    public String getZesaruxPath() {
        return zesaruxPath;
    }

    public void setZesaruxPath(String zesaruxPath) {
        this.zesaruxPath = zesaruxPath;
    }

    public String getSpectrumModel() {
        return spectrumModel;
    }

    public void setSpectrumModel(String spectrumModel) {
        this.spectrumModel = spectrumModel;
    }

    public boolean isFullscreenOnRun() {
        return fullscreenOnRun;
    }

    public void setFullscreenOnRun(boolean fullscreenOnRun) {
        this.fullscreenOnRun = fullscreenOnRun;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public String getLastProjectPath() {
        return lastProjectPath;
    }

    public void setLastProjectPath(String lastProjectPath) {
        this.lastProjectPath = lastProjectPath;
    }

    public boolean isOpenLastProjectOnStartup() {
        return openLastProjectOnStartup;
    }

    public void setOpenLastProjectOnStartup(boolean openLastProjectOnStartup) {
        this.openLastProjectOnStartup = openLastProjectOnStartup;
    }

    public boolean isRememberOpenFiles() {
        return rememberOpenFiles;
    }

    public void setRememberOpenFiles(boolean rememberOpenFiles) {
        this.rememberOpenFiles = rememberOpenFiles;
    }

    public int getMaxBackups() {
        return maxBackups;
    }

    public void setMaxBackups(int maxBackups) {
        this.maxBackups = maxBackups;
    }
}