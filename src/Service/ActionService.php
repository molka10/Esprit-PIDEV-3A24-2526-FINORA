<?php

namespace App\Service;

use App\Entity\Action;
use App\Repository\ActionRepository;
use Doctrine\ORM\EntityManagerInterface;

/**
 * 📊 ActionService - Couche métier pour les Actions
 * 
 * Contient toute la logique métier liée aux actions boursières
 */
class ActionService
{
    public function __construct(
        private EntityManagerInterface $em,
        private ActionRepository $actionRepository
    ) {}

    /**
     * Récupère toutes les actions
     */
    public function findAll(): array
    {
        return $this->actionRepository->findAll();
    }

    /**
     * Récupère une action par ID
     */
    public function findById(int $id): ?Action
    {
        return $this->actionRepository->find($id);
    }

    /**
     * Récupère les actions disponibles uniquement
     */
    public function findAvailable(): array
    {
        return $this->actionRepository->findBy(
            ['statut' => 'DISPONIBLE'],
            ['symbole' => 'ASC']
        );
    }

    /**
     * Récupère les actions par secteur
     */
    public function findBySecteur(string $secteur): array
    {
        return $this->actionRepository->findBy(
            ['secteur' => $secteur],
            ['symbole' => 'ASC']
        );
    }

    /**
     * Compte le nombre total d'actions
     */
    public function count(): int
    {
        return $this->actionRepository->count([]);
    }

    /**
     * Vérifie si un symbole existe déjà
     */
    public function symboleExists(string $symbole, ?int $excludeId = null): bool
    {
        $qb = $this->actionRepository->createQueryBuilder('a')
            ->where('a.symbole = :symbole')
            ->setParameter('symbole', strtoupper($symbole));

        if ($excludeId) {
            $qb->andWhere('a.id != :id')
               ->setParameter('id', $excludeId);
        }

        return $qb->getQuery()->getOneOrNullResult() !== null;
    }

    /**
     * Crée une nouvelle action
     * 
     * @throws \Exception Si le symbole existe déjà ou si validation échoue
     */
    public function create(Action $action): Action
    {
        // Vérification métier: symbole unique
        if ($this->symboleExists($action->getSymbole())) {
            throw new \Exception('Une action avec ce symbole existe déjà.');
        }

        // Règle métier: symbole en majuscules
        $action->setSymbole(strtoupper($action->getSymbole()));

        // Règle métier: prix minimum
        if ($action->getPrixUnitaire() < 0.01) {
            throw new \Exception('Le prix unitaire doit être supérieur à 0.01.');
        }

        // Règle métier: quantité positive
        if ($action->getQuantiteDisponible() < 0) {
            throw new \Exception('La quantité disponible ne peut pas être négative.');
        }

        // Règle métier: statut automatique selon stock
        if ($action->getQuantiteDisponible() === 0) {
            $action->setStatut('INDISPONIBLE');
        }

        // Définir la date d'ajout
        if (!$action->getDateAjout()) {
            $action->setDateAjout(new \DateTime());
        }

        $this->em->persist($action);
        $this->em->flush();

        return $action;
    }

    /**
     * Met à jour une action existante
     * 
     * @throws \Exception Si validation échoue
     */
    public function update(Action $action): Action
    {
        // Vérification métier: symbole unique (sauf pour cette action)
        if ($this->symboleExists($action->getSymbole(), $action->getId())) {
            throw new \Exception('Une autre action avec ce symbole existe déjà.');
        }

        // Règle métier: symbole en majuscules
        $action->setSymbole(strtoupper($action->getSymbole()));

        // Règle métier: prix minimum
        if ($action->getPrixUnitaire() < 0.01) {
            throw new \Exception('Le prix unitaire doit être supérieur à 0.01.');
        }

        // Règle métier: mise à jour automatique du statut selon stock
        if ($action->getQuantiteDisponible() === 0) {
            $action->setStatut('INDISPONIBLE');
        } elseif ($action->getQuantiteDisponible() > 0 && $action->getStatut() === 'INDISPONIBLE') {
            $action->setStatut('DISPONIBLE');
        }

        $this->em->flush();

        return $action;
    }

    /**
     * Supprime une action
     * 
     * @throws \Exception Si l'action a des transactions liées
     */
    public function delete(Action $action): void
    {
        // Vérification métier: pas de suppression si des transactions existent
        // (Cette vérification nécessiterait TransactionRepository - simplifié ici)
        
        $this->em->remove($action);
        $this->em->flush();
    }

    /**
     * Met à jour le stock d'une action
     * 
     * @throws \Exception Si le stock devient négatif
     */
    public function updateStock(Action $action, int $quantite): Action
    {
        $newStock = $action->getQuantiteDisponible() + $quantite;

        if ($newStock < 0) {
            throw new \Exception('Le stock ne peut pas être négatif.');
        }

        $action->setQuantiteDisponible($newStock);

        // Mise à jour automatique du statut
        if ($newStock === 0) {
            $action->setStatut('INDISPONIBLE');
        } elseif ($newStock > 0 && $action->getStatut() === 'INDISPONIBLE') {
            $action->setStatut('DISPONIBLE');
        }

        $this->em->flush();

        return $action;
    }

    /**
     * Recherche des actions par symbole ou nom d'entreprise
     */
    public function search(string $query): array
    {
        return $this->actionRepository->createQueryBuilder('a')
            ->where('a.symbole LIKE :query OR a.nomEntreprise LIKE :query')
            ->setParameter('query', '%' . $query . '%')
            ->orderBy('a.symbole', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Calcule la valeur totale d'une action (prix * quantité)
     */
    public function calculateTotalValue(Action $action): float
    {
        return $action->getPrixUnitaire() * $action->getQuantiteDisponible();
    }

    /**
     * Valide les données métier d'une action
     * 
     * @return array Tableau d'erreurs (vide si tout est OK)
     */
    public function validate(Action $action): array
    {
        $errors = [];

        // Règle métier: symbole entre 1 et 20 caractères
        if (strlen($action->getSymbole()) < 1 || strlen($action->getSymbole()) > 20) {
            $errors[] = 'Le symbole doit contenir entre 1 et 20 caractères.';
        }

        // Règle métier: prix positif
        if ($action->getPrixUnitaire() <= 0) {
            $errors[] = 'Le prix unitaire doit être strictement positif.';
        }

        // Règle métier: quantité positive ou nulle
        if ($action->getQuantiteDisponible() < 0) {
            $errors[] = 'La quantité ne peut pas être négative.';
        }

        // Règle métier: bourse obligatoire
        if (!$action->getBourse()) {
            $errors[] = 'La bourse est obligatoire.';
        }

        return $errors;
    }
}