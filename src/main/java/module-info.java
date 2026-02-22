module com.example.finora_user {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires twilio;


    // allow FXMLLoader to create controllers via reflection
    opens com.example.finora_user.controllers to javafx.fxml;

    // allow TableView PropertyValueFactory to read entity properties
    opens com.example.finora_user.entities to javafx.base;

    exports com.example.finora_user;
}
