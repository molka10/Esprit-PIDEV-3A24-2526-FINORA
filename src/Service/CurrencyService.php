<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class CurrencyService
{
    private $httpClient;

    public function __construct(HttpClientInterface $httpClient)
    {
        $this->httpClient = $httpClient;
    }

    /**
     * Convert TND to other currencies
     * Since Frankfurter doesn't support TND directly, we'll use a fixed approximate rate
     * or use another API. Let's use 'exchangerate-api.com' (free tier, no key needed for some endpoints)
     * Actually, let's use 'v6.exchangerate-api.com' with a public free key if possible, 
     * or just mock it if we can't get a key.
     * 
     * Better: Let's use 'https://api.exchangerate-api.com/v4/latest/TND'
     */
    public function convertTndTo(float $amount, string $targetCurrency = 'EUR'): float
    {
        try {
            $response = $this->httpClient->request('GET', 'https://api.exchangerate-api.com/v4/latest/TND');
            $data = $response->toArray();
            
            $rate = $data['rates'][$targetCurrency] ?? null;
            
            if ($rate) {
                return $amount * $rate;
            }
        } catch (\Exception $e) {
            // Fallback rates if API fails
            $fallbacks = [
                'EUR' => 0.30,
                'USD' => 0.32,
            ];
            return $amount * ($fallbacks[$targetCurrency] ?? 1);
        }

        return $amount;
    }
}
