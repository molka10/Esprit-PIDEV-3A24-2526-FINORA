module com.example.finora {

    /* ================= JAVA FX ================= */
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;
    requires javafx.media;

    /* ================= JAVA ================= */
    requires java.sql;
    requires java.net.http;
    requires java.desktop;
    requires java.datatransfer;

    /* ================= DATABASE ================= */
    requires mysql.connector.j;

    /* ================= PDF ================= */
    requires itextpdf;
    requires org.apache.pdfbox;

    /* ================= EXCEL ================= */
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.commons.collections4;
    requires org.apache.xmlbeans;

    /* ================= JSON ================= */
    requires org.json;
    requires com.google.gson;

    /* ================= SECURITY ================= */
    requires jbcrypt;

    /* ================= EMAIL ================= */
    requires jakarta.mail;

    /* ================= TWILIO ================= */
    requires com.twilio;

    /* ================= CALENDAR ================= */
    requires com.calendarfx.view;

    /* ================= OPEN PACKAGES ================= */

    opens com.example.finora to javafx.fxml;
    opens com.example.finora.controllers to javafx.fxml;
    opens com.example.finora.entities to javafx.base;
    opens com.example.finora.services to com.google.gson;

    opens com.example.crud to javafx.fxml;
    opens com.example.crud.controllers to javafx.fxml;
    opens com.example.crud.utils to javafx.fxml;
    opens com.example.crud.services to com.google.gson;

    opens com.example.finora.finorainves to javafx.fxml;

    opens tn.finora.finoraformation to javafx.fxml;
    opens tn.finora.controllers to javafx.fxml;

    /* ================= EXPORT ================= */

    exports com.example.finora;
    exports com.example.finora.controllers;
    exports com.example.finora.entities;
    exports com.example.finora.services;
    exports com.example.finora.utils;

    exports com.example.crud;
    exports com.example.crud.utils;

    exports com.example.finora.finorainves;

    exports tn.finora.finoraformation;
    exports tn.finora.controllers;
    exports tn.finora.entities;
    exports tn.finora.services;
    exports tn.finora.utils;
}
    requires com.google.gson;
    opens com.example.crud.services to com.google.gson;
    requires java.desktop;
   
}
