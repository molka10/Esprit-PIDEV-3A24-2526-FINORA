<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class DashboardController extends AbstractController
{
    #[Route('/dashboard', name: 'app_dashboard')]
    public function index(): Response
    {
        $user = $this->getUser();

        // 🔒 Not logged → login
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        // 🔴 ADMIN → admin dashboard
        if (in_array('ROLE_ADMIN', $user->getRoles())) {
            return $this->redirectToRoute('app_admin');
        }

        // 🟣 ENTREPRISE
        if (in_array('ROLE_ENTREPRISE', $user->getRoles())) {
            return $this->render('front/index.html.twig', [
                'content' => 'dashboard/entreprise.html.twig'
            ]);
        }

        // 🔵 USER
        return $this->render('front/index.html.twig', [
            'content' => 'dashboard/user.html.twig'
        ]);
    }
}