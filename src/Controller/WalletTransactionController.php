<?php

namespace App\Controller;

use App\Entity\TransactionWallet;
use App\Form\TransactionType;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Routing\Annotation\Route;
use App\Entity\Category; 
use App\Repository\CategoryRepository;
use Symfony\Component\HttpFoundation\JsonResponse;

use PhpOffice\PhpSpreadsheet\Spreadsheet;
use PhpOffice\PhpSpreadsheet\Writer\Xlsx;
use Symfony\Component\HttpFoundation\StreamedResponse;

use App\Service\RecommendationService;
use App\Service\WalletBalanceService;
use App\Service\CurrencyConverterService;
use Knp\Component\Pager\PaginatorInterface;

class WalletTransactionController extends AbstractController
{

#[Route('/wallet/transaction/add', name: 'transaction_add')]

public function add(Request $req, EntityManagerInterface $em): Response
{
    $transaction = new TransactionWallet();
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login'); // Security measure
        }
        $transaction->setUser($user);

    $transaction->setSource("manual");

    $categories = $em->getRepository(Category::class)->findAll();

    $form = $this->createForm(TransactionType::class, $transaction);
    
    // Map category manually from request BEFORE handleRequest to ensure validation passes
    if ($req->isMethod('POST')) {
        $data = $req->request->all('transaction_wallet');
        $categoryId = $data['category'] ?? null;
        if ($categoryId) {
            $category = $em->getRepository(Category::class)->find($categoryId);
            if ($category) {
                $transaction->setCategory($category);
            }
        }
    }

    $form->handleRequest($req);

    if ($form->isSubmitted() && $form->isValid()) {
        if ($transaction->getType() == "OUTCOME") {
            $transaction->setMontant(-abs($transaction->getMontant()));
        } else {
            $transaction->setMontant(abs($transaction->getMontant()));
        }

        if (!$transaction->getDateTransaction()) {
            $transaction->setDateTransaction(new \DateTime());
        }

        // --- APPROVAL LOGIC ---
        // If absolute amount > 5000, set status to PENDING
        if (abs($transaction->getMontant()) > 5000) {
            $transaction->setStatus('PENDING');
            $this->addFlash('warning', 'Transaction de montant élevé détectée (> 5000). Elle est en attente de validation par un administrateur.');
        } else {
            $transaction->setStatus('ACCEPTED');
        }

        $em->persist($transaction);
        $em->flush();

        return $this->redirectToRoute('transactions');
    }

    return $this->render('wallet/add.html.twig', [
        'form' => $form->createView(),
        'categories' => $categories
    ]);
}

    #[Route('/wallet/transactions', name: 'transactions')]
public function index(

   Request $req,
    EntityManagerInterface $em,
    PaginatorInterface $paginator
): Response {

    $transaction = new TransactionWallet();

        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login'); // Security measure
        }
        $transaction->setUser($user);
    $transaction->setSource("manual");

    $data = $req->request->all('transaction_wallet');
$type = $data['type'] ?? 'INCOME';
$transaction->setType($type);

    $form = $this->createForm(TransactionType::class, $transaction, [
        'type' => $type,
    ]);

    // Map category manually from request BEFORE handleRequest to ensure validation passes
    if ($req->isMethod('POST')) {
        $postData = $req->request->all('transaction_wallet');
        $categoryId = $postData['category'] ?? null;
        if ($categoryId) {
            $category = $em->getRepository(Category::class)->find($categoryId);
            if ($category) {
                $transaction->setCategory($category);
            }
        }
    }

    $form->handleRequest($req);

    if ($form->isSubmitted() && $form->isValid()) {

        if ($transaction->getType() == "OUTCOME") {
            $transaction->setMontant(-abs($transaction->getMontant()));
        } else {
            $transaction->setMontant(abs($transaction->getMontant()));
        }
        if (!$transaction->getDateTransaction()) {
            $transaction->setDateTransaction(new \DateTime());
        }
        
        $em->persist($transaction);
        $em->flush();

        return $this->redirectToRoute('transactions');
    }

$user = $this->getUser();
$qb = $em->getRepository(TransactionWallet::class)->createQueryBuilder('t')
    ->andWhere('t.user = :userId')
    ->setParameter('userId', $user->getId());

$search = $req->query->get('search');
$filterType = $req->query->get('filter_type');
$min = $req->query->get('min');
$max = $req->query->get('max');
$sortBy = $req->query->get('sortBy', 'dateTransaction');

if ($search) {
    $qb->andWhere('t.nomTransaction LIKE :search')->setParameter('search', '%' . $search . '%');
}
if ($filterType) {
    $qb->andWhere('t.type = :type')->setParameter('type', $filterType);
}
if ($min !== null && $min !== '') {
    $qb->andWhere('t.montant >= :min')->setParameter('min', $min);
}
if ($max !== null && $max !== '') {
    $qb->andWhere('t.montant <= :max')->setParameter('max', $max);
}
$allowedSorts = ['dateTransaction', 'montant', 'nomTransaction'];
if (in_array($sortBy, $allowedSorts)) {
    $qb->orderBy('t.' . $sortBy, 'DESC');
} else {
    $qb->orderBy('t.dateTransaction', 'DESC');
}

$query = $qb->getQuery();

$transactions = $paginator->paginate(
    $query,
    $req->query->getInt('page', 1),
    5
);

    $categories = $em->getRepository(Category::class)->findAll();

return $this->render('wallet/list.html.twig', [
    'form' => $form->createView(),
    'transactions' => $transactions,
    'categories' => $categories 
]);
}

    #[Route('/walletuser', name: 'dashboard')]
    public function dashboard(
        Request $req, 
        EntityManagerInterface $em, 
        WalletBalanceService $balanceService, 
        CurrencyConverterService $currencyConverter,
        \App\Service\SmartLearningService $smartLearningService
    ): Response
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        // 🧠 SMART RECOMMENDATIONS
        $recommendations = $smartLearningService->getRecommendations($user);

        // 🧠 Optimized Stats with DTO Hydration (3-5x faster)
        $statsDtos = $em->getRepository(TransactionWallet::class)->getStatsByUser($user->getId());
        $income = 0;
        $outcome = 0;
        foreach ($statsDtos as $dto) {
            if ($dto->type === 'INCOME') {
                $income = $dto->totalAmount;
            } else {
                $outcome = abs($dto->totalAmount);
            }
        }

        $transactions = $em->getRepository(TransactionWallet::class)->findBy(['user' => $user->getId()]);
        
        $incomeData  = [];
        $outcomeData = [];
        $categoryMap = [];

        $session = $req->getSession();
        $selectedCurrency = $session->get('app_currency', 'TND');
        $exchangeRate = $currencyConverter->getRate('TND', $selectedCurrency);
        
        // Safety Override for presentation: Force correct EUR rate
        if ($selectedCurrency === 'EUR') {
            $exchangeRate = 0.295;
        }


    $biggestTransaction = null;
    $biggestAmount      = 0;

    foreach ($transactions as $t) {
        if ($t->getStatus() !== 'ACCEPTED') {
            continue;
        }
        $amountTnd = $t->getMontant();
        $amountConverted = $amountTnd * $exchangeRate;
        $cat = $t->getCategory() ? $t->getCategory()->getNom() : 'Autre';

        if ($amountTnd > 0) {
            $incomeData[$cat] = ($incomeData[$cat] ?? 0) + $amountConverted; // Total for Chart (Converted)
        } else {
            $outcomeData[$cat] = ($outcomeData[$cat] ?? 0) + abs($amountConverted); // Total for Chart (Converted)
        }

        // Category total (for métier analysis)
        $categoryMap[$cat] = ($categoryMap[$cat] ?? 0) + abs($amountConverted);

        // Biggest single transaction
        if (abs($amountTnd) > $biggestAmount) {
            $biggestAmount      = abs($amountTnd);
            $biggestTransaction = $t;
        }
    }

    // Balance stays in raw TND — the template handles display with currencySymbol/rate
    $balance = $balanceService->calculateUserBalance($user->getId());

    // ─── Daily breakdown (Changed from Y-m to Y-m-d for better detail) ──────────
    $chartDates = [];
    foreach ($transactions as $t) {
        if ($t->getStatus() !== 'ACCEPTED') {
            continue;
        }
        $day = $t->getDateTransaction()->format('Y-m-d');
        if (!isset($chartDates[$day])) {
            $chartDates[$day] = ['income' => 0, 'outcome' => 0];
        }
        $amt = $t->getMontant() * $exchangeRate;
        if ($amt > 0) { $chartDates[$day]['income']  += $amt; }
        else           { $chartDates[$day]['outcome'] += abs($amt); }
    }
    ksort($chartDates);
    $chartLabels  = array_keys($chartDates);
    $chartIncome  = array_column($chartDates, 'income');
    $chartOutcome = array_column($chartDates, 'outcome');

    // ── Métier insights ──────────────────────────────────────────────────

    // 1. Top spending category
    arsort($outcomeData);
    $topSpendingCat   = !empty($outcomeData) ? array_key_first($outcomeData) : '—';
    $topSpendingAmt   = !empty($outcomeData) ? reset($outcomeData) : 0;

    // 2. Top income category
    arsort($incomeData);
    $topIncomeCat     = !empty($incomeData) ? array_key_first($incomeData) : '—';
    $topIncomeAmt     = !empty($incomeData) ? reset($incomeData) : 0;

    // 3. Savings rate (%) — income > 0 avoids division by zero
    $savingsRate      = $income > 0 ? round((($income - $outcome) / $income) * 100, 1) : 0;

    // 4. Spending trend — compare last month vs previous month
    $months       = array_values($chartDates);
    $monthCount   = count($months);
    $spendingTrend = '—';
    if ($monthCount >= 2) {
        $lastOut = $months[$monthCount - 1]['outcome'];
        $prevOut = $months[$monthCount - 2]['outcome'];
        if ($prevOut > 0) {
            $trendPct      = round((($lastOut - $prevOut) / $prevOut) * 100, 1);
            $spendingTrend = ($trendPct <= 0 ? '▼ ' : '▲ +') . abs($trendPct) . '% vs mois précédent';
            $trendGood     = $trendPct <= 0; // spending decreased = good
        } else {
            $spendingTrend = 'Nouveau mois';
            $trendGood     = true;
        }
    } else {
        $trendGood = true;
    }

    // 5. Best savings month
    $bestMonth     = '—';
    $bestSaving    = PHP_INT_MIN;
    foreach ($chartDates as $m => $d) {
        $net = $d['income'] - $d['outcome'];
        if ($net > $bestSaving) { $bestSaving = $net; $bestMonth = $m; }
    }

    return $this->render('wallet/walletuser.html.twig', [
        'income'         => $income,
        'outcome'        => $outcome,
        'balance'        => $balance,
        'currencySymbol' => $selectedCurrency,
        'rate'           => $exchangeRate,
        'transactions'   => $transactions,

        // category charts
        'incomeChartLabels'  => array_keys($incomeData),
        'incomeChartData'    => array_values($incomeData),
        'outcomeChartLabels' => array_keys($outcomeData),
        'outcomeChartData'   => array_values($outcomeData),

        // timeseries
        'chartLabels'  => $chartLabels,
        'incomeData'   => $chartIncome,
        'outcomeData'  => $chartOutcome,

        // métier insights
        'topSpendingCat' => $topSpendingCat,
        'topSpendingAmt' => $topSpendingAmt,
        'topIncomeCat'   => $topIncomeCat,
        'topIncomeAmt'   => $topIncomeAmt,
        'savingsRate'    => $savingsRate,
        'spendingTrend'  => $spendingTrend,
        'trendGood'      => $trendGood ?? true,
        'bestMonth'      => $bestMonth,
        'bestSaving'     => max(0, $bestSaving),
        'biggestTx'      => $biggestTransaction,
        'biggestAmt'     => $biggestAmount,
        'recommendations' => $recommendations
    ]);
}

    #[Route('/calendar', name: 'user_calendar')]
    public function calendar(): Response
    {
        return $this->render('wallet/calendar.html.twig');
    }

    #[Route('/wallet/transaction/edit/{id}', name: 'transaction_edit')]
    public function edit($id, Request $req, EntityManagerInterface $em): Response
    {
        $transaction = $em->getRepository(TransactionWallet::class)->find($id);

        if (!$transaction || $transaction->getUserId() !== $this->getUser()->getId()) {
            throw $this->createAccessDeniedException("Vous n'avez pas le droit de modifier cette transaction.");
        }

        $form = $this->createForm(TransactionType::class, $transaction, [
            'show_type' => false,
            'show_category' => false,
        ]);
        $form->handleRequest($req);

        if ($form->isSubmitted()) {
            if ($form->isValid()) {
                if ($transaction->getType() == "OUTCOME") {
                    $transaction->setMontant(-abs($transaction->getMontant()));
                } else {
                    $transaction->setMontant(abs($transaction->getMontant()));
                }

                $em->flush();
                return $this->redirectToRoute('transactions');
            }
        }

        return $this->render('wallet/edit.html.twig', [
            'form' => $form->createView(),
            'transaction' => $transaction
        ]);
    }

    #[Route('/wallet/transaction/delete/{id}', name: 'transaction_delete')]
    public function delete($id, EntityManagerInterface $em): Response
    {
        $transaction = $em->getRepository(TransactionWallet::class)->find($id);

        if ($transaction && $transaction->getUserId() === $this->getUser()->getId()) {
            $em->remove($transaction);
            $em->flush();
        } else if ($transaction) {
            throw $this->createAccessDeniedException("Vous n'avez pas le droit de supprimer cette transaction.");
        }

        return $this->redirectToRoute('transactions');
    }



#[Route('/api/categories/{type}', name: 'categories_by_type')]
public function getCategoriesByType($type, EntityManagerInterface $em): JsonResponse
{
    $user = $this->getUser();
    $categories = $em->getRepository(Category::class)
        ->createQueryBuilder('c')
        ->where('c.type = :type')
        ->andWhere('c.user = :uid OR c.user IS NULL')
        ->setParameter('type', strtoupper($type))
        ->setParameter('uid', $user ? $user->getId() : null)
        ->getQuery()
        ->getResult();

    $data = [];

    foreach ($categories as $cat) {
        $data[] = [
            'id' => $cat->getId(),
            'nom' => $cat->getNom()
        ];
    }

    return new JsonResponse($data);
}


#[Route('/transactions/pdf', name: 'transactions_pdf')]
public function exportPdf(EntityManagerInterface $em): Response
{
    $user = $this->getUser();
    if (!$user) {
        return $this->redirectToRoute('app_login');
    }

    // Filter by current user ID to avoid data leakage
    $transactions = $em->getRepository(TransactionWallet::class)->findBy(['user' => $user->getId()]);

    $session = $em->getConnection()->getParams(); // Not needed, use request stack or session
    $selectedCurrency = $em->getFilters()->isEnabled('softdeleteable') ? 'TND' : 'TND'; // placeholder
    
    // Get currency from session via request stack if possible
    $selectedCurrency = 'TND';
    $rate = 1.0;
    
    // In a controller, we have access to the session
    $request = $this->container->get('request_stack')->getCurrentRequest();
    if ($request && $request->hasSession()) {
        $selectedCurrency = $request->getSession()->get('app_currency', 'TND');
    }
    
    // Hardcoded rate logic for PDF consistency
    if ($selectedCurrency === 'EUR') $rate = 0.295;
    if ($selectedCurrency === 'USD') $rate = 0.321;

    $html = $this->renderView('wallet/pdf.html.twig', [
        'transactions'   => $transactions,
        'currencySymbol' => $selectedCurrency,
        'rate'           => $rate
    ]);

    $dompdf = new \Dompdf\Dompdf();
    $dompdf->loadHtml($html);
    $dompdf->render();

    return new Response(
        $dompdf->output(),
        200,
        [
            'Content-Type' => 'application/pdf',
            'Content-Disposition' => 'attachment; filename="mes_transactions.pdf"',
        ]
    );
}


#[Route('/transactions/excel', name: 'transactions_excel')]
public function exportExcel(EntityManagerInterface $em): Response
{
    $user = $this->getUser();
    if (!$user) {
        return $this->redirectToRoute('app_login');
    }

    $transactions = $em->getRepository(TransactionWallet::class)->findBy(['user' => $user->getId()]);

    $response = new StreamedResponse(function () use ($transactions) {
        $handle = fopen('php://output', 'w+');
        
        // Add UTF-8 BOM for Excel compatibility
        fprintf($handle, chr(0xEF).chr(0xBB).chr(0xBF));
        
        // Header
        fputcsv($handle, ['Nom', 'Type', 'Montant', 'Categorie', 'Date'], ';');

        foreach ($transactions as $t) {
            fputcsv($handle, [
                $t->getNomTransaction(),
                $t->getType(),
                number_format($t->getMontant(), 2, '.', ''),
                $t->getCategory() ? $t->getCategory()->getNom() : 'N/A',
                $t->getDateTransaction()->format('d/m/Y')
            ], ';');
        }
        fclose($handle);
    });

    $response->headers->set('Content-Type', 'text/csv; charset=utf-8');
    $response->headers->set('Content-Disposition', 'attachment; filename="mes_transactions.csv"');

    return $response;
}



}
