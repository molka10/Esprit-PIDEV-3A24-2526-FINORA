package com.example.crud.controllers;

import com.example.crud.entities.Action;
import com.example.crud.services.ServiceAction;
import com.example.crud.services.ServicePrediction;
import com.example.crud.services.ServicePrediction.ResultatPrediction;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 🎯 PredictionController
 * Interface de prédiction de prix avec IA
 */
public class PredictionController implements Initializable {

    @FXML private ComboBox<Action> cbAction;
    @FXML private Button btnAnalyser;

    // Résultats
    @FXML private VBox resultsContainer;
    @FXML private VBox loadingContainer;

    // Info action
    @FXML private Label lblActionNom;
    @FXML private Label lblSecteur;
    @FXML private Label lblPrixActuel;

    // Prédictions
    @FXML private Label lblTendance;
    @FXML private Label lblPred7j;
    @FXML private Label lblPred30j;
    @FXML private ProgressBar progressConfiance;
    @FXML private Label lblConfiance;

    // Scénarios
    @FXML private Label lblScenarioOpt;
    @FXML private Label lblScenarioReal;
    @FXML private Label lblScenarioPess;

    // Analyse
    @FXML private TextArea txtAnalyse;

    // Chart
    @FXML private LineChart<String, Number> lineChart;

    private final ServiceAction serviceAction = new ServiceAction();
    private final ServicePrediction servicePrediction = new ServicePrediction();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r); t.setDaemon(true); return t;
    });

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Vérifier configuration API
        if (!servicePrediction.estConfigure()) {
            afficherErreurConfiguration();
            return;
        }

        // Charger les actions
        chargerActions();

        // Configurer le ComboBox
        cbAction.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Action action, boolean empty) {
                super.updateItem(action, empty);
                if (empty || action == null) {
                    setText(null);
                } else {
                    setText(action.getSymbole() + " - " + action.getNomEntreprise() +
                            " (" + action.getPrixUnitaire() + " " + action.getBourse().getDevise() + ")");
                }
            }
        });

        cbAction.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Action action, boolean empty) {
                super.updateItem(action, empty);
                if (empty || action == null) {
                    setText(null);
                } else {
                    setText(action.getSymbole() + " - " + action.getNomEntreprise());
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════
    //  CHARGEMENT ACTIONS
    // ═══════════════════════════════════════════════════════

    private void chargerActions() {
        Task<List<Action>> task = new Task<>() {
            @Override
            protected List<Action> call() {
                return serviceAction.getAll();
            }
        };

        task.setOnSucceeded(e -> {
            List<Action> actions = task.getValue();
            cbAction.setItems(FXCollections.observableArrayList(actions));
            if (!actions.isEmpty()) {
                cbAction.getSelectionModel().selectFirst();
            }
        });

        task.setOnFailed(e -> showError("Erreur chargement actions"));
        executor.submit(task);
    }

    // ═══════════════════════════════════════════════════════
    //  ANALYSER ACTION
    // ═══════════════════════════════════════════════════════

    @FXML
    private void analyserAction(ActionEvent event) {
        Action action = cbAction.getValue();
        if (action == null) {
            showError("Sélectionnez une action !");
            return;
        }

        // Afficher loading
        resultsContainer.setVisible(false);
        resultsContainer.setManaged(false);
        loadingContainer.setVisible(true);
        loadingContainer.setManaged(true);
        btnAnalyser.setDisable(true);
        cbAction.setDisable(true);

        // Lancer l'analyse en arrière-plan
        Task<ResultatPrediction> task = new Task<>() {
            @Override
            protected ResultatPrediction call() {
                return servicePrediction.predirePrix(action);
            }
        };

        task.setOnSucceeded(e -> {
            ResultatPrediction resultat = task.getValue();
            afficherResultats(action, resultat);

            loadingContainer.setVisible(false);
            loadingContainer.setManaged(false);
            resultsContainer.setVisible(true);
            resultsContainer.setManaged(true);
            btnAnalyser.setDisable(false);
            cbAction.setDisable(false);
        });

        task.setOnFailed(e -> {
            showError("Erreur analyse : " + task.getException().getMessage());
            loadingContainer.setVisible(false);
            loadingContainer.setManaged(false);
            btnAnalyser.setDisable(false);
            cbAction.setDisable(false);
        });

        executor.submit(task);
    }

    // ═══════════════════════════════════════════════════════
    //  AFFICHER RÉSULTATS
    // ═══════════════════════════════════════════════════════

    private void afficherResultats(Action action, ResultatPrediction resultat) {
        // Info action
        lblActionNom.setText(action.getSymbole() + " - " + action.getNomEntreprise());
        lblSecteur.setText(action.getSecteur());
        lblPrixActuel.setText(String.format("%.2f %s", action.getPrixUnitaire(), action.getBourse().getDevise()));

        // Prédictions
        lblTendance.setText(resultat.tendance);
        lblTendance.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " +
                getCouleurTendance(resultat.tendance) + ";");

        lblPred7j.setText(String.format("%.2f %s", resultat.prediction7j, action.getBourse().getDevise()));
        lblPred30j.setText(String.format("%.2f %s", resultat.prediction30j, action.getBourse().getDevise()));

        progressConfiance.setProgress(resultat.confiance / 100.0);
        lblConfiance.setText(resultat.confiance + "%");

        // Scénarios
        lblScenarioOpt.setText(String.format("%.2f %s", resultat.scenarioOptimiste, action.getBourse().getDevise()));
        lblScenarioReal.setText(String.format("%.2f %s", resultat.scenarioRealiste, action.getBourse().getDevise()));
        lblScenarioPess.setText(String.format("%.2f %s", resultat.scenarioPessimiste, action.getBourse().getDevise()));

        // Analyse
        txtAnalyse.setText(resultat.reponseComplete);

        // Graphique
        afficherGraphique(action, resultat);
    }

    private String getCouleurTendance(String tendance) {
        if (tendance == null) return "#6b7280";
        if (tendance.toLowerCase().contains("haussi")) return "#10b981";
        if (tendance.toLowerCase().contains("baissi")) return "#ef4444";
        return "#f59e0b";
    }

    // ═══════════════════════════════════════════════════════
    //  GRAPHIQUE
    // ═══════════════════════════════════════════════════════

    private void afficherGraphique(Action action, ResultatPrediction resultat) {
        lineChart.getData().clear();

        // Série historique
        XYChart.Series<String, Number> serieHistorique = new XYChart.Series<>();
        serieHistorique.setName("Historique");

        List<Double> historique = servicePrediction.genererHistoriquePrix(action, 30);

        for (int i = 0; i < historique.size(); i++) {
            if (i % 3 == 0) { // Afficher tous les 3 jours
                serieHistorique.getData().add(new XYChart.Data<>("J-" + (30 - i), historique.get(i)));
            }
        }

        // Série prédiction
        XYChart.Series<String, Number> seriePrediction = new XYChart.Series<>();
        seriePrediction.setName("Prédiction");

        seriePrediction.getData().add(new XYChart.Data<>("Actuel", action.getPrixUnitaire()));
        seriePrediction.getData().add(new XYChart.Data<>("+7j", resultat.prediction7j));
        seriePrediction.getData().add(new XYChart.Data<>("+30j", resultat.prediction30j));

        lineChart.getData().addAll(serieHistorique, seriePrediction);

        // Styles
        Platform.runLater(() -> {
            // Historique en gris
            lineChart.lookupAll(".series0").forEach(node ->
                    node.setStyle("-fx-stroke: #6b7280; -fx-stroke-width: 2px;")
            );
            // Prédiction en bleu pointillé
            lineChart.lookupAll(".series1").forEach(node ->
                    node.setStyle("-fx-stroke: #6366f1; -fx-stroke-width: 3px; -fx-stroke-dash-array: 5 5;")
            );
        });
    }

    // ═══════════════════════════════════════════════════════
    //  ERREUR CONFIGURATION
    // ═══════════════════════════════════════════════════════

    private void afficherErreurConfiguration() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Configuration requise");
        alert.setHeaderText("Clé API Anthropic manquante");
        alert.setContentText(
                "Pour utiliser la prédiction IA, vous devez configurer votre clé API.\n\n" +
                        "1. Obtenez une clé sur : https://console.anthropic.com/\n" +
                        "2. Modifiez ServicePrediction.java\n" +
                        "3. Remplacez 'VOTRE_CLE_API_ICI' par votre clé"
        );
        alert.showAndWait();

        btnAnalyser.setDisable(true);
    }

    // ═══════════════════════════════════════════════════════
    //  NAVIGATION
    // ═══════════════════════════════════════════════════════

    @FXML
    private void retourDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/crud/dashboard-investisseur-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FINORA - Dashboard");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════
    //  UTILS
    // ═══════════════════════════════════════════════════════

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}