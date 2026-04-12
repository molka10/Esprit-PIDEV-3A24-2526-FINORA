<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class TextToSpeechService
{
    private const DG_TTS_URL = 'https://api.deepgram.com/v1/speak?model=aura-2-agathe-fr&encoding=linear16&container=wav';
    private const MAX_CHARS_PER_REQUEST = 700;

    public function __construct(
        private readonly HttpClientInterface $client,
        private readonly string $apiKey,
    ) {
    }

    public function generateAudio(string $text): string
    {
        $text = $this->clean($text);
        $text = $this->limitToFastChunk($text, self::MAX_CHARS_PER_REQUEST);

        if ($text === '') {
            throw new \RuntimeException('Le texte à lire est vide.');
        }

        if (trim($this->apiKey) === '') {
            throw new \RuntimeException('DEEPGRAM_API_KEY manquante.');
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

        $statusCode = $response->getStatusCode();
        if ($statusCode < 200 || $statusCode >= 300) {
            throw new \RuntimeException('Deepgram TTS a échoué avec le code ' . $statusCode . '.');
        }

        return $response->getContent();
    }

    private function clean(string $text): string
    {
        $text = str_replace(["\0", "\r", "\n", "\t"], ' ', $text);
        $text = preg_replace('/\s+/', ' ', $text) ?? '';
        return trim($text);
    }

    private function limitToFastChunk(string $text, int $maxChars): string
    {
        if (mb_strlen($text) <= $maxChars) {
            return $text;
        }

        $chunk = mb_substr($text, 0, $maxChars);
        $cut = $this->lastGoodCut($chunk);

        if ($cut <= 0) {
            return trim($chunk);
        }

        return trim(mb_substr($chunk, 0, $cut));
    }

    private function lastGoodCut(string $text): int
    {
        $length = mb_strlen($text);

        for ($i = $length - 1; $i >= 0; $i--) {
            $char = mb_substr($text, $i, 1);
            if (in_array($char, ['.', '!', '?', ';', ':'], true)) {
                return $i + 1;
            }
        }

        for ($i = $length - 1; $i >= 0; $i--) {
            $char = mb_substr($text, $i, 1);
            if (trim($char) === '') {
                return $i;
            }
        }

        return $length;
    }
}