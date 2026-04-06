<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity]
#[ORM\HasLifecycleCallbacks]
#[ORM\Table(name: "investment")]
class Investment
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: "investment_id", type: "integer")]
    private ?int $investmentId = null;

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: "Le nom est obligatoire")]
    #[Assert\Length(min: 3, minMessage: "Minimum 3 caractères")]
    private ?string $name = null;

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: "La catégorie est obligatoire")]
    private ?string $category = null;

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: "La localisation est obligatoire")]
    private ?string $location = null;

    // 🔥 FIX TYPE FLOAT
    #[ORM\Column(name: "estimated_value", type: "float")]
    #[Assert\NotBlank(message: "La valeur est obligatoire")]
    #[Assert\Positive(message: "La valeur doit être positive")]
    private ?float $estimatedValue = null;

    #[ORM\Column(name: "risk_level", length: 50)]
    #[Assert\NotBlank(message: "Le niveau de risque est obligatoire")]
    #[Assert\Choice(
        choices: ["LOW", "MEDIUM", "HIGH"],
        message: "Choix invalide"
    )]
    private ?string $riskLevel = null;

    #[ORM\Column(name: "image_url", length: 255, nullable: true)]
    #[Assert\Url(message: "URL invalide")]
    private ?string $imageUrl = null;

    #[ORM\Column(type: "text", nullable: true)]
    #[Assert\Length(max: 2000, maxMessage: "Max 2000 caractères")]
    private ?string $description = null;

    #[ORM\Column(length: 50)]
    #[Assert\NotBlank(message: "Le statut est obligatoire")]
    #[Assert\Choice(
        choices: ["ACTIVE", "INACTIVE"],
        message: "Statut invalide"
    )]
    private ?string $status = null;

    #[ORM\Column(type: "datetime")]
    private ?\DateTimeInterface $createdAt = null;

    // 🔥 AUTO DATE
    #[ORM\PrePersist]
    public function setCreatedAtValue(): void
    {
        if (!$this->createdAt) {
            $this->createdAt = new \DateTime();
        }
    }

    // 🔥 POUR SELECT
    public function __toString(): string
    {
        return $this->name ?? '';
    }

    // ===== GETTERS & SETTERS =====

    public function getInvestmentId(): ?int { return $this->investmentId; }

    public function getName(): ?string { return $this->name; }
    public function setName(string $name): self { $this->name = $name; return $this; }

    public function getCategory(): ?string { return $this->category; }
    public function setCategory(string $category): self { $this->category = $category; return $this; }

    public function getLocation(): ?string { return $this->location; }
    public function setLocation(string $location): self { $this->location = $location; return $this; }

    // 🔥 FIX FLOAT
    public function getEstimatedValue(): ?float { return $this->estimatedValue; }
    public function setEstimatedValue(float $value): self { $this->estimatedValue = $value; return $this; }

    public function getRiskLevel(): ?string { return $this->riskLevel; }
    public function setRiskLevel(string $risk): self { $this->riskLevel = $risk; return $this; }

    public function getImageUrl(): ?string { return $this->imageUrl; }
    public function setImageUrl(?string $url): self { $this->imageUrl = $url; return $this; }

    public function getDescription(): ?string { return $this->description; }
    public function setDescription(?string $desc): self { $this->description = $desc; return $this; }

    public function getStatus(): ?string { return $this->status; }
    public function setStatus(string $status): self { $this->status = $status; return $this; }

    public function getCreatedAt(): ?\DateTimeInterface { return $this->createdAt; }
    public function setCreatedAt(\DateTimeInterface $date): self { $this->createdAt = $date; return $this; }
}