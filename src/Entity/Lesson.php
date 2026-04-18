<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\HttpFoundation\File\File;
use Symfony\Component\Validator\Constraints as Assert;
use Vich\UploaderBundle\Mapping\Annotation as Vich;

#[ORM\Entity]
#[ORM\Table(name: 'lesson')]
#[Vich\Uploadable]
class Lesson
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Formation::class, inversedBy: 'lessons')]
    #[ORM\JoinColumn(name: 'formation_id', referencedColumnName: 'id', nullable: false)]
    #[Assert\NotNull(message: 'La formation est obligatoire.')]
    private ?Formation $formation = null;

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
        max: 5000,
        maxMessage: 'Le contenu ne doit pas dépasser {{ limit }} caractères.'
    )]
    private ?string $contenu = null;

    #[ORM\Column(name: 'video_url', type: 'string', length: 500, nullable: true)]
    private ?string $videoUrl = null;

    #[Vich\UploadableField(mapping: 'lesson_videos', fileNameProperty: 'videoUrl')]
    #[Assert\File(
        maxSize: '100M',
        mimeTypes: ['video/mp4', 'video/webm', 'video/quicktime'],
        mimeTypesMessage: 'Veuillez uploader une vidéo valide (MP4, WEBM ou MOV).'
    )]
    private ?File $videoFile = null;

    #[ORM\Column(type: 'integer')]
    #[Assert\NotNull(message: 'L’ordre est obligatoire.')]
    #[Assert\Positive(message: 'L’ordre doit être un entier positif.')]
    private ?int $ordre = null;

    #[ORM\Column(name: 'duree_minutes', type: 'integer')]
    #[Assert\NotNull(message: 'La durée est obligatoire.')]
    #[Assert\Positive(message: 'La durée doit être un entier positif.')]
    private ?int $dureeMinutes = null;

    #[ORM\Column(type: 'datetime_immutable', nullable: true)]
    private ?\DateTimeImmutable $updatedAt = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getFormation(): ?Formation
    {
        return $this->formation;
    }

    public function setFormation(?Formation $formation): self
    {
        $this->formation = $formation;
        return $this;
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

    public function getContenu(): ?string
    {
        return $this->contenu;
    }

    public function setContenu(?string $contenu): self
    {
        $this->contenu = $contenu;
        return $this;
    }

    public function getVideoUrl(): ?string
    {
        return $this->videoUrl;
    }

    public function setVideoUrl(?string $videoUrl): self
    {
        $this->videoUrl = $videoUrl;
        return $this;
    }

    public function getVideoFile(): ?File
    {
        return $this->videoFile;
    }

    public function setVideoFile(?File $videoFile = null): void
    {
        $this->videoFile = $videoFile;

        if ($videoFile !== null) {
            $this->updatedAt = new \DateTimeImmutable();
        }
    }

    public function getOrdre(): ?int
    {
        return $this->ordre;
    }

    public function setOrdre(int $ordre): self
    {
        $this->ordre = $ordre;
        return $this;
    }

    public function getDureeMinutes(): ?int
    {
        return $this->dureeMinutes;
    }

    public function setDureeMinutes(int $dureeMinutes): self
    {
        $this->dureeMinutes = $dureeMinutes;
        return $this;
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

    public function isExternalVideo(): bool
    {
        return $this->videoUrl !== null && preg_match('#^https?://#i', $this->videoUrl) === 1;
    }

    public function isUploadedVideo(): bool
    {
        return $this->videoUrl !== null && !$this->isExternalVideo();
    }

    public function getVideoPath(): ?string
    {
        if ($this->videoUrl === null) {
            return null;
        }

        if ($this->isExternalVideo()) {
            return $this->videoUrl;
        }

        return '/uploads/lessons/videos/' . $this->videoUrl;
    }

    public function getYouTubeVideoId(): ?string
    {
        if ($this->videoUrl === null || !$this->isExternalVideo()) {
            return null;
        }

        $url = trim($this->videoUrl);

        $vPos = strpos($url, 'v=');
        if ($vPos !== false) {
            $id = substr($url, $vPos + 2);
            $ampPos = strpos($id, '&');
            if ($ampPos !== false) {
                $id = substr($id, 0, $ampPos);
            }
            return trim($id) !== '' ? $id : null;
        }

        $shortPos = strpos($url, 'youtu.be/');
        if ($shortPos !== false) {
            $id = substr($url, $shortPos + strlen('youtu.be/'));
            $qPos = strpos($id, '?');
            if ($qPos !== false) {
                $id = substr($id, 0, $qPos);
            }
            return trim($id) !== '' ? $id : null;
        }

        return null;
    }

    public function getYouTubeThumbnailUrl(): ?string
    {
        $id = $this->getYouTubeVideoId();
        if ($id === null) {
            return null;
        }

        return 'https://img.youtube.com/vi/' . $id . '/hqdefault.jpg';
    }
}
