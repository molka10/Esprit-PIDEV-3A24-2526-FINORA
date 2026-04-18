<?php

use App\Entity\QuizResult;
use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;

require __DIR__ . '/vendor/autoload.php';

(new \Symfony\Component\Dotenv\Dotenv())->bootEnv(__DIR__ . '/.env');

$kernel = new \App\Kernel('dev', true);
$kernel->boot();
$container = $kernel->getContainer();
$entityManager = $container->get('doctrine.orm.entity_manager');

$results = $entityManager->getRepository(QuizResult::class)->findBy(['user' => null]);
$count = 0;

echo "Syncing " . count($results) . " records...\n";

foreach ($results as $result) {
    if ($name = $result->getStudentName()) {
        $user = $entityManager->getRepository(User::class)->findOneBy(['username' => $name]);
        if ($user) {
            $result->setUser($user);
            $count++;
        }
    }
}

$entityManager->flush();
echo "Successfully linked $count quiz records to their users.\n";
