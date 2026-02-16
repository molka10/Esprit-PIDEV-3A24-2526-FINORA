package com.example.finora_user.controllers;

import com.example.finora_user.utils.Navigator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AdminShellController {

    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        goBourse(); // default page
    }

    private void loadCenter(String fxml) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/example/finora_user/" + fxml));
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML public void goBourse() { loadCenter("bourse-view.fxml"); }
    @FXML public void goPortefeuille() { loadCenter("portefeuille-view.fxml"); }
    @FXML public void goAppelOffre() { loadCenter("appeloffre-view.fxml"); }
    @FXML public void goGestionUsers() { loadCenter("users-view.fxml"); }

    @FXML
    public void logout() {
        Stage stage = (Stage) contentArea.getScene().getWindow();
        Navigator.goTo(stage, "login-view.fxml", "Connexion");
    }
}
