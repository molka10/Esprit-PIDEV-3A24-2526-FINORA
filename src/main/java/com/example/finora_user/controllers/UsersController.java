package com.example.finora_user.controllers;

import com.example.finora_user.entities.User;
import com.example.finora_user.services.RiskResult;
import com.example.finora_user.services.RiskScoringService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersController {

    // LIST
    @FXML private ListView<User> usersList;
    @FXML private Label countLabel;

    // DETAILS
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private DatePicker dobPicker;

    @FXML private Label statusLabel;

    // SEARCH
    @FXML private ToggleGroup searchModeGroup;
    @FXML private ToggleButton toggleText;
    @FXML private ToggleButton toggleRole;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterCombo;

    private UserService userService;
    private RiskScoringService riskService;

    private final ObservableList<User> data = FXCollections.observableArrayList();
    private final Map<Integer, RiskResult> riskCache = new HashMap<>();

    private User selectedUser;

    private enum SearchMode { TEXT, ROLE }
    private SearchMode mode = SearchMode.TEXT;

    private static final String DEFAULT_PASSWORD = "ChangeMe123!";

    @FXML
    public void initialize() {
        try {
            userService = new UserService();
            riskService = new RiskScoringService();

            roleCombo.setItems(FXCollections.observableArrayList("ADMIN", "ENTREPRISE", "USER"));
            roleFilterCombo.setItems(FXCollections.observableArrayList("ADMIN", "ENTREPRISE", "USER"));

            usersList.setItems(data);
            usersList.setCellFactory(lv -> new UserCardCell(riskCache));

            usersList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                selectedUser = newV;
                if (newV != null) loadUserToForm(newV);
            });

            // default = TEXT search
            if (toggleText != null) toggleText.setSelected(true);
            handleSearchMode();

            handleRefresh();

        } catch (SQLException e) {
            statusLabel.setText("❌ DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // -------------------- CRUD --------------------
    @FXML
    private void handleRefresh() {
        try {
            List<User> users = userService.getAllUsers();
            data.setAll(users);
            recomputeRisk(users);

            updateCount();
            statusLabel.setText("");

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd() {
        try {
            String err = validateForm();
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

            u.setMotDePasse(DEFAULT_PASSWORD);
            userService.addUserReturnId(u);

            statusLabel.setText("✅ Utilisateur ajouté. MDP défaut: " + DEFAULT_PASSWORD);
            handleRefresh();
            handleClear();

        } catch (SQLException e) {
            statusLabel.setText("❌ DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            if (selectedUser == null) {
                statusLabel.setText("⚠️ Sélectionnez un utilisateur.");
                return;
            }

            String err = validateForm();
            if (err != null) {
                statusLabel.setText(err);
                return;
            }

            selectedUser.setUsername(usernameField.getText().trim());
            selectedUser.setEmail(emailField.getText().trim());
            selectedUser.setRole(roleCombo.getValue());
            selectedUser.setPhone(safe(phoneField.getText()));
            selectedUser.setAddress(addressField.getText().trim());
            selectedUser.setDateOfBirth(dobPicker.getValue());

            boolean ok = userService.updateUser(selectedUser);
            statusLabel.setText(ok ? "✅ Utilisateur modifié." : "⚠️ Modification échouée.");

            handleRefresh();

        } catch (SQLException e) {
            statusLabel.setText("❌ DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        try {
            if (selectedUser == null) {
                statusLabel.setText("⚠️ Sélectionnez un utilisateur.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer cet utilisateur ?");
            confirm.setContentText("Cette action est irréversible.");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            boolean ok = userService.deleteUser(selectedUser.getId());
            statusLabel.setText(ok ? "✅ Utilisateur supprimé." : "⚠️ Suppression échouée.");

            handleRefresh();
            handleClear();

        } catch (SQLException e) {
            statusLabel.setText("❌ DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        usersList.getSelectionModel().clearSelection();
        selectedUser = null;

        usernameField.clear();
        emailField.clear();
        roleCombo.setValue(null);
        phoneField.clear();
        addressField.clear();
        dobPicker.setValue(null);

        statusLabel.setText("");
    }

    private void loadUserToForm(User u) {
        usernameField.setText(u.getUsername());
        emailField.setText(u.getEmail());
        roleCombo.setValue(u.getRole());
        phoneField.setText(u.getPhone() == null ? "" : u.getPhone());
        addressField.setText(u.getAddress() == null ? "" : u.getAddress());
        dobPicker.setValue(u.getDateOfBirth());
    }

    // -------------------- SEARCH --------------------
    @FXML
    private void handleSearchMode() {
        Toggle t = (searchModeGroup == null) ? null : searchModeGroup.getSelectedToggle();

        if (t == toggleRole) {
            mode = SearchMode.ROLE;

            // disable text field, show role combo
            searchField.setDisable(true);
            searchField.clear();

            roleFilterCombo.setVisible(true);
            roleFilterCombo.setManaged(true);

            if (roleFilterCombo.getValue() == null) roleFilterCombo.setValue("USER");
            handleRoleFilter();

        } else {
            mode = SearchMode.TEXT;

            searchField.setDisable(false);
            searchField.setPromptText("Rechercher par username/email...");
            searchField.clear();

            roleFilterCombo.setVisible(false);
            roleFilterCombo.setManaged(false);

            handleRefresh();
        }
    }

    @FXML
    private void handleSearchDynamic() {
        try {
            if (mode != SearchMode.TEXT) return;

            String q = safe(searchField.getText());
            if (q.isEmpty()) {
                handleRefresh();
                return;
            }

            List<User> users = userService.searchText(q);
            data.setAll(users);
            recomputeRisk(users);
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
            if (role == null || role.isBlank()) {
                handleRefresh();
                return;
            }

            List<User> users = userService.searchByRole(role);
            data.setAll(users);
            recomputeRisk(users);
            updateCount();

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur filtre: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateCount() {
        if (countLabel != null) countLabel.setText(data.size() + " utilisateurs");
    }

    // -------------------- RISK SCORING --------------------
    private void recomputeRisk(List<User> users) {
        riskCache.clear();

        if (users == null || users.isEmpty()) {
            usersList.refresh();
            return;
        }

        for (User u : users) {
            try {
                riskCache.put(u.getId(), riskService.compute(u));
            } catch (Exception ex) {
                // don’t break UI
                riskCache.put(u.getId(), new RiskResult(0, RiskResult.Level.LOW, List.of("Impossible de calculer le risque")));
            }
        }

        usersList.refresh();
    }

    // -------------------- VALIDATION --------------------
    private String validateForm() {
        String username = safe(usernameField.getText());
        String email = safe(emailField.getText());
        String role = roleCombo.getValue();
        String phone = safe(phoneField.getText());
        String address = safe(addressField.getText());
        LocalDate dob = dobPicker.getValue();

        if (username.isEmpty() || email.isEmpty() || role == null || role.isBlank() || address.isEmpty()) {
            return "⚠️ Champs obligatoires: username, email, role, adresse.";
        }

        if (!username.matches("^[A-Za-z0-9_]{3,20}$")) {
            return "⚠️ Username invalide (3-20 caractères, lettres/chiffres/_).";
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return "⚠️ Email invalide.";
        }

        if (!phone.isEmpty() && !phone.matches("^\\d{8,15}$")) {
            return "⚠️ Téléphone invalide (8 à 15 chiffres).";
        }

        if (dob != null && dob.isAfter(LocalDate.now().minusYears(18))) {
            return "⚠️ L'utilisateur doit avoir au moins 18 ans.";
        }

        return null;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    // -------------------- CARD CELL (NO ID + RISK BADGE) --------------------
    private static class UserCardCell extends ListCell<User> {

        private final Map<Integer, RiskResult> riskCache;

        private UserCardCell(Map<Integer, RiskResult> riskCache) {
            this.riskCache = riskCache;
        }

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

            VBox right = new VBox(8);
            right.setMinWidth(170);
            right.setMaxWidth(170);

            // Role badge
            Label role = new Label(user.getRole());
            role.getStyleClass().addAll("badge", roleClass(user.getRole()));

            // Risk badge
            Label risk = buildRiskBadge(user);

            right.getChildren().addAll(role, risk);

            card.getChildren().addAll(left, spacer, right);

            setText(null);
            setGraphic(card);

            Node g = getGraphic();
            if (isSelected()) g.getStyleClass().add("user-card-selected");
            else g.getStyleClass().remove("user-card-selected");
        }

        private Label buildRiskBadge(User user) {
            RiskResult rr = (user == null) ? null : riskCache.get(user.getId());

            Label risk = new Label();
            risk.getStyleClass().add("risk-badge");

            if (rr == null) {
                risk.setText("LOW • 0");
                risk.getStyleClass().add("risk-low");
                return risk;
            }

            risk.setText(rr.level().name() + " • " + rr.score());

            switch (rr.level()) {
                case LOW -> risk.getStyleClass().add("risk-low");
                case MEDIUM -> risk.getStyleClass().add("risk-medium");
                case HIGH -> risk.getStyleClass().add("risk-high");
            }

            if (rr.reasons() != null && !rr.reasons().isEmpty()) {
                StringBuilder sb = new StringBuilder("Raisons:\n");
                for (String r : rr.reasons()) {
                    sb.append("• ").append(r).append("\n");
                }
                Tooltip tip = new Tooltip(sb.toString().trim());
                Tooltip.install(risk, tip);
            } else {
                Tooltip tip = new Tooltip("Aucun signal de risque détecté");
                Tooltip.install(risk, tip);
            }

            return risk;
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