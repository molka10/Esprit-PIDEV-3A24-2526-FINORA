<?php

namespace App\Service;

use App\Entity\Card;
use App\Entity\RechargeRequest;
use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;

class WalletService
{
    private EntityManagerInterface $entityManager;
    private PaymentGatewayService $paymentGateway;

    public function __construct(
        EntityManagerInterface $entityManager, 
        PaymentGatewayService $paymentGateway
    ) {
        $this->entityManager = $entityManager;
        $this->paymentGateway = $paymentGateway;
    }

    public function createRechargeRequest(int $userId, int $cardId, float $amount): RechargeRequest
    {
        if ($amount <= 0) throw new \InvalidArgumentException('Amount must be positive.');

        $user = $this->entityManager->getRepository(User::class)->find($userId);
        if (!$user) throw new \InvalidArgumentException('User not found.');

        $rechargeRequest = new RechargeRequest();
        $rechargeRequest->setUser($user);
        $rechargeRequest->setCardId($cardId);
        $rechargeRequest->setAmount($amount);
        
        if ($amount > 1000) {
            $rechargeRequest->setOtp((string)rand(100000, 999999));
        }

        $this->entityManager->persist($rechargeRequest);
        $this->entityManager->flush();
        return $rechargeRequest;
    }

    public function confirmRecharge(int $userId, int $requestId, ?string $otp = null): RechargeRequest
    {
        $request = $this->entityManager->getRepository(RechargeRequest::class)->findOneBy([
            'id' => $requestId,
            'user' => $userId,
            'status' => RechargeRequest::STATUS_PENDING
        ]);

        if (!$request) throw new \InvalidArgumentException('Invalid recharge request.');

        // Check OTP
        if ($request->getOtp() !== null && $otp !== $request->getOtp()) {
            throw new \InvalidArgumentException('Invalid OTP.');
        }

        // --- MÉTIER AVANCÉ: External API Call ---
        $result = $this->paymentGateway->processRecharge($request);

        if ($result['status'] === 'failed') {
            throw new \RuntimeException($result['message'] ?? 'Payment failed.');
        }

        return $request;
    }
}
