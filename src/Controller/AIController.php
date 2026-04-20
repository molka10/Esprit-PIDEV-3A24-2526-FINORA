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
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }
        $transactions = $repo->findBy(['userId' => $user->getId()]);
        
        $currentMonth = (int) date('m');
        $currentYear = (int) date('Y');
        
        $prevMonth = $currentMonth - 1;
        $prevYear = $currentYear;
        if ($prevMonth === 0) {
            $prevMonth = 12;
            $prevYear--;
        }

        $currIncome = 0; $currExpenses = 0;
        $prevIncome = 0; $prevExpenses = 0;
        $globalIncome = 0; $globalExpenses = 0;

        foreach ($transactions as $t) {
            $date = $t->getDateTransaction();
            $m = (int) $date->format('m');
            $y = (int) $date->format('Y');
            $amt = abs($t->getMontant());
            $isInc = (strtolower($t->getType()) === 'income' || strtolower($t->getType()) === 'revenu');

            // Global (for overall prediction)
            if ($isInc) { $globalIncome += $amt; } else { $globalExpenses += $amt; }

            // Current Month
            if ($m === $currentMonth && $y === $currentYear) {
                if ($isInc) { $currIncome += $amt; } else { $currExpenses += $amt; }
            }
            // Previous Month
            elseif ($m === $prevMonth && $y === $prevYear) {
                if ($isInc) { $prevIncome += $amt; } else { $prevExpenses += $amt; }
            }
        }

        $result = $ai->analyze($globalIncome, $globalExpenses);
        
        // Add monthly comparison data
        $result['currentMonth'] = ['income' => $currIncome, 'expenses' => $currExpenses, 'balance' => $currIncome - $currExpenses];
        $result['prevMonth'] = ['income' => $prevIncome, 'expenses' => $prevExpenses, 'balance' => $prevIncome - $prevExpenses];

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