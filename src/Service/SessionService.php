<?php

namespace App\Service;

use App\Entity\User;
use App\Entity\UserSession;
use App\Repository\UserSessionRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\HttpFoundation\Request;

class SessionService
{
    public function __construct(
        private EntityManagerInterface $entityManager,
        private UserSessionRepository $userSessionRepo
    ) {}

    public function recordSession(User $user, Request $request): void
    {
        $sessionId = $request->getSession()->getId();
        
        // Check if session already recorded
        $existing = $this->userSessionRepo->findOneBy(['sessionId' => $sessionId]);
        
        if ($existing) {
            $existing->setLastActivity(new \DateTime());
        } else {
            $session = new UserSession();
            $session->setUser($user);
            $session->setSessionId($sessionId);
            $session->setIpAddress($request->getClientIp());
            $session->setDeviceName($this->parseUserAgent($request->headers->get('User-Agent')));
            $session->setLastActivity(new \DateTime());
            
            $this->entityManager->persist($session);
        }
        
        $this->entityManager->flush();
    }

    public function revokeSession(int $sessionId): bool
    {
        $session = $this->userSessionRepo->find($sessionId);
        if ($session) {
            $this->entityManager->remove($session);
            $this->entityManager->flush();
            return true;
        }
        return false;
    }

    private function parseUserAgent(?string $userAgent): string
    {
        if (!$userAgent) return 'Device Inconnu';

        if (preg_match('/iPhone|iPad|iPod/i', $userAgent)) return 'iOS Device';
        if (preg_match('/Android/i', $userAgent)) return 'Android Device';
        if (preg_match('/Windows/i', $userAgent)) return 'Windows PC';
        if (preg_match('/Macintosh/i', $userAgent)) return 'Mac';
        if (preg_match('/Linux/i', $userAgent)) return 'Linux PC';

        return 'Navigateur';
    }
}
