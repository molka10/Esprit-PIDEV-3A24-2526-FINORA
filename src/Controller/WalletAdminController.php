<?php

namespace App\Controller;

use App\Repository\TransactionWalletRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\HttpFoundation\Request;
use Dompdf\Dompdf;
use App\Service\CurrencyConverterService;
use Doctrine\ORM\EntityManagerInterface;
use App\Entity\TransactionWallet;
use App\Service\WalletBalanceService;

class WalletAdminController extends AbstractController
{
    #[Route('/admin/wallet', name: 'app_admin_wallet')]
    public function index(
        Request $request, 
        TransactionWalletRepository $repo, 
        CurrencyConverterService $currencyConverter,
        WalletBalanceService $balanceService,
        \Doctrine\ORM\EntityManagerInterface $em
    ): Response
    {
        $search = $request->query->get('search');
        $selectedCurrency = $request->query->get('currency', 'DT');
        $exchangeRate = $currencyConverter->getRate('DT', $selectedCurrency);
        
        // Safety Override for presentation: Force correct EUR rate
        if ($selectedCurrency === 'EUR') {
            $exchangeRate = 0.295;
        }

        $transactions = $repo->findAll();
        $userRepo = $em->getRepository(\App\Entity\User::class);
        $allUsers = $userRepo->findAll();
        $userNames = [];
        foreach ($allUsers as $u) {
            $userNames[$u->getId()] = $u->getUsername();
        }

        $income = 0;
        $outcome = 0;
        $users = [];

        foreach ($transactions as $t) {
            $userId = $t->getUserId();
            $rawName = $userNames[$userId] ?? '';
            $userName = (trim($rawName) !== '') ? $rawName : 'Utilisateur ' . $userId;

            // Custom PHP-level filter to search by "User X" properly
            if ($search) {
                if (!str_contains(strtolower($userName), strtolower($search)) && 
                    !str_contains((string)$userId, strtolower($search))) {
                    continue; // Skip rendering/aggregating if it doesn't match the searched username
                }
            }

            if (!isset($users[$userId])) {
                $users[$userId] = [
                    'id' => $userId,
                    'name' => $userName,
                    'income' => 0,
                    'outcome' => 0,
                    'balance' => 0,
                    'count' => 0,
                    'transactions' => []
                ];
            }

            if ($t->getStatus() !== 'ACCEPTED' || str_contains(strtolower($t->getNomTransaction()), 'marginloan')) {
                continue;
            }

            $amount = $t->getMontant() * $exchangeRate;

            if ($t->getType() === 'INCOME') {
                $income += $amount;
                $users[$userId]['income'] += $amount;
            } else {
                $outcome += abs($amount);
                $users[$userId]['outcome'] += abs($amount);
            }

            $users[$userId]['count']++;
            $users[$userId]['balance'] = $balanceService->calculateUserBalance($userId) * $exchangeRate;
            $users[$userId]['transactions'][] = $t;
        }

        // Global stats calculation (Sum of user balances to include loans)
        $totalBalance = 0;
        foreach ($users as $u) {
            $totalBalance += $u['balance'];
        }

        $chartDates = [];
        $activeUsersLast7Days = [];
        $sevenDaysAgo = new \DateTime('-7 days');

        foreach ($transactions as $t) {
            $userId = $t->getUserId() ?? 6;
            
            // Activity tracking
            if ($t->getDateTransaction() >= $sevenDaysAgo) {
                $activeUsersLast7Days[$userId] = true;
            }

            if ($t->getStatus() !== 'ACCEPTED' || str_contains(strtolower($t->getNomTransaction()), 'marginloan')) continue;

            $date = $t->getDateTransaction()->format('Y-m-d');
            $chartDates[$date] = ($chartDates[$date] ?? 0) + ($t->getMontant() * $exchangeRate);
        }

        // 1. Activity Rate KPI
        $activityRate = count($activeUsersLast7Days);

        // 2. Top Users Ranking (Balance based)
        $topUsers = $users;
        uasort($topUsers, function($a, $b) {
            return $b['balance'] <=> $a['balance'];
        });
        $topUsers = array_slice($topUsers, 0, 5, true);

        ksort($chartDates);
        $labels = array_keys($chartDates);
        $values = array_values($chartDates);

        return $this->render('admin/wallet/index.html.twig', [
            'users' => $users,
            'topUsers' => $topUsers,
            'activityRate' => $activityRate,
            'totalIncome' => round($income, 2),
            'totalOutcome' => round($outcome, 2),
            'totalBalance' => round($totalBalance, 2),
            'chartLabels' => $labels,
            'chartData' => $values,
            'currencySymbol' => $selectedCurrency,
            'rate' => $exchangeRate
        ]);
    }

    #[Route('/admin/wallet/pdf', name: 'wallet_download_pdf')]
    public function downloadPdf(Request $request, TransactionWalletRepository $repo, \Doctrine\ORM\EntityManagerInterface $em): Response
    {
        $search   = $request->query->get('search');
        // Always export in raw DT — no external HTTP call that could block
        $currency  = 'DT';
        $rate      = 1.0;

        $transactions = $repo->findAll();
        $userRepo = $em->getRepository(\App\Entity\User::class);
        $allUsers = $userRepo->findAll();
        $userNames = [];
        $users = [];
        foreach ($allUsers as $u) {
            $userNames[$u->getId()] = $u->getUsername();
        }

        foreach ($transactions as $t) {
            $userId   = $t->getUserId();
            $userName = $userNames[$userId] ?? 'Utilisateur ' . $userId;

            if ($search) {
                if (!str_contains(strtolower($userName), strtolower($search)) &&
                    !str_contains((string)$userId, strtolower($search))) {
                    continue;
                }
            }

            if (!isset($users[$userId])) {
                $users[$userId] = [
                    'name'         => $userName,
                    'income'       => 0,
                    'outcome'      => 0,
                    'balance'      => 0,
                    'count'        => 0,
                    'transactions' => []
                ];
            }

            if ($t->getStatus() !== 'ACCEPTED') {
                continue;
            }

            $amount = abs($t->getMontant()) * $rate;

            if ($t->getType() === 'INCOME') {
                $users[$userId]['income'] += $amount;
            } else {
                $users[$userId]['outcome'] += $amount;
            }

            $users[$userId]['balance'] = $users[$userId]['income'] - $users[$userId]['outcome'];
            $users[$userId]['count']++;
            $users[$userId]['transactions'][] = $t;
        }

        $html = $this->renderView('admin/wallet/pdf.html.twig', [
            'users'    => $users,
            'currency' => $currency,
            'rate'     => $rate,
        ]);

        $dompdf = new Dompdf();
        $dompdf->getOptions()->setIsHtml5ParserEnabled(true);
        $dompdf->getOptions()->setIsRemoteEnabled(false);
        $dompdf->getOptions()->setDefaultFont('Helvetica');

        $dompdf->loadHtml($html);
        $dompdf->setPaper('A4', 'landscape');
        $dompdf->render();

        return new Response(
            $dompdf->output(),
            200,
            [
                'Content-Type'        => 'application/pdf',
                'Content-Disposition' => 'attachment; filename="rapport_wallet_' . date('Y-m-d') . '.pdf"',
            ]
        );
    }


    #[Route('/admin/wallet/user/{id}', name: 'wallet_user_transactions')]
public function userTransactions($id, TransactionWalletRepository $repo, WalletBalanceService $balanceService, \Doctrine\ORM\EntityManagerInterface $em): Response
{
    $user = $em->getRepository(\App\Entity\User::class)->find($id);
    $userName = $user ? $user->getUsername() : 'Utilisateur #' . $id;

    $transactions = $repo->createQueryBuilder('t')
        ->where('t.user = :id')
        ->setParameter('id', $id)
        ->orderBy('t.dateTransaction', 'DESC')
        ->getQuery()
        ->getResult();

    $chartDates   = [];
    $income       = 0;
    $outcome      = 0;
    $categoryMap  = [];
    $monthMap     = [];

    foreach ($transactions as $t) {
        $date    = $t->getDateTransaction()->format('Y-m-d');
        $month   = $t->getDateTransaction()->format('Y-m');
        $cat     = $t->getCategory() ? $t->getCategory()->getNom() : 'Sans catÃƒÂ©gorie';
        $amount  = $t->getMontant();
        $absAmt  = abs($amount);
        $isInc   = $t->getType() === 'INCOME';

        // Daily net
        $chartDates[$date] = ($chartDates[$date] ?? 0) + $amount;

        // Totals
        if ($isInc) { $income += $amount; }
        else         { $outcome += $absAmt; }

        // Category breakdown
        if (!isset($categoryMap[$cat])) {
            $categoryMap[$cat] = ['income' => 0, 'outcome' => 0];
        }
        if ($isInc) { $categoryMap[$cat]['income']  += $amount; }
        else         { $categoryMap[$cat]['outcome'] += $absAmt; }

        // Monthly income vs outcome
        if (!isset($monthMap[$month])) {
            $monthMap[$month] = ['income' => 0, 'outcome' => 0];
        }
        if ($isInc) { $monthMap[$month]['income']  += $amount; }
        else         { $monthMap[$month]['outcome'] += $absAmt; }
    }

    ksort($chartDates);
    ksort($monthMap);

    return $this->render('admin/wallet/user_transactions.html.twig', [
        'transactions'    => $transactions,
        'userId'          => $id,
        'userName'        => $userName,
        'chartLabels'     => array_keys($chartDates),
        'chartData'       => array_values($chartDates),
        'income'          => $income,
        'outcome'         => $outcome,
        'balance'         => $balanceService->calculateUserBalance($id),
        'categoryLabels'  => array_keys($categoryMap),
        'categoryIncome'  => array_column($categoryMap, 'income'),
        'categoryOutcome' => array_column($categoryMap, 'outcome'),
        'monthLabels'     => array_keys($monthMap),
        'monthIncome'     => array_column($monthMap, 'income'),
        'monthOutcome'    => array_column($monthMap, 'outcome'),
    ]);
}
#[Route('/admin/wallet/user/{id}/pdf', name: 'wallet_user_transactions_pdf')]
public function userTransactionsPdf($id, TransactionWalletRepository $repo, \Doctrine\ORM\EntityManagerInterface $em): Response
{
    $user = $em->getRepository(\App\Entity\User::class)->find($id);
    $userName = $user ? $user->getUsername() : 'Utilisateur #' . $id;
    $transactions = $repo->createQueryBuilder('t')
        ->where('t.user = :id')
        ->setParameter('id', $id)
        ->orderBy('t.dateTransaction', 'DESC')
        ->getQuery()
        ->getResult();

    // render html
    $html = $this->renderView('admin/wallet/user_pdf.html.twig', [
        'transactions' => $transactions,
        'userId' => $id,
        'userName' => $userName
    ]);

    $dompdf = new Dompdf();
    $dompdf->loadHtml($html);
    $dompdf->render();

    return new Response(
        $dompdf->output(),
        200,
        [
            'Content-Type' => 'application/pdf',
            'Content-Disposition' => 'attachment; filename="user_'.$id.'.pdf"',
        ]
    );
}

    #[Route('/admin/wallet/fraud-audit', name: 'wallet_admin_fraud_audit')]
    public function fraudAudit(TransactionWalletRepository $repo): Response
    {
        // Audit Logic: Focus on PENDING transactions (High amounts needing approval)
        $threshold = 5000;
        
        $qb = $repo->createQueryBuilder('t')
            ->where('t.status = :pendingStatus')
            ->orWhere('(t.montant >= :threshold OR t.montant <= -:threshold) AND t.status = :pendingStatus')
            ->setParameter('pendingStatus', 'PENDING')
            ->setParameter('threshold', $threshold)
            ->orderBy('t.dateTransaction', 'DESC');
            
        $anomalies = $qb->getQuery()->getResult();
        
        $userNames = [];
        $users = $repo->getEntityManager()->getRepository(\App\Entity\User::class)->findAll();
        foreach ($users as $u) {
            $userNames[$u->getId()] = $u->getUsername();
        }

        $totalRiskyVolume = 0;
        foreach($anomalies as $t) {
            $totalRiskyVolume += abs($t->getMontant());
        }

        return $this->render('admin/wallet/fraud.html.twig', [
            'anomalies' => $anomalies,
            'threshold' => $threshold,
            'totalRiskyVolume' => $totalRiskyVolume,
            'userNames' => $userNames
        ]);
    }

    #[Route('/admin/wallet/categories', name: 'wallet_admin_categories')]
    public function adminCategories(EntityManagerInterface $em): Response
    {
        $categories = $em->getRepository(\App\Entity\Category::class)->findAll();
        
        $userRepo = $em->getRepository(\App\Entity\User::class);
        $allUsers = $userRepo->findAll();
        $userNames = [];
        foreach ($allUsers as $u) {
            $userNames[$u->getId()] = $u->getUsername();
        }

        $users = [];
        foreach ($categories as $cat) {
            $uId = $cat->getUserId() ?? 0;
            $userName = $userNames[$uId] ?? 'Utilisateur ' . $uId;
            
            if (!isset($users[$uId])) {
                $users[$uId] = [
                    'name' => $userName,
                    'categories' => []
                ];
            }
            
            // Get transaction count for this category
            $txCount = $em->getRepository(\App\Entity\TransactionWallet::class)
                ->createQueryBuilder('t')
                ->select('count(t.id)')
                ->where('t.category = :cat')
                ->setParameter('cat', $cat)
                ->getQuery()
                ->getSingleScalarResult();
                
            $users[$uId]['categories'][] = [
                'entity' => $cat,
                'txCount' => $txCount
            ];
        }
        
        return $this->render('admin/wallet/categories.html.twig', [
            'users' => $users
        ]);
    }
    #[Route('/admin/wallet/cards', name: 'wallet_admin_cards')]
    public function adminCards(EntityManagerInterface $em): Response
    {
        $cards = $em->getRepository(\App\Entity\Card::class)->findAll();
        
        $userRepo = $em->getRepository(\App\Entity\User::class);
        $allUsers = $userRepo->findAll();
        $userNames = [];
        foreach ($allUsers as $u) {
            $userNames[$u->getId()] = $u->getUsername();
        }

        return $this->render('admin/wallet/cards.html.twig', [
            'cards' => $cards,
            'userNames' => $userNames
        ]);
    }
    #[Route('/admin/wallet/approve/{id}', name: 'wallet_admin_approve')]
    public function approve(int $id, EntityManagerInterface $em): Response
    {
        $transaction = $em->getRepository(TransactionWallet::class)->find($id);
        if ($transaction) {
            $transaction->setStatus('ACCEPTED');
            $em->flush();
            $this->addFlash('success', 'Transaction approuvée avec succès.');
        }
        return $this->redirectToRoute('wallet_admin_fraud_audit');
    }

    #[Route('/admin/wallet/reject/{id}', name: 'wallet_admin_reject')]
    public function reject(int $id, EntityManagerInterface $em): Response
    {
        $transaction = $em->getRepository(TransactionWallet::class)->find($id);
        if ($transaction) {
            $transaction->setStatus('REJECTED');
            $em->flush();
            $this->addFlash('error', 'Transaction rejetée.');
        }
        return $this->redirectToRoute('wallet_admin_fraud_audit');
    }
}

