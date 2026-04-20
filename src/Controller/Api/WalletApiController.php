<?php

namespace App\Controller\Api;

use App\Service\ApiService;
use App\Service\WalletService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\HttpKernel\Exception\UnauthorizedHttpException;

#[Route('/api/wallet', name: 'api_wallet_')]
class WalletApiController extends AbstractController
{
    private ApiService $apiService;
    private WalletService $walletService;
    private EntityManagerInterface $entityManager;
    private \App\Service\WalletBalanceService $balanceService;

    public function __construct(
        ApiService $apiService, 
        WalletService $walletService,
        EntityManagerInterface $entityManager,
        \App\Service\WalletBalanceService $balanceService
    ) {
        $this->apiService = $apiService;
        $this->walletService = $walletService;
        $this->entityManager = $entityManager;
        $this->balanceService = $balanceService;
    }

    #[Route('/balance', name: 'balance', methods: ['GET'])]
    public function getBalance(Request $request): JsonResponse
    {
        // Calculate real balance from transactions to match main dashboard
        $user = $this->getUser();
        if (!$user) return $this->json($this->apiService->error('Access Denied.'), 401);
        $userId = $user->getId();
        $balance = $this->balanceService->calculateUserBalance($userId);
        
        return $this->json($this->apiService->success(['balance' => $balance, 'currency' => 'DT']));
    }

    #[Route('/recharge', name: 'recharge_request', methods: ['POST'])]
    public function requestRecharge(Request $request): JsonResponse
    {
        try {
            $user = $this->getUser();
            if (!$user) return $this->json($this->apiService->error('Access Denied.'), 401);
            $userId = $user->getId();
            $data = json_decode($request->getContent(), true);

            if (!isset($data['cardId'], $data['amount'])) {
                return $this->json($this->apiService->error('Missing cardId or amount.'), 400);
            }

            $rechargeRequest = $this->walletService->createRechargeRequest($userId, (int)$data['cardId'], (float)$data['amount']);
            
            return $this->json($this->apiService->success([
                'requestId' => $rechargeRequest->getId(),
                'requiresOtp' => $rechargeRequest->getOtp() !== null,
                'demoOtp' => $rechargeRequest->getOtp(), // Provide OTP for local testing
                'message' => $rechargeRequest->getOtp() !== null ? 'OTP required for high amount.' : 'Request created.'
            ], 'Recharge request initiated.'));

        } catch (\Exception $e) {
            return $this->json($this->apiService->error($e->getMessage()), 400);
        }
    }

    #[Route('/recharge/confirm', name: 'recharge_confirm', methods: ['POST'])]
    public function confirmRecharge(Request $request): JsonResponse
    {
        try {
            $user = $this->getUser();
            if (!$user) return $this->json($this->apiService->error('Access Denied.'), 401);
            $userId = $user->getId();
            $data = json_decode($request->getContent(), true);

            if (!isset($data['requestId'])) {
                return $this->json($this->apiService->error('Missing requestId.'), 400);
            }

            try {
                $recharge = $this->walletService->confirmRecharge($userId, (int)$data['requestId'], $data['otp'] ?? null);

                return $this->json($this->apiService->success([
                    'status' => $recharge->getStatus(),
                    'newBalance' => $this->balanceService->calculateUserBalance($userId),
                    'message' => 'Wallet recharge processed Successfully.'
                ], 'Recharge verified.'));
            } catch (\Exception $e) {
                return $this->json($this->apiService->error($e->getMessage()), 400);
            }

        } catch (\Exception $e) {
            return $this->json($this->apiService->error($e->getMessage(), 401), 401);
        }
    }

    #[Route('/transactions', name: 'transactions', methods: ['GET'])]
    public function getTransactions(Request $request): JsonResponse
    {
        return $this->json($this->apiService->success([], 'Feature disabled in this version.'));
    }
}
