<?php

namespace App\Service;

use App\Entity\User;

class RiskScoringService
{
    private const DISPOSABLE_DOMAINS = [
        'tempmail.com', '10minutemail.com', 'guerrillamail.com', 'mailinator.com',
        'yopmail.com', 'yopmail.fr', 'yopmail.net', 'getnada.com', 'trashmail.com'
    ];

    public function compute(User $user): array
    {
        $score = 0;
        $reasons = [];

        // 1. Disposable email
        if ($this->isDisposableEmail($user->getEmail())) {
            $score += 35;
            $reasons[] = "Email jetable détecté";
        }

        // 2. Profile completeness
        if (empty(trim($user->getPhone() ?? ''))) {
            $score += 10;
            $reasons[] = "Téléphone manquant";
        }

        if (empty(trim($user->getAddress() ?? ''))) {
            $score += 8;
            $reasons[] = "Adresse manquante";
        }

        if ($user->getDateOfBirth() === null) {
            $score += 8;
            $reasons[] = "Date de naissance manquante";
        } else {
            $age = $user->getDateOfBirth()->diff(new \DateTime())->y;
            if ($age < 18) {
                $score += 40;
                $reasons[] = "Âge < 18 (incohérent)";
            }
        }

        // 3. Account age
        $accountAgeDays = $this->getAccountAgeDays($user);
        if ($accountAgeDays >= 0 && $accountAgeDays < 3) {
            $score += 15;
            $reasons[] = "Compte très récent (< 3 jours)";
        } elseif ($accountAgeDays >= 0 && $accountAgeDays < 14) {
            $score += 8;
            $reasons[] = "Compte récent (< 14 jours)";
        }

        // 4. Role impact
        if (strtoupper($user->getRole()) === 'ADMIN') {
            $score += 10;
            $reasons[] = "Compte ADMIN (sensibilité élevée)";
        }

        // Clamp score 0..100
        $score = max(0, min(100, $score));

        $level = 'LOW';
        if ($score >= 60) {
            $level = 'HIGH';
        } elseif ($score >= 30) {
            $level = 'MEDIUM';
        }

        return [
            'score' => $score,
            'level' => $level,
            'reasons' => $reasons,
        ];
    }

    private function isDisposableEmail(?string $email): bool
    {
        if (!$email) return false;
        
        $email = trim(strtolower($email));
        $atIndex = strrpos($email, '@');
        if ($atIndex === false) return false;
        
        $domain = substr($email, $atIndex + 1);
        return in_array($domain, self::DISPOSABLE_DOMAINS, true);
    }

    private function getAccountAgeDays(User $user): int
    {
        $createdAt = $user->getCreatedAt();
        if (!$createdAt) return -1;

        return $createdAt->diff(new \DateTime())->days;
    }
}
