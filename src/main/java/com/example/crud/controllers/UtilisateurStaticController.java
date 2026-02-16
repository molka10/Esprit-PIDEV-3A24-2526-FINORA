package com.example.crud.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class UtilisateurStaticController {

    @FXML
    private void choisirAdmin(ActionEvent event) {
        naviguerVers("/com/example/crud/dashboard-admin-view.fxml",
                "FINORA - Dashboard Admin",
                event);
    }

    @FXML
    private void choisirEntreprise(ActionEvent event) {
        naviguerVers("/com/example/crud/dashboard-investisseur-view.fxml",
                "FINORA - Dashboard Entreprise",
                event);
    }

    @FXML
    private void choisirInvestisseur(ActionEvent event) {
        naviguerVers("/com/example/crud/dashboard-investisseur-view.fxml",
                "FINORA - Dashboard Entreprise",
                event);
    }
    private void naviguerVers(String cheminFxml, String titreWindow, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(cheminFxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(titreWindow);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible de charger la page : " + e.getMessage());
            alert.showAndWait();
        }
    }
}
