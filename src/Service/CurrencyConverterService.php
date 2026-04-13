<?php

namespace App\Service;

use Psr\Log\LoggerInterface;
use Symfony\Contracts\Cache\CacheInterface;
use Symfony\Contracts\Cache\ItemInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

class CurrencyConverterService
{
    // Utilisation d'une API gratuite (sans clé) pour TND -> EUR/USD
    private const API_URL = 'https://open.er-api.com/v6/latest/TND';

    public function __construct(
        private HttpClientInterface $client,
        private CacheInterface $cache,
        private LoggerInterface $logger
    ) {
    }

    public function getRates(): array
    {
        return $this->cache->get('currency_rates_tnd', function (ItemInterface $item) {
            $item->expiresAfter(43200); // Cache valide pendant 12 heures

            try {
                $response = $this->client->request('GET', self::API_URL);
                $data = $response->toArray();
                
                if (isset($data['rates'])) {
                    return [
                        'TND' => 1.0,
                        'EUR' => $data['rates']['EUR'] ?? 0.298,
                        'USD' => $data['rates']['USD'] ?? 0.322,
                    ];
                }
            } catch (\Exception $e) {
                // Enregistre silencieusement l'erreur dans les logs
                $this->logger->error('Currency API Error: ' . $e->getMessage());
            }

            // Fallback (taux de secours) si pas de Wi-fi ou API down
            return [
                'TND' => 1.0,
                'EUR' => 0.298,
                'USD' => 0.322,
            ];
        });
    }

    public function convert(float $amount, string $toCurrency): float
    {
        $rates = $this->getRates();
        $rate = $rates[strtoupper($toCurrency)] ?? 1.0;
        
        return $amount * $rate;
    }
}
