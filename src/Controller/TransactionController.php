<?php

namespace App\Controller;

use App\Repository\ActionRepository;
use App\Repository\TransactionBourseRepository;
use App\Service\TransactionService;
use App\Service\CommissionService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Doctrine\ORM\Tools\Pagination\Paginator;

#[Route('/trading')]
class TransactionController extends AbstractController
{
    private const ITEMS_PER_PAGE = 10;

    public function __construct(
        private TransactionService $transactionService,
        private ActionRepository $actionRepo,
        private CommissionService $commissionService
    ) {}

    /**
     * 📜 Historique avec pagination
     */
    #[Route('/historique', name: 'app_trading_historique')]
    public function historique(
        Request $request,
        TransactionBourseRepository $transactionRepo
    ): Response {
        
        $filter = $request->query->get('filter', 'tous');
        $page = max(1, $request->query->getInt('page', 1));

        // Query de base
        $qb = $transactionRepo->createQueryBuilder('t')
            ->orderBy('t.dateTransaction', 'DESC');

        // Filtrer par type
        if ($filter === 'achats') {
            $qb->where('t.typeTransaction = :type')
               ->setParameter('type', 'ACHAT');
        } elseif ($filter === 'ventes') {
            $qb->where('t.typeTransaction = :type')
               ->setParameter('type', 'VENTE');
        }

        // Pagination
        $qb->setFirstResult(($page - 1) * self::ITEMS_PER_PAGE)
           ->setMaxResults(self::ITEMS_PER_PAGE);

        $paginator = new Paginator($qb);
        $totalItems = count($paginator);
        $totalPages = ceil($totalItems / self::ITEMS_PER_PAGE);

        // Statistiques
        $totalInvesti = $this->transactionService->getTotalAchats();
        $totalVendu = $this->transactionService->getTotalVentes();
        $totalCommissions = $this->transactionService->getTotalCommissions();
        $nbTransactions = $transactionRepo->count([]);

        return $this->render('trading/historique.html.twig', [
            'transactions' => $paginator,
            'filter' => $filter,
            'currentPage' => $page,
            'totalPages' => $totalPages,
            'totalItems' => $totalItems,
            'itemsPerPage' => self::ITEMS_PER_PAGE,
            'stats' => [
                'total_investi' => $totalInvesti,
                'total_vendu' => $totalVendu,
                'commissions' => $totalCommissions,
                'nb_transactions' => $nbTransactions,
            ]
        ]);
    }

    /**
     * 🟢 Interface d'achat avec info stock
     */
    #[Route('/acheter', name: 'app_trading_acheter')]
    public function acheter(Request $request): Response
    {
        $actionId = $request->query->getInt('action');
        $actionSelectionnee = $actionId ? $this->actionRepo->find($actionId) : null;

        return $this->render('trading/acheter.html.twig', [
            'actions' => $this->actionRepo->findAvailable(),
            'actionSelectionnee' => $actionSelectionnee,
        ]);
    }

    /**
     * 🔴 Interface de vente avec validation stock portefeuille
     */
    #[Route('/vendre', name: 'app_trading_vendre')]
    public function vendre(Request $request): Response
    {
        $actionId = $request->query->getInt('action');
        $actionSelectionnee = $actionId ? $this->actionRepo->find($actionId) : null;

        // TODO: Récupérer le portefeuille de l'utilisateur
        // Pour l'instant, simulation avec stock disponible
        $actionsDisponibles = $this->actionRepo->findAvailable();

        return $this->render('trading/vendre.html.twig', [
            'actions' => $actionsDisponibles,
            'actionSelectionnee' => $actionSelectionnee,
        ]);
    }

    /**
     * ✅ Exécuter achat avec validation stock
     */
    #[Route('/acheter/execute', name: 'app_trading_acheter_execute', methods: ['POST'])]
    public function executeAchat(Request $request): Response
    {
        $actionId = (int) $request->request->get('action_id');
        $quantite = (int) $request->request->get('quantite');

        $action = $this->actionRepo->find($actionId);

        if (!$action) {
            $this->addFlash('danger', '❌ Action introuvable.');
            return $this->redirectToRoute('app_trading_acheter');
        }

        // Vérification stock disponible
        if ($quantite > $action->getQuantiteDisponible()) {
            $this->addFlash('danger', sprintf(
                '❌ Stock insuffisant ! Disponible: %d actions. Vous demandez: %d actions.',
                $action->getQuantiteDisponible(),
                $quantite
            ));
            return $this->redirectToRoute('app_trading_acheter', ['action' => $actionId]);
        }

        try {
            $transaction = $this->transactionService->executerTrade('ACHAT', $actionId, $quantite);

            $this->addFlash('success', sprintf(
                '✅ Achat réussi !<br>• %d actions %s achetées<br>• Montant: %.2f TND<br>• Commission: %.2f TND<br>• Stock restant: %d',
                $quantite,
                $action->getSymbole(),
                $transaction->getMontantTotal(),
                $transaction->getCommission(),
                $action->getQuantiteDisponible()
            ));

            return $this->redirectToRoute('app_trading_historique');

        } catch (\Exception $e) {
            $this->addFlash('danger', '❌ ' . $e->getMessage());
            return $this->redirectToRoute('app_trading_acheter', ['action' => $actionId]);
        }
    }

    /**
     * ✅ Exécuter vente avec validation
     */
    #[Route('/vendre/execute', name: 'app_trading_vendre_execute', methods: ['POST'])]
    public function executeVente(Request $request): Response
    {
        $actionId = (int) $request->request->get('action_id');
        $quantite = (int) $request->request->get('quantite');

        $action = $this->actionRepo->find($actionId);

        if (!$action) {
            $this->addFlash('danger', '❌ Action introuvable.');
            return $this->redirectToRoute('app_trading_vendre');
        }

        // TODO: Vérifier portefeuille utilisateur
        // Pour simulation: on utilise le stock disponible comme limite max de vente
        $stockMax = $action->getQuantiteDisponible();

        try {
            $transaction = $this->transactionService->executerTrade('VENTE', $actionId, $quantite);

            $this->addFlash('success', sprintf(
                '✅ Vente réussie !<br>• %d actions %s vendues<br>• Montant reçu: %.2f TND<br>• Commission: %.2f TND',
                $quantite,
                $action->getSymbole(),
                $transaction->getMontantTotal(),
                $transaction->getCommission()
            ));

            return $this->redirectToRoute('app_trading_historique');

        } catch (\Exception $e) {
            $this->addFlash('danger', '❌ ' . $e->getMessage());
            return $this->redirectToRoute('app_trading_vendre', ['action' => $actionId]);
        }
    }
}