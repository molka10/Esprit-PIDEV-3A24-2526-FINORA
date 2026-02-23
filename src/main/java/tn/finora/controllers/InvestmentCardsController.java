package tn.finora.controllers;

import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import tn.finora.entities.Investment;
import tn.finora.finorainves.AppState;
import tn.finora.finorainves.SceneNavigator;
import tn.finora.utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InvestmentCardsController {

    @FXML private FlowPane cardsPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> riskFilter;
    @FXML private Label totalValueLabel;
    @FXML private Label totalCountLabel;

    private final Connection cnx;
    private List<Investment> allInvestments = new ArrayList<>();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(Locale.US);

    public InvestmentCardsController() {
        cnx = DBConnection.getInstance().getCnx();
    }

    @FXML
    public void initialize() {
        riskFilter.setItems(
                FXCollections.observableArrayList("All", "Low", "Medium", "High")
        );
        riskFilter.setValue("All");
        refreshData();
    }

    // =====================================================
    // NAVIGATION
    // =====================================================

    @FXML
    private void onAdd() {
        SceneNavigator.goTo("investment_form.fxml", "Add Investment");
    }

    @FXML
    private void onRefresh() {
        searchField.clear();
        riskFilter.setValue("All");
        refreshData();
    }

    @FXML
    private void goToManagement() {
        SceneNavigator.goTo("investment_management_cards.fxml",
                "Investment Management");
    }

    @FXML
    private void toggleDarkMode() {
        var root = cardsPane.getScene().getRoot();
        if (root.getStyleClass().contains("dark-root")) {
            root.getStyleClass().remove("dark-root");
        } else {
            root.getStyleClass().add("dark-root");
        }
    }

    private void refreshData() {
        allInvestments = getAll();
        loadCards(allInvestments);
        updateDashboard();
    }

    // =====================================================
    // DASHBOARD
    // =====================================================

    private void updateDashboard() {

        BigDecimal total = BigDecimal.ZERO;

        for (Investment inv : allInvestments) {
            if (inv.getEstimatedValue() != null) {
                total = total.add(inv.getEstimatedValue());
            }
        }

        totalValueLabel.setText(currencyFormat.format(total));
        totalCountLabel.setText(String.valueOf(allInvestments.size()));
    }

    // =====================================================
    // LOAD CARDS
    // =====================================================

    private void loadCards(List<Investment> investments) {

        cardsPane.getChildren().clear();

        if (investments.isEmpty()) {
            Label empty = new Label("No investments found.");
            empty.getStyleClass().add("empty-label");
            cardsPane.getChildren().add(empty);
            return;
        }

        for (Investment inv : investments) {
            cardsPane.getChildren().add(createCard(inv));
        }
    }
// =====================================================
// CARD HOVER ANIMATION
// =====================================================

    private void addSmoothHoverAnimation(VBox card) {

        ScaleTransition scaleUp =
                new ScaleTransition(Duration.millis(180), card);
        scaleUp.setToX(1.03);
        scaleUp.setToY(1.03);

        ScaleTransition scaleDown =
                new ScaleTransition(Duration.millis(180), card);
        scaleDown.setToX(1);
        scaleDown.setToY(1);

        card.setOnMouseEntered(e -> scaleUp.playFromStart());
        card.setOnMouseExited(e -> scaleDown.playFromStart());
    }
    private VBox createCard(Investment inv) {

        VBox card = new VBox();
        card.getStyleClass().add("investment-card");
        card.setPrefWidth(280);

        // ===== IMAGE =====
        ImageView image = new ImageView();
        image.setFitWidth(280);
        image.setFitHeight(160);
        image.setPreserveRatio(false);
        image.getStyleClass().add("card-image-modern");

        if (inv.getImageUrl() != null && !inv.getImageUrl().isBlank()) {
            image.setImage(new Image(inv.getImageUrl(), true));
        }

        // ===== TITLE =====
        Label nameLabel = new Label(inv.getName());
        nameLabel.getStyleClass().add("card-title");

        Label categoryLabel = new Label(
                safe(inv.getCategory()) + " • " + safe(inv.getLocation())
        );
        categoryLabel.getStyleClass().add("card-subtitle");

        // ===== VALUE + CURRENCY =====
        Label valueLabel = new Label();
        valueLabel.getStyleClass().add("card-value");

        ComboBox<String> currencyCombo = new ComboBox<>();
        currencyCombo.getItems().addAll("USD", "EUR", "TND");
        currencyCombo.setValue("USD");
        currencyCombo.setPrefWidth(90);

        updateCardValue(valueLabel, inv.getEstimatedValue(), "USD");

        currencyCombo.setOnAction(e ->
                updateCardValue(valueLabel,
                        inv.getEstimatedValue(),
                        currencyCombo.getValue())
        );

        HBox valueBox = new HBox(10, valueLabel, currencyCombo);
        valueBox.setAlignment(Pos.CENTER_LEFT);

        // ===== RISK =====
        Label riskLabel = new Label(
                safe(inv.getRiskLevel()).toUpperCase()
        );
        riskLabel.getStyleClass().add("risk-badge");

        switch (safe(inv.getRiskLevel()).toLowerCase()) {
            case "low" -> riskLabel.getStyleClass().add("badge-low-modern");
            case "medium" -> riskLabel.getStyleClass().add("badge-medium-modern");
            case "high" -> riskLabel.getStyleClass().add("badge-high-modern");
        }

        // ===== TOP INVESTMENT BADGE =====
        Label topBadge = null;
        if (isTopInvestment(inv)) {
            topBadge = new Label("🏆 Top Investment");
            topBadge.getStyleClass().add("top-badge");
        }

        // ===== BUTTONS =====
        Button updateBtn = new Button("Update");
        updateBtn.getStyleClass().add("btn-update-modern");
        updateBtn.setPrefWidth(130);
        updateBtn.setPrefHeight(42);

        updateBtn.setOnAction(e -> {
            AppState.setSelectedInvestment(inv);
            SceneNavigator.goTo("investment_form.fxml", "Update Investment");
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("btn-delete-modern");
        deleteBtn.setPrefWidth(130);
        deleteBtn.setPrefHeight(42);

        deleteBtn.setOnAction(e -> confirmDelete(inv));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttons = new HBox(18, updateBtn, spacer, deleteBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setStyle("-fx-padding: 18 0 0 0;");

        VBox content = new VBox(12,
                nameLabel,
                categoryLabel,
                valueBox,
                riskLabel,
                topBadge != null ? topBadge : new Pane(),
                buttons
        );

        content.setStyle("-fx-padding: 22;");
        card.getChildren().addAll(image, content);

        addSmoothHoverAnimation(card);

        return card;
    }

    // =====================================================
    // VALUE CONVERSION
    // =====================================================

    private void updateCardValue(Label label, BigDecimal value, String currency) {

        if (value == null) {
            label.setText(currency + " 0.00");
            return;
        }

        BigDecimal converted = value;

        switch (currency) {
            case "EUR":
                converted = value.multiply(BigDecimal.valueOf(0.92));
                break;
            case "TND":
                converted = value.multiply(BigDecimal.valueOf(3.10));
                break;
        }

        label.setText(currency + " " +
                converted.setScale(2, RoundingMode.HALF_UP));
    }

    private boolean isTopInvestment(Investment inv) {

        if (inv.getEstimatedValue() == null ||
                inv.getRiskLevel() == null)
            return false;

        BigDecimal value = inv.getEstimatedValue();
        String risk = inv.getRiskLevel().toLowerCase();

        return value.compareTo(new BigDecimal("500000")) > 0
                && (risk.equals("low") || risk.equals("medium"));
    }

    // =====================================================
    // DELETE
    // =====================================================

    private void confirmDelete(Investment inv) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Investment");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this investment?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                delete(inv.getInvestmentId());
                refreshData();
            }
        });
    }

    // =====================================================
    // DATABASE
    // =====================================================

    public List<Investment> getAll() {

        List<Investment> list = new ArrayList<>();
        String sql = "SELECT * FROM investment ORDER BY investment_id DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Investment inv = new Investment();

                inv.setInvestmentId(rs.getInt("investment_id"));
                inv.setName(rs.getString("name"));
                inv.setCategory(rs.getString("category"));
                inv.setLocation(rs.getString("location"));
                inv.setEstimatedValue(rs.getBigDecimal("estimated_value"));
                inv.setRiskLevel(rs.getString("risk_level"));
                inv.setImageUrl(rs.getString("image_url"));
                inv.setDescription(rs.getString("description"));

                Timestamp t = rs.getTimestamp("created_at");
                if (t != null)
                    inv.setCreatedAt(t.toLocalDateTime());

                list.add(inv);
            }

        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        }

        return list;
    }

    public void delete(int id) {

        String sql = "DELETE FROM investment WHERE investment_id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            showError("Delete Error", e.getMessage());
        }
    }

    // =====================================================
    // SEARCH
    // =====================================================

    @FXML
    private void onSearch() {
        applyFilters();
    }

    @FXML
    private void onClearSearch() {
        searchField.clear();
        riskFilter.setValue("All");
        loadCards(allInvestments);
    }

    @FXML
    private void onFilterRisk() {
        applyFilters();
    }

    private void applyFilters() {

        String query = searchField.getText().toLowerCase().trim();
        String selectedRisk = riskFilter.getValue();

        List<Investment> filtered = new ArrayList<>(allInvestments);

        if (!query.isEmpty()) {
            filtered.removeIf(inv ->
                    inv.getName() == null ||
                            !inv.getName().toLowerCase().contains(query)
            );
        }

        if (selectedRisk != null && !"All".equals(selectedRisk)) {
            filtered.removeIf(inv ->
                    inv.getRiskLevel() == null ||
                            !inv.getRiskLevel().equalsIgnoreCase(selectedRisk)
            );
        }

        loadCards(filtered);
    }

    // =====================================================
    // UTIL
    // =====================================================

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}