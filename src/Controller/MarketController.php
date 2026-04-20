<?php

namespace App\Controller;

use Dompdf\Dompdf;
use App\Entity\Action;
use App\Entity\TransactionBourse;
use App\Repository\ActionRepository;
use App\Repository\TransactionBourseRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class MarketController extends AbstractController
{
    public function __construct(
        private \App\Service\TransactionService $transactionService
    ) {}
    // =========================
    // 🔥 MARKET
    // =========================
    #[Route('/market', name: 'app_market')]
    public function index(ActionRepository $repo): Response
    {
        return $this->render('market/index.html.twig', [
            'actions' => $repo->findAll(),
            'type' => null // ✅ FIX
        ]);
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
                $this->transactionService->executerTrade('ACHAT', $action->getId(), $quantite, $this->getUser());
                $this->addFlash('success', '✅ Achat effectué avec succès !');
                return $this->redirectToRoute('app_market');

            } catch (\Exception $e) {
                // Flash message for the SweetAlert popup
                $this->addFlash('danger', '❌ ' . $e->getMessage());
                return $this->redirectToRoute('app_market');
            }
        }

        return $this->render('market/buy.html.twig', [
            'action' => $action
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
                $this->transactionService->executerTrade('VENTE', $action->getId(), $quantite, $this->getUser());
                $this->addFlash('success', '✅ Vente effectuée avec succès !');
                return $this->redirectToRoute('app_market');

            } catch (\Exception $e) {
                $this->addFlash('danger', '❌ ' . $e->getMessage());
                return $this->redirectToRoute('app_market');
            }
        }

        return $this->render('market/sell.html.twig', [
            'action' => $action
        ]);
    }

    // =========================
    // 🔥 HISTORIQUE (PRO)
    // =========================
#[Route('/history', name: 'app_history')]
public function history(Request $request, TransactionBourseRepository $repo): Response
{
    $page = max(1, $request->query->getInt('page', 1));
    $limit = 10;

    $qb = $repo->createQueryBuilder('t')
        ->leftJoin('t.action', 'a')
        ->addSelect('a')
        ->where('t.user = :user')
        ->setParameter('user', $this->getUser())
        ->orderBy('t.dateTransaction', 'DESC');

    // 🔥 PAGINATION
    $qb->setFirstResult(($page - 1) * $limit)
       ->setMaxResults($limit);

    $transactions = $qb->getQuery()->getResult();

    // 🔥 TOTAL (sans pagination)
    $totalItems = $repo->createQueryBuilder('t')
        ->select('COUNT(t.id)')
        ->where('t.user = :user')
        ->setParameter('user', $this->getUser())
        ->getQuery()
        ->getSingleScalarResult();
    
    $totalPages = ceil($totalItems / $limit);

    // 🔥 STATS (optionnel global)
    $all = $repo->findBy(['user' => $this->getUser()]);

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

    return $this->render('market/history.html.twig', [
        'transactions' => $transactions,
        'currentPage' => $page,
        'totalPages' => $totalPages,
        'stats' => [
            'total_investi' => $totalInvesti,
            'total_vendu' => $totalVendu,
            'commissions' => $totalCommission,
            'nb_transactions' => $totalItems
        ]
    ]);
}

    // =========================
    // 📄 PDF
    // =========================
    #[Route('/history/pdf', name: 'app_history_pdf')]
    public function pdf(TransactionBourseRepository $repo): Response
    {
        $transactions = $repo->findAll();

        $html = $this->renderView('market/pdf.html.twig', [
            'transactions' => $transactions
        ]);

        $dompdf = new Dompdf();
        $dompdf->loadHtml($html);
        $dompdf->render();

        return new Response(
            $dompdf->output(),
            200,
            ['Content-Type' => 'application/pdf']
        );
    }
}