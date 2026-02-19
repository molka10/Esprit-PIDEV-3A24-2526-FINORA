package tn.finora.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.finora.entities.Formation;
import tn.finora.services.FormationService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FormationListController {

    @FXML private VBox cardsContainer;
    @FXML private Label lblInfo;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbSort;

    private final FormationService service = new FormationService();

    private Formation selected;
    private List<Formation> allFormations = new ArrayList<>();

    @FXML


    private void initSort() {
        if (cbSort == null) return;

        if (cbSort.getItems() == null || cbSort.getItems().isEmpty()) {
            cbSort.setItems(FXCollections.observableArrayList(
                    "Dernier ajouté",
                    "Titre (A → Z)",
                    "Titre (Z → A)",
                    "Catégorie (A → Z)",
                    "Niveau (A → Z)",
                    "Publié d'abord"
            ));
        }

        cbSort.getSelectionModel().select("Dernier ajouté");
        cbSort.setDisable(cbSort.getItems().isEmpty());
    }

    @FXML
    public void initialize() {
        initSort();
        // Prevent selection bug when list is empty
        if (cbSort != null) {
            cbSort.setDisable(cbSort.getItems().isEmpty());
        }
        refresh();
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

    // ========= FXML HANDLERS (must match your FXML) =========
    @FXML
    private void onSearch() {
        applySearchAndSort();
    }

    @FXML
    private void onSort() {
        applySearchAndSort();
    }

    @FXML
    private void onAdd() {
        openForm(null);
    }

    @FXML
    private void onEdit() {
        if (selected == null) {
            showWarn("Sélectionne une formation (clique sur une carte)");
            return;
        }
        openForm(selected);
    }

    @FXML
    private void onDelete() {
        if (selected == null) {
            showWarn("Sélectionne une formation (clique sur une carte)");
            return;
        }
        deleteFormation(selected);
    }

    @FXML
    private void goLessons() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lesson_list.fxml"));
            Scene scene = new Scene(loader.load(), 1250, 720);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            showError("Impossible d'ouvrir Lessons: " + e.getMessage());
        }
    }

    // ========= FILTER / SORT / RENDER =========
    private void applySearchAndSort() {
        if (cardsContainer == null) return;

        cardsContainer.getChildren().clear();
        selected = null;

        String q = (txtSearch == null || txtSearch.getText() == null)
                ? ""
                : txtSearch.getText().trim().toLowerCase();

        String sort = (cbSort == null) ? "Dernier ajouté" : cbSort.getSelectionModel().getSelectedItem();

        List<Formation> filtered = allFormations.stream()
                .filter(f -> matches(f, q))
                .toList();

        List<Formation> sorted = sortFormations(filtered, sort);

        for (Formation f : sorted) {
            cardsContainer.getChildren().add(createCard(f));
        }

        updateInfo(sorted.size(), allFormations.size());
    }

    private boolean matches(Formation f, String q) {
        if (q.isEmpty()) return true;

        String titre = safeLower(f.getTitre());
        String niveau = safeLower(f.getNiveau());

        List<String> tags = parseTags(f.getCategorie());
        boolean tagMatch = tags.stream().anyMatch(t -> t.toLowerCase().contains(q));

        return titre.contains(q) || niveau.contains(q) || tagMatch;
    }

    private List<Formation> sortFormations(List<Formation> list, String sort) {
        if (sort == null) sort = "Dernier ajouté";

        Comparator<Formation> byIdDesc = (a, b) -> Integer.compare(b.getId(), a.getId());

        return switch (sort) {
            case "Titre (A → Z)" -> list.stream()
                    .sorted(Comparator.comparing(a -> safeLower(a.getTitre())))
                    .toList();

            case "Titre (Z → A)" -> list.stream()
                    .sorted(Comparator.comparing((Formation a) -> safeLower(a.getTitre())).reversed())
                    .toList();

            case "Catégorie (A → Z)" -> list.stream()
                    .sorted(Comparator.comparing(a -> firstTagLower(a.getCategorie())))
                    .toList();

            case "Niveau (A → Z)" -> list.stream()
                    .sorted(Comparator.comparing(a -> safeLower(a.getNiveau())))
                    .toList();

            case "Publié d'abord" -> list.stream()
                    .sorted((a, b) -> {
                        int c = Boolean.compare(b.isPublished(), a.isPublished());
                        return (c != 0) ? c : byIdDesc.compare(a, b);
                    })
                    .toList();

            default -> list.stream().sorted(byIdDesc).toList();
        };
    }

    // ========= UDEMY-LIKE CARD =========
    private VBox createCard(Formation f) {
        VBox wrapper = new VBox();
        wrapper.getStyleClass().add("item-card");
        wrapper.setUserData(f.getId());

        HBox row = new HBox(18);
        row.setPadding(new Insets(18));

        // Image
        String imgUrl = safeRaw(f.getImageUrl()).trim();

        ImageView thumb = new ImageView();
        thumb.setFitWidth(220);
        thumb.setFitHeight(130);
        thumb.setPreserveRatio(false);

        if (!imgUrl.isBlank()) {
            try {
                thumb.setImage(new Image(imgUrl, true));
            } catch (Exception ignored) {}
        }

        StackPane imageBox = new StackPane(thumb);
        imageBox.getStyleClass().add("udemy-thumb");

        // Right content
        VBox content = new VBox(10);

        String titre = safeRaw(f.getTitre()).trim();
        Label title = new Label(titre.isBlank() ? "(Sans titre)" : titre);
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

        String niveau = safeRaw(f.getNiveau()).trim();
        if (niveau.isBlank()) niveau = "Niveau ?";

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

        actions.getChildren().addAll(lessonsBtn, editBtn, deleteBtn);

        content.getChildren().addAll(title, tagsPane, meta, actions);

        row.getChildren().addAll(imageBox, content);
        wrapper.getChildren().add(row);

        // Click card selects (buttons still work)
        wrapper.setOnMouseClicked(e -> {
            selected = f;
            highlightSelection();
        });

        return wrapper;
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

    private void openLessonsForFormation(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lesson_list.fxml"));
            Scene scene = new Scene(loader.load(), 1250, 720);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            LessonListController controller = loader.getController();
            controller.setSelectedFormation(formation);

            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            showError("Impossible d'ouvrir Lessons: " + e.getMessage());
        }
    }

    private void openForm(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/formation_form.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

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
                if (isSel) v.getStyleClass().add("item-selected");
            }
        }
        updateInfo(cardsContainer.getChildren().size(), allFormations.size());
    }

    private void updateInfo(int shown, int total) {
        if (lblInfo != null) {
            lblInfo.setText("Affichées: " + shown + " / " + total
                    + "  |  Sélection: " + (selected == null ? "-" : safeRaw(selected.getTitre())));
        }
    }

    private List<String> parseTags(String raw) {
        if (raw == null) return List.of();
        raw = raw.trim();
        if (raw.isEmpty()) return List.of();

        String[] parts = raw.split("[,;]");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    private String firstTagLower(String raw) {
        List<String> tags = parseTags(raw);
        if (tags.isEmpty()) return "";
        return tags.get(0).toLowerCase();
    }

    private String niveauClass(String niveau) {
        String n = safeLower(niveau);
        if (n.contains("début")) return "badge-beginner";
        if (n.contains("inter")) return "badge-intermediate";
        if (n.contains("avanc")) return "badge-advanced";
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

    private String safeLower(String s) { return s == null ? "" : s.toLowerCase(); }
    private String safeRaw(String s) { return s == null ? "" : s; }

    private void showWarn(String msg) { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
