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
     * 🔥 SEARCH + FILTER restricted to user
     */
    public function searchForUser(\App\Entity\User $user, ?string $search = null, ?string $status = null): array
    {
        $qb = $this->createQueryBuilder('m')
            ->andWhere('m.user = :user')
            ->setParameter('user', $user);

        if ($search) {
            $qb->andWhere('LOWER(m.investmentType) LIKE :search')
               ->setParameter('search', '%' . strtolower($search) . '%');
        }

        if ($status) {
            $qb->andWhere('m.status = :status')
               ->setParameter('status', $status);
        }

        return $qb
            ->orderBy('m.managementId', 'DESC')
            ->getQuery()
            ->getResult();
    }

    public function findByInvestment(Investment $investment): array
    {
        return $this->createQueryBuilder('m')
            ->andWhere('m.investment = :inv')
            ->setParameter('inv', $investment)
            ->orderBy('m.startDate', 'DESC')
            ->getQuery()
            ->getResult();
    }
}
