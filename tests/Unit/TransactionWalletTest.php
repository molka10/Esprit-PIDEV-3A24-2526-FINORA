<?php

namespace App\Tests\Unit;

use App\Entity\TransactionWallet;
use App\Entity\User;
use App\Entity\Category;
use PHPUnit\Framework\TestCase;

class TransactionWalletTest extends TestCase
{
    public function testTransactionWalletGettersAndSetters(): void
    {
        $transaction = new TransactionWallet();
        $transaction->setMontant(500.00);
        $transaction->setType('DEPOSIT');
        
        $user = new User();
        $user->setUsername('walletuser');
        $transaction->setUser($user);

        $this->assertEquals(500.00, $transaction->getMontant());
        $this->assertEquals('DEPOSIT', $transaction->getType());
        $this->assertSame($user, $transaction->getUser());
    }

    public function testTransactionWalletCategoryAndStatus(): void
    {
        $transaction = new TransactionWallet();
        $category = new Category();
        
        // Test default status
        $this->assertEquals('ACCEPTED', $transaction->getStatus());
        $this->assertTrue($transaction->getIsActive());

        $transaction->setCategory($category);
        $transaction->setStatus('PENDING');
        $transaction->setIsActive(false);

        $this->assertSame($category, $transaction->getCategory());
        $this->assertEquals('PENDING', $transaction->getStatus());
        $this->assertFalse($transaction->getIsActive());
    }
}
