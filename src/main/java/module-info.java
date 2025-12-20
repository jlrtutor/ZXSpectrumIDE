module com.lazyzxsoftware.zxspectrumide {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens com.lazyzxsoftware.zxspectrumide to javafx.fxml;
    exports com.lazyzxsoftware.zxspectrumide;
}