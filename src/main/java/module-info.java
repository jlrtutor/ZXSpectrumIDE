module com.lazyzxsoftware.zxspectrumide {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // RichTextFX
    requires org.fxmisc.richtext;
    requires reactfx;
    requires org.fxmisc.flowless;
    requires org.fxmisc.undo;

    // ControlsFX
    requires org.controlsfx.controls;

    // Gson
    requires com.google.gson;

    // Exports
    exports com.lazyzxsoftware.zxspectrumide;

    // Opens para Gson (acceso reflectivo)
    opens com.lazyzxsoftware.zxspectrumide.config to com.google.gson;
    opens com.lazyzxsoftware.zxspectrumide to javafx.fxml;

    // Opens para i18n (ResourceBundle)
    opens com.lazyzxsoftware.zxspectrumide.i18n;
}