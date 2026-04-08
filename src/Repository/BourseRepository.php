<?php

namespace App\Repository;

use App\Entity\Bourse;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Bourse>
 *
 * @method Bourse|null find($id, $lockMode = null, $lockVersion = null)
 * @method Bourse|null findOneBy(array $criteria, array $orderBy = null)
 * @method Bourse[]    findAll()
 * @method Bourse[]    findBy(array $criteria, array $orderBy = null, $limit = null, $offset = null)
 */
class BourseRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Bourse::class);
    }

    /**
     * Récupère toutes les bourses actives
     *
     * @return Bourse[]
     */
    public function findActive(): array
    {
        return $this->createQueryBuilder('b')
            ->where('b.statut = :statut')
            ->setParameter('statut', 'ACTIVE')
            ->orderBy('b.nomBourse', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Recherche des bourses par nom ou pays
     *
     * @param string $query
     * @return Bourse[]
     */
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

    /**
     * Compte le nombre de bourses par statut
     *
     * @param string $statut
     * @return int
     */
    public function countByStatut(string $statut): int
    {
        return $this->createQueryBuilder('b')
            ->select('COUNT(b.id)')
            ->where('b.statut = :statut')
            ->setParameter('statut', $statut)
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * Récupère les bourses par devise
     *
     * @param string $devise
     * @return Bourse[]
     */
    public function findByDevise(string $devise): array
    {
        return $this->createQueryBuilder('b')
            ->where('b.devise = :devise')
            ->setParameter('devise', $devise)
            ->orderBy('b.nomBourse', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Récupère les bourses créées après une certaine date
     *
     * @param \DateTimeInterface $date
     * @return Bourse[]
     */
    public function findCreatedAfter(\DateTimeInterface $date): array
    {
        return $this->createQueryBuilder('b')
            ->where('b.dateCreation > :date')
            ->setParameter('date', $date)
            ->orderBy('b.dateCreation', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Vérifie si une bourse avec ce nom existe déjà
     *
     * @param string $nomBourse
     * @param int|null $excludeId ID à exclure (pour l'édition)
     * @return bool
     */
    public function existsByNom(string $nomBourse, ?int $excludeId = null): bool
    {
        $qb = $this->createQueryBuilder('b')
            ->select('COUNT(b.id)')
            ->where('b.nomBourse = :nom')
            ->setParameter('nom', $nomBourse);

        if ($excludeId !== null) {
            $qb->andWhere('b.id != :id')
               ->setParameter('id', $excludeId);
        }

        return $qb->getQuery()->getSingleScalarResult() > 0;
    }

    /**
     * Récupère les statistiques des bourses
     *
     * @return array
     */
    public function getStatistics(): array
    {
        $total = $this->count([]);
        $actives = $this->countByStatut('ACTIVE');
        $inactives = $this->countByStatut('INACTIVE');

        return [
            'total' => $total,
            'actives' => $actives,
            'inactives' => $inactives,
            'pourcentage_actives' => $total > 0 ? round(($actives / $total) * 100, 2) : 0
        ];
    }

    /**
     * Récupère les bourses les plus récentes
     *
     * @param int $limit
     * @return Bourse[]
     */
    public function findRecent(int $limit = 5): array
    {
        return $this->createQueryBuilder('b')
            ->orderBy('b.dateCreation', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    /**
     * Sauvegarde une bourse
     *
     * @param Bourse $bourse
     * @param bool $flush
     * @return void
     */
    public function save(Bourse $bourse, bool $flush = true): void
    {
        $this->getEntityManager()->persist($bourse);

        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }

    /**
     * Supprime une bourse
     *
     * @param Bourse $bourse
     * @param bool $flush
     * @return void
     */
    public function remove(Bourse $bourse, bool $flush = true): void
    {
        $this->getEntityManager()->remove($bourse);

        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }
}