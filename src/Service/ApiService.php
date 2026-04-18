<?php

namespace App\Service;

use App\Entity\UserApiKey;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpKernel\Exception\UnauthorizedHttpException;

class ApiService
{
    private string $masterApiKey;

    public function __construct(EntityManagerInterface $entityManager, string $masterApiKey)
    {
        $this->entityManager = $entityManager;
        $this->masterApiKey = $masterApiKey;
    }

    /**
     * Simplified authentication: returns the userId.
     * In this refactored version, we removed the Master API Key check for internal routes
     * to focus on the external métier call.
     */
    public function authenticate(Request $request): int
    {
        // For this demo, we assume the user is authenticated via session or default
        // We'll use X-User-Id header if present, or default to 6.
        return (int) $request->headers->get('X-User-Id', 6);
    }

    public function success(mixed $data = [], string $message = 'Success'): array
    {
        return [
            'status' => 'success',
            'message' => $message,
            'data' => $data,
            'timestamp' => (new \DateTime())->format('c')
        ];
    }

    public function error(string $message, int $code = 400): array
    {
        return [
            'status' => 'error',
            'code' => $code,
            'message' => $message,
            'timestamp' => (new \DateTime())->format('c')
        ];
    }
}
