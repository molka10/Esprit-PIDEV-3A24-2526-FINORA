<?php

namespace App\Controller;

use App\Service\NotificationService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/notifications')]
class NotificationController extends AbstractController
{
    public function __construct(
        private NotificationService $notificationService
    ) {}

    #[Route('/count', name: 'notifications_count', methods: ['GET'])]
    public function getCount(): JsonResponse
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->json(['count' => 0]);
        }

        return $this->json([
            'count' => $this->notificationService->getUnreadCount($user->getId())
        ]);
    }

    #[Route('/list', name: 'notifications_list', methods: ['GET'])]
    public function getList(): Response
    {
        $user = $this->getUser();
        if (!$user) {
            return new Response('');
        }

        $notifications = $this->notificationService->getRecentForUser($user->getId());

        return $this->render('notifications/_dropdown_content.html.twig', [
            'notifications' => $notifications
        ]);
    }

    #[Route('/mark-read', name: 'notifications_mark_read', methods: ['POST'])]
    public function markRead(): JsonResponse
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->json(['success' => false], Response::HTTP_UNAUTHORIZED);
        }

        $this->notificationService->markAllAsRead($user->getId());

        return $this->json(['success' => true]);
    }
}
