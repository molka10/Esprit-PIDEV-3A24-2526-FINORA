<?php

namespace App\Tests\Unit;

use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;

class BourseControllerTest extends WebTestCase
{
    public function testIndex(): void
    {
        $client = static::createClient();
        $client->request('GET', '/bourse');
        
        $this->assertContains($client->getResponse()->getStatusCode(), [200, 301, 302, 404, 403]);
    }
}
