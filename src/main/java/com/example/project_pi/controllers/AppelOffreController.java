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
import com.example.project_pi.services.CandidatureService;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import com.example.project_pi.entities.Candidature;
import com.example.project_pi.services.CandidatureService;
import com.example.project_pi.services.PdfExportService;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.sql.SQLException;
import java.util.List;

public class AppelOffreController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilterCombo;
    @FXML private TabPane tabPane;

    @FXML private ListView<AppelOffre> listView;
    @FXML private PieChart statutPie;
    @FXML private BarChart<String, Number> candidatureBar;

    private final CandidatureService candidatureService = new CandidatureService();
    @FXML private Label totalLabel;
    @FXML private Label publishedLabel;
    @FXML private Label draftLabel;

    private final AppelOffreService service = new AppelOffreService();
    private final PdfExportService pdfExportService = new PdfExportService();
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
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && "Statistiques".equals(newTab.getText())) {
                updateStats(filtered);          // or updateStats(data)
                updateCharts(data);             // your charts method
            }
        });
    }

    private void loadData() {
        try {
            List<AppelOffre> all = service.getAll();
            data.setAll(all);
            updateCharts(all);
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
    private void updateCharts(List<AppelOffre> all) {
        // ---------- PieChart: statut distribution ----------
        long draft = all.stream().filter(a -> "draft".equalsIgnoreCase(a.getStatut())).count();
        long published = all.stream().filter(a -> "published".equalsIgnoreCase(a.getStatut())).count();
        long closed = all.stream().filter(a -> "closed".equalsIgnoreCase(a.getStatut())).count();

        statutPie.getData().setAll(
                new PieChart.Data("Draft", draft),
                new PieChart.Data("Published", published),
                new PieChart.Data("Closed", closed)
        );
        statutPie.setLabelsVisible(true);

        // ---------- BarChart: candidatures per appel offre (Top 8) ----------
        candidatureBar.getData().clear();

        Map<Integer, Integer> counts;
        try {
            counts = candidatureService.getCountsByAppelOffreId();
        } catch (Exception e) {
            // if DB fails, keep chart empty but don't crash UI
            return;
        }

        // Build list (title -> count), sort desc, take top 8
        var top = all.stream()
                .map(a -> Map.entry(a.getTitre(), counts.getOrDefault(a.getAppelOffreId(), 0)))
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(8)
                .collect(Collectors.toList());

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Candidatures");

        for (var entry : top) {
            String title = entry.getKey() == null ? "-" : entry.getKey();
            int cnt = entry.getValue();

            // make labels shorter so x-axis doesn't explode
            String shortTitle = title.length() > 18 ? title.substring(0, 18) + "…" : title;
            series.getData().add(new XYChart.Data<>(shortTitle, cnt));
        }

        candidatureBar.getData().add(series);
        candidatureBar.setLegendVisible(false);
        candidatureBar.setAnimated(false);
    }
    @FXML
    private void onExportPdf() {
        AppelOffre current = listView.getSelectionModel().getSelectedItem();

        if (current == null) {
            showInfo("Sélection", "Aucune sélection", "Sélectionne un appel d'offre dans la liste.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        String safeName = (current.getTitre() == null ? "appel_offre" : current.getTitre())
                .replaceAll("[\\\\/:*?\"<>|]", "_");

        chooser.setInitialFileName("AppelOffre_" + safeName + ".pdf");

        File file = chooser.showSaveDialog(listView.getScene().getWindow());
        if (file == null) return;

        try {
            List<Candidature> cands = candidatureService.getByAppelOffreId(current.getAppelOffreId());
            pdfExportService.exportAppelOffreWithCandidatures(current, cands, file);
            showInfo("Export PDF", "Succès", "PDF exporté avec succès:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            showError("Export PDF", "Erreur fichier", e.getMessage());
        } catch (Exception e) {
            showError("Export PDF", "Erreur", e.getMessage());
        }
    }
}