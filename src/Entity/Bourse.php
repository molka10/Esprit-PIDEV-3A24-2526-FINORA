<?php

namespace App\Entity;

use App\Repository\BourseRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;
use Symfony\Bridge\Doctrine\Validator\Constraints\UniqueEntity;

#[ORM\Entity(repositoryClass: BourseRepository::class)]
#[ORM\Table(name: 'bourse')]
#[UniqueEntity(
    fields: ['nomBourse'],
    message: 'Ce nom de bourse existe déjà.'
)]
class Bourse
{
    #[ORM\Id]
    #[ORM\GeneratedValue(strategy: 'AUTO')]
    #[ORM\Column(name: 'id_bourse', type: 'integer')] // <-- colonne primaire exact
    private ?int $id = null;

    #[ORM\Column(name: 'nom_bourse', type: 'string', length: 100)]
    #[Assert\NotBlank(message: 'Le nom de la bourse est obligatoire.')]
    #[Assert\Length(
        min: 3,
        max: 100,
        minMessage: 'Le nom doit contenir au moins {{ limit }} caractères.',
        maxMessage: 'Le nom ne peut pas dépasser {{ limit }} caractères.'
    )]
    #[Assert\Regex(
        pattern: '/^[a-zA-ZÀ-ÿ0-9\s\-]+$/',
        message: 'Le nom ne peut contenir que des lettres, chiffres, espaces et tirets.'
    )]
    private ?string $nomBourse = null;

    #[ORM\Column(type: 'string', length: 50)]
    #[Assert\NotBlank(message: 'Le pays est obligatoire.')]
    #[Assert\Length(
        min: 2,
        max: 50,
        minMessage: 'Le pays doit contenir au moins {{ limit }} caractères.',
        maxMessage: 'Le pays ne peut pas dépasser {{ limit }} caractères.'
    )]
    private ?string $pays = null;

    #[ORM\Column(type: 'string', length: 3)]
    #[Assert\NotBlank(message: 'La devise est obligatoire.')]
    #[Assert\Length(
        min: 3,
        max: 3,
        exactMessage: 'La devise doit contenir exactement 3 caractères.'
    )]
    #[Assert\Regex(
        pattern: '/^[A-Z]{3}$/',
        message: 'La devise doit être un code ISO de 3 lettres majuscules (ex: USD, EUR, TND).'
    )]
    private ?string $devise = null;

    #[ORM\Column(type: 'string', length: 20)]
    #[Assert\NotBlank(message: 'Le statut est obligatoire.')]
    #[Assert\Choice(
        choices: ['ACTIVE', 'INACTIVE'],
        message: 'Le statut doit être ACTIVE ou INACTIVE.'
    )]
    private ?string $statut = 'ACTIVE';

    #[ORM\Column(name: 'date_creation', type: 'datetime')]
    private ?\DateTimeInterface $dateCreation = null;

    public function __construct()
    {
        $this->dateCreation = new \DateTime();
    }

    // Getters & Setters
    public function getId(): ?int
    {
        return $this->id;
    }

    public function getNomBourse(): ?string
    {
        return $this->nomBourse;
    }

    public function setNomBourse(string $nomBourse): self
    {
        $this->nomBourse = $nomBourse;
        return $this;
    }

    public function getPays(): ?string
    {
        return $this->pays;
    }

    public function setPays(string $pays): self
    {
        $this->pays = $pays;
        return $this;
    }

    public function getDevise(): ?string
    {
        return $this->devise;
    }

    public function setDevise(string $devise): self
    {
        $this->devise = strtoupper($devise);
        return $this;
    }

    public function getStatut(): ?string
    {
        return $this->statut;
    }

    public function setStatut(string $statut): self
    {
        $this->statut = $statut;
        return $this;
    }

    public function getDateCreation(): ?\DateTimeInterface
    {
        return $this->dateCreation;
    }

    public function setDateCreation(\DateTimeInterface $dateCreation): self
    {
        $this->dateCreation = $dateCreation;
        return $this;
    }

    public function __toString(): string
    {
        return $this->nomBourse ?? '';
    }
}