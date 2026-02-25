module com.example.finora {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires twilio;
    requires jakarta.mail;
    requires javafx.swing;
    requires java.desktop;      // for java.awt, image, geom
    requires java.datatransfer; // for AWT internals


    // allow FXMLLoader to create controllers via reflection
    opens com.example.finora.controllers to javafx.fxml;

    // allow TableView PropertyValueFactory to read entity properties
    opens com.example.finora.entities to javafx.base;

    exports com.example.finora;
}
