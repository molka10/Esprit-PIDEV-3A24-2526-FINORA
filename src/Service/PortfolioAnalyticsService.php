<?php

namespace App\Service;

use App\Entity\InvestmentManagement;

class PortfolioAnalyticsService
{
    /**
     * Calculates the total value of the active portfolio.
     * 
     * @param InvestmentManagement[] $activeManagements
     * @return float
     */
    public function calculateTotalValue(array $activeManagements): float
    {
        $total = 0.0;
        foreach ($activeManagements as $mng) {
            $total += (float) $mng->getAmountInvested();
        }
        return $total;
    }

    /**
     * Returns the distribution of assets by category.
     * 
     * @param InvestmentManagement[] $activeManagements
     * @return array
     */
    public function getCategoryDistribution(array $activeManagements): array
    {
        $distribution = [];
        foreach ($activeManagements as $mng) {
            $inv = $mng->getInvestment();
            if ($inv) {
                $cat = ltrim(trim($inv->getCategory() ?? 'Autre'));
                $distribution[$cat] = ($distribution[$cat] ?? 0) + 1;
            }
        }
        return [
            'labels' => array_keys($distribution),
            'data'   => array_values($distribution),
        ];
    }

    /**
     * Returns the risk exposure levels.
     * 
     * @param InvestmentManagement[] $activeManagements
     * @return array
     */
    public function getRiskExposure(array $activeManagements): array
    {
        $risks = ['LOW' => 0, 'MEDIUM' => 0, 'HIGH' => 0];
        foreach ($activeManagements as $mng) {
            $inv = $mng->getInvestment();
            if ($inv) {
                $level = $inv->getRiskLevel() ?? 'MEDIUM';
                if (isset($risks[$level])) {
                    $risks[$level]++;
                }
            }
        }
        return array_values($risks);
    }

    /**
     * Returns key performance indicators and stats for the dashboard.
     * 
     * @param array $allInvestments
     * @param array $allManagements
     * @return array
     */
    public function getDashboardStats(array $allInvestments, array $allManagements): array
    {
        $activeManagements = array_filter($allManagements, fn($m) => $m->getStatus() === 'ACTIVE');
        $closedManagements = array_filter($allManagements, fn($m) => $m->getStatus() === 'CLOSED');

        $totalValue = $this->calculateTotalValue($activeManagements);
        $catDist = $this->getCategoryDistribution($activeManagements);
        $riskDist = $this->getRiskExposure($activeManagements);

        return [
            'totalInvestments'  => count($allInvestments),
            'activeManagement'  => count($activeManagements),
            'closedManagement'  => count($closedManagements),
            'totalValue'        => $totalValue,
            'chartCategories'   => json_encode($catDist['labels']),
            'chartCategoryData' => json_encode($catDist['data']),
            'chartRisks'        => json_encode($riskDist),
        ];
    }
}
