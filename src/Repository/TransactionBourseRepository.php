<?php

namespace App\Repository;

use App\Entity\TransactionBourse;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class TransactionBourseRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, TransactionBourse::class);
    }

    // =============================
    // 🔹 TRANSACTIONS PAR TYPE
    // =============================
    public function findByType(string $type): array
    {
        return $this->createQueryBuilder('t')
            ->where('t.typeTransaction = :type')
            ->setParameter('type', $type)
            ->orderBy('t.dateTransaction', 'DESC')
            ->getQuery()
            ->getResult();
    }

    // =============================
    // 🔹 TRANSACTIONS RÉCENTES
    // =============================
    public function findRecent(int $limit = 10): array
    {
        return $this->createQueryBuilder('t')
            ->orderBy('t.dateTransaction', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    // =============================
    // 🔹 STATISTIQUES
    // =============================
    public function getStatistics(): array
    {
        $total = $this->count([]);

        $achats = $this->createQueryBuilder('t')
            ->select('COUNT(t.id)')
            ->where('t.typeTransaction = :type')
            ->setParameter('type', 'ACHAT')
            ->getQuery()
            ->getSingleScalarResult();

        $ventes = $this->createQueryBuilder('t')
            ->select('COUNT(t.id)')
            ->where('t.typeTransaction = :type')
            ->setParameter('type', 'VENTE')
            ->getQuery()
            ->getSingleScalarResult();

        $volumeTotal = $this->createQueryBuilder('t')
            ->select('SUM(t.montantTotal)')
            ->getQuery()
            ->getSingleScalarResult();

        return [
            'total' => $total,
            'achats' => $achats ?? 0,
            'ventes' => $ventes ?? 0,
            'volume_total' => $volumeTotal ?? 0
        ];
    }

    // =============================
    // 🔥 CHART DATA (CORRIGÉ)
    // =============================
    public function getChartData(int $days = 365): array
    {
        $results = $this->createQueryBuilder('t')
            ->select('t.dateTransaction as date, t.montantTotal as total')
            ->where('t.dateTransaction IS NOT NULL')
            ->orderBy('t.dateTransaction', 'ASC')
            ->getQuery()
            ->getResult();

        $groupedData = [];

        foreach ($results as $result) {
            $date = $result['date'];
            
            if ($date instanceof \DateTimeInterface) {
                $day = $date->format('Y-m-d');
            } else {
                $day = substr((string)$date, 0, 10);
            }
            
            if (!$day) continue;

            if (!isset($groupedData[$day])) {
                $groupedData[$day] = 0;
            }
            $groupedData[$day] += (float) $result['total'];
        }

        $labels = [];
        $data = [];

        foreach ($groupedData as $day => $total) {
            $labels[] = (new \DateTime($day))->format('d/m');
            $data[] = $total;
        }

        return [
            'labels' => $labels,
            'data' => $data
        ];
    }

    /**
     * 🔹 PAGINATION MANUELLE
     */
    public function findPaginated(int $page = 1, int $limit = 10, ?string $type = null): array
    {
        $offset = ($page - 1) * $limit;
        $qb = $this->createQueryBuilder('t');

        if ($type) {
            $qb->andWhere('t.typeTransaction = :type')
               ->setParameter('type', $type);
        }

        $qb->orderBy('t.dateTransaction', 'DESC')
           ->setFirstResult($offset)
           ->setMaxResults($limit);

        $totalQuery = $this->createQueryBuilder('t')
            ->select('COUNT(t.id)');
        
        if ($type) {
            $totalQuery->andWhere('t.typeTransaction = :type')
                       ->setParameter('type', $type);
        }
        
        $total = $totalQuery->getQuery()->getSingleScalarResult();

        return [
            'items' => $qb->getQuery()->getResult(),
            'total' => $total,
            'page' => $page,
            'limit' => $limit,
            'pages' => ceil($total / $limit)
        ];
    }

    // =============================
    // 🔹 USER-SPECIFIC METHODS (NEW/FIX)
    // =============================

    public function getTotalAchatsByUser(int $userId): float
    {
        return (float) $this->createQueryBuilder('t')
            ->select('SUM(t.montantTotal)')
            ->where('t.user = :userId')
            ->andWhere('t.typeTransaction = :type')
            ->setParameter('userId', $userId)
            ->setParameter('type', 'ACHAT')
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function getTotalVentesByUser(int $userId): float
    {
        return (float) $this->createQueryBuilder('t')
            ->select('SUM(t.montantTotal)')
            ->where('t.user = :userId')
            ->andWhere('t.typeTransaction = :type')
            ->setParameter('userId', $userId)
            ->setParameter('type', 'VENTE')
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function getTotalCommissionsByUser(int $userId): float
    {
        return (float) $this->createQueryBuilder('t')
            ->select('SUM(t.commission)')
            ->where('t.user = :userId')
            ->setParameter('userId', $userId)
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * 🛡️ Calculate net stock owned by a user for a specific action
     */
    public function getUserStockForAction(int $userId, int $actionId): int
    {
        $achats = (int) $this->createQueryBuilder('t')
            ->select('SUM(t.quantite)')
            ->where('t.user = :userId')
            ->andWhere('t.action = :actionId')
            ->andWhere('t.typeTransaction = :type')
            ->setParameter('userId', $userId)
            ->setParameter('actionId', $actionId)
            ->setParameter('type', 'ACHAT')
            ->getQuery()
            ->getSingleScalarResult();

        $ventes = (int) $this->createQueryBuilder('t')
            ->select('SUM(t.quantite)')
            ->where('t.user = :userId')
            ->andWhere('t.action = :actionId')
            ->andWhere('t.typeTransaction = :type')
            ->setParameter('userId', $userId)
            ->setParameter('actionId', $actionId)
            ->setParameter('type', 'VENTE')
            ->getQuery()
            ->getSingleScalarResult();

        return $achats - $ventes;
    }

    // =============================
    // 🔹 PAR ACTION
    // =============================
    public function findByAction(int $actionId): array
    {
        return $this->createQueryBuilder('t')
            ->where('t.action = :actionId')
            ->setParameter('actionId', $actionId)
            ->orderBy('t.dateTransaction', 'DESC')
            ->getQuery()
            ->getResult();
    }

    // =============================
    // 🔹 SAVE & DELETE
    // =============================
    public function save(TransactionBourse $transaction, bool $flush = true): void
    {
        $this->getEntityManager()->persist($transaction);
        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }

    public function remove(TransactionBourse $transaction, bool $flush = true): void
    {
        $this->getEntityManager()->remove($transaction);
        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }
}