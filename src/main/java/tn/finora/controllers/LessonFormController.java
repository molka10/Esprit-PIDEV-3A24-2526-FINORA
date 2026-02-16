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

    // ✅ instead of TextField formationId
    @FXML private ComboBox<Formation> cbFormation;

    @FXML private TextField txtTitre;
    @FXML private TextArea txtContenu;
    @FXML private TextField txtOrdre;
    @FXML private TextField txtDuree;

    private final LessonService service = new LessonService();

    private Lesson current;
    private Runnable onSaved;

    public void setOnSaved(Runnable onSaved) { this.onSaved = onSaved; }

    // called from LessonListController
    public void setFormations(List<Formation> formations, Formation preselected) {
        cbFormation.setItems(FXCollections.observableArrayList(formations));

        // show nice text in combobox
        cbFormation.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(Formation item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : ("#" + item.getId() + " - " + item.getTitre()));
            }
        });
        cbFormation.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Formation item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Choisir une formation..." : ("#" + item.getId() + " - " + item.getTitre()));
            }
        });

        if (preselected != null) cbFormation.getSelectionModel().select(preselected);
    }

    public void setData(Lesson l) {
        this.current = l;

        if (l != null) {
            txtTitre.setText(l.getTitre());
            txtContenu.setText(l.getContenu());
            txtOrdre.setText(String.valueOf(l.getOrdre()));
            txtDuree.setText(String.valueOf(l.getDureeMinutes()));
            // cbFormation selection handled below if formations already loaded
        }
    }

    @FXML
    private void onSave() {
        try {
            // ✅ must choose formation (no typing ID)
            Formation selectedFormation = cbFormation.getSelectionModel().getSelectedItem();
            if (selectedFormation == null) {
                showWarn("Choisis une formation");
                return;
            }

            String titre = txtTitre.getText().trim();
            if (titre.isEmpty()) { showWarn("Titre obligatoire"); return; }

            int ordre = parseInt(txtOrdre.getText(), "ordre");
            int duree = parseInt(txtDuree.getText(), "durée");

            Lesson l = (current == null) ? new Lesson() : current;

            l.setFormationId(selectedFormation.getId());
            l.setTitre(titre);
            l.setContenu(txtContenu.getText());
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
