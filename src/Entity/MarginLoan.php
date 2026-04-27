<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: 'margin_loan')]
class MarginLoan
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(name: 'user_id')]
    private ?int $userId = null;

    #[ORM\Column(type: 'float')]
    private ?float $montantEmprunte = null;

    #[ORM\Column(length: 50)]
    private ?string $statut = 'ACTIF';

    #[ORM\Column(type: 'datetime')]
    private ?\DateTimeInterface $dateEmprunt = null;

    public function __construct()
    {
        $this->dateEmprunt = new \DateTime();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getUserId(): ?int
    {
        return $this->userId;
    }

    public function setUserId(int $userId): static
    {
        $this->userId = $userId;
        return $this;
    }

    public function getMontantEmprunte(): ?float
    {
        return $this->montantEmprunte;
    }

    public function setMontantEmprunte(float $montantEmprunte): static
    {
        $this->montantEmprunte = $montantEmprunte;
        return $this;
    }

    public function getStatut(): ?string
    {
        return $this->statut;
    }

    public function setStatut(string $statut): static
    {
        $this->statut = $statut;
        return $this;
    }

    public function getDateEmprunt(): ?\DateTimeInterface
    {
        return $this->dateEmprunt;
    }

    public function setDateEmprunt(\DateTimeInterface $dateEmprunt): static
    {
        $this->dateEmprunt = $dateEmprunt;
        return $this;
    }
}
