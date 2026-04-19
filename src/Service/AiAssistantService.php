<?php

namespace App\Service;

use App\Repository\InvestmentRepository;
use Symfony\Contracts\HttpClient\HttpClientInterface;
use Symfony\Component\DependencyInjection\Attribute\Autowire;

class AiAssistantService
{
    public function __construct(
        private readonly InvestmentRepository $investmentRepository,
        private readonly RecommendationsBuilder $recommendationsBuilder,
        private readonly HttpClientInterface $httpClient,
        #[Autowire(param: 'ai_api_key')]
        string $geminiApiKey = '',
        #[Autowire(param: 'groq_api_key')]
        private readonly string $groqApiKey = '',
        private readonly \Symfony\Component\HttpFoundation\RequestStack $requestStack = new \Symfony\Component\HttpFoundation\RequestStack(),
    ) {
        $this->geminiApiKey = trim(str_replace(['"', "'"], '', $geminiApiKey));
    }
    
    private readonly string $geminiApiKey;

    public function processUserMessage(string $message, string $language = 'fr'): array
    {
        $messageLower = strtolower(trim($message));

        // 1. Détection d'intentions de base (Multilingue)
        $isGreeting = preg_match('/^(bonjour|\bbonsoir\b|\bsalut\b|\bhello\b|\bcoucou\b|\bhey\b|\bhi\b|\bhola\b)/i', $messageLower);
        $isHelp = preg_match('/(\baide\b|\bcomment\b|\bhelp\b|expliqu|fonctionne|qui es|who are|how to)/i', $messageLower);
        $isInsult = preg_match('/(idiot|nul|stupide|bête|merde|con|stupid|dumb|fuck|shit)/i', $messageLower);

        // 2. Extraction des critères
        $budget = $this->extractBudget($messageLower);
        $risk = $this->extractRiskLevel($messageLower);
        $category = $this->extractCategory($messageLower);

        // 3. Logique d'interaction
        if ($isInsult) {
            return [
                'text' => $this->callAiApi($message, "L'utilisateur est agressif ou insulte. Réponds calmement et professionnellement de recadrer la discussion sur l'investissement.", $language),
                'investments' => [],
            ];
        }

        if ($budget === null && $risk === null && $category === null) {
            if ($isGreeting || $isHelp) {
                return [
                    'text' => $this->callAiApi($message, "L'utilisateur dit bonjour ou demande de l'aide. Salue-le et explique que tu es l'Analyste IA Finora, capable de trouver des investissements. Demande quel est son budget, niveau de risque souhaité ou secteur cible.", $language),
                    'investments' => [],
                ];
            }
            
            if (preg_match('/(argent|investir|invest|money|recommand|propose)/i', $messageLower)) {
                $risk = "MEDIUM";
            } else {
                return [
                    'text' => $this->callAiApi($message, "L'utilisateur a dit quelque chose de non spécifique. Réponds que tu ne comprends pas complètement sa demande d'investissement et demande-lui de préciser un budget ou secteur.", $language),
                    'investments' => [],
                ];
            }
        }

        // 4. Recherche des meilleurs investissements
        $recommendations = $this->findBestMatches($budget, $risk, $category);
        $responseMessage = $this->generateResponse($budget, $risk, $category, $recommendations, $message, $language);

        $cards = array_map(function ($match) {
            $inv = $match['investment'];
            return [
                'id' => $inv['id'],
                'name' => $inv['name'],
                'category' => $inv['category'],
                'location' => $inv['location'],
                'estimated_value' => $inv['estimated_value'],
                'risk_level' => $inv['risk_level'],
                'image_url' => $inv['image_url'],
                'match_score' => $match['score'],
                'is_external' => $inv['is_external'] ?? false,
            ];
        }, $recommendations);

        return [
            'text' => $responseMessage,
            'investments' => $cards,
        ];
    }

    private function extractBudget(string $message): ?float
    {
        if (preg_match('/(petit budget|pas cher|abordable|somme modique|modeste)/', $message)) {
            return 50000.0;
        }

        $cleanedMessage = preg_replace('/\s+(?=\d)/', '', $message);
        
        if (preg_match('/(\d+(?:\.\d+)?)\s*(k|m|millions?|mille|kilo)/i', $cleanedMessage, $matches)) {
            $val = (float) $matches[1];
            $unit = strtolower($matches[2]);
            if ($unit === 'k' || $unit === 'mille') return $val * 1000;
            if ($unit === 'm' || str_starts_with($unit, 'million')) return $val * 1000000;
        }

        if (preg_match('/(?:budget|max|euros|dt|\$|jusqu\'à|jusqu\'a|up to)\s*(\d+)/i', $cleanedMessage, $matches) || preg_match('/(?:investir|invest)\s*(\d+)/i', $cleanedMessage, $matches) || preg_match('/(\d{4,})/', $cleanedMessage, $matches)) {
            return (float) $matches[1];
        }

        return null;
    }

    private function extractRiskLevel(string $message): ?string
    {
        if (preg_match('/(faible|bas|sécurisé|securise|sûr|sur|prudent|tranquille|sans risque|garanti|low|safe|secure)/i', $message)) return 'LOW';
        if (preg_match('/(moyen|équilibré|equilibre|modéré|normal|medium|moderate|balanced)/i', $message)) return 'MEDIUM';
        if (preg_match('/(haut|élevé|eleve|risqué|risque|dynamique|rentable|high|risky)/i', $message)) return 'HIGH';
        return null;
    }

    private function extractCategory(string $message): ?string
    {
        if (preg_match('/(maison|immobilier|villa|appartement|logement|house|real estate|apartment|property)/i', $message)) return 'Immobilier';
        if (preg_match('/(startup|entreprise|tech|innovation|ia|company|business)/i', $message)) return 'Startup';
        if (preg_match('/(hôtel|hotel|tourisme|voyage|tourism|travel)/i', $message)) return 'Hôtel';
        if (preg_match('/(terrain|agricole|terre|agriculture|land|farm)/i', $message)) return 'Terrain';
        return null;
    }

    private function findBestMatches(?float $budget, ?string $risk, ?string $category): array
    {
        $processableItems = [];
        $allActive = $this->investmentRepository->findBy(['status' => 'ACTIVE']);
        foreach ($allActive as $inv) {
            $processableItems[] = [
                'is_external' => false,
                'id' => $inv->getId(),
                'name' => $inv->getName(),
                'category' => $inv->getCategory(),
                'estimated_value' => $inv->getEstimatedValue(),
                'risk_level' => $inv->getRiskLevel(),
                'image_url' => $inv->getImageUrl(),
                'location' => $inv->getLocation(),
            ];
        }

        $scored = [];
        foreach ($processableItems as $invArray) {
            $score = 0; $maxScore = 0;
            if ($budget !== null) {
                $maxScore += 500;
                $val = (float) $invArray['estimated_value'];
                if ($val <= $budget) $score += 500; 
                elseif ($val <= $budget * 1.25) $score += 200;
            }
            if ($risk !== null) {
                $maxScore += 100;
                if ($invArray['risk_level'] === $risk) $score += 100;
            }
            if ($category !== null) {
                $maxScore += 1000;
                if (str_contains(strtolower($invArray['category'] ?? ''), strtolower($category))) $score += 1000;
            }
            $finalScore = $maxScore > 0 ? (int)round(($score / $maxScore) * 100) : 50;
            $scored[] = ['investment' => $invArray, 'score' => $finalScore];
        }

        usort($scored, fn($a, $b) => $b['score'] <=> $a['score']);
        return array_slice($scored, 0, 3);
    }

    private function generateResponse(?float $budget, ?string $risk, ?string $category, array $investments, string $userMessage, string $language): string
    {
        $contextData = [
            'detected_budget' => $budget,
            'detected_risk' => $risk,
            'detected_category' => $category,
            'available_investments' => array_map(fn($inv) => [
                'name' => $inv['investment']['name'],
                'category' => $inv['investment']['category'],
                'risk_level' => $inv['investment']['risk_level'],
                'match_score' => $inv['score'],
            ], $investments),
        ];
        
        $jsonContext = json_encode($contextData, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);

        try {
            return $this->callAiApi($userMessage, $jsonContext, $language);
        } catch (\Exception $e) {
            return "Je recommande particulièrement ces projets qui correspondent à votre profil d'investisseur.";
        }
    }

    private function callAiApi(string $userMessage, string $jsonContext, string $language = 'fr'): string
    {
        $langNames = [
            'fr' => 'Français', 
            'en' => 'Anglais', 
            'ar' => 'Arabe',
            'es' => 'Espagnol',
            'de' => 'Allemand',
            'it' => 'Italien'
        ];
        $selectedLang = $langNames[$language] ?? 'Français';

        $prompt = "Tu es Finora Analyst AI, un assistant financier d'élite.
        Message utilisateur: " . $userMessage . "
        Contexte système ou recommandations trouvées: " . $jsonContext . "
        Réponds de manière concise, très professionnelle et amicale. Ne renvoie AUCUN code markdown comme ```html autour de ton texte. Formate ton texte en gras si nécessaire.
        RÈGLE ABSOLUE ET CRITIQUE : Tu DOIS obligatoirement générer ta réponse complète en " . strtoupper($selectedLang) . ", quelle que soit la langue de la question posée par l'utilisateur.";

        return $this->sendRequest($prompt);
    }

    public function generatePortfolioAnalysis(array $portfolioStats): string
    {
        $session = $this->requestStack->getCurrentRequest()?->getSession();
        $currency = $session ? $session->get('currency', 'TND') : 'TND';
        $portfolioStats['target_currency'] = $currency;

        $jsonContext = json_encode($portfolioStats, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
        $prompt = "Analyse ce portefeuille financier Finora pour l'utilisateur. 
        Les montants sont exprimés en $currency.
        Données: " . $jsonContext . "
        Tu es un analyste financier d'élite. Puisque des graphiques affichent déjà les chiffres, concentre-toi sur un diagnostic stratégique écrit très professionnel :
        1. Analyse la pertinence de la diversification actuelle.
        2. Identifie les risques majeurs ou opportunités manquées.
        3. Donne 3 conseils stratégiques concrets.
        Réponds directement en HTML propre (utilise des <h5>, <ul>, <li> et des classes Bootstrap comme 'text-primary' ou 'badge') sans balises Markdown code block.
        RÈGLE ABSOLUE : Sauf indication contraire, réponds par défaut en français, mais si tu détectes une session utilisateur en une autre langue, adapte-toi (bien que ce dashboard soit en FR).";

        return $this->sendRequest($prompt);
    }

    private function sendRequest(string $prompt): string
    {
        // 1. Priorité à Groq (plus fiable dans cet environnement)
        if (!empty($this->groqApiKey)) {
            try {
                $response = $this->httpClient->request('POST', 'https://api.groq.com/openai/v1/chat/completions', [
                    'headers' => ['Authorization' => 'Bearer ' . $this->groqApiKey],
                    'json' => [
                        'model' => 'llama-3.3-70b-versatile',
                        'messages' => [['role' => 'user', 'content' => $prompt]],
                        'temperature' => 0.7
                    ]
                ]);
                $data = $response->toArray();
                return $data['choices'][0]['message']['content'] ?? "Erreur d'analyse.";
            } catch (\Exception $e) {
                // Fallback to Gemini if Groq fails
            }
        }

        // 2. Fallback Gemini
        try {
            $payload = [
                'contents' => [['role' => 'user', 'parts' => [['text' => $prompt]]]]
            ];
            $url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" . $this->geminiApiKey;
            $response = $this->httpClient->request('POST', $url, ['json' => $payload]);
            $data = $response->toArray();
            $text = $data['candidates'][0]['content']['parts'][0]['text'] ?? "Désolé, l'IA est indisponible.";
            return trim(str_replace(['```html', '```'], '', $text));
        } catch (\Exception $e) {
            return "Erreur globale de l'assistant IA: " . $e->getMessage();
        }
    }
}
