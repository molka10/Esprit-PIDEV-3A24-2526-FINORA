package com.example.crud.controllers;

import com.example.crud.models.Action;
import com.example.crud.models.Bourse;
import com.example.crud.services.ServiceAction;
import com.example.crud.services.ServiceBourse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 🎯 Dashboard Admin
 *
 * Fonctionnalités :
 * - Vue d'ensemble complète du système
 * - Statistiques avancées (répartition pays, secteurs, devises)
 * - Graphiques multiples (PieChart, BarChart)
 * - Accès rapide au CRUD
 * - Logs d'activité (simulé)
 */
public class Dashboardadmincontroller implements Initializable {

    // =====================
    // FXML Components
    // =====================

    @FXML private Label lblBienvenue;
    @FXML private Label lblDateHeure;

    // Statistiques globales
    @FXML private Label lblTotalBourses;
    @FXML private Label lblTotalActions;
    @FXML private Label lblBoursesPays;
    @FXML private Label lblSecteurs;

    // Graphiques
    @FXML private VBox chartPaysContainer;
    @FXML private VBox chartSecteursContainer;
    @FXML private VBox chartDevisesContainer;

    // Logs
    @FXML private ListView<String> logsListView;

    // =====================
    // Services
    // =====================

    private final ServiceBourse serviceBourse = new ServiceBourse();
    private final ServiceAction serviceAction = new ServiceAction();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lblBienvenue.setText("👨‍💼 Bienvenue, Administrateur !");

        updateDateTime();
        chargerStatistiques();
        chargerGraphiques();
        chargerLogs();

        // Timer pour l'heure
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateDateTime());
            }
        }, 1000, 1000);
    }

    // =====================
    // Date/Heure
    // =====================

    private void updateDateTime() {
        Calendar cal = Calendar.getInstance();
        String jour = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
        String mois = String.format("%02d", cal.get(Calendar.MONTH) + 1);
        String annee = String.valueOf(cal.get(Calendar.YEAR));
        String heure = String.format("%02d:%02d:%02d",
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND));

        lblDateHeure.setText("📅 " + jour + "/" + mois + "/" + annee + " - ⏰ " + heure);
    }

    // =====================
    // Statistiques
    // =====================

    private void chargerStatistiques() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                List<Bourse> bourses = serviceBourse.getAll();
                List<Action> actions = serviceAction.getAll();

                Map<String, Object> stats = new HashMap<>();
                stats.put("totalBourses", bourses.size());
                stats.put("totalActions", actions.size());

                // Nombre de pays distincts
                long nbPays = bourses.stream()
                        .map(Bourse::getPays)
                        .distinct()
                        .count();
                stats.put("nbPays", nbPays);

                // Nombre de secteurs distincts
                long nbSecteurs = actions.stream()
                        .map(Action::getSecteur)
                        .distinct()
                        .count();
                stats.put("nbSecteurs", nbSecteurs);

                return stats;
            }
        };

        task.setOnSucceeded(e -> {
            Map<String, Object> stats = task.getValue();
            lblTotalBourses.setText(String.valueOf(stats.get("totalBourses")));
            lblTotalActions.setText(String.valueOf(stats.get("totalActions")));
            lblBoursesPays.setText(String.valueOf(stats.get("nbPays")));
            lblSecteurs.setText(String.valueOf(stats.get("nbSecteurs")));
        });

        executor.submit(task);
    }

    // =====================
    // Graphiques
    // =====================

    private void chargerGraphiques() {
        chargerGraphiquePays();
        chargerGraphiqueSecteurs();
        chargerGraphiqueDevises();
    }

    private void chargerGraphiquePays() {
        Task<Map<String, Long>> task = new Task<>() {
            @Override
            protected Map<String, Long> call() {
                List<Bourse> bourses = serviceBourse.getAll();

                return bourses.stream()
                        .collect(Collectors.groupingBy(
                                Bourse::getPays,
                                Collectors.counting()
                        ));
            }
        };

        task.setOnSucceeded(e -> {
            Map<String, Long> data = task.getValue();

            if (data.isEmpty()) {
                Label empty = new Label("📊 Aucune donnée");
                chartPaysContainer.getChildren().add(empty);
                return;
            }

            PieChart chart = new PieChart();
            chart.setTitle("🌍 Bourses par Pays");
            chart.setLegendSide(javafx.geometry.Side.BOTTOM);

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            data.forEach((pays, count) -> {
                pieData.add(new PieChart.Data(pays + " (" + count + ")", count));
            });

            chart.setData(pieData);
            chart.setPrefHeight(300);

            chartPaysContainer.getChildren().clear();
            chartPaysContainer.getChildren().add(chart);
        });

        executor.submit(task);
    }

    private void chargerGraphiqueSecteurs() {
        Task<Map<String, Long>> task = new Task<>() {
            @Override
            protected Map<String, Long> call() {
                List<Action> actions = serviceAction.getAll();

                return actions.stream()
                        .collect(Collectors.groupingBy(
                                Action::getSecteur,
                                Collectors.counting()
                        ));
            }
        };

        task.setOnSucceeded(e -> {
            Map<String, Long> data = task.getValue();

            if (data.isEmpty()) {
                Label empty = new Label("📊 Aucune donnée");
                chartSecteursContainer.getChildren().add(empty);
                return;
            }

            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Secteur");
            yAxis.setLabel("Nombre d'actions");

            BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
            chart.setTitle("🏷️ Actions par Secteur");
            chart.setLegendVisible(false);

            XYChart.Series<String, Number> series = new XYChart.Series<>();

            data.forEach((secteur, count) -> {
                series.getData().add(new XYChart.Data<>(secteur, count));
            });

            chart.getData().add(series);
            chart.setPrefHeight(300);

            chartSecteursContainer.getChildren().clear();
            chartSecteursContainer.getChildren().add(chart);
        });

        executor.submit(task);
    }

    private void chargerGraphiqueDevises() {
        Task<Map<String, Long>> task = new Task<>() {
            @Override
            protected Map<String, Long> call() {
                List<Bourse> bourses = serviceBourse.getAll();

                return bourses.stream()
                        .collect(Collectors.groupingBy(
                                Bourse::getDevise,
                                Collectors.counting()
                        ));
            }
        };

        task.setOnSucceeded(e -> {
            Map<String, Long> data = task.getValue();

            if (data.isEmpty()) {
                Label empty = new Label("📊 Aucune donnée");
                chartDevisesContainer.getChildren().add(empty);
                return;
            }

            PieChart chart = new PieChart();
            chart.setTitle("💰 Bourses par Devise");
            chart.setLegendSide(javafx.geometry.Side.BOTTOM);

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            data.forEach((devise, count) -> {
                pieData.add(new PieChart.Data(devise + " (" + count + ")", count));
            });

            chart.setData(pieData);
            chart.setPrefHeight(250);

            chartDevisesContainer.getChildren().clear();
            chartDevisesContainer.getChildren().add(chart);
        });

        executor.submit(task);
    }

    // =====================
    // Logs (simulé)
    // =====================

    private void chargerLogs() {
        ObservableList<String> logs = FXCollections.observableArrayList();

        Calendar cal = Calendar.getInstance();
        String heure = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));

        logs.add("[" + heure + "] ✅ Système démarré");
        logs.add("[" + heure + "] 📊 " + lblTotalBourses.getText() + " bourses chargées");
        logs.add("[" + heure + "] 📈 " + lblTotalActions.getText() + " actions chargées");
        logs.add("[" + heure + "] 🔐 Admin connecté");

        logsListView.setItems(logs);
    }

    // =====================
    // Navigation
    // =====================

    @FXML
    private void gererBourses(ActionEvent event) {
        naviguerVers("/com/example/crud/bourse-view.fxml", "Gestion des Bourses", event);
    }

    @FXML
    private void gererActions(ActionEvent event) {
        naviguerVers("/com/example/crud/action-view.fxml", "Gestion des Actions", event);
    }

    @FXML
    private void deconnexion(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Voulez-vous vraiment vous déconnecter ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            naviguerVers("/com/example/crud/utilisateur-static-view.fxml", "FINORA - Choisir Profil", event);
        }
    }

    private void naviguerVers(String fxml, String titre, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de navigation : " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}