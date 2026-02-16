package com.example.project_pi.controllers;

import com.example.project_pi.entities.AppelOffre;
import com.example.project_pi.services.AppelOffreService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

// IMPORTANT: import your controller
import com.example.project_pi.controllers.AppelOffreFormController;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppelOffreController {

    @FXML private TextField searchField;
    @FXML private TableView<AppelOffre> table;

    @FXML private TableColumn<AppelOffre, Integer> colId;
    @FXML private TableColumn<AppelOffre, String> colTitre;
    @FXML private TableColumn<AppelOffre, String> colCategorie;
    @FXML private TableColumn<AppelOffre, String> colType;
    @FXML private TableColumn<AppelOffre, String> colBudget;
    @FXML private TableColumn<AppelOffre, String> colDateLimite;
    @FXML private TableColumn<AppelOffre, String> colStatut;

    @FXML private Label totalLabel;
    @FXML private Label publishedLabel;
    @FXML private Label draftLabel;

    private final AppelOffreService service = new AppelOffreService();
    private final ObservableList<AppelOffre> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        // 1) Bind columns to AppelOffre getters
        // Property names must match getter names:
        // getAppelOffreId -> "appelOffreId", getTitre -> "titre", etc.
        colId.setCellValueFactory(new PropertyValueFactory<>("appelOffreId"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // 2) Custom display for budget (min-max devise)
        colBudget.setCellValueFactory(cell -> {
            AppelOffre a = cell.getValue();
            String dev = (a.getDevise() == null || a.getDevise().isBlank()) ? "" : " " + a.getDevise();
            String min = (a.getBudgetMin() == 0) ? "-" : String.valueOf(a.getBudgetMin());
            String max = (a.getBudgetMax() == 0) ? "-" : String.valueOf(a.getBudgetMax());
            return new javafx.beans.property.SimpleStringProperty(min + " - " + max + dev);
        });

        // 3) Custom display for date
        colDateLimite.setCellValueFactory(cell -> {
            AppelOffre a = cell.getValue();
            String s = (a.getDateLimite() == null) ? "-" : a.getDateLimite().format(DateTimeFormatter.ISO_DATE);
            return new javafx.beans.property.SimpleStringProperty(s);
        });

        table.setItems(data);

        // 4) Load from DB
        loadData();

        // 5) Search filter (simple)
        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilter(newV));
    }

    private void loadData() {
        try {
            List<AppelOffre> all = service.getAll();
            data.setAll(all);

            // IMPORTANT: always re-attach table to the main list
            table.setItems(data);

            updateStats(all);
        } catch (SQLException e) {
            showError("Erreur DB", "Impossible de charger les appels d'offres", e.getMessage());
        }
    }


    private void applyFilter(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            //  Back to original data without staying stuck on filtered list
            table.setItems(data);
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

        table.setItems(filtered);
        updateStats(filtered);
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
        table.setItems(data);
        searchField.clear();
        loadData();
    }

    @FXML
    private void onNew() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/project_pi/ui/AppelOffreForm.fxml"));
            Parent root = loader.load();

            //  Get controller of the form
            AppelOffreFormController formController = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Nouveau Appel d’Offre");
            dialog.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 820, 600);
            com.example.project_pi.utils.ThemeManager.apply(scene);
            dialog.setScene(scene);

            // Give the dialog stage to form controller (so it can close itself)
            formController.setDialogStage(dialog);

            dialog.showAndWait();

            //  After closing, if saved => refresh
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
        AppelOffre selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showInfo("Sélection", "Aucune sélection", "Sélectionne un appel d'offre dans la table.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/project_pi/ui/AppelOffreForm.fxml"));
            Parent root = loader.load();

            // get form controller
            AppelOffreFormController formController = loader.getController();

            // pass dialog stage + selected object (prefill)
            Stage dialog = new Stage();
            dialog.setTitle("Modifier Appel d’Offre");
            dialog.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 820, 600);
            com.example.project_pi.utils.ThemeManager.apply(scene);
            dialog.setScene(scene);

            formController.setDialogStage(dialog);
            formController.setAppelOffre(selected); //  this pre-fills the form

            dialog.showAndWait();

            // if saved => refresh
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
        AppelOffre selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showInfo("Sélection", "Aucune sélection", "Sélectionne un appel d'offre dans la table.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'appel d'offre ?");
        confirm.setContentText("ID: " + selected.getAppelOffreId() + "\nTitre: " + selected.getTitre());

        ButtonType yes = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("Non", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(yes, no);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == yes) {
                try {
                    service.delete(selected.getAppelOffreId());
                    loadData(); // refresh
                    showInfo("Succès", "Supprimé", "L'appel d'offre a été supprimé.");
                } catch (Exception e) {
                    showError("Erreur DB", "Suppression impossible", e.getMessage());
                }
            }
        });
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
