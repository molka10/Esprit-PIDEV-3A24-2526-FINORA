package com.example.crud.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class UtilisateurStaticController {

    @FXML
    private void choisirAdmin(MouseEvent event) {
        naviguerVers("/com/example/crud/dashboard-admin-view.fxml",
                "FINORA - Dashboard Admin",
                event);
    }

    @FXML
    private void choisirEntreprise(MouseEvent event) {
        naviguerVers("/com/example/crud/dashboard-investisseur-view.fxml",
                "FINORA - Dashboard Entreprise",
                event);
    }

    @FXML
    private void choisirInvestisseur(MouseEvent event) {
        naviguerVers("/com/example/crud/dashboard-investisseur-view.fxml",
                "FINORA - Dashboard Investisseur",
                event);
    }

    private void naviguerVers(String cheminFxml, String titreWindow, MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(cheminFxml));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setTitle(titreWindow);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger la page : " + e.getMessage());
            alert.showAndWait();
        }
    }
}
