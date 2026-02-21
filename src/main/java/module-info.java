module tn.finora.finoraformation {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.media;
    requires java.desktop;
    requires com.google.gson;


    requires itextpdf;

    opens tn.finora.finoraformation to javafx.fxml;
    opens tn.finora.controllers to javafx.fxml;




    exports tn.finora.finoraformation;
    exports tn.finora.controllers;
    exports tn.finora.entities;
    exports tn.finora.services;
    exports tn.finora.utils;
}
