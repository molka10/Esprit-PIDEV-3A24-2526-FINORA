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

    #[ORM\Column(name: "user_id")]
    private ?int $userId = null;

    public function getUserId(): ?int
{
    return $this->userId;
}

 

    #[ORM\ManyToOne(targetEntity: Category::class)]
    #[ORM\JoinColumn(name: "category_id", referencedColumnName: "id_category", nullable: false)]
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

    public function setUserId(int $userId): static
    {
        $this->userId = $userId;
        return $this;
    }

    public function setCategoryId(int $categoryId): static
    {
        $this->categoryId = $categoryId;
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



}