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

    // ✅ category combo (no free text)
    @FXML private ComboBox<String> categorieCombo;

    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField budgetMinField;
    @FXML private TextField budgetMaxField;
    @FXML private ComboBox<String> deviseCombo;
    @FXML private DatePicker dateLimitePicker;
    @FXML private ComboBox<String> statutCombo;

    @FXML private Label errorLabel;

    // ✅ Save button to disable until valid
    @FXML private Button saveBtn;

    private final AppelOffreService service = new AppelOffreService();

    private Stage dialogStage;
    private boolean saved = false;
    private AppelOffre current = null;

    @FXML
    private void initialize() {
        typeCombo.getItems().setAll("achat", "partenariat", "donnant_donnant", "don");
        deviseCombo.getItems().setAll("TND", "EUR", "USD");
        statutCombo.getItems().setAll("draft", "published", "closed");

        // ✅ logical categories (static, teacher friendly)
        categorieCombo.getItems().setAll(
                "Informatique / Développement",
                "Réseaux / Sécurité",
                "Matériel informatique",
                "Bureautique / Fournitures",
                "Maintenance / Support",
                "Construction / BTP",
                "Électricité / Énergie",
                "Transport / Logistique",
                "Nettoyage / Hygiène",
                "Marketing / Communication",
                "Design / Multimédia",
                "Formation / Coaching",
                "Conseil / Audit",
                "Services juridiques",
                "Ressources humaines",
                "Événementiel",
                "Autres"
        );
        categorieCombo.setEditable(false);

        // ✅ restrict typing to money
        allowMoneyOnly(budgetMinField);
        allowMoneyOnly(budgetMaxField);

        // ✅ live validation hooks
        titreField.textProperty().addListener((o,a,b) -> refreshLiveValidation());
        descriptionArea.textProperty().addListener((o,a,b) -> refreshLiveValidation());
        categorieCombo.valueProperty().addListener((o,a,b) -> refreshLiveValidation());
        typeCombo.valueProperty().addListener((o,a,b) -> refreshLiveValidation());
        budgetMinField.textProperty().addListener((o,a,b) -> refreshLiveValidation());
        budgetMaxField.textProperty().addListener((o,a,b) -> refreshLiveValidation());
        deviseCombo.valueProperty().addListener((o,a,b) -> refreshLiveValidation());
        dateLimitePicker.valueProperty().addListener((o,a,b) -> refreshLiveValidation());
        statutCombo.valueProperty().addListener((o,a,b) -> refreshLiveValidation());

        refreshLiveValidation();
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
        categorieCombo.setValue(a.getCategorie());
        typeCombo.setValue(a.getType());

        budgetMinField.setText(a.getBudgetMin() == 0 ? "" : String.valueOf(a.getBudgetMin()));
        budgetMaxField.setText(a.getBudgetMax() == 0 ? "" : String.valueOf(a.getBudgetMax()));

        deviseCombo.setValue(a.getDevise());
        dateLimitePicker.setValue(a.getDateLimite());
        statutCombo.setValue(a.getStatut());

        refreshLiveValidation();
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
                    categorieCombo.getValue(),
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

    // ---------------- Live validation ----------------

    private void refreshLiveValidation() {
        List<String> errors = validateForm();
        boolean ok = errors.isEmpty();

        if (saveBtn != null) saveBtn.setDisable(!ok);

        // show first error only (clean)
        errorLabel.setText(ok ? "" : "• " + errors.get(0));
    }

    // ---------------- Validation rules ----------------

    private List<String> validateForm() {
        List<String> errors = new ArrayList<>();

        String titre = safe(titreField.getText());
        String desc = safe(descriptionArea.getText());
        String type = typeCombo.getValue();
        String devise = deviseCombo.getValue();
        LocalDate dateLimite = dateLimitePicker.getValue();
        String statut = statutCombo.getValue();

        // required + lengths
        requireText(errors, "Titre", titre, 3, 100);
        requireText(errors, "Description", desc, 5, 1000);

        if (categorieCombo.getValue() == null) errors.add("Choisissez une catégorie.");
        if (type == null) errors.add("Choisissez un type.");
        if (devise == null) errors.add("Choisissez une devise.");
        if (statut == null) errors.add("Choisissez un statut.");

        // budgets: required + numeric + >= 0 + min<=max
        Double min = parseMoney(errors, "Budget min", budgetMinField.getText(), true);
        Double max = parseMoney(errors, "Budget max", budgetMaxField.getText(), true);

        if (min != null && min < 0) errors.add("Budget min doit être ≥ 0.");
        if (max != null && max < 0) errors.add("Budget max doit être ≥ 0.");
        if (min != null && max != null && min > max) errors.add("Budget min doit être ≤ Budget max.");

        // date: required + not past
        if (dateLimite == null) errors.add("Date limite est obligatoire.");
        else if (dateLimite.isBefore(LocalDate.now())) errors.add("Date limite ne peut pas être dans le passé.");

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
            if (!newV.matches("\\d*(?:[\\.,]\\d{0,2})?")) {
                tf.setText(oldV);
            }
        });
    }
}