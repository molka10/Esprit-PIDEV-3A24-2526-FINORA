<?php

try {
    $pdo = new PDO(
        "mysql:host=127.0.0.1;port=3306;dbname=finora;charset=utf8mb4",
        "root",
        ""
    );

    echo "OK";
} catch (Exception $e) {
    echo "ERROR: " . $e->getMessage();
}