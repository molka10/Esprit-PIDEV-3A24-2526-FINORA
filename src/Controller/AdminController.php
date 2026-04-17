<?php

namespace App\Controller;

use App\Repository\TransactionWalletRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\HttpFoundation\Request;
use Dompdf\Dompdf;
use App\Service\CurrencyConverterService;

class AdminController extends AbstractController
{
    #[Route('/admin', name: 'app_admin')]
    public function index(Request $request, TransactionWalletRepository $repo, CurrencyConverterService $currencyConverter): Response
    {
        $search = $request->query->get('search');
        $selectedCurrency = $request->query->get('currency', 'DT');
        $exchangeRate = $currencyConverter->getRate('DT', $selectedCurrency);

        $transactions = $repo->findAll();

        $income = 0;
        $outcome = 0;
        $users = [];

        foreach ($transactions as $t) {
            $userId = $t->getUserId();
            $userName = 'User ' . $userId;

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

            $amount = $t->getMontant() * $exchangeRate;

            if ($t->getType() === 'INCOME') {
                $income += $amount;
                $users[$userId]['income'] += $amount;
            } else {
                $outcome += abs($amount);
                $users[$userId]['outcome'] += abs($amount);
            }

            $users[$userId]['count']++;
            $users[$userId]['balance'] = $users[$userId]['income'] - $users[$userId]['outcome'];

            $users[$userId]['transactions'][] = $t;
        }

        $chartDates = [];

        // Now build chart only from filtered users data map
        foreach ($users as $u) {
            foreach ($u['transactions'] as $t) {
                $date = $t->getDateTransaction()->format('Y-m-d');

                if (!isset($chartDates[$date])) {
                    $chartDates[$date] = 0;
                }

                $amount = $t->getMontant() * $exchangeRate;
                $chartDates[$date] += $amount;
            }
        }

        ksort($chartDates);

        $labels = array_keys($chartDates);
        $values = array_values($chartDates);

        return $this->render('admin/index.html.twig', [
            'transactions' => $transactions,
            'users' => $users,
            'totalIncome' => round($income, 2),
            'totalOutcome' => round($outcome, 2),
            'totalBalance' => round($income - $outcome, 2),
            'chartLabels' => $labels,
            'chartData' => $values,
            'currencySymbol' => $selectedCurrency,
            'rate' => $exchangeRate
        ]);
    }

    #[Route('/admin/pdf', name: 'download_pdf')]
    public function downloadPdf(Request $request, TransactionWalletRepository $repo): Response
    {
        $search = $request->query->get('search');
        $transactions = $repo->findAll();

        $users = [];

        foreach ($transactions as $t) {
            $userId = $t->getUserId();
            $userName = 'User ' . $userId;

            if ($search) {
                if (!str_contains(strtolower($userName), strtolower($search)) && 
                    !str_contains((string)$userId, strtolower($search))) {
                    continue;
                }
            }

            if (!isset($users[$userId])) {
                $users[$userId] = [
                    'name' => $userName,
                    'income' => 0,
                    'outcome' => 0,
                    'balance' => 0,
                    'count' => 0,
                    'transactions' => []
                ];
            }

            if ($t->getType() === 'INCOME') {
                $users[$userId]['income'] += $t->getMontant();
            } else {
                $users[$userId]['outcome'] += abs($t->getMontant());
            }

            $users[$userId]['balance'] = $users[$userId]['income'] - $users[$userId]['outcome'];
            $users[$userId]['count']++;
            $users[$userId]['transactions'][] = $t;
        }

        $html = $this->renderView('admin/pdf.html.twig', [
            'users' => $users
        ]);

        $dompdf = new Dompdf();
        $dompdf->loadHtml($html);
        $dompdf->render();

        return new Response(
            $dompdf->output(),
            200,
            [
                'Content-Type' => 'application/pdf',
                'Content-Disposition' => 'attachment; filename="admin.pdf"',
            ]
        );
    }


    #[Route('/admin/user/{id}', name: 'user_transactions')]
public function userTransactions($id, TransactionWalletRepository $repo): Response
{
    $transactions = $repo->createQueryBuilder('t')
        ->where('t.userId = :id')
        ->setParameter('id', $id)
        ->orderBy('t.dateTransaction', 'DESC')
        ->getQuery()
        ->getResult();

    // ✅ chart data (par date)
    $chartDates = [];
    $income = 0;
    $outcome = 0;

    foreach ($transactions as $t) {
        $date = $t->getDateTransaction()->format('Y-m-d');

        if (!isset($chartDates[$date])) {
            $chartDates[$date] = 0;
        }

        $chartDates[$date] += $t->getMontant();

        // donut
        if ($t->getType() === 'INCOME') {
            $income += $t->getMontant();
        } else {
            $outcome += abs($t->getMontant());
        }
    }

    ksort($chartDates);

    return $this->render('admin/user_transactions.html.twig', [
        'transactions' => $transactions,
        'userId' => $id,
        'chartLabels' => array_keys($chartDates),
        'chartData' => array_values($chartDates),
        'income' => $income,
        'outcome' => $outcome
    ]);
}
#[Route('/admin/user/{id}/pdf', name: 'user_transactions_pdf')]
public function userTransactionsPdf($id, TransactionWalletRepository $repo): Response
{
    $transactions = $repo->createQueryBuilder('t')
        ->where('t.userId = :id')
        ->setParameter('id', $id)
        ->orderBy('t.dateTransaction', 'DESC')
        ->getQuery()
        ->getResult();

    // render html
    $html = $this->renderView('admin/user_pdf.html.twig', [
        'transactions' => $transactions,
        'userId' => $id
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

#[Route('/admin/search-users', name: 'search_users')]
public function searchUsers(Request $request, TransactionWalletRepository $repo, CurrencyConverterService $currencyConverter): Response
{
    $search = $request->query->get('search');
    $selectedCurrency = $request->query->get('currency', 'DT');
    $exchangeRate = $currencyConverter->getRate('DT', $selectedCurrency);

    $transactions = $repo->findAll();
    
    $users = [];

    foreach ($transactions as $t) {
        $userId = $t->getUserId();
        $userName = 'User ' . $userId;

        if ($search) {
            if (!str_contains(strtolower($userName), strtolower($search)) && 
                !str_contains((string)$userId, strtolower($search))) {
                continue;
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
            ];
        }

        $amount = $t->getMontant() * $exchangeRate;

        if ($t->getType() === 'INCOME') {
            $users[$userId]['income'] += $amount;
        } else {
            $users[$userId]['outcome'] += abs($amount);
        }

        $users[$userId]['count']++;
        $users[$userId]['balance'] = $users[$userId]['income'] - $users[$userId]['outcome'];
    }

    return $this->render('admin/_table_rows.html.twig', [
        'users' => $users
    ]);
}

    #[Route('/admin/fraud-audit', name: 'admin_fraud_audit')]
    public function fraudAudit(TransactionWalletRepository $repo): Response
    {
        // Métier Logic: Detection of Fraud or Anomalies (Very high transactions)
        $threshold = 5000;
        
        $qb = $repo->createQueryBuilder('t')
            ->where('t.montant >= :threshold OR t.montant <= -:threshold')
            ->setParameter('threshold', $threshold)
            ->orderBy('t.montant', 'DESC');
            
        $anomalies = $qb->getQuery()->getResult();
        
        $totalRiskyVolume = 0;
        foreach($anomalies as $t) {
            $totalRiskyVolume += abs($t->getMontant());
        }

        return $this->render('admin/fraud.html.twig', [
            'anomalies' => $anomalies,
            'threshold' => $threshold,
            'totalRiskyVolume' => $totalRiskyVolume
        ]);
    }
}
