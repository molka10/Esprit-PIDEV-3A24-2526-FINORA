module com.example.finora.finorainves {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;
    requires org.apache.commons.collections4;
    requires org.apache.xmlbeans;
    requires java.sql;
    requires mysql.connector.j;
    requires java.net.http;
    requires jdk.httpserver;
    opens com.example.finora.finorainves to javafx.fxml;
    opens com.example.finora.controllers to javafx.fxml;  // ← AJOUTE CETTE LIGNE

    exports com.example.finora.finorainves;
}