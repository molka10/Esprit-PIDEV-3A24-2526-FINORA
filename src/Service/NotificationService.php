<?php

namespace App\Service;

use App\Entity\User;
use App\Repository\InvestmentNotificationRepository;
use App\Repository\NotificationBourseRepository;
use Doctrine\ORM\EntityManagerInterface;

class NotificationService
{
    public function __construct(
        private InvestmentNotificationRepository $investmentNotifRepo,
        private NotificationBourseRepository $bourseNotifRepo,
        private EntityManagerInterface $em
    ) {}

    /**
     * Returns the last 15 notifications for a user, merged from all sources.
     */
    public function getRecentForUser(int $userId, int $limit = 15): array
    {
        $investmentNotifs = $this->investmentNotifRepo->findBy(
            ['user' => $userId],
            ['createdAt' => 'DESC'],
            $limit
        );

        $bourseNotifs = $this->bourseNotifRepo->findBy(
            ['user' => $userId],
            ['createdAt' => 'DESC'],
            $limit
        );

        // Normalize into a unified array
        $all = [];

        foreach ($investmentNotifs as $n) {
            $all[] = [
                'id'        => 'inv_' . $n->getId(),
                'type'      => $n->getType(),
                'title'     => $n->getTitle(),
                'message'   => $n->getMessage(),
                'is_read'   => $n->isRead(),
                'created_at'=> $n->getCreatedAt(),
                'icon'      => $this->getIcon($n->getType()),
                'source'    => 'investment',
                'source_id' => $n->getId(),
                'link'      => $n->getLink(),
            ];
        }

        foreach ($bourseNotifs as $n) {
            $all[] = [
                'id'        => 'brs_' . $n->getId(),
                'type'      => $n->getType(),
                'title'     => $n->getTitre(),
                'message'   => $n->getMessage(),
                'is_read'   => $n->isRead(),
                'created_at'=> $n->getCreatedAt(),
                'icon'      => $this->getIcon($n->getType()),
                'source'    => 'bourse',
                'source_id' => $n->getId(),
            ];
        }

        // Sort merged array by date DESC
        usort($all, fn($a, $b) => $b['created_at'] <=> $a['created_at']);

        return array_slice($all, 0, $limit);
    }

    /**
     * Returns count of unread notifications for a user.
     */
    public function getUnreadCount(int $userId): int
    {
        $investCount = $this->investmentNotifRepo->count(['user' => $userId, 'isRead' => false]);
        $bourseCount = $this->bourseNotifRepo->count(['user' => $userId, 'isRead' => false]);

        return $investCount + $bourseCount;
    }

    /**
     * Marks all notifications as read for a user.
     */
    public function markAllAsRead(int $userId): void
    {
        // Investment notifications
        $this->em->createQuery(
            'UPDATE App\Entity\InvestmentNotification n SET n.isRead = 1 WHERE n.user = :uid AND n.isRead = 0'
        )->setParameter('uid', $userId)->execute();

        // Bourse notifications
        $this->em->createQuery(
            'UPDATE App\Entity\NotificationBourse n SET n.isRead = 1 WHERE n.user = :uid AND n.isRead = 0'
        )->setParameter('uid', $userId)->execute();
    }

    /**
     * Sends a new notification.
     */
    public function send(User $user, string $type, string $title, string $message, ?string $link = null): void
    {
        $notif = new \App\Entity\InvestmentNotification();
        $notif->setUser($user);
        $notif->setType($type);
        $notif->setTitle($title);
        $notif->setMessage($message);
        $notif->setLink($link);
        
        $this->em->persist($notif);
        $this->em->flush();
    }

    /**
     * Returns a Bootstrap icon class based on notification type.
     */
    private function getIcon(string $type): string
    {
        return match(strtolower($type)) {
            'achat', 'purchase'     => 'bi-cart-check-fill text-success',
            'vente', 'sale'         => 'bi-cash-coin text-danger',
            'investment'            => 'bi-graph-up-arrow text-primary',
            'alert', 'warning'      => 'bi-exclamation-triangle-fill text-warning',
            'message'               => 'bi-chat-dots-fill text-info',
            'candidature'           => 'bi-file-earmark-person-fill text-purple',
            'formation'             => 'bi-mortarboard-fill text-indigo',
            default                 => 'bi-bell-fill text-secondary',
        };
    }
}
