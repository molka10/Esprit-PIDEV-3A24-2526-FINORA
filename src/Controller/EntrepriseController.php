<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[IsGranted('ROLE_ENTREPRISE')]
final class EntrepriseController extends AbstractController
{
    #[Route('/entreprise', name: 'entreprise_dashboard')]
    public function index(): Response
    {
        return $this->render('entreprise/index.html.twig');
    }
}