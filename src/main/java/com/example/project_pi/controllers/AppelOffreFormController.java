package com.example.project_pi.controllers;

import com.example.project_pi.entities.AppelOffre;
import com.example.project_pi.services.AppelOffreService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

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

    //  if not null => EDIT mode
    private AppelOffre current = null;

    @FXML
    private void initialize() {
        typeCombo.getItems().setAll("achat", "partenariat", "donnant_donnant", "don");
        deviseCombo.getItems().setAll("TND", "EUR", "USD");
        statutCombo.getItems().setAll("draft", "published", "closed");
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaved() {
        return saved;
    }

    //  Called from onEdit(): sets EDIT mode + fills fields
    public void setAppelOffre(AppelOffre a) {
        this.current = a;  // IMPORTANT: now current is NOT NULL

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

        // ===== VALIDATION =====
        if (titreField.getText() == null || titreField.getText().isBlank()) {
            errorLabel.setText("Le titre est obligatoire.");
            return;
        }
        if (typeCombo.getValue() == null) {
            errorLabel.setText("Choisissez un type.");
            return;
        }
        if (statutCombo.getValue() == null) {
            errorLabel.setText("Choisissez un statut.");
            return;
        }

        try {
            double budgetMin = budgetMinField.getText().isBlank() ? 0 : Double.parseDouble(budgetMinField.getText());
            double budgetMax = budgetMaxField.getText().isBlank() ? 0 : Double.parseDouble(budgetMaxField.getText());

            AppelOffre a = new AppelOffre(
                    titreField.getText(),
                    descriptionArea.getText(),
                    categorieField.getText(),
                    typeCombo.getValue(),
                    budgetMin,
                    budgetMax,
                    deviseCombo.getValue(),
                    dateLimitePicker.getValue(),
                    statutCombo.getValue()
            );

            //  Decide INSERT vs UPDATE
            if (current == null) {
                // NEW => INSERT
                service.add(a);
            } else {
                // EDIT => UPDATE (keep same ID)
                a.setAppelOffreId(current.getAppelOffreId());
                service.update(a);
            }

            saved = true;
            dialogStage.close();

        } catch (NumberFormatException e) {
            errorLabel.setText("Budget invalide (ex: 15000).");
        } catch (SQLException e) {
            errorLabel.setText("Erreur DB: " + e.getMessage());
        }
    }
}
