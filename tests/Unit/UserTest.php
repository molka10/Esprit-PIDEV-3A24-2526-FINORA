<?php

namespace App\Tests\Unit;

use App\Entity\User;
use App\Entity\Formation;
use PHPUnit\Framework\TestCase;

class UserTest extends TestCase
{
    public function testUserGettersAndSetters(): void
    {
        $user = new User();
        $user->setUsername('testuser');
        $user->setEmail('test@example.com');
        $user->setRole('admin');
        $user->setBalance(150.50);

        $this->assertEquals('testuser', $user->getUsername());
        $this->assertEquals('test@example.com', $user->getEmail());
        $this->assertEquals('ADMIN', $user->getRole());
        $this->assertEquals(150.50, $user->getBalance());
        $this->assertTrue(in_array('ROLE_ADMIN', $user->getRoles()));
    }

    public function testUserWishlist(): void
    {
        $user = new User();
        $formation = new Formation();
        
        $this->assertCount(0, $user->getWishlist());
        
        $user->addToWishlist($formation);
        $this->assertCount(1, $user->getWishlist());
        $this->assertTrue($user->getWishlist()->contains($formation));
        
        $user->removeFromWishlist($formation);
        $this->assertCount(0, $user->getWishlist());
    }

    public function testUserPurchasedFormations(): void
    {
        $user = new User();
        $formation = new Formation();
        
        $this->assertCount(0, $user->getPurchasedFormations());
        
        $user->addPurchasedFormation($formation);
        $this->assertCount(1, $user->getPurchasedFormations());
        $this->assertTrue($user->getPurchasedFormations()->contains($formation));
        
        $user->removePurchasedFormation($formation);
        $this->assertCount(0, $user->getPurchasedFormations());
    }
}
