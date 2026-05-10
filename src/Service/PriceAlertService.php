<?php

namespace App\Service;

use App\Entity\BourseWishlist;
use App\Entity\NotificationBourse;
use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;

class PriceAlertService
{
    public function __construct(
        private EntityManagerInterface $entityManager
    ) {}

    public function checkPriceAlerts(?User $user = null): void
    {
        if (!$user) return;

        $wishlistItems = $this->entityManager->getRepository(BourseWishlist::class)->findBy(['user' => $user]);

        foreach ($wishlistItems as $item) {
            /** @var BourseWishlist $item */
            $action = $item->getAction();
            $targetPrice = $item->getTargetPrice();

            if (!$targetPrice) continue;

            $currentPrice = $action->getPrixUnitaire();

            // Simple logic: if current price is >= target price (or <= if it was a buy dip alert, 
            // but we'll assume "reached target" means current price is at or beyond the target).
            // For simplicity, we'll notify if current price is within 1% of target or passed it.
            if ((float)$currentPrice >= (float)$targetPrice) {
                $title = 'Objectif de prix atteint !';
                $message = sprintf('%s a atteint votre objectif de prix de %s. Prix actuel: %s.', $action->getSymbole(), $targetPrice, $currentPrice);

                // Check if already notified recently (last 24h)
                $existing = $this->entityManager->getRepository(NotificationBourse::class)->createQueryBuilder('n')
                    ->where('n.user = :user')
                    ->andWhere('n.titre = :title')
                    ->andWhere('n.createdAt >= :yesterday')
                    ->setParameter('user', $user)
                    ->setParameter('title', $title)
                    ->setParameter('yesterday', new \DateTime('-1 day'))
                    ->getQuery()
                    ->getResult();

                if (!$existing) {
                    $notif = new NotificationBourse();
                    $notif->setUser($user);
                    $notif->setType(NotificationBourse::TYPE_NEW_ACTION);
                    $notif->setTitre($title);
                    $notif->setMessage($message);
                    $notif->setAction($action);
                    
                    $this->entityManager->persist($notif);
                }
            }
        }

        $this->entityManager->flush();
    }
}
