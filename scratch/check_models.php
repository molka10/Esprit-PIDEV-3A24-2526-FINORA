<?php

$envFile = __DIR__ . '/.env';
if (!file_exists($envFile)) {
    die(".env file not found\n");
}

$env = parse_ini_file($envFile);
$apiKey = $env['GEMINI_API_KEY'] ?? null;

if (!$apiKey) {
    die("GEMINI_API_KEY not set in .env\n");
}

echo "Diagnostic - Listing models for API Key: " . substr($apiKey, 0, 5) . "...\n\n";

$urls = [
    "v1beta" => "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey",
    "v1" => "https://generativelanguage.googleapis.com/v1/models?key=$apiKey"
];

foreach ($urls as $version => $url) {
    echo "--- Testing $version ---\n";
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    echo "HTTP Code: $httpCode\n";
    if ($httpCode === 200) {
        $json = json_decode($response, true);
        if (isset($json['models'])) {
            foreach ($json['models'] as $m) {
                echo " - " . $m['name'] . " (" . implode(', ', $m['supportedGenerationMethods']) . ")\n";
            }
        } else {
            echo "No models found in response.\n";
            print_r($json);
        }
    } else {
        echo "Error: $response\n";
    }
    echo "\n";
}
