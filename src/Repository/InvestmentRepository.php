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
     * 🔥 Search and filter logic
     */
    public function searchAndFilterQuery(?string $search, string|array|null $category, ?string $risk, ?string $sort, ?string $price = null, bool $showOnlyActive = true): \Doctrine\ORM\QueryBuilder
    {
        $qb = $this->createQueryBuilder('i');

        if ($showOnlyActive) {
            $qb->andWhere('i.status = :activeStatus')
               ->setParameter('activeStatus', 'ACTIVE');
        }

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
     * 🔐 Search and filter query restricted to a specific user (Isolation)
     */
    public function searchAndFilterQueryForUser(?string $search, string|array|null $category, ?string $risk, ?string $sort, \App\Entity\User $user): \Doctrine\ORM\QueryBuilder
    {
        $qb = $this->searchAndFilterQuery($search, $category, $risk, $sort, null);
        $qb->andWhere('i.user = :user')
           ->setParameter('user', $user);

        return $qb;
    }

    /**
     * @return Investment[]
     */
    public function findTopActiveByEstimatedValue(int $limit): array
    {
        return $this->createQueryBuilder('i')
            ->andWhere('i.status = :status')
            ->setParameter('status', 'ACTIVE')
            ->orderBy('i.estimatedValue', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    /**
     * @return Investment[]
     */
    public function findTopActiveByLowestRisk(int $limit): array
    {
        $riskOrder = "CASE i.riskLevel 
            WHEN 'LOW' THEN 1 
            WHEN 'MEDIUM' THEN 2 
            WHEN 'HIGH' THEN 3 
            ELSE 4 END";

        return $this->createQueryBuilder('i')
            ->andWhere('i.status = :status')
            ->setParameter('status', 'ACTIVE')
            ->orderBy($riskOrder, 'ASC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }
}
