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

class TransactionController extends AbstractController
{

#[Route('/transaction/add', name: 'transaction_add')]

public function add(Request $req, EntityManagerInterface $em): Response
{
    $transaction = new TransactionWallet();
    $transaction->setUserId(6);

    $transaction->setSource("manual");

    $categories = $em->getRepository(Category::class)->findAll();

    $form = $this->createForm(TransactionType::class, $transaction);
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
$data = $req->request->all('transaction_wallet');

$categoryId = $data['category'] ?? null;

if ($categoryId) {
    $category = $em->getRepository(Category::class)->find($categoryId);
    $transaction->setCategory($category);
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

    #[Route('/transactions', name: 'transactions')]
public function index(

   Request $req,
    EntityManagerInterface $em,
    PaginatorInterface $paginator
): Response {

    $transaction = new TransactionWallet();

    $transaction->setUserId(6);
    $transaction->setSource("manual");

    $data = $req->request->all('transaction_wallet');
$type = $data['type'] ?? 'INCOME';
$transaction->setType($type);

$form = $this->createForm(TransactionType::class, $transaction, [
    'type' => $type,
]);
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
        $data = $req->request->all('transaction_wallet');

$categoryId = $data['category'] ?? null;


        if ($categoryId) {
            $category = $em->getRepository(Category::class)->find($categoryId);
            $transaction->setCategory($category);
        }
        
        $em->persist($transaction);
        $em->flush();

        return $this->redirectToRoute('transactions');
    }

$qb = $em->getRepository(TransactionWallet::class)->createQueryBuilder('t');

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
    public function dashboard(Request $req, EntityManagerInterface $em, WalletBalanceService $balanceService, CurrencyConverterService $currencyConverter): Response
    {
        $transactions = $em->getRepository(TransactionWallet::class)->findAll();
        
        $income  = 0;
    $outcome = 0;
    $incomeData  = [];
    $outcomeData = [];
    $categoryMap = [];

    $selectedCurrency = $req->query->get('currency', 'DT');
    $exchangeRate = $currencyConverter->getRate('DT', $selectedCurrency);

    $biggestTransaction = null;
    $biggestAmount      = 0;

    foreach ($transactions as $t) {
        $amount = $t->getMontant() * $exchangeRate;
        $cat    = $t->getCategory() ? $t->getCategory()->getNom() : 'Autre';

        if ($amount > 0) {
            $income += $amount;
            $incomeData[$cat] = ($incomeData[$cat] ?? 0) + $amount;
        } else {
            $outcome += abs($amount);
            $outcomeData[$cat] = ($outcomeData[$cat] ?? 0) + abs($amount);
        }

        // Category total (for métier analysis)
        $categoryMap[$cat] = ($categoryMap[$cat] ?? 0) + abs($amount);

        // Biggest single transaction
        if (abs($amount) > $biggestAmount) {
            $biggestAmount      = abs($amount);
            $biggestTransaction = $t;
        }
    }

    $balance = $income - $outcome;

    // ── Daily breakdown (Changed from Y-m to Y-m-d for better detail) ──────
    $chartDates = [];
    foreach ($transactions as $t) {
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

    // ── Métier insights ───────────────────────────────────

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
        'income'         => number_format($income,  2, '.', ''),
        'outcome'        => number_format($outcome, 2, '.', ''),
        'balance'        => number_format($balance, 2, '.', ''),
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
        'topSpendingAmt' => number_format($topSpendingAmt, 2, '.', ''),
        'topIncomeCat'   => $topIncomeCat,
        'topIncomeAmt'   => number_format($topIncomeAmt,   2, '.', ''),
        'savingsRate'    => $savingsRate,
        'spendingTrend'  => $spendingTrend,
        'trendGood'      => $trendGood ?? true,
        'bestMonth'      => $bestMonth,
        'bestSaving'     => number_format(max(0, $bestSaving), 2, '.', ''),
        'biggestTx'      => $biggestTransaction,
        'biggestAmt'     => number_format($biggestAmount, 2, '.', ''),
    ]);
}

    #[Route('/calendar', name: 'user_calendar')]
    public function calendar(): Response
    {
        return $this->render('wallet/calendar.html.twig');
    }

    #[Route('/edit/{id}', name: 'transaction_edit')]
    public function edit($id, Request $req, EntityManagerInterface $em): Response
    {
        $transaction = $em->getRepository(TransactionWallet::class)->find($id);

        if (!$transaction) {
            return new Response("Transaction non trouvée");
        }

$data = $req->request->all();


if (isset($data['type'])) {
    $transaction->setType($data['type']);
}

        $form = $this->createForm(TransactionType::class, $transaction, [
    'show_type' => false,
    'show_category' => false,
]);
        $form->handleRequest($req);

        if ($form->isSubmitted() && $form->isValid()) {

            if ($transaction->getType() == "OUTCOME") {
                $transaction->setMontant(-abs($transaction->getMontant()));
            } else {
                $transaction->setMontant(abs($transaction->getMontant()));
            }

            $em->flush();

            return $this->redirectToRoute('transactions');
        }

        return $this->render('wallet/edit.html.twig', [
            'form' => $form->createView()
        ]);
    }

    #[Route('/delete/{id}', name: 'transaction_delete')]
    public function delete($id, EntityManagerInterface $em): Response
    {
        $transaction = $em->getRepository(TransactionWallet::class)->find($id);

        if ($transaction) {
            $em->remove($transaction);
            $em->flush();
        }

        return $this->redirectToRoute('transactions');
    }



#[Route('/api/categories/{type}', name: 'categories_by_type')]
public function getCategoriesByType($type, EntityManagerInterface $em): JsonResponse
{
    $categories = $em->getRepository(Category::class)
    ->createQueryBuilder('c')
    ->where('c.type = :type')
->setParameter('type', strtoupper($type))
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
    $transactions = $em->getRepository(TransactionWallet::class)->findAll();

    $html = $this->renderView('wallet/pdf.html.twig', [
        'transactions' => $transactions
    ]);

    $dompdf = new \Dompdf\Dompdf();
    $dompdf->loadHtml($html);
    $dompdf->render();

    return new Response(
        $dompdf->output(),
        200,
        [
            'Content-Type' => 'application/pdf',
            'Content-Disposition' => 'attachment; filename="transactions.pdf"',
        ]
    );
}


#[Route('/transactions/excel', name: 'transactions_excel')]
public function exportExcel(EntityManagerInterface $em): Response
{
    $transactions = $em->getRepository(TransactionWallet::class)->findAll();

    $spreadsheet = new Spreadsheet();
    $sheet = $spreadsheet->getActiveSheet();

    $sheet->setCellValue('A1', 'Nom');
    $sheet->setCellValue('B1', 'Type');
    $sheet->setCellValue('C1', 'Montant');
    $sheet->setCellValue('D1', 'Category');
    $sheet->setCellValue('E1', 'Date');

    $row = 2;

    foreach ($transactions as $t) {
        $sheet->setCellValue('A'.$row, $t->getNomTransaction());
        $sheet->setCellValue('B'.$row, $t->getType());
        $sheet->setCellValue('C'.$row, $t->getMontant());
        $sheet->setCellValue('D'.$row, $t->getCategory() ? $t->getCategory()->getNom() : '');
        $sheet->setCellValue('E'.$row, $t->getDateTransaction()->format('Y-m-d'));
        $row++;
    }

    $response = new StreamedResponse(function () use ($spreadsheet) {
        $writer = new Xlsx($spreadsheet);
        $writer->save('php://output');
    });

    $response->headers->set('Content-Type', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
    $response->headers->set('Content-Disposition', 'attachment;filename="transactions.xlsx"');

    return $response;
}



}