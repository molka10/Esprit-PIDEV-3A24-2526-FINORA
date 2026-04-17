<?php

namespace App\Controller;

use App\Service\AIAnalyzerService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\HttpFoundation\Request;
use App\Repository\TransactionWalletRepository;

class AIController extends AbstractController
{
    #[Route('/ai/analyse', name: 'ai_analyse')]
    public function analyse(AIAnalyzerService $ai, TransactionWalletRepository $repo): Response
    {
        $transactions = $repo->findBy(['userId' => 6]); // Currently hardcoded user 6 
        
        $income = 0;
        $expenses = 0;

        foreach ($transactions as $t) {
            $amount = abs($t->getMontant());
            if (strtolower($t->getType()) === 'income' || strtolower($t->getType()) === 'revenu') {
                $income += $amount;
            } else {
                $expenses += $amount;
            }
        }

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