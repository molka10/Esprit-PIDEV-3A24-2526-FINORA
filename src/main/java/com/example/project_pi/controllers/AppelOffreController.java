package com.example.project_pi.controllers;

import com.example.project_pi.entities.AppelOffre;
import com.example.project_pi.services.AppelOffreService;
import com.example.project_pi.utils.ThemeManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class AppelOffreController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilterCombo;

    @FXML private ListView<AppelOffre> listView;

    @FXML private Label totalLabel;
    @FXML private Label publishedLabel;
    @FXML private Label draftLabel;

    private final AppelOffreService service = new AppelOffreService();

    private final ObservableList<AppelOffre> data = FXCollections.observableArrayList();
    private FilteredList<AppelOffre> filtered;

    private AppelOffre selected;

    @FXML
    private void initialize() {
        // cards
        listView.setCellFactory(lv -> new AppelOffreCardCell());

        // base list
        filtered = new FilteredList<>(data, p -> true);
        listView.setItems(filtered);

        // selection
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> selected = newV);

        // listeners
        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        categoryFilterCombo.setOnAction(e -> applyFilters());

        loadData();
    }

    private void loadData() {
        try {
            List<AppelOffre> all = service.getAll();
            data.setAll(all);

            // fill category combo
            categoryFilterCombo.getItems().clear();
            categoryFilterCombo.getItems().add("Toutes");

            all.stream()
                    .map(AppelOffre::getCategorie)
                    .filter(s -> s != null && !s.isBlank())
                    .distinct()
                    .sorted()
                    .forEach(categoryFilterCombo.getItems()::add);

            if (categoryFilterCombo.getValue() == null) {
                categoryFilterCombo.setValue("Toutes");
            }

            applyFilters();

        } catch (SQLException e) {
            showError("Erreur DB", "Impossible de charger les appels d'offres", e.getMessage());
        }
    }

    private void applyFilters() {
        String keyword = (searchField.getText() == null) ? "" : searchField.getText().trim().toLowerCase();
        String selectedCat = categoryFilterCombo.getValue();

        filtered.setPredicate(a -> {
            if (a == null) return false;

            boolean catOk = true;
            if (selectedCat != null && !"Toutes".equalsIgnoreCase(selectedCat)) {
                String cat = (a.getCategorie() == null) ? "" : a.getCategorie();
                catOk = cat.equalsIgnoreCase(selectedCat);
            }

            boolean searchOk = true;
            if (!keyword.isBlank()) {
                String t = (a.getTitre() == null) ? "" : a.getTitre().toLowerCase();
                String c = (a.getCategorie() == null) ? "" : a.getCategorie().toLowerCase();
                searchOk = t.contains(keyword) || c.contains(keyword);
            }

            return catOk && searchOk;
        });

        updateStats(filtered);
        listView.refresh();
        listView.getSelectionModel().clearSelection();
        selected = null;
    }

    private void updateStats(List<AppelOffre> list) {
        long total = list.size();
        long published = list.stream().filter(a -> "published".equalsIgnoreCase(a.getStatut())).count();
        long draft = list.stream().filter(a -> "draft".equalsIgnoreCase(a.getStatut())).count();

        totalLabel.setText(String.valueOf(total));
        publishedLabel.setText(String.valueOf(published));
        draftLabel.setText(String.valueOf(draft));
    }

    @FXML
    private void onRefresh() {
        searchField.clear();
        categoryFilterCombo.setValue("Toutes");
        loadData();
    }

    @FXML
    private void onNew() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/project_pi/ui/AppelOffreForm.fxml"));
            Parent root = loader.load();

            AppelOffreFormController formController = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Nouveau Appel d’Offre");
            dialog.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root, 820, 600);
            ThemeManager.apply(scene);
            dialog.setScene(scene);

            formController.setDialogStage(dialog);

            dialog.showAndWait();

            if (formController.isSaved()) {
                loadData();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire", e.getMessage());
        }
    }

    @FXML
    private void onEdit() {
        AppelOffre current = listView.getSelectionModel().getSelectedItem();
        if (current == null) current = selected;

        if (current == null) {
            showInfo("Sélection", "Aucune sélection", "Sélectionne un appel d'offre dans la liste.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/project_pi/ui/AppelOffreForm.fxml"));
            Parent root = loader.load();

            AppelOffreFormController formController = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Modifier Appel d’Offre");
            dialog.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root, 820, 600);
            ThemeManager.apply(scene);
            dialog.setScene(scene);

            formController.setDialogStage(dialog);
            formController.setAppelOffre(current);

            dialog.showAndWait();

            if (formController.isSaved()) {
                loadData();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire (edit)", e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        AppelOffre selectedItem = listView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) selectedItem = selected;

        if (selectedItem == null) {
            showInfo("Sélection", "Aucune sélection", "Sélectionne un appel d'offre dans la liste.");
            return;
        }

        final AppelOffre finalItem = selectedItem; // ✅ make it final

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'appel d'offre ?");
        confirm.setContentText("Titre: " + safe(finalItem.getTitre()));

        ButtonType yes = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("Non", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(yes, no);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == yes) {
                try {
                    service.delete(finalItem.getAppelOffreId());
                    loadData();
                    showInfo("Succès", "Supprimé", "L'appel d'offre a été supprimé.");
                } catch (Exception e) {
                    showError("Erreur DB", "Suppression impossible", e.getMessage());
                }
            }
        });
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private void showError(String title, String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void showInfo(String title, String header, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}