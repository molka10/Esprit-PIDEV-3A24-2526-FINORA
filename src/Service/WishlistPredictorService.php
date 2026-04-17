<?php

namespace App\Service;

class WishlistPredictorService
{
    private WalletBalanceService $balanceService;

    public function __construct(WalletBalanceService $balanceService)
    {
        $this->balanceService = $balanceService;
    }

    public function canAffordWishlistItem(int $userId, float $itemPrice): array
    {
        $currentBalance = $this->balanceService->calculateUserBalance($userId);
        $difference = $currentBalance - $itemPrice;

        return [
            'can_afford' => $difference >= 0,
            'remaining_needed' => $difference < 0 ? abs($difference) : 0,
            'current_balance' => $currentBalance
        ];
    }
}
