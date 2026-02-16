package tn.finora.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.finora.entities.Investment;
import tn.finora.finorainves.AppState;
import tn.finora.finorainves.SceneNavigator;
import tn.finora.services.InvestmentService;

import java.util.List;

public class InvestmentCardsController {

    @FXML private FlowPane cardsPane;

    private final InvestmentService service = new InvestmentService();

    @FXML
    public void initialize() {
        loadCards();
    }

    @FXML
    private void onAdd() {
        AppState.setEditingInvestment(null); // ✅ ADD mode
        SceneNavigator.goTo("investment_form.fxml", "Investment - Form");
    }

    @FXML
    private void onRefresh() {
        loadCards();
    }

    @FXML
    private void goToManagement() {
        SceneNavigator.goTo("investment_management_cards.fxml", "Investment Management - List");
    }

    private void loadCards() {
        cardsPane.getChildren().clear();

        List<Investment> list = service.getAll();
        for (Investment inv : list) {
            cardsPane.getChildren().add(buildCard(inv));
        }
    }

    private VBox buildCard(Investment inv) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPrefWidth(310);

        Label title = new Label(safe(inv.getName()));
        title.getStyleClass().add("card-title");

        Label meta = new Label(
                safe(inv.getCategory()) + " • " +
                        safe(inv.getLocation()) + " • " +
                        safe(inv.getRiskLevel())
        );

        Label value = new Label("Estimated: " + (inv.getEstimatedValue() != null ? inv.getEstimatedValue() : "-"));

        Button editBtn = new Button("✏ Edit");
        editBtn.getStyleClass().add("btn-secondary");
        editBtn.setOnAction(e -> {
            AppState.setEditingInvestment(inv); // ✅ EDIT mode
            SceneNavigator.goTo("investment_form.fxml", "Investment - Edit");
        });

        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("btn-secondary");
        deleteBtn.setOnAction(e -> {
            service.delete(inv.getInvestmentId());
            loadCards();
        });

        VBox actions = new VBox(6, editBtn, deleteBtn);

        card.getChildren().addAll(title, meta, value, actions);
        return card;
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
