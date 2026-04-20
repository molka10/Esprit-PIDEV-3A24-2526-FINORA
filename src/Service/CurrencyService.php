<?php

namespace App\Service;

use Symfony\Component\HttpFoundation\RequestStack;

class CurrencyService
{
    private const BASE_CURRENCY = 'TND';
    private const RATES = [
        'TND' => 1.0,
        'USD' => 0.32,
        'EUR' => 0.30,
    ];

    private const SYMBOLS = [
        'TND' => 'TND',
        'USD' => '$',
        'EUR' => '€',
    ];

    public function __construct(private RequestStack $requestStack) {}

    public function getSelectedCurrency(): string
    {
        $session = $this->requestStack->getSession();
        return $session->get('app_currency', self::BASE_CURRENCY);
    }

    public function setSelectedCurrency(string $code): void
    {
        if (isset(self::RATES[$code])) {
            $session = $this->requestStack->getSession();
            $session->set('app_currency', $code);
        }
    }

    public function convert(float $amount, ?string $targetCode = null): float
    {
        $targetCode = $targetCode ?? $this->getSelectedCurrency();
        $rate = self::RATES[$targetCode] ?? 1.0;
        return $amount * $rate;
    }

    public function format(float $amount, ?string $targetCode = null): string
    {
        $targetCode = $targetCode ?? $this->getSelectedCurrency();
        $converted = $this->convert($amount, $targetCode);
        $symbol = self::SYMBOLS[$targetCode] ?? $targetCode;

        if ($targetCode === 'TND') {
            return number_format($converted, 2, '.', '') . ' ' . $symbol;
        }

        return $symbol . number_format($converted, 2, '.', '');
    }

    public function getAvailableCurrencies(): array
    {
        return array_keys(self::RATES);
    }

    /**
     * Specialized conversion for Tender Module (from TND)
     */
    public function convertTndTo(float $amount, string $targetCurrency = 'EUR'): float
    {
        // Try internal rates first for consistency with existing modules
        if (isset(self::RATES[$targetCurrency])) {
            return $amount * self::RATES[$targetCurrency];
        }

        // Fallback for non-session based calls or external needs
        $fallbacks = [
            'EUR' => 0.30,
            'USD' => 0.32,
        ];
        return $amount * ($fallbacks[$targetCurrency] ?? 1);
    }
}
