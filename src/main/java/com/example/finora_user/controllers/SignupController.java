package com.example.finora_user.controllers;

import com.example.finora_user.entities.User;
import com.example.finora_user.services.UserService;
import com.example.finora_user.utils.InputValidator;
import com.example.finora_user.utils.Navigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class SignupController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private DatePicker dobPicker;

    @FXML private ComboBox<String> roleCombo;
    @FXML private CheckBox termsCheck;
    @FXML private Label statusLabel;

    private UserService service;

    @FXML
    public void initialize() {
        try {
            service = new UserService();
            roleCombo.setItems(FXCollections.observableArrayList("USER", "ENTREPRISE"));
            roleCombo.setValue("USER");
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignup() {
        try {
            // ✅ avoid crash if fx:id missing
            if (termsCheck == null) {
                statusLabel.setText("❌ termsCheck is NULL. Fix fx:id=\"termsCheck\" in signup-view.fxml");
                return;
            }

            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String pass = passwordField.getText();
            String confirmPass = confirmPasswordField.getText();

            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            LocalDate dob = dobPicker.getValue();

            String role = roleCombo.getValue();
            boolean termsAccepted = termsCheck.isSelected();

            String err = InputValidator.validateSignup(
                    username, email, pass, confirmPass,
                    phone, address, dob, termsAccepted
            );

            if (err != null) {
                statusLabel.setText("⚠️ " + err);
                return;
            }

            User u = new User(username, email, pass, role);
            u.setPhone(phone);
            u.setAddress(address);
            u.setDateOfBirth(dob);

            int id = service.addUserReturnId(u);

            if (id != -1) {
                statusLabel.setText("✅ Compte créé ! Retour au login...");
                Navigator.goTo((Stage) usernameField.getScene().getWindow(),
                        "login-view.fxml", "Connexion");
            } else {
                statusLabel.setText("❌ Échec création compte.");
            }

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
