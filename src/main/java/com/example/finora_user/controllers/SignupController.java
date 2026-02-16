package com.example.finora_user.controllers;

import com.example.finora_user.entities.User;
import com.example.finora_user.services.UserService;
import com.example.finora_user.utils.InputValidator;
import com.example.finora_user.utils.Navigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class SignupController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label statusLabel;

    private UserService service;

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList("USER", "ENTREPRISE"));
        roleCombo.setValue("USER");

        try {
            service = new UserService();
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignup() {
        try {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String pass = passwordField.getText();
            String role = roleCombo.getValue();

            String err = InputValidator.validateSignup(username, email, pass, role);
            if (err != null) {
                statusLabel.setText("⚠️ " + err);
                return;
            }

            // store plain password for now
            User u = new User(username, email, pass, role);

            service.addUserReturnId(u);

            statusLabel.setText("✅ Compte créé ! Retour au login...");
            Navigator.goTo((Stage) usernameField.getScene().getWindow(), "login-view.fxml", "Connexion");

        } catch (SQLException e) {
            // if email is UNIQUE, this may happen when email already exists
            statusLabel.setText("❌ Erreur DB: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        Navigator.goTo(stage, "login-view.fxml", "Connexion");
    }
}
