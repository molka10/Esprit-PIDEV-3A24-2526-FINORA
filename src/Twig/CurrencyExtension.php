<?php

namespace App\Twig;

use App\Service\CurrencyConverterService;
use Symfony\Component\HttpFoundation\RequestStack;
use Twig\Extension\AbstractExtension;
use Twig\TwigFilter;
use Twig\TwigFunction;

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
            new TwigFilter('convert_currency', [$this, 'convertCurrency']),
        ];
    }

    public function getFunctions(): array
    {
        return [
            new TwigFunction('get_rate', [$this, 'getRate']),
        ];
    }

    public function getRate(string $from, string $to): float
    {
        return $this->converter->convert(1.0, $from, $to);
    }

    public function convertCurrency($amount): float
    {
        $amount = (float) $amount;
        $request = $this->requestStack->getCurrentRequest();
        $currency = 'TND';
        if ($request && $request->hasSession()) {
            $currency = $request->getSession()->get('app_currency', 'TND');
        }
        return $this->converter->convert($amount, 'TND', $currency);
    }

    public function formatCurrency($amount): string
    {
        $amount = (float) $amount;
        $request = $this->requestStack->getCurrentRequest();
        
        $currency = 'TND';
        if ($request && $request->hasSession()) {
            $currency = $request->getSession()->get('app_currency', 'TND');
        }

        $converted = $this->converter->convert($amount, 'TND', $currency);

        $symbols = [
            'TND' => 'TND',
            'EUR' => '€',
            'USD' => '$'
        ];
        
        $symbol = $symbols[$currency] ?? $currency;
        $decimals = ($currency === 'TND') ? 2 : 2; // Always show 2 decimals for consistency as per user screen
        $formatted = number_format($converted, $decimals, '.', ' ');

        if ($currency === 'TND') {
            return $formatted . ' ' . $symbol;
        }
        return $symbol . ' ' . $formatted;
    }
}
