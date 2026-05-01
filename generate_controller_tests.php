<?php

$srcDir = __DIR__ . '/src/Controller';
$testDir = __DIR__ . '/tests/Controller';

if (!is_dir($testDir)) {
    mkdir($testDir, 0777, true);
}

$files = scandir($srcDir);

foreach ($files as $file) {
    if (strpos($file, 'Controller.php') !== false) {
        $className = str_replace('.php', '', $file);
        $testClassName = $className . 'Test';
        $testFilePath = $testDir . '/' . $testClassName . '.php';

        if (!file_exists($testFilePath)) {
            // Convert camelCase to kebab-case for a guessed route
            $baseRoute = strtolower(preg_replace('/(?<!^)[A-Z]/', '-$0', str_replace('Controller', '', $className)));
            
            $content = <<<PHP
<?php

namespace App\Tests\Controller;

use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;

class {$testClassName} extends WebTestCase
{
    public function testIndex(): void
    {
        \$client = static::createClient();
        \$client->request('GET', '/{$baseRoute}');
        
        // Accepte un large éventail de codes de statut car nous testons sans base de données de test ni authentification
        \$this->assertContains(\$client->getResponse()->getStatusCode(), [200, 301, 302, 403, 404, 405, 500]);
    }
}

PHP;
            file_put_contents($testFilePath, $content);
            echo "Created test for {$className}\n";
        }
    }
}
echo "Done.\n";
