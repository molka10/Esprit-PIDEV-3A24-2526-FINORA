<?php

namespace App\Entity;

use App\Repository\RatingRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: RatingRepository::class)]
#[ORM\HasLifecycleCallbacks]
class Rating
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column]
    #[Assert\NotBlank(message: 'La note est obligatoire')]
    #[Assert\Range(
        min: 1,
        max: 5,
        notInRangeMessage: 'La note doit être entre {{ min }} et {{ max }}'
    )]
    private ?int $note = null;

    #[ORM\Column]
    private ?\DateTime $createdAt = null;

    #[ORM\ManyToOne(inversedBy: 'ratings')]
    #[ORM\JoinColumn(nullable: false)]
    private ?AppelOffre $appelOffre = null;

    #[ORM\ManyToOne(inversedBy: 'ratings')]
    private ?User $user = null;

    #[ORM\PrePersist]
    public function setCreatedAtValue(): void
    {
        if ($this->createdAt === null) {
            $this->createdAt = new \DateTime();
        }
    }

    public function getId(): ?int { return $this->id; }
    public function getNote(): ?int { return $this->note; }
    public function setNote(int $note): static { $this->note = $note; return $this; }
    public function getCreatedAt(): ?\DateTime { return $this->createdAt; }
    public function setCreatedAt(\DateTime $createdAt): static { $this->createdAt = $createdAt; return $this; }
    public function getAppelOffre(): ?AppelOffre { return $this->appelOffre; }
    public function setAppelOffre(?AppelOffre $appelOffre): static { $this->appelOffre = $appelOffre; return $this; }
    public function getUser(): ?User { return $this->user; }
    public function setUser(?User $user): static { $this->user = $user; return $this; }
}