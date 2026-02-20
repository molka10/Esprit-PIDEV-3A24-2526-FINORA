module tn.finora.finoraformation {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.media;
    requires java.desktop;

    // ✅ FXML needs access to controllers (reflection)
    opens tn.finora.controllers to javafx.fxml;

    // ✅ If your FXML uses classes in this package as controller too
    opens tn.finora.finoraformation to javafx.fxml;

    // exports (not mandatory for FXML, but fine)
    exports tn.finora.finoraformation;
    exports tn.finora.controllers;
    exports tn.finora.entities;
    exports tn.finora.services;
    exports tn.finora.utils;
}
