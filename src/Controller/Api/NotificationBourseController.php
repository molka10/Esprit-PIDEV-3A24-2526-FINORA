<?php

namespace App\Controller\Api;

use App\Repository\NotificationBourseRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/api/bourse/notifications', name: 'api_bourse_notif_')]
class NotificationBourseController extends AbstractController
{
    public function __construct(
        private NotificationBourseRepository $repo,
        private EntityManagerInterface       $em
    ) {}

    /**
     * Returns unread notification count + recent notifications for the bell dropdown.
     */
    #[Route('', name: 'list', methods: ['GET'])]
    public function list(): JsonResponse
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->json(['count' => 0, 'notifications' => []]);
        }

        $notifications = $this->repo->findRecentByUser($user, 10);
        $unreadCount   = $this->repo->countUnreadByUser($user);

        $data = array_map(fn($n) => [
            'id'        => $n->getId(),
            'type'      => $n->getType(),
            'titre'     => $n->getTitre(),
            'message'   => $n->getMessage(),
            'isRead'    => $n->isRead(),
            'icon'      => $n->getIcon(),
            'timeAgo'   => $n->getTimeAgo(),
            'symbol'    => $n->getAction()?->getSymbole(),
        ], $notifications);

        return $this->json([
            'count'         => $unreadCount,
            'notifications' => $data,
        ]);
    }

    /**
     * Mark all notifications as read (called when user opens the bell).
     */
    #[Route('/read', name: 'mark_read', methods: ['POST'])]
    public function markRead(): JsonResponse
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->json(['success' => false], Response::HTTP_UNAUTHORIZED);
        }

        $this->repo->markAllReadByUser($user);

        return $this->json(['success' => true, 'count' => 0]);
    }
}
