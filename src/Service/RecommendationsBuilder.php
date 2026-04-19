<?php

namespace App\Service;

use App\Entity\Investment;
use App\Repository\InvestmentRepository;

/**
 * Service de recommandation (Interne & Externe).
 */
final class RecommendationsBuilder
{
    public function __construct(
        private readonly InvestmentRepository $investmentRepository,
    ) {
    }

    /**
     * Récupère les meilleurs investissements internes.
     */
    public function getInternalRecommendations(int $limit = 6): array
    {
        $investments = $this->investmentRepository->findTopActiveByEstimatedValue($limit);
        return array_map(fn (Investment $i) => $this->serializeInvestment($i, 'internal'), $investments);
    }

    /**
     * Récupère les opportunités des partenaires externes.
     */
    public function getExternalRecommendations(int $limit = 6): array
    {
        return array_slice($this->getExternalStaticRows(), 0, $limit);
    }

    /**
     * Recherche un projet externe par ID.
     */
    public function findExternalById(int $id): ?array
    {
        foreach ($this->getExternalStaticRows() as $row) {
            if ($row['id'] === $id) {
                return $row;
            }
        }
        return null;
    }

    private function getExternalStaticRows(): array
    {
        return [
            [
                'id' => 9001, 
                'source' => 'Global Partner', 
                'name' => 'Fonds Vert Régional', 
                'category' => 'STARTUP', 
                'location' => 'Tunis, TN', 
                'estimated_value' => '1250000', 
                'risk_level' => 'LOW', 
                'status' => 'ACTIVE', 
                'published_by' => 'InvestHub Africa',
                'published_at' => '2024-03-15',
                'image_url' => 'https://images.unsplash.com/photo-1466611653911-95481536a6b2?q=80&w=800'
            ],
            [
                'id' => 9002, 
                'source' => 'EuroInvest', 
                'name' => 'Lotissement Indigo', 
                'category' => 'IMMOBILIER', 
                'location' => 'Sousse, TN', 
                'estimated_value' => '890000', 
                'risk_level' => 'MEDIUM', 
                'status' => 'ACTIVE', 
                'published_by' => 'PropTech Global',
                'published_at' => '2024-04-02',
                'image_url' => 'https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?q=80&w=800'
            ],
            [
                'id' => 9003, 
                'source' => 'Tourism Fund', 
                'name' => 'Boutique-Hôtel Azure', 
                'category' => 'HOTEL', 
                'location' => 'Hammamet, TN', 
                'estimated_value' => '2100000', 
                'risk_level' => 'MEDIUM', 
                'status' => 'ACTIVE', 
                'published_by' => 'Hospitality Group',
                'published_at' => '2024-01-20',
                'image_url' => 'https://images.unsplash.com/photo-1566073771259-6a8506099945?q=80&w=800'
            ],
            [
                'id' => 9004, 
                'source' => 'AgriNetwork', 
                'name' => 'AgriTech Valley', 
                'category' => 'AGRICULTURE', 
                'location' => 'Béja, TN', 
                'estimated_value' => '650000', 
                'risk_level' => 'HIGH', 
                'status' => 'ACTIVE', 
                'published_by' => 'BioFarm Systems',
                'published_at' => '2024-04-10',
                'image_url' => 'https://images.unsplash.com/photo-1625246333195-78d9c38ad449?q=80&w=800'
            ],
            [
                'id' => 9005, 
                'source' => 'TechNodes', 
                'name' => 'Tech Hub Gafsa', 
                'category' => 'STARTUP', 
                'location' => 'Gafsa, TN', 
                'estimated_value' => '450000', 
                'risk_level' => 'MEDIUM', 
                'status' => 'ACTIVE', 
                'published_by' => 'Digital Maghreb',
                'published_at' => '2024-03-28',
                'image_url' => 'https://images.unsplash.com/photo-1497366216548-37526070297c?q=80&w=800'
            ],
            [
                'id' => 9006, 
                'source' => 'LuxuryStates', 
                'name' => 'Résidence Carthage', 
                'category' => 'IMMOBILIER', 
                'location' => 'Carthage, TN', 
                'estimated_value' => '3200000', 
                'risk_level' => 'LOW', 
                'status' => 'ACTIVE', 
                'published_by' => 'Elite Real Estate',
                'published_at' => '2024-02-14',
                'image_url' => 'https://images.unsplash.com/photo-1613490493576-7fde63acd811?q=80&w=800'
            ],
        ];
    }

    private function serializeInvestment(Investment $i, string $source): array
    {
        return [
            'id' => $i->getId(),
            'source' => $source,
            'name' => $i->getName(),
            'category' => $i->getCategory(),
            'location' => $i->getLocation(),
            'estimated_value' => $i->getEstimatedValue(),
            'risk_level' => $i->getRiskLevel(),
            'image_url' => $i->getImageUrl(),
            'status' => $i->getStatus(),
        ];
    }
}
