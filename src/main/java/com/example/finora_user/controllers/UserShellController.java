package com.example.finora_user.controllers;

import com.example.finora_user.utils.Navigator;
import com.example.finora_user.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class UserShellController {

    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        // Route protection: must be logged in to access the user shell
        if (!Session.isLoggedIn()) {
            logout();
            return;
        }
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

    @FXML
    public void logout() {
        Session.clear();
        Stage stage = (Stage) contentArea.getScene().getWindow();
        Navigator.goTo(stage, "login-view.fxml", "Connexion");
    }
}
