<?php

namespace App\Controller\Api;

use App\Entity\Investment;
use App\Entity\InvestmentManagement;
use App\Repository\InvestmentManagementRepository;
use App\Repository\InvestmentRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/api/investments')]
class InvestmentRatingApiController extends AbstractController
{
    private $entityManager;

    public function __construct(EntityManagerInterface $entityManager)
    {
        $this->entityManager = $entityManager;
    }

    #[Route('/{id}/rate', name: 'api_investment_rate', methods: ['POST'])]
    public function rate(int $id, Request $request, InvestmentRepository $investmentRepo, InvestmentManagementRepository $mgmtRepo): JsonResponse
    {
        $user = $this->getUser();
        if (!$user) {
            return new JsonResponse(['error' => 'Authentification requise'], 401);
        }

        $investment = $investmentRepo->find($id);
        if (!$investment) {
            return new JsonResponse(['error' => 'Investissement non trouvé'], 404);
        }

        // Vérifier si l'utilisateur participe à cet investissement
        $mgmt = $mgmtRepo->findOneBy(['user' => $user, 'investment' => $investment]);
        if (!$mgmt) {
            return new JsonResponse(['error' => 'Vous devez investir dans ce projet pour le noter'], 403);
        }

        // Vérification bonus : investissement non actif
        if ($investment->getStatus() !== 'ACTIVE' && $investment->getStatus() !== 'CLOSED') {
             return new JsonResponse(['error' => 'Rating non autorisé pour ce statut'], 403);
        }

        $data = json_decode($request->getContent(), true);
        $rating = $data['rating'] ?? null;

        if ($rating === null || $rating < 1 || $rating > 5) {
            return new JsonResponse(['error' => 'Le rating doit être entre 1 et 5'], 400);
        }

        $mgmt->setRating((int)$rating);
        $this->entityManager->flush();

        return new JsonResponse([
            'message' => 'Rating mis à jour avec succès',
            'rating' => $mgmt->getRating()
        ]);
    }

    #[Route('/{id}/rating', name: 'api_investment_get_rating', methods: ['GET'])]
    public function getRating(int $id, InvestmentRepository $investmentRepo, InvestmentManagementRepository $mgmtRepo): JsonResponse
    {
        $investment = $investmentRepo->find($id);
        if (!$investment) {
            return new JsonResponse(['error' => 'Investissement non trouvé'], 404);
        }

        $stats = $mgmtRepo->createQueryBuilder('m')
            ->select('AVG(m.rating) as averageRating, COUNT(m.rating) as totalVotes')
            ->where('m.investment = :investment')
            ->andWhere('m.rating IS NOT NULL')
            ->setParameter('investment', $investment)
            ->getQuery()
            ->getOneOrNullResult();

        $avg = $stats['averageRating'] ? round((float)$stats['averageRating'], 1) : 0;
        $total = (int)($stats['totalVotes'] ?? 0);

        $canRate = false;
        $userRating = null;
        $user = $this->getUser();
        
        if ($user) {
            $mgmt = $mgmtRepo->findOneBy(['user' => $user, 'investment' => $investment]);
            if ($mgmt) {
                $userRating = $mgmt->getRating();
                // Check if status allows rating
                if ($investment->getStatus() === 'ACTIVE' || $investment->getStatus() === 'CLOSED') {
                    $canRate = true;
                }
            }
        }

        return new JsonResponse([
            'averageRating' => $avg,
            'totalVotes' => $total,
            'userRating' => $userRating,
            'canRate' => $canRate
        ]);
    }
}
