package com.example.project_pi.controllers;

import com.example.project_pi.entities.AppelOffre;
import com.example.project_pi.services.AppelOffreService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppelOffreFormController {

    @FXML private Label formTitle;
    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField categorieField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField budgetMinField;
    @FXML private TextField budgetMaxField;
    @FXML private ComboBox<String> deviseCombo;
    @FXML private DatePicker dateLimitePicker;
    @FXML private ComboBox<String> statutCombo;
    @FXML private Label errorLabel;

    private final AppelOffreService service = new AppelOffreService();

    private Stage dialogStage;
    private boolean saved = false;

    private AppelOffre current = null;

    @FXML
    private void initialize() {
        typeCombo.getItems().setAll("achat", "partenariat", "donnant_donnant", "don");
        deviseCombo.getItems().setAll("TND", "EUR", "USD");
        statutCombo.getItems().setAll("draft", "published", "closed");

        // ✅ restrict typing to money (0-2 decimals)
        allowMoneyOnly(budgetMinField);
        allowMoneyOnly(budgetMaxField);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setAppelOffre(AppelOffre a) {
        this.current = a;
        formTitle.setText("Modifier Appel d’Offre");

        titreField.setText(a.getTitre());
        descriptionArea.setText(a.getDescription());
        categorieField.setText(a.getCategorie());
        typeCombo.setValue(a.getType());

        budgetMinField.setText(a.getBudgetMin() == 0 ? "" : String.valueOf(a.getBudgetMin()));
        budgetMaxField.setText(a.getBudgetMax() == 0 ? "" : String.valueOf(a.getBudgetMax()));

        deviseCombo.setValue(a.getDevise());
        dateLimitePicker.setValue(a.getDateLimite());
        statutCombo.setValue(a.getStatut());
    }

    @FXML
    private void onBack() {
        dialogStage.close();
    }

    @FXML
    private void onCancel() {
        dialogStage.close();
    }

    @FXML
    private void onSave() {
        errorLabel.setText("");

        List<String> errors = validateForm();
        if (!errors.isEmpty()) {
            errorLabel.setText("• " + String.join("\n• ", errors));
            return;
        }

        try {
            double budgetMin = parseDoubleMoney(budgetMinField.getText());
            double budgetMax = parseDoubleMoney(budgetMaxField.getText());

            AppelOffre a = new AppelOffre(
                    titreField.getText().trim(),
                    descriptionArea.getText().trim(),
                    categorieField.getText().trim(),
                    typeCombo.getValue(),
                    budgetMin,
                    budgetMax,
                    deviseCombo.getValue(),
                    dateLimitePicker.getValue(),
                    statutCombo.getValue()
            );

            if (current == null) {
                service.add(a);
            } else {
                a.setAppelOffreId(current.getAppelOffreId());
                service.update(a);
            }

            saved = true;
            dialogStage.close();

        } catch (NumberFormatException e) {
            errorLabel.setText("Budget invalide (ex: 15000 ou 15000.50).");
        } catch (SQLException e) {
            errorLabel.setText("Erreur DB: " + e.getMessage());
        }
    }

    // ---------------- Validation ----------------

    private List<String> validateForm() {
        List<String> errors = new ArrayList<>();

        String titre = safe(titreField.getText());
        String desc = safe(descriptionArea.getText());
        String cat = safe(categorieField.getText());
        String type = typeCombo.getValue();
        String devise = deviseCombo.getValue();
        LocalDate dateLimite = dateLimitePicker.getValue();
        String statut = statutCombo.getValue();

        // required + lengths
        requireText(errors, "Titre", titre, 3, 100);
        requireText(errors, "Description", desc, 5, 1000);
        requireText(errors, "Catégorie", cat, 2, 50);

        if (type == null) errors.add("Choisissez un type.");
        if (devise == null) errors.add("Choisissez une devise.");
        if (statut == null) errors.add("Choisissez un statut.");

        // budgets required and numeric >= 0
        Double min = parseMoney(errors, "Budget min", budgetMinField.getText(), true);
        Double max = parseMoney(errors, "Budget max", budgetMaxField.getText(), true);

        if (min != null && min < 0) errors.add("Budget min doit être ≥ 0.");
        if (max != null && max < 0) errors.add("Budget max doit être ≥ 0.");
        if (min != null && max != null && min > max) errors.add("Budget min doit être ≤ Budget max.");

        // date required and not past
        if (dateLimite == null) {
            errors.add("Date limite est obligatoire.");
        } else if (dateLimite.isBefore(LocalDate.now())) {
            errors.add("Date limite ne peut pas être dans le passé.");
        }

        return errors;
    }

    private void requireText(List<String> errors, String field, String value, int min, int max) {
        if (value.isEmpty()) {
            errors.add(field + " est obligatoire.");
            return;
        }
        if (value.length() < min) errors.add(field + " doit contenir au moins " + min + " caractères.");
        if (value.length() > max) errors.add(field + " ne doit pas dépasser " + max + " caractères.");
    }

    private Double parseMoney(List<String> errors, String field, String value, boolean required) {
        String v = safe(value);
        if (v.isEmpty()) {
            if (required) errors.add(field + " est obligatoire.");
            return null;
        }
        try {
            return parseDoubleMoney(v);
        } catch (Exception e) {
            errors.add(field + " doit être un nombre valide.");
            return null;
        }
    }

    private double parseDoubleMoney(String txt) {
        String cleaned = safe(txt).replace(",", ".");
        return Double.parseDouble(cleaned);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private void allowMoneyOnly(TextField tf) {
        tf.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            // allow empty, digits, optional decimal with up to 2 digits
            if (!newV.matches("\\d*(?:[\\.,]\\d{0,2})?")) {
                tf.setText(oldV);
            }
        });
    }
}