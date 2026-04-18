<?php

namespace App\Controller;

use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Annotation\Route;

class FixAdminController extends AbstractController
{
    #[Route('/fix-admin', name: 'app_fix_admin')]
    public function fixAdmin(EntityManagerInterface $em, UserPasswordHasherInterface $hasher): Response
    {
        $email = 'admin@finora.com';
        $user = $em->getRepository(User::class)->findOneBy(['email' => $email]);

        if (!$user) {
            $user = new User();
            $user->setEmail($email);
            $user->setUsername('Admin');
            $user->setCreatedAt(new \DateTime());
        }

        $user->setRole('ADMIN');
        $user->setIsVerified(true);
        $user->setPassword($hasher->hashPassword($user, 'admin'));

        $em->persist($user);
        $em->flush();

        return new Response("✅ Admin @ finora.com has been RESET with password 'admin'. Please try logging in now!");
    }
}
