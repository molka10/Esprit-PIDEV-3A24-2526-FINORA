package tn.finora.entities;

/**
 * Classe représentant la réponse de prédiction pour un investissement.
 * Contient le retour prédit, le risque associé et la recommandation.
 */
public class InvestmentPredictionResponse {

    private double predictedReturn;    // Retour sur investissement prédit
    private String predictedRisk;      // Niveau de risque prédit
    private String recommendation;     // Recommandation pour l'investissement

    // Constructeur complet
    public InvestmentPredictionResponse(double predictedReturn, String predictedRisk, String recommendation) {
        this.predictedReturn = predictedReturn;
        this.predictedRisk = predictedRisk;
        this.recommendation = recommendation;
    }

    // ================= GETTERS =================
    public double getPredictedReturn() {
        return predictedReturn;
    }

    public String getPredictedRisk() {
        return predictedRisk;
    }

    public String getRecommendation() {
        return recommendation;
    }

    // ================= SETTERS (optionnel) =================
    public void setPredictedReturn(double predictedReturn) {
        this.predictedReturn = predictedReturn;
    }

    public void setPredictedRisk(String predictedRisk) {
        this.predictedRisk = predictedRisk;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    @Override
    public String toString() {
        return "InvestmentPredictionResponse{" +
                "predictedReturn=" + predictedReturn +
                ", predictedRisk='" + predictedRisk + '\'' +
                ", recommendation='" + recommendation + '\'' +
                '}';
    }
}