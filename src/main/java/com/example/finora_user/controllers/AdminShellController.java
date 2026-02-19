package com.example.finora_user.controllers;

import com.example.finora_user.utils.Navigator;
import com.example.finora_user.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AdminShellController {

    @FXML private StackPane contentArea;

    // Topbar profile widgets
    @FXML private Label avatarText;
    @FXML private Label profileNameLabel;

    @FXML
    public void initialize() {
        if (!Session.isAdmin()) {
            logout();
            return;
        }
        setupProfileHeader();
        goBourse();
    }

    private void setupProfileHeader() {
        if (Session.getCurrentUser() == null) return;

        String username = Session.getCurrentUser().getUsername();
        profileNameLabel.setText(username);

        avatarText.setText(getInitials(username));
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "A";
        String n = name.trim();
        if (n.length() == 1) return n.toUpperCase();
        return ("" + n.charAt(0) + n.charAt(n.length() - 1)).toUpperCase();
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
    public void goProfile() {
        loadCenter("profile-view.fxml");
    }

    @FXML
    public void logout() {
        Session.clear();
        Stage stage = (Stage) contentArea.getScene().getWindow();
        Navigator.goTo(stage, "login-view.fxml", "Connexion");
    }
}
