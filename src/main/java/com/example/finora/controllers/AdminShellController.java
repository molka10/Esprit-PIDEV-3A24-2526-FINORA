package com.example.finora.controllers;

import com.example.finora.utils.Navigator;
import com.example.finora.utils.Session;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AdminShellController {

    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        Platform.runLater(() -> loadCenterSafe("dashboard-admin.fxml"));
    }

    @FXML
    private void goBourse(ActionEvent event) {
        loadCenterSafe("bourse-view.fxml");
    }

    @FXML
    private void goPortefeuille(ActionEvent event) {
        loadCenterSafe("portefeuille-view.fxml");
    }

    @FXML
    private void goAppelOffre(ActionEvent event) {
        loadCenterSafe("appeloffre-view.fxml");
    }

    @FXML
    private void goGestionUsers(ActionEvent event) {
        loadCenterSafe("users-view.fxml");
    }

    @FXML
    private void goProfile(ActionEvent event) {
        loadCenterSafe("profile-view.fxml");
    }

    private void loadCenterSafe(String fxml) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/example/finora/" + fxml));
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            contentArea.getChildren().setAll(new Label("Erreur chargement: " + fxml + "\n" + e.getMessage()));
        }
    }

    @FXML
    private void logout(ActionEvent event) {
        try {
            Session.clear();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Navigator.goTo(stage, "login-view.fxml", "Connexion");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}