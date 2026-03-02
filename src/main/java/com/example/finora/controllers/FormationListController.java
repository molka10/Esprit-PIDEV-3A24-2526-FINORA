package com.example.finora.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.example.finora.entities.Formation;
import com.example.finora.HelloApplication;
import com.example.finora.services.FormationService;
import com.example.finora.services.SpeechToTextService;
import com.example.finora.utils.Navigator;
import com.example.finora.utils.Session;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class FormationListController {

    @FXML
    private VBox cardsContainer;
    @FXML
    private Label lblInfo;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbSort;

    @FXML
    private Button btnAdd;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnQuizResults;

    @FXML
    private Button btnMic;

    // ✅ NEW: "Parlez..." hint label (add in FXML)
    @FXML
    private Label lblVoiceHint;

    private final FormationService service;
    private final SpeechToTextService sttService = new SpeechToTextService();

    public FormationListController() {
        this.service = new FormationService();
    }

    private Formation selected;
    private List<Formation> allFormations = new ArrayList<>();

    @FXML
    public void initialize() {
        initSort();
        applyRolePermissions();
        refresh();
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

        if (btnQuizResults != null) {
            btnQuizResults.setVisible(admin);
            btnQuizResults.setManaged(admin);
        }
    }

    private void initSort() {
        if (cbSort == null)
            return;
        if (cbSort.getItems() == null || cbSort.getItems().isEmpty()) {
            cbSort.setItems(FXCollections.observableArrayList(
                    "Dernier ajouté",
                    "Titre (A → Z)",
                    "Titre (Z → A)",
                    "Catégorie (A → Z)",
                    "Niveau (A → Z)",
                    "Publié d'abord"));
        }
        cbSort.getSelectionModel().select("Dernier ajouté");
        cbSort.setDisable(cbSort.getItems().isEmpty());
    }

    private void refresh() {
        try {
            selected = null;
            allFormations = service.getAll();
            applySearchAndSort();
        } catch (Exception e) {
            showError("Erreur chargement formations: " + e.getMessage());
        }
    }

    @FXML
    private void onSearch() {
        applySearchAndSort();
    }

    @FXML
    private void onSort() {
        applySearchAndSort();
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
                javafx.application.Platform.runLater(() -> {
                    if (txtSearch != null)
                        txtSearch.setText(text);
                    onSearch();

                    btnMic.setText("🎤");
                    if (txtSearch != null)
                        txtSearch.setPromptText("Rechercher (titre / catégorie / niveau)...");
                });
            });

        } else {
            sttService.stopListening();

            btnMic.setText("🎤");
            if (txtSearch != null)
                txtSearch.setPromptText("Rechercher (titre / catégorie / niveau)...");
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

    @FXML
    private void onAdd() {
        if (!Session.isAdmin())
            return;
        openForm(null);
    }

    @FXML
    private void onOpenCenterMap() {
        try {
            var url = getClass().getResource("/map/center_map.fxml");
            if (url == null) {
                new Alert(Alert.AlertType.ERROR,
                        "FXML not found: /map/center_map.fxml\n" +
                                "Make sure it exists in src/main/resources/map/")
                        .showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Find a Training Center");
            stage.setScene(new Scene(root, 1100, 700));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Cannot open map: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onFindCenter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/map.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 650);
            Stage st = new Stage();
            st.setTitle("Find a Center");
            st.setScene(scene);
            st.show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onEdit() {
        if (!Session.isAdmin())
            return;
        if (selected == null) {
            showWarn("Sélectionne une formation");
            return;
        }
        openForm(selected);
    }

    @FXML
    private void onDelete() {
        if (!Session.isAdmin())
            return;
        if (selected == null) {
            showWarn("Sélectionne une formation");
            return;
        }
        deleteFormation(selected);
    }

    @FXML
    private void goLessons() {
        Stage stage = (Stage) cardsContainer.getScene().getWindow();
        Navigator.goTo(stage, "/formation/lesson_list.fxml", "Finora Academy - Leçons");
    }

    @FXML
    private void goRoleChoice() {
        HelloApplication.showRoleChoice();
    }

    @FXML
    private void goQuizResults() {
        if (!Session.isAdmin())
            return;
        Stage stage = (Stage) cardsContainer.getScene().getWindow();
        Navigator.goTo(stage, "/formation/quiz_results.fxml", "Finora Academy - Résultats Quiz");
    }

    private void applySearchAndSort() {
        if (cardsContainer == null)
            return;
        cardsContainer.getChildren().clear();
        selected = null;

        String q = (txtSearch == null || txtSearch.getText() == null)
                ? ""
                : txtSearch.getText().trim().toLowerCase();

        String sort = (cbSort == null) ? "Dernier ajouté"
                : cbSort.getSelectionModel().getSelectedItem();

        List<Formation> filtered = allFormations.stream()
                .filter(f -> matches(f, q)).toList();

        List<Formation> sorted = sortFormations(filtered, sort);

        for (Formation f : sorted) {
            cardsContainer.getChildren().add(createCard(f));
        }

        updateInfo(sorted.size(), allFormations.size());
    }

    private boolean matches(Formation f, String q) {
        if (q.isEmpty())
            return true;
        String titre = safeLower(f.getTitre());
        String niveau = safeLower(f.getNiveau());
        List<String> tags = parseTags(f.getCategorie());
        boolean tagMatch = tags.stream().anyMatch(t -> t.toLowerCase().contains(q));
        return titre.contains(q) || niveau.contains(q) || tagMatch;
    }

    private List<Formation> sortFormations(List<Formation> list, String sort) {
        if (sort == null)
            sort = "Dernier ajouté";
        Comparator<Formation> byIdDesc = (a, b) -> Integer.compare(b.getId(), a.getId());
        return switch (sort) {
            case "Titre (A → Z)" -> list.stream().sorted(Comparator.comparing(a -> safeLower(a.getTitre()))).toList();
            case "Titre (Z → A)" -> list.stream()
                    .sorted(Comparator.comparing((Formation a) -> safeLower(a.getTitre())).reversed()).toList();
            case "Catégorie (A → Z)" ->
                list.stream().sorted(Comparator.comparing(a -> firstTagLower(a.getCategorie()))).toList();
            case "Niveau (A → Z)" -> list.stream().sorted(Comparator.comparing(a -> safeLower(a.getNiveau()))).toList();
            case "Publié d'abord" -> list.stream().sorted((a, b) -> {
                int c = Boolean.compare(b.isPublished(), a.isPublished());
                return (c != 0) ? c : byIdDesc.compare(a, b);
            }).toList();
            default -> list.stream().sorted(byIdDesc).toList();
        };
    }

    private VBox createCard(Formation f) {
        VBox wrapper = new VBox();
        wrapper.getStyleClass().add("item-card");
        wrapper.setUserData(f.getId());

        HBox row = new HBox(18);
        row.setPadding(new Insets(18));

        String imgUrl = safeRaw(f.getImageUrl()).trim();
        ImageView thumb = new ImageView();
        thumb.setFitWidth(220);
        thumb.setFitHeight(130);
        thumb.setPreserveRatio(false);
        if (!imgUrl.isBlank()) {
            try {
                thumb.setImage(new Image(imgUrl, true));
            } catch (Exception ignored) {
            }
        }
        StackPane imageBox = new StackPane(thumb);
        imageBox.getStyleClass().add("udemy-thumb");
        imageBox.setPickOnBounds(false);

        VBox content = new VBox(10);

        Label title = new Label(safeRaw(f.getTitre()).isBlank() ? "(Sans titre)" : f.getTitre());
        title.getStyleClass().add("udemy-title");

        FlowPane tagsPane = new FlowPane(8, 8);
        tagsPane.getStyleClass().add("chips-row");
        List<String> tags = parseTags(f.getCategorie());
        if (tags.isEmpty()) {
            Label empty = new Label("Aucune catégorie");
            empty.getStyleClass().addAll("tag-chip", "tag-gray");
            tagsPane.getChildren().add(empty);
        } else {
            for (String tag : tags) {
                Label chip = new Label(tag);
                chip.getStyleClass().addAll("tag-chip", colorClassFor(tag));
                tagsPane.getChildren().add(chip);
            }
        }

        HBox meta = new HBox(8);
        String niveau = safeRaw(f.getNiveau()).isBlank() ? "Niveau ?" : f.getNiveau();
        Label niveauBadge = new Label(niveau);
        niveauBadge.getStyleClass().addAll("badge", niveauClass(niveau));
        Label pubBadge = new Label(f.isPublished() ? "Publié" : "Non publié");
        pubBadge.getStyleClass().addAll("badge", f.isPublished() ? "badge-green" : "badge-gray");
        meta.getChildren().addAll(niveauBadge, pubBadge);

        HBox actions = new HBox(12);
        Button lessonsBtn = new Button("Voir lessons");
        lessonsBtn.getStyleClass().add("btn-primary");
        lessonsBtn.setOnAction(e -> openLessonsForFormation(f));

        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("btn-ghost");
        editBtn.setOnAction(e -> openForm(f));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> deleteFormation(f));

        if (!Session.isAdmin()) {
            editBtn.setVisible(false);
            editBtn.setManaged(false);
            deleteBtn.setVisible(false);
            deleteBtn.setManaged(false);
        }

        actions.getChildren().addAll(lessonsBtn, editBtn, deleteBtn);
        content.getChildren().addAll(title, tagsPane, meta, actions);
        row.getChildren().addAll(imageBox, content);
        wrapper.getChildren().add(row);

        wrapper.setOnMouseClicked(e -> {
            if (e.getTarget() instanceof Button)
                return;
            selected = f;
            highlightSelection();
        });

        return wrapper;
    }

    private void openLessonsForFormation(Formation formation) {
        Stage stage = (Stage) cardsContainer.getScene().getWindow();
        LessonListController controller = Navigator.goTo(stage, "/formation/lesson_list.fxml",
                "Finora Academy - Leçons");
        if (controller != null) {
            controller.setSelectedFormation(formation);
        }
    }

    private void deleteFormation(Formation f) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cette formation ? (les lessons seront supprimées aussi)",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                service.delete(f.getId());
                refresh();
            } catch (Exception e) {
                showError("Erreur suppression: " + e.getMessage());
            }
        }
    }

    private void openForm(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/formation/formation_form.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets()
                    .add(Objects.requireNonNull(getClass().getResource("/formation/style.css")).toExternalForm());
            FormationFormController controller = loader.getController();
            controller.setData(formation);
            controller.setOnSaved(this::refresh);
            Stage popup = new Stage();
            popup.setTitle(formation == null ? "Ajouter Formation" : "Modifier Formation");
            popup.setScene(scene);
            popup.show();
        } catch (Exception e) {
            showError("Erreur ouverture form: " + e.getMessage());
        }
    }

    private void highlightSelection() {
        for (var node : cardsContainer.getChildren()) {
            if (node instanceof VBox v) {
                boolean isSel = selected != null && selected.getId() == (int) v.getUserData();
                v.getStyleClass().remove("item-selected");
                if (isSel)
                    v.getStyleClass().add("item-selected");
            }
        }
        updateInfo(cardsContainer.getChildren().size(), allFormations.size());
    }

    private void updateInfo(int shown, int total) {
        if (lblInfo != null)
            lblInfo.setText("Affichées: " + shown + " / " + total
                    + "  |  Sélection: " + (selected == null ? "-" : safeRaw(selected.getTitre())));
    }

    private List<String> parseTags(String raw) {
        if (raw == null || raw.isBlank())
            return List.of();
        String[] parts = raw.split("[,;]");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty())
                out.add(t);
        }
        return out;
    }

    private String firstTagLower(String raw) {
        List<String> tags = parseTags(raw);
        return tags.isEmpty() ? "" : tags.get(0).toLowerCase();
    }

    private String niveauClass(String niveau) {
        String n = safeLower(niveau);
        if (n.contains("début"))
            return "badge-beginner";
        if (n.contains("inter"))
            return "badge-intermediate";
        if (n.contains("avanc"))
            return "badge-advanced";
        return "badge-gray";
    }

    private String colorClassFor(String tag) {
        int h = Math.abs(tag.toLowerCase().hashCode());
        return switch (h % 6) {
            case 0 -> "tag-purple";
            case 1 -> "tag-pink";
            case 2 -> "tag-blue";
            case 3 -> "tag-green";
            case 4 -> "tag-orange";
            default -> "tag-gray";
        };
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private String safeRaw(String s) {
        return s == null ? "" : s;
    }

    private void showWarn(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).showAndWait();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}