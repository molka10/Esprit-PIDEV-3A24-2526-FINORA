module com.example.finora {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive java.sql;
    requires javafx.web;
    requires javafx.media;
    requires javafx.swing;
    requires java.desktop;
    requires java.net.http;
    requires com.google.gson;
    requires com.calendarfx.view;
    requires org.controlsfx.controls;
    requires jakarta.mail;
    requires twilio;
    requires jbcrypt;
    requires org.json;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires itextpdf;
    requires org.jooq.jool;
    requires jdk.jsobject;
    requires jdk.httpserver;
    requires org.apache.pdfbox;
    requires webcam.capture;

    exports com.example.finora;
    exports com.example.finora.controllers;
    exports com.example.finora.controllers.bourse;
    exports com.example.finora.controllers.investment;
    exports com.example.finora.controllers.appeldoffre;
    exports com.example.finora.entities;
    exports com.example.finora.services;
    exports com.example.finora.services.bourse;
    exports com.example.finora.services.appeldoffre;
    exports com.example.finora.utils;
    exports com.example.finora.finorainves;

    opens com.example.finora to javafx.fxml;
    opens com.example.finora.controllers to javafx.fxml;
    opens com.example.finora.controllers.bourse to javafx.fxml;
    opens com.example.finora.controllers.investment to javafx.fxml;
    opens com.example.finora.controllers.appeldoffre to javafx.fxml;
    opens com.example.finora.entities to javafx.fxml, com.google.gson;
    opens com.example.finora.services.bourse to com.google.gson, javafx.fxml;
    opens com.example.finora.services.appeldoffre to javafx.fxml;
    opens com.example.finora.utils to javafx.fxml;
    opens com.example.finora.finorainves to javafx.fxml;
}
