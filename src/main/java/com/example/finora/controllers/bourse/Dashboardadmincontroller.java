package com.example.finora.controllers.bourse;

import com.example.finora.entities.Action;
import com.example.finora.entities.Bourse;
import com.example.finora.services.bourse.ServiceAction;
import com.example.finora.services.bourse.ServiceBourse;
import com.example.finora.services.bourse.ServiceTransactionBourse;
import com.example.finora.utils.Navigator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 📊 DashboardAdminController - Version Moderne
 * Dashboard professionnel avec graphiques et statistiques
 */
public class Dashboardadmincontroller implements Initializable {

    // Header
    @FXML
    private Label lblDateTime;

    // Stats cards
    @FXML
    private Label lblNbBourses;
    @FXML
    private Label lblNbActions;
    @FXML
    private Label lblNbTransactions;
    @FXML
    private Label lblTransactionsDetail;
    @FXML
    private Label lblCommissions;

    // Charts
    @FXML
    private LineChart<String, Number> lineChart;
    @FXML
    private PieChart pieChartPays;
    @FXML
    private BarChart<String, Number> barChartSecteur;

    // Logs
    @FXML
    private VBox logsContainer;

    // Services
    private final ServiceBourse serviceBourse = new ServiceBourse();
    private final ServiceAction serviceAction = new ServiceAction();
    private final ServiceTransactionBourse serviceTransaction = new ServiceTransactionBourse();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Date/heure
        lblDateTime.setText(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.FRENCH)));

        // Configurer les graphiques
        configureCharts();

        // Charger les données
        chargerDonnees();

        // Ajouter les logs système
        ajouterLogs();
    }

    // ═══════════════════════════════════════════════════════
    // CHARGEMENT DES DONNÉES
    // ═══════════════════════════════════════════════════════

    private void chargerDonnees() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                // Charger toutes les données en parallèle
                List<Bourse> bourses = serviceBourse.getAll();
                List<Action> actions = serviceAction.getAll();
                int nbTransactions = serviceTransaction.getNombreTransactions();
                double commissions = serviceTransaction.getTotalCommissions();

                Platform.runLater(() -> {
                    // Mettre à jour les stats
                    lblNbBourses.setText(String.valueOf(bourses.size()));
                    lblNbActions.setText(String.valueOf(actions.size()));
                    lblNbTransactions.setText(String.valueOf(nbTransactions));
                    lblTransactionsDetail.setText("Total enregistrées");
                    lblCommissions.setText(String.format("%.2f TND", commissions));

                    // Remplir les graphiques
                    remplirLineChart();
                    remplirPieChartPays(bourses);
                    remplirBarChartSecteur(actions);
                });

                return null;
            }
        };

        task.setOnFailed(e -> System.err.println("Erreur chargement : " + task.getException()));
        executor.submit(task);
    }

    // ═══════════════════════════════════════════════════════
    // CONFIGURATION DES GRAPHIQUES
    // ═══════════════════════════════════════════════════════

    private void configureCharts() {
        // LineChart
        lineChart.setLegendVisible(false);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(true);

        // PieChart
        pieChartPays.setLegendVisible(true);
        pieChartPays.setAnimated(false);
        pieChartPays.setLabelsVisible(false);

        // BarChart
        barChartSecteur.setLegendVisible(false);
        barChartSecteur.setAnimated(false);
    }

    // ═══════════════════════════════════════════════════════
    // LINE CHART - ÉVOLUTION DES TRANSACTIONS
    // ═══════════════════════════════════════════════════════

    private void remplirLineChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Transactions");

        String[] jours = { "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim" };
        Random rand = new Random();

        int base = 5;
        for (String jour : jours) {
            base += rand.nextInt(5) - 2;
            if (base < 0)
                base = 0;
            series.getData().add(new XYChart.Data<>(jour, base));
        }

        lineChart.getData().clear();
        lineChart.getData().add(series);

        // Style custom
        Platform.runLater(() -> {
            lineChart.lookup(".chart-series-line").setStyle("-fx-stroke: #6366f1; -fx-stroke-width: 3px;");
            lineChart.lookupAll(".chart-line-symbol").forEach(node -> node.setStyle(
                    "-fx-background-color: #6366f1, white; -fx-background-insets: 0, 2; -fx-background-radius: 5px; -fx-padding: 5px;"));
        });
    }

    // ═══════════════════════════════════════════════════════
    // PIE CHART - BOURSES PAR PAYS
    // ═══════════════════════════════════════════════════════

    private void remplirPieChartPays(List<Bourse> bourses) {
        // Compter les bourses par pays
        Map<String, Long> countByPays = bourses.stream()
                .collect(Collectors.groupingBy(Bourse::getPays, Collectors.counting()));

        pieChartPays.getData().clear();

        // Couleurs personnalisées
        String[] colors = { "#6366f1", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6", "#06b6d4" };
        int colorIndex = 0;

        for (Map.Entry<String, Long> entry : countByPays.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                    entry.getKey() + " (" + entry.getValue() + ")",
                    entry.getValue());
            pieChartPays.getData().add(slice);

            // Appliquer la couleur
            final String color = colors[colorIndex % colors.length];
            Platform.runLater(() -> {
                slice.getNode().setStyle("-fx-pie-color: " + color + ";");
            });
            colorIndex++;
        }
    }

    // ═══════════════════════════════════════════════════════
    // BAR CHART - ACTIONS PAR SECTEUR
    // ═══════════════════════════════════════════════════════

    private void remplirBarChartSecteur(List<Action> actions) {
        // Compter les actions par secteur
        Map<String, Long> countBySecteur = actions.stream()
                .collect(Collectors.groupingBy(Action::getSecteur, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Actions");

        for (Map.Entry<String, Long> entry : countBySecteur.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChartSecteur.getData().clear();
        barChartSecteur.getData().add(series);

        // Style custom (barres orange)
        Platform.runLater(() -> {
            barChartSecteur.lookupAll(".default-color0.chart-bar")
                    .forEach(node -> node.setStyle("-fx-bar-fill: #f59e0b;"));
        });
    }

    // ═══════════════════════════════════════════════════════
    // LOGS SYSTÈME
    // ═══════════════════════════════════════════════════════

    private void ajouterLogs() {
        String timestamp = new SimpleDateFormat("HH:mm").format(new Date());

        ajouterLog(timestamp, "✓", "Système démarré", "#10b981");
        ajouterLog(timestamp, "📊", serviceBourse.getAll().size() + " bourses chargées", "#6366f1");
        ajouterLog(timestamp, "📈", serviceAction.getAll().size() + " actions chargées", "#10b981");
        ajouterLog(timestamp, "👤", "Admin connecté", "#f59e0b");
    }

    private void ajouterLog(String time, String icon, String message, String color) {
        HBox logItem = new HBox(12);
        logItem.setPadding(new Insets(10));
        logItem.setAlignment(Pos.CENTER_LEFT);
        logItem.setStyle("-fx-background-color: #0f111777; -fx-background-radius: 8;");

        Label timeLabel = new Label("[" + time + "]");
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-font-family: monospace;");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 14px;");

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");

        logItem.getChildren().addAll(timeLabel, iconLabel, msgLabel);
        logsContainer.getChildren().add(logItem);
    }

    // ═══════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════

    @FXML
    private void navDashboard(ActionEvent event) {
        // Déjà sur le dashboard
    }

    @FXML
    private void ouvrirBourses(ActionEvent event) {
        naviguerVers("/bourse/bourse-view.fxml", "FINORA - Gestion Bourses", event);
    }

    @FXML
    private void ouvrirActions(ActionEvent event) {
        naviguerVers("/bourse/action-view.fxml", "FINORA - Gestion Actions", event);
    }

    @FXML
    private void ouvrirCommission(ActionEvent event) {
        naviguerVers("/bourse/Commission view.fxml", "FINORA - Gestion Commissions", event);
    }

    @FXML
    private void ouvrirSupervision(ActionEvent event) {
        naviguerVers("/bourse/supervision-view.fxml", "FINORA - Supervision", event);
    }

    @FXML
    private void deconnexion(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Navigator.returnToDashboard(stage);
    }

    @FXML
    private void handleActualiser(ActionEvent event) {
        chargerDonnees();
        logsContainer.getChildren().clear();
        ajouterLogs();

        String timestamp = new SimpleDateFormat("HH:mm").format(new Date());
        ajouterLog(timestamp, "🔄", "Données actualisées", "#6366f1");
    }

    private void naviguerVers(String fxml, String titre, ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Navigator.goTo(stage, fxml, titre);
    }
}