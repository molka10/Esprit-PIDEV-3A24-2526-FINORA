<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use App\Entity\Category;
use App\Repository\TransactionWalletRepository;
use Symfony\Component\Validator\Constraints as Assert;
#[ORM\Entity(repositoryClass: TransactionWalletRepository::class)]
#[ORM\Table(name: "transaction_wallet")]
class TransactionWallet
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: "id_transaction")]
    private ?int $id = null;

    #[ORM\Column(name: "nom_transaction")]
    #[Assert\NotBlank(message: "Nom obligatoire")]
    private ?string $nomTransaction = null;

    #[ORM\Column]
    private ?string $type = null;


    #[ORM\Column(name: "montant")]
    #[Assert\NotBlank(message: "Montant obligatoire")]

    private ?float $montant = null;

    #[ORM\Column(name: "date_transaction", type: "datetime")]    
    #[Assert\NotNull(message: "Date obligatoire")]
    private ?\DateTimeInterface $dateTransaction = null;

    #[ORM\Column(nullable: true)]
    private ?string $source = null;

    #[ORM\ManyToOne(targetEntity: User::class, inversedBy: "walletTransactions")]
    #[ORM\JoinColumn(name: "user_id", referencedColumnName: "id", nullable: true)]
    private ?User $user = null;

    #[ORM\Column(type: "boolean", options: ["default" => true])]
    private ?bool $isActive = true;

    #[ORM\Column(length: 20, options: ["default" => "ACCEPTED"])]
    private string $status = "ACCEPTED";

    public function getUser(): ?User
    {
        return $this->user;
    }

    public function setUser(?User $user): static
    {
        $this->user = $user;
        return $this;
    }

    public function getUserId(): ?int
    {
        return $this->user ? $this->user->getId() : null;
    }

    public function setUserId(?int $userId): static
    {
        // This is a helper for legacy code
        // We will need the EntityManager to fetch the user if we really want to set it via ID
        // For now, we keep the getter compatible.
        return $this;
    }

 

    #[ORM\ManyToOne(targetEntity: Category::class, inversedBy: "transactions")]
    #[ORM\JoinColumn(name: "category_id", referencedColumnName: "id_category", nullable: false)]
    #[Assert\NotNull(message: "Veuillez sélectionner une catégorie")]
    private ?Category $category = null;

    
    // getters & setters

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getNomTransaction(): ?string
    {
        return $this->nomTransaction;
    }

    public function setNomTransaction(?string $nomTransaction): static
    {
        $this->nomTransaction = $nomTransaction;
        return $this;
    }

    public function getType(): ?string
    {
        return $this->type;
    }

    public function setType(string $type): static
    {
        $this->type = $type;
        return $this;
    }

    public function getMontant(): ?float
    {
        return $this->montant;
    }

  public function setMontant(?float $montant): static
    {
        $this->montant = $montant;
        return $this;
    }

    public function getDateTransaction(): ?\DateTimeInterface
    {
        return $this->dateTransaction;
    }

    public function setDateTransaction(?\DateTimeInterface $dateTransaction): static
    {
        $this->dateTransaction = $dateTransaction;
        return $this;
    }



    public function getIsActive(): ?bool
    {
        return $this->isActive;
    }

    public function setIsActive(bool $isActive): static
    {
        $this->isActive = $isActive;
        return $this;
    }

    public function setSource(?string $source): static
    {
        $this->source = $source;
        return $this;
    }


public function getCategory(): ?Category
{
    return $this->category;
}

public function setCategory(?Category $category): self
{
    $this->category = $category;
    return $this;
}

public function getStatus(): string
{
    return $this->status;
}

public function setStatus(string $status): self
{
    $this->status = $status;
    return $this;
}

}