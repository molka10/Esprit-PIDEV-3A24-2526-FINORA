package com.example.finora.controllers.appeldoffre;

import com.example.finora.utils.Session;
import com.example.finora.utils.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class DashboardController {

    private static DashboardController instance;

    public static DashboardController getInstance() {
        return instance;
    }

    @FXML
    private StackPane contentPane;
    @FXML
    private Label roleLabel;

    @FXML
    private Button btnHome;
    @FXML
    private Button btnAppelOffre;
    @FXML
    private Button btnCandidature;
    @FXML
    private Button btnStats;

    @FXML
    private Button btnTheme;

    @FXML
    public void initialize() {
        instance = this;

        if (Session.getCurrentUser() != null) {
            String roleStr = Session.getCurrentUser().getRole();
            String email = Session.getCurrentUser().getEmail();
            roleLabel.setText(roleStr + " • " + email);
        }

        applyRolePermissions();
        syncThemeButtonText();

        goHome();
    }

    private void applyRolePermissions() {
        if (Session.getCurrentUser() == null)
            return;
        String role = Session.getCurrentUser().getRole();

        // ADMIN or ENTREPRISE: everything
        if ("ADMIN".equalsIgnoreCase(role) || "ENTREPRISE".equalsIgnoreCase(role)) {
            setAllowed(btnAppelOffre, true);
            setAllowed(btnCandidature, true);
            setAllowed(btnStats, true);
            return;
        }

        // USER: AppelOffre + Candidature. No Stats.
        setAllowed(btnAppelOffre, true);
        setAllowed(btnCandidature, true);
        setAllowed(btnStats, false);
    }

    private void setAllowed(Button btn, boolean allowed) {
        if (btn == null)
            return;
        btn.setDisable(!allowed);
        btn.setVisible(allowed);
        btn.setManaged(allowed);
    }

    private void loadScreen(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean requireRole(String... allowed) {
        if (Session.getCurrentUser() == null)
            return false;
        String role = Session.getCurrentUser().getRole();
        for (var r : allowed) {
            if (r.equalsIgnoreCase(role))
                return true;
        }
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Accès refusé");
        a.setHeaderText("Vous n'avez pas accès à cette section.");
        a.setContentText("Rôle actuel: " + role);
        a.showAndWait();
        return false;
    }

    @FXML
    public void goHome() {
        if (Session.getCurrentUser() == null)
            return;
        String role = Session.getCurrentUser().getRole();
        String fxml = "/ui/home/UserHome.fxml"; // Default
        if ("ADMIN".equalsIgnoreCase(role))
            fxml = "/ui/home/AdminHome.fxml";
        else if ("ENTREPRISE".equalsIgnoreCase(role))
            fxml = "/ui/home/EntrepriseHome.fxml";

        loadScreen(fxml);
    }

    @FXML
    public void goAppelOffre() {
        loadScreen("/ui/AppelOffreView.fxml");
    }

    @FXML
    public void goCandidature() {
        loadScreen("/ui/CandidatureView.fxml");
    }

    @FXML
    public void goStats() {
        if (!requireRole("ADMIN", "ENTREPRISE"))
            return;
        loadScreen("/ui/StatsView.fxml");
    }

    @FXML
    public void toggleTheme() {
        ThemeManager.toggle();
        if (contentPane != null && contentPane.getScene() != null) {
            ThemeManager.apply(contentPane.getScene());
        }
        syncThemeButtonText();
    }

    private void syncThemeButtonText() {
        if (btnTheme == null)
            return;
        boolean dark = ThemeManager.getCurrent() == ThemeManager.Theme.DARK;
        btnTheme.setText(dark ? "🌙 Dark" : "☀️ Light");
    }

    @FXML
    public void logout() {
        Session.clear();
        // Redirection should be handled by the shell or a Navigator
    }
}
