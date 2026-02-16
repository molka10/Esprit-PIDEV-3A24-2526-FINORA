package tn.finora.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.finora.entities.InvestmentManagement;
import tn.finora.finorainves.AppState;
import tn.finora.finorainves.SceneNavigator;
import tn.finora.services.InvestmentManagementService;

import java.util.List;

public class InvestmentManagementCardsController {

    @FXML private FlowPane cardsPane;

    private final InvestmentManagementService service = new InvestmentManagementService();

    @FXML
    public void initialize() {
        System.out.println("InvestmentManagementCardsController loaded ✅");
        loadCards();
    }

    @FXML
    private void onAdd() {
        System.out.println("CLICK Add Management ✅");
        AppState.setSelectedManagement(null); // ✅ ADD mode
        SceneNavigator.goTo("investment_management_form.fxml", "Investment Management - Form");
    }

    @FXML
    private void onRefresh() {
        System.out.println("CLICK Refresh ✅");
        loadCards();
    }

    @FXML
    private void goToInvestments() {
        System.out.println("CLICK Back ✅");
        SceneNavigator.goTo("investment_cards.fxml", "Investment - Cards");
    }

    private void loadCards() {
        cardsPane.getChildren().clear();

        List<InvestmentManagement> list = service.getAll();
        System.out.println("MGMT COUNT = " + list.size());

        for (InvestmentManagement m : list) {
            cardsPane.getChildren().add(buildCard(m));
        }
    }

    private VBox buildCard(InvestmentManagement m) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPrefWidth(310);

        Label title = new Label("Management #" + m.getManagementId());
        title.getStyleClass().add("card-title");

        Label l1 = new Label("🔗 Investment ID: " + m.getInvestmentId());
        Label l2 = new Label("📌 Type: " + safe(m.getInvestmentType()));
        Label l3 = new Label("💰 Amount: " + (m.getAmountInvested() != null ? m.getAmountInvested().toPlainString() : "-"));
        Label l4 = new Label("📊 Ownership: " + (m.getOwnershipPercentage() != null ? m.getOwnershipPercentage().toPlainString() + "%" : "-"));
        Label l5 = new Label("📅 Start: " + (m.getStartDate() != null ? m.getStartDate() : "-"));
        Label l6 = new Label("✅ Status: " + safe(m.getStatus()));

        Button editBtn = new Button("✏ Edit");
        editBtn.getStyleClass().add("btn-secondary");
        editBtn.setOnAction(e -> {
            AppState.setSelectedManagement(m); // ✅ EDIT mode
            SceneNavigator.goTo("investment_management_form.fxml", "Management - Edit");
        });

        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("btn-secondary");
        deleteBtn.setOnAction(e -> onDelete(m));

        VBox actions = new VBox(6, editBtn, deleteBtn);

        card.getChildren().addAll(title, l1, l2, l3, l4, l5, l6, actions);
        return card;
    }

    private void onDelete(InvestmentManagement m) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm delete");
        alert.setHeaderText("Delete Management ID = " + m.getManagementId() + " ?");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                service.delete(m.getManagementId());
                loadCards();
            }
        });
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
