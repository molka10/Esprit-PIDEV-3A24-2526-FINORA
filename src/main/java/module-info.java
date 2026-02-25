module com.example.finora {
    requires org.apache.pdfbox;
    // ===== Required JavaFX modules =====
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    // ===== Required for JDBC =====
    requires java.sql;

    requires java.net.http;     // AI calls (HTTP)
    requires jakarta.mail;      // Email

    // ===== Allow FXML to access controllers =====
    opens com.example.finora.controllers to javafx.fxml;

    // ===== Allow FXML to access main app package =====
    opens com.example.finora to javafx.fxml;

    // ===== Allow TableView / PropertyValueFactory to access entity fields =====
    opens com.example.finora.entities to javafx.base;

    // ===== Export packages =====
    exports com.example.finora;
    exports com.example.finora.controllers;
    exports com.example.finora.entities;
    exports com.example.finora.services;
    exports com.example.finora.utils;
}
