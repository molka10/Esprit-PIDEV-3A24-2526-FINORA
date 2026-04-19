<?php
use PDO;
require_once __DIR__ . '/vendor/autoload.php';
use Symfony\Component\Dotenv\Dotenv;

$dotenv = new Dotenv();
$dotenv->loadEnv(__DIR__.'/.env');

$dbUrl = $_ENV['DATABASE_URL'];
preg_match('/mysql:\/\/([^:]+):([^@]*)@([^:]+):(\d+)\/([^?]+)/', $dbUrl, $matches);
$user = $matches[1] ?? 'root';
$pass = $matches[2] ?? '';
$host = $matches[3] ?? '127.0.0.1';
$port = $matches[4] ?? '3306';
$db = $matches[5] ?? 'finora_bourse';

try {
    $pdo = new PDO("mysql:host=$host;port=$port;dbname=$db", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $pdo->exec("DROP TABLE IF EXISTS investment_wishlist");

    $sql1 = "CREATE TABLE investment_wishlist (
        id INT AUTO_INCREMENT NOT NULL, 
        user_id INT NOT NULL, 
        investment_id INT NOT NULL, 
        created_at DATETIME NOT NULL, 
        INDEX IDX_FAVORITE_USER (user_id), 
        INDEX IDX_FAVORITE_INVEST (investment_id), 
        UNIQUE INDEX user_investment_unique (user_id, investment_id), 
        PRIMARY KEY(id)
    ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ENGINE = InnoDB";
    $pdo->exec($sql1);
    echo "Table investment_wishlist created.\n";

    // Table is 'users', not 'user'
    $sql2 = "ALTER TABLE investment_wishlist ADD CONSTRAINT FK_FAVORITE_USER FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE";
    $pdo->exec($sql2);
    echo "Constraint User added.\n";

    // Table is 'investment'
    $sql3 = "ALTER TABLE investment_wishlist ADD CONSTRAINT FK_FAVORITE_INVEST FOREIGN KEY (investment_id) REFERENCES investment (investment_id) ON DELETE CASCADE";
    $pdo->exec($sql3);
    echo "Constraint Investment added.\n";
    
} catch (\PDOException $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
