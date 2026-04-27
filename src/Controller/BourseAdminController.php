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

        // --- Core Counters (Needed for advanced logic) ---
        $totalBourses = $bourseRepo->count([]);
        $totalActions = $actionRepo->count([]);
        $volumeTotal = $transactionRepo->getStatistics()['volume_total'];

        // --- Advanced Intelligence Logic ---
        $allTransactions = $transactionRepo->findAll();
        $buyCount = 0; $sellCount = 0; $totalCommissions = 0;
        foreach ($allTransactions as $t) {
            if ($t->getTypeTransaction() === 'ACHAT') $buyCount++;
            else $sellCount++;
            $totalCommissions += $t->getCommission();
        }
        $sentiment = [
            'buys' => $buyCount,
            'sells' => $sellCount,
            'total' => max(1, $buyCount + $sellCount)
        ];

        // Whale transactions (Largest ones)
        $whales = $transactionRepo->findBy([], ['montantTotal' => 'DESC'], 6);

        // Top Traders (By Volume)
        $qbTraders = $transactionRepo->createQueryBuilder('t')
            ->select('u.username, SUM(t.montantTotal) as totalVolume')
            ->join('t.user', 'u')
            ->groupBy('u.id')
            ->orderBy('totalVolume', 'DESC')
            ->setMaxResults(5)
            ->getQuery();
        $topTraders = $qbTraders->getResult();

        // Top Securities (By Volume)
        $qbActions = $transactionRepo->createQueryBuilder('t')
            ->select('a.symbole, a.nomEntreprise, SUM(t.montantTotal) as totalVolume')
            ->join('t.action', 'a')
            ->groupBy('a.id')
            ->orderBy('totalVolume', 'DESC')
            ->setMaxResults(5)
            ->getQuery();
        $topActions = $qbActions->getResult();

        // --- 🌡️ Market Health Score ---
        $uniqueTradersCount = count($transactionRepo->createQueryBuilder('t')
            ->select('DISTINCT(t.user)')
            ->getQuery()->getResult());
        $distinctActionsTraded = count($transactionRepo->createQueryBuilder('t')
            ->select('DISTINCT(t.action)')
            ->getQuery()->getResult());
        
        // Simple formula: (Active Actions % * 40) + (Traders Participation * 30) + (Diversity * 30)
        $healthScore = min(100, round(
            (($totalActions / max(1, $totalBourses * 10)) * 40) + 
            (($uniqueTradersCount / 10) * 30) + 
            (($distinctActionsTraded / max(1, $totalActions)) * 30)
        ));

        // --- 📊 Trading Heatmap (7 Days x 24 Hours) ---
        $heatmapData = [];
        for ($d = 0; $d < 7; $d++) {
            $dayData = [];
            for ($h = 0; $h < 24; $h++) { $dayData[$h] = 0; }
            $heatmapData[$d] = $dayData;
        }
        foreach ($allTransactions as $t) {
            $day = (int) $t->getDateTransaction()->format('w'); // 0-6
            $hour = (int) $t->getDateTransaction()->format('G'); // 0-23
            $heatmapData[$day][$hour]++;
        }

        // --- 🚨 Anomaly Detection ---
        $anomalies = [];
        // 1. Wash Trading Detection (Buy & Sell same action < 10 mins)
        foreach ($allTransactions as $t1) {
            foreach ($allTransactions as $t2) {
                if ($t1->getId() !== $t2->getId() && 
                    $t1->getUser() === $t2->getUser() && 
                    $t1->getAction() === $t2->getAction() && 
                    $t1->getTypeTransaction() !== $t2->getTypeTransaction()) {
                    
                    $diff = abs($t1->getDateTransaction()->getTimestamp() - $t2->getDateTransaction()->getTimestamp());
                    if ($diff < 600) { // 10 minutes
                        $anomalies[] = [
                            'type' => 'Wash Trading',
                            'user' => $t1->getUser()->getUsername(),
                            'action' => $t1->getAction()->getSymbole(),
                            'severity' => 'High',
                            'time' => $t1->getDateTransaction()->format('H:i')
                        ];
                        if (count($anomalies) >= 3) break 2;
                    }
                }
            }
        }
        // 2. High Impact Spike (> 40% of current volume)
        foreach ($whales as $w) {
            if ($volumeTotal > 0 && ($w->getMontantTotal() / $volumeTotal) > 0.4) {
                $anomalies[] = [
                    'type' => 'Volume Spike',
                    'user' => $w->getUser()->getUsername(),
                    'action' => $w->getAction()->getSymbole(),
                    'severity' => 'Medium',
                    'time' => $w->getDateTransaction()->format('H:i')
                ];
            }
        }

        return $this->render('admin/bourse/dashboard.html.twig', [
            // Core counters
            'totalBourses' => $totalBourses,
            'totalActions' => $totalActions,
            'totalTransactions' => count($allTransactions),
            'volumeTotal' => $volumeTotal,
            'totalCommissions' => $totalCommissions,
            
            // Intelligence
            'sentiment' => $sentiment,
            'whales' => $whales,
            'topTraders' => $topTraders,
            'topActions' => $topActions,
            'healthScore' => $healthScore,
            'heatmapData' => $heatmapData,
            'anomalies' => array_slice($anomalies, 0, 4),

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
        $type = $request->query->get('type');
        $tag = $request->query->get('tag');

        // --- Custom Filtering Logic for Tags ---
        $criteria = [];
        if ($type) $criteria['typeTransaction'] = $type;
        
        $pagination = $transactionRepo->findPaginated($page, 15, $type);
        $transactions = $pagination['items'];

        // --- Advanced Analytics for this Page ---
        $pageVolume = 0;
        $pageCommissions = 0;
        $activeTraders = [];
        
        // Calculate Global Avg for Risk Scoring
        $allStats = $transactionRepo->getStatistics();
        $globalAvg = $allStats['volume_total'] / max(1, $allStats['total']);

        foreach ($transactions as $t) {
            $pageVolume += $t->getMontantTotal();
            $pageCommissions += $t->getCommission();
            if ($t->getUser()) {
                $activeTraders[$t->getUser()->getId()] = $t->getUser()->getUsername();
            }

            // --- Risk Scoring Logic ---
            $riskScore = 0;
            $riskReasons = [];
            
            // 1. High Value Outlier
            if ($t->getMontantTotal() > ($globalAvg * 5)) {
                $riskScore += 40;
                $riskReasons[] = "Volume exceptionnel (>5x moy.)";
            }
            
            // 2. Round Numbers (Often bot-like)
            if ($t->getMontantTotal() > 0 && fmod($t->getMontantTotal(), 100) == 0) {
                $riskScore += 10;
                $riskReasons[] = "Montant rond suspect";
            }

            // 3. Commissions Check
            if ($t->getCommission() <= 0) {
                $riskScore += 20;
                $riskReasons[] = "Zéro commission détectée";
            }

            $t->riskScore = min(100, $riskScore);
            $t->riskReasons = $riskReasons;

            // --- 🎭 Investor Personas Logic ---
            $t->persona = 'Trader';
            $t->personaIcon = 'bi-person';
            $t->personaColor = 'secondary';

            if ($t->getMontantTotal() > ($globalAvg * 10)) {
                $t->persona = 'Baleine';
                $t->personaIcon = 'bi-water';
                $t->personaColor = 'primary';
            } elseif ($t->getQuantite() > 50) {
                $t->persona = 'Sniper';
                $t->personaIcon = 'bi-target';
                $t->personaColor = 'danger';
            } else {
                $t->persona = 'HODLer';
                $t->personaIcon = 'bi-shield-lock';
                $t->personaColor = 'success';
            }

            // --- Mock Portfolio for Popover (Simulated for Performance) ---
            $t->mockExposure = [
                ['label' => 'Tech', 'value' => rand(30, 70)],
                ['label' => 'Finance', 'value' => rand(10, 40)],
                ['label' => 'Energy', 'value' => rand(5, 20)],
            ];
        }

        return $this->render('admin/bourse/transactions.html.twig', [
            'transactions' => $transactions,
            'total' => $pagination['total'],
            'page' => $pagination['page'],
            'pages' => $pagination['pages'],
            'limit' => $pagination['limit'],
            'currentType' => $type,
            'currentTag' => $tag,
            
            // Summary Stats
            'summary' => [
                'pageVolume' => $pageVolume,
                'pageCommissions' => $pageCommissions,
                'tradersCount' => count($activeTraders),
                'globalAvg' => $globalAvg
            ]
        ]);
    }
}