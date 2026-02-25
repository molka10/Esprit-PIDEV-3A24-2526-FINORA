package com.example.finora.controllers;

import com.example.finora.entities.User;
import com.example.finora.services.CaptchaService;
import com.example.finora.services.UserService;
import com.example.finora.utils.InputValidator;
import com.example.finora.utils.Navigator;
import com.example.finora.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    // CAPTCHA
    @FXML private ImageView captchaImage;
    @FXML private TextField captchaField;

    private UserService userService;
    private final CaptchaService captchaService = new CaptchaService();

    @FXML
    public void initialize() {
        try {
            userService = new UserService();
            refreshCaptcha();
        } catch (SQLException e) {
            statusLabel.setText("❌ DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshCaptcha() {
        if (captchaImage != null) {
            captchaImage.setImage(captchaService.generateCaptchaImage(220, 60, 6));
        }
        if (captchaField != null) captchaField.clear();
    }

    @FXML
    private void handleLogin() {
        try {
            String email = emailField.getText() == null ? "" : emailField.getText().trim();
            String pass = passwordField.getText() == null ? "" : passwordField.getText();

            String err = InputValidator.validateLogin(email, pass);
            if (err != null) {
                statusLabel.setText("⚠️ " + err);
                return;
            }

            // CAPTCHA check
            if (captchaField == null || !captchaService.verify(captchaField.getText())) {
                statusLabel.setText("⚠️ CAPTCHA incorrect. Réessayez.");
                refreshCaptcha();
                return;
            }

            // Your service method
            User u = userService.login(email, pass);

            if (u == null) {
                statusLabel.setText("❌ Email ou mot de passe incorrect.");
                refreshCaptcha();
                return;
            }

            // ✅ Save session
            Session.setCurrentUser(u);

            Stage stage = (Stage) emailField.getScene().getWindow();

            if ("ADMIN".equalsIgnoreCase(u.getRole())) {
                Navigator.goTo(stage, "admin-shell.fxml", "Dashboard Admin");
            } else {
                Navigator.goTo(stage, "user-shell.fxml", "Dashboard");
            }

        } catch (Exception e) {
            statusLabel.setText("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            refreshCaptcha();
        }
    }

    @FXML
    private void goToSignup() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Navigator.goTo(stage, "signup-view.fxml", "Inscription");
    }

    @FXML
    private void goForgotPassword() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Navigator.goTo(stage, "forgot-password-view.fxml", "Mot de passe oublié");
    }
}