<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;
use Psr\Log\LoggerInterface;
use Doctrine\ORM\EntityManagerInterface;
use App\Entity\RechargeRequest;
use App\Entity\Card;
use App\Entity\Category;
use App\Entity\TransactionWallet;
use App\Entity\User;

class PaymentGatewayService
{
    private HttpClientInterface $httpClient;
    private string $apiKey;
    private string $apiUrl;
    private LoggerInterface $logger;
    private EntityManagerInterface $entityManager;

    public function __construct(
        HttpClientInterface $httpClient, 
        string $paymentApiKey, 
        string $paymentApiUrl,
        LoggerInterface $logger,
        EntityManagerInterface $entityManager
    ) {
        $this->httpClient = $httpClient;
        $this->apiKey = $paymentApiKey;
        $this->apiUrl = $paymentApiUrl;
        $this->logger = $logger;
        $this->entityManager = $entityManager;
    }

    /**
     * Handles wallet recharge by calling an external payment API.
     * Demonstrates advanced métier logic using .env API keys.
     */
    public function processRecharge(RechargeRequest $recharge): array
    {
        $amount = $recharge->getAmount();
        $card = $this->entityManager->getRepository(Card::class)->find($recharge->getCardId());
        $userId = $recharge->getUserId() ?? 6;

        if (!$card) {
            $recharge->setStatus('FAILED');
            $this->entityManager->flush();
            return ['status' => 'failed', 'message' => 'Card not found.'];
        }

        $this->logger->info('Initiating payment process', ['url' => $this->apiUrl, 'amount' => $amount]);

        // Simulation Mode (for the placeholder URL)
        if (str_contains($this->apiUrl, 'api.finora-payment.com')) {
            $this->handleSuccessfulRecharge($recharge, $userId, $amount);
            return ['status' => 'success'];
        }

        try {
            // STRIPE INTEGRATION LOGIC
            // Note: Stripe uses application/x-www-form-urlencoded
            $payload = [
                'amount' => (int)($amount * 100), // Stripe expects cents
                'currency' => 'usd', // Demo currency
                'payment_method' => 'pm_card_visa', // Using Stripe test card token for success
                'confirm' => 'true',
                'description' => 'Wallet Recharge for User ID: ' . $userId,
                'automatic_payment_methods[enabled]' => 'true',
                'automatic_payment_methods[allow_redirects]' => 'never'
            ];

            $response = $this->httpClient->request('POST', $this->apiUrl, [
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                    'Content-Type' => 'application/x-www-form-urlencoded'
                ],
                'body' => $payload
            ]);

            $statusCode = $response->getStatusCode();
            if ($statusCode === 200 || $statusCode === 201) {
                $this->handleSuccessfulRecharge($recharge, $userId, $amount);
                return ['status' => 'success'];
            }

            $errorData = $response->toArray(false);
            $recharge->setStatus('FAILED');
            $this->entityManager->flush();
            return ['status' => 'failed', 'message' => $errorData['error']['message'] ?? 'Stripe declined transaction.'];

        } catch (\Exception $e) {
            $this->logger->error('External Payment API Error', ['error' => $e->getMessage()]);
            $recharge->setStatus('FAILED');
            $this->entityManager->flush();
            return ['status' => 'failed', 'message' => 'Gateway Connection Error: ' . $e->getMessage()];
        }
    }

    private function handleSuccessfulRecharge(RechargeRequest $recharge, int $userId, float $amount): void
    {
        // Mark Recharge as Completed
        $recharge->setStatus('COMPLETED');
        $recharge->setConfirmedAt(new \DateTimeImmutable());

        // Create Transaction for Dashboard
        $user = $this->entityManager->getRepository(User::class)->find($userId);
        if (!$user) {
            $this->logger->error('Cannot create recharge transaction: User not found', ['userId' => $userId]);
            return;
        }
        $transaction = new TransactionWallet();
        $transaction->setUser($user);
        $transaction->setNomTransaction('Recharge Portfolio (API Stripe)');
        $transaction->setMontant($amount);
        $transaction->setType('INCOME');
        $transaction->setDateTransaction(new \DateTime());
        $transaction->setSource('STRIPE_GW');
        
        // Assign default category
        $category = $this->entityManager->getRepository(Category::class)->findOneBy(['nom' => 'Revenu'])
                   ?? $this->entityManager->getRepository(Category::class)->findOneBy(['nom' => 'Transfert'])
                   ?? $this->entityManager->getRepository(Category::class)->findAll()[0] ?? null;
        
        if ($category) {
            $transaction->setCategory($category);
        }

        // --- APPROVAL LOGIC ---
        if (abs($amount) > 5000) {
            $transaction->setStatus('PENDING');
        } else {
            $transaction->setStatus('ACCEPTED');
        }
        
        $this->entityManager->persist($transaction);
        $this->entityManager->flush();
    }
}
