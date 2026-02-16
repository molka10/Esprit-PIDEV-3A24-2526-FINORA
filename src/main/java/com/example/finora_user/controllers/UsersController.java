package com.example.finora_user.controllers;

import com.example.finora_user.entities.User;
import com.example.finora_user.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class UsersController {

    // Table
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;

    // Form (Détails utilisateur)
    @FXML private TextField idField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label statusLabel;

    // Search UI
    @FXML private ToggleGroup searchModeGroup;
    @FXML private ToggleButton toggleText;
    @FXML private ToggleButton toggleId;
    @FXML private ToggleButton toggleRole;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterCombo;

    private UserService userService;
    private final ObservableList<User> data = FXCollections.observableArrayList();

    private enum SearchMode { TEXT, ID, ROLE }
    private SearchMode mode = SearchMode.TEXT;

    // -------------------- INIT --------------------
    @FXML
    public void initialize() {
        try {
            userService = new UserService();

            // ComboBoxes
            roleCombo.setItems(FXCollections.observableArrayList("ADMIN", "ENTREPRISE", "USER"));
            roleFilterCombo.setItems(FXCollections.observableArrayList("ADMIN", "ENTREPRISE", "USER"));

            // Table columns
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
            colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
            colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

            usersTable.setItems(data);

            // default UI state
            if (searchModeGroup != null && toggleText != null) {
                toggleText.setSelected(true);
                handleSearchMode();
            }

            handleRefresh();

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // -------------------- VALIDATION UI (Détails utilisateur) --------------------
    private void clearErrors() {
        usernameField.getStyleClass().remove("field-error");
        emailField.getStyleClass().remove("field-error");
        passwordField.getStyleClass().remove("field-error");
        roleCombo.getStyleClass().remove("field-error");
    }

    private void markError(Control c) {
        if (!c.getStyleClass().contains("field-error")) {
            c.getStyleClass().add("field-error");
        }
    }

    /**
     * @param requirePassword true on ADD, false on UPDATE
     * @return null if OK else error message
     */
    private String validateDetailsForm(boolean requirePassword) {
        clearErrors();

        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pass = passwordField.getText() == null ? "" : passwordField.getText();
        String role = roleCombo.getValue();

        boolean ok = true;

        // username
        if (username.isEmpty()) {
            markError(usernameField);
            ok = false;
        } else if (!username.matches("^[A-Za-z0-9_]{3,20}$")) {
            markError(usernameField);
            return "⚠️ Username invalide (3-20 caractères, lettres/chiffres/_).";
        }

        // email
        if (email.isEmpty()) {
            markError(emailField);
            ok = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            markError(emailField);
            return "⚠️ Email invalide. Exemple: user@mail.com";
        }

        // role
        if (role == null || role.trim().isEmpty()) {
            markError(roleCombo);
            ok = false;
        }

        // password only when adding
        if (requirePassword) {
            if (pass.isEmpty()) {
                markError(passwordField);
                ok = false;
            } else if (pass.length() < 6) {
                markError(passwordField);
                return "⚠️ Mot de passe trop court (min 6 caractères).";
            }
        }

        if (!ok) return "⚠️ Veuillez remplir correctement tous les champs.";

        return null;
    }

    // -------------------- CRUD --------------------
    @FXML
    private void handleRefresh() {
        try {
            data.setAll(userService.getAllUsers());
            statusLabel.setText("");
            clearErrors();
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd() {
        try {
            String err = validateDetailsForm(true); // password required
            if (err != null) {
                statusLabel.setText(err);
                return;
            }

            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String pass = passwordField.getText();
            String role = roleCombo.getValue();

            User u = new User(username, email, pass, role);
            userService.addUserReturnId(u);

            statusLabel.setText("✅ Utilisateur ajouté.");
            handleRefresh();
            handleClear();

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            if (idField.getText() == null || idField.getText().trim().isEmpty()) {
                statusLabel.setText("⚠️ Sélectionnez un utilisateur dans la table.");
                return;
            }

            String err = validateDetailsForm(false); // password not required
            if (err != null) {
                statusLabel.setText(err);
                return;
            }

            int id = Integer.parseInt(idField.getText().trim());
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String role = roleCombo.getValue();

            User u = new User(username, email, "", role);
            u.setId(id);

            boolean ok = userService.updateUser(u);

            if (ok) {
                statusLabel.setText("✅ Utilisateur modifié.");
                handleRefresh();
            } else {
                statusLabel.setText("⚠️ Modification échouée (ID introuvable).");
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("⚠️ ID invalide.");
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        try {
            if (idField.getText() == null || idField.getText().trim().isEmpty()) {
                statusLabel.setText("⚠️ Sélectionner un utilisateur.");
                return;
            }

            int id = Integer.parseInt(idField.getText().trim());

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer utilisateur ID=" + id + " ?");
            confirm.setContentText("Cette action est irréversible.");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            boolean ok = userService.deleteUser(id);

            if (ok) {
                handleRefresh();
                handleClear();
                statusLabel.setText("✅ Utilisateur supprimé.");
            } else {
                statusLabel.setText("⚠️ ID introuvable.");
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("⚠️ ID invalide.");
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        idField.clear();
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        roleCombo.setValue(null);
        statusLabel.setText("");
        clearErrors();
    }

    @FXML
    private void handleTableClick() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        idField.setText(String.valueOf(selected.getId()));
        usernameField.setText(selected.getUsername());
        emailField.setText(selected.getEmail());
        roleCombo.setValue(selected.getRole());
        passwordField.clear();
        clearErrors();
    }

    // -------------------- SEARCH --------------------
    @FXML
    private void handleSearchMode() {
        Toggle selected = (searchModeGroup == null) ? null : searchModeGroup.getSelectedToggle();
        if (selected == null) selected = toggleText;

        if (selected == toggleId) {
            mode = SearchMode.ID;

            searchField.setDisable(false);
            searchField.clear();
            searchField.setPromptText("Rechercher par ID...");

            roleFilterCombo.setVisible(false);
            roleFilterCombo.setManaged(false);

            handleRefresh();

        } else if (selected == toggleRole) {
            mode = SearchMode.ROLE;

            searchField.setDisable(true);
            searchField.clear();
            searchField.setPromptText("Mode role: utiliser la liste");

            roleFilterCombo.setVisible(true);
            roleFilterCombo.setManaged(true);

            if (roleFilterCombo.getValue() == null) {
                roleFilterCombo.setValue("USER");
            }
            handleRoleFilter();

        } else {
            mode = SearchMode.TEXT;

            searchField.setDisable(false);
            searchField.clear();
            searchField.setPromptText("Rechercher par username/email...");

            roleFilterCombo.setVisible(false);
            roleFilterCombo.setManaged(false);

            handleRefresh();
        }
    }

    @FXML
    private void handleSearchDynamic() {
        try {
            if (mode == SearchMode.ROLE) return;

            String q = (searchField.getText() == null) ? "" : searchField.getText().trim();

            if (q.isEmpty()) {
                handleRefresh();
                return;
            }

            if (mode == SearchMode.ID) {
                if (!q.matches("\\d+")) return;

                int id = Integer.parseInt(q);
                User u = userService.getUserById(id);
                data.clear();
                if (u != null) data.add(u);

            } else {
                data.setAll(userService.searchText(q));
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur recherche: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRoleFilter() {
        try {
            String role = roleFilterCombo.getValue();
            if (role == null || role.trim().isEmpty()) {
                handleRefresh();
                return;
            }

            data.setAll(userService.searchByRole(role.trim()));
            statusLabel.setText("✅ Filtre role: " + role + " (" + data.size() + ")");

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur filtre role: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
