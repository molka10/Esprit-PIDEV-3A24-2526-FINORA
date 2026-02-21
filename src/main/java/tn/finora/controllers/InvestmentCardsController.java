package tn.finora.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.finora.entities.Investment;
import tn.finora.utils.DBConnection;
import tn.finora.finorainves.AppState;
import tn.finora.finorainves.SceneNavigator;
import javafx.scene.control.ButtonType;

import java.math.BigDecimal;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InvestmentCardsController {

    @FXML
    private FlowPane cardsPane;

    private final Connection cnx;

    public InvestmentCardsController() {
        cnx = DBConnection.getInstance().getCnx();
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
        loadCards();
    }

    @FXML
    private void goToManagement() {
        try {
            SceneNavigator.goTo("investment_management_cards.fxml", "Investment Management");
        } catch (RuntimeException e) {
            showAlert("Erreur", "Impossible de charger Investment Management: " + e.getMessage());
        }
    }

    // ================= CARDS LOADING =================

    @FXML
    public void initialize() {
        loadCards();
    }

    private void loadCards() {
        cardsPane.getChildren().clear();
        List<Investment> investments = getAll();

        for (Investment inv : investments) {
            VBox card = createCard(inv);
            cardsPane.getChildren().add(card);
        }
    }

    private VBox createCard(Investment inv) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        // Image
        ImageView imgView = new ImageView();
        imgView.setFitWidth(150);
        imgView.setFitHeight(100);
        if (inv.getImageUrl() != null && !inv.getImageUrl().isEmpty()) {
            try {
                imgView.setImage(new Image(inv.getImageUrl(), true));
            } catch (Exception ignored) {}
        }

        // Labels
        Label nameLabel = new Label(inv.getName());
        nameLabel.getStyleClass().add("card-title");

        Label infoLabel = new Label(inv.getCategory() + " | " + inv.getLocation());
        Label valueLabel = new Label("Value: " + inv.getEstimatedValue());
        Label riskLabel = new Label("Risk: " + inv.getRiskLevel());
        Label descLabel = new Label(inv.getDescription());
        descLabel.setWrapText(true);

        String created = "";
        if (inv.getCreatedAt() != null)
            created = inv.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Label createdLabel = new Label("Created: " + created);

        // ================= BOUTONS =================
        Button updateBtn = new Button("✎ Update");
        Button deleteBtn = new Button("🗑 Delete");

        updateBtn.setOnAction(e -> {
            AppState.setSelectedInvestment(inv);
            SceneNavigator.goTo("investment_form.fxml", "Edit Investment");
        });

        deleteBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete this investment?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    delete(inv.getInvestmentId());
                    loadCards(); // rafraîchir les cartes
                }
            });
        });

        HBox buttonsBox = new HBox(10, updateBtn, deleteBtn);

        // Ajouter tous les éléments à la carte
        card.getChildren().addAll(imgView, nameLabel, infoLabel, valueLabel, riskLabel, descLabel, createdLabel, buttonsBox);

        return card;
    }

    // ================= CRUD =================

    public int add(Investment inv) {
        String sql = "INSERT INTO investment (name, category, location, estimated_value, risk_level, image_url, description, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

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

    // ================= UTILS =================

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}