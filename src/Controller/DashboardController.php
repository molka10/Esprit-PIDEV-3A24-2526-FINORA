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

        // 🔴 Role-based redirection logic simplified
        // Admin and User now share the same premium landing dashboard


        // 🟣 ENTREPRISE
        if (in_array('ROLE_ENTREPRISE', $user->getRoles())) {
            return $this->render('dashboard/entreprise.html.twig');
        }

        // 🔵 USER
        return $this->render('dashboard/user.html.twig');
    }
}