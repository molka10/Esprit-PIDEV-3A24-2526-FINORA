<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity]
#[ORM\Table(name: "investment_management")]
class InvestmentManagement
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: "management_id", type: "integer")]
    private ?int $managementId = null;

    // 🔥 RELATION AVEC INVESTMENT
    #[ORM\ManyToOne(inversedBy: 'managements', targetEntity: Investment::class)]
    #[ORM\JoinColumn(name: "investment_id", referencedColumnName: "investment_id", nullable: false)]
    #[Assert\NotNull(message: "Please select an investment")]
    private ?Investment $investment = null;

    #[ORM\Column(name: "investment_type", type: "string", length: 255)]
    #[Assert\NotBlank]
    private ?string $investmentType = null;

    #[ORM\Column(name: "amount_invested", type: "decimal", precision: 10, scale: 2)]
    #[Assert\NotBlank]
    #[Assert\Positive]
    private ?string $amountInvested = null;

    #[ORM\Column(name: "ownership_percentage", type: "decimal", precision: 5, scale: 2)]
    #[Assert\NotBlank]
    #[Assert\Range(min: 0, max: 100)]
    private ?string $ownershipPercentage = null;

    #[ORM\Column(name: "start_date", type: "date")]
    #[Assert\NotNull]
    private ?\DateTimeInterface $startDate = null;

    #[ORM\Column(name: "status", type: "string", length: 50)]
    private ?string $status = null;

    #[ORM\Column(name: "created_at", type: "datetime", nullable: true)]
    private ?\DateTimeInterface $createdAt = null;

    // 🔥 AJOUT IMPORTANT (comme Investment)
    public function getId(): ?int
    {
        return $this->managementId;
    }

    // ===== GETTERS & SETTERS =====

    public function getManagementId(): ?int
    {
        return $this->managementId;
    }

    public function getInvestment(): ?Investment
    {
        return $this->investment;
    }

    public function setInvestment(?Investment $investment): self
    {
        $this->investment = $investment;
        return $this;
    }

    public function getInvestmentType(): ?string
    {
        return $this->investmentType;
    }

    public function setInvestmentType(string $investmentType): self
    {
        $this->investmentType = $investmentType;
        return $this;
    }

    public function getAmountInvested(): ?string
    {
        return $this->amountInvested;
    }

    public function setAmountInvested(string $amountInvested): self
    {
        $this->amountInvested = $amountInvested;
        return $this;
    }

    public function getOwnershipPercentage(): ?string
    {
        return $this->ownershipPercentage;
    }

    public function setOwnershipPercentage(string $ownershipPercentage): self
    {
        $this->ownershipPercentage = $ownershipPercentage;
        return $this;
    }

    public function getStartDate(): ?\DateTimeInterface
    {
        return $this->startDate;
    }

    public function setStartDate(\DateTimeInterface $startDate): self
    {
        $this->startDate = $startDate;
        return $this;
    }

    public function getStatus(): ?string
    {
        return $this->status;
    }

    public function setStatus(string $status): self
    {
        $this->status = $status;
        return $this;
    }

    public function getCreatedAt(): ?\DateTimeInterface
    {
        return $this->createdAt;
    }

    public function setCreatedAt(?\DateTimeInterface $createdAt): self
    {
        $this->createdAt = $createdAt;
        return $this;
    }
}