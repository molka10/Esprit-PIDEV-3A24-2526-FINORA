<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
class UserFormationProgress
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne]
    #[ORM\JoinColumn(nullable: false)]
    private ?User $user = null;

    #[ORM\ManyToOne]
    #[ORM\JoinColumn(nullable: false)]
    private ?Formation $formation = null;

    #[ORM\Column(type: 'datetime')]
    private ?\DateTimeInterface $purchasedAt = null;

    #[ORM\Column(type: 'datetime')]
    private ?\DateTimeInterface $deadlineAt = null;

    #[ORM\Column(type: 'boolean', options: ['default' => false])]
    private bool $reminderSent = false;

    public function __construct()
    {
        $this->purchasedAt = new \DateTime();
        $this->deadlineAt = new \DateTime('+30 days');
    }

    public function getId(): ?int { return $this->id; }

    public function getUser(): ?User { return $this->user; }
    public function setUser(?User $user): self { $this->user = $user; return $this; }

    public function getFormation(): ?Formation { return $this->formation; }
    public function setFormation(?Formation $formation): self { $this->formation = $formation; return $this; }

    public function getPurchasedAt(): ?\DateTimeInterface { return $this->purchasedAt; }
    public function setPurchasedAt(\DateTimeInterface $purchasedAt): self { $this->purchasedAt = $purchasedAt; return $this; }

    public function getDeadlineAt(): ?\DateTimeInterface { return $this->deadlineAt; }
    public function setDeadlineAt(\DateTimeInterface $deadlineAt): self { $this->deadlineAt = $deadlineAt; return $this; }

    public function isReminderSent(): bool { return $this->reminderSent; }
    public function setReminderSent(bool $reminderSent): self { $this->reminderSent = $reminderSent; return $this; }
}
