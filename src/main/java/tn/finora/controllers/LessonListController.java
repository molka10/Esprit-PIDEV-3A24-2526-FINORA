package tn.finora.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.finora.entities.Formation;
import tn.finora.entities.Lesson;
import tn.finora.services.FormationService;
import tn.finora.services.LessonService;

import java.net.URI;
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

    private VBox createCard(Lesson l) {
        VBox card = new VBox();
        card.getStyleClass().add("ud-lesson-card");
        card.setPrefWidth(320);
        card.setMaxWidth(320);
        card.setUserData(l.getId());

        // ── Header (fake thumbnail) ─────────────────────────────
        StackPane header = new StackPane();
        header.getStyleClass().add("ud-lesson-header");
        header.setPrefHeight(92);

        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.TOP_LEFT);
        headerRow.setPadding(new Insets(10, 12, 10, 12));

        Label chipNum = new Label("LEÇON " + String.format("%02d", l.getOrdre()));
        chipNum.getStyleClass().add("ud-chip-num");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label chipVideo = new Label("🎬 Vidéo");
        chipVideo.getStyleClass().add("ud-chip-video-clickable");

        boolean hasVideo = hasVideo(l);
        chipVideo.setVisible(hasVideo);
        chipVideo.setManaged(hasVideo);

        if (hasVideo) {
            chipVideo.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, ev -> {
                ev.consume();
                openVideo(l.getVideoUrl());
            });
        }

        headerRow.getChildren().addAll(chipNum, spacer, chipVideo);
        header.getChildren().add(headerRow);

        // ── Body ────────────────────────────────────────────────
        VBox body = new VBox(12);
        body.getStyleClass().add("ud-lesson-body");
        body.setPadding(new Insets(14, 14, 14, 14));

        // Title
        Label title = new Label(safe(l.getTitre(), "(Sans titre)"));
        title.getStyleClass().add("ud-lesson-title");
        title.setWrapText(true);
        title.setMaxHeight(48); // visually keeps it like Udemy (2 lines)

        // Chips row (formation + duration)
        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);

        String ft = formationTitleById.getOrDefault(l.getFormationId(), "Formation inconnue");

        Label chipFormation = new Label("📌 " + ft);
        chipFormation.getStyleClass().add("ud-chip-formation");

        Label chipDuration = new Label("⏱ " + l.getDureeMinutes() + " min");
        chipDuration.getStyleClass().add("ud-chip-duration");

        meta.getChildren().addAll(chipFormation, chipDuration);

        // Spacer pushes actions to bottom
        Region grow = new Region();
        VBox.setVgrow(grow, Priority.ALWAYS);

        // Footer actions (bottom-right)
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getStyleClass().add("ud-lesson-actions-bottom");

        Button viewBtn = new Button("Voir");
        viewBtn.getStyleClass().add("ud-btn-primary");

        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("ud-btn-ghost");

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("ud-btn-danger");

        viewBtn.setOnAction(e -> openLessonViewer(l, lastDisplayed));
        editBtn.setOnAction(e -> { selectedLesson = l; openForm(l); });
        deleteBtn.setOnAction(e -> { selectedLesson = l; onDelete(); });

        actions.getChildren().addAll(viewBtn, editBtn, deleteBtn);

        // ✅ No preview text anymore
        body.getChildren().addAll(title, meta, grow, actions);

        card.getChildren().addAll(header, body);

        // select card on click (buttons still work)
        card.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, e -> {
            Object t = e.getTarget();
            if (t instanceof Button) return;
            if (t instanceof Label lab && lab.getStyleClass().contains("ud-chip-video-clickable")) return;
            selectedLesson = l;
            highlightSelection();
        });

        return card;
    }
    private boolean hasVideo(Lesson l) {
        if (l == null) return false;
        String url = l.getVideoUrl(); // ✅ requires Lesson.getVideoUrl()
        return url != null && !url.trim().isBlank();
    }

    private void openVideo(String url) {
        if (url == null || url.isBlank()) {
            showWarn("Aucune vidéo associée à cette leçon.");
            return;
        }

        try {
            // open in default browser
            java.awt.Desktop.getDesktop().browse(new URI(url.trim()));
        } catch (Exception e) {
            showError("Impossible d'ouvrir la vidéo: " + e.getMessage());
        }
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
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());

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
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());

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
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
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

    private String safe(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s.trim();
    }

    private void showWarn(String msg)  { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}