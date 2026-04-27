<?php

namespace App\Tests\Unit;

use App\Entity\TransactionBourse;
use PHPUnit\Framework\TestCase;

class TransactionBourseManagerTest extends TestCase
{
    public function testBourseTrade(): void
    {
        $t = new TransactionBourse();
        $t->setQuantite(5);
        $t->setPrixUnitaire(200.0);
        $t->setTypeTransaction('ACHAT');
        
        $this->assertEquals(5, $t->getQuantite());
        $this->assertEquals(200.0, $t->getPrixUnitaire());
        $this->assertEquals('ACHAT', $t->getTypeTransaction());
    }
}
