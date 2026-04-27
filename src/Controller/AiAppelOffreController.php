<?php

namespace App\Controller;

use App\Service\AiService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/ai/appel-offre')]
class AiAppelOffreController extends AbstractController
{
    #[Route('/suggest-description', name: 'ai_suggest_description', methods: ['POST'])]
    public function suggestDescription(Request $request, AiService $aiService): JsonResponse
    {
        $data = json_decode($request->getContent(), true);
        $title = $data['title'] ?? '';
        $type = $data['type'] ?? '';
        $category = $data['category'] ?? '';

        if (empty($title)) {
            return new JsonResponse(['error' => 'Le titre est obligatoire'], 400);
        }

        $description = $aiService->generateTenderDescription($title, $type, $category);
        return new JsonResponse(['description' => $description]);
    }

    #[Route('/suggest-budget', name: 'ai_suggest_budget', methods: ['POST'])]
    public function suggestBudget(Request $request, AiService $aiService): JsonResponse
    {
        $data = json_decode($request->getContent(), true);
        $title = $data['title'] ?? '';
        $type = $data['type'] ?? '';
        $category = $data['category'] ?? '';

        $budget = $aiService->suggestBudget($title, $type, $category);
        return new JsonResponse($budget);
    }

    #[Route('/suggest-criteria', name: 'ai_suggest_criteria', methods: ['POST'])]
    public function suggestCriteria(Request $request, AiService $aiService): JsonResponse
    {
        $data = json_decode($request->getContent(), true);
        $title = $data['title'] ?? '';
        $type = $data['type'] ?? '';
        $category = $data['category'] ?? '';

        if (empty($title)) {
            return new JsonResponse(['error' => 'Le titre est obligatoire'], 400);
        }

        $criteria = $aiService->suggestCriteria($title, $type, $category);
        return new JsonResponse(['criteria' => $criteria]);
    }
}
