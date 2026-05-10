<?php

namespace App\Tests\Unit;

use App\Entity\AppelOffre;
use App\Entity\Candidature;
use App\Entity\Rating;
use PHPUnit\Framework\TestCase;

class AppelOffreTest extends TestCase
{
    // -------------------------------------------------------
    // Test 1: Basic getters and setters
    // -------------------------------------------------------
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

    // -------------------------------------------------------
    // Test 2: Candidatures collection (add & remove)
    // -------------------------------------------------------
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

    // -------------------------------------------------------
    // Test 3: Budget min/max logic
    // -------------------------------------------------------
    public function testAppelOffreBudget(): void
    {
        $appelOffre = new AppelOffre();
        $appelOffre->setBudgetMin('1000.00');
        $appelOffre->setBudgetMax('5000.00');
        $appelOffre->setDevise('TND');

        $this->assertEquals('1000.00', $appelOffre->getBudgetMin());
        $this->assertEquals('5000.00', $appelOffre->getBudgetMax());
        $this->assertEquals('TND', $appelOffre->getDevise());
        // Budget max must be greater than budget min
        $this->assertGreaterThan(
            (float) $appelOffre->getBudgetMin(),
            (float) $appelOffre->getBudgetMax()
        );
    }

    // -------------------------------------------------------
    // Test 4: Ratings collection (add & remove)
    // -------------------------------------------------------
    public function testAppelOffreRatingsCollection(): void
    {
        $appelOffre = new AppelOffre();
        $rating = new Rating();

        $this->assertCount(0, $appelOffre->getRatings());

        $appelOffre->addRating($rating);
        $this->assertCount(1, $appelOffre->getRatings());
        $this->assertSame($appelOffre, $rating->getAppelOffre());

        $appelOffre->removeRating($rating);
        $this->assertCount(0, $appelOffre->getRatings());
        $this->assertNull($rating->getAppelOffre());
    }

    // -------------------------------------------------------
    // Test 5: No duplicate candidature added
    // -------------------------------------------------------
    public function testNoDuplicateCandidature(): void
    {
        $appelOffre = new AppelOffre();
        $candidature = new Candidature();

        $appelOffre->addCandidature($candidature);
        $appelOffre->addCandidature($candidature); // add the same one again

        $this->assertCount(1, $appelOffre->getCandidatures());
    }

    // -------------------------------------------------------
    // Test 6: Type and required criteria fields
    // -------------------------------------------------------
    public function testAppelOffreTypeAndCriteria(): void
    {
        $appelOffre = new AppelOffre();
        $appelOffre->setType('partenariat');
        $appelOffre->setRequiredCriteria('Experience in PHP, Symfony, MySQL');

        $this->assertEquals('partenariat', $appelOffre->getType());
        $this->assertEquals('Experience in PHP, Symfony, MySQL', $appelOffre->getRequiredCriteria());
    }

    // -------------------------------------------------------
    // Test 7: Date limite setter and getter
    // -------------------------------------------------------
    public function testAppelOffreDateLimite(): void
    {
        $appelOffre = new AppelOffre();
        $date = new \DateTime('+30 days');
        $appelOffre->setDateLimite($date);

        $this->assertSame($date, $appelOffre->getDateLimite());
        $this->assertGreaterThan(new \DateTime('today'), $appelOffre->getDateLimite());
    }

    // -------------------------------------------------------
    // Test 8: Candidature default status is 'submitted'
    // -------------------------------------------------------
    public function testCandidatureDefaultStatus(): void
    {
        $candidature = new Candidature();
        $this->assertEquals('submitted', $candidature->getStatut());
    }

    // -------------------------------------------------------
    // Test 9: Candidature AI score and analysis
    // -------------------------------------------------------
    public function testCandidatureAiScoring(): void
    {
        $candidature = new Candidature();
        $candidature->setAiScore(85);
        $candidature->setAiAnalysis('Strong profile matching all required criteria.');

        $this->assertEquals(85, $candidature->getAiScore());
        $this->assertEquals('Strong profile matching all required criteria.', $candidature->getAiAnalysis());
        // A score above 70 is considered a good match
        $this->assertGreaterThan(70, $candidature->getAiScore());
    }

    // -------------------------------------------------------
    // Test 10: Candidature status transitions
    // -------------------------------------------------------
    public function testCandidatureStatusTransitions(): void
    {
        $candidature = new Candidature();

        // Start as submitted
        $this->assertEquals('submitted', $candidature->getStatut());

        // Transition to accepted
        $candidature->setStatut('accepted');
        $this->assertEquals('accepted', $candidature->getStatut());

        // Transition to rejected
        $candidature->setStatut('rejected');
        $this->assertEquals('rejected', $candidature->getStatut());
    }
}
