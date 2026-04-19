<?php

namespace App\Twig;

use App\Service\CurrencyConverterService;
use Symfony\Component\HttpFoundation\RequestStack;
use Twig\Extension\AbstractExtension;
use Twig\TwigFilter;

class CurrencyExtension extends AbstractExtension
{
    public function __construct(
        private CurrencyConverterService $converter,
        private RequestStack $requestStack
    ) {
    }

    public function getFilters(): array
    {
        return [
            new TwigFilter('format_currency', [$this, 'formatCurrency']),
        ];
    }

    public function formatCurrency($amount): string
    {
        $amount = (float) $amount;
        $request = $this->requestStack->getCurrentRequest();
        
        $currency = 'TND';
        if ($request && $request->hasSession()) {
            $currency = $request->getSession()->get('currency', 'TND');
        }

        $converted = $this->converter->convert($amount, $currency);

        $symbols = [
            'TND' => 'TND',
            'EUR' => '€',
            'USD' => '$'
        ];
        
        $symbol = $symbols[$currency] ?? $currency;
        $decimals = ($currency === 'TND') ? 0 : 2;
        $formatted = number_format($converted, $decimals, ',', ' ');

        return $formatted . ' ' . $symbol;
    }
}
