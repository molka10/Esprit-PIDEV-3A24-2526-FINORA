<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class HomeController extends AbstractController
{
    #[Route('/', name: 'app_home')]
    public function index(): Response
    {
        $user = $this->getUser();

        // If logged → go to dashboard logic
        if ($user) {
            return $this->redirectToRoute('app_dashboard');
        }

        // Visitor → landing page
        return $this->render('front/index.html.twig');
    }
}