package com.example.finora_user.controllers;

import com.example.finora_user.entities.User;
import com.example.finora_user.services.UserService;
import com.example.finora_user.utils.InputValidator;
import com.example.finora_user.utils.Navigator;
import com.example.finora_user.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private UserService service;

    @FXML
    public void initialize() {
        try {
            service = new UserService();
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() {
        try {
            String email = emailField.getText().trim();
            String pass = passwordField.getText();

            String err = InputValidator.validateLogin(email, pass);
            if (err != null) {
                statusLabel.setText("⚠️ " + err);
                return;
            }

            User user = service.login(email, pass);

            if (user == null) {
                statusLabel.setText("❌ Email ou mot de passe incorrect.");
                return;
            }

            // Store authenticated user in session
            Session.setCurrentUser(user);

            Stage stage = (Stage) emailField.getScene().getWindow();

            if ("ADMIN".equals(user.getRole())) {
                Navigator.goTo(stage, "admin-shell.fxml", "Dashboard Admin");
            } else {
                Navigator.goTo(stage, "user-shell.fxml", "Dashboard");
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur DB: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSignup() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Navigator.goTo(stage, "signup-view.fxml", "Créer un compte");
    }
}
