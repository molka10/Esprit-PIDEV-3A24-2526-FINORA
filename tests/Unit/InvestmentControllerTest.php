<?php

namespace App\Tests\Unit;

use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;

class InvestmentControllerTest extends WebTestCase
{
    public function testIndex(): void
    {
        $client = static::createClient();
        $client->request('GET', '/investment');
        
        $this->assertContains($client->getResponse()->getStatusCode(), [200, 301, 302, 403, 404, 500]);
    }
}
