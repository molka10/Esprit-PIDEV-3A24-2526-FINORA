package com.example.finora.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import com.example.finora.entities.Formation;
import com.example.finora.entities.Lesson;
import com.example.finora.HelloApplication;
import com.example.finora.utils.Navigator;
import javafx.scene.Parent;
import com.example.finora.services.FormationService;
import com.example.finora.services.LessonService;
import com.example.finora.services.SpeechToTextService;
import com.example.finora.utils.Session;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class LessonListController {

    @FXML
    private ComboBox<Formation> cbFormation;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbSort;
    @FXML
    private FlowPane cardsBox;
    @FXML
    private Label lblInfo;

    @FXML
    private Button btnAdd;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;

    @FXML
    private Button btnMic;

    // ✅ NEW: "Parlez..." hint label (add in FXML)
    @FXML
    private Label lblVoiceHint;

    private final LessonService lessonService;
    private final FormationService formationService;
    private final SpeechToTextService sttService = new SpeechToTextService();

    public LessonListController() {
        this.lessonService = new LessonService();
        this.formationService = new FormationService();
    }

    private List<Lesson> allLessons = new ArrayList<>();
    private List<Lesson> lastDisplayed = new ArrayList<>();
    private final Map<Integer, String> formationTitleById = new HashMap<>();
    private Lesson selectedLesson;

    @FXML
    public void initialize() {
        initFormationCombo();
        initSort();
        applyRolePermissions();
        loadAllLessons();
        applyAllFilters();
    }

    private void applyRolePermissions() {
        boolean admin = Session.isAdmin();
        if (btnAdd != null) {
            btnAdd.setVisible(admin);
            btnAdd.setManaged(admin);
        }
        if (btnEdit != null) {
            btnEdit.setVisible(admin);
            btnEdit.setManaged(admin);
        }
        if (btnDelete != null) {
            btnDelete.setVisible(admin);
            btnDelete.setManaged(admin);
        }
    }

    private void initFormationCombo() {
        try {
            List<Formation> formations = formationService.getAll();
            cbFormation.setItems(FXCollections.observableArrayList(formations));

            formationTitleById.clear();
            for (Formation f : formations)
                formationTitleById.put(f.getId(), f.getTitre());

            cbFormation.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(Formation item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTitre());
                }
            });

            cbFormation.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Formation item, boolean empty) {
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
                "Durée (décroissante)"));
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

    @FXML
    private void onFilter() {
        applyAllFilters();
    }

    @FXML
    private void onSearch() {
        applyAllFilters();
    }

    @FXML
    private void onSort() {
        applyAllFilters();
    }

    private void applyAllFilters() {
        selectedLesson = null;

        Formation fSel = cbFormation.getSelectionModel().getSelectedItem();
        Integer formationId = (fSel == null) ? null : fSel.getId();

        String q = (txtSearch == null || txtSearch.getText() == null)
                ? ""
                : txtSearch.getText().trim().toLowerCase();

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
        if (sort == null)
            sort = "Ordre (croissant)";
        return switch (sort) {
            case "Titre (A → Z)" -> list.stream().sorted(Comparator.comparing(l -> safeLower(l.getTitre()))).toList();
            case "Titre (Z → A)" ->
                list.stream().sorted(Comparator.comparing((Lesson l) -> safeLower(l.getTitre())).reversed()).toList();
            case "Ordre (décroissant)" ->
                list.stream().sorted((a, b) -> Integer.compare(b.getOrdre(), a.getOrdre())).toList();
            case "Durée (croissante)" ->
                list.stream().sorted(Comparator.comparingInt(Lesson::getDureeMinutes)).toList();
            case "Durée (décroissante)" ->
                list.stream().sorted((a, b) -> Integer.compare(b.getDureeMinutes(), a.getDureeMinutes())).toList();
            case "Dernier ajouté" -> list.stream().sorted((a, b) -> Integer.compare(b.getId(), a.getId())).toList();
            default -> list.stream().sorted(Comparator.comparingInt(Lesson::getOrdre)).toList();
        };
    }

    private void renderCards(List<Lesson> list) {
        cardsBox.getChildren().clear();
        for (Lesson l : list)
            cardsBox.getChildren().add(createCard(l));
    }

    // ===============================
    // ✅ STT VOICE SEARCH + "Parlez..."
    // ===============================
    @FXML
    private void onToggleVoice() {

        if (!sttService.isListening()) {

            btnMic.setText("⏹");
            if (txtSearch != null)
                txtSearch.setPromptText("Parlez…");

            sttService.startListening(text -> {
                Platform.runLater(() -> {
                    if (txtSearch != null)
                        txtSearch.setText(text);
                    onSearch();

                    btnMic.setText("🎤");
                    if (txtSearch != null)
                        txtSearch.setPromptText("Rechercher...");
                });
            });

        } else {
            sttService.stopListening();

            btnMic.setText("🎤");
            if (txtSearch != null)
                txtSearch.setPromptText("Rechercher...");
        }
    }

    private void resetVoiceUi() {
        if (btnMic != null) {
            btnMic.setText("🎤");
            btnMic.setDisable(false);
        }
        if (lblVoiceHint != null) {
            lblVoiceHint.setText("");
            lblVoiceHint.setVisible(false);
            lblVoiceHint.setManaged(false);
        }
    }

    // ===============================

    // ------- your existing code below (unchanged) -------

    private VBox createCard(Lesson l) {
        VBox card = new VBox();
        card.getStyleClass().add("ud-lesson-card");
        card.setPrefWidth(320);
        card.setMaxWidth(320);
        card.setUserData(l.getId());

        // ================= HEADER =================
        StackPane header = new StackPane();
        header.getStyleClass().add("ud-lesson-header");
        header.setPrefHeight(96);
        header.setMinHeight(96);
        header.setMaxHeight(96);

        // Clip the whole header (image + overlay + chips) with rounded corners
        Rectangle headerClip = new Rectangle();
        headerClip.setArcWidth(18);
        headerClip.setArcHeight(18);
        headerClip.widthProperty().bind(header.widthProperty());
        headerClip.heightProperty().bind(header.heightProperty());
        header.setClip(headerClip);

        // Thumbnail background
        ImageView thumb = new ImageView();
        thumb.setPreserveRatio(false);
        thumb.fitWidthProperty().bind(header.widthProperty());
        thumb.fitHeightProperty().bind(header.heightProperty());
        thumb.setSmooth(true);

        String thumbUrl = getThumbnailUrl(l);
        if (thumbUrl != null && !thumbUrl.isBlank()) {
            thumb.setImage(new Image(thumbUrl, true));
        } else {
            var is = getClass().getResourceAsStream("/images/lesson_placeholder.png");
            if (is != null)
                thumb.setImage(new Image(is));
        }

        // Dark overlay so text stays readable
        Region overlay = new Region();
        overlay.setStyle("""
                    -fx-background-color: linear-gradient(to bottom, rgba(0,0,0,0.10), rgba(0,0,0,0.40));
                """);

        // Chips row (top)
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.TOP_LEFT);
        headerRow.setPadding(new Insets(10, 12, 10, 12));

        Label chipNum = new Label("LEÇON " + String.format("%02d", l.getOrdre()));
        chipNum.getStyleClass().add("ud-chip-num");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boolean hasVideo = hasVideo(l);

        Label chipVideo = new Label("🎬 Vidéo");
        chipVideo.getStyleClass().add("ud-chip-video-clickable");
        chipVideo.setVisible(hasVideo);
        chipVideo.setManaged(hasVideo);

        if (hasVideo) {
            chipVideo.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, ev -> {
                ev.consume();
                openVideo(l.getVideoUrl());
            });
        }

        headerRow.getChildren().addAll(chipNum, spacer, chipVideo);

        // Order: image -> overlay -> chips row
        header.getChildren().addAll(thumb, overlay, headerRow);

        // Play icon in center
        if (hasVideo) {
            Label play = new Label("▶");
            play.getStyleClass().add("ud-play-overlay");
            StackPane.setAlignment(play, Pos.CENTER);
            header.getChildren().add(play);
        }

        // ================= BODY =================
        VBox body = new VBox(12);
        body.getStyleClass().add("ud-lesson-body");
        body.setPadding(new Insets(14, 14, 14, 14));

        Label title = new Label(safe(l.getTitre(), "(Sans titre)"));
        title.getStyleClass().add("ud-lesson-title");
        title.setWrapText(true);
        title.setMaxHeight(48);

        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);

        String ft = formationTitleById.getOrDefault(l.getFormationId(), "Formation inconnue");

        Label chipFormation = new Label("📌 " + ft);
        chipFormation.getStyleClass().add("ud-chip-formation");

        Label chipDuration = new Label("⏱ " + l.getDureeMinutes() + " min");
        chipDuration.getStyleClass().add("ud-chip-duration");

        meta.getChildren().addAll(chipFormation, chipDuration);

        Region grow = new Region();
        VBox.setVgrow(grow, Priority.ALWAYS);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getStyleClass().add("ud-lesson-actions-bottom");

        Button viewBtn = new Button("Voir");
        viewBtn.getStyleClass().add("ud-btn-primary");
        viewBtn.setOnAction(e -> openLessonViewer(l, lastDisplayed));

        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().addAll("ud-btn-ghost", "ud-hover-action");
        editBtn.setOnAction(e -> {
            selectedLesson = l;
            openForm(l);
        });

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().addAll("ud-btn-danger", "ud-hover-action");
        deleteBtn.setOnAction(e -> {
            selectedLesson = l;
            onDelete();
        });

        if (!Session.isAdmin()) {
            editBtn.setVisible(false);
            editBtn.setManaged(false);
            deleteBtn.setVisible(false);
            deleteBtn.setManaged(false);
        }

        actions.getChildren().addAll(viewBtn, editBtn, deleteBtn);

        body.getChildren().addAll(title, meta, grow, actions);

        card.getChildren().addAll(header, body);

        // Card click selection (ignore buttons / video chip)
        card.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, e -> {
            Object t = e.getTarget();
            if (t instanceof Button)
                return;
            if (t instanceof Label lab && lab.getStyleClass().contains("ud-chip-video-clickable"))
                return;
            selectedLesson = l;
            highlightSelection();
        });

        return card;
    }

    private boolean hasVideo(Lesson l) {
        if (l == null)
            return false;
        String url = l.getVideoUrl();
        return url != null && !url.trim().isBlank();
    }

    private void openVideo(String url) {
        if (url == null || url.isBlank()) {
            showWarn("Aucune vidéo associée.");
            return;
        }
        try {
            java.awt.Desktop.getDesktop().browse(new URI(url.trim()));
        } catch (Exception e) {
            showError("Impossible d'ouvrir la vidéo: " + e.getMessage());
        }
    }

    private void highlightSelection() {
        for (var node : cardsBox.getChildren()) {
            if (node instanceof VBox v) {
                boolean isSel = selectedLesson != null && selectedLesson.getId() == (int) v.getUserData();
                v.getStyleClass().remove("lesson-card-selected");
                if (isSel)
                    v.getStyleClass().add("lesson-card-selected");
            }
        }
    }

    public void setSelectedFormation(Formation formation) {
        if (formation == null)
            return;
        cbFormation.getSelectionModel().select(formation);
        cbSort.getSelectionModel().select("Ordre (croissant)");
        applyAllFilters();
    }

    private void openForm(Lesson lesson) {
        if (!Session.isAdmin())
            return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/formation/lesson_form.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets()
                    .add(Objects.requireNonNull(getClass().getResource("/formation/style.css")).toExternalForm());

            LessonFormController controller = loader.getController();
            controller.setOnSaved(() -> {
                loadAllLessons();
                applyAllFilters();
            });
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/formation/lesson_view.fxml"));
            Parent root = loader.load();

            LessonViewController controller = loader.getController();

            Formation formation = null;
            for (Formation f : cbFormation.getItems()) {
                if (f.getId() == lesson.getFormationId()) {
                    formation = f;
                    break;
                }
            }

            int idx = 0;
            if (displayedList != null) {
                for (int i = 0; i < displayedList.size(); i++) {
                    if (displayedList.get(i).getId() == lesson.getId()) {
                        idx = i;
                        break;
                    }
                }
            }

            controller.setLessons(displayedList, idx, formation);

            Stage stage = (Stage) cardsBox.getScene().getWindow();
            controller.setOnBack(() -> Navigator.goTo(stage, "/formation/lesson_list.fxml", "Finora Academy - Leçons"));

            if (stage.getScene() != null
                    && stage.getScene().lookup("#contentArea") instanceof javafx.scene.layout.StackPane contentArea) {
                contentArea.getChildren().setAll(root);
            } else {
                Scene scene = new Scene(root, 1200, 700);
                scene.getStylesheets().add(getClass().getResource("/formation/style.css").toExternalForm());
                stage.setScene(scene);
            }
        } catch (Exception e) {
            showError("Impossible d'ouvrir la lesson: " + e.getMessage());
        }
    }

    @FXML
    private void onAdd() {
        if (Session.isAdmin())
            openForm(null);
    }

    @FXML
    private void onEdit() {
        if (!Session.isAdmin())
            return;
        if (selectedLesson == null) {
            showWarn("Sélectionne une lesson");
            return;
        }
        openForm(selectedLesson);
    }

    @FXML
    private void onDelete() {
        if (!Session.isAdmin())
            return;
        if (selectedLesson == null) {
            showWarn("Sélectionne une lesson");
            return;
        }

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

    private String getThumbnailUrl(Lesson l) {
        if (l == null)
            return null;

        // ✅ If you later add a field in DB like lesson.thumbnailUrl, use it here
        // Example:
        // if (l.getThumbnailUrl() != null && !l.getThumbnailUrl().isBlank()) return
        // l.getThumbnailUrl();

        // ✅ If video is YouTube, generate thumbnail automatically
        String video = l.getVideoUrl();
        if (video == null)
            return null;

        String id = extractYouTubeId(video);
        if (id != null) {
            // good quality thumbnail
            return "https://img.youtube.com/vi/" + id + "/hqdefault.jpg";
        }

        return null;
    }

    private String extractYouTubeId(String url) {
        if (url == null)
            return null;
        url = url.trim();

        // formats:
        // https://www.youtube.com/watch?v=VIDEO_ID
        // https://youtu.be/VIDEO_ID
        try {
            if (url.contains("watch?v=")) {
                String part = url.substring(url.indexOf("watch?v=") + 8);
                int amp = part.indexOf('&');
                return (amp > 0) ? part.substring(0, amp) : part;
            }
            if (url.contains("youtu.be/")) {
                String part = url.substring(url.indexOf("youtu.be/") + 9);
                int q = part.indexOf('?');
                return (q > 0) ? part.substring(0, q) : part;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    @FXML
    private void goFormations() {
        Stage stage = (Stage) cardsBox.getScene().getWindow();
        Navigator.goTo(stage, "/formation/formation_list.fxml", "Finora Academy - Formations");
    }

    @FXML
    private void goRoleChoice() {
        HelloApplication.showRoleChoice();
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private String safe(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s.trim();
    }

    private void showWarn(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).showAndWait();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}