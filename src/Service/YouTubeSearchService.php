<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class YouTubeSearchService
{
    private const API_URL = 'https://www.googleapis.com/youtube/v3/search';

    public function __construct(
        private readonly HttpClientInterface $httpClient,
        private readonly ?string $apiKey,
    ) {
    }

    /**
     * @return array<int, array{
     *     title: string,
     *     channelTitle: string,
     *     thumbnailUrl: string|null,
     *     videoId: string,
     *     url: string
     * }>
     */
    public function search(string $query, int $maxResults = 8, string $order = 'relevance'): array
    {
        $query = trim($query);

        if ($query === '') {
            return [];
        }

        if ($this->apiKey === null || trim($this->apiKey) === '') {
            throw new \RuntimeException('La clé API YouTube est manquante.');
        }

        $allowedOrders = ['date', 'rating', 'relevance', 'title', 'viewCount'];
        if (!in_array($order, $allowedOrders, true)) {
            $order = 'relevance';
        }

        $response = $this->httpClient->request('GET', self::API_URL, [
            'query' => [
                'part' => 'snippet',
                'q' => $query,
                'type' => 'video',
                'maxResults' => $maxResults,
                'order' => $order,
                'key' => $this->apiKey,
            ],
        ]);

        if ($response->getStatusCode() !== 200) {
            throw new \RuntimeException('Erreur YouTube API.');
        }

        $data = $response->toArray(false);

        if (isset($data['error']['message'])) {
            throw new \RuntimeException('YouTube API : ' . $data['error']['message']);
        }

        $results = [];

        foreach ($data['items'] ?? [] as $item) {
            $videoId = $item['id']['videoId'] ?? null;
            if (!$videoId) {
                continue;
            }

            $snippet = $item['snippet'] ?? [];
            $thumbs = $snippet['thumbnails'] ?? [];

            $thumbnailUrl =
                $thumbs['high']['url']
                ?? $thumbs['medium']['url']
                ?? $thumbs['default']['url']
                ?? null;

            $results[] = [
                'title' => (string) ($snippet['title'] ?? 'Sans titre'),
                'channelTitle' => (string) ($snippet['channelTitle'] ?? ''),
                'thumbnailUrl' => $thumbnailUrl,
                'videoId' => $videoId,
                'url' => 'https://www.youtube.com/watch?v=' . $videoId,
            ];
        }

        return $results;
    }
}