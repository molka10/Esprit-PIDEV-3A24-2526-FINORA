<?php

namespace App\Repository;

use App\Entity\InvestmentManagement;
use App\Entity\Investment;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<InvestmentManagement>
 */
class InvestmentManagementRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, InvestmentManagement::class);
    }

    /**
     * 🔥 LISTE TRIÉE (récent en premier)
     */
    public function findAllSorted(): array
    {
        return $this->createQueryBuilder('i')
            ->orderBy('i.managementId', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * 🔍 SEARCH + FILTER
     */
    public function search(?string $search = null, ?string $status = null): array
    {
        $qb = $this->createQueryBuilder('i');

        if ($search) {
            $qb->andWhere('LOWER(i.investmentType) LIKE :search')
               ->setParameter('search', '%' . strtolower($search) . '%');
        }

        if ($status) {
            $qb->andWhere('i.status = :status')
               ->setParameter('status', $status);
        }

        return $qb
            ->orderBy('i.managementId', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * 🔥 PAR INVESTMENT (important pour show)
     */
    public function findByInvestment(Investment $investment): array
    {
        return $this->createQueryBuilder('i')
            ->andWhere('i.investment = :inv')
            ->setParameter('inv', $investment)
            ->orderBy('i.startDate', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * 🔥 COUNT PAR STATUS (dashboard)
     */
    public function countByStatus(string $status): int
    {
        return (int) $this->createQueryBuilder('i')
            ->select('COUNT(i.managementId)')
            ->andWhere('i.status = :status')
            ->setParameter('status', $status)
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * 🔥 TOTAL INVESTED (dashboard)
     */
    public function getTotalInvested(): float
    {
        return (float) $this->createQueryBuilder('i')
            ->select('SUM(i.amountInvested)')
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * 🔥 DERNIERS INVESTISSEMENTS (home)
     */
    public function findRecent(int $limit = 5): array
    {
        return $this->createQueryBuilder('i')
            ->orderBy('i.startDate', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }
}