<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class QuizAiCommentService
{
    private const GROQ_URL = 'https://api.groq.com/openai/v1/chat/completions';
    private const MODEL = 'llama-3.1-8b-instant';

    public function __construct(
        private readonly HttpClientInterface $client,
        private readonly string $groqApiKey
    ) {
    }

    public function generateFraudComment(array $fraudDetails): string
    {
        $prompt = <<<PROMPT
Tu es un examinateur strict analysant un comportement suspect lors d'un quiz.
Voici les données de télémétrie de l'étudiant :
- Pertes de focus (changement d'onglets) : {$fraudDetails['focusLossCount']}
- Sorties du mode plein écran : {$fraudDetails['exitFullscreenCount']}
- Réponses anormalement rapides (< 3 secondes) : {$fraudDetails['fastAnswers']}

Écris un court paragraphe (maximum 2 à 3 phrases) en français expliquant de manière professionnelle pourquoi ces statistiques indiquent une suspicion de triche.
Adresse l'explication directement à l'étudiant (ex: "Nous avons détecté..."). Ne donne aucun conseil, juste l'explication.
PROMPT;

        $body = [
            'model' => self::MODEL,
            'temperature' => 0.5,
            'max_tokens' => 300,
            'messages' => [
                [
                    'role' => 'user',
                    'content' => $prompt,
                ],
            ],
        ];

        try {
            $response = $this->client->request('POST', self::GROQ_URL, [
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->groqApiKey,
                    'Content-Type' => 'application/json',
                    'Accept' => 'application/json',
                ],
                'json' => $body,
                'timeout' => 20,
            ]);

            $decoded = json_decode($response->getContent(), true);
            if (isset($decoded['choices'][0]['message']['content'])) {
                return trim($decoded['choices'][0]['message']['content']);
            }
        } catch (\Throwable $e) {
            // Silently fallback on default message if API is down
        }

        return "Nous avons détecté un comportement anormal lors de votre session (changements d'onglets, sorties du mode plein écran ou réponses trop rapides). Votre évaluation a été invalidée.";
    }
}
