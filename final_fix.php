<?php
require_once 'vendor/autoload.php';
use App\Kernel;
$kernel = new Kernel('dev', true);
$kernel->boot();
$container = $kernel->getContainer();
$em = $container->get('doctrine.orm.entity_manager');
$hasher = $container->get('security.user_password_hasher');
$user = $em->getRepository(\App\Entity\User::class)->findOneBy(['email' => 'admin@finora.com']);
if ($user) {
    $user->setPassword($hasher->hashPassword($user, 'admin'));
    $user->setIsVerified(true);
    $em->persist($user);
    $em->flush();
    echo "SUCCESS: Password for admin@finora.com set to 'admin'\n";
} else {
    echo "ERROR: User not found\n";
}
