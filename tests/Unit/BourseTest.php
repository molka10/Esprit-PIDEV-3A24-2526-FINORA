<?php

namespace App\Tests\Unit;

use App\Entity\Bourse;
use PHPUnit\Framework\TestCase;

class BourseTest extends TestCase
{
    public function testBourseGettersAndSetters(): void
    {
        $bourse = new Bourse();
        $bourse->setNomBourse('NASDAQ');
        $bourse->setPays('USA');

        $this->assertEquals('NASDAQ', $bourse->getNomBourse());
        $this->assertEquals('USA', $bourse->getPays());
    }

    public function testBourseDefaultsAndStatut(): void
    {
        $bourse = new Bourse();
        
        // Test defaults
        $this->assertEquals('ACTIVE', $bourse->getStatut());
        $this->assertInstanceOf(\DateTimeInterface::class, $bourse->getDateCreation());

        // Test custom status
        $bourse->setStatut('INACTIVE');
        $this->assertEquals('INACTIVE', $bourse->getStatut());

        // Test devise
        $bourse->setDevise('usd'); // should capitalize
        $this->assertEquals('USD', $bourse->getDevise());
    }
}
