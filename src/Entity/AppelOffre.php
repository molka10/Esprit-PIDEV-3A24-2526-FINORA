<?php

namespace App\Entity;

use App\Repository\AppelOffreRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: AppelOffreRepository::class)]
#[ORM\HasLifecycleCallbacks]
class AppelOffre
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: 'Le titre est obligatoire')]
    #[Assert\Length(
        min: 3,
        max: 255,
        minMessage: 'Le titre doit contenir au moins {{ limit }} caractères',
        maxMessage: 'Le titre ne peut pas dépasser {{ limit }} caractères'
    )]
    private ?string $titre = null;

    #[ORM\Column(type: Types::TEXT, nullable: true)]
    #[Assert\Length(
        min: 10,
        minMessage: 'La description doit contenir au moins {{ limit }} caractères'
    )]
    private ?string $description = null;

    #[ORM\Column(length: 50)]
    #[Assert\NotBlank(message: 'Le type est obligatoire')]
    #[Assert\Choice(
        choices: ['achat', 'partenariat', 'don'],
        message: 'Le type doit être achat, partenariat ou don'
    )]
    private ?string $type = null;

    #[ORM\Column(type: Types::DECIMAL, precision: 10, scale: 2, nullable: true)]
    #[Assert\Positive(message: 'Le budget minimum doit être positif')]
    private ?string $budgetMin = null;

    #[ORM\Column(type: Types::DECIMAL, precision: 10, scale: 2, nullable: true)]
    #[Assert\Positive(message: 'Le budget maximum doit être positif')]
    #[Assert\GreaterThanOrEqual(
        propertyPath: 'budgetMin',
        message: 'Le budget maximum doit être supérieur au budget minimum'
    )]
    private ?string $budgetMax = null;

    #[ORM\Column(length: 20, nullable: true)]
    #[Assert\Choice(
        choices: ['TND', 'EUR', 'USD'],
        message: 'La devise doit être TND, EUR ou USD'
    )]
    private ?string $devise = null;

    #[ORM\Column(type: Types::DATE_MUTABLE, nullable: true)]
    #[Assert\GreaterThan(
        value: 'today',
        message: 'La date limite doit être dans le futur'
    )]
    private ?\DateTimeInterface $dateLimite = null;

    #[ORM\Column(length: 50)]
    #[Assert\NotBlank(message: 'Le statut est obligatoire')]
    #[Assert\Choice(
        choices: ['draft', 'published', 'closed'],
        message: 'Le statut doit être draft, published ou closed'
    )]
    private ?string $statut = null;

    #[ORM\Column(type: Types::DATETIME_MUTABLE)]
    private ?\DateTimeInterface $createdAt = null;

    #[ORM\ManyToOne(inversedBy: 'appelOffres')]
    private ?Categorie $categorie = null;

    #[ORM\ManyToOne(inversedBy: 'appelOffres')]
    private ?User $createdBy = null;

    #[ORM\OneToMany(targetEntity: Candidature::class, mappedBy: 'appelOffre', cascade: ['remove'], orphanRemoval: true)]
    private Collection $candidatures;

    #[ORM\OneToMany(targetEntity: Rating::class, mappedBy: 'appelOffre', cascade: ['remove'], orphanRemoval: true)]
    private Collection $ratings;

    #[ORM\Column(type: Types::TEXT, nullable: true)]
    private ?string $requiredCriteria = null;

    public function __construct()
    {
        $this->candidatures = new ArrayCollection();
        $this->ratings = new ArrayCollection();
    }

    #[ORM\PrePersist]
    public function setCreatedAtValue(): void
    {
        if ($this->createdAt === null) {
            $this->createdAt = new \DateTime();
        }
    }

    public function getId(): ?int { return $this->id; }
    public function getTitre(): ?string { return $this->titre; }
    public function setTitre(string $titre): static { $this->titre = $titre; return $this; }
    public function getDescription(): ?string { return $this->description; }
    public function setDescription(?string $description): static { $this->description = $description; return $this; }
    public function getType(): ?string { return $this->type; }
    public function setType(string $type): static { $this->type = $type; return $this; }
    public function getBudgetMin(): ?string { return $this->budgetMin; }
    public function setBudgetMin(?string $budgetMin): static { $this->budgetMin = $budgetMin; return $this; }
    public function getBudgetMax(): ?string { return $this->budgetMax; }
    public function setBudgetMax(?string $budgetMax): static { $this->budgetMax = $budgetMax; return $this; }
    public function getDevise(): ?string { return $this->devise; }
    public function setDevise(?string $devise): static { $this->devise = $devise; return $this; }
    public function getDateLimite(): ?\DateTimeInterface { return $this->dateLimite; }
    public function setDateLimite(?\DateTimeInterface $dateLimite): static { $this->dateLimite = $dateLimite; return $this; }
    public function getStatut(): ?string { return $this->statut; }
    public function setStatut(string $statut): static { $this->statut = $statut; return $this; }
    public function getCreatedAt(): ?\DateTimeInterface { return $this->createdAt; }
    public function setCreatedAt(\DateTimeInterface $createdAt): static { $this->createdAt = $createdAt; return $this; }
    public function getCategorie(): ?Categorie { return $this->categorie; }
    public function setCategorie(?Categorie $categorie): static { $this->categorie = $categorie; return $this; }
    public function getCreatedBy(): ?User { return $this->createdBy; }
    public function setCreatedBy(?User $createdBy): static { $this->createdBy = $createdBy; return $this; }
    public function getCandidatures(): Collection { return $this->candidatures; }
    public function addCandidature(Candidature $candidature): static
    {
        if (!$this->candidatures->contains($candidature)) {
            $this->candidatures->add($candidature);
            $candidature->setAppelOffre($this);
        }
        return $this;
    }
    public function removeCandidature(Candidature $candidature): static
    {
        if ($this->candidatures->removeElement($candidature)) {
            if ($candidature->getAppelOffre() === $this) {
                $candidature->setAppelOffre(null);
            }
        }
        return $this;
    }

    /**
     * @return Collection<int, Rating>
     */
    public function getRatings(): Collection
    {
        return $this->ratings;
    }

    public function addRating(Rating $rating): static
    {
        if (!$this->ratings->contains($rating)) {
            $this->ratings->add($rating);
            $rating->setAppelOffre($this);
        }

        return $this;
    }

    public function removeRating(Rating $rating): static
    {
        if ($this->ratings->removeElement($rating)) {
            // set the owning side to null (unless already changed)
            if ($rating->getAppelOffre() === $this) {
                $rating->setAppelOffre(null);
            }
        }

        return $this;
    }

    public function getRequiredCriteria(): ?string
    {
        return $this->requiredCriteria;
    }

    public function setRequiredCriteria(?string $requiredCriteria): static
    {
        $this->requiredCriteria = $requiredCriteria;

        return $this;
    }
}