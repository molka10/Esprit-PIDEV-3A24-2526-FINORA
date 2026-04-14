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
    public function searchAndFilter(?string $search, string|array|null $category, ?string $risk, ?string $sort, ?string $price = null): array
    {
        return $this->searchAndFilterQuery($search, $category, $risk, $sort, $price)->getQuery()->getResult();
    }

    /**
     * 🔥 Recherche et tri global (Retourne le QueryBuilder pour la pagination)
     */
    public function searchAndFilterQuery(?string $search, string|array|null $category, ?string $risk, ?string $sort, ?string $price = null): \Doctrine\ORM\QueryBuilder
    {
        $qb = $this->createQueryBuilder('i');

        if ($search) {
            $qb->andWhere('LOWER(i.name) LIKE :search OR LOWER(i.location) LIKE :search OR LOWER(i.description) LIKE :search')
               ->setParameter('search', '%' . strtolower($search) . '%');
        }

        if (!empty($category)) {
            if (is_array($category)) {
                $qb->andWhere('i.category IN (:category)')
                   ->setParameter('category', $category);
            } else {
                $qb->andWhere('i.category = :category')
                   ->setParameter('category', $category);
            }
        }

        if ($risk) {
            $qb->andWhere('i.riskLevel = :risk')
               ->setParameter('risk', $risk);
        }

        if ($price) {
            if ($price === '<10k') {
                $qb->andWhere('i.estimatedValue < 10000');
            } elseif ($price === '10k-50k') {
                $qb->andWhere('i.estimatedValue >= 10000 AND i.estimatedValue <= 50000');
            } elseif ($price === '50k-200k') {
                $qb->andWhere('i.estimatedValue >= 50000 AND i.estimatedValue <= 200000');
            } elseif ($price === '>200k') {
                $qb->andWhere('i.estimatedValue > 200000');
            }
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

        return $qb;
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

    /**
     * 🔐 Recherche et tri limité à un utilisateur (data isolation)
     */
    public function searchAndFilterQueryForUser(?string $search, string|array|null $category, ?string $risk, ?string $sort, int $userId): \Doctrine\ORM\QueryBuilder
    {
        $qb = $this->searchAndFilterQuery($search, $category, $risk, $sort, null);
        $qb->andWhere('i.createdByUserId = :userId')
           ->setParameter('userId', $userId);

        return $qb;
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