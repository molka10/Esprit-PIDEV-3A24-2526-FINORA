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
        $transactions = $this->transactionRepository->findBy(['userId' => $userId]);
        $balance = 0.0;

        foreach ($transactions as $transaction) {
            // The native wallet stores INCOME as positive montant, OUTCOME as negative montant.
            // We simply sum all raw montant values.
            $balance += $transaction->getMontant();
        }

        return $balance;
    }
}
