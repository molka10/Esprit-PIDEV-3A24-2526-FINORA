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

    public function getRate(string $from, string $to): float
    {
        if ($from === $to || ($from === 'TND' && $to === 'DT') || ($from === 'DT' && $to === 'TND')) {
            return 1.0;
        }

        $from = $from === 'DT' ? 'TND' : $from;
        $to = $to === 'DT' ? 'TND' : $to;

        try {
            $response = $this->client->request(
                'GET', 
                "https://open.er-api.com/v6/latest/{$from}"
            );

            $data = $response->toArray();
            return $data['rates'][$to] ?? 1.0;
        } catch (Throwable $e) {
            return 1.0; // Fallback rate
        }
    }
}
