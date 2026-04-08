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
use Symfony\Component\Routing\Annotation\Route;

class MarketController extends AbstractController
{
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
    public function buy(Action $action, Request $request, EntityManagerInterface $em): Response
    {
        if ($request->isMethod('POST')) {

            $quantite = (int) $request->request->get('quantite');

            if ($quantite <= 0) {
                $this->addFlash('error', 'Quantité invalide');
                return $this->redirectToRoute('app_market');
            }

            if ($quantite > $action->getQuantiteDisponible()) {
                $this->addFlash('error', 'Stock insuffisant');
                return $this->redirectToRoute('app_market');
            }

            $montant = $quantite * $action->getPrixUnitaire();

            $transaction = new TransactionBourse();
            $transaction->setAction($action);
            $transaction->setQuantite($quantite);
            $transaction->setTypeTransaction('ACHAT');
            $transaction->setPrixUnitaire($action->getPrixUnitaire());
            $transaction->setMontantTotal($montant);
            $transaction->setDateTransaction(new \DateTime());

            $action->setQuantiteDisponible(
                $action->getQuantiteDisponible() - $quantite
            );

            $em->persist($transaction);
            $em->flush();

            $this->addFlash('success', 'Achat effectué');
            return $this->redirectToRoute('app_market');
        }

        return $this->render('market/buy.html.twig', [
            'action' => $action
        ]);
    }

    // =========================
    // 🔥 VENTE (SÉCURISÉE)
    // =========================
    #[Route('/sell/{id}', name: 'app_sell')]
    public function sell(Action $action, Request $request, EntityManagerInterface $em): Response
    {
        if ($request->isMethod('POST')) {

            $quantite = (int) $request->request->get('quantite');

            if ($quantite <= 0) {
                $this->addFlash('error', 'Quantité invalide');
                return $this->redirectToRoute('app_market');
            }

            $repo = $em->getRepository(TransactionBourse::class);

            $totalAchete = $repo->createQueryBuilder('t')
                ->select('COALESCE(SUM(t.quantite),0)')
                ->where('t.typeTransaction = :type')
                ->andWhere('t.action = :action')
                ->setParameter('type', 'ACHAT')
                ->setParameter('action', $action)
                ->getQuery()
                ->getSingleScalarResult();

            $totalVendu = $repo->createQueryBuilder('t')
                ->select('COALESCE(SUM(t.quantite),0)')
                ->where('t.typeTransaction = :type')
                ->andWhere('t.action = :action')
                ->setParameter('type', 'VENTE')
                ->setParameter('action', $action)
                ->getQuery()
                ->getSingleScalarResult();

            $stockUser = $totalAchete - $totalVendu;

            if ($quantite > $stockUser) {
                $this->addFlash('error', 'Stock insuffisant');
                return $this->redirectToRoute('app_market');
            }

            $montant = $quantite * $action->getPrixUnitaire();

            $transaction = new TransactionBourse();
            $transaction->setAction($action);
            $transaction->setQuantite($quantite);
            $transaction->setTypeTransaction('VENTE');
            $transaction->setPrixUnitaire($action->getPrixUnitaire());
            $transaction->setMontantTotal($montant);
            $transaction->setDateTransaction(new \DateTime());

            $action->setQuantiteDisponible(
                $action->getQuantiteDisponible() + $quantite
            );

            $em->persist($transaction);
            $em->flush();

            $this->addFlash('success', 'Vente effectuée');
            return $this->redirectToRoute('app_market');
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
        $type = $request->query->get('type');

        $qb = $repo->createQueryBuilder('t')
            ->orderBy('t.dateTransaction', 'DESC');

        if ($type) {
            $qb->andWhere('t.typeTransaction = :type')
               ->setParameter('type', $type);
        }

        $transactions = $qb->getQuery()->getResult();

        $totalInvesti = 0;
        $totalVendu = 0;

        foreach ($transactions as $t) {
            if ($t->getTypeTransaction() === 'ACHAT') {
                $totalInvesti += $t->getMontantTotal();
            } else {
                $totalVendu += $t->getMontantTotal();
            }
        }

        return $this->render('market/history.html.twig', [
            'transactions' => $transactions,
            'totalInvesti' => $totalInvesti,
            'totalVendu' => $totalVendu,
            'nbTransactions' => count($transactions),
            'type' => $type
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