package com.example.finora_user.controllers;

import com.example.finora_user.entities.User;
import com.example.finora_user.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;

public class UsersController {

    @FXML private ListView<User> usersList;

    @FXML private TextField idField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleCombo;

    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private DatePicker dobPicker;

    @FXML private Label statusLabel;

    @FXML private ToggleGroup searchModeGroup;
    @FXML private ToggleButton toggleText;
    @FXML private ToggleButton toggleId;
    @FXML private ToggleButton toggleRole;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterCombo;

    @FXML private Label countLabel;

    private UserService userService;
    private final ObservableList<User> data = FXCollections.observableArrayList();

    private enum SearchMode { TEXT, ID, ROLE }
    private SearchMode mode = SearchMode.TEXT;

    // Admin-added users need a password in DB.
    // Since you removed password field from Admin form, we set a safe default.
    private static final String DEFAULT_PASSWORD = "ChangeMe123!";

    @FXML
    public void initialize() {
        try {
            userService = new UserService();

            roleCombo.setItems(FXCollections.observableArrayList("ADMIN", "ENTREPRISE", "USER"));
            roleFilterCombo.setItems(FXCollections.observableArrayList("ADMIN", "ENTREPRISE", "USER"));

            usersList.setItems(data);
            usersList.setCellFactory(lv -> new UserCardCell());
            usersList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            usersList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV != null) loadUserToForm(newV);
            });

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

    // -------------------- Form validation --------------------
    private void clearErrors() {
        usernameField.getStyleClass().remove("field-error");
        emailField.getStyleClass().remove("field-error");
        roleCombo.getStyleClass().remove("field-error");
        phoneField.getStyleClass().remove("field-error");
        addressField.getStyleClass().remove("field-error");
        dobPicker.getStyleClass().remove("field-error");
    }

    private void markError(Control c) {
        if (!c.getStyleClass().contains("field-error")) {
            c.getStyleClass().add("field-error");
        }
    }

    private String validateDetailsForm() {
        clearErrors();

        String username = safe(usernameField.getText());
        String email = safe(emailField.getText());
        String role = roleCombo.getValue();
        String phone = safe(phoneField.getText());
        String address = safe(addressField.getText());
        LocalDate dob = dobPicker.getValue();

        boolean ok = true;

        if (username.isEmpty()) { markError(usernameField); ok = false; }
        if (email.isEmpty()) { markError(emailField); ok = false; }
        if (role == null || role.isBlank()) { markError(roleCombo); ok = false; }
        if (address.isEmpty()) { markError(addressField); ok = false; }

        if (!ok) return "⚠️ Veuillez remplir correctement tous les champs obligatoires.";

        if (!username.matches("^[A-Za-z0-9_]{3,20}$")) {
            markError(usernameField);
            return "⚠️ Username invalide (3-20 caractères, lettres/chiffres/_).";
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            markError(emailField);
            return "⚠️ Email invalide. Exemple: user@mail.com";
        }

        if (!phone.isEmpty() && !phone.matches("^\\d{8,15}$")) {
            markError(phoneField);
            return "⚠️ Téléphone invalide (8 à 15 chiffres).";
        }

        if (dob != null) {
            LocalDate min = LocalDate.now().minusYears(18);
            if (dob.isAfter(min)) {
                markError(dobPicker);
                return "⚠️ L'utilisateur doit avoir au moins 18 ans.";
            }
        }

        return null;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    // -------------------- CRUD --------------------
    @FXML
    private void handleRefresh() {
        try {
            data.setAll(userService.getAllUsers());
            updateCount();
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
            String err = validateDetailsForm();
            if (err != null) {
                statusLabel.setText(err);
                return;
            }

            User u = new User();
            u.setUsername(usernameField.getText().trim());
            u.setEmail(emailField.getText().trim());
            u.setRole(roleCombo.getValue());

            u.setPhone(safe(phoneField.getText()));
            u.setAddress(addressField.getText().trim());
            u.setDateOfBirth(dobPicker.getValue());

            // Since password is removed from admin form:
            // Insert with a default password (BCrypt hashing handled by service).
            u.setMotDePasse(DEFAULT_PASSWORD);

            userService.addUserReturnId(u);

            statusLabel.setText("✅ Utilisateur ajouté. Mot de passe par défaut: " + DEFAULT_PASSWORD);
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
                statusLabel.setText("⚠️ Sélectionnez un utilisateur dans la liste.");
                return;
            }

            String err = validateDetailsForm();
            if (err != null) {
                statusLabel.setText(err);
                return;
            }

            int id = Integer.parseInt(idField.getText().trim());

            User u = new User();
            u.setId(id);
            u.setUsername(usernameField.getText().trim());
            u.setEmail(emailField.getText().trim());
            u.setRole(roleCombo.getValue());
            u.setPhone(safe(phoneField.getText()));
            u.setAddress(addressField.getText().trim());
            u.setDateOfBirth(dobPicker.getValue());

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
            confirm.setHeaderText("Supprimer cet utilisateur ?");
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
        usersList.getSelectionModel().clearSelection();
        idField.clear();
        usernameField.clear();
        emailField.clear();
        roleCombo.setValue(null);
        phoneField.clear();
        addressField.clear();
        dobPicker.setValue(null);
        statusLabel.setText("");
        clearErrors();
    }

    private void loadUserToForm(User selected) {
        idField.setText(String.valueOf(selected.getId()));
        usernameField.setText(selected.getUsername());
        emailField.setText(selected.getEmail());
        roleCombo.setValue(selected.getRole());
        phoneField.setText(selected.getPhone() == null ? "" : selected.getPhone());
        addressField.setText(selected.getAddress() == null ? "" : selected.getAddress());
        dobPicker.setValue(selected.getDateOfBirth());
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

            if (roleFilterCombo.getValue() == null) roleFilterCombo.setValue("USER");
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

            String q = safe(searchField.getText());

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

            updateCount();

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
            updateCount();
            statusLabel.setText("✅ Filtre role: " + role + " (" + data.size() + ")");

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur filtre role: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateCount() {
        if (countLabel != null) countLabel.setText(data.size() + " utilisateurs");
    }

    // -------------------- CARD CELL --------------------
    private static class UserCardCell extends ListCell<User> {

        @Override
        protected void updateItem(User user, boolean empty) {
            super.updateItem(user, empty);

            if (empty || user == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            HBox card = new HBox(12);
            card.getStyleClass().add("user-card");
            card.setPadding(new Insets(12, 12, 12, 12));

            VBox left = new VBox(3);

            Label name = new Label(user.getUsername());
            name.getStyleClass().add("user-name");

            Label email = new Label(user.getEmail());
            email.getStyleClass().add("user-email");

            left.getChildren().addAll(name, email);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            VBox right = new VBox(6);
            right.setMinWidth(160);
            right.setMaxWidth(160);

            Label role = new Label(user.getRole());
            role.getStyleClass().addAll("badge", roleClass(user.getRole()));

            right.getChildren().add(role);

            card.getChildren().addAll(left, spacer, right);

            setText(null);
            setGraphic(card);

            Node g = getGraphic();
            if (isSelected()) g.getStyleClass().add("user-card-selected");
            else g.getStyleClass().remove("user-card-selected");
        }

        @Override
        public void updateSelected(boolean selected) {
            super.updateSelected(selected);
            if (getGraphic() == null) return;
            if (selected) getGraphic().getStyleClass().add("user-card-selected");
            else getGraphic().getStyleClass().remove("user-card-selected");
        }

        private static String roleClass(String role) {
            if (role == null) return "badge-user";
            return switch (role.toUpperCase()) {
                case "ADMIN" -> "badge-admin";
                case "ENTREPRISE" -> "badge-entreprise";
                default -> "badge-user";
            };
        }
    }
}
