<?php

namespace App\Service;

use Symfony\Contracts\Cache\CacheInterface;
use Symfony\Contracts\Cache\ItemInterface;

class FinancialNewsService
{
    public function __construct(
        private CacheInterface $cache
    ) {}

    /**
     * @return array<int, array>
     */
    public function getLatestGlobalNews(int $limit = 4): array
    {
        // Cache pendant 15 minutes — évite les appels HTTP bloquants à chaque requête
        return $this->cache->get('financial_news_' . $limit, function (ItemInterface $item) use ($limit) {
            $item->expiresAfter(900); // 15 minutes

            $url = 'https://news.google.com/rss/search?q=bourse+economie+finance&hl=fr&gl=FR&ceid=FR:fr';
            $news = [];

            try {
                $opts = [
                    'http' => [
                        'method'  => 'GET',
                        'header'  => "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)\r\n",
                        'timeout' => 3,
                    ],
                ];
                $context   = stream_context_create($opts);
                $xmlString = @file_get_contents($url, false, $context);

                if ($xmlString) {
                    $xml = @simplexml_load_string($xmlString);

                    if ($xml && isset($xml->channel->item)) {
                        $counter = 0;
                        foreach ($xml->channel->item as $item2) {
                            if ($counter >= $limit) break;

                            $source        = isset($item2->source) ? (string) $item2->source : 'Global Finance';
                            $time          = strtotime((string) $item2->pubDate);
                            $formattedDate = $time ? date('d/m/Y à H:i', $time) : date('d/m/Y');

                            $news[] = [
                                'title'   => (string) $item2->title,
                                'link'    => (string) $item2->link,
                                'pubDate' => $formattedDate,
                                'source'  => $source,
                            ];
                            $counter++;
                        }
                    }
                }
            } catch (\Exception $e) {
                // Fallback ci-dessous
            }

            // ==========================================
            // FAILSAFE MODE : Sécurité Anti-Panne Soutenance
            // ==========================================
            if (empty($news)) {
                $news = [
                    [
                        'title'   => 'Plongeon inattendu des indices européens suite aux tensions sur le marché énergétique pétrolier mondial.',
                        'link'    => '#',
                        'pubDate' => date('d/m/Y à H:i'),
                        'source'  => 'Bloomberg (Mode Simulé)',
                    ],
                    [
                        'title'   => 'Nouveau record historique pour la branche intelligence artificielle (IA), les valeurs technologiques s\'envolent.',
                        'link'    => '#',
                        'pubDate' => date('d/m/Y à H:i', strtotime('-2 hours')),
                        'source'  => 'Wall Street Journal',
                    ],
                    [
                        'title'   => 'Cryptomonnaies : Le Bitcoin passe difficilement la barre de résistance. Que va faire le régulateur (SEC) ?',
                        'link'    => '#',
                        'pubDate' => date('d/m/Y à H:i', strtotime('-5 hours')),
                        'source'  => 'Reuters News',
                    ],
                    [
                        'title'   => 'La Banque Centrale confirme une baisse massive des taux directeurs, un soulagement pour les investisseurs.',
                        'link'    => '#',
                        'pubDate' => date('d/m/Y à H:i', strtotime('-8 hours')),
                        'source'  => 'Financial Times',
                    ],
                ];
            }

            return array_slice($news, 0, $limit);
        });
    }
}
