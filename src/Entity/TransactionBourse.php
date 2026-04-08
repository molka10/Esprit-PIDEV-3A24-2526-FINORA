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
        name: 'id_action',
        referencedColumnName: 'id_action', // ✅ TRÈS IMPORTANT
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
    private ?string $typeTransaction = null;

    // =============================
    // 🔹 QUANTITÉ
    // =============================
    #[ORM\Column]
    #[Assert\NotBlank(message: 'La quantité est obligatoire.')]
    #[Assert\Positive(message: 'La quantité doit être positive.')]
    private ?int $quantite = null;

    // =============================
    // 🔹 PRIX
    // =============================
    #[ORM\Column(name: 'prix_unitaire', type: 'float')]
    #[Assert\NotBlank(message: 'Le prix unitaire est obligatoire.')]
    #[Assert\Positive(message: 'Le prix doit être positif.')]
    private ?float $prixUnitaire = null;

    // =============================
    // 🔹 MONTANT TOTAL
    // =============================
    #[ORM\Column(name: 'montant_total', type: 'float')]
    private ?float $montantTotal = null;

    // =============================
    // 🔹 COMMISSION
    // =============================
    #[ORM\Column(type: 'float')]
    private ?float $commission = 0;

    // =============================
    // 🔹 DATE
    // =============================
    #[ORM\Column(name: 'date_transaction', type: 'datetime')]
    private ?\DateTimeInterface $dateTransaction = null;

    public function __construct()
    {
        $this->dateTransaction = new \DateTime();
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

    public function getTypeTransaction(): ?string
    {
        return $this->typeTransaction;
    }

    public function setTypeTransaction(string $typeTransaction): self
    {
        $this->typeTransaction = $typeTransaction;
        return $this;
    }

    public function getQuantite(): ?int
    {
        return $this->quantite;
    }

    public function setQuantite(int $quantite): self
    {
        $this->quantite = $quantite;
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

    public function getMontantTotal(): ?float
    {
        return $this->montantTotal;
    }

    public function setMontantTotal(float $montantTotal): self
    {
        $this->montantTotal = $montantTotal;
        return $this;
    }

    public function getCommission(): ?float
    {
        return $this->commission;
    }

    public function setCommission(float $commission): self
    {
        $this->commission = $commission;
        return $this;
    }

    public function getDateTransaction(): ?\DateTimeInterface
    {
        return $this->dateTransaction;
    }

    public function setDateTransaction(\DateTimeInterface $dateTransaction): self
    {
        $this->dateTransaction = $dateTransaction;
        return $this;
    }
}