<?php

namespace App\Controller;

use App\Entity\TransactionBourse;
use App\Repository\TransactionBourseRepository;
use App\Repository\ActionRepository;
use App\Service\TransactionService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/transaction')]
class TransactionController extends AbstractController
{
    public function __construct(
        private TransactionService $transactionService,
        private ActionRepository $actionRepo
    ) {}

    /**
     * 📜 Historique de toutes les transactions
     */
    #[Route('/', name: 'app_transaction_index', methods: ['GET'])]
    public function index(TransactionBourseRepository $transactionRepo): Response
    {
        return $this->render('transaction/index.html.twig', [
            'transactions' => $transactionRepo->findBy([], ['dateTransaction' => 'DESC']),
            'stats' => [
                'total' => $transactionRepo->count([]),
                'total_achats' => $this->transactionService->getTotalAchats(),
                'total_ventes' => $this->transactionService->getTotalVentes(),
                'total_commissions' => $this->transactionService->getTotalCommissions(),
            ]
        ]);
    }

    /**
     * 💹 Interface de trading
     */
    #[Route('/trading', name: 'app_transaction_trading', methods: ['GET'])]
    public function trading(): Response
    {
        return $this->render('transaction/trading.html.twig', [
            'actions' => $this->actionRepo->findAvailable(),
            'secteurs' => $this->actionRepo->getAvailableSecteurs(),
        ]);
    }

    /**
     * ✅ Exécuter un trade (ACHAT ou VENTE)
     */
    #[Route('/execute', name: 'app_transaction_execute', methods: ['POST'])]
    public function execute(Request $request): Response
    {
        $type = $request->request->get('type'); // ACHAT ou VENTE
        $actionId = (int) $request->request->get('action_id');
        $quantite = (int) $request->request->get('quantite');

        try {
            $transaction = $this->transactionService->executerTrade(
                $type,
                $actionId,
                $quantite
            );

            $this->addFlash('success', sprintf(
                '✅ %s de %d actions effectué avec succès ! Montant: %.2f TND',
                $type,
                $quantite,
                $transaction->getMontantTotal()
            ));

            return $this->redirectToRoute('app_transaction_index');

        } catch (\Exception $e) {
            $this->addFlash('danger', '❌ ' . $e->getMessage());
            return $this->redirectToRoute('app_transaction_trading');
        }
    }

    /**
     * 👁️ Voir les détails d'une transaction
     */
    #[Route('/{id}', name: 'app_transaction_show', methods: ['GET'])]
    public function show(TransactionBourse $transaction): Response
    {
        return $this->render('transaction/show.html.twig', [
            'transaction' => $transaction,
        ]);
    }

    /**
     * ❌ Annuler une transaction
     */
    #[Route('/{id}/annuler', name: 'app_transaction_annuler', methods: ['POST'])]
    public function annuler(Request $request, TransactionBourse $transaction): Response
    {
        if ($this->isCsrfTokenValid('annuler' . $transaction->getId(), $request->request->get('_token'))) {
            try {
                $this->transactionService->annuler($transaction);
                $this->addFlash('success', '✅ Transaction annulée avec succès !');
            } catch (\Exception $e) {
                $this->addFlash('danger', '❌ ' . $e->getMessage());
            }
        }

        return $this->redirectToRoute('app_transaction_index');
    }

    /**
     * 📊 Statistiques des transactions
     */
    #[Route('/stats', name: 'app_transaction_stats', methods: ['GET'])]
    public function stats(): Response
    {
        return $this->render('transaction/stats.html.twig', [
            'stats' => $this->transactionService->getStatistics(),
        ]);
    }
}