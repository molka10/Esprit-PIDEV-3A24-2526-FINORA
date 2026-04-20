<?php

namespace App\Controller\Api;

use App\Entity\UserApiKey;
use App\Service\ApiService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/api/security', name: 'api_security_')]
class SecurityApiController extends AbstractController
{
    private ApiService $apiService;
    private EntityManagerInterface $entityManager;

    public function __construct(ApiService $apiService, EntityManagerInterface $entityManager)
    {
        $this->apiService = $apiService;
        $this->entityManager = $entityManager;
    }

    /**
     * Simulates a login or API key generation for a user.
     * In a real app, this would verify credentials.
     */
    #[Route('/token', name: 'generate_token', methods: ['POST'])]
    public function generateToken(Request $request): JsonResponse
    {
        $data = json_decode($request->getContent(), true);
        $userId = $data['userId'] ?? 6; // Default to 6 for testing

        // Check if user already has a valid token
        $existingToken = $this->entityManager->getRepository(UserApiKey::class)->findOneBy(['userId' => $userId]);
        
        if ($existingToken && $existingToken->isValid()) {
            return $this->json($this->apiService->success([
                'token' => $existingToken->getToken(),
                'expiresAt' => $existingToken->getExpiresAt()?->format('c')
            ], 'Existing active token retrieved.'));
        }

        // Generate new token
        $token = bin2hex(random_bytes(32));
        $apiKey = new UserApiKey();
        $apiKey->setUserId($userId);
        $apiKey->setToken($token);
        $apiKey->setExpiresAt((new \DateTimeImmutable())->modify('+30 days'));

        $this->entityManager->persist($apiKey);
        $this->entityManager->flush();

        return $this->json($this->apiService->success([
            'token' => $token,
            'expiresAt' => $apiKey->getExpiresAt()->format('c')
        ], 'New Bearer token generated. Use this in Authorization header.'));
    }

    #[Route('/validate', name: 'validate_token', methods: ['GET'])]
    public function validate(Request $request): JsonResponse
    {
        try {
            $userId = $this->apiService->authenticate($request);
            return $this->json($this->apiService->success(['userId' => $userId], 'Token is valid.'));
        } catch (\Exception $e) {
            return $this->json($this->apiService->error($e->getMessage(), 401), 401);
        }
    }
}
