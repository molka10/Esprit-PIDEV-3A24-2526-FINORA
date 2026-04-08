<?php

namespace App\Controller;

use App\Repository\BourseRepository;
use App\Repository\ActionRepository;
use App\Repository\TransactionBourseRepository;
use App\Service\StatisticsService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class DashboardController extends AbstractController
{
    public function __construct(
        private StatisticsService $statisticsService
    ) {}

    #[Route('/', name: 'app_home')]
    public function index(
        BourseRepository $bourseRepo,
        ActionRepository $actionRepo,
        TransactionBourseRepository $transactionRepo
    ): Response {

        $globalStats = $this->statisticsService->getGlobalStatistics();

        $evolutionChart = $this->statisticsService->getEvolutionChartData(30);
        $repartitionChart = $this->statisticsService->getRepartitionBySecteur();

        return $this->render('dashboard/index.html.twig', [
            'totalBourses' => $bourseRepo->count([]),
            'totalActions' => $actionRepo->count([]),
            'totalTransactions' => $transactionRepo->count([]),

            'chart_evolution_labels' => $evolutionChart['labels'],
            'chart_evolution_data' => $evolutionChart['data'],

            'chart_repartition_labels' => $repartitionChart['labels'],
            'chart_repartition_data' => $repartitionChart['data'],

            'actions' => $actionRepo->findBy([], ['id' => 'DESC'], 5),
        ]);
    }
}