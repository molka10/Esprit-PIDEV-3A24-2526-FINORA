<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[IsGranted('ROLE_USER')]
final class InvestisseurController extends AbstractController
{
    #[Route('/investisseur', name: 'investisseur_dashboard')]
    public function index(): Response
    {
        return $this->render('investisseur/index.html.twig');
    }
}