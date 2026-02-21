module com.example.project_pi {
    requires org.apache.pdfbox;
    // ===== Required JavaFX modules =====
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    // ===== Required for JDBC =====
    requires java.sql;

    // ===== Allow FXML to access controllers =====
    opens com.example.project_pi.controllers to javafx.fxml;

    // ===== Allow FXML to access main app package =====
    opens com.example.project_pi to javafx.fxml;

    // ===== Allow TableView / PropertyValueFactory to access entity fields =====
    opens com.example.project_pi.entities to javafx.base;

    // ===== Export packages =====
    exports com.example.project_pi;
    exports com.example.project_pi.controllers;
    exports com.example.project_pi.entities;
    exports com.example.project_pi.services;
    exports com.example.project_pi.utils;
}
