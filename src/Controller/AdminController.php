<?php

namespace App\Controller;

use App\Repository\UserRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[IsGranted('ROLE_ADMIN')]
class AdminController extends AbstractController
{
    #[Route('/admin/dashboard', name: 'admin_dashboard')]
    public function index(UserRepository $repo): Response
    {
        $users = $repo->findAll();

        $total = count($users);
        $active = 0;
        $blocked = 0;

        foreach ($users as $user) {
            if ($user->isActive()) {
                $active++;
            } else {
                $blocked++;
            }
        }

        return $this->render('admin/dashboard.html.twig', [
            'totalUsers' => $total,
            'activeUsers' => $active,
            'blockedUsers' => $blocked,
        ]);
    }
}