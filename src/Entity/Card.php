<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity]
#[ORM\Table(name: "cards")]
class Card
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank]
    private ?string $cardHolderName = null;

    #[ORM\Column(length: 20)]
    #[Assert\NotBlank]
    #[Assert\Luhn]
    private ?string $cardNumber = null; // Stored masked or encrypted in real apps, but here we store as string for simplicity

    #[ORM\Column(length: 5)]
    #[Assert\NotBlank]
    #[Assert\Regex(pattern: "/^(0[1-9]|1[0-2])\/\d{2}$/")]
    private ?string $expiryDate = null; // Format: MM/YY

    #[ORM\Column(length: 4)]
    #[Assert\NotBlank]
    private ?string $cvv = null;

    #[ORM\Column]
    private ?int $userId = null;

    #[ORM\Column(type: "boolean")]
    private bool $isDefault = false;

    #[ORM\Column(type: "datetime_immutable")]
    private ?\DateTimeImmutable $createdAt = null;

    public function __construct()
    {
        $this->createdAt = new \DateTimeImmutable();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getCardHolderName(): ?string
    {
        return $this->cardHolderName;
    }

    public function setCardHolderName(string $cardHolderName): static
    {
        $this->cardHolderName = $cardHolderName;
        return $this;
    }

    public function getCardNumber(): ?string
    {
        return $this->cardNumber;
    }

    public function setCardNumber(string $cardNumber): static
    {
        $this->cardNumber = $cardNumber;
        return $this;
    }

    public function getExpiryDate(): ?string
    {
        return $this->expiryDate;
    }

    public function setExpiryDate(string $expiryDate): static
    {
        $this->expiryDate = $expiryDate;
        return $this;
    }

    public function getCvv(): ?string
    {
        return $this->cvv;
    }

    public function setCvv(string $cvv): static
    {
        $this->cvv = $cvv;
        return $this;
    }

    public function getUserId(): ?int
    {
        return $this->userId;
    }

    public function setUserId(int $userId): static
    {
        $this->userId = $userId;
        return $this;
    }

    public function isDefault(): bool
    {
        return $this->isDefault;
    }

    public function setIsDefault(bool $isDefault): static
    {
        $this->isDefault = $isDefault;
        return $this;
    }

    public function getCreatedAt(): ?\DateTimeImmutable
    {
        return $this->createdAt;
    }

    public function getMaskedNumber(): string
    {
        return '**** **** **** ' . substr($this->cardNumber, -4);
    }
}
