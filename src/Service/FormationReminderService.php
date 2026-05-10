<?php

namespace App\Service;

use App\Entity\User;
use App\Entity\UserFormationProgress;
use Doctrine\ORM\EntityManagerInterface;

class FormationReminderService
{
    public function __construct(
        private EntityManagerInterface $entityManager,
        private NotificationService $notificationService
    ) {}

    public function checkDeadlines(?User $user = null): void
    {
        $qb = $this->entityManager->createQueryBuilder()
            ->select('p')
            ->from(UserFormationProgress::class, 'p')
            ->where('p.reminderSent = false')
            ->andWhere('p.deadlineAt <= :threeDaysFromNow')
            ->setParameter('threeDaysFromNow', new \DateTime('+3 days'));

        if ($user) {
            $qb->andWhere('p.user = :user')
               ->setParameter('user', $user);
        }

        $pendingProgress = $qb->getQuery()->getResult();

        foreach ($pendingProgress as $progress) {
            /** @var UserFormationProgress $progress */
            $formation = $progress->getFormation();
            $user = $progress->getUser();

            $this->notificationService->send(
                $user,
                'urgent',
                'Échéance de formation proche !',
                sprintf('Votre accès à la formation "%s" expire dans moins de 3 jours. Profitez des dernières leçons !', $formation->getTitre())
            );

            $progress->setReminderSent(true);
        }

        $this->entityManager->flush();
    }
}
