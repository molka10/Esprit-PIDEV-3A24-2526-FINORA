package com.example.finora.controllers;

import com.example.finora.utils.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DashboardUserController {

    @FXML private Label statusLabel;

    @FXML void openBourse() { statusLabel.setText("TODO: Page Bourse"); }
    @FXML void openPortefeuille() { statusLabel.setText("TODO: Page Portefeuille"); }
    @FXML void openAppelOffre() { statusLabel.setText("TODO: Page Appel d'offre"); }

    @FXML
    void logout() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        Navigator.goTo(stage, "login-view.fxml", "Connexion");
    }
}
