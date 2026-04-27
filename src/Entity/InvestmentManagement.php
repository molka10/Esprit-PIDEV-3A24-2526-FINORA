<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity]
#[ORM\HasLifecycleCallbacks]
#[ORM\Table(name: "investment_management")]
class InvestmentManagement
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: "management_id", type: "integer")]
    private ?int $managementId = null;

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
    #[Assert\Choice(choices: ['ACTIVE', 'CLOSED', 'CRITICAL'], message: "Statut invalide")]
    private ?string $status = 'ACTIVE';

    #[ORM\Column(name: "created_at", type: "datetime", nullable: true)]
    private ?\DateTimeInterface $createdAt = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: 'user_id', referencedColumnName: 'id', nullable: true, onDelete: 'SET NULL')]
    private ?User $user = null;

    #[ORM\Column(name: "rating", type: "integer", nullable: true)]
    #[Assert\Range(min: 1, max: 5, notInRangeMessage: "Le rating doit être entre 1 et 5")]
    private ?int $rating = null;

    #[ORM\Column(length: 20, nullable: true)]
    #[Assert\Choice(choices: ['LOW', 'MEDIUM', 'HIGH'])]
    private ?string $priority = 'MEDIUM';

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $notes = null;

    public function getId(): ?int
    {
        return $this->managementId;
    }

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

    public function setInvestmentType(?string $investmentType): self
    {
        $this->investmentType = $investmentType;
        return $this;
    }

    public function getAmountInvested(): ?string
    {
        return $this->amountInvested;
    }

    public function setAmountInvested(?string $amountInvested): self
    {
        $this->amountInvested = $amountInvested;
        return $this;
    }

    public function getOwnershipPercentage(): ?string
    {
        return $this->ownershipPercentage;
    }

    public function setOwnershipPercentage(?string $ownershipPercentage): self
    {
        $this->ownershipPercentage = $ownershipPercentage;
        return $this;
    }

    public function getStartDate(): ?\DateTimeInterface
    {
        return $this->startDate;
    }

    public function setStartDate(?\DateTimeInterface $startDate): self
    {
        $this->startDate = $startDate;
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

    public function setCreatedAt(?\DateTimeInterface $createdAt): self
    {
        $this->createdAt = $createdAt;
        return $this;
    }

    public function getUser(): ?User
    {
        return $this->user;
    }

    public function setUser(?User $user): self
    {
        $this->user = $user;
        return $this;
    }

    #[ORM\PrePersist]
    #[ORM\PreUpdate]
    public function calculateSmartStatus(): void
    {
        // 1. CLOSED
        if ((float) $this->ownershipPercentage == 100) {
            $this->status = 'CLOSED';
            return;
        }

        $amount = (float) $this->amountInvested;
        $ownership = (float) $this->ownershipPercentage;
        $now = new \DateTime();

        // 2. CRITICAL : Risque financier (Montant élevé ET faible possession)
        if ($amount > 500000 && $ownership < 20) {
            $this->status = 'CRITICAL';
            return;
        }

        // 2. CRITICAL : Investissement trop ancien (> 5 ans sans être closed)
        if ($this->startDate) {
            $diff = $now->diff($this->startDate);
            if ($diff->y >= 5) {
                $this->status = 'CRITICAL';
                return;
            }
        }

        // 2. CRITICAL : Stagnation (Faible rendement de l'investissement parent)
        if ($this->investment && $this->investment->getAnnualReturn() < 2.0) {
            $this->status = 'CRITICAL';
            return;
        }

        // 3. ACTIVE par défaut si rien d'autre ne match
        $this->status = 'ACTIVE';
    }
    public function getRepaymentSchedule(): array
    {
        $investment = $this->getInvestment();
        if (!$investment || !$this->startDate) {
            return [];
        }

        $duration = $investment->getDurationMonths();
        $rate = $investment->getAnnualReturn() / 100;
        $principal = (float) $this->amountInvested;
        
        // Monthly interest (annual profit / 12)
        $monthlyInterest = ($principal * $rate) / 12;
        // Monthly capital repayment (fully amortized over duration)
        $monthlyCapital = $principal / $duration;
        
        $schedule = [];
        $currentDate = clone $this->startDate;
        $now = new \DateTime();

        for ($i = 1; $i <= $duration; $i++) {
            // We use modify('+1 month') to generate monthly dates
            $date = (clone $currentDate)->modify("+$i month");
            $schedule[] = [
                'month' => $i,
                'date' => $date,
                'interest' => $monthlyInterest,
                'capital' => $monthlyCapital,
                'total' => $monthlyInterest + $monthlyCapital,
                'status' => $date < $now ? 'PAID' : 'PENDING'
            ];
        }

        return $schedule;
    }

    public function getEstimatedTotalReturn(): float
    {
        $investment = $this->getInvestment();
        if (!$investment) return 0.0;
        
        $rate = $investment->getAnnualReturn() / 100;
        $durationYears = $investment->getDurationMonths() / 12;
        
        return (float)$this->amountInvested * $rate * $durationYears;
    }

    public function getRating(): ?int
    {
        return $this->rating;
    }

    public function setRating(?int $rating): self
    {
        $this->rating = $rating;
        return $this;
    }

    public function getPriority(): ?string
    {
        return $this->priority;
    }

    public function setPriority(?string $priority): self
    {
        $this->priority = $priority;
        return $this;
    }

    public function getNotes(): ?string
    {
        return $this->notes;
    }

    public function setNotes(?string $notes): self
    {
        $this->notes = $notes;
        return $this;
    }

    /**
     * 🔥 Logic: Calculate progress based on time elapsed in the investment duration
     */
    public function getProgressPercentage(): int
    {
        if (!$this->startDate || !$this->investment) {
            return 0;
        }

        $now = new \DateTime();
        $start = $this->startDate;
        $durationMonths = $this->investment->getDurationMonths();
        $end = (clone $start)->modify("+$durationMonths months");

        if ($now >= $end) {
            return 100;
        }

        if ($now <= $start) {
            return 0;
        }

        $totalDays = $end->getTimestamp() - $start->getTimestamp();
        $elapsedDays = $now->getTimestamp() - $start->getTimestamp();

        return (int) round(($elapsedDays / $totalDays) * 100);
    }
}

