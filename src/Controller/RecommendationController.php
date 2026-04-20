<?php

namespace App\Controller;

use App\Service\RecommendationService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class RecommendationController extends AbstractController
{
    #[Route('/api/recommendations', name: 'api_recommendations', methods: ['GET'])]
    public function getRecommendations(Request $request, RecommendationService $recommendationService): JsonResponse
    {
        $userId = $request->query->get('user_id');

        if (!$userId) {
            return $this->json([
                'status' => 'error',
                'message' => 'Query parameter user_id is required'
            ], 400);
        }

        try {
            $recommendations = $recommendationService->getRecommendations((int) $userId);

            return $this->json([
                'user_id' => (int) $userId,
                'recommendations' => $recommendations,
                'timestamp' => time()
            ]);
        } catch (\Exception $e) {
            return $this->json([
                'status' => 'error',
                'message' => 'Internal Server Error'
            ], 500);
        }
    }

    #[Route('/admin/recommendations/audit', name: 'admin_recommendations_audit')]
    public function auditView(): Response 
    {
        return $this->render('admin/recommendations.html.twig');
    }
}
