<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class CurrencyController extends AbstractController
{
    #[Route('/switch-currency/{currency}', name: 'app_switch_currency', methods: ['GET'])]
    public function switchCurrency(string $currency, Request $request): Response
    {
        $allowed = ['TND', 'EUR', 'USD'];
        $currency = strtoupper($currency);
        
        if (in_array($currency, $allowed)) {
            $session = $request->getSession();
            $session->set('currency', $currency);
            $session->set('app_currency', $currency);
        }

        $referer = $request->headers->get('referer');
        return $this->redirect($referer ?: $this->generateUrl('app_home'));
    }
}
