<?php

namespace App\Tests\Unit;

use App\Entity\TransactionWallet;
use PHPUnit\Framework\TestCase;

class TransactionWalletManagerTest extends TestCase
{
    public function testTransactionCreation(): void
    {
        $t = new TransactionWallet();
        $t->setMontant(150.75);
        $t->setType('INCOME');
        $t->setStatus('ACCEPTED');
        
        $this->assertEquals(150.75, $t->getMontant());
        $this->assertEquals('INCOME', $t->getType());
        $this->assertEquals('ACCEPTED', $t->getStatus());
    }

    public function testNegativeAmount(): void
    {
        $t = new TransactionWallet();
        $t->setMontant(-50.0);
        $this->assertEquals(-50.0, $t->getMontant());
    }
}
