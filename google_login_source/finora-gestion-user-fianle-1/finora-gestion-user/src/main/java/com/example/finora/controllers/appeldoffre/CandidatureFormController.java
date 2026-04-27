package com.example.finora.controllers.appeldoffre;

import com.example.finora.entities.AppelOffre;
import com.example.finora.entities.Candidature;
import com.example.finora.services.WalletBridge;
import com.example.finora.services.appeldoffre.AppelOffreService;
import com.example.finora.services.appeldoffre.CandidatureService;
import com.example.finora.services.appeldoffre.EmailService;
import com.example.finora.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CandidatureFormController {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @FXML
    private Label formTitle;
    @FXML
    private ComboBox<AppelOffre> appelOffreCombo;
    @FXML
    private TextField nomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField montantField;
    @FXML
    private TextArea messageArea;
    @FXML
    private ComboBox<String> statutCombo;
    @FXML
    private Label errorLabel;

    private final CandidatureService service = new CandidatureService();
    private final AppelOffreService appelOffreService = new AppelOffreService();

    private final EmailService emailService = new EmailService();
    private String oldStatut = null;

    private Stage dialogStage;
    private boolean saved = false;

    private Candidature current = null;

    @FXML
    private void initialize() {

        // ✅ Set statut options based on user role
        if (Session.getCurrentUser() != null) {
            String role = Session.getCurrentUser().getRole();
            if ("ENTREPRISE".equalsIgnoreCase(role)) {
                // Entreprise can change status to accepted or rejected
                statutCombo.getItems().setAll("accepted", "rejected");
            } else if ("ADMIN".equalsIgnoreCase(role)) {
                // Admin can see all options
                statutCombo.getItems().setAll("submitted", "accepted", "rejected");
            } else {
                // Regular users can only submit (status fixed to submitted)
                statutCombo.getItems().setAll("submitted");
                statutCombo.setValue("submitted"); // Auto-set to submitted
                statutCombo.setDisable(true); // Make it read-only for users
            }
        } else {
            // Default fallback - all options
            statutCombo.getItems().setAll("submitted", "accepted", "rejected");
        }

        // ✅ money typing
        allowMoneyOnly(montantField);

        // ✅ Auto-fill user details if logged in
        if (Session.getCurrentUser() != null) {
            String role = Session.getCurrentUser().getRole();
            if (!"ENTREPRISE".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
                nomField.setText(Session.getCurrentUser().getUsername());
                emailField.setText(Session.getCurrentUser().getEmail());
                emailField.setEditable(false); // Link specifically to this account
            }
        }

        try {
            var offres = appelOffreService.getAll();
            appelOffreCombo.getItems().setAll(offres);

            // show titles
            appelOffreCombo.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected void updateItem(AppelOffre item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getTitre());
                }
            });
            appelOffreCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(AppelOffre item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getTitre());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur: impossible de charger les Appels d’Offres.");
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setCandidature(Candidature c) {
        this.current = c;
        oldStatut = c.getStatut();
        formTitle.setText("Modifier Candidature");

        if (c != null) {
            for (AppelOffre ao : appelOffreCombo.getItems()) {
                if (ao.getAppelOffreId() == c.getAppelOffreId()) {
                    appelOffreCombo.setValue(ao);
                    break;
                }
            }
        }

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

        List<String> errors = validateForm();
        if (!errors.isEmpty()) {
            errorLabel.setText("• " + String.join("\n• ", errors));
            return;
        }

        try {
            AppelOffre selectedOffre = appelOffreCombo.getValue();
            int appelOffreId = selectedOffre.getAppelOffreId();

            double montant = parseDoubleMoney(montantField.getText());

            Candidature c = new Candidature(
                    appelOffreId,
                    nomField.getText().trim(),
                    emailField.getText().trim(),
                    montant,
                    messageArea.getText() == null ? "" : messageArea.getText().trim(),
                    statutCombo.getValue());

            if (current == null) {
                // ✅ INSERT (usually "submitted")
                service.add(c);

                // ✅ Record in Wallet as outcome (money committed)
                try {
                    int userId = Session.getCurrentUser().getId();
                    String titre = selectedOffre.getTitre();
                    WalletBridge.recordAppelOffreSubmitted(userId, titre, montant);
                } catch (Exception walletEx) {
                    System.err.println("⚠️ Wallet bridge failed: " + walletEx.getMessage());
                }
            } else {
                // ✅ UPDATE
                c.setCandidatureId(current.getCandidatureId());
                service.update(c);

                // ✅ Email when status changes to accepted/rejected
                String newStatut = c.getStatut();
                boolean changed = (oldStatut == null) || !oldStatut.equalsIgnoreCase(newStatut);

                if (changed && ("accepted".equalsIgnoreCase(newStatut) || "rejected".equalsIgnoreCase(newStatut))) {
                    String subject = "Mise à jour de votre candidature";
                    String body = "Bonjour " + c.getNomCandidat() + ",\n\n"
                            + "Votre candidature a été mise à jour.\n"
                            + "Nouveau statut: " + newStatut + "\n\n"
                            + "Merci.";

                    System.out.println(">>> Sending decision email to: " + c.getEmailCandidat());
                    emailService.send(c.getEmailCandidat(), subject, body);
                    System.out.println(">>> Decision email sent OK");

                    // ✅ Record accepted candidature as income in Wallet
                    if ("accepted".equalsIgnoreCase(newStatut)) {
                        try {
                            int userId = Session.getCurrentUser().getId();
                            String titre = selectedOffre.getTitre();
                            WalletBridge.recordAppelOffreAccepted(userId, titre, montant);
                        } catch (Exception walletEx) {
                            System.err.println("⚠️ Wallet bridge failed: " + walletEx.getMessage());
                        }
                    }
                }
            }

            saved = true;
            dialogStage.close();

        } catch (NumberFormatException e) {
            errorLabel.setText("Montant doit être un nombre valide (ex: 1500 ou 1500.50).");
        } catch (SQLException e) {
            errorLabel.setText("Erreur DB: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur email: " + e.getMessage());
        }
    }

    // ---------------- Validation ----------------

    private List<String> validateForm() {
        List<String> errors = new ArrayList<>();

        AppelOffre ao = appelOffreCombo.getValue();
        String nom = safe(nomField.getText());
        String email = safe(emailField.getText());
        String montant = safe(montantField.getText());
        String statut = statutCombo.getValue();

        if (ao == null)
            errors.add("Veuillez sélectionner un Appel d’Offre.");

        requireText(errors, "Nom candidat", nom, 2, 80);

        requireText(errors, "Email candidat", email, 6, 120);
        if (!email.isEmpty() && !EMAIL.matcher(email).matches()) {
            errors.add("Email candidat n’est pas valide (ex: nom@domaine.com).");
        }

        // montant required, numeric, > 0
        if (montant.isEmpty()) {
            errors.add("Montant proposé est obligatoire.");
        } else {
            try {
                double m = parseDoubleMoney(montant);
                if (m <= 0)
                    errors.add("Montant proposé doit être > 0.");
            } catch (Exception e) {
                errors.add("Montant proposé doit être un nombre valide.");
            }
        }

        if (statut == null)
            errors.add("Choisissez un statut.");

        return errors;
    }

    private void requireText(List<String> errors, String field, String value, int min, int max) {
        if (value.isEmpty()) {
            errors.add(field + " est obligatoire.");
            return;
        }
        if (value.length() < min)
            errors.add(field + " doit contenir au moins " + min + " caractères.");
        if (value.length() > max)
            errors.add(field + " ne doit pas dépasser " + max + " caractères.");
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
            if (newV == null)
                return;
            if (!newV.matches("\\d*(?:[\\.,]\\d{0,2})?")) {
                tf.setText(oldV);
            }
        });
    }
}