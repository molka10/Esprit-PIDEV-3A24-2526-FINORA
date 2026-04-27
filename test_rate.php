<?php
require_once __DIR__ . '/vendor/autoload.php';

use App\Service\CurrencyConverterService;
use Symfony\Component\HttpClient\HttpClient;

$client = HttpClient::create();
$converter = new CurrencyConverterService($client);

echo "TND to EUR: " . $converter->getRate('TND', 'EUR') . "\n";
echo "9156.15 TND to EUR: " . $converter->convert(9156.15, 'TND', 'EUR') . "\n";
