module tn.finora.finorainves {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires mysql.connector.j;

    opens tn.finora.finorainves to javafx.fxml;
    opens tn.finora.controllers to javafx.fxml;  // ← AJOUTE CETTE LIGNE

    exports tn.finora.finorainves;
}