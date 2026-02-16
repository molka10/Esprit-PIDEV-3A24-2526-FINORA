package tn.finora.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.finora.entities.Formation;
import tn.finora.services.FormationService;

public class FormationFormController {

    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtCategorie;
    @FXML private ComboBox<String> cbNiveau;
    @FXML private TextField txtImageUrl;
    @FXML private CheckBox chkPublished;

    private final FormationService service = new FormationService();

    private Formation current;
    private Runnable onSaved;

    @FXML
    public void initialize() {
        cbNiveau.setItems(FXCollections.observableArrayList(
                "Débutant", "Intermédiaire", "Avancé"
        ));
        cbNiveau.setPromptText("Choisir un niveau");
    }

    public void setOnSaved(Runnable onSaved) { this.onSaved = onSaved; }

    public void setData(Formation f) {
        this.current = f;
        if (f != null) {
            txtTitre.setText(f.getTitre());
            txtDescription.setText(f.getDescription());
            txtCategorie.setText(f.getCategorie());
            cbNiveau.setValue(f.getNiveau());
            txtImageUrl.setText(f.getImageUrl());
            chkPublished.setSelected(f.isPublished());
        }
    }

    @FXML
    private void onSave() {
        clearErrors();

        String titre = safe(txtTitre.getText());
        String categorie = safe(txtCategorie.getText());
        String niveau = cbNiveau.getValue();
        String imageUrl = safe(txtImageUrl.getText());
        String description = txtDescription.getText(); // peut être vide

        // ✅ validations
        boolean ok = true;

        if (titre.isBlank()) { markError(txtTitre); ok = false; }
        else if (titre.length() < 3) { markError(txtTitre); ok = false; }

        if (categorie.isBlank()) { markError(txtCategorie); ok = false; }
        else if (categorie.length() < 3) { markError(txtCategorie); ok = false; }

        if (niveau == null || niveau.isBlank()) { markError(cbNiveau); ok = false; }


        if (!imageUrl.isBlank() && !isValidUrl(imageUrl)) {
            markError(txtImageUrl);
            ok = false;
        }

        if (!ok) {
            showWarn("""
                    Vérifie les champs :
                    - Titre (obligatoire, min 3 caractères)
                    - Catégorie (obligatoire, min 3 caractères)
                    - Niveau (obligatoire)
                    - Image URL (optionnel mais doit être valide si rempli)
                    """);
            return;
        }

        try {
            Formation f = (current == null) ? new Formation() : current;

            f.setTitre(titre);
            f.setDescription(description);
            f.setCategorie(categorie);
            f.setNiveau(niveau);
            f.setImageUrl(imageUrl);
            f.setPublished(chkPublished.isSelected());

            if (current == null) service.add(f);
            else service.update(f);

            if (onSaved != null) onSaved.run();
            close();
        } catch (Exception e) {
            showError("Erreur Save: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() { close(); }

    // ---------------- helpers ----------------

    private String safe(String s) { return s == null ? "" : s.trim(); }

    private boolean isValidUrl(String url) {
        // simple validation (enough for school project)
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private void clearErrors() {
        txtTitre.setStyle(null);
        txtCategorie.setStyle(null);
        txtImageUrl.setStyle(null);
        cbNiveau.setStyle(null);
    }

    private void markError(Control c) {
        c.setStyle("-fx-border-color: #ff4d4d; -fx-border-width: 2; -fx-border-radius: 8;");
    }

    private void close() {
        Stage stage = (Stage) txtTitre.getScene().getWindow();
        stage.close();
    }

    private void showWarn(String msg) { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
