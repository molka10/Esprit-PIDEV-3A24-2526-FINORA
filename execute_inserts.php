<?php
$pdo = new PDO('mysql:host=127.0.0.1;dbname=finora', 'root', '');
$pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

$sql = file_get_contents(__DIR__ . '/wallet_inserts.sql');

try {
    $pdo->exec($sql);
    echo "SQL execution completed successfully!\n";
} catch (PDOException $e) {
    echo "Query failed: " . $e->getMessage() . "\n";
    exit(1);
}
