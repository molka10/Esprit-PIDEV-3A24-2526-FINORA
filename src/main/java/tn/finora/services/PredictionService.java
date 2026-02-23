package tn.finora.services;

import tn.finora.entities.InvestmentPredictionResponse;
import tn.finora.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PredictionService {

    private final Connection cnx;

    public PredictionService() {
        cnx = DBConnection.getInstance().getCnx();
    }

    public InvestmentPredictionResponse predict(Long investmentId,
                                                double currentValue,
                                                String riskLevel) throws SQLException {

        double predictedReturn = currentValue * 1.10; // exemple : +10%

        String predictedRisk = switch (riskLevel.toLowerCase()) {
            case "high" -> "Medium";
            case "medium" -> "Low";
            default -> "Low";
        };

        String recommendation = predictedReturn > currentValue ? "BUY" : "HOLD";

        // Sauvegarde en DB
        savePrediction(investmentId, predictedReturn, predictedRisk, recommendation);

        // ⚡ NE PAS PASSER investmentId au constructeur
        return new InvestmentPredictionResponse(
                predictedReturn,
                predictedRisk,
                recommendation
        );
    }

    private void savePrediction(Long investmentId,
                                double predictedReturn,
                                String predictedRisk,
                                String recommendation) throws SQLException {

        String sql = """
                INSERT INTO investment_prediction_history
                (investment_id, predicted_return, predicted_risk, recommendation)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, investmentId);
            ps.setDouble(2, predictedReturn);
            ps.setString(3, predictedRisk);
            ps.setString(4, recommendation);
            ps.executeUpdate();
        }
    }
}