<?php

namespace App\Service;

class AIAnalyzerService
{
    public function analyze(float $income, float $expenses): array
    {
        $balance = $income - $expenses;
        // Avoid division by zero
        $ratio = $income > 0 ? ($expenses / $income) * 100 : ($expenses > 0 ? 100 : 0);

        // Advanced Risk level
        $cssClass = 'low';
        if ($ratio > 90) {
            $risk = "Critique";
            $cssClass = 'high';
        } elseif ($ratio > 70) {
            $risk = "Élevé";
            $cssClass = 'high';
        } elseif ($ratio > 40) {
            $risk = "Modéré";
            $cssClass = 'medium';
        } else {
            $risk = "Faible";
            $cssClass = 'low';
        }

        // Advanced Prediction (Predictive analytics using baseline trend curve)
        $volatilityFactor = $ratio > 80 ? 1.08 : 1.02; 
        $predictedExpenses = $expenses * $volatilityFactor;
        $predictedBalance = $income - $predictedExpenses;

        // Rich Insights & Recommendations (in French)
        $recommendation = [];
        
        if ($ratio > 90) {
            $recommendation[] = "⚠️ Alerte Rouge : Réduisez vos dépenses immédiatement. Plus de 90% de vos revenus sont engloutis.";
        } elseif ($ratio > 70) {
            $recommendation[] = "📊 Attention : Optimisez votre budget. Vous êtes exposé à un risque de déficit en cas d'imprévu.";
        } else {
            $recommendation[] = "✅ Bonne gestion : Votre ratio dépenses/revenus est très sain.";
        }

        if ($balance < 0) {
            $recommendation[] = "❌ Découvert détecté : Vous vivez au-dessus de vos moyens. Stoppez les achats non essentiels.";
        } elseif ($balance > 0 && $balance <= ($income * 0.10)) {
            $recommendation[] = "💡 Conseil : Vous épargnez, mais le montant est faible. Visez au moins 20% d'épargne mensuelle.";
        } elseif ($balance > ($income * 0.20)) {
            $recommendation[] = "🚀 Croissance optimale : Vous construisez une excellente réserve financière. Pensez à l'investissement.";
        }

        return [
            'income' => $income,
            'expenses' => $expenses,
            'balance' => round($balance, 2),
            'ratio' => round($ratio, 2),
            'risk' => $risk, 
            'cssClass' => $cssClass,
            'predictedExpenses' => round($predictedExpenses, 2),
            'predictedBalance' => round($predictedBalance, 2),
            'recommendation' => $recommendation
        ];
    }
}