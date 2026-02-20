package tn.finora.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.finora.entities.Formation;
import tn.finora.entities.Lesson;
import tn.finora.services.FormationService;
import tn.finora.services.LessonService;

import java.util.*;
import java.util.stream.Collectors;

public class LessonListController {

    @FXML private ComboBox<Formation> cbFormation;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbSort;
    @FXML private FlowPane cardsBox;      // ✅ Grid container
    @FXML private Label lblInfo;

    private final LessonService lessonService = new LessonService();
    private final FormationService formationService = new FormationService();

    private List<Lesson> allLessons = new ArrayList<>();
    private List<Lesson> lastDisplayed = new ArrayList<>();
    private final Map<Integer, String> formationTitleById = new HashMap<>();
    private Lesson selectedLesson;

    @FXML
    public void initialize() {
        initFormationCombo();
        initSort();
        loadAllLessons();
        applyAllFilters();
    }

    private void initFormationCombo() {
        try {
            List<Formation> formations = formationService.getAll();
            cbFormation.setItems(FXCollections.observableArrayList(formations));

            formationTitleById.clear();
            for (Formation f : formations)
                formationTitleById.put(f.getId(), f.getTitre());

            // Title only
            cbFormation.setCellFactory(list -> new ListCell<>() {
                @Override protected void updateItem(Formation item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTitre());
                }
            });
            cbFormation.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Formation item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "Toutes les formations" : item.getTitre());
                }
            });

        } catch (Exception e) {
            showError("Erreur chargement formations: " + e.getMessage());
        }
    }

    private void initSort() {
        cbSort.setItems(FXCollections.observableArrayList(
                "Ordre (croissant)",
                "Dernier ajouté",
                "Titre (A → Z)",
                "Titre (Z → A)",
                "Ordre (décroissant)",
                "Durée (croissante)",
                "Durée (décroissante)"
        ));
        cbSort.getSelectionModel().select("Ordre (croissant)");
    }

    private void loadAllLessons() {
        try {
            allLessons = lessonService.getAll();
        } catch (Exception e) {
            showError("Erreur chargement lessons: " + e.getMessage());
            allLessons = new ArrayList<>();
        }
    }

    @FXML private void onFilter() { applyAllFilters(); }
    @FXML private void onSearch() { applyAllFilters(); }
    @FXML private void onSort()   { applyAllFilters(); }

    private void applyAllFilters() {
        selectedLesson = null;

        Formation fSel = cbFormation.getSelectionModel().getSelectedItem();
        Integer formationId = (fSel == null) ? null : fSel.getId();

        String q = (txtSearch == null || txtSearch.getText() == null)
                ? "" : txtSearch.getText().trim().toLowerCase();

        String sort = cbSort.getSelectionModel().getSelectedItem();

        List<Lesson> filtered = allLessons.stream()
                .filter(l -> formationId == null || l.getFormationId() == formationId)
                .filter(l -> q.isEmpty()
                        || safeLower(l.getTitre()).contains(q)
                        || safeLower(l.getContenu()).contains(q))
                .collect(Collectors.toList());

        filtered = sortLessons(filtered, sort);
        lastDisplayed = filtered;

        renderCards(filtered);

        if (lblInfo != null) {
            lblInfo.setText("Affichées: " + filtered.size() + " / " + allLessons.size()
                    + (fSel == null ? "" : "  |  " + fSel.getTitre()));
        }
    }

    private List<Lesson> sortLessons(List<Lesson> list, String sort) {
        if (sort == null) sort = "Ordre (croissant)";
        return switch (sort) {
            case "Titre (A → Z)"        -> list.stream().sorted(Comparator.comparing(l -> safeLower(l.getTitre()))).toList();
            case "Titre (Z → A)"        -> list.stream().sorted(Comparator.comparing((Lesson l) -> safeLower(l.getTitre())).reversed()).toList();
            case "Ordre (décroissant)"  -> list.stream().sorted((a, b) -> Integer.compare(b.getOrdre(), a.getOrdre())).toList();
            case "Durée (croissante)"   -> list.stream().sorted(Comparator.comparingInt(Lesson::getDureeMinutes)).toList();
            case "Durée (décroissante)" -> list.stream().sorted((a, b) -> Integer.compare(b.getDureeMinutes(), a.getDureeMinutes())).toList();
            case "Dernier ajouté"       -> list.stream().sorted((a, b) -> Integer.compare(b.getId(), a.getId())).toList();
            default                     -> list.stream().sorted(Comparator.comparingInt(Lesson::getOrdre)).toList();
        };
    }

    private void renderCards(List<Lesson> list) {
        cardsBox.getChildren().clear();
        for (Lesson l : list) {
            cardsBox.getChildren().add(createCard(l));
        }
    }

    // ✅ Grid square card + 🎬 video badge
    private VBox createCard(Lesson l) {
        VBox card = new VBox(0);
        card.getStyleClass().add("lesson-square-card");
        card.setPrefWidth(260);
        card.setPrefHeight(260);
        card.setMaxWidth(260);
        card.setMaxHeight(260);
        card.setUserData(l.getId());

        // Top accent
        HBox accent = new HBox();
        accent.setPrefHeight(5);
        accent.setMaxHeight(5);
        accent.setStyle(
                "-fx-background-color: linear-gradient(to right, #7C3AED, #A855F7);" +
                        "-fx-background-radius: 14 14 0 0;"
        );

        VBox body = new VBox(10);
        body.setPadding(new Insets(16, 18, 16, 18));
        VBox.setVgrow(body, Priority.ALWAYS);

        // Title row
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label orderCircle = new Label(String.valueOf(l.getOrdre()));
        orderCircle.setStyle(
                "-fx-background-color: #7C3AED;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-min-width: 30px;" +
                        "-fx-min-height: 30px;" +
                        "-fx-max-width: 30px;" +
                        "-fx-max-height: 30px;" +
                        "-fx-alignment: center;"
        );

        Label title = new Label(l.getTitre() == null ? "(Sans titre)" : l.getTitre());
        title.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-font-family: 'Georgia', serif;" +
                        "-fx-text-fill: #1E0A3C;"
        );
        title.setWrapText(true);
        title.setMaxWidth(190);
        HBox.setHgrow(title, Priority.ALWAYS);

        titleRow.getChildren().addAll(orderCircle, title);

        // Badges row: formation + duration + (NEW) video badge
        String ft = formationTitleById.getOrDefault(l.getFormationId(), "Formation inconnue");
        Label formation = new Label("📌  " + ft);
        formation.getStyleClass().addAll("badge", "badge-purple");
        formation.setMaxWidth(220);

        Label duree = new Label("⏱  " + l.getDureeMinutes() + " min");
        duree.getStyleClass().addAll("badge", "badge-green");

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);
        badges.getChildren().addAll(formation, duree);

        // ✅ NEW: show badge only if video URL exists
        if (hasVideo(l)) {
            Label video = new Label("🎬  Vidéo");
            video.getStyleClass().addAll("badge", "badge-video");
            badges.getChildren().add(video);
        }

        // Preview
        Label preview = new Label(preview(l.getContenu(), 80));
        preview.setWrapText(true);
        preview.setStyle(
                "-fx-text-fill: #6B5A8A;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-family: 'Segoe UI', Arial, sans-serif;"
        );
        VBox.setVgrow(preview, Priority.ALWAYS);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Actions
        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button viewBtn   = new Button("Voir");
        Button editBtn   = new Button("Modifier");
        Button deleteBtn = new Button("Supprimer");

        viewBtn.getStyleClass().add("btn-primary");
        editBtn.getStyleClass().add("btn-ghost");
        deleteBtn.getStyleClass().add("btn-danger");

        String compactStyle = "-fx-padding: 6 12 6 12; -fx-font-size: 11px;";
        viewBtn.setStyle(compactStyle);
        editBtn.setStyle(compactStyle);
        deleteBtn.setStyle(compactStyle);

        viewBtn.setOnAction(e -> openLessonViewer(l, lastDisplayed));
        editBtn.setOnAction(e -> { selectedLesson = l; openForm(l); });
        deleteBtn.setOnAction(e -> { selectedLesson = l; onDelete(); });

        actions.getChildren().addAll(viewBtn, editBtn, deleteBtn);

        body.getChildren().addAll(titleRow, badges, preview, spacer, actions);
        card.getChildren().addAll(accent, body);

        // Selection click (buttons remain clickable)
        card.setOnMouseClicked(e -> {
            if (e.getTarget() instanceof Button) return;
            selectedLesson = l;
            highlightSelection();
        });

        return card;
    }

    private boolean hasVideo(Lesson l) {
        if (l == null) return false;
        String url = l.getVideoUrl(); // requires your updated Lesson.java
        return url != null && !url.trim().isBlank();
    }

    private void highlightSelection() {
        for (var node : cardsBox.getChildren()) {
            if (node instanceof VBox v) {
                boolean isSel = selectedLesson != null
                        && selectedLesson.getId() == (int) v.getUserData();
                v.getStyleClass().remove("lesson-card-selected");
                if (isSel) v.getStyleClass().add("lesson-card-selected");
            }
        }
    }

    public void setSelectedFormation(Formation formation) {
        if (formation == null) return;
        cbFormation.getSelectionModel().select(formation);
        cbSort.getSelectionModel().select("Ordre (croissant)");
        applyAllFilters();
    }

    private void openForm(Lesson lesson) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lesson_form.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            LessonFormController controller = loader.getController();
            controller.setOnSaved(() -> { loadAllLessons(); applyAllFilters(); });
            controller.setData(lesson);
            controller.setFormations(cbFormation.getItems(),
                    cbFormation.getSelectionModel().getSelectedItem());

            Stage popup = new Stage();
            popup.setTitle(lesson == null ? "Ajouter Lesson" : "Modifier Lesson");
            popup.setScene(scene);
            popup.show();
        } catch (Exception e) {
            showError("Erreur ouverture form: " + e.getMessage());
        }
    }

    private void openLessonViewer(Lesson lesson, List<Lesson> displayedList) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lesson_view.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 720);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            LessonViewController controller = loader.getController();

            Formation formation = null;
            for (Formation f : cbFormation.getItems()) {
                if (f.getId() == lesson.getFormationId()) { formation = f; break; }
            }

            int idx = 0;
            if (displayedList != null) {
                for (int i = 0; i < displayedList.size(); i++) {
                    if (displayedList.get(i).getId() == lesson.getId()) { idx = i; break; }
                }
            }

            controller.setLessons(displayedList, idx, formation);

            Stage popup = new Stage();
            popup.setTitle("Leçon — " + lesson.getTitre());
            popup.setScene(scene);
            popup.show();
        } catch (Exception e) {
            showError("Impossible d'ouvrir la lesson: " + e.getMessage());
        }
    }

    @FXML private void onAdd() { openForm(null); }

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
                loadAllLessons();
                applyAllFilters();
            } catch (Exception e) {
                showError("Erreur suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void goFormations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/formation_list.fxml"));
            Scene scene = new Scene(loader.load(), 1250, 720);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            Stage stage = (Stage) cbFormation.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            showError("Impossible d'ouvrir Formations: " + e.getMessage());
        }
    }

    private String safeLower(String s) { return s == null ? "" : s.toLowerCase(); }

    private String preview(String s, int max) {
        if (s == null) return "";
        s = s.trim();
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private void showWarn(String msg)  { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}