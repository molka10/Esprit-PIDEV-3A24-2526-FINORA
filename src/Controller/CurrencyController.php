<?php

namespace App\Controller;

use App\Service\CurrencyService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class CurrencyController extends AbstractController
{
    #[Route('/currency/switch/{code}', name: 'app_currency_switch')]
    public function switch(string $code, CurrencyService $currencyService, Request $request): Response
    {
        $currencyService->setSelectedCurrency($code);
        
        if ($request->isXmlHttpRequest()) {
            return $this->json(['success' => true, 'currency' => $code]);
        }

        $referer = $request->headers->get('referer');
        if ($referer) {
            return $this->redirect($referer);
        }

        return $this->redirectToRoute('app_formations');
    }
}
