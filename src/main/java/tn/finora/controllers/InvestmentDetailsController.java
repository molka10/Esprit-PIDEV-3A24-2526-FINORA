package tn.finora.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import tn.finora.entities.Investment;
import tn.finora.entities.InvestmentPredictionResponse;
import tn.finora.services.PredictionService;
import tn.finora.finorainves.AppState;

public class InvestmentDetailsController {

    @FXML private Label nameLabel;
    @FXML private Label categoryLabel;
    @FXML private Label valueLabel;
    @FXML private Label riskLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label predictionLabel;
    @FXML private LineChart<String, Number> valueChart;
    @FXML private TextArea commentsArea;

    private final PredictionService predictionService = new PredictionService();
    private Investment investment;

    @FXML
    public void initialize() {
        investment = AppState.getSelectedInvestment();
        if (investment == null) return;

        // Remplissage des labels
        nameLabel.setText(investment.getName());
        categoryLabel.setText(investment.getCategory() + " | " + investment.getLocation());
        valueLabel.setText("Current Value: " + investment.getEstimatedValue());
        riskLabel.setText("Risk: " + investment.getRiskLevel());
        descriptionLabel.setText(investment.getDescription());

        // Prediction
        try {
            InvestmentPredictionResponse pred = predictionService.predict(
                    (long) investment.getInvestmentId(),
                    investment.getEstimatedValue() != null ? investment.getEstimatedValue().doubleValue() : 0,
                    investment.getRiskLevel()
            );
            predictionLabel.setText("Predicted Return: " + String.format("%.2f", pred.getPredictedReturn())
                    + " | Risk: " + pred.getPredictedRisk()
                    + " | Recommendation: " + pred.getRecommendation());
        } catch (Exception e) {
            predictionLabel.setText("Prediction unavailable");
        }

        // Graphique historique fictif
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Value History");
        series.getData().add(new XYChart.Data<>("Jan", 1000));
        series.getData().add(new XYChart.Data<>("Feb", 1100));
        series.getData().add(new XYChart.Data<>("Mar", 1050));
        valueChart.getData().add(series);
    }

    @FXML
    private void onAddComment() {
        String comment = commentsArea.getText().trim();
        if (!comment.isEmpty()) {
            // TODO: sauvegarder le commentaire en DB
            commentsArea.clear();
            System.out.println("Comment added: " + comment);
        }
    }
}