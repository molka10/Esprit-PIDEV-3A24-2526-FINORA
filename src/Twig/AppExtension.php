<?php

namespace App\Twig;

use App\Service\CurrencyService;
use Twig\Extension\AbstractExtension;
use Twig\TwigFilter;

class AppExtension extends AbstractExtension
{
    public function __construct(private CurrencyService $currencyService) {}

    public function getFilters(): array
    {
        return [
            new TwigFilter('format_currency', [$this, 'formatCurrency']),
            new TwigFilter('convert_currency', [$this, 'convertCurrency']),
        ];
    }

    public function formatCurrency(?float $amount): string
    {
        if ($amount === null || $amount <= 0) {
            return 'Gratuit';
        }
        return $this->currencyService->format($amount);
    }

    public function convertCurrency(?float $amount): string
    {
        if ($amount === null || $amount <= 0) {
            return '0';
        }
        return (string) round($this->currencyService->convert($amount), 2);
    }
}
