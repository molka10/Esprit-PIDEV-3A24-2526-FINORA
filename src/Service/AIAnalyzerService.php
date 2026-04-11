<?php

namespace App\Service;

class AIAnalyzerService
{
    public function analyze(float $income, float $expenses): array
    {
        $balance = $income - $expenses;
        $ratio = ($expenses / $income) * 100;

        // Risk level
        if ($ratio > 80) {
            $risk = "High";
        } elseif ($ratio > 60) {
            $risk = "Medium";
        } else {
            $risk = "Low";
        }

        // Prediction (simple trend)
        $predictedExpenses = $expenses * 1.05; // +5%
        $predictedBalance = $income - $predictedExpenses;

        // Recommendation
        $recommendation = [];
        if ($ratio > 80) {
            $recommendation[] = " Reduce your expenses immediately.";
        }
        if ($balance > 0) {
            $recommendation[] = " You can save part of your income.";
        } else {
            $recommendation[] = " You are spending more than you earn.";
        }

        return [
    'income' => $income,
    'expenses' => $expenses,
    'balance' => round($balance, 2),
    'ratio' => round($ratio, 2),
    'risk' => $risk, 
    'predictedExpenses' => round($predictedExpenses, 2),
    'predictedBalance' => round($predictedBalance, 2),
    'recommendation' => $recommendation
];
    }
}