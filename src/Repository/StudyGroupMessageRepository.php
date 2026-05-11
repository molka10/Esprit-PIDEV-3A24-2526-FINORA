<?php

namespace App\Repository;

use App\Entity\StudyGroupMessage;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<StudyGroupMessage>
 */
class StudyGroupMessageRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, StudyGroupMessage::class);
    }

    /**
     * @return StudyGroupMessage[]
     */
    public function findRecentByGroup(int $groupId, int $limit = 50): array
    {
        return $this->createQueryBuilder('m')
            ->where('m.studyGroup = :groupId')
            ->setParameter('groupId', $groupId)
            ->orderBy('m.createdAt', 'ASC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }
}
