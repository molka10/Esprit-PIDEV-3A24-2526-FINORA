<?php

namespace App\Service;

use App\Entity\Investment;
use App\Repository\InvestmentRepository;

/**
 * Données de recommandation partagées entre l’API JSON et la page d’accueil.
 */
final class RecommendationsBuilder
{
    public function __construct(
        private readonly InvestmentRepository $investmentRepository,
    ) {
    }

    /** @return array<string, mixed> */
    public function getInternalApiData(int $limit = 6): array
    {
        $byValue = $this->investmentRepository->findTopActiveByEstimatedValue($limit);
        $byRisk = $this->investmentRepository->findTopActiveByLowestRisk($limit);

        $topValue = array_map(fn (Investment $i) => $this->serializeInvestment($i, 'internal_value'), $byValue);
        $topRisk = array_map(fn (Investment $i) => $this->serializeInvestment($i, 'internal_risk'), $byRisk);

        $mergedInternal = $this->mergeTopByEstimatedValue(
            $topValue,
            $topRisk,
            $limit,
        );

        return [
            'top_by_estimated_value' => $topValue,
            'top_by_lowest_risk' => $topRisk,
            'merged_top' => $mergedInternal,
            'by_highest_estimated_value' => $topValue,
            'by_lowest_risk_level' => $topRisk,
            'meta' => [
                'scope' => 'ACTIVE investments only',
                'limit_per_ranking' => $limit,
            ],
        ];
    }

    /** @return array<string, mixed> */
    public function getExternalApiData(int $limit = 6): array
    {
        $internalValue = $this->investmentRepository->findTopActiveByEstimatedValue($limit);
        $internalRisk = $this->investmentRepository->findTopActiveByLowestRisk($limit);

        $externalStatic = $this->getExternalStaticRows();
        // Optionnel : Couper seulement une partie si on veut imiter une API ? On simule tout le tableau.

        $internalRows = array_merge(
            array_map(fn (Investment $i) => $this->serializeInvestment($i, 'internal_value'), $internalValue),
            array_map(fn (Investment $i) => $this->serializeInvestment($i, 'internal_risk'), $internalRisk),
        );

        $mergedTop = $this->mergeInternalExternalBalanced($internalRows, $externalStatic, $limit);

        return [
            'internal' => [
                'top_by_estimated_value' => array_map(fn (Investment $i) => $this->serializeInvestment($i, 'internal_value'), $internalValue),
                'top_by_lowest_risk' => array_map(fn (Investment $i) => $this->serializeInvestment($i, 'internal_risk'), $internalRisk),
            ],
            'external' => array_slice($externalStatic, 0, $limit),
            'merged_top' => $mergedTop,
            'meta' => [
                'internal_value_count' => \count($internalValue),
                'internal_risk_count' => \count($internalRisk),
                'external_count' => min(\count($externalStatic), $limit),
                'note' => 'merged_top: quota 2 internes + 1 externe (si les deux sources existent), puis tri par valeur.',
            ],
        ];
    }

    public function getMergedTopForHome(): array
    {
        $data = $this->getExternalApiData(3);

        return $data['merged_top'];
    }

    /**
     * @return list<array<string, mixed>>
     */
    public function getTop3Interne(): array
    {
        $internalValue = $this->investmentRepository->findTopActiveByEstimatedValue(3);
        $topValue = array_map(fn (Investment $i) => $this->serializeInvestment($i, 'internal_value'), $internalValue);
        
        return $this->dedupeByIdSortByValueDesc($topValue);
    }

    /**
     * @return list<array<string, mixed>>
     */
    public function getAllExterne(): array
    {
        $externalStatic = $this->getExternalStaticRows();
        return $this->dedupeByIdSortByValueDesc($externalStatic);
    }

    /**
     * @return array<string, mixed>|null
     */
    public function findExternalById(int $id): ?array
    {
        $rows = $this->getExternalStaticRows();
        foreach ($rows as $row) {
            if ($row['id'] === $id) {
                return $row;
            }
        }
        return null;
    }

    /**
     * @return list<array<string, mixed>>
     */
    private function getExternalStaticRows(): array
    {
        return [
            [
                'id' => 9001,
                'source' => 'external_partner',
                'name' => 'Fonds vert régional (simulé)',
                'category' => 'STARTUP',
                'location' => 'Hub externe',
                'estimated_value' => '1250000.00',
                'risk_level' => 'LOW',
                'image_filename' => null,
                'status' => 'ACTIVE',
            ],
            [
                'id' => 9002,
                'source' => 'external_partner',
                'name' => 'Lotissement pilote (simulé)',
                'category' => 'TERRAIN',
                'location' => 'Périphérie',
                'estimated_value' => '890000.50',
                'risk_level' => 'MEDIUM',
                'image_filename' => null,
                'status' => 'ACTIVE',
            ],
            [
                'id' => 9003,
                'source' => 'external_partner',
                'name' => 'Boutique-hôtel (simulé)',
                'category' => 'HOTEL',
                'location' => 'Centre-ville',
                'estimated_value' => '2100000.00',
                'risk_level' => 'MEDIUM',
                'image_filename' => null,
                'status' => 'ACTIVE',
            ],
            [
                'id' => 9004,
                'source' => 'external_partner',
                'name' => 'AgriTech Valley (simulé)',
                'category' => 'STARTUP',
                'location' => 'Zone Rurale',
                'estimated_value' => '650000.00',
                'risk_level' => 'HIGH',
                'image_filename' => null,
                'status' => 'ACTIVE',
            ],
            [
                'id' => 9005,
                'source' => 'external_partner',
                'name' => 'Complexe Résidentiel (simulé)',
                'category' => 'IMMOBILIER',
                'location' => 'Quartier Nouveau',
                'estimated_value' => '4500000.00',
                'risk_level' => 'LOW',
                'image_filename' => null,
                'status' => 'ACTIVE',
            ],
            [
                'id' => 9006,
                'source' => 'external_partner',
                'name' => 'Ferme Solaire Intelligente (simulé)',
                'category' => 'STARTUP',
                'location' => 'Désert',
                'estimated_value' => '1000000.00',
                'risk_level' => 'MEDIUM',
                'image_filename' => null,
                'status' => 'ACTIVE',
            ],
        ];
    }

    /**
     * @param array<int, array<string, mixed>> $a
     * @param array<int, array<string, mixed>> $b
     *
     * @return list<array<string, mixed>>
     */
    private function mergeTopByEstimatedValue(array $a, array $b, int $limit): array
    {
        $byId = [];
        foreach (array_merge($a, $b) as $row) {
            $id = $row['id'] ?? 0;
            $ev = (float) ($row['estimated_value'] ?? 0);
            if (!isset($byId[$id]) || (float) ($byId[$id]['estimated_value'] ?? 0) < $ev) {
                $byId[$id] = $row;
            }
        }
        $list = array_values($byId);
        usort($list, static function (array $x, array $y): int {
            return ((float) ($y['estimated_value'] ?? 0)) <=> ((float) ($x['estimated_value'] ?? 0));
        });

        return \array_slice($list, 0, $limit);
    }

    /**
     * @param array<int, array<string, mixed>> $internalRows
     * @param array<int, array<string, mixed>> $externalRows
     *
     * @return list<array<string, mixed>>
     */
    private function mergeInternalExternalBalanced(array $internalRows, array $externalRows, int $limit): array
    {
        $internalSorted = $this->dedupeByIdSortByValueDesc($internalRows);
        $externalSorted = $this->dedupeByIdSortByValueDesc($externalRows);

        if ($internalSorted === []) {
            return \array_slice($externalSorted, 0, $limit);
        }
        if ($externalSorted === []) {
            return \array_slice($internalSorted, 0, $limit);
        }

        $picked = [];
        foreach (\array_slice($internalSorted, 0, 2) as $row) {
            $picked[] = $row;
        }
        $picked[] = $externalSorted[0];

        $picked = $this->dedupeByIdKeepHighestValue($picked);
        $seenIds = [];
        foreach ($picked as $row) {
            $seenIds[$row['id'] ?? 0] = true;
        }

        $pool = array_merge($internalSorted, $externalSorted);
        usort($pool, static function (array $x, array $y): int {
            return ((float) ($y['estimated_value'] ?? 0)) <=> ((float) ($x['estimated_value'] ?? 0));
        });
        foreach ($pool as $row) {
            if (\count($picked) >= $limit) {
                break;
            }
            $id = $row['id'] ?? 0;
            if (!isset($seenIds[$id])) {
                $picked[] = $row;
                $seenIds[$id] = true;
            }
        }

        usort($picked, static function (array $x, array $y): int {
            return ((float) ($y['estimated_value'] ?? 0)) <=> ((float) ($x['estimated_value'] ?? 0));
        });

        return \array_slice($picked, 0, $limit);
    }

    /**
     * @param array<int, array<string, mixed>> $rows
     *
     * @return list<array<string, mixed>>
     */
    private function dedupeByIdSortByValueDesc(array $rows): array
    {
        $list = $this->dedupeByIdKeepHighestValue($rows);
        usort($list, static function (array $x, array $y): int {
            return ((float) ($y['estimated_value'] ?? 0)) <=> ((float) ($x['estimated_value'] ?? 0));
        });

        return $list;
    }

    /**
     * @param array<int, array<string, mixed>> $rows
     *
     * @return list<array<string, mixed>>
     */
    private function dedupeByIdKeepHighestValue(array $rows): array
    {
        $byId = [];
        foreach ($rows as $row) {
            $id = $row['id'] ?? 0;
            $ev = (float) ($row['estimated_value'] ?? 0);
            if (!isset($byId[$id]) || (float) ($byId[$id]['estimated_value'] ?? 0) < $ev) {
                $byId[$id] = $row;
            }
        }

        return array_values($byId);
    }

    /** @return array<string, mixed> */
    private function serializeInvestment(Investment $i, string $source): array
    {
        return [
            'id' => $i->getInvestmentId(),
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
