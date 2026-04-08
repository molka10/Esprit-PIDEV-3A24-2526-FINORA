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
    public function getChartData(int $days = 30): array
    {
        $startDate = new \DateTime("-{$days} days");

        $results = $this->createQueryBuilder('t')
            ->select('t.dateTransaction as date, SUM(t.montantTotal) as total')
            ->where('t.dateTransaction >= :startDate')
            ->andWhere('t.dateTransaction IS NOT NULL')
            ->setParameter('startDate', $startDate)
            ->orderBy('t.dateTransaction', 'ASC')
            ->getQuery()
            ->getResult();

        $groupedData = [];

        foreach ($results as $result) {

            // 🔥 CORRECTION PRINCIPALE
            if (!($result['date'] instanceof \DateTimeInterface)) {
                continue;
            }

            $date = $result['date']->format('Y-m-d');

            if (!isset($groupedData[$date])) {
                $groupedData[$date] = 0;
            }

            $groupedData[$date] += (float) $result['total'];
        }

        $labels = [];
        $data = [];

        foreach ($groupedData as $date => $total) {
            $labels[] = (new \DateTime($date))->format('d/m');
            $data[] = $total;
        }

        return [
            'labels' => $labels,
            'data' => $data
        ];
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
    // 🔹 SAVE
    // =============================
    public function save(TransactionBourse $transaction, bool $flush = true): void
    {
        $this->getEntityManager()->persist($transaction);

        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }

    // =============================
    // 🔹 DELETE
    // =============================
    public function remove(TransactionBourse $transaction, bool $flush = true): void
    {
        $this->getEntityManager()->remove($transaction);

        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }
}