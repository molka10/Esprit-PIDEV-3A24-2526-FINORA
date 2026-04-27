<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;

#[ORM\Entity]
#[ORM\Table(name: 'centre_formation')]
class CentreFormation
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(type: 'string', length: 255)]
    #[Assert\NotBlank(message: 'Le nom est obligatoire.')]
    private ?string $nom = null;

    #[ORM\Column(type: 'string', length: 500)]
    #[Assert\NotBlank(message: "L'adresse est obligatoire.")]
    private ?string $adresse = null;

    #[ORM\Column(type: 'string', length: 50)]
    #[Assert\NotBlank(message: 'La ville est obligatoire.')]
    private ?string $ville = null;

    /**
     * Latitude (WGS84) — Tunisia range ~30.2 to 37.5
     */
    #[ORM\Column(type: 'decimal', precision: 10, scale: 7)]
    #[Assert\NotNull(message: 'La latitude est obligatoire.')]
    #[Assert\Range(min: 30.0, max: 38.0, notInRangeMessage: 'Latitude invalide pour la Tunisie.')]
    private ?string $latitude = null;

    /**
     * Longitude (WGS84) — Tunisia range ~7.5 to 11.6
     */
    #[ORM\Column(type: 'decimal', precision: 10, scale: 7)]
    #[Assert\NotNull(message: 'La longitude est obligatoire.')]
    #[Assert\Range(min: 7.0, max: 12.0, notInRangeMessage: 'Longitude invalide pour la Tunisie.')]
    private ?string $longitude = null;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $description = null;

    #[ORM\Column(type: 'string', length: 30, nullable: true)]
    private ?string $telephone = null;

    #[ORM\Column(type: 'string', length: 255, nullable: true)]
    private ?string $email = null;

    #[ORM\Column(type: 'string', length: 255, nullable: true)]
    private ?string $siteWeb = null;

    /**
     * Centre active/visible on frontend map.
     * Future: link to ownerType/ownerId (Entreprise) for multi-tenant.
     */
    #[ORM\Column(type: 'boolean')]
    private bool $isActive = true;

    #[ORM\Column(type: 'datetime_immutable')]
    private \DateTimeImmutable $createdAt;

    #[ORM\OneToMany(targetEntity: RatingCentre::class, mappedBy: 'centre', cascade: ['remove'])]
    private Collection $ratings;

    public function __construct()
    {
        $this->createdAt = new \DateTimeImmutable();
        $this->ratings = new ArrayCollection();
    }

    public function getId(): ?int { return $this->id; }

    public function getNom(): ?string { return $this->nom; }
    public function setNom(string $nom): self { $this->nom = $nom; return $this; }

    public function getAdresse(): ?string { return $this->adresse; }
    public function setAdresse(string $adresse): self { $this->adresse = $adresse; return $this; }

    public function getVille(): ?string { return $this->ville; }
    public function setVille(string $ville): self { $this->ville = $ville; return $this; }

    public function getLatitude(): ?float { return $this->latitude !== null ? (float) $this->latitude : null; }
    public function setLatitude(string|float|null $latitude): self { $this->latitude = (string) $latitude; return $this; }

    public function getLongitude(): ?float { return $this->longitude !== null ? (float) $this->longitude : null; }
    public function setLongitude(string|float|null $longitude): self { $this->longitude = (string) $longitude; return $this; }

    public function getDescription(): ?string { return $this->description; }
    public function setDescription(?string $description): self { $this->description = $description; return $this; }

    public function getTelephone(): ?string { return $this->telephone; }
    public function setTelephone(?string $telephone): self { $this->telephone = $telephone; return $this; }

    public function getEmail(): ?string { return $this->email; }
    public function setEmail(?string $email): self { $this->email = $email; return $this; }

    public function getSiteWeb(): ?string { return $this->siteWeb; }
    public function setSiteWeb(?string $siteWeb): self { $this->siteWeb = $siteWeb; return $this; }

    public function isActive(): bool { return $this->isActive; }
    public function setIsActive(bool $isActive): self { $this->isActive = $isActive; return $this; }

    public function getCreatedAt(): \DateTimeImmutable { return $this->createdAt; }

    /**
     * @return Collection<int, RatingCentre>
     */
    public function getRatings(): Collection { return $this->ratings; }

    public function addRating(RatingCentre $rating): self
    {
        if (!$this->ratings->contains($rating)) {
            $this->ratings->add($rating);
            $rating->setCentre($this);
        }
        return $this;
    }

    public function getAverageRating(): float
    {
        if ($this->ratings->isEmpty()) return 0.0;
        
        $total = 0;
        foreach ($this->ratings as $rating) {
            $total += $rating->getNote();
        }
        return round($total / $this->ratings->count(), 1);
    }
}
