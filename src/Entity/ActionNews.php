<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: \App\Repository\ActionNewsRepository::class)]
#[ORM\Table(name: 'action_news')]
class ActionNews
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Action::class)]
    #[ORM\JoinColumn(name: 'action_id', referencedColumnName: 'id_action', nullable: false, onDelete: 'CASCADE')]
    private ?Action $action = null;

    #[ORM\Column(type: 'string', length: 255)]
    private string $titre;

    #[ORM\Column(type: 'decimal', precision: 10, scale: 2)]
    private string $impactPercent;

    #[ORM\Column(name: 'date_ajout', type: 'datetime_immutable')]
    private \DateTimeImmutable $dateAjout;

    public function __construct()
    {
        $this->dateAjout = new \DateTimeImmutable();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getAction(): ?Action
    {
        return $this->action;
    }

    public function setAction(?Action $action): static
    {
        $this->action = $action;
        return $this;
    }

    public function getTitre(): string
    {
        return $this->titre;
    }

    public function setTitre(string $titre): self
    {
        $this->titre = $titre;
        return $this;
    }

    public function getImpactPercent(): string
    {
        return $this->impactPercent;
    }

    public function setImpactPercent(string $impactPercent): self
    {
        $this->impactPercent = $impactPercent;
        return $this;
    }

    public function getDateAjout(): \DateTimeImmutable
    {
        return $this->dateAjout;
    }

    public function setDateAjout(\DateTimeImmutable $dateAjout): self
    {
        $this->dateAjout = $dateAjout;
        return $this;
    }
}
