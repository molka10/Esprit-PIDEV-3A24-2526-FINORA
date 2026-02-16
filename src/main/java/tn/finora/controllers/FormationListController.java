package tn.finora.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.finora.entities.Formation;
import tn.finora.services.FormationService;

import java.util.List;

public class FormationListController {

    @FXML private VBox cardsContainer;
    @FXML private Label lblInfo;

    private final FormationService service = new FormationService();


    private Formation selected;

    @FXML
    public void initialize() {
        refresh();
    }

    private void refresh() {
        try {
            selected = null;
            cardsContainer.getChildren().clear();

            List<Formation> list = service.getAll();
            for (Formation f : list) {
                cardsContainer.getChildren().add(createCard(f));
            }

            if (lblInfo != null) lblInfo.setText("Total formations: " + list.size() + "  |  Sélection: " + (selected == null ? "-" : selected.getTitre()));
        } catch (Exception e) {
            showError("Erreur chargement formations: " + e.getMessage());
        }
    }

    private VBox createCard(Formation f) {

        VBox card = new VBox(12);
        card.getStyleClass().add("item-card");
        card.setUserData(f.getId());

        // Title
        Label title = new Label(f.getTitre());
        title.getStyleClass().add("item-title");

        // ===== BADGES ROW =====
        HBox badges = new HBox(8);

        Label catBadge = new Label(f.getCategorie());
        catBadge.getStyleClass().addAll("badge", "badge-purple");

        Label niveauBadge = new Label(f.getNiveau());
        niveauBadge.getStyleClass().addAll("badge", "badge-gray");

        Label pubBadge = new Label(f.isPublished() ? "Publié" : "Non publié");
        pubBadge.getStyleClass().addAll("badge",
                f.isPublished() ? "badge-green" : "badge-gray");

        badges.getChildren().addAll(catBadge, niveauBadge, pubBadge);

        // Image link (clean style)
        Label image = new Label("🔗 " + f.getImageUrl());
        image.setStyle("-fx-text-fill: #6B5A8A; -fx-font-size: 12px;");

        // Buttons
        HBox actions = new HBox(10);

        Button selectBtn = new Button("Sélectionner");
        selectBtn.getStyleClass().add("btn-ghost");
        selectBtn.setOnAction(e -> {
            selected = f;
            highlightSelection();
        });

        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("btn-ghost");
        editBtn.setOnAction(e -> openForm(f));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> {
            selected = f;
            onDelete();
        });

        actions.getChildren().addAll(selectBtn, editBtn, deleteBtn);

        card.getChildren().addAll(title, badges, image, actions);

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
            lblInfo.setText("Total formations: " + cardsContainer.getChildren().size()
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
    private void onAdd() {
        openForm(null);
    }

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
            Scene scene = new Scene(loader.load(), 1000, 650);
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

    private String safe(String s) { return s == null ? "" : s; }

    private void showWarn(String msg) { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
