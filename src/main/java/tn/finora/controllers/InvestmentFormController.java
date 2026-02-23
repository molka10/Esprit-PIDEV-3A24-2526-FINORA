package tn.finora.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

        riskCombo.getItems().setAll("LOW", "MEDIUM", "HIGH");

        editing = AppState.getSelectedInvestment();

        // Live Image Preview
        imageField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.startsWith("http")) {
                previewImage.setImage(new Image(newVal, true));
            } else {
                previewImage.setImage(null);
            }
        });

        if (editing != null) {
            titleLabel.setText("Update Investment");
            loadEditingData();
        } else {
            titleLabel.setText("Add Investment");
            riskCombo.getSelectionModel().selectFirst();
            valueField.setText("0");
        }
    }

    private void loadEditingData() {
        nameField.setText(editing.getName());
        categoryField.setText(editing.getCategory());
        locationField.setText(editing.getLocation());
        valueField.setText(editing.getEstimatedValue().toPlainString());
        riskCombo.setValue(editing.getRiskLevel());
        imageField.setText(editing.getImageUrl());
        descArea.setText(editing.getDescription());
    }

    @FXML
    private void onSave() {

        clearErrors();
        boolean valid = true;

        // -------- NAME --------
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            setError(nameField, nameError, "Name is required");
            valid = false;
        } else if (name.length() > 50) {
            setError(nameField, nameError, "Max 50 characters");
            valid = false;
        }

        // -------- CATEGORY --------
        String category = categoryField.getText().trim();
        if (category.isEmpty()) {
            setError(categoryField, categoryError, "Category required");
            valid = false;
        }

        // -------- LOCATION --------
        String location = locationField.getText().trim();
        if (location.isEmpty()) {
            setError(locationField, locationError, "Location required");
            valid = false;
        }

        // -------- VALUE --------
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

        // -------- RISK --------
        String risk = riskCombo.getValue();
        if (risk == null) {
            riskError.setText("Select risk level");
            valid = false;
        }

        // -------- IMAGE --------
        String imageUrl = imageField.getText().trim();
        if (!imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
            setError(imageField, imageError, "Invalid URL");
            valid = false;
        }

        // -------- DESCRIPTION --------
        String description = descArea.getText().trim();
        if (description.length() > 500) {
            descError.setText("Max 500 characters");
            valid = false;
        }

        if (!valid) return;

        // ===== BUSINESS FEATURE =====
        // Auto-adjust Risk based on Value
        if (value.compareTo(new BigDecimal("1000000")) > 0) {
            risk = "HIGH";
        }

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