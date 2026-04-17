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
            if (strtolower($transaction->getType()) === 'income' || strtolower($transaction->getType()) === 'revenu') {
                $balance += $transaction->getMontant();
            } else {
                $balance -= $transaction->getMontant();
            }
        }

        return $balance;
    }
}
