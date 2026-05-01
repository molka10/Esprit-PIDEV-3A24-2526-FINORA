<?php

namespace App\Entity;

use App\Repository\TransactionBourseRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: TransactionBourseRepository::class)]
#[ORM\Table(name: 'transaction_bourse')]
class TransactionBourse
{
    // =============================
    // 🔹 ID
    // =============================
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_transaction')]
    private ?int $id = null;

    // =============================
    // 🔥 RELATION AVEC ACTION (FIX)
    // =============================
    #[ORM\ManyToOne(targetEntity: Action::class)]
    #[ORM\JoinColumn(
        name: 'action_id',
        referencedColumnName: 'id_action',
        nullable: false
    )]
    #[Assert\NotNull(message: "L'action est obligatoire.")]
    private ?Action $action = null;

    // =============================
    // 🔹 TYPE TRANSACTION
    // =============================
    #[ORM\Column(name: 'type_transaction', length: 20)]
    #[Assert\NotBlank(message: 'Le type de transaction est obligatoire.')]
    #[Assert\Choice(
        choices: ['ACHAT', 'VENTE'],
        message: 'Le type doit être ACHAT ou VENTE.'
    )]
    private string $typeTransaction;

    // =============================
    // 🔹 QUANTITÉ
    // =============================
    #[ORM\Column]
    #[Assert\NotBlank(message: 'La quantité est obligatoire.')]
    #[Assert\Positive(message: 'La quantité doit être positive.')]
    private int $quantite;

    // =============================
    // 🔹 PRIX
    // =============================
    #[ORM\Column(name: 'prix_unitaire', type: 'decimal', precision: 10, scale: 2)]
    #[Assert\NotBlank(message: 'Le prix unitaire est obligatoire.')]
    #[Assert\Positive(message: 'Le prix doit être positif.')]
    private string $prixUnitaire;

    // =============================
    // 🔹 MONTANT TOTAL
    // =============================
    #[ORM\Column(name: 'montant_total', type: 'decimal', precision: 10, scale: 2)]
    private string $montantTotal;

    // =============================
    // 🔹 COMMISSION
    // =============================
    #[ORM\Column(type: 'decimal', precision: 10, scale: 2)]
    private string $commission = '0';

    // =============================
    // 🔹 DATE
    // =============================
    #[ORM\Column(name: 'date_transaction', type: 'datetime_immutable')]
    private \DateTimeImmutable $dateTransaction;

    // =============================
    // 👤 RELATION AVEC UTILISATEUR
    // =============================
    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: 'user_id', referencedColumnName: 'id', nullable: true)]
    private ?User $user = null;

    #[ORM\Column(name: 'acteur_role', length: 50, nullable: true)]
    private ?string $acteurRole = null;

    #[ORM\Column(name: 'acteur_label', length: 100, nullable: true)]
    private ?string $acteurLabel = null;

    public function __construct()
    {
        $this->dateTransaction = new \DateTimeImmutable();
    }

    // =============================
    // 🔹 GETTERS & SETTERS
    // =============================

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getAction(): ?Action
    {
        return $this->action;
    }

    public function setAction(?Action $action): self
    {
        $this->action = $action;
        return $this;
    }

    public function getTypeTransaction(): string
    {
        return $this->typeTransaction;
    }

    public function setTypeTransaction(string $typeTransaction): self
    {
        $this->typeTransaction = $typeTransaction;
        return $this;
    }

    public function getQuantite(): int
    {
        return $this->quantite;
    }

    public function setQuantite(int $quantite): self
    {
        $this->quantite = $quantite;
        return $this;
    }

    public function getPrixUnitaire(): string
    {
        return $this->prixUnitaire;
    }

    public function setPrixUnitaire(string $prixUnitaire): self
    {
        $this->prixUnitaire = $prixUnitaire;
        return $this;
    }

    public function getMontantTotal(): string
    {
        return $this->montantTotal;
    }

    public function setMontantTotal(string $montantTotal): self
    {
        $this->montantTotal = $montantTotal;
        return $this;
    }

    public function getCommission(): string
    {
        return $this->commission;
    }

    public function setCommission(string $commission): self
    {
        $this->commission = $commission;
        return $this;
    }

    public function getDateTransaction(): \DateTimeImmutable
    {
        return $this->dateTransaction;
    }

    public function setDateTransaction(\DateTimeImmutable $dateTransaction): self
    {
        $this->dateTransaction = $dateTransaction;
        return $this;
    }

    public function getUser(): ?User
    {
        return $this->user;
    }

    public function setUser(?User $user): self
    {
        $this->user = $user;
        return $this;
    }

    public function getActeurRole(): ?string
    {
        return $this->acteurRole;
    }

    public function setActeurRole(?string $acteurRole): self
    {
        $this->acteurRole = $acteurRole;
        return $this;
    }

    public function getActeurLabel(): ?string
    {
        return $this->acteurLabel;
    }

    public function setActeurLabel(?string $acteurLabel): self
    {
        $this->acteurLabel = $acteurLabel;
        return $this;
    }
}