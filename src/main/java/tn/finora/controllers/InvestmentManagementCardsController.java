package tn.finora.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.chart.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.finora.entities.InvestmentManagement;
import tn.finora.finorainves.AppState;
import tn.finora.finorainves.SceneNavigator;
import tn.finora.services.InvestmentManagementService;
import javafx.scene.control.Label;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvestmentManagementCardsController {

    @FXML
    private FlowPane cardsPane;

    @FXML
    private PieChart statusPieChart;

    @FXML
    private BarChart<String, Number> amountBarChart;

    @FXML
    private CategoryAxis typeAxis;

    @FXML
    private NumberAxis amountAxis;

    private final InvestmentManagementService service = new InvestmentManagementService();

    // ================= INITIALIZE =================
    @FXML
    public void initialize() {
        loadCards();
    }

    // ================= NAVIGATION =================
    @FXML
    private void onAdd() {
        AppState.setSelectedManagement(null);
        SceneNavigator.goTo("investment_management_form.fxml", "Add Investment Management");
    }

    @FXML
    private void onRefresh() {
        loadCards();
    }

    @FXML
    private void goToInvestments() {
        SceneNavigator.goTo("investment_cards.fxml", "Investments");
    }

    // ================= LOAD CARDS =================
    private void loadCards() {
        cardsPane.getChildren().clear();
        List<InvestmentManagement> list = service.getAll();
        for (InvestmentManagement m : list) {
            cardsPane.getChildren().add(buildCard(m));
        }
        loadStatistics(list); // mettre à jour les graphiques
    }

    // ================= BUILD CARD =================
    private VBox buildCard(InvestmentManagement m) {
        VBox card = new VBox(8);
        card.getStyleClass().add("management-card");

        Label title = new Label("Management #" + m.getManagementId());
        title.getStyleClass().add("management-title");

        Label l1 = new Label("Investment ID: " + m.getInvestmentId());
        Label lName = new Label("Investment Name: " + safe(m.getInvestmentName()));
        Label l2 = new Label("Type: " + safe(m.getInvestmentType()));
        Label l3 = new Label("Amount: " + (m.getAmountInvested() != null ? m.getAmountInvested().toPlainString() : "-"));
        Label l4 = new Label("Ownership: " + (m.getOwnershipPercentage() != null ? m.getOwnershipPercentage().toPlainString() + " %" : "-"));
        Label l5 = new Label("Start Date: " + (m.getStartDate() != null ? m.getStartDate().toString() : "-"));
        Label l6 = new Label("Status: " + safe(m.getStatus()));

        // Appliquer style label
        l1.getStyleClass().add("management-label");
        lName.getStyleClass().add("management-label");
        l2.getStyleClass().add("management-label");
        l3.getStyleClass().add("management-label");
        l4.getStyleClass().add("management-label");
        l5.getStyleClass().add("management-label");
        l6.getStyleClass().add("management-label");

        // Buttons Edit & Delete
        Button editBtn = new Button("✏ Edit");
        editBtn.getStyleClass().add("btn-edit");
        editBtn.setOnAction(e -> {
            AppState.setSelectedManagement(m);
            SceneNavigator.goTo("investment_management_form.fxml", "Edit Management");
        });

        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.setOnAction(e -> onDelete(m));

        HBox actions = new HBox(8, editBtn, deleteBtn);

        card.getChildren().addAll(title, l1, lName, l2, l3, l4, l5, l6, actions);
        return card;
    }

    // ================= DELETE =================
    private void onDelete(InvestmentManagement m) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Management ID = " + m.getManagementId() + " ?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                service.delete(m.getManagementId());
                loadCards(); // refresh cards and stats
            }
        });
    }

    // ================= SAFE STRING =================
    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    // ================= STATISTICS =================
    private void loadStatistics(List<InvestmentManagement> list) {
        // PieChart: répartition par status
        long activeCount = list.stream()
                .filter(m -> "Active".equalsIgnoreCase(m.getStatus()))
                .count();

        long closedCount = list.stream()
                .filter(m -> "Closed".equalsIgnoreCase(m.getStatus()))
                .count();

        statusPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Active", activeCount),
                new PieChart.Data("Closed", closedCount)
        ));

        // BarChart: montant total par type
        Map<String, Double> totalByType = new HashMap<>();
        for (InvestmentManagement m : list) {
            String type = safe(m.getInvestmentType());
            double amount = m.getAmountInvested() != null ? m.getAmountInvested().doubleValue() : 0;
            totalByType.put(type, totalByType.getOrDefault(type, 0.0) + amount);
        }

        amountBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Invested");
        totalByType.forEach((type, total) -> series.getData().add(new XYChart.Data<>(type, total)));
        amountBarChart.getData().add(series);
    }
}