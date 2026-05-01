<?php

namespace App\Tests\Unit;

use App\Entity\Formation;
use App\Entity\Lesson;
use PHPUnit\Framework\TestCase;

class FormationTest extends TestCase
{
    public function testFormationGettersAndSetters(): void
    {
        $formation = new Formation();
        $formation->setTitre('Test Formation');
        $formation->setDescription('This is a test description');
        $formation->setPrix(199.99);
        $formation->setNiveau('Débutant');

        $this->assertEquals('Test Formation', $formation->getTitre());
        $this->assertEquals('This is a test description', $formation->getDescription());
        $this->assertEquals(199.99, $formation->getPrix());
        $this->assertEquals('Débutant', $formation->getNiveau());
    }

    public function testFormationLessonsCollection(): void
    {
        $formation = new Formation();
        $lesson = new Lesson();
        
        $this->assertCount(0, $formation->getLessons());
        
        $formation->addLesson($lesson);
        $this->assertCount(1, $formation->getLessons());
        $this->assertSame($formation, $lesson->getFormation());
        
        $formation->removeLesson($lesson);
        $this->assertCount(0, $formation->getLessons());
        $this->assertNull($lesson->getFormation());
    }
}
