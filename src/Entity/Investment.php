<?php

namespace App\Entity;

use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use App\Enum\InvestmentCategory;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity]
#[ORM\HasLifecycleCallbacks]
#[ORM\Table(
    name: 'investment',
    indexes: [new ORM\Index(name: 'idx_inv_creator', columns: ['created_by_user_id'])]
)]
class Investment
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'investment_id', type: 'integer')]
    private ?int $investmentId = null;

    #[ORM\Column(type: 'string', length: 255)]
    #[Assert\NotBlank(message: 'Le nom est obligatoire')]
    #[Assert\Length(min: 3, max: 255, minMessage: 'Minimum 3 caractères')]
    private ?string $name = null;

    #[ORM\Column(type: 'string', length: 50)]
    #[Assert\NotBlank(message: 'La catégorie est obligatoire')]
    #[Assert\Choice(callback: [InvestmentCategory::class, 'values'], message: 'Choisissez : maison, startup, hôtel ou terrain.')]
    private ?string $category = null;

    #[ORM\Column(type: 'string', length: 255)]
    #[Assert\NotBlank(message: 'La localisation est obligatoire')]
    private ?string $location = null;

    #[ORM\Column(name: 'estimated_value', type: 'decimal', precision: 10, scale: 2)]
    #[Assert\NotBlank(message: 'La valeur est obligatoire')]
    #[Assert\Positive(message: 'La valeur doit être positive')]
    private ?string $estimatedValue = null;

    #[ORM\Column(name: 'risk_level', type: 'string', length: 50)]
    #[Assert\NotBlank(message: 'Le niveau de risque est obligatoire')]
    #[Assert\Choice(choices: ['LOW', 'MEDIUM', 'HIGH'], message: 'Choix invalide')]
    private ?string $riskLevel = null;

    /** Filename in public/uploads/investments (column historique: image_url). Anciennes URLs complètes restent affichables. */
    #[ORM\Column(name: 'image_url', type: 'string', length: 255, nullable: true)]
    #[Assert\Length(max: 255)]
    private ?string $imageUrl = null;

    #[ORM\Column(type: 'text', nullable: true)]
    #[Assert\Length(max: 2000, maxMessage: 'Max 2000 caractères')]
    private ?string $description = null;

    #[ORM\Column(name: 'status', type: 'string', length: 50)]
    #[Assert\NotBlank(message: 'Le statut est obligatoire')]
    #[Assert\Choice(choices: ['ACTIVE', 'INACTIVE'], message: 'Statut invalide')]
    private ?string $status = null;

    #[ORM\Column(name: 'created_at', type: 'datetime')]
    private ?\DateTimeInterface $createdAt = null;

    /** Colonne existante en base (autre module) — conservée pour rester alignée avec le schéma. */
    #[ORM\Column(name: 'created_by_user_id', type: 'integer', nullable: true)]
    private ?int $createdByUserId = null;

    /** @var Collection<int, InvestmentManagement> */
    #[ORM\OneToMany(targetEntity: InvestmentManagement::class, mappedBy: 'investment', orphanRemoval: true)]
    private Collection $managements;

    public function __construct()
    {
        $this->managements = new ArrayCollection();
    }

    #[ORM\PrePersist]
    public function setCreatedAtValue(): void
    {
        if (!$this->createdAt) {
            $this->createdAt = new \DateTime();
        }
    }

    public function __toString(): string
    {
        return $this->name ?? '';
    }

    public function getId(): ?int
    {
        return $this->investmentId;
    }

    public function getInvestmentId(): ?int
    {
        return $this->investmentId;
    }

    public function getName(): ?string
    {
        return $this->name;
    }

    public function setName(?string $name): self
    {
        $this->name = $name;

        return $this;
    }

    public function getCategory(): ?string
    {
        return $this->category;
    }

    public function setCategory(?string $category): self
    {
        $this->category = $category;

        return $this;
    }

    public function getLocation(): ?string
    {
        return $this->location;
    }

    public function setLocation(?string $location): self
    {
        $this->location = $location;

        return $this;
    }

    public function getEstimatedValue(): ?string
    {
        return $this->estimatedValue;
    }

    public function setEstimatedValue(?string $value): self
    {
        $this->estimatedValue = $value;

        return $this;
    }

    public function getRiskLevel(): ?string
    {
        return $this->riskLevel;
    }

    public function setRiskLevel(?string $risk): self
    {
        $this->riskLevel = $risk;

        return $this;
    }

    public function getImageUrl(): ?string
    {
        return $this->imageUrl;
    }

    public function setImageUrl(?string $imageUrl): self
    {
        $this->imageUrl = $imageUrl;

        return $this;
    }

    public function getDescription(): ?string
    {
        return $this->description;
    }

    public function setDescription(?string $desc): self
    {
        $this->description = $desc;

        return $this;
    }

    public function getStatus(): ?string
    {
        return $this->status;
    }

    public function setStatus(?string $status): self
    {
        $this->status = $status;

        return $this;
    }

    public function getCreatedAt(): ?\DateTimeInterface
    {
        return $this->createdAt;
    }

    public function setCreatedAt(\DateTimeInterface $date): self
    {
        $this->createdAt = $date;

        return $this;
    }

    public function getCreatedByUserId(): ?int
    {
        return $this->createdByUserId;
    }

    public function setCreatedByUserId(?int $createdByUserId): self
    {
        $this->createdByUserId = $createdByUserId;

        return $this;
    }

    /** @return Collection<int, InvestmentManagement> */
    public function getManagements(): Collection
    {
        return $this->managements;
    }

    public function addManagement(InvestmentManagement $m): self
    {
        if (!$this->managements->contains($m)) {
            $this->managements->add($m);
            $m->setInvestment($this);
        }

        return $this;
    }

    public function removeManagement(InvestmentManagement $m): self
    {
        $this->managements->removeElement($m);

        return $this;
    }
}
