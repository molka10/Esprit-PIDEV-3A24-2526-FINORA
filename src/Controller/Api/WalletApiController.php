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
    private \App\Service\CurrencyConverterService $currencyConverter;

    public function __construct(
        ApiService $apiService, 
        WalletService $walletService,
        EntityManagerInterface $entityManager,
        \App\Service\WalletBalanceService $balanceService,
        \App\Service\CurrencyConverterService $currencyConverter
    ) {
        $this->apiService = $apiService;
        $this->walletService = $walletService;
        $this->entityManager = $entityManager;
        $this->balanceService = $balanceService;
        $this->currencyConverter = $currencyConverter;
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

    #[Route('/transfer', name: 'transfer', methods: ['POST'])]
    public function transfer(Request $request): JsonResponse
    {
        try {
            $user = $this->getUser();
            if (!$user) return $this->json($this->apiService->error('Access Denied.'), 401);
            $userId = $user->getId();
            
            $data = json_decode($request->getContent(), true);
            
            if (!isset($data['recipientEmail'], $data['amount'])) {
                return $this->json($this->apiService->error('Missing recipientEmail or amount.'), 400);
            }

            $amount = (float) $data['amount'];
            $currency = $data['currency'] ?? 'TND';

            if ($amount <= 0) {
                return $this->json($this->apiService->error('Amount must be strictly positive.'), 400);
            }

            // If the user is sending in another currency (e.g. EUR), convert to TND for processing
            if ($currency !== 'TND') {
                $amount = $this->currencyConverter->convert($amount, $currency, 'TND');
            }

            $recipientEmail = trim($data['recipientEmail']);
            if (strtolower($user->getUserIdentifier()) === strtolower($recipientEmail)) {
                return $this->json($this->apiService->error('You cannot transfer money to yourself.'), 400);
            }

            // Verify Sender Balance
            $senderBalance = $this->balanceService->calculateUserBalance($userId);
            if ($senderBalance < $amount) {
                return $this->json($this->apiService->error("Solde insuffisant pour ce transfert."), 400);
            }

            // Find Recipient
            $recipient = $this->entityManager->getRepository(\App\Entity\User::class)->findOneBy(['email' => $recipientEmail]);
            if (!$recipient) {
                return $this->json($this->apiService->error("Aucun utilisateur trouvé avec cet email."), 404);
            }

            // Find valid categories (fallback to the first available if no default system category exists)
            $catRepo = $this->entityManager->getRepository(\App\Entity\Category::class);
            $catOut = $catRepo->findOneBy(['type' => 'OUTCOME', 'user' => null]) ?? $catRepo->findOneBy(['type' => 'OUTCOME']);
            $catIn = $catRepo->findOneBy(['type' => 'INCOME', 'user' => null]) ?? $catRepo->findOneBy(['type' => 'INCOME']);

            if (!$catOut || !$catIn) {
                return $this->json($this->apiService->error("Erreur de configuration système (Catégories manquantes)."), 500);
            }

            // Execute Atomic Transaction
            $this->entityManager->getConnection()->beginTransaction();
            try {
                // Determine status based on amount (> 5000)
                $status = (abs($amount) > 5000) ? 'PENDING' : 'ACCEPTED';

                // 1. Debit Sender
                $tOut = new \App\Entity\TransactionWallet();
                $tOut->setUser($user);
                $tOut->setNomTransaction("Finora Pay - Envoi à " . $recipientEmail);
                $tOut->setType('OUTCOME');
                $tOut->setMontant(-abs($amount)); // Explicitly negative
                $tOut->setDateTransaction(new \DateTime());
                $tOut->setCategory($catOut);
                $tOut->setStatus($status);
                $this->entityManager->persist($tOut);
                $this->entityManager->flush();

                // 2. Credit Recipient
                $tIn = new \App\Entity\TransactionWallet();
                $tIn->setUser($recipient);
                $tIn->setNomTransaction("Finora Pay - Reçu de " . $user->getUserIdentifier());
                $tIn->setType('INCOME');
                $tIn->setMontant(abs($amount)); // Explicitly positive
                $tIn->setDateTransaction(new \DateTime());
                $tIn->setCategory($catIn);
                $tIn->setStatus($status);
                $this->entityManager->persist($tIn);
                $this->entityManager->flush();
                $this->entityManager->getConnection()->commit();

                return $this->json($this->apiService->success([
                    'newBalance' => $senderBalance - $amount,
                    'transferred' => $amount,
                    'recipient' => $recipientEmail
                ], 'Transfert réussi.'));

            } catch (\Exception $e) {
                $this->entityManager->getConnection()->rollBack();
                throw $e;
            }

        } catch (\Exception $e) {
            return $this->json($this->apiService->error("Erreur critique: " . $e->getMessage()), 500);
        }
    }
}
