package com.example.project_pi.controllers;

import com.example.project_pi.entities.Candidature;
import com.example.project_pi.services.CandidatureService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class CandidatureFormController {

    @FXML private Label formTitle;
    @FXML private TextField appelOffreIdField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField montantField;
    @FXML private TextArea messageArea;
    @FXML private ComboBox<String> statutCombo;
    @FXML private Label errorLabel;

    private final CandidatureService service = new CandidatureService();

    private Stage dialogStage;
    private boolean saved = false;

    // edit mode
    private Candidature current = null;

    @FXML
    private void initialize() {
        statutCombo.getItems().setAll("submitted", "accepted", "rejected");
        statutCombo.setValue("submitted");
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaved() {
        return saved;
    }

    // called when editing (next step)
    public void setCandidature(Candidature c) {
        this.current = c;
        formTitle.setText("Modifier Candidature");

        appelOffreIdField.setText(String.valueOf(c.getAppelOffreId()));
        nomField.setText(c.getNomCandidat());
        emailField.setText(c.getEmailCandidat());
        montantField.setText(c.getMontantPropose() == 0 ? "" : String.valueOf(c.getMontantPropose()));
        messageArea.setText(c.getMessage());
        statutCombo.setValue(c.getStatut());
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

        // ===== Validation =====
        if (appelOffreIdField.getText() == null || appelOffreIdField.getText().isBlank()) {
            errorLabel.setText("appel_offre_id est obligatoire.");
            return;
        }
        if (nomField.getText() == null || nomField.getText().isBlank()) {
            errorLabel.setText("Nom candidat est obligatoire.");
            return;
        }
        if (emailField.getText() == null || emailField.getText().isBlank()) {
            errorLabel.setText("Email est obligatoire.");
            return;
        }
        if (statutCombo.getValue() == null) {
            errorLabel.setText("Choisissez un statut.");
            return;
        }

        try {
            int appelOffreId = Integer.parseInt(appelOffreIdField.getText().trim());

            double montant = 0;
            if (!montantField.getText().isBlank()) {
                montant = Double.parseDouble(montantField.getText().trim());
            }

            Candidature c = new Candidature(
                    appelOffreId,
                    nomField.getText().trim(),
                    emailField.getText().trim(),
                    montant,
                    messageArea.getText(),
                    statutCombo.getValue()
            );

            if (current == null) {
                // INSERT
                service.add(c);
            } else {
                // UPDATE
                c.setCandidatureId(current.getCandidatureId());
                service.update(c);
            }

            saved = true;
            dialogStage.close();

        } catch (NumberFormatException e) {
            errorLabel.setText("appel_offre_id / montant doivent être des nombres.");
        } catch (SQLException e) {
            // FK error example: appel_offre_id doesn't exist
            errorLabel.setText("Erreur DB: " + e.getMessage());
        }
    }
}
