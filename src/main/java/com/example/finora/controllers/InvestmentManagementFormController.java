package com.example.finora.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.example.finora.entities.Investment;
import com.example.finora.entities.InvestmentManagement;
import com.example.finora.finorainves.AppState;
import com.example.finora.finorainves.SceneNavigator;
import com.example.finora.services.InvestmentManagementService;
import com.example.finora.services.InvestmentService;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvestmentManagementFormController {

    @FXML private Label titleLabel;

    @FXML private ComboBox<Investment> investmentCombo;
    @FXML private TextField typeField;
    @FXML private TextField amountField;
    @FXML private TextField percentField;
    @FXML private DatePicker startDatePicker;
    @FXML private ComboBox<String> statusCombo;

    @FXML private Label investmentErrorLabel;
    @FXML private Label typeErrorLabel;
    @FXML private Label amountErrorLabel;
    @FXML private Label percentErrorLabel;
    @FXML private Label startDateErrorLabel;
    @FXML private Label statusErrorLabel;

    // BUSINESS UI
    @FXML private Label exposureLabel;
    @FXML private ProgressBar fundingProgressBar;
    @FXML private Label fundingPercentLabel;
    @FXML private Label fundingStatusBadge;

    private final InvestmentManagementService service = new InvestmentManagementService();
    private final InvestmentService investmentService = new InvestmentService();

    private InvestmentManagement editing;

    @FXML
    public void initialize() {

        clearErrorLabels();

        statusCombo.getItems().setAll("ACTIVE", "CLOSED");
        investmentCombo.getItems().setAll(investmentService.getAll());

        investmentCombo.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Investment item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" :
                        item.getInvestmentId() + " - " + item.getName());
            }
        });

        investmentCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Investment item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" :
                        item.getInvestmentId() + " - " + item.getName());
            }
        });

        // Listeners for business updates
        amountField.textProperty().addListener((obs, oldVal, newVal) -> updateBusinessView());
        percentField.textProperty().addListener((obs, oldVal, newVal) -> updateBusinessView());

        editing = AppState.getSelectedManagement();

        if (editing != null) {
            titleLabel.setText("Edit Investment Management");
            fillForm(editing);
        } else {
            titleLabel.setText("Add Investment Management");
            startDatePicker.setValue(LocalDate.now());
            statusCombo.setValue("ACTIVE");
        }
    }

    private void fillForm(InvestmentManagement m) {

        Investment selected = investmentCombo.getItems()
                .stream()
                .filter(x -> x.getInvestmentId() == m.getInvestmentId())
                .findFirst()
                .orElse(null);

        investmentCombo.setValue(selected);
        typeField.setText(m.getInvestmentType());
        amountField.setText(m.getAmountInvested() != null ?
                m.getAmountInvested().toPlainString() : "");
        percentField.setText(m.getOwnershipPercentage() != null ?
                m.getOwnershipPercentage().toPlainString() : "");
        startDatePicker.setValue(m.getStartDate());
        statusCombo.setValue(m.getStatus() != null ?
                m.getStatus() : "ACTIVE");

        updateBusinessView();
    }

    @FXML
    private void onCancel() {
        AppState.setSelectedManagement(null);
        SceneNavigator.goTo(
                "investment_management_cards.fxml",
                "Investment Management - List");
    }

    private void clearErrorLabels() {
        investmentErrorLabel.setText("");
        typeErrorLabel.setText("");
        amountErrorLabel.setText("");
        percentErrorLabel.setText("");
        startDateErrorLabel.setText("");
        statusErrorLabel.setText("");
    }

    // ==============================
    // BUSINESS CALCULATION
    // ==============================

    private void updateBusinessView() {

        try {
            if (amountField.getText().isBlank() || percentField.getText().isBlank()) {
                exposureLabel.setText("$0.00");
                fundingProgressBar.setProgress(0);
                fundingPercentLabel.setText("0%");
                fundingStatusBadge.setText("PARTIALLY FUNDED");
                return;
            }

            BigDecimal amount = new BigDecimal(amountField.getText());
            BigDecimal percent = new BigDecimal(percentField.getText());

            BigDecimal exposure = amount.multiply(percent)
                    .divide(new BigDecimal("100"));

            exposureLabel.setText("$" + exposure.setScale(2, BigDecimal.ROUND_HALF_UP));

            double progress = percent.doubleValue() / 100.0;
            fundingProgressBar.setProgress(progress);
            fundingPercentLabel.setText(percent.setScale(0) + "%");

            if (percent.compareTo(new BigDecimal("100")) == 0) {
                fundingStatusBadge.setText("FULLY FUNDED");
                fundingStatusBadge.setStyle("-fx-background-color:#2ecc71;-fx-text-fill:white;");
                statusCombo.setValue("CLOSED");
            } else {
                fundingStatusBadge.setText("PARTIALLY FUNDED");
                fundingStatusBadge.setStyle("-fx-background-color:#f39c12;-fx-text-fill:white;");
                statusCombo.setValue("ACTIVE");
            }

        } catch (Exception e) {
            exposureLabel.setText("$0.00");
            fundingProgressBar.setProgress(0);
            fundingPercentLabel.setText("0%");
        }
    }

    // ==============================
    // SAVE
    // ==============================

    @FXML
    private void onSave() {

        clearErrorLabels();

        Investment inv = investmentCombo.getValue();
        if (inv == null) {
            investmentErrorLabel.setText("Investment required.");
            return;
        }

        String type = typeField.getText().trim();
        if (type.length() < 3) {
            typeErrorLabel.setText("Minimum 3 characters required.");
            return;
        }

        BigDecimal amount;
        BigDecimal percent;

        try {
            amount = new BigDecimal(amountField.getText());
            percent = new BigDecimal(percentField.getText());
        } catch (Exception e) {
            amountErrorLabel.setText("Invalid numeric value.");
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            amountErrorLabel.setText("Amount must be > 0.");
            return;
        }

        if (percent.compareTo(BigDecimal.ZERO) < 0 ||
                percent.compareTo(new BigDecimal("100")) > 0) {
            percentErrorLabel.setText("Ownership must be between 0 and 100.");
            return;
        }

        LocalDate startDate = startDatePicker.getValue();
        if (startDate == null) {
            startDateErrorLabel.setText("Start date required.");
            return;
        }

        // BUSINESS RULE BigDecimal SAFE
        BigDecimal existingOwnership =
                service.getTotalOwnershipForInvestment(inv.getInvestmentId());

        if (editing != null && editing.getOwnershipPercentage() != null) {
            existingOwnership =
                    existingOwnership.subtract(editing.getOwnershipPercentage());
        }

        BigDecimal totalAfterSave = existingOwnership.add(percent);

        if (totalAfterSave.compareTo(new BigDecimal("100")) > 0) {
            percentErrorLabel.setText(
                    "Total exceeds 100% (Current: "
                            + existingOwnership.setScale(2) + "%)");
            return;
        }

        InvestmentManagement m =
                (editing != null) ? editing : new InvestmentManagement();

        m.setInvestmentId(inv.getInvestmentId());
        m.setInvestmentType(type);
        m.setAmountInvested(amount);
        m.setOwnershipPercentage(percent);
        m.setStartDate(startDate);

        if (totalAfterSave.compareTo(new BigDecimal("100")) == 0) {
            m.setStatus("CLOSED");
        } else {
            m.setStatus("ACTIVE");
        }

        if (editing == null)
            service.add(m);
        else
            service.update(m);

        service.closeInvestmentIfFullyOwned(inv.getInvestmentId());

        AppState.setSelectedManagement(null);

        SceneNavigator.goTo(
                "investment_management_cards.fxml",
                "Investment Management - List");
    }
}