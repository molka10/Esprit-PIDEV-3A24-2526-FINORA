<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class TextToSpeechService
{
    private const DG_TTS_URL = 'https://api.deepgram.com/v1/speak?model=aura-2-agathe-fr&encoding=linear16&container=wav';

    public function __construct(
        private readonly HttpClientInterface $client,
        private readonly string $apiKey,
    ) {}

    public function generateAndSave(string $text, string $filePath): void
    {
        if (file_exists($filePath)) {
            return; // already generated
        }

        $response = $this->client->request('POST', self::DG_TTS_URL, [
            'headers' => [
                'Authorization' => 'Token ' . $this->apiKey,
                'Content-Type' => 'application/json',
                'Accept' => 'audio/wav',
            ],
            'json' => [
                'text' => $text,
            ],
            'timeout' => 30,
        ]);

        $audio = $response->getContent();

        file_put_contents($filePath, $audio);
    }
}
