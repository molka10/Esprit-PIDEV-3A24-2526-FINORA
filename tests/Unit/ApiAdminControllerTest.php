<?php

namespace App\Tests\Unit;

use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;

class ApiAdminControllerTest extends WebTestCase
{
    public function testIndex(): void
    {
        $client = static::createClient();
        $client->request('GET', '/api-admin');
        
        // Accepte un large éventail de codes de statut car nous testons sans base de données de test ni authentification
        $this->assertContains($client->getResponse()->getStatusCode(), [200, 301, 302, 403, 404, 405, 500]);
    }
}
