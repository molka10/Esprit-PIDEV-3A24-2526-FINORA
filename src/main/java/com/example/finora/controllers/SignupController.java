package com.example.finora.controllers;

import com.example.finora.entities.User;
import com.example.finora.services.DuplicateDetectionService;
import com.example.finora.services.DuplicateMatch;
import com.example.finora.services.UserService;
import com.example.finora.utils.InputValidator;
import com.example.finora.utils.Navigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

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
    private DuplicateDetectionService duplicateService;

    @FXML
    public void initialize() {
        try {
            service = new UserService();
            duplicateService = new DuplicateDetectionService(service);

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

            String username = safe(usernameField.getText());
            String email = safe(emailField.getText());
            String pass = passwordField.getText();
            String confirmPass = confirmPasswordField.getText();

            String phone = safe(phoneField.getText());
            String address = safe(addressField.getText());
            LocalDate dob = dobPicker.getValue();

            String role = roleCombo.getValue();
            boolean termsAccepted = termsCheck.isSelected();

            // ✅ Use YOUR signature (8 params, no role)
            String err = InputValidator.validateSignup(
                    username, email, pass, confirmPass,
                    phone, address, dob, termsAccepted
            );

            if (err != null) {
                statusLabel.setText("⚠️ " + err);
                return;
            }

            // ✅ Duplicate detection BEFORE insert (username + email only)
            List<DuplicateMatch> dups = duplicateService.findDuplicates(username, email, -1);
            if (!dups.isEmpty()) {
                boolean proceed = confirmDuplicates("Comptes similaires détectés", dups);
                if (!proceed) {
                    statusLabel.setText("⚠️ Inscription annulée (doublon possible).");
                    return;
                }
            }

            // ✅ Create user as your project does
            User u = new User(username, email, pass, role);
            u.setPhone(phone);
            u.setAddress(address);
            u.setDateOfBirth(dob);

            int id = service.addUserReturnId(u);

            if (id != -1) {
                statusLabel.setText("✅ Compte créé ! Retour au login...");

                Stage stage = (Stage) usernameField.getScene().getWindow();
                Navigator.goTo(stage, "login-view.fxml", "Connexion");
            } else {
                statusLabel.setText("❌ Échec création compte.");
            }

        } catch (SQLException e) {
            // ✅ Friendly message for unique constraint (email already exists)
            if ("23000".equals(e.getSQLState())) {
                statusLabel.setText("⚠️ Cet email existe déjà. Essayez un autre.");
            } else {
                statusLabel.setText("❌ Erreur base de données: " + e.getMessage());
            }
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

    // -------------------- UI: duplicates dialog --------------------

    private boolean confirmDuplicates(String title, List<DuplicateMatch> matches) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText("Des comptes similaires existent déjà.\nVoulez-vous continuer ?");

        StringBuilder sb = new StringBuilder();
        for (DuplicateMatch m : matches) {
            sb.append("• ")
                    .append(m.username()).append(" (").append(m.email()).append(")\n")
                    .append("  ").append(String.join(", ", m.reasons()))
                    .append("\n\n");
        }

        TextArea area = new TextArea(sb.toString().trim());
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(9);

        alert.getDialogPane().setContent(area);

        ButtonType proceed = new ButtonType("Continuer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(proceed, cancel);

        return alert.showAndWait().orElse(cancel) == proceed;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}