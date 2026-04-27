<?php

namespace App\Service;

use App\Entity\User;
use App\Entity\Formation;
use App\Entity\TransactionWallet;
use App\Entity\TransactionBourse;
use Doctrine\ORM\EntityManagerInterface;

class SmartLearningService
{
    private $entityManager;

    public function __construct(EntityManagerInterface $entityManager)
    {
        $this->entityManager = $entityManager;
    }

    /**
     * Analyzes user behavior and suggests the most relevant formation.
     */
    /**
     * Analyzes user behavior and suggests relevant formations.
     * Returns an array of recommendations.
     */
    public function getRecommendations(User $user): array
    {
        $recommendations = [];
        $balance = $user->getBalance();
        
        // 1. Analyze Spending Behavior (Last 30 days)
        $oneMonthAgo = new \DateTime('-30 days');
        $transactions = $this->entityManager->getRepository(TransactionWallet::class)->createQueryBuilder('t')
            ->where('t.user = :uid')
            ->andWhere('t.dateTransaction >= :date')
            ->andWhere('t.status = :status')
            ->setParameter('uid', $user->getId())
            ->setParameter('date', $oneMonthAgo)
            ->setParameter('status', 'ACCEPTED')
            ->getQuery()
            ->getResult();

        $totalOutcome = 0;
        foreach ($transactions as $t) {
            if ($t->getType() === 'OUTCOME') {
                $totalOutcome += abs($t->getMontant());
            }
        }

        if ($totalOutcome > 1200) {
            $recommendations[] = $this->buildRecommendation(
                "📉 Optimisez votre budget",
                "Vos dépenses récentes (" . number_format($totalOutcome, 0) . " DT) sont élevées. Apprenez à dégager de l'épargne.",
                "Budget",
                "Gestion des risques"
            );
        }

        // 2. Analyze Trading Performance
        $trades = $this->entityManager->getRepository(TransactionBourse::class)->findBy(['user' => $user]);
        if (count($trades) > 0) {
            if ($balance < 300) {
                $recommendations[] = $this->buildRecommendation(
                    "🛡️ Stratégie de Survie",
                    "Le trading sans gestion des risques est un pari. Sécurisez votre capital restant.",
                    "Risque",
                    "Gestion des risques"
                );
            }
            
            if (count($trades) > 15) {
                $recommendations[] = $this->buildRecommendation(
                    "📊 Maîtrisez le Marché",
                    "Vous êtes très actif. L'analyse fondamentale vous donnera l'avantage sur les autres traders.",
                    "Analyse",
                    "Analyse fondamentale"
                );
            }
        }

        // 3. Analyze Investment & Tenders (Role Based)
        $investments = $this->entityManager->getRepository(\App\Entity\InvestmentManagement::class)->findBy(['user' => $user]);
        if (count($investments) === 0 && $balance > 1000) {
            $recommendations[] = $this->buildRecommendation(
                "🏢 Devenez Actionnaire",
                "Pourquoi se contenter du livret A ? Découvrez l'investissement direct dans des projets réels.",
                "Investissement",
                "Investissement"
            );
        }

        if (in_array('ROLE_ENTREPRISE', $user->getRoles())) {
            $recommendations[] = $this->buildRecommendation(
                "🤝 Développez votre réseau",
                "Optimisez vos coûts en lançant des appels d'offres stratégiques pour votre entreprise.",
                "Partenariat",
                "Investissement"
            );
        }

        // 4. Growth & Diversification (Only if not already too many recs)
        if (count($recommendations) < 2) {
            if ($balance > 2500) {
                $recommendations[] = $this->buildRecommendation(
                    "📈 Diversification",
                    "Ne mettez pas tous vos œufs dans le même panier. Apprenez à construire un portefeuille solide.",
                    "Portefeuille",
                    "Marchés financiers"
                );
            }

            if ($balance < 150 && count($recommendations) === 0) {
                $recommendations[] = $this->buildRecommendation(
                    "🌱 Fondations Financières",
                    "Apprenez les bases de l'éducation financière pour faire fructifier vos premiers deniers.",
                    "Éducation",
                    "Marchés financiers"
                );
            }
        }

        // Final fallback if empty
        if (empty($recommendations)) {
            $recommendations[] = $this->buildRecommendation(
                "🚀 Cap sur l'Indépendance",
                "Découvrez comment générer des revenus passifs grâce à des stratégies éprouvées.",
                "Revenus",
                "Marchés financiers"
            );
        }

        return $recommendations;
    }



    private function buildRecommendation(string $title, string $reason, string $keyword, string $fallbackCategory): array
    {
        // Try to find a formation matching the keyword
        $formation = $this->entityManager->getRepository(Formation::class)->createQueryBuilder('f')
            ->where('LOWER(f.titre) LIKE LOWER(:k) OR LOWER(f.description) LIKE LOWER(:k)')
            ->setParameter('k', '%' . $keyword . '%')
            ->setMaxResults(1)
            ->getQuery()
            ->getOneOrNullResult();

        if (!$formation) {
            // Fallback to category search in title/desc if not exact match found
            $formation = $this->entityManager->getRepository(Formation::class)->createQueryBuilder('f')
                ->where('LOWER(f.categorie) LIKE LOWER(:c)')
                ->setParameter('c', '%' . $fallbackCategory . '%')
                ->setMaxResults(1)
                ->getQuery()
                ->getOneOrNullResult();
        }

        return [
            'title' => $title,
            'reason' => $reason,
            'formation' => $formation,
            'category_slug' => $fallbackCategory,
            'icon' => $this->getIconForCategory($fallbackCategory)
        ];
    }

    private function getIconForCategory(string $cat): string
    {
        $cat = strtolower($cat);
        if (str_contains($cat, 'risqu')) return 'bi-shield-check';
        if (str_contains($cat, 'analys')) return 'bi-graph-up-arrow';
        if (str_contains($cat, 'marché')) return 'bi-bank';
        if (str_contains($cat, 'budget')) return 'bi-piggy-bank';
        
        return 'bi-book';
    }
}

