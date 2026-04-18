<?php

namespace App\Repository;

use App\Entity\Bourse;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class BourseRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Bourse::class);
    }

    /**
     * Récupère toutes les bourses avec le nombre d'actions
     * 
     * @return array
     */
    public function findAllWithActionsCount(): array
    {
        return $this->createQueryBuilder('b')
            ->select('b', 'COUNT(a.id) as nbActions')
            ->leftJoin('App\Entity\Action', 'a', 'WITH', 'a.bourse = b.id')
            ->groupBy('b.id')
            ->orderBy('b.nomBourse', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Compte le nombre d'actions pour une bourse
     * 
     * @param Bourse $bourse
     * @return int
     */
    public function countActions(Bourse $bourse): int
    {
        return $this->getEntityManager()
            ->createQuery('
                SELECT COUNT(a.id)
                FROM App\Entity\Action a
                WHERE a.bourse = :bourse
            ')
            ->setParameter('bourse', $bourse)
            ->getSingleScalarResult();
    }

    /**
     * Récupère les bourses sans actions
     * 
     * @return Bourse[]
     */
    public function findBoursesWithoutActions(): array
    {
        return $this->createQueryBuilder('b')
            ->leftJoin('App\Entity\Action', 'a', 'WITH', 'a.bourse = b.id')
            ->groupBy('b.id')
            ->having('COUNT(a.id) = 0')
            ->getQuery()
            ->getResult();
    }

    // ... autres méthodes existantes ...
    
    public function findActive(): array
    {
        return $this->createQueryBuilder('b')
            ->where('b.statut = :statut')
            ->setParameter('statut', 'ACTIVE')
            ->orderBy('b.nomBourse', 'ASC')
            ->getQuery()
            ->getResult();
    }

    public function search(string $query): array
    {
        return $this->createQueryBuilder('b')
            ->where('b.nomBourse LIKE :query')
            ->orWhere('b.pays LIKE :query')
            ->setParameter('query', '%' . $query . '%')
            ->orderBy('b.nomBourse', 'ASC')
            ->getQuery()
            ->getResult();
    }

    public function save(Bourse $bourse, bool $flush = true): void
    {
        $this->getEntityManager()->persist($bourse);
        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }

    public function remove(Bourse $bourse, bool $flush = true): void
    {
        $this->getEntityManager()->remove($bourse);
        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }

    public function getStatistics(): array
{
    return $this->createQueryBuilder('b')
        ->select('COUNT(b.id) as totalBourses')
        ->addSelect('SUM(CASE WHEN b.statut = :active THEN 1 ELSE 0 END) as activeBourses')
        ->setParameter('active', 'ACTIVE')
        ->getQuery()
        ->getSingleResult();
}
}