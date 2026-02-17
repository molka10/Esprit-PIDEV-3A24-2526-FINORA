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
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Dashboardadmincontroller implements Initializable {

    @FXML private Label lblBienvenue;
    @FXML private Label lblDateHeure;

    @FXML private Label lblTotalBourses;
    @FXML private Label lblTotalActions;
    @FXML private Label lblBoursesPays;
    @FXML private Label lblSecteurs;

    @FXML private VBox chartPaysContainer;
    @FXML private VBox chartSecteursContainer;
    @FXML private VBox chartDevisesContainer;

    @FXML private ListView<String> logsListView;

    private final ServiceBourse serviceBourse = new ServiceBourse();
    private final ServiceAction serviceAction = new ServiceAction();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    private final Timer timer = new Timer(true);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lblBienvenue.setText("👨‍💼 Bienvenue, Administrateur !");
        updateDateTime();

        // refresh time every second
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateDateTime());
            }
        }, 1000, 1000);

        // load everything
        chargerStatistiquesEtLogs();
        chargerGraphiques();
    }

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
    @FXML
    private void ouvrirCommission(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/crud/Commission view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FINORA - Gestion des Commissions");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ========= Stats + Logs (fix async ordering) =========
    private void chargerStatistiquesEtLogs() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                List<Bourse> bourses = serviceBourse.getAll();
                List<Action> actions = serviceAction.getAll();

                Map<String, Object> stats = new HashMap<>();
                stats.put("totalBourses", bourses.size());
                stats.put("totalActions", actions.size());

                long nbPays = bourses.stream().map(Bourse::getPays).filter(Objects::nonNull).distinct().count();
                stats.put("nbPays", nbPays);

                long nbSecteurs = actions.stream().map(Action::getSecteur).filter(Objects::nonNull).distinct().count();
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

            // logs AFTER labels updated
            chargerLogs(
                    (int) stats.get("totalBourses"),
                    (int) stats.get("totalActions")
            );
        });

        task.setOnFailed(e -> showError("Erreur chargement statistiques : " + task.getException().getMessage()));
        executor.submit(task);
    }

    private void chargerLogs(int totalBourses, int totalActions) {
        ObservableList<String> logs = FXCollections.observableArrayList();
        Calendar cal = Calendar.getInstance();
        String heure = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));

        logs.add("[" + heure + "] ✅ Système démarré");
        logs.add("[" + heure + "] 📊 " + totalBourses + " bourses chargées");
        logs.add("[" + heure + "] 📈 " + totalActions + " actions chargées");
        logs.add("[" + heure + "] 🔐 Admin connecté");

        logsListView.setItems(logs);
    }

    // ========= Graphiques =========
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
                        .filter(b -> b.getPays() != null)
                        .collect(Collectors.groupingBy(Bourse::getPays, Collectors.counting()));
            }
        };

        task.setOnSucceeded(e -> {
            chartPaysContainer.getChildren().clear();

            Map<String, Long> data = task.getValue();
            if (data.isEmpty()) {
                chartPaysContainer.getChildren().add(new Label("📊 Aucune donnée"));
                return;
            }

            PieChart chart = new PieChart();
            chart.setLegendSide(Side.BOTTOM);
            chart.setLabelsVisible(true);

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            data.forEach((pays, count) -> pieData.add(new PieChart.Data(pays + " (" + count + ")", count)));

            chart.setData(pieData);
            chart.setPrefHeight(300);

            chartPaysContainer.getChildren().add(chart);
        });

        task.setOnFailed(e -> showError("Erreur graphique pays : " + task.getException().getMessage()));
        executor.submit(task);
    }

    private void chargerGraphiqueSecteurs() {
        Task<Map<String, Long>> task = new Task<>() {
            @Override
            protected Map<String, Long> call() {
                List<Action> actions = serviceAction.getAll();
                return actions.stream()
                        .filter(a -> a.getSecteur() != null)
                        .collect(Collectors.groupingBy(Action::getSecteur, Collectors.counting()));
            }
        };

        task.setOnSucceeded(e -> {
            chartSecteursContainer.getChildren().clear();

            Map<String, Long> data = task.getValue();
            if (data.isEmpty()) {
                chartSecteursContainer.getChildren().add(new Label("📊 Aucune donnée"));
                return;
            }

            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Secteur");
            yAxis.setLabel("Nombre d'actions");

            BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
            chart.setLegendVisible(false);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            data.forEach((secteur, count) -> series.getData().add(new XYChart.Data<>(secteur, count)));

            chart.getData().add(series);
            chart.setPrefHeight(320);

            chartSecteursContainer.getChildren().add(chart);
        });

        task.setOnFailed(e -> showError("Erreur graphique secteurs : " + task.getException().getMessage()));
        executor.submit(task);
    }

    private void chargerGraphiqueDevises() {
        Task<Map<String, Long>> task = new Task<>() {
            @Override
            protected Map<String, Long> call() {
                List<Bourse> bourses = serviceBourse.getAll();
                return bourses.stream()
                        .filter(b -> b.getDevise() != null)
                        .collect(Collectors.groupingBy(Bourse::getDevise, Collectors.counting()));
            }
        };

        task.setOnSucceeded(e -> {
            chartDevisesContainer.getChildren().clear();

            Map<String, Long> data = task.getValue();
            if (data.isEmpty()) {
                chartDevisesContainer.getChildren().add(new Label("📊 Aucune donnée"));
                return;
            }

            PieChart chart = new PieChart();
            chart.setLegendSide(Side.BOTTOM);
            chart.setLabelsVisible(true);

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            data.forEach((devise, count) -> pieData.add(new PieChart.Data(devise + " (" + count + ")", count)));

            chart.setData(pieData);
            chart.setPrefHeight(280);

            chartDevisesContainer.getChildren().add(chart);
        });

        task.setOnFailed(e -> showError("Erreur graphique devises : " + task.getException().getMessage()));
        executor.submit(task);
    }

    // ========= Navigation =========
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

            // Optionnel : stop resources when leaving admin
            // timer.cancel();
            // executor.shutdownNow();

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
