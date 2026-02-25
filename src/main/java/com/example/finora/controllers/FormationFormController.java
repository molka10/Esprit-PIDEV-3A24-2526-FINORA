package com.example.finora.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import com.example.finora.entities.Formation;
import com.example.finora.services.FormationService;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class FormationFormController {

    @FXML private Label lblTitle;

    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;

    // ===== Categories (Pinterest tags) =====
    @FXML private ComboBox<String> cbCategorie;      // pick from list
    @FXML private TextField txtCategorieTag;         // type tag manually
    @FXML private FlowPane tagsPane;                 // chips container

    // ===== Other fields =====
    @FXML private ComboBox<String> cbNiveau;
    @FXML private TextField txtImageUrl;
    @FXML private CheckBox chkPublished;

    @FXML private Label lblError;

    private final FormationService service = new FormationService();

    private Formation current;
    private Runnable onSaved;

    // Tags state
    private final LinkedHashSet<String> tags = new LinkedHashSet<>();

    public void setOnSaved(Runnable onSaved) { this.onSaved = onSaved; }

    @FXML
    public void initialize() {

        // niveaux
        if (cbNiveau != null && cbNiveau.getItems().isEmpty()) {
            cbNiveau.getItems().addAll("Débutant", "Intermédiaire", "Avancé");
        }

        // ✅ Finance / Bourse / Invest categories
        if (cbCategorie != null && cbCategorie.getItems().isEmpty()) {
            cbCategorie.setItems(FXCollections.observableArrayList(
                    "Bourse", "Investissement", "Trading", "Actions", "ETF",
                    "Obligations", "Crypto", "Forex", "Analyse technique",
                    "Analyse fondamentale", "Gestion des risques", "Portefeuille",
                    "Dividendes", "Marchés financiers", "Psychologie du trader",
                    "Économie", "Inflation", "Taux d’intérêt", "Fiscalité"
            ));
        }

        // enter in text field = add tag
        if (txtCategorieTag != null) {
            txtCategorieTag.setOnAction(e -> onAddTag());
        }
    }

    public void setData(Formation f) {
        this.current = f;

        tags.clear();
        if (tagsPane != null) tagsPane.getChildren().clear();

        if (f != null) {
            if (lblTitle != null) lblTitle.setText("Modifier Formation");

            txtTitre.setText(nz(f.getTitre()));
            txtDescription.setText(nz(f.getDescription()));
            txtImageUrl.setText(nz(f.getImageUrl()));
            chkPublished.setSelected(f.isPublished());

            if (cbNiveau != null) cbNiveau.getSelectionModel().select(nz(f.getNiveau()));

            parseTagsFromString(f.getCategorie()).forEach(this::addTagInternal);
        } else {
            if (lblTitle != null) lblTitle.setText("Ajouter Formation");
        }

        renderTags();
    }

    // when user selects from ComboBox
    @FXML
    private void onPickCategorie() {
        clearError();

        if (cbCategorie == null) return;
        String picked = cbCategorie.getSelectionModel().getSelectedItem();
        if (picked == null || picked.isBlank()) return;

        if (txtCategorieTag != null) txtCategorieTag.setText(picked);
        onAddTag();

        cbCategorie.getSelectionModel().clearSelection();
    }

    @FXML
    private void onAddTag() {
        clearError();

        if (txtCategorieTag == null) return;
        String raw = nz(txtCategorieTag.getText()).trim();
        if (raw.isEmpty()) return;

        // allow "Bourse, ETF; Crypto"
        List<String> parsed = parseTagsFromString(raw);
        for (String t : parsed) addTagInternal(t);

        txtCategorieTag.clear();
        renderTags();
    }

    private void addTagInternal(String tag) {
        String t = normalizeTag(tag);
        if (t.isEmpty()) return;

        // small limit for nice chips
        if (t.length() > 24) t = t.substring(0, 24);

        tags.add(t);
    }

    private void renderTags() {
        if (tagsPane == null) return;
        tagsPane.getChildren().clear();

        for (String t : tags) {
            HBox chip = new HBox(6);
            chip.getStyleClass().addAll("chip", "chip-purple");

            Label lbl = new Label(t);
            lbl.getStyleClass().add("chip-text");

            Button x = new Button("✕");
            x.getStyleClass().add("chip-x");
            x.setOnAction(e -> {
                tags.remove(t);
                renderTags();
            });

            chip.getChildren().addAll(lbl, x);
            tagsPane.getChildren().add(chip);
        }
    }

    @FXML
    private void onSave() {
        clearError();

        try {
            String titre = nz(txtTitre.getText()).trim();
            String niveau = (cbNiveau == null) ? "" : nz(cbNiveau.getSelectionModel().getSelectedItem()).trim();

            if (titre.isEmpty()) { markError("Titre obligatoire"); return; }
            if (niveau.isEmpty()) { markError("Niveau obligatoire"); return; }
            if (tags.isEmpty()) { markError("Ajoute au moins 1 catégorie (tag)"); return; }

            Formation f = (current == null) ? new Formation() : current;

            f.setTitre(titre);
            f.setDescription(nz(txtDescription.getText()));
            f.setNiveau(niveau);

            // store tags as "tag1, tag2, tag3"
            f.setCategorie(String.join(", ", tags));

            f.setImageUrl(nz(txtImageUrl.getText()));
            f.setPublished(chkPublished != null && chkPublished.isSelected());

            if (current == null) service.add(f);
            else service.update(f);

            if (onSaved != null) onSaved.run();
            close();

        } catch (Exception e) {
            markError("Erreur Save: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() { close(); }

    private void close() {
        Stage stage = (Stage) txtTitre.getScene().getWindow();
        stage.close();
    }

    // ===== Helpers =====

    private List<String> parseTagsFromString(String s) {
        if (s == null) return List.of();
        return Arrays.stream(s.split("[,;]"))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toList());
    }

    private String normalizeTag(String s) {
        if (s == null) return "";
        String t = s.trim().replaceAll("\\s{2,}", " ");
        return t;
    }

    private String nz(String s) { return s == null ? "" : s; }

    private void markError(String msg) {
        if (lblError != null) lblError.setText(msg);
        else new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    private void clearError() {
        if (lblError != null) lblError.setText("");
    }
}
