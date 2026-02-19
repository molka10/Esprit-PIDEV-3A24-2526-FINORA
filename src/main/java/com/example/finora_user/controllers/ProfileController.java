package com.example.finora_user.controllers;

import com.example.finora_user.entities.User;
import com.example.finora_user.services.UserService;
import com.example.finora_user.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
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

        String initials = getInitials(u.getUsername());
        avatarText.setText(initials);

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
            if (ok) {
                statusLabel.setText("✅ Profil mis à jour.");
                // refresh avatar + title
                loadFromSession();
            } else {
                statusLabel.setText("⚠️ Mise à jour échouée.");
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur DB: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur: " + e.getMessage());
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
