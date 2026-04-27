<?php

namespace App\Tests\Unit;

use App\Entity\QuizResult;
use PHPUnit\Framework\TestCase;

class QuizResultManagerTest extends TestCase
{
    public function testQuizScore(): void
    {
        $q = new QuizResult();
        $q->setScore(90);
        
        $this->assertEquals(90, $q->getScore());
    }
}
