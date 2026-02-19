module com.example.crud {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.json;
    requires java.net.http;

    opens com.example.crud to javafx.fxml;
    exports com.example.crud;
    exports com.example.crud.utils;  // ← IMPORTANT : Ajouter cette ligne
    opens com.example.crud.utils to javafx.fxml;

    opens com.example.crud.controllers to javafx.fxml;



}