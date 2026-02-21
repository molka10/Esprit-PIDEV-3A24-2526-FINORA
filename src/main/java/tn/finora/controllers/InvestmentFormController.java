package tn.finora.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.finora.entities.Investment;
import tn.finora.finorainves.AppState;
import tn.finora.finorainves.SceneNavigator;
import tn.finora.services.InvestmentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private Investment editing;

    @FXML
    public void initialize() {
        riskCombo.getItems().setAll("LOW", "MEDIUM", "HIGH");
        errorLabel.setText("");

        editing = AppState.getSelectedInvestment();

        if (editing != null) {
            titleLabel.setText("Edit Investment");
            nameField.setText(editing.getName());
            categoryField.setText(editing.getCategory());
            locationField.setText(editing.getLocation());
            BigDecimal estValue = editing.getEstimatedValue();
            valueField.setText(estValue != null ? estValue.toPlainString() : "0");
            riskCombo.setValue(editing.getRiskLevel());
            imageField.setText(editing.getImageUrl());
            descArea.setText(editing.getDescription());
        } else {
            titleLabel.setText("Add Investment");
            riskCombo.getSelectionModel().selectFirst();
            valueField.setText("0");
        }
    }

    @FXML
    private void onCancel() {
        AppState.setSelectedInvestment(null);
        SceneNavigator.goTo("investment_cards.fxml", "Investment - List");
    }

    @FXML
    private void onSave() {
        errorLabel.setText("");

        StringBuilder errors = new StringBuilder();

        // Name validation
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errors.append("❌ Name is required\n");
        } else if (name.length() > 50) {
            errors.append("❌ Name too long (max 50 chars)\n");
        }

        // Category validation
        String category = categoryField.getText().trim();
        if (category.isEmpty()) {
            errors.append("❌ Category is required\n");
        } else if (category.length() > 50) {
            errors.append("❌ Category too long (max 50 chars)\n");
        }

        // Location validation
        String location = locationField.getText().trim();
        if (location.isEmpty()) {
            errors.append("❌ Location is required\n");
        } else if (location.length() > 100) {
            errors.append("❌ Location too long (max 100 chars)\n");
        }

        // Estimated value validation
        BigDecimal estimatedValue = null;
        String valueText = valueField.getText().trim();
        if (valueText.isEmpty()) {
            errors.append("❌ Estimated value is required\n");
        } else {
            try {
                estimatedValue = new BigDecimal(valueText);
                if (estimatedValue.compareTo(BigDecimal.ZERO) < 0) {
                    errors.append("❌ Estimated value cannot be negative\n");
                }
            } catch (NumberFormatException ex) {
                errors.append("❌ Enter a valid number for estimated value\n");
            }
        }

        // Risk validation
        String risk = riskCombo.getValue();
        if (risk == null || risk.isEmpty()) {
            errors.append("❌ Select a risk level\n");
        } else if (!risk.equals("LOW") && !risk.equals("MEDIUM") && !risk.equals("HIGH")) {
            errors.append("❌ Invalid risk value\n");
        }

        // Image URL validation (optionnel)
        String imageUrl = imageField.getText().trim();
        if (!imageUrl.isEmpty()) {
            if (imageUrl.length() > 255) {
                errors.append("❌ Image URL too long (max 255 chars)\n");
            } else if (!imageUrl.matches("^(https?://.*|.*\\.(png|jpg|jpeg|gif))$")) {
                errors.append("❌ Invalid image URL or path\n");
            }
        }

        // Description validation (optionnel)
        String description = descArea.getText().trim();
        if (description.length() > 500) {
            errors.append("❌ Description too long (max 500 chars)\n");
        }

        // If any error, show and stop
        if (errors.length() > 0) {
            errorLabel.setText(errors.toString());
            return;
        }

        // Everything is valid → create or edit Investment
        Investment inv = (editing != null) ? editing : new Investment();
        inv.setName(name);
        inv.setCategory(category);
        inv.setLocation(location);
        inv.setEstimatedValue(estimatedValue);
        inv.setRiskLevel(risk);
        inv.setImageUrl(imageUrl);
        inv.setDescription(description);

        try {
            if (editing == null) {
                inv.setCreatedAt(LocalDateTime.now());
                service.add(inv);
            } else {
                service.update(inv);
            }
            AppState.setSelectedInvestment(null);
            SceneNavigator.goTo("investment_cards.fxml", "Investment - List");
        } catch (Exception ex) {
            errorLabel.setText("❌ Error saving investment: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}