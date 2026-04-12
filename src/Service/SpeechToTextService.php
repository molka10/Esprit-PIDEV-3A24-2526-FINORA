<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class SpeechToTextService
{
    private HttpClientInterface $client;
    private string $apiKey;

    public function __construct(HttpClientInterface $client, string $apiKey)
    {
        $this->client = $client;
        $this->apiKey = $apiKey;
    }

    public function transcribe(string $audioContent): string
    {
        $response = $this->client->request('POST',
            'https://api.deepgram.com/v1/listen?model=nova-2&language=fr&smart_format=true',
            [
                'headers' => [
                    'Authorization' => 'Token ' . $this->apiKey,
                    'Content-Type' => 'audio/webm'
                ],
                'body' => $audioContent
            ]
        );

        $data = $response->toArray(false);

        return $data['results']['channels'][0]['alternatives'][0]['transcript'] ?? '';
    }
}