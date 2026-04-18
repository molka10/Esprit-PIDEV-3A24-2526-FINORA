<?php

namespace App\Entity;

use App\Repository\ActionRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;
use Symfony\Bridge\Doctrine\Validator\Constraints\UniqueEntity;

#[ORM\Entity(repositoryClass: ActionRepository::class)]
#[ORM\Table(name: 'action')]
#[UniqueEntity(
    fields: ['symbole'],
    message: 'Ce symbole d\'action existe déjà.'
)]
class Action
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_action', type: 'integer')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Bourse::class)]
    #[ORM\JoinColumn(name: 'id_bourse', referencedColumnName: 'id_bourse', nullable: false)]
    #[Assert\NotNull(message: 'La bourse est obligatoire.')]
    private ?Bourse $bourse = null;

    #[ORM\Column(length: 20, unique: true)]
    #[Assert\NotBlank(message: 'Le symbole est obligatoire.')]
    #[Assert\Length(
        min: 1,
        max: 20,
        minMessage: 'Le symbole doit contenir au moins {{ limit }} caractère.',
        maxMessage: 'Le symbole ne peut pas dépasser {{ limit }} caractères.'
    )]
    #[Assert\Regex(
        pattern: '/^[A-Z0-9]+$/',
        message: 'Le symbole ne peut contenir que des lettres majuscules et des chiffres (ex: AAPL, GOOGL).'
    )]
    private ?string $symbole = null;

    #[ORM\Column(name: 'nom_entreprise', length: 150)]
    #[Assert\NotBlank(message: 'Le nom de l\'entreprise est obligatoire.')]
    #[Assert\Length(
        min: 2,
        max: 150,
        minMessage: 'Le nom doit contenir au moins {{ limit }} caractères.',
        maxMessage: 'Le nom ne peut pas dépasser {{ limit }} caractères.'
    )]
    private ?string $nomEntreprise = null;

    #[ORM\Column(length: 100)]
    #[Assert\NotBlank(message: 'Le secteur est obligatoire.')]
    #[Assert\Length(
        min: 2,
        max: 100,
        minMessage: 'Le secteur doit contenir au moins {{ limit }} caractères.',
        maxMessage: 'Le secteur ne peut pas dépasser {{ limit }} caractères.'
    )]
    #[Assert\Choice(
        choices: ['Technologie', 'Finance', 'Santé', 'Énergie', 'Industrie', 'Consommation', 'Télécommunications', 'Immobilier', 'Autre'],
        message: 'Le secteur sélectionné n\'est pas valide.'
    )]
    private ?string $secteur = null;

    #[ORM\Column(name: 'prix_unitaire', type: 'float')]
    #[Assert\NotBlank(message: 'Le prix unitaire est obligatoire.')]
    #[Assert\Positive(message: 'Le prix unitaire doit être positif.')]
    #[Assert\Range(
        min: 0.01,
        max: 999999.99,
        notInRangeMessage: 'Le prix doit être entre {{ min }} et {{ max }}.'
    )]
    private ?float $prixUnitaire = null;

    #[ORM\Column(name: 'quantite_disponible', type: 'integer')]
    #[Assert\NotBlank(message: 'La quantité est obligatoire.')]
    #[Assert\PositiveOrZero(message: 'La quantité doit être positive ou nulle.')]
    #[Assert\Range(
        min: 0,
        max: 999999999,
        notInRangeMessage: 'La quantité doit être entre {{ min }} et {{ max }}.'
    )]
    private ?int $quantiteDisponible = null;

    #[ORM\Column(length: 20)]
    #[Assert\NotBlank(message: 'Le statut est obligatoire.')]
    #[Assert\Choice(
        choices: ['DISPONIBLE', 'INDISPONIBLE'],
        message: 'Le statut doit être DISPONIBLE ou INDISPONIBLE.'
    )]
    private ?string $statut = 'DISPONIBLE';

    #[ORM\Column(name: 'date_ajout', type: 'datetime')]
    private ?\DateTimeInterface $dateAjout = null;

    public function __construct()
    {
        $this->dateAjout = new \DateTime();
    }

    // Getters & Setters
    public function getId(): ?int
    {
        return $this->id;
    }

    public function getBourse(): ?Bourse
    {
        return $this->bourse;
    }

    public function setBourse(?Bourse $bourse): self
    {
        $this->bourse = $bourse;
        return $this;
    }

    public function getSymbole(): ?string
    {
        return $this->symbole;
    }

   // Action.php
public function setSymbole(?string $symbole): self
{
    $this->symbole = $symbole !== null ? strtoupper($symbole) : null;
    return $this;
}

    public function getNomEntreprise(): ?string
    {
        return $this->nomEntreprise;
    }

    public function setNomEntreprise(string $nomEntreprise): self
    {
        $this->nomEntreprise = $nomEntreprise;
        return $this;
    }

    public function getSecteur(): ?string
    {
        return $this->secteur;
    }

    public function setSecteur(string $secteur): self
    {
        $this->secteur = $secteur;
        return $this;
    }

    public function getPrixUnitaire(): ?float
    {
        return $this->prixUnitaire;
    }

    public function setPrixUnitaire(float $prixUnitaire): self
    {
        $this->prixUnitaire = $prixUnitaire;
        return $this;
    }

    public function getQuantiteDisponible(): ?int
    {
        return $this->quantiteDisponible;
    }

    public function setQuantiteDisponible(int $quantiteDisponible): self
    {
        $this->quantiteDisponible = $quantiteDisponible;
        return $this;
    }

    public function getStatut(): ?string
    {
        return $this->statut;
    }

    public function setStatut(string $statut): self
    {
        $this->statut = $statut;
        return $this;
    }

    public function getDateAjout(): ?\DateTimeInterface
    {
        return $this->dateAjout;
    }

    public function setDateAjout(\DateTimeInterface $dateAjout): self
    {
        $this->dateAjout = $dateAjout;
        return $this;
    }
}