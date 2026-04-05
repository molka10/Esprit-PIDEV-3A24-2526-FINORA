<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;

#[ORM\Entity]
#[ORM\Table(name: "category")]

class Category
{

#[ORM\OneToMany(mappedBy: "category", targetEntity: TransactionWallet::class)]
private Collection $transactions;

public function __construct()
{
    $this->transactions = new ArrayCollection();
}



    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: "id_category")]
    private ?int $id = null;

    #[ORM\Column]
    private ?string $nom = null;

    #[ORM\Column]
    private ?string $type = null;

   #[ORM\Column(type: "string")]
    private ?string $priorite = null;

    #[ORM\Column(name: "user_id", nullable: true)]
    private ?int $userId = null;

    // GETTERS & SETTERS

    public function getId(): ?int { return $this->id; }

    public function getNom(): ?string { return $this->nom; }
    public function setNom(string $nom): self { $this->nom = $nom; return $this; }

    public function getType(): ?string { return $this->type; }
    public function setType(string $type): self { $this->type = $type; return $this; }

    public function getPriorite(): ?string { return $this->priorite; }

    public function setPriorite(string $priorite): self { $this->priorite = $priorite; return $this; }

    public function getUserId(): ?int { return $this->userId; }
    public function setUserId(int $id): self { $this->userId = $id; return $this; }
}