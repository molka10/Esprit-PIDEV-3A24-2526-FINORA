module tn.finora {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // Controllers FXML
    opens tn.finora.controllers to javafx.fxml;

    // Si HelloController est dans finorainves
    opens tn.finora.finorainves to javafx.fxml;

    // Entities pour TableView / PropertyValueFactory
    opens tn.finora.entities to javafx.base;

    //exports tn.finora;
    exports tn.finora.finorainves;
}
