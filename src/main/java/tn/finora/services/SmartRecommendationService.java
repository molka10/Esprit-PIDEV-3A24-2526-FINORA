package tn.finora.recommendation_api.services;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SmartRecommendationService {

    public String calculateBest(List<InvestmentDTO> investments,
                                String searchKeyword,
                                String trendData) {

        double bestScore = -1;
        String bestName = null;

        for (InvestmentDTO inv : investments) {

            double score = 0;

            // 1️⃣ Match recherche
            if (inv.getCategory().toLowerCase()
                    .contains(searchKeyword.toLowerCase())) {
                score += 3;
            }

            // 2️⃣ Risk
            if ("low".equalsIgnoreCase(inv.getRiskLevel()))
                score += 3;
            else if ("medium".equalsIgnoreCase(inv.getRiskLevel()))
                score += 2;

            // 3️⃣ Valeur élevée
            if (inv.getEstimatedValue() > 500000)
                score += 2;

            // 4️⃣ Localisation stratégique
            if (inv.getLocation().toLowerCase().contains("tunis"))
                score += 2;

            // 5️⃣ Si tendance externe parle du mot
            if (trendData.toLowerCase()
                    .contains(inv.getCategory().toLowerCase())) {
                score += 2;
            }

            if (score > bestScore) {
                bestScore = score;
                bestName = inv.getName();
            }
        }

        return bestName;
    }
}