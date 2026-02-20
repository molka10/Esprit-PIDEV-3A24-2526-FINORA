package tn.finora.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.finora.entities.Formation;
import tn.finora.entities.Lesson;
import tn.finora.services.LessonService;

import java.util.List;

public class LessonFormController {

    @FXML private Label lblTitle;

    @FXML private ComboBox<Formation> cbFormation;
    @FXML private TextField txtTitre;
    @FXML private TextArea txtContenu;
    @FXML private TextField txtVideoUrl; // ✅ NEW
    @FXML private TextField txtOrdre;
    @FXML private TextField txtDuree;

    private final LessonService service = new LessonService();

    private Lesson current;
    private Runnable onSaved;

    public void setOnSaved(Runnable onSaved) { this.onSaved = onSaved; }

    public void setFormations(List<Formation> formations, Formation preselected) {
        cbFormation.setItems(FXCollections.observableArrayList(formations));

        // ✅ Title only
        cbFormation.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(Formation item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitre());
            }
        });
        cbFormation.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Formation item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Choisir une formation..." : item.getTitre());
            }
        });

        if (preselected != null) cbFormation.getSelectionModel().select(preselected);
    }

    public void setData(Lesson l) {
        this.current = l;

        if (l != null) {
            if (lblTitle != null) lblTitle.setText("Modifier Lesson");
            txtTitre.setText(l.getTitre());
            txtContenu.setText(l.getContenu());
            txtVideoUrl.setText(l.getVideoUrl());
            txtOrdre.setText(String.valueOf(l.getOrdre()));
            txtDuree.setText(String.valueOf(l.getDureeMinutes()));
        } else {
            if (lblTitle != null) lblTitle.setText("Ajouter Lesson");
        }
    }

    @FXML
    private void onSave() {
        try {
            Formation selectedFormation = cbFormation.getSelectionModel().getSelectedItem();
            if (selectedFormation == null) { showWarn("Choisis une formation"); return; }

            String titre = txtTitre.getText() == null ? "" : txtTitre.getText().trim();
            if (titre.isEmpty()) { showWarn("Titre obligatoire"); return; }

            int ordre = parseInt(txtOrdre.getText(), "ordre");
            int duree = parseInt(txtDuree.getText(), "durée");

            String video = (txtVideoUrl == null || txtVideoUrl.getText() == null) ? null : txtVideoUrl.getText().trim();
            if (video != null && video.isBlank()) video = null;

            Lesson l = (current == null) ? new Lesson() : current;

            l.setFormationId(selectedFormation.getId());
            l.setTitre(titre);
            l.setContenu(txtContenu.getText());
            l.setVideoUrl(video);
            l.setOrdre(ordre);
            l.setDureeMinutes(duree);

            if (current == null) service.add(l);
            else service.update(l);

            if (onSaved != null) onSaved.run();
            close();

        } catch (Exception e) {
            showError("Erreur Save: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() { close(); }

    private int parseInt(String s, String fieldName) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Champ " + fieldName + " doit être un entier");
        }
    }

    private void close() {
        Stage stage = (Stage) txtTitre.getScene().getWindow();
        stage.close();
    }

    private void showWarn(String msg) { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}