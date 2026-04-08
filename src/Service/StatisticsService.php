<?php

namespace App\Service;

use App\Repository\BourseRepository;
use App\Repository\ActionRepository;
use App\Repository\TransactionBourseRepository;

/**
 * 📊 StatisticsService - Génération des statistiques pour le dashboard
 */
class StatisticsService
{
    public function __construct(
        private BourseRepository $bourseRepo,
        private ActionRepository $actionRepo,
        private TransactionbourseRepository $transactionRepo
    ) {}

    /**
     * Récupère toutes les statistiques globales
     */
    public function getGlobalStatistics(): array
    {
        return [
            'bourses' => $this->getBourseStatistics(),
            'actions' => $this->getActionStatistics(),
            'transactions' => $this->getTransactionStatistics(),
        ];
    }

    /**
     * Statistiques des bourses
     */
    public function getBourseStatistics(): array
    {
        return $this->bourseRepo->getStatistics();
    }

    /**
     * Statistiques des actions
     */
    public function getActionStatistics(): array
    {
        return $this->actionRepo->getStatistics();
    }

    /**
     * Statistiques des transactions
     */
    public function getTransactionStatistics(): array
    {
        return $this->transactionRepo->getStatistics();
    }

    /**
     * Génère les données pour le graphique d'évolution
     */
    public function getEvolutionChartData(int $days = 30): array
    {
        return $this->transactionRepo->getChartData($days);
    }

    /**
     * Récupère les actions les plus tradées
     */
    public function getTopActions(int $limit = 5): array
    {
        $qb = $this->transactionRepo->createQueryBuilder('t');
        
        $results = $qb
            ->select('a.symbole, a.nomEntreprise, COUNT(t.id) as nb_transactions, SUM(t.montantTotal) as volume')
            ->join('t.action', 'a')
            ->groupBy('a.id')
            ->orderBy('nb_transactions', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();

        return $results;
    }

    /**
     * Calcule le gain/perte global
     */
    public function getGainPerte(): array
    {
        $totalAchats = $this->transactionRepo->createQueryBuilder('t')
            ->select('SUM(t.montantTotal)')
            ->where('t.typeTransaction = :type')
            ->setParameter('type', 'ACHAT')
            ->getQuery()
            ->getSingleScalarResult() ?? 0;

        $totalVentes = $this->transactionRepo->createQueryBuilder('t')
            ->select('SUM(t.montantTotal)')
            ->where('t.typeTransaction = :type')
            ->setParameter('type', 'VENTE')
            ->getQuery()
            ->getSingleScalarResult() ?? 0;

        $gainPerte = $totalVentes - $totalAchats;

        return [
            'total_achats' => round($totalAchats, 2),
            'total_ventes' => round($totalVentes, 2),
            'gain_perte' => round($gainPerte, 2),
            'pourcentage' => $totalAchats > 0 ? round(($gainPerte / $totalAchats) * 100, 2) : 0
        ];
    }

    /**
     * Génère les données pour graphique en secteurs (répartition)
     */
    public function getRepartitionBySecteur(): array
    {
        $qb = $this->actionRepo->createQueryBuilder('a');
        
        $results = $qb
            ->select('a.secteur, COUNT(a.id) as count')
            ->groupBy('a.secteur')
            ->getQuery()
            ->getResult();

        $labels = [];
        $data = [];

        foreach ($results as $result) {
            $labels[] = $result['secteur'];
            $data[] = (int) $result['count'];
        }

        return [
            'labels' => $labels,
            'data' => $data
        ];
    }

    /**
     * Génère un résumé pour le dashboard
     */
    public function getDashboardSummary(): array
    {
        $bourseStats = $this->getBourseStatistics();
        $actionStats = $this->getActionStatistics();
        $transactionStats = $this->getTransactionStatistics();
        $gainPerte = $this->getGainPerte();

        return [
            'cards' => [
                'bourses' => [
                    'total' => $bourseStats['total'],
                    'actives' => $bourseStats['actives'],
                    'label' => 'Bourses',
                    'icon' => 'building',
                    'color' => 'primary'
                ],
                'actions' => [
                    'total' => $actionStats['total'],
                    'disponibles' => $actionStats['disponibles'],
                    'label' => 'Actions',
                    'icon' => 'chart-line',
                    'color' => 'success'
                ],
                'transactions' => [
                    'total' => $transactionStats['total'],
                    'volume' => $transactionStats['volume_total'],
                    'label' => 'Transactions',
                    'icon' => 'exchange-alt',
                    'color' => 'info'
                ],
                'gain_perte' => [
                    'montant' => $gainPerte['gain_perte'],
                    'pourcentage' => $gainPerte['pourcentage'],
                    'label' => 'Gain/Perte',
                    'icon' => 'wallet',
                    'color' => $gainPerte['gain_perte'] >= 0 ? 'success' : 'danger'
                ]
            ]
        ];
    }
}