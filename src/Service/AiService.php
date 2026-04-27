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
     * Feature 1: Advanced Matching Analysis
     * Uses AI to analyze the candidate's profile against specific criteria.
     * Returns an array with 'score' and 'analysis'.
     */
    public function analyzeCandidature(string $tenderTitle, string $tenderCriteria, string $candidateProfile): array
    {
        if (!$this->isKeyValid()) {
            return ['score' => 0, 'analysis' => 'Clé API non configurée.'];
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
                        ['role' => 'system', 'content' => 'Tu es un expert en recrutement. Évalue la candidature par rapport aux critères fournis.
                        Réponds UNIQUEMENT au format JSON suivant :
                        {
                            "score": <nombre entre 0 et 100>,
                            "analysis": "<analyse détaillée en 2-3 phrases en français soulignant les points forts et faibles>"
                        }'],
                        ['role' => 'user', 'content' => "Appel d'offre: $tenderTitle\nCritères requis: $tenderCriteria\nProfil du candidat: $candidateProfile"]
                    ],
                    'temperature' => 0.4,
                    'response_format' => ['type' => 'json_object']
                ],
            ]);

            $data = $response->toArray();
            $result = json_decode($data['choices'][0]['message']['content'], true);

            return [
                'score' => (int)($result['score'] ?? 0),
                'analysis' => $result['analysis'] ?? 'Analyse indisponible.',
            ];
        } catch (\Exception $e) {
            return ['score' => 0, 'analysis' => 'Erreur lors de l\'analyse AI : ' . $e->getMessage()];
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
    /**
     * Feature 4: Criteria Suggestion
     * Uses AI to suggest selection criteria based on the offer details.
     */
    public function suggestCriteria(string $title, string $type, string $category): string
    {
        if (!$this->isKeyValid()) {
            return "Veuillez configurer votre clé API.";
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
                        ['role' => 'system', 'content' => 'Tu es un expert en recrutement. Réponds UNIQUEMENT en Français. Suggère 3 à 5 critères de sélection précis pour cet appel d\'offre sous forme de liste courte séparée par des virgules. Ne fais pas de phrases complètes. Exemple: "Maîtrise de Java, Certification AWS, 2 ans d\'expérience"'],
                        ['role' => 'user', 'content' => "Génère des critères pour l'offre suivante : \nTitre: $title\nType: $type\nCatégorie: $category"]
                    ],
                    'temperature' => 0.5,
                ],
            ]);

            $data = $response->toArray();
            return trim($data['choices'][0]['message']['content'], '"\' ');
        } catch (\Exception $e) {
            return "Erreur lors de la suggestion : " . $e->getMessage();
        }
    }
}
