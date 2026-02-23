package tn.finora.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.finora.entities.Investment;
import tn.finora.entities.InvestmentPredictionResponse;
import tn.finora.finorainves.AppState;
import tn.finora.finorainves.SceneNavigator;
import tn.finora.services.HistoryService;
import tn.finora.services.PredictionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InvestmentDetailsController {

    @FXML private Label nameLabel;
    @FXML private Label categoryLabel;
    @FXML private Label valueLabel;
    @FXML private Label riskLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label predictionLabel;
    @FXML private ImageView imageView;
    @FXML private LineChart<String, Number> valueChart;
    @FXML private Button backBtn;

    private Investment investment;

    private final PredictionService predictionService = new PredictionService();
    private final HistoryService historyService = new HistoryService();

    @FXML
    public void initialize() {
        this.investment = AppState.getSelectedInvestment();
        loadData();
    }

    private void loadData() {
        if (investment == null) return;

        nameLabel.setText(investment.getName());
        categoryLabel.setText(investment.getCategory() + " | " + investment.getLocation());
        valueLabel.setText("Current Value: " + investment.getEstimatedValue());
        riskLabel.setText("Risk: " + investment.getRiskLevel());
        descriptionLabel.setText(investment.getDescription());

        if (investment.getImageUrl() != null && !investment.getImageUrl().isEmpty()) {
            try {
                imageView.setImage(new Image(investment.getImageUrl(), true));
            } catch (Exception ignored) {}
        }

        loadPrediction();
        loadChart();
    }

    private void loadPrediction() {
        try {
            InvestmentPredictionResponse pred = predictionService.predict(
                    (long) investment.getInvestmentId(),
                    investment.getEstimatedValue().doubleValue(),
                    investment.getRiskLevel()
            );

            predictionLabel.setText(
                    "Return: %.2f | Risk: %s | Recommendation: %s"
                            .formatted(
                                    pred.getPredictedReturn(),
                                    pred.getPredictedRisk(),
                                    pred.getRecommendation()
                            )
            );

        } catch (Exception e) {
            predictionLabel.setText("Prediction unavailable");
        }
    }

    private void loadChart() {
        valueChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Value History");

        // Historique réaliste basé sur le risque
        List<Double> history = generateHistory(investment.getEstimatedValue().doubleValue(), investment.getRiskLevel());

        int month = 1;
        for (Double value : history) {
            series.getData().add(new XYChart.Data<>("M" + month++, value));
        }

        valueChart.setCreateSymbols(true); // points visibles
        valueChart.getData().add(series);
    }

    // Génération d’un historique réaliste
    private List<Double> generateHistory(double currentValue, String riskLevel) {
        int months = 12;
        List<Double> history = new ArrayList<>();
        Random random = new Random();
        double value = currentValue;

        double volatility;
        switch (riskLevel.toLowerCase()) {
            case "high" -> volatility = 0.15; // ±15%
            case "medium" -> volatility = 0.07; // ±7%
            default -> volatility = 0.03; // ±3%
        }

        for (int i = months; i >= 1; i--) {
            double change = 1 + (random.nextDouble() * 2 - 1) * volatility; // variation ±volatility
            value = value / change; // inverse pour retrouver la valeur passée
            history.add(0, Math.round(value * 100.0) / 100.0); // arrondi 2 décimales
        }

        return history;
    }

    @FXML
    private void onBack() {
        try {
            SceneNavigator.goTo("investment_cards.fxml", "Investments");
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Impossible de revenir à la liste: " + e.getMessage());
            alert.showAndWait();
        }
    }
}