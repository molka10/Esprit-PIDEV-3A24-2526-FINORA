<?php

namespace App\Controller\Api;

use App\Entity\Card;
use App\Service\ApiService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\HttpKernel\Exception\UnauthorizedHttpException;

#[Route('/api/cards', name: 'api_cards_')]
class CardApiController extends AbstractController
{
    private ApiService $apiService;
    private EntityManagerInterface $entityManager;

    public function __construct(ApiService $apiService, EntityManagerInterface $entityManager)
    {
        $this->apiService = $apiService;
        $this->entityManager = $entityManager;
    }

    #[Route('', name: 'list', methods: ['GET'])]
    public function list(Request $request): JsonResponse
    {
        try {
            $user = $this->getUser();
            if (!$user) return $this->json($this->apiService->error('Access Denied.'), 401);
            $userId = $user->getId();
            $cards = $this->entityManager->getRepository(Card::class)->findBy(['userId' => $userId]);
            
            $data = array_map(fn(Card $card) => [
                'id' => $card->getId(),
                'cardHolderName' => $card->getCardHolderName(),
                'maskedNumber' => $card->getMaskedNumber(),
                'expiryDate' => $card->getExpiryDate(),
                'isDefault' => $card->isDefault(),
                'createdAt' => $card->getCreatedAt()->format('c')
            ], $cards);

            return $this->json($this->apiService->success($data));
        } catch (\Exception $e) {
            return $this->json($this->apiService->error($e->getMessage()), 400);
        }
    }

    #[Route('/link', name: 'link', methods: ['POST'])]
    public function link(Request $request): JsonResponse
    {
        try {
            $user = $this->getUser();
            if (!$user) return $this->json($this->apiService->error('Access Denied.'), 401);
            $userId = $user->getId();
            $data = json_decode($request->getContent(), true);

            if (!isset($data['stripePaymentMethodId'], $data['last4'], $data['brand'], $data['expiryDate'], $data['cardHolderName'])) {
                return $this->json($this->apiService->error('Missing required secure card fields.'), 400);
            }

            $card = new Card();
            $card->setUserId($userId);
            $card->setCardHolderName($data['cardHolderName']);
            $card->setStripePaymentMethodId($data['stripePaymentMethodId']);
            $card->setLast4($data['last4']);
            $card->setBrand($data['brand']);
            $card->setExpiryDate($data['expiryDate']);

            
            $existingCards = $this->entityManager->getRepository(Card::class)->findBy(['userId' => $userId]);
            if (empty($existingCards)) {
                $card->setIsDefault(true);
            }

            $this->entityManager->persist($card);
            $this->entityManager->flush();

            return $this->json($this->apiService->success([
                'id' => $card->getId(),
                'maskedNumber' => $card->getMaskedNumber()
            ], 'Card linked successfully.'));

        } catch (\Exception $e) {
            return $this->json($this->apiService->error($e->getMessage()), 400);
        }
    }

    #[Route('/{id}', name: 'delete', methods: ['DELETE'])]
    public function delete(int $id, Request $request): JsonResponse
    {
        try {
            $user = $this->getUser();
            if (!$user) return $this->json($this->apiService->error('Access Denied.'), 401);
            $userId = $user->getId();
            $card = $this->entityManager->getRepository(Card::class)->findOneBy(['id' => $id, 'userId' => $userId]);

            if (!$card) {
                return $this->json($this->apiService->error('Card not found.'), 404);
            }

            $this->entityManager->remove($card);
            $this->entityManager->flush();

            return $this->json($this->apiService->success([], 'Card deleted successfully.'));
        } catch (\Exception $e) {
            return $this->json($this->apiService->error($e->getMessage()), 400);
        }
    }

    #[Route('/{id}/default', name: 'set_default', methods: ['PATCH'])]
    public function setDefault(int $id, Request $request): JsonResponse
    {
        try {
            $user = $this->getUser();
            if (!$user) return $this->json($this->apiService->error('Access Denied.'), 401);
            $userId = $user->getId();
            $cardRepository = $this->entityManager->getRepository(Card::class);
            $card = $cardRepository->findOneBy(['id' => $id, 'userId' => $userId]);

            if (!$card) {
                return $this->json($this->apiService->error('Card not found.'), 404);
            }

            $cards = $cardRepository->findBy(['userId' => $userId]);
            foreach ($cards as $c) {
                $c->setIsDefault($c->getId() === $id);
            }

            $this->entityManager->flush();

            return $this->json($this->apiService->success([], 'Default card updated.'));
        } catch (\Exception $e) {
            return $this->json($this->apiService->error($e->getMessage()), 400);
        }
    }
}
