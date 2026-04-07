<?php

namespace App\Controller;

use App\Repository\TransactionWalletRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\HttpFoundation\Request;
use Dompdf\Dompdf;

class AdminController extends AbstractController
{
    #[Route('/admin', name: 'app_admin')]
    public function index(Request $request, TransactionWalletRepository $repo): Response
    {
        $search = $request->query->get('search');

        if ($search) {
            $transactions = $repo->createQueryBuilder('t')
                ->where('t.nomTransaction LIKE :search')
                ->orWhere('t.userId LIKE :search')
                ->orWhere('t.type LIKE :search')
                ->orWhere('CAST(t.montant AS string) LIKE :search')
                ->setParameter('search', '%' . $search . '%')
                ->getQuery()
                ->getResult();
        } else {
            $transactions = $repo->findAll();
        }

        $income = 0;
        $outcome = 0;
        $users = [];

        foreach ($transactions as $t) {

            $userId = $t->getUserId();

            if (!isset($users[$userId])) {
                $users[$userId] = [
                    'id' => $userId,
                    'name' => 'User ' . $userId,
                    'income' => 0,
                    'outcome' => 0,
                    'balance' => 0,
                    'count' => 0,
                    'transactions' => []
                ];
            }

            if ($t->getType() === 'INCOME') {
                $income += $t->getMontant();
                $users[$userId]['income'] += $t->getMontant();
            } else {
                $outcome += abs($t->getMontant());
                $users[$userId]['outcome'] += abs($t->getMontant());
            }

            $users[$userId]['count']++;
            $users[$userId]['balance'] =
                $users[$userId]['income'] - $users[$userId]['outcome'];

            $users[$userId]['transactions'][] = $t;
        }

        $chartDates = [];

        foreach ($transactions as $t) {
            $date = $t->getDateTransaction()->format('Y-m-d');

            if (!isset($chartDates[$date])) {
                $chartDates[$date] = 0;
            }

            $chartDates[$date] += $t->getMontant();
        }

        ksort($chartDates);

        $labels = array_keys($chartDates);
        $values = array_values($chartDates);

        return $this->render('admin/index.html.twig', [
            'transactions' => $transactions,
            'users' => $users,
            'totalIncome' => $income,
            'totalOutcome' => $outcome,
            'totalBalance' => $income - $outcome,
            'chartLabels' => $labels,
            'chartData' => $values
        ]);
    }

    #[Route('/admin/pdf', name: 'download_pdf')]
    public function downloadPdf(Request $request, TransactionWalletRepository $repo): Response
    {
        $search = $request->query->get('search');

        if ($search) {
            $transactions = $repo->createQueryBuilder('t')
                ->where('t.nomTransaction LIKE :search')
                ->orWhere('t.userId LIKE :search')
                ->orWhere('t.type LIKE :search')
                ->orWhere('CAST(t.montant AS string) LIKE :search')
                ->setParameter('search', '%' . $search . '%')
                ->getQuery()
                ->getResult();
        } else {
            $transactions = $repo->findAll();
        }

        $users = [];

        foreach ($transactions as $t) {
            $userId = $t->getUserId();

            if (!isset($users[$userId])) {
                $users[$userId] = [
                    'name' => 'User ' . $userId,
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

            $users[$userId]['balance'] =
                $users[$userId]['income'] - $users[$userId]['outcome'];

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

}