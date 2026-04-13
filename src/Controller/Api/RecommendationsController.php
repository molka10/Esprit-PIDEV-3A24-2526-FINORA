<?php

namespace App\Controller\Api;

use App\Service\RecommendationsBuilder;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/api/recommendations')]
final class RecommendationsController extends AbstractController
{
    public function __construct(
        private readonly RecommendationsBuilder $recommendationsBuilder,
    ) {
    }

    #[Route('/internal', name: 'api_recommendations_internal', methods: ['GET'])]
    public function internal(\Symfony\Component\HttpFoundation\Request $request): JsonResponse
    {
        $limit = $request->query->getInt('limit', 6);
        return $this->json($this->recommendationsBuilder->getInternalApiData($limit));
    }

    #[Route('/external', name: 'api_recommendations_external', methods: ['GET'])]
    public function external(\Symfony\Component\HttpFoundation\Request $request): JsonResponse
    {
        $limit = $request->query->getInt('limit', 6);
        return $this->json($this->recommendationsBuilder->getExternalApiData($limit));
    }
}
