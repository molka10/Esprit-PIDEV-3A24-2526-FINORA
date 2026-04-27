<?php

namespace App\Controller;

use Dompdf\Dompdf;
use App\Entity\Action;
use App\Entity\TransactionBourse;
use App\Repository\ActionRepository;
use App\Repository\TransactionBourseRepository;
use App\Service\WalletBalanceService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class MarketController extends AbstractController
{
    public function __construct(
        private \App\Service\TransactionService $transactionService,
        private WalletBalanceService $walletBalanceService
    ) {}
    // =========================
    // 🔥 MARKET
    // =========================
    #[Route('/market', name: 'app_market')]
    public function index(
        ActionRepository $repo, 
        EntityManagerInterface $em, 
        \App\Service\FinancialNewsService $financialNewsService,
        \App\Service\SmartLearningService $smartLearningService
    ): Response
    {
        $user         = $this->getUser();
        $walletBalance = $user ? $this->walletBalanceService->calculateUserBalance($user->getId()) : 0.0;

        $news = $em->getRepository(\App\Entity\ActionNews::class)->findBy([], ['dateAjout' => 'DESC'], 5);
        $globalNews = $financialNewsService->getLatestGlobalNews(4);

        $topTraders = [
            ['name' => 'Amine Trader', 'roi' => '+24.5%', 'assets' => 'AAPL, BTC', 'avatar' => 'AM'],
            ['name' => 'Sarah Invest', 'roi' => '+18.2%', 'assets' => 'TSLA, ETH', 'avatar' => 'SI'],
            ['name' => 'Mourad Pro', 'roi' => '+15.7%', 'assets' => 'GOOGL, MSFT', 'avatar' => 'MP'],
        ];

        // 🧠 SMART RECOMMENDATIONS
        $recommendations = $user ? $smartLearningService->getRecommendations($user) : [];

        return $this->render('market/index.html.twig', [
            'actions'       => $repo->findAll(),
            'type'          => null,
            'walletBalance' => round($walletBalance, 2),
            'latest_news'   => $news,
            'global_news'   => $globalNews,
            'top_traders'   => $topTraders,
            'recommendations' => $recommendations
        ]);
    }

    // =========================
    // 🎭 SIMULATE MARKET NEWS (DYNAMIQUE)
    // =========================
    #[Route('/market/simulate-news', name: 'app_market_simulate_news')]
    public function simulateNews(ActionRepository $repo, EntityManagerInterface $em): Response
    {
        $actions = $repo->findAll();
        if (empty($actions)) {
            $this->addFlash('error', 'Aucune action disponible pour simuler la bourse.');
            return $this->redirectToRoute('app_market');
        }

        $action = $actions[array_rand($actions)];
        $impacts = [
            ['Scandale financier révélé ! L\'entreprise plonge.', -30],
            ['Hausse record des bénéfices trimestriels.', 15],
            ['Fusion surprise annoncée avec un concurrent.', 30],
            ['Problème de chaîne d\'approvisionnement.', -15],
            ['Lancement d\'un nouveau produit révolutionnaire.', 25]
        ];

        $randomEvent = $impacts[array_rand($impacts)];
        $titre = $randomEvent[0] . ' (' . $action->getNomEntreprise() . ')';
        $pourcentage = $randomEvent[1];

        // Create News
        $news = new \App\Entity\ActionNews();
        $news->setAction($action);
        $news->setTitre($titre);
        $news->setImpactPercent($pourcentage);

        // Modify Action Price
        $oldPrice = $action->getPrixUnitaire();
        $newPrice = $oldPrice + ($oldPrice * ($pourcentage / 100));
        // Prevent price dropping below 1
        $newPrice = max(1.0, $newPrice);
        
        $action->setPrixUnitaire(round($newPrice, 2));

        $em->persist($news);
        $em->flush();

        $couleur = $pourcentage > 0 ? 'success' : 'danger';
        $this->addFlash($couleur, '🔔 Breaking News : ' . $titre . ' Impact: ' . $pourcentage . '% !');
        return $this->redirectToRoute('app_market');
    }

    // =========================
    // 🏦 MARGIN LENDING (LEVIER)
    // =========================
    #[Route('/market/request-margin', name: 'app_market_margin', methods: ['POST'])]
    public function requestMargin(Request $request, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $montant = (float) $request->request->get('montant_marge', 500);

        // Security check
        $activeLoans = $em->getRepository(\App\Entity\MarginLoan::class)->findBy(['userId' => $user->getId(), 'statut' => 'ACTIF']);
        if (count($activeLoans) > 0) {
            $this->addFlash('warning', 'Vous avez déjà un emprunt sur marge actif. Remboursez-le (ou revendez vos actifs) pour l\'instant.');
            return $this->redirectToRoute('app_market');
        }

        $loan = new \App\Entity\MarginLoan();
        $loan->setUserId($user->getId());
        $loan->setMontantEmprunte($montant);

        $em->persist($loan);
        $em->flush();

        $this->addFlash('success', '🏦 Prêt sur Marge de ' . $montant . ' TND approuvé et ajouté à votre pouvoir d\'achat !');
        return $this->redirectToRoute('app_market');
    }

    // =========================
    // 🔥 ACHAT
    // =========================
    #[Route('/buy/{id}', name: 'app_buy')]
    public function buy(Action $action, Request $request): Response
    {
        if ($request->isMethod('POST')) {
            $quantite = (int) $request->request->get('quantite');

            try {
                $transaction = $this->transactionService->executerTrade('ACHAT', $action->getId(), $quantite, $this->getUser());
                return $this->redirectToRoute('app_market_confirmation', ['id' => $transaction->getId()]);

            } catch (\Exception $e) {
                // Flash message for the SweetAlert popup
                $this->addFlash('danger', '❌ ' . $e->getMessage());
                return $this->redirectToRoute('app_market');
            }
        }

        return $this->render('market/buy.html.twig', [
            'action'        => $action,
            'walletBalance' => $this->getUser() ? round($this->walletBalanceService->calculateUserBalance($this->getUser()->getId()), 2) : 0.0,
        ]);
    }

    // =========================
    // 🔥 VENTE (SÉCURISÉE)
    // =========================
    #[Route('/sell/{id}', name: 'app_sell')]
    public function sell(Action $action, Request $request): Response
    {
        if ($request->isMethod('POST')) {
            $quantite = (int) $request->request->get('quantite');

            try {
                $transaction = $this->transactionService->executerTrade('VENTE', $action->getId(), $quantite, $this->getUser());
                return $this->redirectToRoute('app_market_confirmation', ['id' => $transaction->getId()]);

            } catch (\Exception $e) {
                $this->addFlash('danger', '❌ ' . $e->getMessage());
                return $this->redirectToRoute('app_market');
            }
        }

        return $this->render('market/sell.html.twig', [
            'action'        => $action,
            'walletBalance' => $this->getUser() ? round($this->walletBalanceService->calculateUserBalance($this->getUser()->getId()), 2) : 0.0,
        ]);
    }

    // =========================
    // 🔥 CONFIRMATION (REÇU)
    // =========================
    #[Route('/market/confirmation/{id}', name: 'app_market_confirmation')]
    public function confirmation(TransactionBourse $transaction): Response
    {
        // Securiser la page de confirmation pour qu'elle appartienne a l'utilisateur actuel
        if ($transaction->getUser() !== $this->getUser()) {
            throw $this->createAccessDeniedException("Accès refusé.");
        }

        return $this->render('market/confirmation.html.twig', [
            'transaction'   => $transaction,
            'walletBalance' => round($this->walletBalanceService->calculateUserBalance($this->getUser()->getId()), 2),
        ]);
    }

    // =========================
    // 🔥 HISTORIQUE (PRO)
    // =========================
    #[Route('/history', name: 'app_history')]
    public function history(
        Request $request, 
        TransactionBourseRepository $repo,
        \App\Service\GamificationService $gamificationService,
        \App\Service\AiAssistantService $aiAssistantService
    ): Response
    {
        $user = $this->getUser();
        $page = max(1, $request->query->getInt('page', 1));
        $limit = 10;

        $qb = $repo->createQueryBuilder('t')
            ->leftJoin('t.action', 'a')
            ->addSelect('a')
            ->where('t.user = :user')
            ->setParameter('user', $user)
            ->orderBy('t.dateTransaction', 'DESC');

        // 🔥 PAGINATION
        $qb->setFirstResult(($page - 1) * $limit)
           ->setMaxResults($limit);

        $transactions = $qb->getQuery()->getResult();

        // 🔥 TOTAL (sans pagination)
        $totalItems = $repo->createQueryBuilder('t')
            ->select('COUNT(t.id)')
            ->where('t.user = :user')
            ->setParameter('user', $user)
            ->getQuery()
            ->getSingleScalarResult();
        
        $totalPages = ceil($totalItems / $limit);

        // 🔥 STATS
        $all = $repo->findBy(['user' => $user]);

        $totalInvesti = 0;
        $totalVendu = 0;
        $totalCommission = 0;

        foreach ($all as $t) {
            if ($t->getTypeTransaction() === 'ACHAT') {
                $totalInvesti += $t->getMontantTotal();
            } else {
                $totalVendu += $t->getMontantTotal();
            }
            $totalCommission += $t->getCommission() ?? 0;
        }

        // 🔥 VUE GLOBALE PORTEFEUILLE
        $portfolio = [];
        foreach ($all as $t) {
            $actionId = $t->getAction()->getId();
            if (!isset($portfolio[$actionId])) {
                $portfolio[$actionId] = ['qty' => 0, 'action' => $t->getAction(), 'totalCost' => 0];
            }
            if ($t->getTypeTransaction() === 'ACHAT') {
                $portfolio[$actionId]['qty'] += $t->getQuantite();
                $portfolio[$actionId]['totalCost'] += $t->getMontantTotal();
            } else {
                $portfolio[$actionId]['qty'] -= $t->getQuantite();
                // Avoid division by zero if qty becomes 0
            }
        }
        
        $valeurEstimee = 0;
        $actifsDetenus = 0;
        $simulatorData = [];

        foreach ($portfolio as $item) {
            if ($item['qty'] > 0) {
                $currentVal = $item['qty'] * $item['action']->getPrixUnitaire();
                $valeurEstimee += $currentVal;
                $actifsDetenus += $item['qty'];
                
                $simulatorData[] = [
                    'symbole' => $item['action']->getSymbole(),
                    'qty' => $item['qty'],
                    'currentPrice' => $item['action']->getPrixUnitaire(),
                    'totalCost' => $item['totalCost']
                ];
            }
        }

        // 🔥 DIVERSIFICATION & AI INSIGHTS
        $maxWeight = 0;
        $diversificationScore = 0;
        if ($valeurEstimee > 0) {
            foreach ($portfolio as $item) {
                if ($item['qty'] > 0) {
                    $weight = ($item['qty'] * $item['action']->getPrixUnitaire() / $valeurEstimee) * 100;
                    if ($weight > $maxWeight) $maxWeight = $weight;
                }
            }
            // Score: 100 is perfectly diversified, 0 is fully concentrated on 1 asset
            $diversificationScore = max(0, 100 - ($maxWeight - (100 / count($portfolio))));
        }

        $aiInsights = "Analyse en attente...";
        try {
            $aiInsights = $aiAssistantService->generatePortfolioAnalysis([
                'valeur_totale' => $valeurEstimee,
                'actifs_detenus' => $actifsDetenus,
                'nb_actions_differentes' => count($portfolio),
                'concentration_max' => round($maxWeight, 2),
                'total_investi' => $totalInvesti,
                'profit_reel' => $totalVendu + $valeurEstimee - $totalInvesti
            ]);
        } catch (\Exception $e) {
            $aiInsights = "L'IA est momentanément indisponible, mais votre diversification est de " . round($diversificationScore) . "%.";
        }

        // 🔥 ALLOCATION & PERFORMANCE DATA
        $allocationData = [];
        foreach ($portfolio as $item) {
            if ($item['qty'] > 0) {
                $allocationData[] = [
                    'label' => $item['action']->getSymbole(),
                    'value' => round(($item['qty'] * $item['action']->getPrixUnitaire() / $valeurEstimee) * 100, 1)
                ];
            }
        }

        // Mock Performance Curve (from -5% to current profit/loss)
        $currentROI = $totalInvesti > 0 ? (($valeurEstimee - $totalInvesti) / $totalInvesti) * 100 : 0;
        $performanceCurve = [];
        for ($i = 1; $i <= 10; $i++) {
            $performanceCurve[] = round(-5 + ($i * ($currentROI + 5) / 10) + rand(-2, 2), 2);
        }

        return $this->render('market/history.html.twig', [
            'transactions' => $transactions,
            'currentPage' => $page,
            'totalPages' => $totalPages,
            'badges' => $gamificationService->getUserBadges($user),
            'simulatorData' => $simulatorData,
            'ai_insights' => $aiInsights,
            'diversification_score' => round($diversificationScore),
            'max_weight' => round($maxWeight, 2),
            'allocation_data' => $allocationData,
            'performance_data' => $performanceCurve,
            'stats' => [
                'total_investi'   => $totalInvesti,
                'total_vendu'     => $totalVendu,
                'commissions'     => $totalCommission,
                'nb_transactions' => $totalItems,
                'valeur_estimee'  => $valeurEstimee,
                'actifs_detenus'  => $actifsDetenus
            ]
        ]);
    }

    // =========================
    // 📄 PDF (RELEVÉ BANCAIRE SÉCURISÉ)
    // =========================
    #[Route('/history/pdf', name: 'app_history_pdf')]
    public function pdf(TransactionBourseRepository $repo): Response
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        // Sécurité critique : Uniquement les transactions de NOTRE utilisateur
        $transactions = $repo->findBy(
            ['user' => $user],
            ['dateTransaction' => 'DESC']
        );

        // Récupérer le vrai solde pour le RIB
        $walletBalance = round($this->walletBalanceService->calculateUserBalance($user->getId()), 2);

        $html = $this->renderView('market/pdf.html.twig', [
            'transactions'  => $transactions,
            'walletBalance' => $walletBalance,
            'user'          => $user
        ]);

        $dompdf = new Dompdf();
        $dompdf->setOptions(new \Dompdf\Options(['isHtml5ParserEnabled' => true, 'isRemoteEnabled' => true]));
        $dompdf->loadHtml($html);
        $dompdf->setPaper('A4', 'portrait');
        $dompdf->render();

        return new Response(
            $dompdf->output(),
            200,
            [
                'Content-Type' => 'application/pdf',
                'Content-Disposition' => 'inline; filename="Releve_Bancaire_Finora_ID'.$user->getId().'.pdf"'
            ]
        );
    }
}