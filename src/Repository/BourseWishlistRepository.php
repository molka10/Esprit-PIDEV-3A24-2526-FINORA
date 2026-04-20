<?php

namespace App\Repository;

use App\Entity\BourseWishlist;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<BourseWishlist>
 *
 * @method BourseWishlist|null find($id, $lockMode = null, $lockVersion = null)
 * @method BourseWishlist|null findOneBy(array $criteria, array $orderBy = null)
 * @method BourseWishlist[]    findAll()
 * @method BourseWishlist[]    findBy(array $criteria, array $orderBy = null, $limit = null, $offset = null)
 */
class BourseWishlistRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, BourseWishlist::class);
    }
}
