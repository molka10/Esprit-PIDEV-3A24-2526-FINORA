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
        ouvrir(event, "/com/example/crud/bourse-view.fxml", "Gestion des Bourses");
    }

    @FXML
    private void choisirEntreprise(ActionEvent event) {
        ouvrir(event, "/com/example/crud/trading-view.fxml", "FINORA - Trading (Entreprise)");
    }

    @FXML
    private void choisirInvestisseur(ActionEvent event) {
        ouvrir(event, "/com/example/crud/trading-view.fxml", "FINORA - Trading (Investisseur)");
    }

    private void ouvrir(ActionEvent event, String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Erreur");
            a.setHeaderText("Erreur");
            a.setContentText("Erreur affichage : " + e.getMessage());
            a.showAndWait();
        }
    }
}
