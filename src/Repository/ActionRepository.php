<?php

namespace App\Repository;

use App\Entity\Action;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Action>
 *
 * @method Action|null find($id, $lockMode = null, $lockVersion = null)
 * @method Action|null findOneBy(array $criteria, array $orderBy = null)
 * @method Action[]    findAll()
 * @method Action[]    findBy(array $criteria, array $orderBy = null, $limit = null, $offset = null)
 */
class ActionRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Action::class);
    }

    /**
     * Récupère toutes les actions disponibles
     *
     * @return Action[]
     */
    public function findAvailable(): array
    {
        return $this->createQueryBuilder('a')
            ->where('a.statut = :statut')
            ->andWhere('a.quantiteDisponible > 0')
            ->setParameter('statut', 'DISPONIBLE')
            ->orderBy('a.symbole', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Recherche des actions par symbole ou nom d'entreprise
     *
     * @param string $query
     * @return Action[]
     */
    public function search(string $query): array
    {
        return $this->createQueryBuilder('a')
            ->where('a.symbole LIKE :query')
            ->orWhere('a.nomEntreprise LIKE :query')
            ->setParameter('query', '%' . $query . '%')
            ->orderBy('a.symbole', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Récupère les actions par secteur
     *
     * @param string $secteur
     * @return Action[]
     */
    public function findBySecteur(string $secteur): array
    {
        return $this->createQueryBuilder('a')
            ->where('a.secteur = :secteur')
            ->setParameter('secteur', $secteur)
            ->orderBy('a.symbole', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Récupère les actions par bourse
     *
     * @param int $bourseId
     * @return Action[]
     */
    public function findByBourse(int $bourseId): array
    {
        return $this->createQueryBuilder('a')
            ->where('a.bourse = :bourse')
            ->setParameter('bourse', $bourseId)
            ->orderBy('a.symbole', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Compte le nombre d'actions par statut
     *
     * @param string $statut
     * @return int
     */
    public function countByStatut(string $statut): int
    {
        return $this->createQueryBuilder('a')
            ->select('COUNT(a.id)')
            ->where('a.statut = :statut')
            ->setParameter('statut', $statut)
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * Récupère les actions avec un prix dans une fourchette
     *
     * @param float $minPrix
     * @param float $maxPrix
     * @return Action[]
     */
    public function findByPriceRange(float $minPrix, float $maxPrix): array
    {
        return $this->createQueryBuilder('a')
            ->where('a.prixUnitaire BETWEEN :min AND :max')
            ->setParameter('min', $minPrix)
            ->setParameter('max', $maxPrix)
            ->orderBy('a.prixUnitaire', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Vérifie si une action avec ce symbole existe déjà
     *
     * @param string $symbole
     * @param int|null $excludeId ID à exclure (pour l'édition)
     * @return bool
     */
    public function existsBySymbole(string $symbole, ?int $excludeId = null): bool
    {
        $qb = $this->createQueryBuilder('a')
            ->select('COUNT(a.id)')
            ->where('a.symbole = :symbole')
            ->setParameter('symbole', strtoupper($symbole));

        if ($excludeId !== null) {
            $qb->andWhere('a.id != :id')
               ->setParameter('id', $excludeId);
        }

        return $qb->getQuery()->getSingleScalarResult() > 0;
    }

    /**
     * Récupère les actions avec stock faible (moins de 10)
     *
     * @return Action[]
     */
    public function findLowStock(): array
    {
        return $this->createQueryBuilder('a')
            ->where('a.quantiteDisponible < :seuil')
            ->andWhere('a.quantiteDisponible > 0')
            ->setParameter('seuil', 10)
            ->orderBy('a.quantiteDisponible', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Récupère les actions sans stock
     *
     * @return Action[]
     */
    public function findOutOfStock(): array
    {
        return $this->createQueryBuilder('a')
            ->where('a.quantiteDisponible = 0')
            ->orderBy('a.symbole', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Récupère les actions les plus chères
     *
     * @param int $limit
     * @return Action[]
     */
    public function findMostExpensive(int $limit = 10): array
    {
        return $this->createQueryBuilder('a')
            ->orderBy('a.prixUnitaire', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    /**
     * Récupère les actions les moins chères
     *
     * @param int $limit
     * @return Action[]
     */
    public function findCheapest(int $limit = 10): array
    {
        return $this->createQueryBuilder('a')
            ->where('a.prixUnitaire > 0')
            ->orderBy('a.prixUnitaire', 'ASC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    /**
     * Récupère les statistiques des actions
     *
     * @return array
     */
    public function getStatistics(): array
    {
        $total = $this->count([]);
        $disponibles = $this->countByStatut('DISPONIBLE');
        $indisponibles = $this->countByStatut('INDISPONIBLE');

        $avgPrice = $this->createQueryBuilder('a')
            ->select('AVG(a.prixUnitaire)')
            ->getQuery()
            ->getSingleScalarResult();

        $totalStock = $this->createQueryBuilder('a')
            ->select('SUM(a.quantiteDisponible)')
            ->getQuery()
            ->getSingleScalarResult();

        return [
            'total' => $total,
            'disponibles' => $disponibles,
            'indisponibles' => $indisponibles,
            'prix_moyen' => round($avgPrice ?? 0, 2),
            'stock_total' => $totalStock ?? 0,
            'pourcentage_disponibles' => $total > 0 ? round(($disponibles / $total) * 100, 2) : 0
        ];
    }

    /**
     * Récupère les actions les plus récentes
     *
     * @param int $limit
     * @return Action[]
     */
    public function findRecent(int $limit = 5): array
    {
        return $this->createQueryBuilder('a')
            ->orderBy('a.dateAjout', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    /**
     * Récupère les secteurs disponibles
     *
     * @return array
     */
    public function getAvailableSecteurs(): array
    {
        $result = $this->createQueryBuilder('a')
            ->select('DISTINCT a.secteur')
            ->orderBy('a.secteur', 'ASC')
            ->getQuery()
            ->getResult();

        return array_column($result, 'secteur');
    }

    /**
     * Sauvegarde une action
     *
     * @param Action $action
     * @param bool $flush
     * @return void
     */
    public function save(Action $action, bool $flush = true): void
    {
        $this->getEntityManager()->persist($action);

        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }

    /**
     * Supprime une action
     *
     * @param Action $action
     * @param bool $flush
     * @return void
     */
    public function remove(Action $action, bool $flush = true): void
    {
        $this->getEntityManager()->remove($action);

        if ($flush) {
            $this->getEntityManager()->flush();
        }
    }
}