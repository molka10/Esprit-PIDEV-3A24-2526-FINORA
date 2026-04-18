<?php
require_once __DIR__ . '/vendor/autoload.php';
use Symfony\Component\Dotenv\Dotenv;
$dotenv = new Dotenv();
$dotenv->loadEnv(__DIR__ . '/.env');
$databaseUrl = $_ENV['DATABASE_URL'];
preg_match('#mysql://([^:@]*)(?::([^@]*))?@([^:/]+)(?::(\d+))?/([^?]+)#', $databaseUrl, $m);
$pdo = new PDO("mysql:host={$m[3]};port={$m[4]};dbname={$m[5]};charset=utf8mb4", $m[1], $m[2] ?? '', [
    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
]);

$statements = [
    // Bourse
    "INSERT IGNORE INTO `bourse` (`id_bourse`, `nom_bourse`, `pays`, `devise`, `statut`, `date_creation`) VALUES (1, 'llll', 'Albanie', 'TND', 'ACTIVE', '2026-02-24 23:01:35')",

    // Actions
    "INSERT IGNORE INTO `action` (`id_action`, `id_bourse`, `symbole`, `nom_entreprise`, `secteur`, `prix_unitaire`, `quantite_disponible`, `statut`, `date_ajout`) VALUES
        (1, 1, 'AAPL', 'apple', 'Technologie', 272.95, 1, 'DISPONIBLE', '2026-02-25 05:22:43'),
        (2, 1, 'MFST', 'micro', 'Finance', 0, 6, 'DISPONIBLE', '2026-02-26 22:24:56'),
        (3, 1, 'GOOGL', 'google', 'Télécommunications', 141.25, 1, 'DISPONIBLE', '2026-02-27 01:45:47')",

    // Transactions bourse
    "INSERT IGNORE INTO `transaction_bourse` (`id_transaction`, `id_action`, `id_user`, `type_transaction`, `quantite`, `prix_unitaire`, `montant_total`, `commission`, `acteur_role`, `acteur_label`, `date_transaction`) VALUES
        (1, 1, 1, 'ACHAT', 1, 272.95, 272.95, 1.36, 'INVESTISSEUR', 'USER_STATIC', '2026-02-27 01:11:17'),
        (2, 1, 1, 'VENTE', 1, 272.95, 272.95, 1.36, 'INVESTISSEUR', 'USER_STATIC', '2026-02-27 01:11:21'),
        (3, 3, 1, 'ACHAT', 1, 141.25, 141.25, 2.19, 'INVESTISSEUR', 'USER_STATIC', '2026-02-27 01:46:37'),
        (4, 1, 1, 'ACHAT', 2, 272.95, 545.9, 8.46, 'INVESTISSEUR', 'USER_STATIC', '2026-02-27 02:30:42'),
        (5, 1, 1, 'VENTE', 1, 272.95, 272.95, 4.23, 'INVESTISSEUR', 'USER_STATIC', '2026-02-27 02:30:48'),
        (6, 1, 1, 'ACHAT', 1, 272.95, 272.95, 4.23, 'INVESTISSEUR', 'USER_STATIC', '2026-02-27 02:41:42'),
        (7, 1, 1, 'VENTE', 1, 272.95, 272.95, 4.23, 'INVESTISSEUR', 'USER_STATIC', '2026-02-27 02:41:55'),
        (8, 3, 1, 'ACHAT', 2, 141.25, 282.5, 4.38, 'INVESTISSEUR', 'USER_STATIC', '2026-02-27 02:53:32'),
        (9, 1, 1, 'ACHAT', 1, 272.95, 272.95, 4.23, 'INVESTISSEUR', 'aziz', '2026-02-28 00:08:30'),
        (10, 1, 1, 'ACHAT', 2, 272.95, 545.9, 8.46, 'INVESTISSEUR', 'aziz', '2026-02-28 23:34:39'),
        (11, 1, 1, 'VENTE', 2, 272.95, 545.9, 8.46, 'INVESTISSEUR', 'aziz', '2026-02-28 23:34:48'),
        (12, 1, 1, 'ACHAT', 7, 272.95, 1910.6499999999999, 29.62, 'INVESTISSEUR', 'aziz', '2026-03-01 04:59:31'),
        (13, 1, 1, 'ACHAT', 1, 272.95, 272.95, 4.23, 'INVESTISSEUR', 'aziz', '2026-03-01 05:02:02'),
        (14, 1, 1, 'VENTE', 1, 272.95, 272.95, 4.23, 'INVESTISSEUR', 'aziz', '2026-03-01 05:02:15')",
];

$pdo->exec('SET FOREIGN_KEY_CHECKS=0');
$ok = 0;
$errors = [];
foreach ($statements as $stmt) {
    try {
        $pdo->exec($stmt);
        $ok++;
    } catch (PDOException $e) {
        $errors[] = substr($stmt, 0, 80) . ' => ' . $e->getMessage();
    }
}
$pdo->exec('SET FOREIGN_KEY_CHECKS=1');

echo "\n✅ $ok statements successful.\n";
if ($errors) {
    foreach ($errors as $err) { echo "  ⚠️  $err\n"; }
} else {
    echo "✅ bourse, action, and transaction_bourse data restored!\n";
}

// Verify
echo "\n📋 Verification:\n";
foreach (['bourse', 'action', 'transaction_bourse'] as $t) {
    $c = $pdo->query("SELECT COUNT(*) FROM `$t`")->fetchColumn();
    echo "  ✅ $t: $c rows\n";
}
