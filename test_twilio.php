<?php
// Test basic connectivity + Twilio with verbose SSL info
echo "=== cURL Info ===" . PHP_EOL;
echo "cURL version: " . curl_version()['version'] . PHP_EOL;
echo "SSL version: " . curl_version()['ssl_version'] . PHP_EOL;

// Test 1: Basic HTTPS connectivity
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, "https://api.twilio.com");
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_TIMEOUT, 15);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$error = curl_error($ch);
curl_close($ch);

echo PHP_EOL . "=== Connection Test ===" . PHP_EOL;
echo "HTTP Status: {$httpCode}" . PHP_EOL;
if ($error) {
    echo "cURL Error: {$error}" . PHP_EOL;
    
    // Retry without SSL verify
    echo PHP_EOL . "=== Retrying without SSL verify ===" . PHP_EOL;
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, "https://api.twilio.com/2010-04-01/Accounts/AC957348af993befcc41902b959328028d.json");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 15);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_USERPWD, "AC957348af993befcc41902b959328028d:13cb64345fdf1e2a256d14962d238ef1");
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $error2 = curl_error($ch);
    curl_close($ch);
    
    echo "HTTP Status: {$httpCode}" . PHP_EOL;
    if ($error2) echo "cURL Error: {$error2}" . PHP_EOL;
    if ($response) {
        $data = json_decode($response, true);
        if ($data && isset($data['status'])) {
            echo "Account Status: {$data['status']}" . PHP_EOL;
        } elseif ($data && isset($data['message'])) {
            echo "API Error: {$data['message']}" . PHP_EOL;
        } else {
            echo "Response: " . substr($response, 0, 300) . PHP_EOL;
        }
    }
} else {
    echo "Connection OK!" . PHP_EOL;
    
    // Now test credentials
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, "https://api.twilio.com/2010-04-01/Accounts/AC957348af993befcc41902b959328028d.json");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_USERPWD, "AC957348af993befcc41902b959328028d:13cb64345fdf1e2a256d14962d238ef1");
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    
    echo PHP_EOL . "=== Twilio Credentials ===" . PHP_EOL;
    echo "HTTP Status: {$httpCode}" . PHP_EOL;
    $data = json_decode($response, true);
    if ($data && isset($data['status'])) {
        echo "Account Status: {$data['status']}" . PHP_EOL;
        echo "Friendly Name: " . ($data['friendly_name'] ?? 'N/A') . PHP_EOL;
        echo "=> SMS IS READY" . PHP_EOL;
    } elseif ($data && isset($data['message'])) {
        echo "ERROR: {$data['message']}" . PHP_EOL;
    }
}
