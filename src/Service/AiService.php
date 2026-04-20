<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class AiService
{
    private $httpClient;
    private $apiKey;

    public function __construct(HttpClientInterface $httpClient, string $aiApiKey)
    {
        $this->httpClient = $httpClient;
        $this->apiKey = $aiApiKey;
    }

    private function getUrl(): string
    {
        return str_contains($this->apiKey, 'sk-or-')
            ? 'https://openrouter.ai/api/v1/chat/completions'
            : 'https://api.openai.com/v1/chat/completions';
    }

    private function getModel(): string
    {
        return str_contains($this->apiKey, 'sk-or-') ? 'openrouter/auto' : 'gpt-3.5-turbo';
    }

    private function isKeyValid(): bool
    {
        return !empty($this->apiKey) && !str_contains($this->apiKey, 'your_openai_api_key');
    }

    /**
     * Feature 1: Matching Score
     * Uses AI to compare the candidate's message with the tender requirements.
     */
    public function calculateMatchingScore(string $tenderTitle, string $tenderDescription, string $candidateMessage): int
    {
        if (!$this->isKeyValid()) {
            return 0;
        }

        try {
            $response = $this->httpClient->request('POST', $this->getUrl(), [
                'verify_peer' => false,
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                    'Content-Type' => 'application/json',
                    'HTTP-Referer' => 'http://localhost:8000',
                    'X-Title' => 'Gestion Appel Offre',
                ],
                'json' => [
                    'model' => $this->getModel(),
                    'messages' => [
                        ['role' => 'system', 'content' => 'Tu es un expert RH. Évalue le matching. RÈGLE CRUCIALE : Si le candidat mentionne des mots techniques comme "PHP", "Symfony", "PFE", ou une motivation sérieuse, donne un score ÉLEVÉ (entre 75 et 95). Ne sois pas sévère. Réponds UNIQUEMENT par un nombre.'],
                        ['role' => 'user', 'content' => "Offre: $tenderTitle\nDescription: $tenderDescription\n\nCandidat: $candidateMessage"]
                    ],
                    'temperature' => 0.5,
                ],
            ]);

            $data = $response->toArray();
            $content = trim($data['choices'][0]['message']['content']);

            if (preg_match('/\d+/', $content, $matches)) {
                return (int)$matches[0];
            }

            return 15;
        } catch (\Exception $e) {
            return 10;
        }
    }

    /**
     * Feature 2: Description Generator
     * Uses AI to generate a professional tender description.
     */
    public function generateTenderDescription(string $title, string $type, string $category): string
    {
        if (!$this->isKeyValid()) {
            return "Veuillez configurer votre clé API dans le fichier .env";
        }

        try {
            $response = $this->httpClient->request('POST', $this->getUrl(), [
                'verify_peer' => false,
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                    'Content-Type' => 'application/json',
                    'HTTP-Referer' => 'http://localhost:8000',
                ],
                'json' => [
                    'model' => $this->getModel(),
                    'messages' => [
                        ['role' => 'system', 'content' => 'Tu es un expert en appels d\'offre. Réponds UNIQUEMENT en Français. Rédige une description professionnelle de maximum 60 caractères. NE mets PAS de guillemets, NE mentionne JAMAIS le nombre de caractères. Donne juste le texte final.'],
                        ['role' => 'user', 'content' => "Génère une description courte pour l'offre suivante : \nTitre: $title\nType: $type\nCatégorie: $category"]
                    ],
                ],
            ]);

            $statusCode = $response->getStatusCode();
            if ($statusCode !== 200) {
                return "Erreur AI (Code $statusCode) : " . $response->getContent(false);
            }

            $data = $response->toArray();
            $content = $data['choices'][0]['message']['content'];
            $content = preg_replace('/\s*\(.*caractère.*\)\s*/i', '', $content);
            $content = trim($content, '"\' ');

            return $content;
        } catch (\Exception $e) {
            return "Exception AI : " . $e->getMessage();
        }
    }

    /**
     * Feature 3: Budget Suggestion
     * Uses AI to suggest a realistic min/max budget in TND.
     */
    public function suggestBudget(string $title, string $type, string $category): array
    {
        if (!$this->isKeyValid()) {
            return ['min' => 0, 'max' => 0, 'error' => 'Clé API non configurée'];
        }

        try {
            $response = $this->httpClient->request('POST', $this->getUrl(), [
                'verify_peer' => false,
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                    'Content-Type' => 'application/json',
                    'HTTP-Referer' => 'http://localhost:8000',
                ],
                'json' => [
                    'model' => $this->getModel(),
                    'messages' => [
                        ['role' => 'system', 'content' => 'Tu es un expert financier en appels d\'offre en Tunisie. Réponds UNIQUEMENT avec deux nombres entiers séparés par un tiret (ex: 5000-15000) représentant le budget minimum et maximum en TND. Pas de texte, pas de symboles, juste deux nombres.'],
                        ['role' => 'user', 'content' => "Suggère un budget réaliste en TND pour cette offre:\nTitre: $title\nType: $type\nCatégorie: $category"]
                    ],
                    'temperature' => 0.3,
                ],
            ]);

            $statusCode = $response->getStatusCode();
            if ($statusCode !== 200) {
                return ['min' => 0, 'max' => 0, 'error' => 'Erreur API: ' . $statusCode];
            }

            $data = $response->toArray();
            $content = trim($data['choices'][0]['message']['content']);

            // Parse "5000-15000" format
            if (preg_match('/(\d+)\s*[-–]\s*(\d+)/', $content, $matches)) {
                return [
                    'min' => (int)$matches[1],
                    'max' => (int)$matches[2],
                ];
            }

            return ['min' => 0, 'max' => 0, 'error' => 'Format inattendu: ' . $content];
        } catch (\Exception $e) {
            return ['min' => 0, 'max' => 0, 'error' => $e->getMessage()];
        }
    }
}
