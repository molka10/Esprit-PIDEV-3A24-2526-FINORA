package tn.finora.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.finora.entities.Investment;
import tn.finora.finorainves.AppState;
import tn.finora.finorainves.SceneNavigator;
import tn.finora.services.InvestmentService;

import java.math.BigDecimal;

public class InvestmentFormController {

    @FXML private Label titleLabel;

    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private TextField locationField;
    @FXML private TextField valueField;
    @FXML private ComboBox<String> riskCombo;
    @FXML private TextField imageField;
    @FXML private TextArea descArea;

    @FXML private Label errorLabel;

    private final InvestmentService service = new InvestmentService();
    private Investment editing; // ✅ if not null => edit mode

    @FXML
    public void initialize() {
        riskCombo.getItems().setAll("LOW", "MEDIUM", "HIGH");
        errorLabel.setText("");

        editing = AppState.getEditingInvestment();

        if (editing != null) {
            // ✅ EDIT MODE
            titleLabel.setText("Edit Investment");

            nameField.setText(editing.getName());
            categoryField.setText(editing.getCategory());
            locationField.setText(editing.getLocation());
            valueField.setText(editing.getEstimatedValue() != null ? editing.getEstimatedValue().toString() : "");
            riskCombo.setValue(editing.getRiskLevel() != null ? editing.getRiskLevel() : "LOW");
            imageField.setText(editing.getImageUrl());
            descArea.setText(editing.getDescription());

        } else {
            // ✅ ADD MODE
            titleLabel.setText("Add Investment");
            riskCombo.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void onSave() {
        try {
            errorLabel.setText("");

            String name = nameField.getText() == null ? "" : nameField.getText().trim();
            if (name.isEmpty()) {
                errorLabel.setText("❌ Name is required");
                return;
            }

            BigDecimal estimated;
            try {
                estimated = new BigDecimal(valueField.getText().trim());
            } catch (Exception ex) {
                errorLabel.setText("❌ Estimated Value must be a number (ex: 15000.50)");
                return;
            }

            Investment inv = (editing != null) ? editing : new Investment();

            inv.setName(name);
            inv.setCategory(trim(categoryField.getText()));
            inv.setLocation(trim(locationField.getText()));
            inv.setEstimatedValue(estimated);
            inv.setRiskLevel(riskCombo.getValue());
            inv.setImageUrl(trim(imageField.getText()));
            inv.setDescription(trim(descArea.getText()));

            if (editing == null) {
                service.add(inv);       // ✅ INSERT
            } else {
                service.update(inv);    // ✅ UPDATE
            }

            AppState.setEditingInvestment(null);
            SceneNavigator.goTo("investment_cards.fxml", "Investment - Cards");

        } catch (Exception e) {
            errorLabel.setText("❌ " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancel() {
        AppState.setEditingInvestment(null);
        SceneNavigator.goTo("investment_cards.fxml", "Investment - Cards");
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
