<?php

namespace App\Tests\Unit;

use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;

class UserControllerTest extends WebTestCase
{
    public function testIndex(): void
    {
        $client = static::createClient();
        // Just testing that the client boots and we can make a request
        $client->request('GET', '/login');
        
        // We expect either 200 OK or 302 Redirect
        $this->assertContains($client->getResponse()->getStatusCode(), [200, 301, 302]);
    }
}
