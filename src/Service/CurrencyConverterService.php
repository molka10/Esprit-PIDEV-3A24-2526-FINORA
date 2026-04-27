<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;
use Throwable;

class CurrencyConverterService
{
    private HttpClientInterface $client;

    public function __construct(HttpClientInterface $client)
    {
        $this->client = $client;
    }

    /**
     * Converts an amount from one currency to another using ExchangeRate-API.
     */
    public function convert(float $amount, string $from = 'TND', string $to = 'USD'): float
    {
        return $amount * $this->getRate($from, $to);
    }

    private array $rates = [
        'TND' => [
            'EUR' => 0.295,
            'USD' => 0.321,
            'TND' => 1.0
        ],
        'EUR' => [
            'TND' => 3.39,
            'USD' => 1.09,
            'EUR' => 1.0
        ],
        'USD' => [
            'TND' => 3.12,
            'EUR' => 0.92,
            'USD' => 1.0
        ]
    ];

    public function getRate(string $from, string $to): float
    {
        $from = strtoupper($from === 'DT' ? 'TND' : $from);
        $to = strtoupper($to === 'DT' ? 'TND' : $to);

        if ($from === $to) return 1.0;

        // 1. Try static table first for absolute reliability
        if (isset($this->rates[$from][$to])) {
            return $this->rates[$from][$to];
        }

        // 2. Fallback to API for other currencies
        try {
            $response = $this->client->request('GET', "https://open.er-api.com/v6/latest/USD");
            $data = $response->toArray();
            $apiRates = $data['rates'] ?? [];
            
            $fromRate = $apiRates[$from] ?? 1.0;
            $toRate = $apiRates[$to] ?? 1.0;

            if ($fromRate == 0) return 1.0;
            return $toRate / $fromRate;
        } catch (Throwable $e) {
            return 1.0;
        }
    }
}
