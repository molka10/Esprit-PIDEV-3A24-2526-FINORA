<?php

namespace App\Tests\Unit;

use App\Entity\AppelOffre;
use App\Entity\Candidature;
use PHPUnit\Framework\TestCase;

class AppelOffreTest extends TestCase
{
    public function testAppelOffreGettersAndSetters(): void
    {
        $appelOffre = new AppelOffre();
        $appelOffre->setTitre('Development Project');
        $appelOffre->setDescription('Looking for a Symfony developer');
        $appelOffre->setStatut('published');

        $this->assertEquals('Development Project', $appelOffre->getTitre());
        $this->assertEquals('Looking for a Symfony developer', $appelOffre->getDescription());
        $this->assertEquals('published', $appelOffre->getStatut());
    }

    public function testAppelOffreCandidaturesCollection(): void
    {
        $appelOffre = new AppelOffre();
        $candidature = new Candidature();
        
        $this->assertCount(0, $appelOffre->getCandidatures());
        
        $appelOffre->addCandidature($candidature);
        $this->assertCount(1, $appelOffre->getCandidatures());
        $this->assertSame($appelOffre, $candidature->getAppelOffre());
        
        $appelOffre->removeCandidature($candidature);
        $this->assertCount(0, $appelOffre->getCandidatures());
        $this->assertNull($candidature->getAppelOffre());
    }
}
