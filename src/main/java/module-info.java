module com.example.imageprocessorfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.imageprocessorfx to javafx.fxml;
    exports com.example.imageprocessorfx;
}