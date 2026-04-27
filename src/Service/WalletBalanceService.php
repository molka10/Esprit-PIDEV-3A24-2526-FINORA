<?php

namespace App\Service;

use App\Repository\TransactionWalletRepository;

class WalletBalanceService
{
    private TransactionWalletRepository $transactionRepository;

    public function __construct(TransactionWalletRepository $transactionRepository)
    {
        $this->transactionRepository = $transactionRepository;
    }

    public function calculateUserBalance(int $userId): float
    {
        $transactions = $this->transactionRepository->findBy(['user' => $userId]);
        $balance = 0.0;

        foreach ($transactions as $transaction) {
            // Only count ACCEPTED transactions
            if ($transaction->getStatus() !== 'ACCEPTED') {
                continue;
            }
            // Robust calculation: Use the type field to determine the sign,
            // ensuring that even if signs are inconsistent in the DB, the balance is correct.
            $amount = abs($transaction->getMontant());
            if ($transaction->getType() === 'OUTCOME') {
                $balance -= $amount;
            } else {
                $balance += $amount;
            }
        }

        return $balance;
    }
}
