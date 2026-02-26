module com.example.finora {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires itextpdf;
    requires com.calendarfx.view;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires java.net.http;
    requires org.json;

    opens com.example.finora.controllers to javafx.fxml;
    opens com.example.finora.entities to javafx.base;

    exports com.example.finora;
    exports com.example.finora.controllers;
}
