<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class SpeechToTextService
{
    private const DEEPGRAM_URL = 'https://api.deepgram.com/v1/listen?model=nova-2&language=fr&smart_format=true';

    public function __construct(
        private readonly HttpClientInterface $client,
        private readonly string $apiKey,
    ) {
    }

    public function transcribe(string $audioContent, string $contentType = 'audio/webm'): string
    {
        if (trim($this->apiKey) === '') {
            throw new \RuntimeException('DEEPGRAM_API_KEY manquante.');
        }

        if ($audioContent === '') {
            throw new \RuntimeException('Audio vide.');
        }

        $response = $this->client->request('POST', self::DEEPGRAM_URL, [
            'headers' => [
                'Authorization' => 'Token ' . $this->apiKey,
                'Content-Type' => $contentType,
                'Accept' => 'application/json',
            ],
            'body' => $audioContent,
            'timeout' => 25,
        ]);

        $statusCode = $response->getStatusCode();
        $raw = $response->getContent(false);

        if ($statusCode < 200 || $statusCode >= 300) {
            throw new \RuntimeException('Deepgram error ' . $statusCode . ': ' . $raw);
        }

        $data = json_decode($raw, true);

        if (!is_array($data)) {
            throw new \RuntimeException('Réponse Deepgram invalide.');
        }

        return trim((string) ($data['results']['channels'][0]['alternatives'][0]['transcript'] ?? ''));
    }
}