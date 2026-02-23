package tn.finora.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import tn.finora.entities.Investment;
import tn.finora.utils.DBConnection;
import tn.finora.finorainves.AppState;
import tn.finora.finorainves.SceneNavigator;

import java.math.BigDecimal;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InvestmentCardsController {

    @FXML
    private FlowPane cardsPane;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> riskFilter;

    private final Connection cnx;
    private List<Investment> allInvestments;

    public InvestmentCardsController() {
        cnx = DBConnection.getInstance().getCnx();
    }

    @FXML
    public void initialize() {
        allInvestments = getAll();
        loadCards(allInvestments);

        riskFilter.setItems(FXCollections.observableArrayList("All", "Low", "Medium", "High"));
        riskFilter.setValue("All");
    }

    // ================= FXML BUTTON HANDLERS =================
    @FXML
    private void onAdd() {
        try {
            SceneNavigator.goTo("investment_form.fxml", "Add Investment");
        } catch (RuntimeException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void onRefresh() {
        allInvestments = getAll();
        loadCards(allInvestments);
        searchField.clear();
        riskFilter.setValue("All");
    }

    @FXML
    private void goToManagement() {
        try {
            SceneNavigator.goTo("investment_management_cards.fxml", "Investment Management");
        } catch (RuntimeException e) {
            showAlert("Erreur", "Impossible de charger Investment Management: " + e.getMessage());
        }
    }

    // ================= LOAD CARDS =================
    private void loadCards(List<Investment> investments) {
        cardsPane.getChildren().clear();
        for (Investment inv : investments) {
            cardsPane.getChildren().add(createCard(inv));
        }
    }

    private VBox createCard(Investment inv) {
        VBox card = new VBox(10);
        card.setPrefWidth(220);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-padding: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);"
        );

        // Hover effect sur toute la card
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #f0f0ff;" +
                        "-fx-padding: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 0);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-padding: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);"
        ));

        // ------------------ IMAGE ------------------
        ImageView imgView = new ImageView();
        imgView.setFitWidth(200);
        imgView.setFitHeight(120);
        imgView.setPreserveRatio(true);
        if (inv.getImageUrl() != null && !inv.getImageUrl().isEmpty()) {
            try {
                imgView.setImage(new Image(inv.getImageUrl(), true));
            } catch (Exception ignored) {}
        }

        // ------------------ LABELS ------------------
        Label nameLabel = new Label(inv.getName());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #4B0082;");

        Label infoLabel = new Label(inv.getCategory() + " | " + inv.getLocation());
        infoLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #555555;");

        Label valueLabel = new Label("Value: " + inv.getEstimatedValue());
        valueLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #333333;");

        Label riskLabel = new Label(inv.getRiskLevel());
        riskLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 8;");
        switch (inv.getRiskLevel().toLowerCase()) {
            case "low" -> riskLabel.setStyle(riskLabel.getStyle() + "-fx-background-color: #28a745;");
            case "medium" -> riskLabel.setStyle(riskLabel.getStyle() + "-fx-background-color: #ffc107;");
            case "high" -> riskLabel.setStyle(riskLabel.getStyle() + "-fx-background-color: #dc3545;");
        }

        Label descLabel = new Label(inv.getDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666666;");

        String created = "";
        if (inv.getCreatedAt() != null)
            created = inv.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Label createdLabel = new Label("Created: " + created);
        createdLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #999999;");

        // ------------------ BUTTONS ------------------
        Button updateBtn = new Button("✏ Update");
        updateBtn.setStyle(
                "-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-padding: 5 10;"
        );
        updateBtn.setOnAction(e -> {
            AppState.setSelectedInvestment(inv);
            SceneNavigator.goTo("investment_form.fxml", "Edit Investment");
        });

        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.setStyle(
                "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-padding: 5 10;"
        );
        deleteBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete this investment?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    delete(inv.getInvestmentId());
                    allInvestments = getAll();
                    loadCards(allInvestments);
                }
            });
        });

        Button detailsBtn = new Button("👁 Details");
        detailsBtn.setStyle(
                "-fx-background-color: #4B0082; -fx-text-fill: white; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-padding: 5 10;"
        );
        detailsBtn.setOnMouseEntered(e -> detailsBtn.setStyle(
                "-fx-background-color: #6A0DAD; -fx-text-fill: white; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-padding: 5 10;"
        ));
        detailsBtn.setOnMouseExited(e -> detailsBtn.setStyle(
                "-fx-background-color: #4B0082; -fx-text-fill: white; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-padding: 5 10;"
        ));
        detailsBtn.setOnAction(e -> {
            AppState.setSelectedInvestment(inv);
            SceneNavigator.goTo("investment_details.fxml", "Investment Details");
        });

        HBox buttonsBox = new HBox(10, updateBtn, deleteBtn, detailsBtn);

        // ------------------ ASSEMBLY ------------------
        card.getChildren().addAll(imgView, nameLabel, infoLabel, valueLabel, riskLabel, descLabel, createdLabel, buttonsBox);

        return card;
    }

    // ================= CRUD =================
    public int add(Investment inv) {
        String sql = "INSERT INTO investment (name, category, location, estimated_value, risk_level, image_url, description, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, inv.getName());
            ps.setString(2, inv.getCategory());
            ps.setString(3, inv.getLocation());
            ps.setBigDecimal(4, inv.getEstimatedValue() != null ? inv.getEstimatedValue() : BigDecimal.ZERO);
            ps.setString(5, inv.getRiskLevel());
            ps.setString(6, inv.getImageUrl());
            ps.setString(7, inv.getDescription());
            ps.setTimestamp(8, inv.getCreatedAt() != null ? Timestamp.valueOf(inv.getCreatedAt()) : new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible d'ajouter l'investment: " + e.getMessage());
            return -1;
        }
    }

    public void update(Investment inv) {
        String sql = "UPDATE investment SET name=?, category=?, location=?, estimated_value=?, risk_level=?, image_url=?, description=? WHERE investment_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, inv.getName());
            ps.setString(2, inv.getCategory());
            ps.setString(3, inv.getLocation());
            ps.setBigDecimal(4, inv.getEstimatedValue() != null ? inv.getEstimatedValue() : BigDecimal.ZERO);
            ps.setString(5, inv.getRiskLevel());
            ps.setString(6, inv.getImageUrl());
            ps.setString(7, inv.getDescription());
            ps.setInt(8, inv.getInvestmentId());
            ps.executeUpdate();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de mettre à jour l'investment: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM investment WHERE investment_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de supprimer l'investment: " + e.getMessage());
        }
    }

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
                if (t != null) inv.setCreatedAt(t.toLocalDateTime());

                list.add(inv);
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les investments: " + e.getMessage());
        }

        return list;
    }

    // ================= SEARCH & FILTER =================
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
        String query = searchField.getText().trim().toLowerCase();
        String selectedRisk = riskFilter.getValue();

        List<Investment> filtered = new ArrayList<>(allInvestments);

        if (!query.isEmpty()) {
            filtered.removeIf(inv -> inv.getName() == null || !inv.getName().toLowerCase().contains(query));
        }

        if (selectedRisk != null && !"All".equals(selectedRisk)) {
            filtered.removeIf(inv -> inv.getRiskLevel() == null || !inv.getRiskLevel().equalsIgnoreCase(selectedRisk));
        }

        loadCards(filtered);
    }

    // ================= UTILS =================
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}