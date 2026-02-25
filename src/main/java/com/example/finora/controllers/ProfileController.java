package com.example.finora.controllers;

import com.example.finora.entities.User;
import com.example.finora.services.UserService;
import com.example.finora.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class ProfileController {

    @FXML private Label avatarText;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;

    @FXML private TextField usernameField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private DatePicker dobPicker;

    @FXML private Label statusLabel;

    // Change password fields
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label pwdStatusLabel;

    private UserService userService;

    @FXML
    public void initialize() {
        try {
            userService = new UserService();
            loadFromSession();
        } catch (SQLException e) {
            statusLabel.setText("❌ DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFromSession() {
        User u = Session.getCurrentUser();
        if (u == null) {
            statusLabel.setText("⚠️ Session expirée.");
            return;
        }

        avatarText.setText(getInitials(u.getUsername()));
        fullNameLabel.setText(u.getUsername());
        emailLabel.setText(u.getEmail());

        usernameField.setText(u.getUsername());
        phoneField.setText(u.getPhone() == null ? "" : u.getPhone());
        addressField.setText(u.getAddress() == null ? "" : u.getAddress());
        dobPicker.setValue(u.getDateOfBirth());
    }

    @FXML
    private void handleSave() {
        try {
            User current = Session.getCurrentUser();
            if (current == null) {
                statusLabel.setText("⚠️ Session expirée.");
                return;
            }

            current.setUsername(usernameField.getText().trim());
            current.setPhone(phoneField.getText() == null ? "" : phoneField.getText().trim());
            current.setAddress(addressField.getText() == null ? "" : addressField.getText().trim());
            current.setDateOfBirth(dobPicker.getValue());

            boolean ok = userService.updateUser(current);
            statusLabel.setText(ok ? "✅ Profil mis à jour." : "⚠️ Mise à jour échouée.");

            // refresh header + avatar
            loadFromSession();

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChangePassword() {
        try {
            User current = Session.getCurrentUser();
            if (current == null) {
                pwdStatusLabel.setText("⚠️ Session expirée.");
                return;
            }

            String oldPass = oldPasswordField.getText();
            String newPass = newPasswordField.getText();
            String confirm = confirmPasswordField.getText();

            if (oldPass == null || oldPass.isBlank() ||
                    newPass == null || newPass.isBlank() ||
                    confirm == null || confirm.isBlank()) {
                pwdStatusLabel.setText("⚠️ Remplissez tous les champs.");
                return;
            }

            if (newPass.length() < 8) {
                pwdStatusLabel.setText("⚠️ Nouveau mot de passe (min 8 caractères).");
                return;
            }

            if (!newPass.equals(confirm)) {
                pwdStatusLabel.setText("⚠️ Confirmation incorrecte.");
                return;
            }

            boolean ok = userService.changePassword(current.getId(), oldPass, newPass);
            if (ok) {
                pwdStatusLabel.setText("✅ Mot de passe mis à jour.");
                oldPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
            } else {
                pwdStatusLabel.setText("❌ Ancien mot de passe incorrect.");
            }

        } catch (SQLException e) {
            pwdStatusLabel.setText("❌ DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "U";
        String n = name.trim();
        if (n.length() == 1) return n.toUpperCase();
        return ("" + n.charAt(0) + n.charAt(n.length() - 1)).toUpperCase();
    }
}