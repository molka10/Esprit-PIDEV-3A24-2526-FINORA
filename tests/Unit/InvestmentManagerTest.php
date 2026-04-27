<?php

namespace App\Tests\Unit;

use App\Entity\Investment;
use PHPUnit\Framework\TestCase;

class InvestmentManagerTest extends TestCase
{
    public function testInvestmentEntity(): void
    {
        $i = new Investment();
        $i->setEstimatedValue('10000.0');
        
        $this->assertEquals('10000.0', $i->getEstimatedValue());
    }
}
