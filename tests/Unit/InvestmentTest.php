<?php

namespace App\Tests\Unit;

use App\Entity\Investment;
use App\Entity\InvestmentManagement;
use PHPUnit\Framework\TestCase;

class InvestmentTest extends TestCase
{
    public function testInvestmentGettersAndSetters(): void
    {
        $investment = new Investment();
        $investment->setEstimatedValue('1500.50');
        $investment->setStatus('ACTIVE');

        $this->assertEquals('1500.50', $investment->getEstimatedValue());
        $this->assertEquals('ACTIVE', $investment->getStatus());
    }

    public function testInvestmentComputedMethods(): void
    {
        $investment = new Investment();
        
        $investment->setRiskLevel('LOW');
        $this->assertEquals(7.5, $investment->getAnnualReturn());
        $this->assertEquals(36, $investment->getDurationMonths());

        $investment->setRiskLevel('HIGH');
        $this->assertEquals(12.0, $investment->getAnnualReturn());
        $this->assertEquals(12, $investment->getDurationMonths());
    }

    public function testInvestmentManagementCollection(): void
    {
        $investment = new Investment();
        $management = new InvestmentManagement();
        
        $this->assertCount(0, $investment->getManagements());
        
        $investment->addManagement($management);
        $this->assertCount(1, $investment->getManagements());
        
        $investment->removeManagement($management);
        $this->assertCount(0, $investment->getManagements());
    }
}
