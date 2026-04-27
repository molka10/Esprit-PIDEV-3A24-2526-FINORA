package com.example.finora.controllers.bourse;

import com.example.finora.utils.Navigator;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class UtilisateurStaticController {

    @FXML
    private void choisirAdmin(MouseEvent event) {
        naviguerVers("/bourse/dashboard-admin-view.fxml",
                "FINORA - Dashboard Admin",
                event);
    }

    @FXML
    private void choisirEntreprise(MouseEvent event) {
        naviguerVers("/bourse/dashboard-investisseur-view.fxml",
                "FINORA - Dashboard Entreprise",
                event);
    }

    @FXML
    private void choisirInvestisseur(MouseEvent event) {
        naviguerVers("/bourse/dashboard-investisseur-view.fxml",
                "FINORA - Dashboard Investisseur",
                event);
    }

    private void naviguerVers(String cheminFxml, String titreWindow, MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Navigator.goTo(stage, cheminFxml, titreWindow);
    }
}
