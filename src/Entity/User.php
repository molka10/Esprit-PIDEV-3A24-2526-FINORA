<?php

namespace App\Entity;

use App\Repository\UserRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Security\Core\User\UserInterface;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;
use Symfony\Component\Validator\Constraints as Assert;
use Symfony\Bridge\Doctrine\Validator\Constraints\UniqueEntity;

#[UniqueEntity(fields: ['firstname'], message: 'This username is already used.')]

#[ORM\Entity(repositoryClass: UserRepository::class)]
#[ORM\Table(name: "users")]
class User implements UserInterface, PasswordAuthenticatedUserInterface
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 180)]
    #[Assert\NotBlank]
    #[Assert\Email]
    private ?string $email = null;

    #[ORM\Column(name: "mot_de_passe")]
    private ?string $password = null;

    #[ORM\Column(length: 50)]
    private ?string $role = null;

    // 🔥 IMPORTANT FIX → map username to firstname
    #[ORM\Column(name: "username", length: 100)]
    #[Assert\NotBlank]
    private ?string $firstname = null;

    #[ORM\Column(name: "role_type", length: 50)]
    private ?string $roleType = null;

    #[ORM\Column(name: "is_active")]
    private bool $isActive = true;

    // OPTIONAL FIELDS
    #[ORM\Column(nullable: true)]
    private ?string $phone = null;

    #[ORM\Column(nullable: true)]
    private ?string $address = null;

   #[ORM\Column(name: "date_of_birth", type: "date", nullable: true)]
    private ?\DateTimeInterface $dateOfBirth = null;

    #[ORM\Column(name: "created_at", type: "datetime")]
    private ?\DateTimeInterface $createdAt = null;

    // ================= SECURITY =================

    public function getUserIdentifier(): string
    {
        return $this->email;
    }

    public function getRoles(): array
{
    $roles = ['ROLE_' . strtoupper($this->role)];

    // ✅ ALWAYS add ROLE_USER
    $roles[] = 'ROLE_USER';

    return array_unique($roles);
}

    public function eraseCredentials() {}

    // ================= GETTERS & SETTERS =================

    public function getId(): ?int { return $this->id; }

    public function getEmail(): ?string { return $this->email; }
    public function setEmail(string $email): static { $this->email = $email; return $this; }

    public function getPassword(): string { return $this->password; }
    public function setPassword(string $password): static { $this->password = $password; return $this; }

    public function getRole(): ?string { return $this->role; }
    public function setRole(string $role): static { $this->role = $role; return $this; }

    public function getFirstname(): ?string { return $this->firstname; }
    public function setFirstname(string $firstname): static { $this->firstname = $firstname; return $this; }

    public function getRoleType(): ?string { return $this->roleType; }
    public function setRoleType(string $roleType): static { $this->roleType = $roleType; return $this; }

    public function isActive(): bool { return $this->isActive; }
    public function setIsActive(bool $isActive): static { $this->isActive = $isActive; return $this; }

    public function getCreatedAt(): ?\DateTimeInterface { return $this->createdAt; }
    public function setCreatedAt(\DateTimeInterface $createdAt): static { $this->createdAt = $createdAt; return $this; }

    public function getPhone(): ?string { return $this->phone; }
    public function setPhone(?string $phone): static { $this->phone = $phone; return $this; }

    public function getAddress(): ?string { return $this->address; }
    public function setAddress(?string $address): static { $this->address = $address; return $this; }

    public function getDateOfBirth(): ?\DateTimeInterface { return $this->dateOfBirth; }
    public function setDateOfBirth(?\DateTimeInterface $dateOfBirth): static { $this->dateOfBirth = $dateOfBirth; return $this; }
}