<?php

namespace App\Entity;

use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Bridge\Doctrine\Validator\Constraints\UniqueEntity;
use Symfony\Component\Security\Core\User\UserInterface;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity]
#[ORM\Table(name: "users")]
#[UniqueEntity(fields: ['email'], message: 'There is already an account with this email')]
#[UniqueEntity(fields: ['username'], message: 'This username is already taken')]
class User implements UserInterface, PasswordAuthenticatedUserInterface
{
    public function __construct()
    {
        $this->wishlist = new ArrayCollection();
        $this->purchasedFormations = new ArrayCollection();
        $this->created_at = new \DateTime();
    }
    // ================= ID =================
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\OneToOne(mappedBy: 'user', targetEntity: UserBiometrics::class, cascade: ['persist', 'remove'])]
    private ?UserBiometrics $userBiometrics = null;

    // ================= IMAGE =================
    #[ORM\Column(length: 255, nullable: true)]
    private ?string $image = null;

    public function getImage(): ?string
    {
        return $this->image;
    }

    public function setImage(?string $image): static
    {
        $this->image = $image;
        return $this;
    }

    // ================= USERNAME =================
    #[Assert\NotBlank(message: 'Username is required')]
    #[ORM\Column(length: 100, unique: true)]
    private ?string $username = null;

    // ================= EMAIL =================
    #[Assert\NotBlank(message: 'Email is required')]
    #[Assert\Email(message: 'Invalid email format')]
    #[ORM\Column(length: 180, unique: true)]
    private ?string $email = null;

    // ================= PASSWORD =================
    #[ORM\Column(name: "mot_de_passe", nullable: true)]
    private ?string $password = null;

    // ================= ROLE =================
    #[ORM\Column(length: 20)]
    private ?string $role = 'USER';

    // ================= PHONE =================
    #[Assert\Regex(
        pattern: '/^[0-9]{8}$/',
        message: 'Phone must be 8 digits'
    )]
    #[ORM\Column(length: 20, nullable: true)]
    private ?string $phone = null;

    // ================= ADDRESS =================
    #[Assert\NotBlank(message: 'Address is required')]
    #[ORM\Column(length: 100, nullable: true)]
    private ?string $address = null;

    // ================= DOB =================
    #[ORM\Column(type: "date", nullable: true)]
    private ?\DateTimeInterface $date_of_birth = null;

    // ================= CREATED =================
    #[ORM\Column(type: "datetime")]
    private ?\DateTimeInterface $created_at = null;

    // ================= VERIFIED =================
    #[ORM\Column(type: 'boolean')]
    private bool $isVerified = false;

    // ================= SECURITY =================

    public function getUserIdentifier(): string
    {
        return $this->email ?? '';
    }

    // 🔥 FIXED ROLE SYSTEM (IMPORTANT)
    public function getRoles(): array
    {
        return ['ROLE_' . strtoupper($this->role)];
    }

    public function getPassword(): string
    {
        return $this->password ?? '';
    }

    public function eraseCredentials(): void {}

    // ================= GETTERS / SETTERS =================

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getUsername(): ?string
    {
        return $this->username;
    }

    public function setUsername(string $username): static
    {
        $this->username = $username;
        return $this;
    }

    public function getEmail(): ?string
    {
        return $this->email;
    }

    public function setEmail(string $email): static
    {
        $this->email = $email;
        return $this;
    }

    public function setPassword(?string $password): static
    {
        $this->password = $password;
        return $this;
    }

    public function getRole(): ?string
    {
        return $this->role;
    }

    public function setRole(string $role): static
    {
        $this->role = strtoupper($role); // 🔥 ensure consistency
        return $this;
    }

    public function getPhone(): ?string
    {
        return $this->phone;
    }

    public function setPhone(?string $phone): static
    {
        $this->phone = $phone;
        return $this;
    }

    public function getAddress(): ?string
    {
        return $this->address;
    }

    public function setAddress(?string $address): static
    {
        $this->address = $address;
        return $this;
    }

    public function getDateOfBirth(): ?\DateTimeInterface
    {
        return $this->date_of_birth;
    }

    public function setDateOfBirth(?\DateTimeInterface $date): static
    {
        $this->date_of_birth = $date;
        return $this;
    }

    public function getCreatedAt(): ?\DateTimeInterface
    {
        return $this->created_at;
    }

    public function setCreatedAt(\DateTimeInterface $date): static
    {
        $this->created_at = $date;
        return $this;
    }

    public function isVerified(): bool
    {
        return $this->isVerified;
    }

    public function setIsVerified(bool $isVerified): static
    {
        $this->isVerified = $isVerified;
        return $this;
    }

    #[ORM\ManyToMany(targetEntity: Formation::class, inversedBy: 'wishlistedBy')]
    #[ORM\JoinTable(name: 'user_formation_wishlist')]
    private Collection $wishlist;

    #[ORM\ManyToMany(targetEntity: Formation::class, inversedBy: 'purchasedBy')]
    #[ORM\JoinTable(name: 'user_formation_purchased')]
    private Collection $purchasedFormations;

    public function getWishlist(): Collection
    {
        return $this->wishlist;
    }

    public function getPurchasedFormations(): Collection
    {
        return $this->purchasedFormations;
    }

    public function addToWishlist(Formation $formation): static
    {
        if (!$this->wishlist->contains($formation)) {
            $this->wishlist->add($formation);
        }
        return $this;
    }

    public function addPurchasedFormation(Formation $formation): static
    {
        if (!$this->purchasedFormations->contains($formation)) {
            $this->purchasedFormations->add($formation);
        }
        return $this;
    }

    public function removeFromWishlist(Formation $formation): static
    {
        $this->wishlist->removeElement($formation);
        return $this;
    }

    public function removePurchasedFormation(Formation $formation): static
    {
        $this->purchasedFormations->removeElement($formation);
        return $this;
    }

    public function getUserBiometrics(): ?UserBiometrics
    {
        return $this->userBiometrics;
    }

    public function setUserBiometrics(UserBiometrics $userBiometrics): static
    {
        // set the owning side of the relation if necessary
        if ($userBiometrics->getUser() !== $this) {
            $userBiometrics->setUser($this);
        }

        $this->userBiometrics = $userBiometrics;

        return $this;
    }
}