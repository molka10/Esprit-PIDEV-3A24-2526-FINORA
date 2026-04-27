<?php

require __DIR__ . '/vendor/autoload.php';

use App\Kernel;
use Symfony\Component\HttpFoundation\Request;

$kernel = new Kernel('dev', true);
$kernel->boot();
$container = $kernel->getContainer();

$aiService = $container->get('App\Service\AiAssistantService');

try {
    $response = $aiService->processUserMessage("je veux investir dans l'immobilier 10000", "fr");
    echo json_encode($response, JSON_PRETTY_PRINT);
} catch (\Exception $e) {
    echo "ERROR: " . $e->getMessage() . "\n" . $e->getTraceAsString();
}
