<?php

namespace App\Entity;

use App\Repository\NotificationBourseRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: NotificationBourseRepository::class)]
#[ORM\Table(name: 'notification_bourse')]
class NotificationBourse
{
    public const TYPE_NEW_ACTION = 'new_action';
    public const TYPE_ACHAT     = 'achat';
    public const TYPE_VENTE     = 'vente';

    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(nullable: false, onDelete: 'CASCADE')]
    private ?User $user = null;

    #[ORM\Column(length: 30)]
    private string $type = self::TYPE_NEW_ACTION;

    #[ORM\Column(length: 200)]
    private string $titre = '';

    #[ORM\Column(type: 'text')]
    private string $message = '';

    #[ORM\Column(type: 'boolean')]
    private bool $isRead = false;

    #[ORM\Column(type: 'datetime')]
    private \DateTimeInterface $createdAt;

    #[ORM\ManyToOne(targetEntity: Action::class)]
    #[ORM\JoinColumn(name: 'id_action', referencedColumnName: 'id_action', nullable: true, onDelete: 'SET NULL')]
    private ?Action $action = null;

    public function __construct()
    {
        $this->createdAt = new \DateTime();
    }

    public function getId(): ?int { return $this->id; }

    public function getUser(): ?User { return $this->user; }
    public function setUser(?User $user): static { $this->user = $user; return $this; }

    public function getType(): string { return $this->type; }
    public function setType(string $type): static { $this->type = $type; return $this; }

    public function getTitre(): string { return $this->titre; }
    public function setTitre(string $titre): static { $this->titre = $titre; return $this; }

    public function getMessage(): string { return $this->message; }
    public function setMessage(string $message): static { $this->message = $message; return $this; }

    public function isRead(): bool { return $this->isRead; }
    public function setIsRead(bool $isRead): static { $this->isRead = $isRead; return $this; }

    public function getCreatedAt(): \DateTimeInterface { return $this->createdAt; }
    public function setCreatedAt(\DateTimeInterface $createdAt): static { $this->createdAt = $createdAt; return $this; }

    public function getAction(): ?Action { return $this->action; }
    public function setAction(?Action $action): static { $this->action = $action; return $this; }

    /** Return a Bootstrap icon class based on notification type */
    public function getIcon(): string
    {
        return match($this->type) {
            self::TYPE_NEW_ACTION => 'bi bi-bell-fill text-warning',
            self::TYPE_ACHAT      => 'bi bi-arrow-up-circle-fill text-success',
            self::TYPE_VENTE      => 'bi bi-arrow-down-circle-fill text-danger',
            default               => 'bi bi-info-circle-fill text-primary',
        };
    }

    /** Human-readable time ago */
    public function getTimeAgo(): string
    {
        $diff = (new \DateTime())->getTimestamp() - $this->createdAt->getTimestamp();
        if ($diff < 60)    return 'À l\'instant';
        if ($diff < 3600)  return round($diff / 60) . ' min';
        if ($diff < 86400) return round($diff / 3600) . 'h';
        return $this->createdAt->format('d/m/Y');
    }
}
