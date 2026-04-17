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
            // Because outcomes are often saved as negative values natively in the DB, 
            // we should just add the raw values, or strictly use absolute values.
            if (strtolower($transaction->getType()) === 'income' || strtolower($transaction->getType()) === 'revenu') {
                $balance += abs($transaction->getMontant());
            } else {
                $balance -= abs($transaction->getMontant());
            }
        }

        return $balance;
    }
}
