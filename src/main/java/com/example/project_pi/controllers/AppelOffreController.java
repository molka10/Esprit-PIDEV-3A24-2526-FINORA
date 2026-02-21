package com.example.project_pi.controllers;

import com.example.project_pi.entities.AppelOffre;
import com.example.project_pi.services.AppelOffreService;
import com.example.project_pi.utils.ThemeManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppelOffreController {

    @FXML private TextField searchField;

    // ✅ replaced TableView by ListView
    @FXML private ListView<AppelOffre> listView;

    @FXML private Label totalLabel;
    @FXML private Label publishedLabel;
    @FXML private Label draftLabel;

    private final AppelOffreService service = new AppelOffreService();

    // main source list (loaded from DB)
    private final ObservableList<AppelOffre> data = FXCollections.observableArrayList();

    // selected item cache
    private AppelOffre selected;

    @FXML
    private void initialize() {

        // ✅ Card UI renderer (no table columns anymore)
        listView.setCellFactory(lv -> new AppelOffreCardCell());

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            selected = newV;
        });

        // attach main list
        listView.setItems(data);

        // load DB
        loadData();

        // live search filter
        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilter(newV));
    }

    private void loadData() {
        try {
            List<AppelOffre> all = service.getAll();
            data.setAll(all);

            // ensure list view shows main list
            listView.setItems(data);

            updateStats(all);

            // optional: clear selection after reload
            listView.getSelectionModel().clearSelection();
            selected = null;

        } catch (SQLException e) {
            showError("Erreur DB", "Impossible de charger les appels d'offres", e.getMessage());
        }
    }

    private void applyFilter(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            // back to original list
            listView.setItems(data);
            updateStats(data);
            return;
        }

        String k = keyword.toLowerCase().trim();
        ObservableList<AppelOffre> filtered = FXCollections.observableArrayList();

        for (AppelOffre a : data) {
            String t = (a.getTitre() == null) ? "" : a.getTitre().toLowerCase();
            String c = (a.getCategorie() == null) ? "" : a.getCategorie().toLowerCase();
            if (t.contains(k) || c.contains(k)) {
                filtered.add(a);
            }
        }

        listView.setItems(filtered);
        updateStats(filtered);

        // keep selection coherent
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
        AppelOffre current = getSelectedFromList();

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
            formController.setAppelOffre(current); // prefill

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
        AppelOffre current = getSelectedFromList();

        if (current == null) {
            showInfo("Sélection", "Aucune sélection", "Sélectionne un appel d'offre dans la liste.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'appel d'offre ?");
        confirm.setContentText(
                "Titre: " + safe(current.getTitre()) + "\n" +
                        "Catégorie: " + safe(current.getCategorie()) + "\n" +
                        "Date limite: " + formatDate(current) + "\n" +
                        "Statut: " + safe(current.getStatut())
        );

        ButtonType yes = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("Non", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(yes, no);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == yes) {
                try {
                    service.delete(current.getAppelOffreId());
                    loadData();
                    showInfo("Succès", "Supprimé", "L'appel d'offre a été supprimé.");
                } catch (Exception e) {
                    showError("Erreur DB", "Suppression impossible", e.getMessage());
                }
            }
        });
    }

    // ---- helpers ----

    private AppelOffre getSelectedFromList() {
        // prefer live selection from listView, fallback to cached field
        AppelOffre lvSelected = listView.getSelectionModel().getSelectedItem();
        if (lvSelected != null) return lvSelected;
        return selected;
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private String formatDate(AppelOffre a) {
        if (a == null || a.getDateLimite() == null) return "-";
        return a.getDateLimite().format(DateTimeFormatter.ISO_DATE);
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