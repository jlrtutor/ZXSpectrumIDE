module com.lazyzxsoftware.zxspectrumide {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Si ya no usas WebView, podrías quitar estas dos líneas en el futuro,
    // pero déjalas por ahora para asegurar que compila si quedó algún rastro.
    requires javafx.web;
    requires jdk.jsobject;

    // RichTextFX (Editor de código)
    requires org.fxmisc.richtext;
    requires reactfx;
    requires org.fxmisc.flowless;
    requires org.fxmisc.undo;

    // ControlsFX
    requires org.controlsfx.controls;

    // --- NUEVO: Ikonli (Iconos) ---
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2; // Necesario para los iconos 'mdi2...'

    // Gson (JSON)
    requires com.google.gson;
    requires java.desktop;

    // Exports
    exports com.lazyzxsoftware.zxspectrumide;

    // Opens para Gson
    opens com.lazyzxsoftware.zxspectrumide.config to com.google.gson;

    // Opens para JavaFX FXML
    opens com.lazyzxsoftware.zxspectrumide to javafx.fxml;

    // Opens para i18n
    opens com.lazyzxsoftware.zxspectrumide.i18n;

    // NOTA: Hemos eliminado la línea 'opens ...ui.webview' porque borraste esa carpeta.
}