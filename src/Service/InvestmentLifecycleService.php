<?php

namespace App\Service;

use App\Entity\InvestmentManagement;

class InvestmentLifecycleService
{
    /**
     * Calcule et compile toutes les statistiques du cycle de vie de l'investissement.
     */
    public function getLifecycleData(InvestmentManagement $investment): array
    {
        $currentPhase = $this->getCurrentPhase($investment);
        $timeline = $this->reconstructTimeline($investment);
        
        return [
            'id' => $investment->getId(),
            'currentPhase' => $currentPhase,
            'durationInCurrentPhase' => $this->getDurationInCurrentPhase($currentPhase, $timeline),
            'progression' => $this->getProgression($investment),
            'insights' => $this->generateInsights($investment, $currentPhase),
            'timeline' => $timeline,
        ];
    }

    /**
     * Détermine la phase actuelle basée sur les indicateurs métier.
     */
    private function getCurrentPhase(InvestmentManagement $investment): string
    {
        // Phases: CREATED -> ACTIVE -> GROWING -> CLOSED
        if ($investment->getStatus() === 'CLOSED') {
            return 'CLOSED';
        }

        $ownership = (float) $investment->getOwnershipPercentage();
        $amount = (float) $investment->getAmountInvested();

        $now = new \DateTime();
        $startDate = $investment->getStartDate() ?? clone $now;
        
        $monthsElapsed = $startDate->diff($now)->m + ($startDate->diff($now)->y * 12);
        
        // Objectifs de croissance atteints ou forte implication
        if ($ownership >= 20 || $amount > 100000 || $monthsElapsed >= 6) {
            return 'GROWING';
        }

        // Investissement qui a démarré
        if ($monthsElapsed >= 1 || $amount > 0) {
            return 'ACTIVE';
        }

        // Vient d'être posté ou créé
        return 'CREATED';
    }

    /**
     * Reconstruit la timeline rétroactivement.
     */
    private function reconstructTimeline(InvestmentManagement $investment): array
    {
        $timeline = [];
        $creationDate = $investment->getCreatedAt() ?? $investment->getStartDate() ?? clone (new \DateTime());
        
        $timeline[] = [
            'phase' => 'CREATED',
            'date' => $creationDate->format('Y-m-d\TH:i:sP'),
            'description' => 'Initialisation du dossier et validation des fonds.'
        ];

        $currentPhase = $this->getCurrentPhase($investment);
        
        if (in_array($currentPhase, ['ACTIVE', 'GROWING', 'CLOSED'])) {
            $activeDate = clone $creationDate;
            $activeDate->modify('+2 days');
            
            // Si la date calculée est dans le futur, la plafonner à "maintenant"
            if ($activeDate > new \DateTime()) {
                $activeDate = new \DateTime();
            }

            $timeline[] = [
                'phase' => 'ACTIVE',
                'date' => $activeDate->format('Y-m-d\TH:i:sP'),
                'description' => 'Investissement validé et opérationnel.'
            ];
        }

        if (in_array($currentPhase, ['GROWING', 'CLOSED'])) {
            $growingDate = clone $creationDate;
            $growingDate->modify('+1 month');
            
            if ($growingDate > new \DateTime()) {
                $growingDate = new \DateTime();
            }

            $timeline[] = [
                'phase' => 'GROWING',
                'date' => $growingDate->format('Y-m-d\TH:i:sP'),
                'description' => 'Croissance constatée des engagements ou de la part détenue.'
            ];
        }

        if ($currentPhase === 'CLOSED') {
            $timeline[] = [
                'phase' => 'CLOSED',
                'date' => (new \DateTime())->format('Y-m-d\TH:i:sP'),
                'description' => 'Cycle de vie achevé (retrait, maturité ou plafond atteint).'
            ];
        }

        return $timeline;
    }

    /**
     * Calcule la durée passée dans la phase actuelle textuellement.
     */
    private function getDurationInCurrentPhase(string $currentPhase, array $timeline): string
    {
        $phaseDate = null;
        foreach ($timeline as $item) {
            if ($item['phase'] === $currentPhase) {
                $phaseDate = new \DateTime($item['date']);
                break;
            }
        }

        if (!$phaseDate) {
            return '0 jours';
        }

        $now = new \DateTime();
        $diff = $phaseDate->diff($now);
        
        if ($diff->y > 0) return $diff->y . ' an(s)';
        if ($diff->m > 0) return $diff->m . ' mois';
        if ($diff->d > 0) return $diff->d . ' jour(s)';
        if ($diff->h > 0) return $diff->h . ' heure(s)';
        
        return 'Moins d\'une heure';
    }

    /**
     * Calcul de progression absolue en pourcentage.
     */
    private function getProgression(InvestmentManagement $investment): int
    {
        $ownership = (float) $investment->getOwnershipPercentage();
        if ($ownership >= 100 || $investment->getStatus() === 'CLOSED') {
            return 100;
        }

        // Base la progression sur l'ownership (jusqu'à 99% max si non CLOSED)
        return (int) min(99, max(5, $ownership)); // Minimum 5% de progression quand amorcé
    }

    /**
     * Génère des messages "insights" métier dynamiques.
     */
    private function generateInsights(InvestmentManagement $investment, string $currentPhase): array
    {
        $insights = [];
        
        $ownership = (float) $investment->getOwnershipPercentage();
        $amount = (float) $investment->getAmountInvested();
        $status = $investment->getStatus();

        if ($status === 'CRITICAL') {
            $insights[] = [
                'type' => 'warning',
                'message' => 'L\'investissement requiert une action immédiate suite à une alerte.'
            ];
        }

        if ($currentPhase === 'CREATED') {
            $insights[] = [
                'type' => 'info',
                'message' => 'Activité initiale détectée. En attente de déploiement réel du capital.'
            ];
        } elseif ($currentPhase === 'ACTIVE') {
            $insights[] = [
                'type' => 'success',
                'message' => 'Flux nominaux conformes. Le projet évolue normalement.'
            ];
            if ($amount > 500000) {
                $insights[] = [
                    'type' => 'info',
                    'message' => 'Engagement fort : exposition mesurée à hauteur de plus de 500k.'
                ];
            }
        } elseif ($currentPhase === 'GROWING') {
            $insights[] = [
                'type' => 'success',
                'message' => 'Très bonne dynamique, investissement mâture.'
            ];
            if ($ownership > 50) {
                $insights[] = [
                    'type' => 'info',
                    'message' => 'Investisseur majoritaire détecté.'
                ];
            }
        } elseif ($currentPhase === 'CLOSED') {
            $insights[] = [
                'type' => 'secondary',
                'message' => 'Statistiques figées. Le cycle de cet investissement est verrouillé.'
            ];
        }

        // 💎 BONUS : Détection de stagnation
        $startDate = $investment->getStartDate();
        if ($startDate && $currentPhase === 'ACTIVE' && $ownership < 5) {
            $diff = $startDate->diff(new \DateTime());
            if ($diff->m >= 6 || $diff->y > 0) {
                $insights[] = [
                    'type' => 'warning',
                    'message' => 'STAGNATION : L\'investissement est dans la phase ACTIVE depuis ' . $diff->m . ' mois sans augmentation notable des parts (< 5%).'
                ];
            }
        }

        return $insights;
    }
}
