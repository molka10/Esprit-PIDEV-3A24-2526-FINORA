<?php

namespace App\Entity;

use App\Repository\CandidatureRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: CandidatureRepository::class)]
#[ORM\HasLifecycleCallbacks]
class Candidature
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;
    
    public function __construct()
    {
        $this->statut = 'submitted';
    }

    #[ORM\Column(type: Types::DECIMAL, precision: 10, scale: 2, nullable: true)]
    #[Assert\Positive(message: 'Le montant proposé doit être positif')]
    #[Assert\LessThanOrEqual(
        value: 9999999999,
        message: 'Le montant proposé est trop élevé'
    )]
    private ?string $montantPropose = null;

    #[ORM\Column(type: Types::TEXT, nullable: true)]
    #[Assert\Length(
        min: 20,
        minMessage: 'Le message doit contenir au moins {{ limit }} caractères'
    )]
    private ?string $message = null;

    #[ORM\Column(length: 50)]
    #[Assert\NotBlank(message: 'Le statut est obligatoire')]
    #[Assert\Choice(
        choices: ['draft', 'submitted', 'accepted', 'rejected', 'withdrawn'],
        message: 'Statut invalide'
    )]
    private string $statut;

    #[ORM\Column(type: Types::DATETIME_IMMUTABLE)]
    private \DateTimeImmutable $createdAt;

    #[ORM\ManyToOne(targetEntity: AppelOffre::class, inversedBy: 'candidatures')]
    #[ORM\JoinColumn(name: 'appel_offre_id', referencedColumnName: 'id', nullable: false, onDelete: 'CASCADE')]
    #[Assert\NotNull(message: "L'appel d'offre est obligatoire")]
    private ?AppelOffre $appelOffre = null;

    #[ORM\ManyToOne(inversedBy: 'candidatures')]
    private ?User $user = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $cvPath = null;

    #[ORM\Column(nullable: true)]
    private ?int $aiScore = null;

    #[ORM\Column(type: Types::TEXT, nullable: true)]
    private ?string $aiAnalysis = null;

    #[ORM\PrePersist]
    public function setCreatedAtValue(): void
    {
        if (!isset($this->createdAt)) {
            $this->createdAt = new \DateTimeImmutable();
        }
    }

    public function getId(): ?int { return $this->id; }
    public function getMontantPropose(): ?string { return $this->montantPropose; }
    public function setMontantPropose(?string $montantPropose): static { $this->montantPropose = $montantPropose; return $this; }
    public function getMessage(): ?string { return $this->message; }
    public function setMessage(?string $message): static { $this->message = $message; return $this; }
    public function getStatut(): ?string { return $this->statut; }
    public function setStatut(string $statut): static { $this->statut = $statut; return $this; }
    public function getCreatedAt(): \DateTimeImmutable
    {
        return $this->createdAt;
    }

    public function setCreatedAt(\DateTimeImmutable $createdAt): static { $this->createdAt = $createdAt; return $this; }
    public function getAppelOffre(): ?AppelOffre { return $this->appelOffre; }
    public function setAppelOffre(?AppelOffre $appelOffre): static { $this->appelOffre = $appelOffre; return $this; }
    public function getAppelOffreId(): ?int { return $this->appelOffre ? $this->appelOffre->getId() : null; }
    public function getUser(): ?User { return $this->user; }
    public function setUser(?User $user): static { $this->user = $user; return $this; }

    public function getCvPath(): ?string
    {
        return $this->cvPath;
    }

    public function setCvPath(?string $cvPath): static
    {
        $this->cvPath = $cvPath;

        return $this;
    }

    public function getAiScore(): ?int
    {
        return $this->aiScore;
    }

    public function setAiScore(?int $aiScore): static
    {
        $this->aiScore = $aiScore;

        return $this;
    }

    public function getAiAnalysis(): ?string
    {
        return $this->aiAnalysis;
    }

    public function setAiAnalysis(?string $aiAnalysis): static
    {
        $this->aiAnalysis = $aiAnalysis;

        return $this;
    }
}