package tn.finora.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.finora.entities.Formation;
import tn.finora.services.FormationService;

import java.util.*;
import java.util.stream.Collectors;

public class FormationFormController {

    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;

    // Tags UI
    @FXML private TextField txtCategorieInput;
    @FXML private FlowPane tagsPane;

    // Niveau + autres
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
        // niveau options (si tu avais déjà ça, garde le même contenu)
        if (cbNiveau != null && cbNiveau.getItems().isEmpty()) {
            cbNiveau.getItems().addAll("Débutant", "Intermédiaire", "Avancé");
        }

        // Enter key -> add tag
        if (txtCategorieInput != null) {
            txtCategorieInput.setOnAction(e -> onAddTag());
        }
    }

    public void setData(Formation f) {
        this.current = f;

        tags.clear();
        if (tagsPane != null) tagsPane.getChildren().clear();

        if (f != null) {
            txtTitre.setText(nz(f.getTitre()));
            txtDescription.setText(nz(f.getDescription()));
            txtImageUrl.setText(nz(f.getImageUrl()));
            chkPublished.setSelected(f.isPublished());

            if (cbNiveau != null) cbNiveau.getSelectionModel().select(nz(f.getNiveau()));

            // parse categorie string => tags
            parseTagsFromString(f.getCategorie()).forEach(this::addTagInternal);
        }

        renderTags();
    }

    @FXML
    private void onAddTag() {
        clearError();

        if (txtCategorieInput == null) return;
        String raw = nz(txtCategorieInput.getText()).trim();

        if (raw.isEmpty()) return;

        // allow paste "Java, Web, JDBC"
        List<String> parsed = parseTagsFromString(raw);
        for (String t : parsed) addTagInternal(t);

        txtCategorieInput.clear();
        renderTags();
    }

    private void addTagInternal(String tag) {
        String t = normalizeTag(tag);
        if (t.isEmpty()) return;
        if (t.length() > 20) t = t.substring(0, 20); // small limit to look nice
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
            if (tags.isEmpty()) { markError("Ajoute au moins un tag catégorie"); return; }

            Formation f = (current == null) ? new Formation() : current;

            f.setTitre(titre);
            f.setDescription(nz(txtDescription.getText()));
            f.setNiveau(niveau);

            // store tags as one String (DB unchanged)
            f.setCategorie(String.join(", ", tags));

            f.setImageUrl(nz(txtImageUrl.getText()));
            f.setPublished(chkPublished.isSelected());

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
        String t = s.trim();
        // remove double spaces
        t = t.replaceAll("\\s{2,}", " ");
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
