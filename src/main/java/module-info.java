module com.example.gestionwallet {

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

    opens com.example.gestionwallet.controllers to javafx.fxml;
    opens com.example.gestionwallet.models to javafx.base;

    exports com.example.gestionwallet;
    exports com.example.gestionwallet.controllers;
}
