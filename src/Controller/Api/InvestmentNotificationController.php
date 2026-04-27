<?php

namespace App\Controller\Api;

use App\Repository\InvestmentNotificationRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/api/investment/notifications', name: 'api_investment_notif_')]
class InvestmentNotificationController extends AbstractController
{
    public function __construct(
        private InvestmentNotificationRepository $repo,
        private EntityManagerInterface           $em
    ) {}

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
            'titre'     => $n->getTitle(),
            'message'   => $n->getMessage(),
            'isRead'    => $n->isRead(),
            'icon'      => $n->getIcon(),
            'timeAgo'   => $n->getTimeAgo(),
            'investmentId' => $n->getInvestment()?->getId(),
        ], $notifications);

        return $this->json([
            'count'         => $unreadCount,
            'notifications' => $data,
        ]);
    }

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
