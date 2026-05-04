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

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(nullable: false, onDelete: 'CASCADE')]
    private User $user;

    #[ORM\Column(name: 'montant_emprunte', type: 'decimal', precision: 10, scale: 2)]
    private ?string $montantEmprunte = null;

    #[ORM\Column(length: 50)]
    private string $statut = 'ACTIF';

    #[ORM\Column(type: 'datetime_immutable')]
    private \DateTimeImmutable $dateEmprunt;

    public function __construct(User $user, string $montant)
    {
        $this->user = $user;
        $this->montantEmprunte = $montant;
        $this->dateEmprunt = new \DateTimeImmutable();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getUser(): User
    {
        return $this->user;
    }

    public function setUser(User $user): static
    {
        $this->user = $user;
        return $this;
    }

    public function getMontantEmprunte(): ?string
    {
        return $this->montantEmprunte;
    }

    public function setMontantEmprunte(string $montantEmprunte): static
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

    public function setDateEmprunt(\DateTimeImmutable $dateEmprunt): static
    {
        $this->dateEmprunt = $dateEmprunt;
        return $this;
    }
}
