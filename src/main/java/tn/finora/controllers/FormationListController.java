package tn.finora.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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

    // NEW
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbSort;

    private final FormationService service = new FormationService();

    private Formation selected;

    // NEW: keep full list in memory for search/sort
    private List<Formation> allFormations = new ArrayList<>();

    @FXML
    public void initialize() {
        initSort();
        refresh();
    }

    private void initSort() {
        if (cbSort == null) return;
        cbSort.setItems(FXCollections.observableArrayList(
                "Dernier ajouté",
                "Titre (A → Z)",
                "Titre (Z → A)",
                "Catégorie (A → Z)",
                "Niveau (A → Z)",
                "Publié d'abord"
        ));
        cbSort.getSelectionModel().select("Dernier ajouté");
    }

    private void refresh() {
        try {
            selected = null;
            allFormations = service.getAll();
            applySearchAndSort(); // render cards
        } catch (Exception e) {
            showError("Erreur chargement formations: " + e.getMessage());
        }
    }

    // NEW: called by FXML onKeyReleased
    @FXML
    private void onSearch() {
        applySearchAndSort();
    }

    // NEW: called by FXML onAction
    @FXML
    private void onSort() {
        applySearchAndSort();
    }

    private void applySearchAndSort() {
        cardsContainer.getChildren().clear();
        selected = null;

        String q = (txtSearch == null || txtSearch.getText() == null) ? "" : txtSearch.getText().trim().toLowerCase();
        String sort = (cbSort == null) ? "Dernier ajouté" : cbSort.getSelectionModel().getSelectedItem();

        // 1) filter
        List<Formation> filtered = allFormations.stream()
                .filter(f -> matches(f, q))
                .toList();

        // 2) sort
        List<Formation> sorted = sortFormations(filtered, sort);

        // 3) render
        for (Formation f : sorted) {
            cardsContainer.getChildren().add(createCard(f));
        }

        if (lblInfo != null) {
            lblInfo.setText("Affichées: " + sorted.size() + " / " + allFormations.size()
                    + "  |  Sélection: " + (selected == null ? "-" : selected.getTitre()));
        }
    }

    private boolean matches(Formation f, String q) {
        if (q.isEmpty()) return true;
        return safe(f.getTitre()).contains(q)
                || safe(f.getCategorie()).contains(q)
                || safe(f.getNiveau()).contains(q);
    }

    private List<Formation> sortFormations(List<Formation> list, String sort) {
        if (sort == null) sort = "Dernier ajouté";

        Comparator<Formation> byIdDesc = (a, b) -> Integer.compare(b.getId(), a.getId());

        return switch (sort) {
            case "Titre (A → Z)" -> list.stream()
                    .sorted(Comparator.comparing(a -> safe(a.getTitre())))
                    .toList();
            case "Titre (Z → A)" -> list.stream()
                    .sorted(Comparator.comparing((Formation a) -> safe(a.getTitre())).reversed())
                    .toList();
            case "Catégorie (A → Z)" -> list.stream()
                    .sorted(Comparator.comparing(a -> safe(a.getCategorie())))
                    .toList();
            case "Niveau (A → Z)" -> list.stream()
                    .sorted(Comparator.comparing(a -> safe(a.getNiveau())))
                    .toList();
            case "Publié d'abord" -> list.stream()
                    .sorted((a, b) -> {
                        int c = Boolean.compare(b.isPublished(), a.isPublished());
                        return (c != 0) ? c : byIdDesc.compare(a, b);
                    })
                    .toList();
            default -> list.stream().sorted(byIdDesc).toList(); // Dernier ajouté
        };
    }

    private VBox createCard(Formation f) {
        VBox card = new VBox(12);
        card.getStyleClass().add("item-card");
        card.setUserData(f.getId());

        Label title = new Label(f.getTitre());
        title.getStyleClass().add("item-title");

        HBox badges = new HBox(8);

        Label catBadge = new Label(f.getCategorie());
        catBadge.getStyleClass().addAll("badge", "badge-purple");

        Label niveauBadge = new Label(f.getNiveau());
        niveauBadge.getStyleClass().addAll("badge", "badge-gray");

        Label pubBadge = new Label(f.isPublished() ? "Publié" : "Non publié");
        pubBadge.getStyleClass().addAll("badge",
                f.isPublished() ? "badge-green" : "badge-gray");

        badges.getChildren().addAll(catBadge, niveauBadge, pubBadge);

        String imgUrl = safeRaw(f.getImageUrl());
        Label image = new Label("🔗 " + (imgUrl.isBlank() ? "(aucune image)" : imgUrl));
        image.setStyle("-fx-text-fill: #6B5A8A; -fx-font-size: 12px;");

        HBox actions = new HBox(10);

        Button selectBtn = new Button("Sélectionner");
        selectBtn.getStyleClass().add("btn-ghost");
        selectBtn.setOnAction(e -> { selected = f; highlightSelection(); });

        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("btn-ghost");
        editBtn.setOnAction(e -> openForm(f));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> { selected = f; onDelete(); });

        actions.getChildren().addAll(selectBtn, editBtn, deleteBtn);

        card.getChildren().addAll(title, badges, image, actions);

        // click on card = select
        card.setOnMouseClicked(e -> { selected = f; highlightSelection(); });

        return card;
    }

    private void highlightSelection() {
        for (var node : cardsContainer.getChildren()) {
            if (node instanceof VBox v) {
                boolean isSel = selected != null && selected.getId() == (int) v.getUserData();
                v.getStyleClass().remove("item-selected");
                if (isSel) v.getStyleClass().add("item-selected");
            }
        }
        if (lblInfo != null) {
            lblInfo.setText("Affichées: " + cardsContainer.getChildren().size() + " / " + allFormations.size()
                    + "  |  Sélection: " + (selected == null ? "-" : selected.getTitre()));
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

    @FXML
    private void onAdd() { openForm(null); }

    @FXML
    private void onEdit() {
        if (selected == null) { showWarn("Sélectionne une formation (clique sur une carte)"); return; }
        openForm(selected);
    }

    @FXML
    private void onDelete() {
        if (selected == null) { showWarn("Sélectionne une formation (clique sur une carte)"); return; }
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

    private String safe(String s) { return s == null ? "" : s.toLowerCase(); }
    private String safeRaw(String s) { return s == null ? "" : s; }

    private void showWarn(String msg) { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
