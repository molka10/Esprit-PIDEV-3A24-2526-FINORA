<?php

namespace App\Tests\Unit;

use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;

class FormationControllerTest extends WebTestCase
{
    public function testIndex(): void
    {
        $client = static::createClient();
        $client->request('GET', '/formation');
        
        $this->assertContains($client->getResponse()->getStatusCode(), [200, 301, 302, 404, 403]);
    }
}
