<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: "users")]
class User
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 50, unique: true)]
    private ?string $username = null;

    #[ORM\Column(length: 150, unique: true)]
    private ?string $email = null;

    #[ORM\Column(name: "mot_de_passe", length: 255)]
    private ?string $motDePasse = null;

    #[ORM\Column(type: "string", columnDefinition: "ENUM('ADMIN', 'ENTREPRISE', 'USER')")]
    private ?string $role = 'USER';

    #[ORM\Column(length: 20, nullable: true)]
    private ?string $phone = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $address = null;

    #[ORM\Column(type: "date", nullable: true)]
    private ?\DateTimeInterface $dateOfBirth = null;

    #[ORM\Column(type: "datetime", options: ["default" => "CURRENT_TIMESTAMP"])]
    private ?\DateTimeInterface $createdAt = null;

    #[ORM\Column(type: "float", options: ["default" => 0.0])]
    private ?float $balance = 0.0;

    public function getId(): ?int { return $this->id; }
    public function getUsername(): ?string { return $this->username; }
    public function getEmail(): ?string { return $this->email; }
    public function getBalance(): ?float { return $this->balance; }
    public function setBalance(float $balance): self { $this->balance = $balance; return $this; }
}
