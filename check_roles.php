<?php
require_once __DIR__ . '/vendor/autoload.php';
use Symfony\Component\Dotenv\Dotenv;

$dotenv = new Dotenv();
$dotenv->loadEnv(__DIR__ . '/.env');

$databaseUrl = $_ENV['DATABASE_URL'];
preg_match('#mysql://([^:@]*)(?::([^@]*))?@([^:/]+)(?::(\d+))?/([^?]+)#', $databaseUrl, $m);

try {
    $pdo = new PDO("mysql:host={$m[3]};port={$m[4]};dbname={$m[5]};charset=utf8mb4", $m[1], $m[2] ?? '', [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
    ]);
    
    $email = 'molkaomrani1412@gmail.com';
    $stmt = $pdo->prepare("SELECT id, email, role FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if ($user) {
        echo "👤 User found: " . $user['email'] . "\n";
        echo "🔑 Role: " . $user['role'] . "\n";
    } else {
        echo "❌ User '$email' not found.\n";
    }

} catch (PDOException $e) {
    die("❌ Connection failed: " . $e->getMessage() . "\n");
}
