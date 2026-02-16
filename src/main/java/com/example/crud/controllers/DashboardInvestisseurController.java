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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
 * 🎯 Dashboard Investisseur
 *
 * Fonctionnalités :
 * - Vue d'ensemble des bourses et actions
 * - Top Gainers / Top Losers (simulé avec des variations aléatoires)
 * - Graphiques interactifs
 * - Watchlist (actions favorites)
 * - Statistiques en temps réel
 * - Alertes de prix
 */
public class DashboardInvestisseurController implements Initializable {

    // =====================
    // FXML Components
    // =====================

    // Header
    @FXML private Label lblBienvenue;
    @FXML private Label lblDateHeure;

    // Statistiques globales
    @FXML private Label lblTotalBourses;
    @FXML private Label lblTotalActions;
    @FXML private Label lblWatchlistCount;
    @FXML private Label lblAlertes;

    // Containers
    @FXML private FlowPane topGainersContainer;
    @FXML private FlowPane topLosersContainer;
    @FXML private VBox watchlistContainer;
    @FXML private VBox chartContainer;

    // Boutons
    @FXML private Button btnBourses;
    @FXML private Button btnActions;
    @FXML private Button btnWatchlist;
    @FXML private Button btnAlertes;
    @FXML private Button btnDeconnexion;

    // =====================
    // Services & Data
    // =====================

    private final ServiceBourse serviceBourse = new ServiceBourse();
    private final ServiceAction serviceAction = new ServiceAction();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    // Watchlist (actions favorites)
    private final Set<Integer> watchlistIds = new HashSet<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Message de bienvenue
        lblBienvenue.setText("👋 Bienvenue, Investisseur !");

        // Date et heure
        updateDateTime();

        // Charger les données
        chargerStatistiques();
        chargerTopGainersLosers();
        creerGraphiqueSecteurs();

        // Timer pour rafraîchir l'heure toutes les secondes
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateDateTime());
            }
        }, 1000, 1000);
    }

    // =====================
    // Mise à jour date/heure
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
    // Charger statistiques
    // =====================

    private void chargerStatistiques() {
        Task<Map<String, Integer>> task = new Task<>() {
            @Override
            protected Map<String, Integer> call() {
                Map<String, Integer> stats = new HashMap<>();
                stats.put("bourses", serviceBourse.getAll().size());
                stats.put("actions", serviceAction.getAll().size());
                stats.put("watchlist", watchlistIds.size());
                stats.put("alertes", 0); // TODO: implémenter service alertes
                return stats;
            }
        };

        task.setOnSucceeded(e -> {
            Map<String, Integer> stats = task.getValue();
            lblTotalBourses.setText(String.valueOf(stats.get("bourses")));
            lblTotalActions.setText(String.valueOf(stats.get("actions")));
            lblWatchlistCount.setText(String.valueOf(stats.get("watchlist")));
            lblAlertes.setText(String.valueOf(stats.get("alertes")));
        });

        executor.submit(task);
    }

    // =====================
    // Top Gainers / Losers
    // =====================

    private void chargerTopGainersLosers() {
        Task<List<Action>> task = new Task<>() {
            @Override
            protected List<Action> call() {
                return serviceAction.getAll();
            }
        };

        task.setOnSucceeded(e -> {
            List<Action> actions = task.getValue();

            if (actions.isEmpty()) {
                afficherMessageVide(topGainersContainer, "Aucune action disponible");
                afficherMessageVide(topLosersContainer, "Aucune action disponible");
                return;
            }

            // Simuler des variations de prix (en %)
            Map<Action, Double> variations = new HashMap<>();
            Random rand = new Random();

            for (Action a : actions) {
                // Variation entre -10% et +10%
                double variation = (rand.nextDouble() * 20) - 10;
                variations.put(a, variation);
            }

            // Trier par variation (ordre décroissant)
            List<Action> sorted = actions.stream()
                    .sorted((a1, a2) -> Double.compare(variations.get(a2), variations.get(a1)))
                    .collect(Collectors.toList());

            // Top 5 Gainers
            List<Action> gainers = sorted.stream()
                    .filter(a -> variations.get(a) > 0)
                    .limit(5)
                    .collect(Collectors.toList());

            afficherTopGainers(gainers, variations);

            // Top 5 Losers
            List<Action> losers = sorted.stream()
                    .filter(a -> variations.get(a) < 0)
                    .sorted((a1, a2) -> Double.compare(variations.get(a1), variations.get(a2)))
                    .limit(5)
                    .collect(Collectors.toList());

            afficherTopLosers(losers, variations);
        });

        executor.submit(task);
    }

    private void afficherTopGainers(List<Action> gainers, Map<Action, Double> variations) {
        topGainersContainer.getChildren().clear();

        if (gainers.isEmpty()) {
            afficherMessageVide(topGainersContainer, "Aucun gagnant aujourd'hui");
            return;
        }

        for (Action a : gainers) {
            VBox card = creerCarteAction(a, variations.get(a), true);
            topGainersContainer.getChildren().add(card);
        }
    }

    private void afficherTopLosers(List<Action> losers, Map<Action, Double> variations) {
        topLosersContainer.getChildren().clear();

        if (losers.isEmpty()) {
            afficherMessageVide(topLosersContainer, "Aucun perdant aujourd'hui");
            return;
        }

        for (Action a : losers) {
            VBox card = creerCarteAction(a, variations.get(a), false);
            topLosersContainer.getChildren().add(card);
        }
    }

    private VBox creerCarteAction(Action action, double variation, boolean isGainer) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(10));
        card.setPrefWidth(200);
        card.getStyleClass().add("action-card");

        // Couleur selon gainer/loser
        String borderColor = isGainer ? "#10b981" : "#ef4444"; // vert ou rouge
        String bgColor = isGainer ? "#f0fdf4" : "#fef2f2";

        card.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 2;"
        );

        // Symbole
        Label lblSymbole = new Label(action.getSymbole());
        lblSymbole.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // Nom entreprise
        Label lblNom = new Label(action.getNomEntreprise());
        lblNom.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        lblNom.setWrapText(true);

        // Prix
        Label lblPrix = new Label(String.format("%.2f %s",
                action.getPrixUnitaire(),
                action.getBourse() != null ? action.getBourse().getDevise() : ""));
        lblPrix.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Variation
        String signe = variation > 0 ? "▲" : "▼";
        String couleurTexte = isGainer ? "#10b981" : "#ef4444";
        Label lblVariation = new Label(String.format("%s %.2f%%", signe, Math.abs(variation)));
        lblVariation.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + couleurTexte + ";");

        // Bouton Watchlist
        Button btnWatch = new Button(watchlistIds.contains(action.getIdAction()) ? "★" : "☆");
        btnWatch.setStyle("-fx-background-color: transparent; -fx-font-size: 18px;");
        btnWatch.setOnAction(e -> toggleWatchlist(action.getIdAction(), btnWatch));

        HBox header = new HBox(10, lblSymbole, new Region(), btnWatch);
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);

        card.getChildren().addAll(header, lblNom, lblPrix, lblVariation);

        return card;
    }

    private void afficherMessageVide(FlowPane container, String message) {
        Label lbl = new Label(message);
        lbl.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 14px; -fx-padding: 20;");
        container.getChildren().clear();
        container.getChildren().add(lbl);
    }

    // =====================
    // Watchlist (Favoris)
    // =====================

    private void toggleWatchlist(int actionId, Button btn) {
        if (watchlistIds.contains(actionId)) {
            watchlistIds.remove(actionId);
            btn.setText("☆");
            showInfo("Retiré de la watchlist");
        } else {
            watchlistIds.add(actionId);
            btn.setText("★");
            showInfo("Ajouté à la watchlist");
        }

        chargerStatistiques();
    }

    // =====================
    // Graphiques
    // =====================

    private void creerGraphiqueSecteurs() {
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
                Label empty = new Label("📊 Aucune donnée pour les graphiques");
                empty.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 14px;");
                chartContainer.getChildren().clear();
                chartContainer.getChildren().add(empty);
                return;
            }

            // Créer un PieChart
            PieChart pieChart = new PieChart();
            pieChart.setTitle("📊 Répartition par Secteur");
            pieChart.setLegendSide(javafx.geometry.Side.RIGHT);

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            data.forEach((secteur, count) -> {
                pieData.add(new PieChart.Data(secteur + " (" + count + ")", count));
            });

            pieChart.setData(pieData);
            pieChart.setPrefHeight(300);

            chartContainer.getChildren().clear();
            chartContainer.getChildren().add(pieChart);
        });

        executor.submit(task);
    }

    // =====================
    // Navigation
    // =====================

    @FXML
    private void ouvrirBourses(ActionEvent event) {
        naviguerVers("/com/example/crud/bourse-view.fxml", "Gestion des Bourses", event);
    }

    @FXML
    private void ouvrirActions(ActionEvent event) {
        naviguerVers("/com/example/crud/action-view.fxml", "Gestion des Actions", event);
    }

    @FXML
    private void afficherWatchlist(ActionEvent event) {
        if (watchlistIds.isEmpty()) {
            showInfo("Votre watchlist est vide. Ajoutez des actions en cliquant sur ☆");
            return;
        }

        // TODO: créer une vue dédiée pour la watchlist
        showInfo("Watchlist : " + watchlistIds.size() + " action(s) suivie(s)");
    }

    @FXML
    private void afficherAlertes(ActionEvent event) {
        // TODO: implémenter système d'alertes
        showInfo("Fonctionnalité en développement");
    }

    @FXML
    private void deconnexion(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Voulez-vous vraiment vous déconnecter ?");
        confirm.setContentText("Vous serez redirigé vers la sélection de profil.");

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

    // =====================
    // Alerts
    // =====================

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}