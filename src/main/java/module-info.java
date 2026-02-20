module com.example.gestionwallet {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires itextpdf;



    opens com.example.gestionwallet.controllers to javafx.fxml;
    opens com.example.gestionwallet.models to javafx.base;

    exports com.example.gestionwallet;
    exports com.example.gestionwallet.controllers;
}
