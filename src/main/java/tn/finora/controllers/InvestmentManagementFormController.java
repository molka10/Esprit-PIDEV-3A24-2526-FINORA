package tn.finora.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.finora.entities.Investment;
import tn.finora.entities.InvestmentManagement;
import tn.finora.finorainves.AppState;
import tn.finora.finorainves.SceneNavigator;
import tn.finora.services.InvestmentManagementService;
import tn.finora.services.InvestmentService;

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

    // Labels pour les messages d'erreur par champ
    @FXML private Label investmentErrorLabel;
    @FXML private Label typeErrorLabel;
    @FXML private Label amountErrorLabel;
    @FXML private Label percentErrorLabel;
    @FXML private Label startDateErrorLabel;
    @FXML private Label statusErrorLabel;

    private final InvestmentManagementService service = new InvestmentManagementService();
    private final InvestmentService investmentService = new InvestmentService();

    private InvestmentManagement editing;

    @FXML
    public void initialize() {
        // Initialisation des erreurs vides
        clearErrorLabels();

        // Remplissage des listes combo
        statusCombo.getItems().setAll("ACTIVE", "CLOSED");
        investmentCombo.getItems().setAll(investmentService.getAll());

        // Affichage personnalisé dans le ComboBox Investment
        investmentCombo.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Investment item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : (item.getInvestmentId() + " - " + item.getName()));
            }
        });
        investmentCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Investment item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : (item.getInvestmentId() + " - " + item.getName()));
            }
        });

        // Vérifier si on édite un management existant
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
        amountField.setText(m.getAmountInvested() != null ? m.getAmountInvested().toPlainString() : "");
        percentField.setText(m.getOwnershipPercentage() != null ? m.getOwnershipPercentage().toPlainString() : "");
        startDatePicker.setValue(m.getStartDate());
        statusCombo.setValue(m.getStatus() != null ? m.getStatus() : "ACTIVE");
    }

    @FXML
    private void onCancel() {
        AppState.setSelectedManagement(null);
        SceneNavigator.goTo("investment_management_cards.fxml", "Investment Management - List");
    }

    // Nettoyer tous les labels d'erreur
    @FXML
    private void clearErrorLabels() {
        investmentErrorLabel.setText("");
        typeErrorLabel.setText("");
        amountErrorLabel.setText("");
        percentErrorLabel.setText("");
        startDateErrorLabel.setText("");
        statusErrorLabel.setText("");
    }

    @FXML
    private void onSave() {
        clearErrorLabels();

        boolean hasError = false;

        // ---------------------------
        // Validation des champs
        // ---------------------------
        // Investment
        Investment inv = investmentCombo.getValue();
        if (inv == null) {
            investmentErrorLabel.setText("You must choose an Investment.");
            hasError = true;
        }

        // Type
        String type = typeField.getText() == null ? "" : typeField.getText().trim();
        if (type.length() < 3) {
            typeErrorLabel.setText("Investment type is required (min 3 chars).");
            hasError = true;
        }

        // Amount
        BigDecimal amount = null;
        try {
            amount = new BigDecimal(amountField.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                amountErrorLabel.setText("Amount invested must be > 0.");
                hasError = true;
            }
        } catch (Exception e) {
            amountErrorLabel.setText("Amount must be numeric (ex: 5000.00).");
            hasError = true;
        }

        // Percent
        BigDecimal percent = null;
        String percentText = percentField.getText() == null ? "" : percentField.getText().trim();
        if (!percentText.isEmpty()) {
            try {
                percent = new BigDecimal(percentText);
                if (percent.compareTo(BigDecimal.ZERO) < 0 || percent.compareTo(new BigDecimal("100")) > 0) {
                    percentErrorLabel.setText("Ownership % must be between 0 and 100.");
                    hasError = true;
                }
            } catch (Exception e) {
                percentErrorLabel.setText("Ownership % must be numeric (0-100).");
                hasError = true;
            }
        }

        // Start Date
        LocalDate startDate = startDatePicker.getValue();
        if (startDate == null) {
            startDateErrorLabel.setText("Start date is required.");
            hasError = true;
        }

        // Status
        String status = statusCombo.getValue();
        if (status == null || status.isBlank()) {
            statusErrorLabel.setText("Status is required (ACTIVE/CLOSED).");
            hasError = true;
        }

        // Stop si erreur
        if (hasError) return;

        // ---------------------------
        // Save ou Update
        // ---------------------------
        InvestmentManagement m = (editing != null) ? editing : new InvestmentManagement();
        m.setInvestmentId(inv.getInvestmentId());
        m.setInvestmentType(type);
        m.setAmountInvested(amount);
        m.setOwnershipPercentage(percent);
        m.setStartDate(startDate);
        m.setStatus(status);

        if (editing == null) service.add(m);
        else service.update(m);

        AppState.setSelectedManagement(null);
        SceneNavigator.goTo("investment_management_cards.fxml", "Investment Management - List");
    }
}