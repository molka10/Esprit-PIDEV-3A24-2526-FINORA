<<<<<<< HEAD
module tn.finora {

=======
module com.example.crud {
>>>>>>> 1887bb665258b7b8e6d48e72b39b0ddefb674861
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

<<<<<<< HEAD
    // Controllers FXML
    opens tn.finora.controllers to javafx.fxml;

    // Si HelloController est dans finorainves
    opens tn.finora.finorainves to javafx.fxml;

    // Entities pour TableView / PropertyValueFactory
    opens tn.finora.entities to javafx.base;

    //exports tn.finora;
    exports tn.finora.finorainves;
}
=======

    opens com.example.crud to javafx.fxml;
    exports com.example.crud;
    exports com.example.crud.utils;  // ← IMPORTANT : Ajouter cette ligne
    opens com.example.crud.utils to javafx.fxml;

    opens com.example.crud.controllers to javafx.fxml;



}
>>>>>>> 1887bb665258b7b8e6d48e72b39b0ddefb674861
