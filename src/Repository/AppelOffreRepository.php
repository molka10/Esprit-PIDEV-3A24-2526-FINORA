<?php

namespace App\Repository;

use App\Entity\AppelOffre;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class AppelOffreRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, AppelOffre::class);
    }

    public function findByFilters(?string $type, ?string $statut, ?int $categorieId, ?string $search, ?string $role = null, int $limit = null, int $offset = null): array
    {
        $qb = $this->getQueryBuilderByFilters($type, $statut, $categorieId, $search, $role);

        if ($limit) {
            $qb->setMaxResults($limit);
        }
        if ($offset) {
            $qb->setFirstResult($offset);
        }

        return $qb->orderBy('a.createdAt', 'DESC')
                  ->getQuery()
                  ->getResult();
    }

    public function countByFilters(?string $type, ?string $statut, ?int $categorieId, ?string $search, ?string $role = null): int
    {
        $qb = $this->getQueryBuilderByFilters($type, $statut, $categorieId, $search, $role);
        $qb->select('COUNT(a.id)');
        
        return (int) $qb->getQuery()->getSingleScalarResult();
    }

    private function getQueryBuilderByFilters(?string $type, ?string $statut, ?int $categorieId, ?string $search, ?string $role = null)
    {
        $qb = $this->createQueryBuilder('a')
            ->leftJoin('a.categorie', 'c')
            ->addSelect('c');

        if ($role !== 'admin') {
            $qb->andWhere('a.statut = :published')
               ->setParameter('published', 'published');
        }

        if ($type) {
            $qb->andWhere('a.type = :type')
               ->setParameter('type', $type);
        }

        if ($statut && $role === 'admin') {
            $qb->andWhere('a.statut = :statut')
               ->setParameter('statut', $statut);
        }

        if ($categorieId) {
            $qb->andWhere('c.id = :categorieId')
               ->setParameter('categorieId', $categorieId);
        }

        if ($search) {
            $qb->andWhere('a.titre LIKE :search OR a.description LIKE :search')
               ->setParameter('search', '%' . $search . '%');
        }

        return $qb;
    }
    public function findAppelsExpires(\DateTime $today): array
{
    return $this->createQueryBuilder('a')
        ->where('a.dateLimite < :today')
        ->andWhere('a.statut != :closed')
        ->andWhere('a.statut != :draft')
        ->setParameter('today', $today)
        ->setParameter('closed', 'closed')
        ->setParameter('draft', 'draft')
        ->getQuery()
        ->getResult();
}
}