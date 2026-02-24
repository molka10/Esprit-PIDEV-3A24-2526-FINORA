module tn.finora.finorainves {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;
    requires org.apache.commons.collections4;
    requires org.apache.xmlbeans;
    requires java.sql;
    requires mysql.connector.j;

    opens tn.finora.finorainves to javafx.fxml;
    opens tn.finora.controllers to javafx.fxml;  // ← AJOUTE CETTE LIGNE

    opens com.example.crud to javafx.graphics, javafx.fxml; // pour ton HelloApplication
    exports tn.finora.finorainves;
}