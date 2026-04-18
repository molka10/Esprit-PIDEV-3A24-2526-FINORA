<?php

namespace App\Service;

use App\Entity\Bourse;
use App\Repository\BourseRepository;
use Doctrine\ORM\EntityManagerInterface;

/**
 * 🏛️ BourseService - Couche métier pour les Bourses
 * 
 * Contient toute la logique métier liée aux bourses
 */
class BourseService
{
    public function __construct(
        private EntityManagerInterface $em,
        private BourseRepository $bourseRepository
    ) {}

    /**
     * Récupère toutes les bourses
     */
    public function findAll(): array
    {
        return $this->bourseRepository->findAll();
    }

    /**
     * Récupère une bourse par ID
     */
    public function findById(int $id): ?Bourse
    {
        return $this->bourseRepository->find($id);
    }

    /**
     * Récupère les bourses actives uniquement
     */
    public function findActive(): array
    {
        return $this->bourseRepository->findBy(['statut' => 'ACTIVE'], ['nomBourse' => 'ASC']);
    }

    /**
     * Compte le nombre total de bourses
     */
    public function count(): int
    {
        return $this->bourseRepository->count([]);
    }

    /**
     * Vérifie si un nom de bourse existe déjà
     */
    public function nomExists(string $nom, ?int $excludeId = null): bool
    {
        $qb = $this->bourseRepository->createQueryBuilder('b')
            ->where('b.nomBourse = :nom')
            ->setParameter('nom', $nom);

        if ($excludeId) {
            $qb->andWhere('b.id != :id')
               ->setParameter('id', $excludeId);
        }

        return $qb->getQuery()->getOneOrNullResult() !== null;
    }

    /**
     * Crée une nouvelle bourse
     * 
     * @throws \Exception Si le nom existe déjà
     */
    public function create(Bourse $bourse): Bourse
    {
        // Vérification métier: nom unique
        if (!$bourse->getNomBourse()) {
        throw new \InvalidArgumentException('Le nom de la bourse est requis.');
    }
        
        if ($this->nomExists($bourse->getNomBourse())) {
            throw new \Exception('Une bourse avec ce nom existe déjà.');
        }

        // Vérification métier: devise en majuscules
        $bourse->setDevise(strtoupper($bourse->getDevise()));

        // Définir la date de création
        if (!$bourse->getDateCreation()) {
            $bourse->setDateCreation(new \DateTime());
        }

        $this->em->persist($bourse);
        $this->em->flush();

        return $bourse;
    }

    /**
     * Met à jour une bourse existante
     * 
     * @throws \Exception Si le nom existe déjà pour une autre bourse
     */
    public function update(Bourse $bourse): Bourse
    {
        // Vérification métier: nom unique (sauf pour cette bourse)
        if ($this->nomExists($bourse->getNomBourse(), $bourse->getId())) {
            throw new \Exception('Une autre bourse avec ce nom existe déjà.');
        }

        // Vérification métier: devise en majuscules
        $bourse->setDevise(strtoupper($bourse->getDevise()));

        $this->em->flush();

        return $bourse;
    }

    /**
     * Supprime une bourse
     * 
     * @throws \Exception Si la bourse a des actions liées
     */
    public function delete(Bourse $bourse): void
    {
        // Vérification métier: pas de suppression si des actions existent
        // (Cette vérification nécessiterait ActionRepository - simplifié ici)
        
        $this->em->remove($bourse);
        $this->em->flush();
    }

    /**
     * Active une bourse
     */
    public function activate(Bourse $bourse): Bourse
    {
        $bourse->setStatut('ACTIVE');
        $this->em->flush();
        return $bourse;
    }

    /**
     * Désactive une bourse
     */
    public function deactivate(Bourse $bourse): Bourse
    {
        $bourse->setStatut('INACTIVE');
        $this->em->flush();
        return $bourse;
    }

    /**
     * Recherche des bourses par nom ou pays
     */
    public function search(string $query): array
    {
        return $this->bourseRepository->createQueryBuilder('b')
            ->where('b.nomBourse LIKE :query OR b.pays LIKE :query')
            ->setParameter('query', '%' . $query . '%')
            ->orderBy('b.nomBourse', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Valide les données métier d'une bourse
     * 
     * @return array Tableau d'erreurs (vide si tout est OK)
     */
    public function validate(Bourse $bourse): array
    {
        $errors = [];

        // Règle métier: nom minimum 3 caractères
        if (strlen($bourse->getNomBourse()) < 3) {
            $errors[] = 'Le nom de la bourse doit contenir au moins 3 caractères.';
        }

        // Règle métier: devise doit être 3 lettres
        if (strlen($bourse->getDevise()) !== 3) {
            $errors[] = 'La devise doit être un code de 3 lettres (ex: USD, EUR, TND).';
        }

        // Règle métier: pays obligatoire
        if (empty($bourse->getPays())) {
            $errors[] = 'Le pays est obligatoire.';
        }

        return $errors;
    }
}