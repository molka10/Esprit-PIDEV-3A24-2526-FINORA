<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class WalletSystemController extends AbstractController
{
    #[Route('/wallet/system', name: 'app_wallet_system')]
    public function index(): Response
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }
        return $this->render('wallet/system.html.twig', [
            'userId' => $user->getId(),
        ]);
    }
}
