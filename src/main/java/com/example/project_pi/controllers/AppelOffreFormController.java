package com.example.project_pi.controllers;

import com.example.project_pi.entities.AppelOffre;
import com.example.project_pi.services.AppelOffreService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.example.project_pi.services.EmailService;
import com.example.project_pi.services.OpenAiDescriptionService;
import javafx.application.Platform;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.example.project_pi.services.CurrencyApiService;
import com.example.project_pi.services.HolidayApiService;
public class AppelOffreFormController {

    @FXML private Label formTitle;
    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private Button aiBtn;
    @FXML private Label aiStatusLabel;
    // ✅ category combo (no free text)
    @FXML private ComboBox<String> categorieCombo;

    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField budgetMinField;
    @FXML private TextField budgetMaxField;
    @FXML private ComboBox<String> deviseCombo;
    @FXML private DatePicker dateLimitePicker;
    @FXML private ComboBox<String> statutCombo;
    @FXML private Label rateLabel;
    @FXML private Label convertedMinLabel;
    @FXML private Label convertedMaxLabel;
    @FXML private Label deadlineWarningLabel;
    @FXML private Label errorLabel;

    // ✅ Save button to disable until valid
    @FXML private Button saveBtn;

    private final OpenAiDescriptionService aiService = new OpenAiDescriptionService();
    private final EmailService emailService = new EmailService();
    private String oldStatut = null;
    private final AppelOffreService service = new AppelOffreService();
    private boolean deadlineInvalid = false;   // ✅ tracks if date is not allowed
    private final CurrencyApiService currencyApi = new CurrencyApiService();
    private final HolidayApiService holidayApi = new HolidayApiService();
    private Stage dialogStage;
    private boolean saved = false;
    private AppelOffre current = null;

    @FXML
    private void initialize() {
        typeCombo.getItems().setAll("achat", "partenariat", "donnant_donnant", "don");
        deviseCombo.getItems().setAll("TND", "EUR", "USD");
        statutCombo.getItems().setAll("draft", "published", "closed");
        // Live conversion when budget/devise changes
        budgetMinField.textProperty().addListener((o,a,b) -> refreshConversion());
        budgetMaxField.textProperty().addListener((o,a,b) -> refreshConversion());
        deviseCombo.valueProperty().addListener((o,a,b) -> refreshConversion());

        // Deadline validation (weekend + holiday)
        dateLimitePicker.valueProperty().addListener((o,a,b) -> refreshDeadlineWarning());

        // init labels
        rateLabel.setText("-");
        convertedMinLabel.setText("-");
        convertedMaxLabel.setText("-");
        deadlineWarningLabel.setText("");
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
        this.oldStatut = a.getStatut();
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
            // ✅ Block saving if deadline is weekend/holiday
            if (dateLimitePicker.getValue() != null && deadlineInvalid) {
                errorLabel.setText("Date limite invalide (weekend / jour férié). Choisissez une autre date.");
                return;
            }
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
    private void refreshConversion() {
        String from = deviseCombo.getValue();
        if (from == null || from.isBlank()) {
            rateLabel.setText("-");
            convertedMinLabel.setText("-");
            convertedMaxLabel.setText("-");
            return;
        }

        // Choose a reference currency to show (example: always show TND)
        String to = "TND";

        double min = parseDoubleSafe(budgetMinField.getText());
        double max = parseDoubleSafe(budgetMaxField.getText());

        // If both empty → show only rate
        new Thread(() -> {
            try {
                var opt = currencyApi.getRate(from, to);
                if (opt.isEmpty()) {
                    javafx.application.Platform.runLater(() -> rateLabel.setText("Taux indisponible"));
                    return;
                }
                double rate = opt.getAsDouble();

                javafx.application.Platform.runLater(() -> {
                    rateLabel.setText("1 " + from + " = " + String.format("%.4f", rate) + " " + to);

                    if (min > 0) convertedMinLabel.setText("Budget min ≈ " + String.format("%.2f", min * rate) + " " + to);
                    else convertedMinLabel.setText("Budget min ≈ -");

                    if (max > 0) convertedMaxLabel.setText("Budget max ≈ " + String.format("%.2f", max * rate) + " " + to);
                    else convertedMaxLabel.setText("Budget max ≈ -");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> rateLabel.setText("Erreur taux: " + e.getMessage()));
            }
        }).start();
    }

    private void refreshDeadlineWarning() {
        var date = dateLimitePicker.getValue();
        if (date == null) {
            deadlineWarningLabel.setText("");
            return;
        }

        // Tunisia ISO code is "TN"
        String country = "TN";

        new Thread(() -> {
            try {
                boolean weekend = holidayApi.isWeekend(date);
                boolean holiday = holidayApi.isPublicHoliday(date, country);

                javafx.application.Platform.runLater(() -> {
                    if (holiday && weekend) {
                        deadlineWarningLabel.setText("Date limite sur un weekend ET un jour férié.");
                        deadlineInvalid = true;
                    } else if (holiday) {
                        deadlineWarningLabel.setText("Date limite sur un jour férié.");
                        deadlineInvalid = true;
                    } else if (weekend) {
                        deadlineWarningLabel.setText("Date limite sur un weekend.");
                        deadlineInvalid = true;
                    } else {
                        deadlineWarningLabel.setText("");
                        deadlineInvalid = false;
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    deadlineWarningLabel.setText("Info jours fériés indisponible.");
                    deadlineInvalid = false;
                });
            }
        }).start();
    }

    private double parseDoubleSafe(String s) {
        if (s == null) return 0;
        String t = s.trim().replace(",", ".");
        if (t.isEmpty()) return 0;
        try {
            return Double.parseDouble(t);
        } catch (Exception e) {
            return 0;
        }
    }
}