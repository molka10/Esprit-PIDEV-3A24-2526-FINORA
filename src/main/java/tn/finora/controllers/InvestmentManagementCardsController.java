package tn.finora.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.finora.entities.InvestmentManagement;
import tn.finora.finorainves.AppState;
import tn.finora.finorainves.SceneNavigator;
import tn.finora.services.InvestmentManagementService;

import java.util.List;

public class InvestmentManagementCardsController {

    @FXML
    private FlowPane cardsPane;

    private final InvestmentManagementService service = new InvestmentManagementService();

    @FXML
    public void initialize() {
        loadCards();
    }

    // ================= Navigation =================

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

    // ================= Load Cards =================

    private void loadCards() {
        cardsPane.getChildren().clear();
        List<InvestmentManagement> list = service.getAll();
        for (InvestmentManagement m : list) {
            cardsPane.getChildren().add(buildCard(m));
        }
    }

    // ================= Build Card =================

    private VBox buildCard(InvestmentManagement m) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPrefWidth(320);

        Label title = new Label("Management #" + m.getManagementId());
        title.getStyleClass().add("card-title");

        Label l1 = new Label("Investment ID: " + m.getInvestmentId());
        Label lName = new Label("Investment Name: " + safe(m.getInvestmentName()));
        Label l2 = new Label("Type: " + safe(m.getInvestmentType()));
        Label l3 = new Label("Amount: " + (m.getAmountInvested() != null ? m.getAmountInvested().toPlainString() : "-"));
        Label l4 = new Label("Ownership: " + (m.getOwnershipPercentage() != null ? m.getOwnershipPercentage().toPlainString() + " %" : "-"));
        Label l5 = new Label("Start Date: " + (m.getStartDate() != null ? m.getStartDate().toString() : "-"));
        Label l6 = new Label("Status: " + safe(m.getStatus()));

        // Buttons Edit & Delete
        Button editBtn = new Button("✏ Edit");
        editBtn.getStyleClass().add("btn-secondary");
        editBtn.setOnAction(e -> {
            AppState.setSelectedManagement(m);
            SceneNavigator.goTo("investment_management_form.fxml", "Edit Management");
        });

        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("btn-secondary");
        deleteBtn.setOnAction(e -> onDelete(m));

        VBox actions = new VBox(6, editBtn, deleteBtn);

        card.getChildren().addAll(title, l1, lName, l2, l3, l4, l5, l6, actions);
        return card;
    }

    // ================= Delete =================

    private void onDelete(InvestmentManagement m) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Management ID = " + m.getManagementId() + " ?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                service.delete(m.getManagementId());
                loadCards();
            }
        });
    }

    // ================= Safe String =================

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}