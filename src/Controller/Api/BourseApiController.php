<?php

namespace App\Controller\Api;

use App\Repository\ActionRepository;
use App\Repository\TransactionBourseRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/api/bourse')]
class BourseApiController extends AbstractController
{
    #[Route('/chart/{symbol}', name: 'api_bourse_chart_ohlc', methods: ['GET'])]
    public function getOhlcData(
        string $symbol,
        ActionRepository $actionRepo,
        TransactionBourseRepository $transactionRepo
    ): JsonResponse {
        $action = $actionRepo->findOneBy(['symbole' => $symbol]);

        if (!$action) {
            return new JsonResponse(['error' => 'Action non trouvée'], 404);
        }

        // Récupérer les transactions groupées par jour
        $transactions = $transactionRepo->createQueryBuilder('t')
            ->where('t.action = :action')
            ->setParameter('action', $action)
            ->orderBy('t.dateTransaction', 'ASC')
            ->getQuery()
            ->getResult();

        $dailyGroups = [];
        foreach ($transactions as $t) {
            $day = $t->getDateTransaction()->format('Y-m-d');
            $dailyGroups[$day][] = $t->getPrixUnitaire();
        }

        $ohlcData = [];
        foreach ($dailyGroups as $day => $prices) {
            $ohlcData[] = [
                'x' => $day, // Date
                'y' => [
                    $prices[0], // Open
                    max($prices), // High
                    min($prices), // Low
                    end($prices) // Close
                ]
            ];
        }

        return new JsonResponse([
            'symbol' => $symbol,
            'name' => $action->getNomEntreprise(),
            'data' => $ohlcData
        ]);
    }

    #[Route('/sentiment', name: 'api_bourse_sentiment', methods: ['GET'])]
    public function getMarketSentiment(TransactionBourseRepository $transactionRepo): JsonResponse
    {
        $transactions = $transactionRepo->findAll();
        $buys = 0;
        $sells = 0;

        foreach ($transactions as $t) {
            if ($t->getTypeTransaction() === 'ACHAT') $buys++;
            else $sells++;
        }

        $total = max(1, $buys + $sells);
        
        return new JsonResponse([
            'bullish' => round(($buys / $total) * 100),
            'bearish' => round(($sells / $total) * 100),
            'total_volume_count' => count($transactions)
        ]);
    }
}
