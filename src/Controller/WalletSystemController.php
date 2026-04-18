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
        // For convenience in the UI demo, we'll pass the default userId
        return $this->render('wallet/system.html.twig', [
            'userId' => 6,
        ]);
    }
}
