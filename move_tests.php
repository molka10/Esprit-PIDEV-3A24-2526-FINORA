<?php

$controllerDir = __DIR__ . '/tests/Controller';
$unitDir = __DIR__ . '/tests/Unit';

if (is_dir($controllerDir)) {
    $files = scandir($controllerDir);
    foreach ($files as $file) {
        if (strpos($file, '.php') !== false) {
            $content = file_get_contents($controllerDir . '/' . $file);
            $content = str_replace('namespace App\Tests\Controller;', 'namespace App\Tests\Unit;', $content);
            file_put_contents($unitDir . '/' . $file, $content);
            unlink($controllerDir . '/' . $file);
        }
    }
    rmdir($controllerDir);
    echo "Moved files successfully.\n";
} else {
    echo "Directory not found.\n";
}
