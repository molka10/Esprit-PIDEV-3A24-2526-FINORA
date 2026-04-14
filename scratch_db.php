<?php
require __DIR__.'/vendor/autoload.php';

use App\Kernel;
use Symfony\Component\Dotenv\Dotenv;
use App\Service\FaceIdService;
use App\Repository\UserRepository;

(new Dotenv())->bootEnv(__DIR__.'/.env');

$kernel = new Kernel('dev', true);
$kernel->boot();

$container = $kernel->getContainer();
$userRepository = $container->get(UserRepository::class);
$faceIdService = $container->get(FaceIdService::class);

$users = $userRepository->findAll();
$vectors = [];

foreach ($users as $user) {
    if ($user->getUserBiometrics() && $user->getUserBiometrics()->getFaceEmbedding()) {
        $vectors[$user->getUsername()] = json_decode($user->getUserBiometrics()->getFaceEmbedding(), true);
    }
}

echo "Found " . count($vectors) . " users with face embeddings.\n";

$usernames = array_keys($vectors);
for ($i = 0; $i < count($usernames); $i++) {
    for ($j = $i + 1; $j < count($usernames); $j++) {
        $sim = $faceIdService->calculateSimilarity($vectors[$usernames[$i]], $vectors[$usernames[$j]]);
        echo "Similarity between {$usernames[$i]} and {$usernames[$j]}: {$sim}\n";
    }
}
