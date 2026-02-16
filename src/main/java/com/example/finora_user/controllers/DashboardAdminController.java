package com.example.finora_user.controllers;

import com.example.finora_user.utils.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DashboardAdminController {

    @FXML private Label statusLabel;

    @FXML void openBourse() { statusLabel.setText("TODO: Page Bourse"); }
    @FXML void openPortefeuille() { statusLabel.setText("TODO: Page Portefeuille"); }
    @FXML void openAppelOffre() { statusLabel.setText("TODO: Page Appel d'offre"); }

    @FXML
    void openGestionUsers() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        Navigator.goTo(stage, "users-view.fxml", "Gestion Utilisateurs");
    }

    @FXML
    void logout() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        Navigator.goTo(stage, "login-view.fxml", "Connexion");
    }
}
