<?php

namespace App\Service;

use Psr\Log\LoggerInterface;
use Symfony\Contracts\Cache\CacheInterface;
use Symfony\Contracts\Cache\ItemInterface;

class RecommendationService
{
    public function __construct(
        private CacheInterface $cache,
        private LoggerInterface $logger
    ) {}

    public function getRecommendations(int $userId): array
    {
        $this->logger->info("Generating recommendations for user {user}", ['user' => $userId]);

        // Caching for 1 hour to improve performance
        return $this->cache->get("user_recommendations_$userId", function (ItemInterface $item) use ($userId) {
            $item->expiresAfter(3600);
            
            return $this->buildRecommendations($userId);
        });
    }

    private function buildRecommendations(int $userId): array
    {
        // Mock data representing User History & Preferences
        $userHistory = [
            1 => ['categories' => ['Tech', 'Gadgets'], 'recent' => [101, 105]],
            2 => ['categories' => ['Fitness', 'Health'], 'recent' => [202]]
        ];

        // Mock Database of Items
        $items = [
            ['id' => 10, 'name' => 'Smartphone X', 'category' => 'Tech', 'popularity' => 95],
            ['id' => 12, 'name' => 'Wireless Buds', 'category' => 'Tech', 'popularity' => 88],
            ['id' => 15, 'name' => 'Yoga Mat', 'category' => 'Fitness', 'popularity' => 92],
            ['id' => 20, 'name' => 'Smart Watch', 'category' => 'Tech', 'popularity' => 75],
            ['id' => 25, 'name' => 'Protein Shake', 'category' => 'Fitness', 'popularity' => 85],
            ['id' => 30, 'name' => 'Gaming Mouse', 'category' => 'Tech', 'popularity' => 60],
            ['id' => 40, 'name' => 'Dumbbell Set', 'category' => 'Fitness', 'popularity' => 70],
        ];

        $userData = $userHistory[$userId] ?? null;
        $recommendations = [];

        if ($userData) {
            // 1. Scoring Logic (Preference matching)
            foreach ($items as $item) {
                $score = 0;
                
                // Boost score if item is in user's favorite category
                if (in_array($item['category'], $userData['categories'])) {
                    $score += 50;
                }

                // Add popularity factor
                $score += $item['popularity'] * 0.5;

                $item['score'] = $score;
                $recommendations[] = $item;
            }
        } else {
            // 2. Fallback: Popular items if no history
            $recommendations = $items;
            foreach ($recommendations as &$item) {
                $item['score'] = $item['popularity'];
            }
        }

        // 3. Ranking & Filtering
        usort($recommendations, fn($a, $b) => $b['score'] <=> $a['score']);
        
        // Remove 'score' and 'popularity' from final mapping and limit to 5
        return array_map(function($item) {
            return [
                'id' => $item['id'],
                'name' => $item['name'],
                'category' => $item['category']
            ];
        }, array_slice($recommendations, 0, 5));
    }
}
