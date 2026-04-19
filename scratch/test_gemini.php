<?php
require 'vendor/autoload.php';

use Symfony\Component\HttpClient\HttpClient;

$apiKey = 'AIzaSyCerUCk2dLJ_nYr2bw5OtgCWJykwe2ByFA';
$client = HttpClient::create();

try {
    $response = $client->request('GET', 'https://generativelanguage.googleapis.com/v1beta/models?key=' . $apiKey);
    echo "Response Code: " . $response->getStatusCode() . "\n";
    print_r($response->toArray());
} catch (\Exception $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
