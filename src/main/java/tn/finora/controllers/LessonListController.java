package tn.finora.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.finora.entities.Formation;
import tn.finora.entities.Lesson;
import tn.finora.services.FormationService;
import tn.finora.services.LessonService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LessonListController {

    // FXML
    @FXML private ComboBox<Formation> cbFormation;
    @FXML private VBox cardsBox;
    @FXML private Label lblInfo;

    // Services
    private final LessonService lessonService = new LessonService();
    private final FormationService formationService = new FormationService();

    // Selection
    private Lesson selectedLesson;

    // Option B: id -> formation title (no JOIN needed)
    private final Map<Integer, String> formationTitleById = new HashMap<>();

    @FXML
    public void initialize() {
        initFormationCombo();
        refreshAll();
    }

    private void initFormationCombo() {
        try {
            List<Formation> formations = formationService.getAll();
            cbFormation.setItems(FXCollections.observableArrayList(formations));

            formationTitleById.clear();
            for (Formation f : formations) formationTitleById.put(f.getId(), f.getTitre());

            cbFormation.setCellFactory(list -> new ListCell<>() {
                @Override protected void updateItem(Formation item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : ("#" + item.getId() + " - " + item.getTitre()));
                }
            });
            cbFormation.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Formation item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "Toutes les formations" : ("#" + item.getId() + " - " + item.getTitre()));
                }
            });

        } catch (Exception e) {
            showError("Erreur chargement formations: " + e.getMessage());
        }
    }

    // called by onAction="#onFilter" ✅
    @FXML
    private void onFilter() {
        Formation selected = cbFormation.getSelectionModel().getSelectedItem();
        if (selected == null) refreshAll();
        else refreshByFormation(selected.getId());
    }

    private void refreshAll() {
        try {
            selectedLesson = null;
            List<Lesson> list = lessonService.getAll();
            renderCards(list);
            lblInfo.setText("Total lessons: " + list.size());
        } catch (Exception e) {
            showError("Erreur chargement lessons: " + e.getMessage());
        }
    }

    private void refreshByFormation(int formationId) {
        try {
            selectedLesson = null;
            List<Lesson> list = lessonService.getByFormation(formationId);
            renderCards(list);
            lblInfo.setText("Lessons de la formation #" + formationId + ": " + list.size());
        } catch (Exception e) {
            showError("Erreur filtrage lessons: " + e.getMessage());
        }
    }

    private void renderCards(List<Lesson> lessons) {
        cardsBox.getChildren().clear();
        for (Lesson l : lessons) {
            cardsBox.getChildren().add(createLessonCard(l));
        }
    }

    private Pane createLessonCard(Lesson l) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(16));

        // Title
        Label title = new Label(l.getTitre() == null ? "(Sans titre)" : l.getTitre());
        title.getStyleClass().add("card-title");

        // Badges row (Option B for formation title)
        HBox badges = new HBox(8);

        String ft = formationTitleById.getOrDefault(l.getFormationId(), "Formation inconnue");
        Label bFormation = badge("📌 " + "#" + l.getFormationId() + " • " + ft, "badge-purple");
        Label bOrdre = badge("↕ Ordre: " + l.getOrdre(), "badge-gray");
        Label bDuree = badge("⏱ " + l.getDureeMinutes() + " min", "badge-gray");

        badges.getChildren().addAll(bFormation, bOrdre, bDuree);

        // Buttons
        HBox actions = new HBox(10);

        Button btnSelect = new Button("Sélectionner");
        btnSelect.getStyleClass().add("btn-ghost");
        btnSelect.setOnAction(e -> selectLesson(l, card));

        Button btnEdit = new Button("Modifier");
        btnEdit.getStyleClass().add("btn-ghost");
        btnEdit.setOnAction(e -> { selectLesson(l, card); onEdit(); });

        Button btnDel = new Button("Supprimer");
        btnDel.getStyleClass().add("btn-danger");
        btnDel.setOnAction(e -> { selectLesson(l, card); onDelete(); });

        actions.getChildren().addAll(btnSelect, btnEdit, btnDel);

        card.getChildren().addAll(title, badges, actions);
        return card;
    }

    private void selectLesson(Lesson l, VBox card) {
        selectedLesson = l;

        // clear highlight
        for (var n : cardsBox.getChildren()) n.getStyleClass().remove("selected-card");
        card.getStyleClass().add("selected-card");

        lblInfo.setText("Sélection: Lesson #" + l.getId());
    }

    private Label badge(String text, String style) {
        Label b = new Label(text);
        b.getStyleClass().addAll("badge", style);
        return b;
    }

    @FXML
    private void onAdd() { openForm(null); }

    @FXML
    private void onEdit() {
        if (selectedLesson == null) { showWarn("Sélectionne une lesson"); return; }
        openForm(selectedLesson);
    }

    @FXML
    private void onDelete() {
        if (selectedLesson == null) { showWarn("Sélectionne une lesson"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cette lesson ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            try {
                lessonService.delete(selectedLesson.getId());
                onFilter();
            } catch (Exception e) {
                showError("Erreur suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void goFormations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/formation_list.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 650);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            Stage stage = (Stage) cbFormation.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            showError("Impossible d'ouvrir Formations: " + e.getMessage());
        }
    }

    private void openForm(Lesson lesson) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lesson_form.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            LessonFormController controller = loader.getController();
            controller.setOnSaved(this::onFilter);
            controller.setData(lesson);

            controller.setFormations(cbFormation.getItems(), cbFormation.getSelectionModel().getSelectedItem());

            Stage popup = new Stage();
            popup.setTitle(lesson == null ? "Ajouter Lesson" : "Modifier Lesson");
            popup.setScene(scene);
            popup.show();
        } catch (Exception e) {
            showError("Erreur ouverture form: " + e.getMessage());
        }
    }

    private void showWarn(String msg) { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
