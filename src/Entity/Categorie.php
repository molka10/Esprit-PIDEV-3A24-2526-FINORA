<?php

namespace App\Entity;

use App\Repository\CategorieRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;
use Symfony\Bridge\Doctrine\Validator\Constraints\UniqueEntity;

#[ORM\Entity(repositoryClass: CategorieRepository::class)]
#[UniqueEntity(fields: ['nom'], message: 'Cette catégorie existe déjà')]
class Categorie
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 100)]
    #[Assert\NotBlank(message: 'Le nom est obligatoire')]
    #[Assert\Length(
        min: 2,
        max: 100,
        minMessage: 'Le nom doit contenir au moins {{ limit }} caractères',
        maxMessage: 'Le nom ne peut pas dépasser {{ limit }} caractères'
    )]
    #[Assert\Regex(
        pattern: '/^[a-zA-ZÀ-ÿ0-9\s\-]+$/',
        message: 'Le nom ne peut contenir que des lettres, chiffres, espaces et tirets'
    )]
    private ?string $nom = null;

    #[ORM\OneToMany(targetEntity: AppelOffre::class, mappedBy: 'categorie')]
    private Collection $appelOffres;

    public function __construct()
    {
        $this->appelOffres = new ArrayCollection();
    }

    public function getId(): ?int { return $this->id; }
    public function getNom(): ?string { return $this->nom; }
    public function setNom(string $nom): static { $this->nom = $nom; return $this; }
    public function getAppelOffres(): Collection { return $this->appelOffres; }
    public function addAppelOffre(AppelOffre $appelOffre): static
    {
        if (!$this->appelOffres->contains($appelOffre)) {
            $this->appelOffres->add($appelOffre);
            $appelOffre->setCategorie($this);
        }
        return $this;
    }
    public function removeAppelOffre(AppelOffre $appelOffre): static
    {
        if ($this->appelOffres->removeElement($appelOffre)) {
            if ($appelOffre->getCategorie() === $this) {
                $appelOffre->setCategorie(null);
            }
        }
        return $this;
    }
}