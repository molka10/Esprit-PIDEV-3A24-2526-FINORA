<?php

namespace App\Repository;

use App\Entity\InvestmentWishlist;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<InvestmentWishlist>
 */
class InvestmentWishlistRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, InvestmentWishlist::class);
    }
    
    public function getTopWishlistedInvestments(int $limit = 5): array
    {
        return $this->createQueryBuilder('w')
            ->select('IDENTITY(w.investment) as investment_id, COUNT(w.id) as total_likes')
            ->groupBy('w.investment')
            ->orderBy('total_likes', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }
}
