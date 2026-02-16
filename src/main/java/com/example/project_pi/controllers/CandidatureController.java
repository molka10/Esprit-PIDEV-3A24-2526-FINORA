package com.example.project_pi.controllers;

import com.example.project_pi.entities.Candidature;
import com.example.project_pi.services.CandidatureService;
import javafx.beans.property.SimpleStringProperty;
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

import java.sql.SQLException;
import java.util.List;

public class CandidatureController {

    @FXML private TextField searchField;
    @FXML private TextField appelOffreIdFilterField;

    @FXML private TableView<Candidature> table;

    @FXML private TableColumn<Candidature, Integer> colId;
    @FXML private TableColumn<Candidature, Integer> colAppelOffreId;
    @FXML private TableColumn<Candidature, String> colNom;
    @FXML private TableColumn<Candidature, String> colEmail;
    @FXML private TableColumn<Candidature, String> colMontant;
    @FXML private TableColumn<Candidature, String> colStatut;
    @FXML private TableColumn<Candidature, String> colCreatedAt; // we will display "-" for now

    private final CandidatureService service = new CandidatureService();
    private final ObservableList<Candidature> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Bind columns to entity properties
        colId.setCellValueFactory(new PropertyValueFactory<>("candidatureId"));
        colAppelOffreId.setCellValueFactory(new PropertyValueFactory<>("appelOffreId"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomCandidat"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("emailCandidat"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colMontant.setCellValueFactory(cell -> {
            Candidature c = cell.getValue();
            String s = (c.getMontantPropose() == 0) ? "-" : String.valueOf(c.getMontantPropose());
            return new SimpleStringProperty(s);
        });

        // created_at not in entity yet -> show "-"
        colCreatedAt.setCellValueFactory(cell -> new SimpleStringProperty("-"));

        table.setItems(data);

        loadData();

        // Search (nom/email)
        searchField.textProperty().addListener((obs, oldV, newV) -> applySearch(newV));
    }

    private void loadData() {
        try {
            List<Candidature> all = service.getAll();
            data.setAll(all);
            table.setItems(data); // ensure reset after filtering
        } catch (SQLException e) {
            showError("Erreur DB", "Impossible de charger les candidatures", e.getMessage());
        }
    }

    private void applySearch(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            table.setItems(data);
            return;
        }
        String k = keyword.toLowerCase().trim();
        ObservableList<Candidature> filtered = FXCollections.observableArrayList();

        for (Candidature c : data) {
            String n = (c.getNomCandidat() == null) ? "" : c.getNomCandidat().toLowerCase();
            String e = (c.getEmailCandidat() == null) ? "" : c.getEmailCandidat().toLowerCase();
            if (n.contains(k) || e.contains(k)) filtered.add(c);
        }
        table.setItems(filtered);
    }

    @FXML
    private void onApplyFilter() {
        String txt = appelOffreIdFilterField.getText().trim();
        if (txt.isEmpty()) {
            showInfo("Filtre", "Champ vide", "Entre un appel_offre_id.");
            return;
        }
        try {
            int id = Integer.parseInt(txt);
            List<Candidature> list = service.getByAppelOffreId(id);
            table.setItems(FXCollections.observableArrayList(list));
        } catch (NumberFormatException ex) {
            showInfo("Filtre", "ID invalide", "appel_offre_id doit être un nombre.");
        } catch (SQLException e) {
            showError("Erreur DB", "Impossible d'appliquer le filtre", e.getMessage());
        }
    }

    @FXML
    private void onResetFilter() {
        appelOffreIdFilterField.clear();
        searchField.clear();
        table.setItems(data);
    }

    @FXML
    private void onRefresh() {
        appelOffreIdFilterField.clear();
        searchField.clear();
        loadData();
    }

    @FXML
    private void onNew() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/project_pi/ui/CandidatureForm.fxml"));
            Parent root = loader.load();

            CandidatureFormController formController = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Nouvelle Candidature");
            dialog.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 700, 550);
            com.example.project_pi.utils.ThemeManager.apply(scene);
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
        Candidature selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showInfo("Sélection", "Aucune sélection", "Sélectionne une candidature.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/project_pi/ui/CandidatureForm.fxml"));
            Parent root = loader.load();

            CandidatureFormController formController = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Modifier Candidature");
            dialog.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 700, 550);
            com.example.project_pi.utils.ThemeManager.apply(scene);
            dialog.setScene(scene);

            formController.setDialogStage(dialog);
            formController.setCandidature(selected); // ✅ pre-fill + switch to edit mode

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
        Candidature selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showInfo("Sélection", "Aucune sélection", "Sélectionne une candidature.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la candidature ?");
        confirm.setContentText(
                "ID: " + selected.getCandidatureId() +
                        "\nNom: " + selected.getNomCandidat() +
                        "\nEmail: " + selected.getEmailCandidat()
        );

        ButtonType yes = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("Non", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(yes, no);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == yes) {
                try {
                    service.delete(selected.getCandidatureId());
                    loadData();
                    showInfo("Succès", "Supprimée", "La candidature a été supprimée.");
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
