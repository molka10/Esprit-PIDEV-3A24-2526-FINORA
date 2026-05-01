<?php

namespace App\Repository;

use App\Entity\ActionNews;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<ActionNews>
 */
class ActionNewsRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, ActionNews::class);
    }

    public function findRecentWithAction(int $limit = 5): array
    {
        return $this->createQueryBuilder('n')
            ->innerJoin('n.action', 'a')
            ->addSelect('a')
            ->orderBy('n.dateAjout', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }
}
