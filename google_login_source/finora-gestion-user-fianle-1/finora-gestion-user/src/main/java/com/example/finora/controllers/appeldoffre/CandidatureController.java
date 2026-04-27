package com.example.finora.controllers.appeldoffre;

import com.example.finora.entities.AppelOffre;
import com.example.finora.entities.Candidature;
import com.example.finora.services.appeldoffre.AppelOffreService;
import com.example.finora.services.appeldoffre.CandidatureService;
import com.example.finora.utils.Session;
import com.example.finora.utils.ThemeManager;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CandidatureController {

    @FXML
    private TextField searchField;

    // ✅ NEW filter (by AppelOffre title)
    @FXML
    private ComboBox<AppelOffre> appelOffreFilterCombo;

    @FXML
    private ListView<Candidature> listView;

    @FXML
    private Button newBtn;
    @FXML
    private Button editBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private Button refreshBtn;

    private final CandidatureService candidatureService = new CandidatureService();
    private final AppelOffreService appelOffreService = new AppelOffreService();

    private final ObservableList<Candidature> data = FXCollections.observableArrayList();

    // id -> titre (no JOIN)
    private final Map<Integer, String> appelOffreTitles = new HashMap<>();

    private Candidature selected;

    @FXML
    private void initialize() {

        // list uses cards (shows AppelOffre title from map)
        listView.setCellFactory(lv -> new CandidatureCardCell(appelOffreTitles));
        listView.setItems(data);

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> selected = newV);

        // load data
        loadAppelOffreTitles();
        loadFilterCombo();
        loadData();
        applyRoleUI();

        // live search by nom/email
        searchField.textProperty().addListener((obs, oldV, newV) -> applySearchFilter(newV));
    }

    private void loadAppelOffreTitles() {
        try {
            appelOffreTitles.clear();
            List<AppelOffre> offres = appelOffreService.getAll();
            for (AppelOffre a : offres) {
                appelOffreTitles.put(a.getAppelOffreId(), a.getTitre());
            }
        } catch (SQLException e) {
            // UI will fallback to "Appel d’Offre #id"
            showError("Erreur DB", "Impossible de charger les titres des Appels d’Offres", e.getMessage());
        }
    }

    private void loadFilterCombo() {
        try {
            List<AppelOffre> offres = appelOffreService.getAll();
            appelOffreFilterCombo.getItems().setAll(offres);

            // show titles in ComboBox
            appelOffreFilterCombo.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected void updateItem(AppelOffre item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getTitre());
                }
            });
            appelOffreFilterCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(AppelOffre item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getTitre());
                }
            });

        } catch (SQLException e) {
            showError("Erreur DB", "Impossible de charger le filtre Appel d’Offre", e.getMessage());
        }
    }

    private void applyRoleUI() {
        String role = Session.getCurrentUser() != null
                ? Session.getCurrentUser().getRole()
                : "";

        boolean isUser = "USER".equalsIgnoreCase(role);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        boolean isEntreprise = "ENTREPRISE".equalsIgnoreCase(role);

        if (newBtn != null)
            newBtn.setVisible(isUser);
        if (editBtn != null)
            editBtn.setVisible(isAdmin || isEntreprise);
        if (deleteBtn != null)
            deleteBtn.setVisible(isAdmin);
    }

    private void loadData() {
        try {
            List<Candidature> all;
            if (Session.getCurrentUser() != null && "USER".equalsIgnoreCase(Session.getCurrentUser().getRole())) {
                all = candidatureService.getByEmail(Session.getCurrentUser().getEmail());
            } else {
                all = candidatureService.getAll();
            }
            data.setAll(all);
            listView.refresh();
            listView.getSelectionModel().clearSelection();
            selected = null;

        } catch (SQLException e) {
            showError("Erreur DB", "Impossible de charger les candidatures", e.getMessage());
        }
    }

    private void applySearchFilter(String keyword) {
        ObservableList<Candidature> base = data;

        // If AppelOffre filter is active, apply it first
        AppelOffre ao = appelOffreFilterCombo.getValue();
        if (ao != null) {
            base = FXCollections.observableArrayList();
            int id = ao.getAppelOffreId();
            for (Candidature c : data) {
                if (c.getAppelOffreId() == id)
                    base.add(c);
            }
        }

        if (keyword == null || keyword.isBlank()) {
            listView.setItems(base);
            listView.getSelectionModel().clearSelection();
            selected = null;
            return;
        }

        String k = keyword.toLowerCase().trim();
        ObservableList<Candidature> filtered = FXCollections.observableArrayList();

        for (Candidature c : base) {
            String n = c.getNomCandidat() == null ? "" : c.getNomCandidat().toLowerCase();
            String e = c.getEmailCandidat() == null ? "" : c.getEmailCandidat().toLowerCase();
            if (n.contains(k) || e.contains(k))
                filtered.add(c);
        }

        listView.setItems(filtered);
        listView.getSelectionModel().clearSelection();
        selected = null;
    }

    @FXML
    private void onApplyFilter() {
        // Apply AppelOffre title filter
        applySearchFilter(searchField.getText());
    }

    @FXML
    private void onResetFilter() {
        appelOffreFilterCombo.getSelectionModel().clearSelection();
        applySearchFilter(searchField.getText());
    }

    @FXML
    private void onRefresh() {
        searchField.clear();
        appelOffreFilterCombo.getSelectionModel().clearSelection();
        loadAppelOffreTitles();
        loadFilterCombo();
        loadData();
        applyRoleUI();
        listView.setItems(data);
    }

    @FXML
    private void onReturn() {
        if (com.example.finora.controllers.AdminShellController.getInstance() != null) {
            com.example.finora.controllers.AdminShellController.getInstance()
                    .loadCenterSafe("/ui/home/AdminHome.fxml");
        } else if (com.example.finora.controllers.EntrepriseShellController.getInstance() != null) {
            com.example.finora.controllers.EntrepriseShellController.getInstance()
                    .loadCenterSafe("/ui/home/EntrepriseHome.fxml");
        } else if (com.example.finora.controllers.UserShellController.getInstance() != null) {
            com.example.finora.controllers.UserShellController.getInstance()
                    .loadCenterSafe("/ui/home/UserHome.fxml");
        }
    }

    @FXML
    private void onNew() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/CandidatureForm.fxml"));
            Parent root = loader.load();

            CandidatureFormController formController = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Nouvelle Candidature");
            dialog.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root, 820, 600);
            ThemeManager.apply(scene);
            dialog.setScene(scene);

            formController.setDialogStage(dialog);

            dialog.showAndWait();

            if (formController.isSaved()) {
                onRefresh();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire", e.getMessage());
        }
    }

    @FXML
    private void onEdit() {
        Candidature current = getSelected();
        if (current == null) {
            showInfo("Sélection", "Aucune sélection", "Sélectionne une candidature dans la liste.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/CandidatureForm.fxml"));
            Parent root = loader.load();

            CandidatureFormController formController = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Modifier Candidature");
            dialog.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root, 820, 600);
            ThemeManager.apply(scene);
            dialog.setScene(scene);

            formController.setDialogStage(dialog);
            formController.setCandidature(current);

            dialog.showAndWait();

            if (formController.isSaved()) {
                onRefresh();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire (edit)", e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        Candidature current = getSelected();
        if (current == null) {
            showInfo("Sélection", "Aucune sélection", "Sélectionne une candidature dans la liste.");
            return;
        }

        String aoTitle = appelOffreTitles.getOrDefault(
                current.getAppelOffreId(),
                "Appel d’Offre #" + current.getAppelOffreId());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la candidature ?");
        confirm.setContentText(
                "Appel d’Offre: " + aoTitle + "\n" +
                        "Nom: " + safe(current.getNomCandidat()) + "\n" +
                        "Email: " + safe(current.getEmailCandidat()));

        ButtonType yes = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("Non", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(yes, no);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == yes) {
                try {
                    candidatureService.delete(current.getCandidatureId());
                    onRefresh();
                    showInfo("Succès", "Supprimé", "La candidature a été supprimée.");
                } catch (Exception e) {
                    showError("Erreur DB", "Suppression impossible", e.getMessage());
                }
            }
        });
    }

    private Candidature getSelected() {
        Candidature lv = listView.getSelectionModel().getSelectedItem();
        return lv != null ? lv : selected;
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