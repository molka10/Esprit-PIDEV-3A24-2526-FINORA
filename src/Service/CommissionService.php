<?php

namespace App\Service;

/**
 * 💰 CommissionService - Calcul des commissions sur les transactions
 * 
 * Logique métier : Commission = 1% du montant de la transaction
 */
class CommissionService
{
    private const TAUX_COMMISSION = 0.01; // 1%
    private const COMMISSION_MIN = 5.0;   // Commission minimum 5 TND
    private const COMMISSION_MAX = 100.0; // Commission maximum 100 TND

    /**
     * Calcule la commission d'une transaction
     * 
     * @param float $montantTransaction Montant total de la transaction
     * @return float Commission calculée
     */
    public function calculerCommission(float $montantTransaction): float
    {
        // Calcul de base : 1% du montant
        $commission = $montantTransaction * self::TAUX_COMMISSION;

        // Appliquer minimum
        if ($commission < self::COMMISSION_MIN) {
            $commission = self::COMMISSION_MIN;
        }

        // Appliquer maximum
        if ($commission > self::COMMISSION_MAX) {
            $commission = self::COMMISSION_MAX;
        }

        return round($commission, 2);
    }

    /**
     * Calcule la commission selon le symbole de l'action
     * (différents taux selon les actions)
     * 
     * @param string $symbole Symbole de l'action
     * @param float $prixUnitaire Prix unitaire
     * @param int $quantite Quantité
     * @return float Commission calculée
     */
    public function calculerCommissionParAction(
        string $symbole, 
        float $prixUnitaire, 
        int $quantite
    ): float {
        $montantTotal = $prixUnitaire * $quantite;

        // Taux spéciaux pour certaines actions
        $tauxSpeciaux = [
            'AAPL' => 0.005, // 0.5% pour Apple
            'GOOGL' => 0.005, // 0.5% pour Google
            'TSLA' => 0.015,  // 1.5% pour Tesla (plus volatile)
        ];

        $taux = $tauxSpeciaux[$symbole] ?? self::TAUX_COMMISSION;
        $commission = $montantTotal * $taux;

        // Appliquer min/max
        $commission = max($commission, self::COMMISSION_MIN);
        $commission = min($commission, self::COMMISSION_MAX);

        return round($commission, 2);
    }

    /**
     * Calcule le montant total avec commission
     * 
     * @param float $prixUnitaire Prix unitaire
     * @param int $quantite Quantité
     * @return array ['montant' => float, 'commission' => float, 'total' => float]
     */
    public function calculerMontantTotal(float $prixUnitaire, int $quantite): array
    {
        $montant = $prixUnitaire * $quantite;
        $commission = $this->calculerCommission($montant);
        $total = $montant + $commission;

        return [
            'montant' => round($montant, 2),
            'commission' => round($commission, 2),
            'total' => round($total, 2)
        ];
    }

    /**
     * Récupère le taux de commission
     */
    public function getTauxCommission(): float
    {
        return self::TAUX_COMMISSION;
    }

    /**
     * Récupère la commission minimum
     */
    public function getCommissionMin(): float
    {
        return self::COMMISSION_MIN;
    }

    /**
     * Récupère la commission maximum
     */
    public function getCommissionMax(): float
    {
        return self::COMMISSION_MAX;
    }
}