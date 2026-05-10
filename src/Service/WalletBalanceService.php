<?php

namespace App\Service;

use App\Repository\TransactionWalletRepository;

class WalletBalanceService
{
    private TransactionWalletRepository $transactionRepository;
    private \Doctrine\ORM\EntityManagerInterface $em;

    public function __construct(TransactionWalletRepository $transactionRepository, \Doctrine\ORM\EntityManagerInterface $em)
    {
        $this->transactionRepository = $transactionRepository;
        $this->em = $em;
    }

    public function calculateUserBalance(int $userId): float
    {
        $user = $this->em->getRepository(\App\Entity\User::class)->find($userId);
        if (!$user) return 0.0;

        $currentField = (float)$user->getBalance();

        // 🔥 One-time sync: if balance field is 0 but history exists
        if ($currentField == 0) {
            $transactions = $this->transactionRepository->findBy(['user' => $userId, 'status' => 'ACCEPTED']);
            $calculated = 0.0;
            foreach ($transactions as $t) {
                $amt = abs((float)$t->getMontant());
                $calculated += ($t->getType() === 'OUTCOME') ? -$amt : $amt;
            }
            if ($calculated != 0) {
                $user->setBalance((string)$calculated);
                $this->em->flush();
                return $calculated;
            }
        }

        return $currentField;
    }
}
