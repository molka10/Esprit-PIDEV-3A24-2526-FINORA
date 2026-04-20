<?php

namespace App\Service;

use App\Entity\TransactionBourse;
use App\Entity\Action;
use App\Repository\TransactionBourseRepository;
use App\Repository\ActionRepository;
use Doctrine\ORM\EntityManagerInterface;
use App\Service\CommissionService;
/**
 * 💹 TransactionService - Logique métier des transactions boursières
 * 
 * Gère : achats, ventes, calculs, validations
 */
class TransactionService
{
    public function __construct(
        private EntityManagerInterface $em,
        private TransactionBourseRepository $transactionRepo,
        private ActionRepository $actionRepo,
        private CommissionService $commissionService,
        private ActionService $actionService,
        private \App\Service\WalletBalanceService $walletBalanceService
    ) {}

    /**
     * Exécute une transaction (ACHAT ou VENTE)
     * 
     * @throws \Exception Si validation échoue
     */
    public function executerTrade(
        string $typeTransaction,
        int $idAction,
        int $quantite,
        \App\Entity\User $user = null
    ): TransactionBourse {
        
        // 1. Récupérer l'action
        $action = $this->actionRepo->find($idAction);
        if (!$action) {
            throw new \Exception("Action introuvable (ID: {$idAction})");
        }

        // 2. Valider le stock
        if ($typeTransaction === 'ACHAT') {
            if ($action->getQuantiteDisponible() < $quantite) {
                throw new \Exception(
                    "Stock insuffisant. Disponible: {$action->getQuantiteDisponible()}, Demandé: {$quantite}"
                );
            }
        }

        // 3. Valider la quantité
        if ($quantite <= 0) {
            throw new \Exception("La quantité doit être positive.");
        }

        // 4. Récupérer le prix actuel
        $prixUnitaire = $action->getPrixUnitaire();

        // 5. Calculer le montant et la commission
        $montant = $prixUnitaire * $quantite;
        $commission = $this->commissionService->calculerCommissionParAction(
            $action->getSymbole(),
            $prixUnitaire,
            $quantite
        );
        
        // Pour un ACHAT : montant total = montant + commission
        // Pour une VENTE : montant total = montant - commission
        $montantTotal = $typeTransaction === 'ACHAT' 
            ? $montant + $commission 
            : $montant - $commission;

        if ($user) {
            // WALLET SYNCHRONISATION
            $categoryRepo = $this->em->getRepository(\App\Entity\Category::class);
            $categoryName = "Trading Bourse";
            $category = $categoryRepo->findOneBy(['nom' => $categoryName]);
            if (!$category) {
                $category = new \App\Entity\Category();
                $category->setNom($categoryName);
                $category->setType('SERVICES');
                $category->setPriorite('MOYENNE');
                $category->setUserId($user->getId());
                $this->em->persist($category);
                $this->em->flush();
            }

            if ($typeTransaction === 'ACHAT') {
                $balance = $this->walletBalanceService->calculateUserBalance($user->getId());
                if ($balance < $montantTotal) {
                    throw new \Exception(sprintf(
                        "Solde insuffisant dans votre portefeuille. Requis: %.2f DT, Actuel: %.2f DT",
                        $montantTotal,
                        $balance
                    ));
                }
                
                $walletTx = new \App\Entity\TransactionWallet();
                $walletTx->setMontant(-abs($montantTotal)); // Negative = expense/outcome
                $walletTx->setType('OUTCOME');
                $walletTx->setDateTransaction(new \DateTime());
                $walletTx->setCategory($category);
                $walletTx->setUserId($user->getId());
                $walletTx->setNomTransaction('Achat de ' . $quantite . ' actions ' . $action->getSymbole());
                $this->em->persist($walletTx);

            } else { // VENTE
                $walletTx = new \App\Entity\TransactionWallet();
                $walletTx->setMontant(abs($montantTotal)); // Positive = income
                $walletTx->setType('INCOME');
                $walletTx->setDateTransaction(new \DateTime());
                $walletTx->setCategory($category);
                $walletTx->setUserId($user->getId());
                $walletTx->setNomTransaction('Vente de ' . $quantite . ' actions ' . $action->getSymbole());
                $this->em->persist($walletTx);
            }
        }

        // 6. Créer la transaction
        $transaction = new TransactionBourse();
        $transaction->setAction($action);
        $transaction->setTypeTransaction($typeTransaction);
        $transaction->setQuantite($quantite);
        $transaction->setPrixUnitaire($prixUnitaire);
        $transaction->setMontantTotal($montantTotal);
        $transaction->setCommission($commission);
        $transaction->setDateTransaction(new \DateTime());
        if ($user) {
            $transaction->setUser($user);
        }

        // 7. Mettre à jour le stock de l'action
        if ($typeTransaction === 'ACHAT') {
            $newStock = $action->getQuantiteDisponible() - $quantite;
            $action->setQuantiteDisponible($newStock);
        } else { // VENTE
            $newStock = $action->getQuantiteDisponible() + $quantite;
            $action->setQuantiteDisponible($newStock);
        }

        // Mettre à jour le statut de l'action automatiquement
        $this->actionService->update($action);

        // 8. Sauvegarder
        $this->em->persist($transaction);
        $this->em->flush();

        return $transaction;
    }

    /**
     * Récupère toutes les transactions
     */
    public function findAll(): array
    {
        return $this->transactionRepo->findBy([], ['dateTransaction' => 'DESC']);
    }

    /**
     * Récupère les transactions d'un utilisateur
     */
    public function findByUser(int $userId): array
    {
        return $this->transactionRepo->findByUser($userId);
    }

    /**
     * Récupère une transaction par ID
     */
    public function findById(int $id): ?TransactionBourse
    {
        return $this->transactionRepo->find($id);
    }

    /**
     * Récupère les transactions récentes
     */
    public function getRecent(int $limit = 10): array
    {
        return $this->transactionRepo->findRecent($limit);
    }

    /**
     * Calcule le total des achats d'un utilisateur
     */
    public function getTotalAchats(?int $userId = null): float
    {
        if ($userId) {
            return $this->transactionRepo->getTotalAchatsByUser($userId);
        }

        $result = $this->transactionRepo->createQueryBuilder('t')
            ->select('SUM(t.montantTotal)')
            ->where('t.typeTransaction = :type')
            ->setParameter('type', 'ACHAT')
            ->getQuery()
            ->getSingleScalarResult();

        return $result ?? 0;
    }

    /**
     * Calcule le total des ventes d'un utilisateur
     */
    public function getTotalVentes(?int $userId = null): float
    {
        if ($userId) {
            return $this->transactionRepo->getTotalVentesByUser($userId);
        }

        $result = $this->transactionRepo->createQueryBuilder('t')
            ->select('SUM(t.montantTotal)')
            ->where('t.typeTransaction = :type')
            ->setParameter('type', 'VENTE')
            ->getQuery()
            ->getSingleScalarResult();

        return $result ?? 0;
    }

    /**
     * Calcule le total des commissions
     */
    public function getTotalCommissions(?int $userId = null): float
    {
        if ($userId) {
            return $this->transactionRepo->getTotalCommissionsByUser($userId);
        }

        $result = $this->transactionRepo->createQueryBuilder('t')
            ->select('SUM(t.commission)')
            ->getQuery()
            ->getSingleScalarResult();

        return $result ?? 0;
    }

    /**
     * Récupère les statistiques globales
     */
    public function getStatistics(): array
    {
        return $this->transactionRepo->getStatistics();
    }

    /**
     * Annule une transaction (inverse l'opération sur le stock)
     * 
     * @throws \Exception Si l'annulation n'est pas possible
     */
    public function annuler(TransactionBourse $transaction): void
    {
        $action = $transaction->getAction();

        // Inverser l'opération sur le stock
        if ($transaction->getTypeTransaction() === 'ACHAT') {
            // C'était un achat, on remet le stock
            $newStock = $action->getQuantiteDisponible() + $transaction->getQuantite();
        } else {
            // C'était une vente, on retire du stock
            $newStock = $action->getQuantiteDisponible() - $transaction->getQuantite();
            
            if ($newStock < 0) {
                throw new \Exception("Impossible d'annuler : stock insuffisant.");
            }
        }

        $action->setQuantiteDisponible($newStock);
        $this->actionService->update($action);

        // Supprimer la transaction
        $this->em->remove($transaction);
        $this->em->flush();
    }

    /**
     * Valide les données d'une transaction
     */
    public function validate(TransactionBourse $transaction): array
    {
        $errors = [];

        if (!$transaction->getAction()) {
            $errors[] = "L'action est obligatoire.";
        }

        if ($transaction->getQuantite() <= 0) {
            $errors[] = "La quantité doit être positive.";
        }

        if ($transaction->getPrixUnitaire() <= 0) {
            $errors[] = "Le prix unitaire doit être positif.";
        }

        if (!in_array($transaction->getTypeTransaction(), ['ACHAT', 'VENTE'])) {
            $errors[] = "Le type de transaction doit être ACHAT ou VENTE.";
        }

        return $errors;
    }
}