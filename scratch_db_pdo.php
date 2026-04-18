<?php
function calculateSimilarity($vectorA, $vectorB) {
    if (count($vectorA) !== count($vectorB)) return 0.0;
    $dot = 0.0; $normA = 0.0; $normB = 0.0;
    for ($i = 0; $i < count($vectorA); $i++) {
        $dot += $vectorA[$i] * $vectorB[$i];
        $normA += $vectorA[$i] * $vectorA[$i];
        $normB += $vectorB[$i] * $vectorB[$i];
    }
    $norm = sqrt($normA) * sqrt($normB);
    return $norm <= 0 ? 0.0 : $dot / $norm;
}

$pdo = new PDO('mysql:host=127.0.0.1;dbname=finora1', 'root', '');
$stmt = $pdo->query("SELECT u.username, b.face_embedding FROM user u JOIN user_biometrics b ON u.id = b.user_id WHERE b.face_embedding IS NOT NULL AND b.is_active = 1");

$users = $stmt->fetchAll(PDO::FETCH_ASSOC);
$vectors = [];

foreach ($users as $u) {
    $vectors[$u['username']] = json_decode($u['face_embedding'], true);
}

echo "Found " . count($vectors) . " users with face embeddings.\n";

$usernames = array_keys($vectors);
for ($i = 0; $i < count($usernames); $i++) {
    for ($j = $i + 1; $j < count($usernames); $j++) {
        $sim = calculateSimilarity($vectors[$usernames[$i]], $vectors[$usernames[$j]]);
        echo "Similarity between {$usernames[$i]} and {$usernames[$j]}: {$sim}\n";
    }
}
