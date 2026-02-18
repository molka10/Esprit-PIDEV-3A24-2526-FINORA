package tn.finora.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.finora.entities.Formation;
import tn.finora.entities.Lesson;
import tn.finora.services.FormationService;
import tn.finora.services.LessonService;

import java.util.*;
import java.util.stream.Collectors;

public class LessonListController {

    // ===== FXML =====
    @FXML private ComboBox<Formation> cbFormation;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbSort;

    @FXML private VBox cardsBox;
    @FXML private Label lblInfo;

    // ===== Services =====
    private final LessonService lessonService = new LessonService();
    private final FormationService formationService = new FormationService();

    // ===== Data =====
    private List<Lesson> allLessons = new ArrayList<>();
    private List<Lesson> lastDisplayed = new ArrayList<>(); // ✅ for Next/Prev
    private final Map<Integer, String> formationTitleById = new HashMap<>();

    // ===== Selection =====
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

    private void initSort() {
        cbSort.setItems(FXCollections.observableArrayList(
                "Dernier ajouté",
                "Titre (A → Z)",
                "Titre (Z → A)",
                "Ordre (croissant)",
                "Ordre (décroissant)",
                "Durée (croissante)",
                "Durée (décroissante)"
        ));
        cbSort.getSelectionModel().select("Dernier ajouté");
    }

    private void loadAllLessons() {
        try {
            allLessons = lessonService.getAll();
        } catch (Exception e) {
            showError("Erreur chargement lessons: " + e.getMessage());
            allLessons = new ArrayList<>();
        }
    }

    // ===== EVENTS =====
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

        // 1) filter by formation
        List<Lesson> filtered = allLessons.stream()
                .filter(l -> formationId == null || l.getFormationId() == formationId)
                .collect(Collectors.toList());

        // 2) search by titre/contenu
        filtered = filtered.stream()
                .filter(l -> q.isEmpty()
                        || safeLower(l.getTitre()).contains(q)
                        || safeLower(l.getContenu()).contains(q))
                .collect(Collectors.toList());

        // 3) sort
        filtered = sortLessons(filtered, sort);

        // ✅ store what is displayed (needed for Next/Prev)
        lastDisplayed = filtered;

        // 4) render cards
        renderCards(filtered);

        if (lblInfo != null) {
            lblInfo.setText("Affichées: " + filtered.size() + " / " + allLessons.size()
                    + (formationId == null ? "" : "  |  Formation #" + formationId));
        }
    }

    private List<Lesson> sortLessons(List<Lesson> list, String sort) {
        if (sort == null) sort = "Dernier ajouté";

        Comparator<Lesson> byIdDesc = (a, b) -> Integer.compare(b.getId(), a.getId());

        return switch (sort) {
            case "Titre (A → Z)" -> list.stream()
                    .sorted(Comparator.comparing(l -> safeLower(l.getTitre())))
                    .toList();
            case "Titre (Z → A)" -> list.stream()
                    .sorted(Comparator.comparing((Lesson l) -> safeLower(l.getTitre())).reversed())
                    .toList();
            case "Ordre (croissant)" -> list.stream()
                    .sorted(Comparator.comparingInt(Lesson::getOrdre))
                    .toList();
            case "Ordre (décroissant)" -> list.stream()
                    .sorted((a, b) -> Integer.compare(b.getOrdre(), a.getOrdre()))
                    .toList();
            case "Durée (croissante)" -> list.stream()
                    .sorted(Comparator.comparingInt(Lesson::getDureeMinutes))
                    .toList();
            case "Durée (décroissante)" -> list.stream()
                    .sorted((a, b) -> Integer.compare(b.getDureeMinutes(), a.getDureeMinutes()))
                    .toList();
            default -> list.stream().sorted(byIdDesc).toList();
        };
    }

    private void renderCards(List<Lesson> list) {
        cardsBox.getChildren().clear();
        for (Lesson l : list) {
            cardsBox.getChildren().add(createCard(l));
        }
    }

    private VBox createCard(Lesson l) {
        VBox card = new VBox(10);
        card.getStyleClass().add("item-card");
        card.setPadding(new Insets(16));
        card.setUserData(l.getId());

        Label title = new Label(l.getTitre() == null ? "(Sans titre)" : l.getTitre());
        title.getStyleClass().add("item-title");

        HBox badges = new HBox(8);

        String ft = formationTitleById.getOrDefault(l.getFormationId(), "Formation inconnue");
        Label bFormation = new Label("📌" + ft);
        bFormation.getStyleClass().addAll("badge", "badge-purple");

        Label bOrdre = new Label("↕ Ordre: " + l.getOrdre());
        bOrdre.getStyleClass().addAll("badge", "badge-gray");

        Label bDuree = new Label("⏱ " + l.getDureeMinutes() + " min");
        bDuree.getStyleClass().addAll("badge", "badge-green");

        badges.getChildren().addAll(bFormation, bOrdre, bDuree);

        Label content = new Label(preview(l.getContenu(), 120));
        content.setWrapText(true);
        content.setStyle("-fx-text-fill: #334155; -fx-font-size: 12px;");

        HBox actions = new HBox(10);

        Button viewBtn = new Button("Voir");
        viewBtn.getStyleClass().add("btn-ghost");
        viewBtn.setOnAction(e -> openLessonViewer(l, lastDisplayed)); // ✅ FIX

        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("btn-ghost");
        editBtn.setOnAction(e -> { selectedLesson = l; openForm(l); });

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> { selectedLesson = l; onDelete(); });

        actions.getChildren().addAll(viewBtn, editBtn, deleteBtn);

        card.getChildren().addAll(title, badges, content, actions);
        card.setOnMouseClicked(e -> { selectedLesson = l; highlightSelection(); });

        return card;
    }

    private void highlightSelection() {
        for (var node : cardsBox.getChildren()) {
            if (node instanceof VBox v) {
                boolean isSel = selectedLesson != null && selectedLesson.getId() == (int) v.getUserData();
                v.getStyleClass().remove("item-selected");
                if (isSel) v.getStyleClass().add("item-selected");
            }
        }
    }

    private void openForm(Lesson lesson) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lesson_form.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            LessonFormController controller = loader.getController();
            controller.setOnSaved(() -> {
                loadAllLessons();
                applyAllFilters();
            });
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
    public void setSelectedFormation(Formation formation) {
        if (formation == null) return;

        // select it in combo
        cbFormation.getSelectionModel().select(formation);

        // apply filter after selection
        applyAllFilters();
    }


    // ===== Lesson Viewer (Next/Prev) =====
    private void openLessonViewer(Lesson lesson, List<Lesson> displayedList) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lesson_view.fxml"));
            Scene scene = new Scene(loader.load(), 950, 650);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            LessonViewController controller = loader.getController();

            // find formation object for better display
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

            Stage popup = new Stage();
            popup.setTitle("Lesson - " + lesson.getTitre());
            popup.setScene(scene);
            popup.show();

        } catch (Exception e) {
            showError("Impossible d'ouvrir la lesson: " + e.getMessage());
        }
    }

    // ===== Buttons =====
    @FXML private void onAdd() { openForm(null); }

    @FXML
    private void onEdit() {
        if (selectedLesson == null) { showWarn("Sélectionne une lesson (clique sur une carte)"); return; }
        openForm(selectedLesson);
    }

    @FXML
    private void onDelete() {
        if (selectedLesson == null) { showWarn("Sélectionne une lesson (clique sur une carte)"); return; }

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

    // ===== Helpers =====
    private String safeLower(String s) { return s == null ? "" : s.toLowerCase(); }

    private String preview(String s, int max) {
        if (s == null) return "";
        s = s.trim();
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...";
    }

    private void showWarn(String msg) { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
