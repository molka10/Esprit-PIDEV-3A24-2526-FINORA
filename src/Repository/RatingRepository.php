<?php

namespace App\Repository;

use App\Entity\Rating;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class RatingRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Rating::class);
    }

    public function getMoyenne(int $appelOffreId): float
    {
        $result = $this->createQueryBuilder('r')
            ->select('AVG(r.note) as moyenne')
            ->where('r.appelOffre = :id')
            ->setParameter('id', $appelOffreId)
            ->getQuery()
            ->getSingleScalarResult();

        return $result ? (float) $result : 0;
    }

    public function getTotalVotes(int $appelOffreId): int
    {
        return $this->createQueryBuilder('r')
            ->select('COUNT(r.id)')
            ->where('r.appelOffre = :id')
            ->setParameter('id', $appelOffreId)
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function findByIpAndAppel(string $ip, int $appelOffreId): ?Rating
    {
        return null; // Simplifié - on utilise la session
    }
}