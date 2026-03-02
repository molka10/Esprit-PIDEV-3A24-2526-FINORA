package com.example.finora.controllers;

import com.example.finora.entities.User;
import com.example.finora.utils.Navigator;
import com.example.finora.utils.Session;
import com.example.finora.finorainves.SceneNavigator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import com.example.finora.utils.ThemeManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AdminShellController {

    @FXML
    private Button btnTheme;

    @FXML
    private StackPane contentArea;

    @FXML
    private Label avatarText;

    @FXML
    private Label profileNameLabel;

    @FXML
    private Label sidebarAvatarText;

    @FXML
    private Label sidebarNameLabel;

    @FXML
    private Label sidebarIdLabel;

    private static AdminShellController instance;

    public static AdminShellController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        User user = Session.getCurrentUser();
        if (user != null) {
            String name = user.getUsername();
            String idStr = "ID: " + user.getId();
            String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "A";

            if (avatarText != null)
                avatarText.setText(initial);
            if (profileNameLabel != null)
                profileNameLabel.setText(name);

            if (sidebarAvatarText != null)
                sidebarAvatarText.setText(initial);
            if (sidebarNameLabel != null)
                sidebarNameLabel.setText(name);
            if (sidebarIdLabel != null)
                sidebarIdLabel.setText(idStr);

            SceneNavigator.setContentArea(contentArea);
            updateThemeButton();
        }
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
        ThemeManager.setTheme(ThemeManager.getTheme() == ThemeManager.Theme.LIGHT ? ThemeManager.Theme.DARK
                : ThemeManager.Theme.LIGHT);
        ThemeManager.apply(contentArea.getScene());
        updateThemeButton();
    }

    private void updateThemeButton() {
        if (btnTheme != null) {
            if (ThemeManager.getTheme() == ThemeManager.Theme.LIGHT) {
                btnTheme.setText("☀️ Light");
            } else {
                btnTheme.setText("🌙 Dark");
            }
        }
    }

    @FXML
    private void goBourse(ActionEvent event) {
        loadCenterSafe("/bourse/dashboard-admin-view.fxml");
    }

    @FXML
    private void goGestionBourses(ActionEvent event) {
        loadCenterSafe("/bourse/bourse-view.fxml");
    }

    @FXML
    private void goGestionActions(ActionEvent event) {
        loadCenterSafe("/bourse/action-view.fxml");
    }

    @FXML
    private void goCommissions(ActionEvent event) {
        loadCenterSafe("/bourse/Commission view.fxml");
    }

    @FXML
    private void goSupervision(ActionEvent event) {
        loadCenterSafe("/bourse/supervision-view.fxml");
    }

    @FXML
    private void goPortefeuille(ActionEvent event) {
        loadCenterSafe("/wallet/admin-dashboard.fxml");
    }

    @FXML
    private void goAppelOffre(ActionEvent event) {
        loadCenterSafe("/ui/home/AdminHome.fxml");
    }

    @FXML
    private void goCandidature(ActionEvent event) {
        loadCenterSafe("/ui/CandidatureView.fxml");
    }

    @FXML
    private void goStats(ActionEvent event) {
        loadCenterSafe("/ui/StatsView.fxml");
    }

    @FXML
    private void goFormation(ActionEvent event) {
        loadCenterSafe("/formation/formation_list.fxml");
    }

    @FXML
    private void goGestionUsers(ActionEvent event) {
        loadCenterSafe("users-view.fxml");
    }

    @FXML
    private void goInvestmentManagement(ActionEvent event) {
        loadCenterSafe("/com/example/finora/investment/investment_cards.fxml");
    }

    @FXML
    private void goProfile(ActionEvent event) {
        loadCenterSafe("profile-view.fxml");
    }

    public void loadCenterSafe(String fxml) {
        try {
            String path = fxml.startsWith("/") ? fxml : "/com/example/finora/" + fxml;
            Parent view = FXMLLoader.load(getClass().getResource(path));
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
            Navigator.setRoot(stage, "login-view.fxml", "Connexion");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}