<?php

namespace App\Repository;

use App\Entity\TransactionWallet;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class TransactionWalletRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, TransactionWallet::class);
    }

    /**
     * @return \App\Dto\TransactionStatsDto[]
     */
    public function getStatsByUser(int $userId): array
    {
        return $this->createQueryBuilder('t')
            ->select(sprintf(
                'NEW %s(t.type, SUM(t.montant), COUNT(t.id))',
                \App\Dto\TransactionStatsDto::class
            ))
            ->andWhere('t.user = :userId')
            ->andWhere('t.status = :status')
            ->setParameter('userId', $userId)
            ->setParameter('status', 'ACCEPTED')
            ->groupBy('t.type')
            ->getQuery()
            ->getResult();
    }

    public function findByUserWithCategory($user): array
    {
        return $this->createQueryBuilder('t')
            ->innerJoin('t.category', 'c')
            ->addSelect('c')
            ->andWhere('t.user = :user')
            ->setParameter('user', $user)
            ->orderBy('t.dateTransaction', 'DESC')
            ->getQuery()
            ->getResult();
    }
}