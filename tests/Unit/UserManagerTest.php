<?php

namespace App\Tests\Unit;

use App\Entity\User;
use PHPUnit\Framework\TestCase;

class UserManagerTest extends TestCase
{
    public function testUserEntity(): void
    {
        $user = new User();
        $user->setEmail('test@finora.tn');
        $user->setUsername('Admin');
        
        $this->assertEquals('test@finora.tn', $user->getEmail());
        $this->assertEquals('Admin', $user->getUsername());
    }

    public function testUserRoles(): void
    {
        $user = new User();
        $this->assertContains('ROLE_USER', $user->getRoles());
    }
}
