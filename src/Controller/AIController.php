<?php

namespace App\Controller;

use App\Service\AIAnalyzerService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\HttpFoundation\Request;

class AIController extends AbstractController
{
    #[Route('/ai/analyse', name: 'ai_analyse')]
    public function analyse(AIAnalyzerService $ai): Response
    {
        $income = 1992.54;
        $expenses = 1656.72;

        $result = $ai->analyze($income, $expenses);

        return $this->render('ai/analyse.html.twig', [
            'data' => $result
        ]);
    }

  #[Route('/ai/analyse-data', name: 'ai_data')]
public function analyseData(Request $request, AIAnalyzerService $ai): JsonResponse
{
    $income = (float) $request->query->get('income');
    $expenses = (float) $request->query->get('expenses');

    $result = $ai->analyze($income, $expenses);

    return $this->json($result);
}
}