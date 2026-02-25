package com.example.finora.controllers;

import com.example.finora.entities.User;
import com.example.finora.services.*;
import com.example.finora.utils.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private TextField otpField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;

    private UserService userService;
    private PasswordResetService resetService;

    // must never be null
    private SmsService smsService;

    // step-2 state
    private User targetUser;

    @FXML
    public void initialize() {
        try {
            userService = new UserService();
            resetService = new PasswordResetService();

            // Try Twilio; if not configured -> fallback to console (dev mode)
            try {
                smsService = new TwilioSmsService();
                statusLabel.setText("✅ SMS configuré (Twilio).");
            } catch (Exception ex) {
                smsService = new ConsoleSmsService();
                statusLabel.setText("⚠️ SMS non configuré (mode DEV). Le code sera affiché dans la console.");
            }

        } catch (SQLException e) {
            // Avoid NPE even if DB init fails
            smsService = new ConsoleSmsService();
            statusLabel.setText("❌ DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSendOtp() {
        try {
            if (userService == null || resetService == null) {
                statusLabel.setText("❌ Services non initialisés. Vérifiez la connexion DB.");
                return;
            }

            String email = emailField.getText() == null ? "" : emailField.getText().trim();
            if (email.isEmpty()) {
                statusLabel.setText("⚠️ Entrez votre email.");
                return;
            }

            // Find user by email
            targetUser = userService.getUserByEmail(email);
            if (targetUser == null) {
                statusLabel.setText("❌ Aucun utilisateur avec cet email.");
                return;
            }

            // Must have phone number stored
            String phone = targetUser.getPhone();
            if (phone == null || phone.isBlank()) {
                statusLabel.setText("❌ Aucun numéro de téléphone enregistré pour ce compte.");
                return;
            }

            // Create OTP
            String code = resetService.createOtpForUser(targetUser.getId());

            // Send SMS (TwilioSmsService converts 8-digit to +216 automatically)
            smsService.send(phone, "Finora: votre code de reinitialisation est " + code + " (valide 5 min).");

            statusLabel.setText("✅ Code envoyé par SMS. (Si mode DEV, regardez la console)");

        } catch (SQLException e) {
            statusLabel.setText("❌ DB: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // from TwilioSmsService if phone format invalid
            statusLabel.setText("❌ Numéro invalide: " + e.getMessage());
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReset() {
        try {
            if (userService == null || resetService == null) {
                statusLabel.setText("❌ Services non initialisés. Vérifiez la connexion DB.");
                return;
            }

            if (targetUser == null) {
                statusLabel.setText("⚠️ Envoyez le code d’abord.");
                return;
            }

            String code = otpField.getText() == null ? "" : otpField.getText().trim();
            String newPass = newPasswordField.getText();
            String confirm = confirmPasswordField.getText();

            if (code.isEmpty() || newPass == null || newPass.isBlank() || confirm == null || confirm.isBlank()) {
                statusLabel.setText("⚠️ Remplissez tous les champs.");
                return;
            }

            if (newPass.length() < 8) {
                statusLabel.setText("⚠️ Mot de passe trop court (min 8 caractères).");
                return;
            }

            if (!newPass.equals(confirm)) {
                statusLabel.setText("⚠️ Confirmation incorrecte.");
                return;
            }

            // Verify OTP (also marks it as used)
            boolean okCode = resetService.verifyOtp(targetUser.getId(), code);
            if (!okCode) {
                statusLabel.setText("❌ Code invalide ou expiré.");
                return;
            }

            // Update password (bcrypt inside UserService.updatePassword -> PasswordUtils.hash)
            boolean okReset = resetService.resetPassword(targetUser.getId(), newPass, userService);
            if (!okReset) {
                statusLabel.setText("❌ Réinitialisation échouée.");
                return;
            }

            statusLabel.setText("✅ Mot de passe réinitialisé. Vous pouvez vous connecter.");

            // Clear fields and state
            otpField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            targetUser = null;

        } catch (SQLException e) {
            statusLabel.setText("❌ DB: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Navigator.goTo(stage, "login-view.fxml", "Connexion");
    }
}