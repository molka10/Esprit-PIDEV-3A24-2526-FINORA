<?php

namespace App\Service;

use App\Entity\Candidature;
use App\Entity\InvestmentNotification;
use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;

class OfferExpiryService
{
    public function __construct(
        private EntityManagerInterface $entityManager,
        private NotificationService $notificationService
    ) {}

    public function checkExpiries(?User $user = null): void
    {
        if (!$user) return;

        $candidatures = $this->entityManager->getRepository(Candidature::class)->findBy(['user' => $user]);

        foreach ($candidatures as $candidature) {
            /** @var Candidature $candidature */
            $offer = $candidature->getAppelOffre();
            if (!$offer || !$offer->getDateLimite()) continue;

            $diff = (new \DateTime())->diff($offer->getDateLimite());
            $daysLeft = (int)$diff->format('%r%a');

            if ($daysLeft >= 0 && $daysLeft <= 2) {
                // Check if already notified
                $title = 'Offre bientôt expirée !';
                $message = sprintf('L\'appel d\'offre "%s" expire dans %d jours — vous avez postulé !', $offer->getTitre(), $daysLeft);
                
                $existing = $this->entityManager->getRepository(InvestmentNotification::class)->findOneBy([
                    'user' => $user,
                    'title' => $title,
                    'message' => $message
                ]);

                if (!$existing) {
                    $this->notificationService->send($user, 'warning', $title, $message);
                }
            }
        }
    }
}
