<?php

namespace App\Service;

use App\Entity\Action;
use App\Entity\NotificationBourse;
use App\Entity\User;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;

/**
 * 🔔 NotificationBourseService
 *
 * Handles creation of in-app Bourse notifications:
 * - Admin creates a new Action (DISPONIBLE) → notify all users
 * - User buys an action → notify that user
 * - User sells an action → notify that user
 *
 * Optionally sends an SMS via SmsService if the user has a phone (does NOT modify SmsService).
 */
class NotificationBourseService
{
    public function __construct(
        private EntityManagerInterface $em,
        private UserRepository         $userRepository,
        private SmsService             $smsService
    ) {}

    /**
     * Called when an admin creates/activates a new Action.
     * Sends an in-app notification to every user in the system.
     */
    public function notifyNewAction(Action $action): void
    {
        $users = $this->userRepository->findAll();

        foreach ($users as $user) {
            $notif = new NotificationBourse();
            $notif->setUser($user);
            $notif->setType(NotificationBourse::TYPE_NEW_ACTION);
            $notif->setTitre('📈 Nouvelle action disponible');
            $notif->setMessage(sprintf(
                'L\'action %s (%s) vient d\'être ajoutée au marché à %.2f TND.',
                $action->getNomEntreprise(),
                $action->getSymbole(),
                $action->getPrixUnitaire()
            ));
            $notif->setAction($action);
            $this->em->persist($notif);

            // Optional SMS (only if user has phone; does not modify SmsService)
            if ($user->getPhone()) {
                $this->smsService->sendSms(
                    $user->getPhone(),
                    sprintf('FINORA — Nouvelle action %s disponible à %.2f TND !', $action->getSymbole(), $action->getPrixUnitaire())
                );
            }
        }

        $this->em->flush();
    }

    /**
     * Called after a successful BUY or SELL trade.
     * Notifies the trading user with a confirmation.
     */
    public function notifyTradeConfirmation(
        User   $user,
        string $type,      // 'ACHAT' or 'VENTE'
        Action $action,
        int    $quantite,
        float  $montantTotal
    ): void {
        $isAchat = $type === 'ACHAT';

        $notif = new NotificationBourse();
        $notif->setUser($user);
        $notif->setType($isAchat ? NotificationBourse::TYPE_ACHAT : NotificationBourse::TYPE_VENTE);
        $notif->setAction($action);

        if ($isAchat) {
            $notif->setTitre('✅ Achat confirmé');
            $notif->setMessage(sprintf(
                'Vous avez acheté %d action(s) %s (%s) pour %.2f TND.',
                $quantite,
                $action->getNomEntreprise(),
                $action->getSymbole(),
                $montantTotal
            ));
        } else {
            $notif->setTitre('💰 Vente confirmée');
            $notif->setMessage(sprintf(
                'Vous avez vendu %d action(s) %s (%s). +%.2f TND crédités dans votre wallet.',
                $quantite,
                $action->getNomEntreprise(),
                $action->getSymbole(),
                $montantTotal
            ));
        }

        $this->em->persist($notif);
        $this->em->flush();

        // Optional SMS confirmation
        if ($user->getPhone()) {
            $smsText = $isAchat
                ? sprintf('FINORA — Achat de %d actions %s confirmé (%.2f TND débités).', $quantite, $action->getSymbole(), $montantTotal)
                : sprintf('FINORA — Vente de %d actions %s confirmée (+%.2f TND).', $quantite, $action->getSymbole(), $montantTotal);
            $this->smsService->sendSms($user->getPhone(), $smsText);
        }
    }
}
