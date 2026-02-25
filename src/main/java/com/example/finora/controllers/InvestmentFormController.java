package com.example.finora.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.example.finora.entities.Investment;
import com.example.finora.finorainves.AppState;
import com.example.finora.finorainves.SceneNavigator;
import com.example.finora.services.InvestmentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvestmentFormController {

    @FXML private Label titleLabel;

    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private TextField locationField;
    @FXML private TextField valueField;

    @FXML private ComboBox<String> currencyCombo;   // 💱 NEW
    @FXML private ComboBox<String> riskCombo;

    @FXML private TextField imageField;
    @FXML private TextArea descArea;
    @FXML private ImageView previewImage;

    // Error labels
    @FXML private Label nameError;
    @FXML private Label categoryError;
    @FXML private Label locationError;
    @FXML private Label valueError;
    @FXML private Label riskError;
    @FXML private Label imageError;
    @FXML private Label descError;

    private final InvestmentService service = new InvestmentService();
    private Investment editing;

    @FXML
    public void initialize() {

        // Devise
        currencyCombo.getItems().addAll("USD", "EUR", "TND");
        currencyCombo.setValue("USD");

        // Risk
        riskCombo.getItems().setAll("LOW", "MEDIUM", "HIGH");
        riskCombo.getSelectionModel().selectFirst();

        editing = AppState.getSelectedInvestment();

        // Image Preview sécurisé
        imageField.textProperty().addListener((obs, oldVal, newVal) -> loadImagePreview(newVal));

        if (editing != null) {
            titleLabel.setText("Update Investment");
            loadEditingData();
        } else {
            titleLabel.setText("Add Investment");
            valueField.setText("0");
        }
    }

    private void loadEditingData() {

        nameField.setText(editing.getName());
        categoryField.setText(editing.getCategory());
        locationField.setText(editing.getLocation());

        if (editing.getEstimatedValue() != null) {
            valueField.setText(editing.getEstimatedValue().toPlainString());
        }

        riskCombo.setValue(editing.getRiskLevel());
        imageField.setText(editing.getImageUrl());
        descArea.setText(editing.getDescription());
    }

    private void loadImagePreview(String url) {

        try {
            if (url != null && url.startsWith("http")) {
                previewImage.setImage(new Image(url, true));
            } else {
                previewImage.setImage(null);
            }
        } catch (Exception e) {
            previewImage.setImage(null);
        }
    }

    @FXML
    private void onSave() {

        clearErrors();
        boolean valid = true;

        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String location = locationField.getText().trim();
        String imageUrl = imageField.getText().trim();
        String description = descArea.getText().trim();

        // NAME
        if (name.isEmpty()) {
            setError(nameField, nameError, "Name is required");
            valid = false;
        }

        // CATEGORY
        if (category.isEmpty()) {
            setError(categoryField, categoryError, "Category required");
            valid = false;
        }

        // LOCATION
        if (location.isEmpty()) {
            setError(locationField, locationError, "Location required");
            valid = false;
        }

        // VALUE
        BigDecimal value = null;
        try {
            value = new BigDecimal(valueField.getText().trim());
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                setError(valueField, valueError, "Must be greater than 0");
                valid = false;
            }
        } catch (Exception e) {
            setError(valueField, valueError, "Invalid number");
            valid = false;
        }

        // RISK
        String risk = riskCombo.getValue();
        if (risk == null) {
            riskError.setText("Select risk level");
            valid = false;
        }

        // IMAGE URL
        if (!imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
            setError(imageField, imageError, "Invalid URL");
            valid = false;
        }

        // DESCRIPTION
        if (description.length() > 500) {
            descError.setText("Max 500 characters");
            valid = false;
        }

        if (!valid) return;

        // ======================
        // 💱 Currency Conversion
        // ======================
        value = convertCurrency(value);

        // ======================
        // 📈 Smart Business Logic
        // ======================

        if (value.compareTo(new BigDecimal("1000000")) > 0) {
            risk = "HIGH";
        } else if (value.compareTo(new BigDecimal("200000")) > 0) {
            risk = "MEDIUM";
        } else {
            risk = "LOW";
        }

        riskCombo.setValue(risk); // UI update

        Investment inv = (editing != null) ? editing : new Investment();

        inv.setName(name);
        inv.setCategory(category);
        inv.setLocation(location);
        inv.setEstimatedValue(value);
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
            SceneNavigator.goTo("investment_cards.fxml", "Investments");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private BigDecimal convertCurrency(BigDecimal value) {

        String currency = currencyCombo.getValue();

        switch (currency) {
            case "EUR":
                return value.multiply(BigDecimal.valueOf(1.08)); // example rate
            case "TND":
                return value.multiply(BigDecimal.valueOf(0.32)); // example rate
            default:
                return value;
        }
    }

    private void setError(Control field, Label label, String message) {
        field.getStyleClass().add("input-error");
        label.setText(message);
    }

    private void clearErrors() {

        nameError.setText("");
        categoryError.setText("");
        locationError.setText("");
        valueError.setText("");
        riskError.setText("");
        imageError.setText("");
        descError.setText("");

        nameField.getStyleClass().remove("input-error");
        categoryField.getStyleClass().remove("input-error");
        locationField.getStyleClass().remove("input-error");
        valueField.getStyleClass().remove("input-error");
        imageField.getStyleClass().remove("input-error");
    }

    @FXML
    private void onCancel() {
        AppState.setSelectedInvestment(null);
        SceneNavigator.goTo("investment_cards.fxml", "Investments");
    }
}