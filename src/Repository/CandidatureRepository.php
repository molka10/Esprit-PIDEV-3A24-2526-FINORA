<?php

namespace App\Repository;

use App\Entity\Candidature;
use App\Entity\User;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Candidature>
 */
class CandidatureRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Candidature::class);
    }

    public function findByUserRole(?User $user, string $role, int $limit = null, int $offset = null): array
    {
        $qb = $this->getQueryBuilderByRole($user, $role);
        
        if ($limit) {
            $qb->setMaxResults($limit);
        }
        if ($offset) {
            $qb->setFirstResult($offset);
        }

        return $qb->orderBy('c.createdAt', 'DESC')
                  ->getQuery()
                  ->getResult();
    }

    public function countByUserRole(?User $user, string $role): int
    {
        $qb = $this->getQueryBuilderByRole($user, $role);
        $qb->select('COUNT(c.id)');
        
        return (int) $qb->getQuery()->getSingleScalarResult();
    }

    private function getQueryBuilderByRole(?User $user, string $role)
    {
        $qb = $this->createQueryBuilder('c')
            ->leftJoin('c.appelOffre', 'a')
            ->addSelect('a');

        if ($role === 'entreprise' && $user) {
            $qb->andWhere('c.user = :user')
               ->setParameter('user', $user);
        }

        return $qb;
    }
}
