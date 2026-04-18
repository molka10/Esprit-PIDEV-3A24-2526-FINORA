<?php

namespace App\Entity;

use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\HttpFoundation\File\File;
use Symfony\Component\Validator\Constraints as Assert;
use Vich\UploaderBundle\Mapping\Annotation as Vich;

#[ORM\Entity]
#[ORM\Table(name: 'formation')]
#[Vich\Uploadable]
class Formation
{
    #[ORM\OneToMany(mappedBy: 'formation', targetEntity: Lesson::class, orphanRemoval: true)]
    #[ORM\OrderBy(['ordre' => 'ASC'])]
    private Collection $lessons;

    public function __construct()
    {
        $this->lessons = new ArrayCollection();
        $this->wishlistedBy = new ArrayCollection();
        $this->purchasedBy = new ArrayCollection();
    }
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(type: 'string', length: 255)]
    #[Assert\NotBlank(message: 'Le titre est obligatoire.')]
    #[Assert\Length(
        min: 3,
        max: 255,
        minMessage: 'Le titre doit contenir au moins {{ limit }} caractères.',
        maxMessage: 'Le titre ne doit pas dépasser {{ limit }} caractères.'
    )]
    private ?string $titre = null;

    #[ORM\Column(type: 'text', nullable: true)]
    #[Assert\Length(
        max: 2000,
        maxMessage: 'La description ne doit pas dépasser {{ limit }} caractères.'
    )]
    private ?string $description = null;

    #[ORM\Column(type: 'string', length: 255)]
    #[Assert\NotBlank(message: 'La catégorie est obligatoire.')]
    #[Assert\Length(
        min: 3,
        max: 255,
        minMessage: 'La catégorie doit contenir au moins {{ limit }} caractères.',
        maxMessage: 'La catégorie ne doit pas dépasser {{ limit }} caractères.'
    )]
    private ?string $categorie = null;

    #[ORM\Column(type: 'string', length: 255)]
    #[Assert\NotBlank(message: 'Le niveau est obligatoire.')]
    #[Assert\Choice(
        choices: ['Débutant', 'Intermédiaire', 'Avancé'],
        message: 'Le niveau choisi est invalide.'
    )]
    private ?string $niveau = null;

    #[ORM\Column(type: 'integer')]
    #[Assert\NotNull(message: 'Le statut de publication est obligatoire.')]
    #[Assert\Choice(
        choices: [0, 1],
        message: 'Le statut de publication doit être Oui ou Non.'
    )]
    private ?int $is_published = null;

    #[ORM\Column(type: 'string', length: 255, nullable: true)]
    private ?string $image_url = null;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $pourquoiAcheter = null;

    #[ORM\Column(type: 'float', nullable: true)]
    private ?float $prix = null;

    #[ORM\Column(type: 'float', nullable: true)]
    #[Assert\Range(min: 0, max: 5)]
    private ?float $rating = 0.0;

    #[ORM\Column(type: 'integer', options: ['default' => 0])]
    private int $ratingCount = 0;

    #[Vich\UploadableField(mapping: 'formation_images', fileNameProperty: 'image_url')]
    #[Assert\Image(
        maxSize: '4M',
        mimeTypes: ['image/jpeg', 'image/png', 'image/webp', 'image/jpg'],
        mimeTypesMessage: 'Veuillez uploader une image valide (JPG, PNG ou WEBP).'
    )]
    private ?File $imageFile = null;

    #[ORM\Column(type: 'datetime_immutable', nullable: true)]
    private ?\DateTimeImmutable $updatedAt = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getTitre(): ?string
    {
        return $this->titre;
    }

    public function setTitre(string $titre): self
    {
        $this->titre = $titre;
        return $this;
    }

    public function getDescription(): ?string
    {
        return $this->description;
    }

    public function setDescription(?string $description): self
    {
        $this->description = $description;
        return $this;
    }

    public function getCategorie(): ?string
    {
        return $this->categorie;
    }

    public function setCategorie(string $categorie): self
    {
        $this->categorie = $categorie;
        return $this;
    }

    public function getNiveau(): ?string
    {
        return $this->niveau;
    }

    public function setNiveau(string $niveau): self
    {
        $this->niveau = $niveau;
        return $this;
    }

    public function getIsPublished(): ?int
    {
        return $this->is_published;
    }

    public function setIsPublished(int $is_published): self
    {
        $this->is_published = $is_published;
        return $this;
    }

    public function getImageUrl(): ?string
    {
        return $this->image_url;
    }

    public function setImageUrl(?string $image_url): self
    {
        $this->image_url = $image_url;
        return $this;
    }

    public function getPourquoiAcheter(): ?string
    {
        return $this->pourquoiAcheter;
    }

    public function setPourquoiAcheter(?string $pourquoiAcheter): self
    {
        $this->pourquoiAcheter = $pourquoiAcheter;
        return $this;
    }

    public function getPrix(): ?float
    {
        return $this->prix;
    }

    public function setPrix(?float $prix): self
    {
        $this->prix = $prix;
        return $this;
    }

    public function getRating(): ?float
    {
        return $this->rating;
    }

    public function setRating(?float $rating): self
    {
        $this->rating = $rating;
        return $this;
    }

    public function getImageFile(): ?File
    {
        return $this->imageFile;
    }

    public function setImageFile(?File $imageFile = null): void
    {
        $this->imageFile = $imageFile;

        if ($imageFile !== null) {
            $this->updatedAt = new \DateTimeImmutable();
        }
    }

    public function getUpdatedAt(): ?\DateTimeImmutable
    {
        return $this->updatedAt;
    }

    public function setUpdatedAt(?\DateTimeImmutable $updatedAt): self
    {
        $this->updatedAt = $updatedAt;
        return $this;
    }

    /**
     * @return Collection<int, Lesson>
     */
    public function getLessons(): Collection
    {
        return $this->lessons;
    }

    public function addLesson(Lesson $lesson): self
    {
        if (!$this->lessons->contains($lesson)) {
            $this->lessons->add($lesson);
            $lesson->setFormation($this);
        }
        return $this;
    }

    public function removeLesson(Lesson $lesson): self
    {
        if ($this->lessons->removeElement($lesson)) {
            // set the owning side to null (unless already changed)
            if ($lesson->getFormation() === $this) {
                $lesson->setFormation(null);
            }
        }
        return $this;
    }

    #[ORM\ManyToMany(targetEntity: User::class, mappedBy: 'wishlist')]
    private Collection $wishlistedBy;

    #[ORM\ManyToMany(targetEntity: User::class, mappedBy: 'purchasedFormations')]
    private Collection $purchasedBy;

    public function getWishlistedBy(): Collection
    {
        return $this->wishlistedBy;
    }

    public function getPurchasedBy(): Collection
    {
        return $this->purchasedBy;
    }

    public function getRatingCount(): int
    {
        return $this->ratingCount;
    }

    public function setRatingCount(int $ratingCount): static
    {
        $this->ratingCount = $ratingCount;
        return $this;
    }

    public function __toString(): string
    {
        return $this->titre ?? '';
    }
}
