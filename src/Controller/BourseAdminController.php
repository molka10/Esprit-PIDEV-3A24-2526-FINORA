<?php

namespace App\Controller;

use App\Repository\BourseRepository;
use App\Repository\ActionRepository;
use App\Repository\TransactionBourseRepository;
use App\Service\StatisticsService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class BourseAdminController extends AbstractController
{
    public function __construct(
        private StatisticsService $statisticsService
    ) {}

    #[Route('/admin/bourse', name: 'app_bourse_admin')]
    public function index(
        BourseRepository $bourseRepo,
        ActionRepository $actionRepo,
        TransactionBourseRepository $transactionRepo
    ): Response {

        $gainPerte = $this->statisticsService->getGainPerte();
        $evolutionChart = $this->statisticsService->getEvolutionChartData(90);
        $repartitionChart = $this->statisticsService->getRepartitionBySecteur();

        return $this->render('admin/bourse/dashboard.html.twig', [
            // Core counters
            'totalBourses' => $bourseRepo->count([]),
            'totalActions' => $actionRepo->count([]),
            'totalTransactions' => $transactionRepo->count([]),
            'volumeTotal' => $transactionRepo->getStatistics()['volume_total'],
            
            // Gain/Perte
            'gainPerte' => $gainPerte,

            // Charts
            'chart_evolution_labels' => $evolutionChart['labels'],
            'chart_evolution_data' => $evolutionChart['data'],
            'chart_repartition_labels' => $repartitionChart['labels'],
            'chart_repartition_data' => $repartitionChart['data'],

            // Lists
            'recentActions' => $actionRepo->findBy([], ['id' => 'DESC'], 5),
            'recentTransactions' => $transactionRepo->findRecent(6),
        ]);
    }

    #[Route('/admin/bourse/transactions', name: 'app_admin_bourse_transactions')]
    public function transactions(
        \Symfony\Component\HttpFoundation\Request $request,
        TransactionBourseRepository $transactionRepo
    ): Response {
        $page = $request->query->getInt('page', 1);
        $type = $request->query->get('type'); // ACHAT, VENTE or null

        $pagination = $transactionRepo->findPaginated($page, 10, $type);

        return $this->render('admin/bourse/transactions.html.twig', [
            'transactions' => $pagination['items'],
            'total' => $pagination['total'],
            'page' => $pagination['page'],
            'pages' => $pagination['pages'],
            'limit' => $pagination['limit'],
            'currentType' => $type, // Pass to template for active filter state
        ]);
    }
}