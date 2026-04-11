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
class TransactionController extends AbstractController
{


public function add(Request $req, EntityManagerInterface $em): Response
{
    $transaction = new TransactionWallet();

$transaction->setUserId(rand(1, 5));
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
public function index(Request $req, EntityManagerInterface $em): Response
{
    $transaction = new TransactionWallet();

    $transaction->setUserId(1);
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

        if ($categoryId) {
            $category = $em->getRepository(Category::class)->find($categoryId);
            $transaction->setCategory($category);
        }
        $em->persist($transaction);
        $em->flush();

        return $this->redirectToRoute('transactions');
    }

    $transactions = $em->getRepository(TransactionWallet::class)->findAll();

    $categories = $em->getRepository(Category::class)->findAll();

return $this->render('wallet/list.html.twig', [
    'form' => $form->createView(),
    'transactions' => $transactions,
    'categories' => $categories 
]);
}

   #[Route('/walletuser', name: 'dashboard')]
public function dashboard(EntityManagerInterface $em): Response
{
    $transactions = $em->getRepository(TransactionWallet::class)->findAll();

    $income = 0;
    $outcome = 0;

    $incomeData = [];
    $outcomeData = [];

    foreach ($transactions as $t) {

        $amount = $t->getMontant();
        $cat = $t->getCategory() ? $t->getCategory()->getNom() : 'Autre';

        if ($amount > 0) {
            $income += $amount;

            if (!isset($incomeData[$cat])) {
                $incomeData[$cat] = 0;
            }
            $incomeData[$cat] += $amount;

        } else {
            $outcome += abs($amount);

            if (!isset($outcomeData[$cat])) {
                $outcomeData[$cat] = 0;
            }
            $outcomeData[$cat] += abs($amount);
        }
    }

    $balance = $income - $outcome;
    $chartDates = [];

foreach ($transactions as $t) {



    // 🔥 month format
    $month = $t->getDateTransaction()->format('Y-m'); 

    if (!isset($chartDates[$month])) {
        $chartDates[$month] = [
            'income' => 0,
            'outcome' => 0
        ];
    }

    if ($t->getType() === 'INCOME') {
        $chartDates[$month]['income'] += $t->getMontant();
    } else {
        $chartDates[$month]['outcome'] += abs($t->getMontant());
    }
}



// final arrays
$chartLabels = array_keys($chartDates);
$chartIncome = array_column($chartDates, 'income');
$chartOutcome = array_column($chartDates, 'outcome');

ksort($chartDates);

$chartLabels = array_keys($chartDates);
$chartIncome = array_column($chartDates, 'income');
$chartOutcome = array_column($chartDates, 'outcome');

    return $this->render('wallet/walletuser.html.twig', [
        'income' => $income,
        'outcome' => $outcome,
        'balance' => $balance,
        'transactions' => $transactions,

        // 🔥 charts
        'incomeChartLabels' => array_keys($incomeData),
        'incomeChartData' => array_values($incomeData),

        'outcomeChartLabels' => array_keys($outcomeData),
        'outcomeChartData' => array_values($outcomeData),

        'chartLabels' => $chartLabels,
    'incomeData' => $chartIncome,
    'outcomeData' => $chartOutcome,
    ]);
}

    #[Route('/edit/{id}', name: 'transaction_edit')]
    public function edit($id, Request $req, EntityManagerInterface $em): Response
    {
        $transaction = $em->getRepository(TransactionWallet::class)->find($id);

        if (!$transaction) {
            return new Response("Transaction non trouvée");
        }


$type = $data['type'] ?? 'INCOME';
$transaction->setType($type);

        $form = $this->createForm(TransactionType::class, $transaction, [
    'show_type' => false,
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