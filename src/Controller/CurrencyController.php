<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class CurrencyController extends AbstractController
{
    #[Route('/switch-currency/{currency}', name: 'app_switch_currency')]
    public function switchCurrency(string $currency, Request $request): Response
    {
        $validCurrencies = ['TND', 'EUR', 'USD'];
        
        $currency = strtoupper($currency);
        if (in_array($currency, $validCurrencies)) {
            $request->getSession()->set('currency', $currency);
            
            $this->addFlash('success', 'Devise mise à jour en ' . $currency);
        } else {
            $this->addFlash('error', 'Devise non valide.');
        }

        // Redirige l'utilisateur vers la page d'où il vient, ou vers l'accueil par défaut
        $referer = $request->headers->get('referer', $this->generateUrl('app_home'));
        
        return $this->redirect($referer);
    }
}
