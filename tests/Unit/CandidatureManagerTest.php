<?php

namespace App\Tests\Unit;

use App\Entity\Candidature;
use PHPUnit\Framework\TestCase;

class CandidatureManagerTest extends TestCase
{
    public function testCandidatureEntity(): void
    {
        $c = new Candidature();
        $c->setStatut('submitted');
        
        $this->assertEquals('submitted', $c->getStatut());
    }
}
