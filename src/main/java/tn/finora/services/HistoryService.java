package tn.finora.services;

import tn.finora.entities.Investment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HistoryService {

    private final Random random = new Random();

    /**
     * Retourne l'historique mensuel pour un investissement
     */
    public List<Double> getHistory(int investmentId, double currentValue, String riskLevel) {
        int months = 12; // Historique sur 12 mois
        List<Double> history = new ArrayList<>();

        double value = currentValue;

        // Définir un facteur de volatilité selon le risque
        double volatility;
        switch (riskLevel.toLowerCase()) {
            case "high" -> volatility = 0.15; // ±15%
            case "medium" -> volatility = 0.07; // ±7%
            default -> volatility = 0.03; // ±3%
        }

        // Générer l'historique à rebours
        for (int i = months; i >= 1; i--) {
            double change = 1 + (random.nextDouble() * 2 - 1) * volatility; // variation ±volatility
            value = value / change; // calcul inverse pour retrouver la valeur passée
            history.add(0, Math.round(value * 100.0) / 100.0); // 2 décimales
        }

        return history;
    }
}