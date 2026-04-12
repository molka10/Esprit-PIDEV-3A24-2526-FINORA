<?php

namespace App\Repository;

use App\Entity\Investment;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Investment>
 */
class InvestmentRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Investment::class);
    }

    /**
     * 🔥 Top actifs par valeur (HOME + API)
     */
    public function findTopActiveByEstimatedValue(int $limit = 3): array
    {
        return $this->createQueryBuilder('i')
            ->andWhere('i.status = :active')
            ->setParameter('active', 'ACTIVE')
            ->orderBy('i.estimatedValue', 'DESC')
            ->addOrderBy('i.investmentId', 'ASC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    /**
     * 🔥 Top actifs avec risque faible (SMART RECOMMENDATION)
     */
    public function findTopActiveByLowestRisk(int $limit = 3): array
    {
        return $this->createQueryBuilder('i')
            ->andWhere('i.status = :active')
            ->setParameter('active', 'ACTIVE')
            ->orderBy(
                "CASE 
                    WHEN i.riskLevel = 'LOW' THEN 1 
                    WHEN i.riskLevel = 'MEDIUM' THEN 2 
                    WHEN i.riskLevel = 'HIGH' THEN 3 
                    ELSE 4 
                END",
                'ASC'
            )
            ->addOrderBy('i.estimatedValue', 'DESC')
            ->addOrderBy('i.investmentId', 'ASC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    /**
     * 🔥 Portfolio avec filtre catégorie
     */
    public function findForPortfolio(?string $category = null): array
    {
        $qb = $this->createQueryBuilder('i')
            ->orderBy('i.name', 'ASC');

        if ($category !== null && $category !== '') {
            $qb->andWhere('i.category = :cat')
               ->setParameter('cat', $category);
        }

        return $qb->getQuery()->getResult();
    }

    /**
     * 🔥 Recherche et tri global
     */
    public function searchAndFilter(?string $search, ?string $category, ?string $risk, ?string $sort): array
    {
        $qb = $this->createQueryBuilder('i');

        if ($search) {
            $qb->andWhere('LOWER(i.name) LIKE :search OR LOWER(i.location) LIKE :search OR LOWER(i.description) LIKE :search')
               ->setParameter('search', '%' . strtolower($search) . '%');
        }

        if ($category) {
            $qb->andWhere('i.category = :category')
               ->setParameter('category', $category);
        }

        if ($risk) {
            $qb->andWhere('i.riskLevel = :risk')
               ->setParameter('risk', $risk);
        }

        if ($sort === 'asc') {
            $qb->orderBy('i.estimatedValue', 'ASC');
        } elseif ($sort === 'desc') {
            $qb->orderBy('i.estimatedValue', 'DESC');
        } elseif ($sort === 'new') {
            $qb->orderBy('i.createdAt', 'DESC');
        } else {
            $qb->orderBy('i.investmentId', 'DESC');
        }

        return $qb->getQuery()->getResult();
    }

    /**
     * 🔥 Stats dashboard
     */
    public function countByStatus(string $status): int
    {
        return (int) $this->createQueryBuilder('i')
            ->select('COUNT(i.investmentId)')
            ->andWhere('i.status = :status')
            ->setParameter('status', $status)
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * 🔥 Somme totale des investissements
     */
    public function getTotalEstimatedValue(): float
    {
        return (float) $this->createQueryBuilder('i')
            ->select('SUM(i.estimatedValue)')
            ->getQuery()
            ->getSingleScalarResult();
    }

    // ================= LEGACY =================

    /** @deprecated */
    public function findTopByEstimatedValue(int $limit = 3): array
    {
        return $this->findTopActiveByEstimatedValue($limit);
    }

    /** @deprecated */
    public function findTopByLowestRisk(int $limit = 3): array
    {
        return $this->findTopActiveByLowestRisk($limit);
    }
}