module tn.finora.finoraformation {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.media;
    requires java.desktop;
    requires com.google.gson;




    requires itextpdf;

    opens com.example.finora.finoraformation to javafx.fxml;
    opens com.example.finora.controllers to javafx.fxml;




    exports com.example.finora.finoraformation;
    exports com.example.finora.controllers;
    exports com.example.finora.entities;
    exports com.example.finora.services;
    exports com.example.finora.utils;
}
