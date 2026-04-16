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
        #[Autowire(env: 'GEMINI_API_KEY')]
        private readonly string $geminiApiKey = '',
    ) {
    }

    public function processUserMessage(string $message): array
    {
        $messageLower = strtolower(trim($message));

        // 1. Détection d'intentions de base (Salutations, Demande d'aide)
        $isGreeting = preg_match('/^(bonjour|\bbonsoir\b|\bsalut\b|\bhello\b|\bcoucou\b|\bhey\b)/i', $messageLower);
        $isHelp = preg_match('/(\baide\b|\bcomment\b|\bhelp\b|expliqu|fonctionne|qui es(.?)tu)/i', $messageLower);
        $isInsult = preg_match('/(idiot|nul|stupide|bête|merde|con)/i', $messageLower);

        // 2. Extraction des critères
        $budget = $this->extractBudget($messageLower);
        $risk = $this->extractRiskLevel($messageLower);
        $category = $this->extractCategory($messageLower);

        // 3. Logique d'interaction intelligente
        if ($isInsult) {
            return [
                'text' => "<div class='text-white-50 small'><i class='bi bi-robot me-1'></i> <b>Finora Analyst AI</b></div><p>Restons professionnels s'il vous plaît. Je suis programmé pour vous trouver les meilleurs rendements, pas pour les joutes verbales. Que recherchez-vous ? 📊</p>",
                'investments' => [],
            ];
        }

        // Si l'utilisateur dit juste bonjour ou demande de l'aide SANS critère d'investissement
        if ($budget === null && $risk === null && $category === null) {
            if ($isGreeting) {
                return [
                    'text' => "<div class='text-white-50 small'><i class='bi bi-robot me-1'></i> <b>Finora Analyst AI</b></div><p>Bonjour ! 👋 Je suis votre analyste financier virtuel. Je peux analyser des centaines d'opportunités sur notre plateforme Finora et chez nos partenaires.<br><br><b>Que cherchez-vous aujourd'hui ?</b><br>- Un secteur précis ? (Technologie, Immobilier...)<br>- Un budget ciblé ? (ex: 50k, 2 millions)<br>- Un type de rendement ? (Sans risque, risqué...)</p>",
                    'investments' => [],
                ];
            }
            if ($isHelp) {
                return [
                    'text' => "<div class='text-white-50 small'><i class='bi bi-robot me-1'></i> <b>Finora Analyst AI</b></div><p>Je suis là pour vous faciliter la vie ! 🤖<br><br>Tapez simplement ce que vous avez en tête, avec vos mots, par exemple :<br><i>« Je voudrais investir 15000 euros de façon sécurisée dans le tourisme »</i> ou <i>« Montre-moi des startups rentables »</i>.</p>",
                    'investments' => [],
                ];
            }
            
            // Si la demande est vague : par exemple "Je veux faire de l'argent" ou "montre"
            if (preg_match('/(argent|investir|tout|quoi|recommand|propose)/i', $messageLower)) {
                // On passe au matching par défaut pour lui montrer le Top du moment
                $risk = "MEDIUM"; // Profil par défaut implicite pour éviter le hasard total
            } else {
                return [
                    'text' => "<div class='text-white-50 small'><i class='bi bi-robot me-1'></i> <b>Finora Analyst AI</b></div><div class='alert alert-secondary bg-opacity-10 border-0 text-white'><i class='bi bi-question-circle text-info me-2'></i>Je ne suis pas sûr de bien cerner votre demande financière. Pourriez-vous préciser si vous avez un budget ou un secteur de préférence ?</div>",
                    'investments' => [],
                ];
            }
        }

        // 4. Recherche des meilleurs investissements correspondants
        $recommendations = $this->findBestMatches($budget, $risk, $category);

        $responseMessage = $this->generateResponse($budget, $risk, $category, $recommendations, $message);

        // Format cards for frontend
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
        // "Petit budget", "pas cher" -> On assigne un budget virtuel de 50 000 max
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

        if (preg_match('/(?:budget|max|euros|dt|\$|jusqu\'à)\s*(\d+)/i', $cleanedMessage, $matches) || preg_match('/(?:investir)\s*(\d+)/i', $cleanedMessage, $matches) || preg_match('/(\d{4,})/', $cleanedMessage, $matches)) {
            return (float) $matches[1];
        }

        return null;
    }

    private function extractRiskLevel(string $message): ?string
    {
        if (preg_match('/(faible|bas|sécurisé|securise|sûr|sur|prudent|tranquille|sans risque|garanti|père de famille)/', $message)) return 'LOW';
        if (preg_match('/(moyen|équilibré|equilibre|modéré|normal)/', $message)) return 'MEDIUM';
        if (preg_match('/(haut|élevé|eleve|risqué|risque|dynamique|rentable|max|ambitieux|gagnant|gros rendements|crypto)/', $message)) return 'HIGH';
        return null; // Si non précisé, on laisse l'algorithme faire son mix
    }

    private function extractCategory(string $message): ?string
    {
        if (preg_match('/(maison|immobilier.*résidentiel|villa|appartement|logement|dormir|résidence|foyer)/', $message)) return 'Immobilier';
        if (preg_match('/(startup|entreprise|tech|innovation|logiciel|b2b|web|internet|application|ia)/', $message)) return 'Startup';
        if (preg_match('/(hôtel|hotel|tourisme|voyage|resort|vacances|bnb)/', $message)) return 'Hôtel';
        if (preg_match('/(terrain|agricole|lotissement|terre|champ|agriculture)/', $message)) return 'Terrain';
        return null;
    }

    private function findBestMatches(?float $budget, ?string $risk, ?string $category): array
    {
        $processableItems = [];

        // 1. Fetch Internal
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
            $score = 0;
            $maxScore = 0;

            if ($budget !== null) {
                $maxScore += 500;
                $val = (float) $invArray['estimated_value'];
                if ($val <= $budget) {
                    $score += 500; 
                } elseif ($val <= $budget * 1.25) {
                    $score += 200;
                }
            }

            if ($risk !== null) {
                $maxScore += 100;
                if ($invArray['risk_level'] === $risk) {
                    $score += 100;
                } else {
                    $riskMap = ['LOW' => 1, 'MEDIUM' => 2, 'HIGH' => 3];
                    $asked = $riskMap[$risk] ?? 0;
                    $actual = $riskMap[$invArray['risk_level']] ?? 0;
                    if (abs($asked - $actual) === 1) {
                        $score += 40;
                    }
                }
            }

            if ($category !== null) {
                $maxScore += 1000;
                // Harder matching for Category
                if (str_contains(strtolower($invArray['category'] ?? ''), strtolower($category))) {
                    $score += 1000;
                } elseif ($category === 'Immobilier' && in_array($invArray['category'], ['Maison', 'Appartement', 'Villa'])) {
                    $score += 1000;
                }
            }

            if ($maxScore === 0) {
                $score = 50;
                if ($invArray['risk_level'] === 'LOW') $score += 20;
                if ((float)$invArray['estimated_value'] < 500000) $score += 10;
                $maxScore = 100;
            }

            $finalScore = $maxScore > 0 ? (int)round(($score / $maxScore) * 100) : 0;

            if ($finalScore > 0) {
                $scored[] = [
                    'investment' => $invArray,
                    'score' => $finalScore
                ];
            }
        }

        usort($scored, function ($a, $b) {
            return $b['score'] <=> $a['score'];
        });

        // Try to mix internal and external if scores are close
        $top = array_slice($scored, 0, 3);
        return $top;
    }

    private function generateResponse(?float $budget, ?string $risk, ?string $category, array $investments, string $userMessage): string
    {
        // On prépare le contexte pour l'API Gemini
        $contextData = [
            'detected_budget' => $budget,
            'detected_risk' => $risk,
            'detected_category' => $category,
            'available_investments' => [],
        ];

        foreach ($investments as $inv) {
            $contextData['available_investments'][] = [
                'name' => $inv['investment']['name'] ?? '',
                'category' => $inv['investment']['category'] ?? '',
                'risk_level' => $inv['investment']['risk_level'] ?? '',
                'estimated_value' => $inv['investment']['estimated_value'] ?? 0,
                'is_external' => $inv['investment']['is_external'] ?? false,
                'match_score' => $inv['score'],
            ];
        }
        
        $jsonContext = json_encode($contextData, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);

        if (!empty($this->geminiApiKey) && trim($this->geminiApiKey) !== 'VOTRE_CLE_API_ICI') {
            try {
                return $this->callGeminiApi($userMessage, $jsonContext);
            } catch (\Exception $e) {
                // Ignore l'erreur API et passe silencieusement au Fallback local
            }
        }
        
        return $this->generateLocalResponse($budget, $risk, $category, $investments);
    }

    private function callGeminiApi(string $userMessage, string $jsonContext): string
    {
        $systemInstruction = $this->getChatSystemInstruction();
        $payload = [
            'contents' => [
                ['role' => 'user', 'parts' => [['text' => "INSTRUCTIONS SYSTEME:\n" . $systemInstruction . "\n\nMESSAGE UTILISATEUR:\n" . $userMessage . "\n\nCONTEXTE JSON:\n" . $jsonContext]]]
            ],
            'generationConfig' => ['temperature' => 0.5]
        ];

        return $this->sendRequest($payload);
    }

    public function generatePortfolioAnalysis(array $portfolioStats): string
    {
        if (empty($this->geminiApiKey) || trim($this->geminiApiKey) === 'VOTRE_CLE_API_ICI') {
            return "<div class='alert alert-warning'>API Gemini non configurée.</div>";
        }

        $jsonContext = json_encode($portfolioStats, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
        $systemInstruction = $this->getRoboSystemInstruction();
        $payload = [
            'contents' => [
                ['role' => 'user', 'parts' => [['text' => "INSTRUCTIONS SYSTEME:\n" . $systemInstruction . "\n\nVoici les statistiques:\n" . $jsonContext]]]
            ],
            'generationConfig' => ['temperature' => 0.4]
        ];

        return $this->sendRequest($payload);
    }

    private function sendRequest(array $payload): string
    {
        try {
            $url = 'https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent';

            $response = $this->httpClient->request('POST', $url, [
                'query' => ['key' => $this->geminiApiKey],
                'json' => $payload
            ]);

            $data = $response->toArray();
            if (isset($data['candidates'][0]['content']['parts'][0]['text'])) {
                $text = $data['candidates'][0]['content']['parts'][0]['text'];
                return trim(str_replace(['```html', '```'], '', $text));
            }
        } catch (\Exception $e) {
            return "<div class='alert alert-danger border-0'><i class='bi bi-exclamation-triangle me-2'></i>Erreur de l'assistant IA: " . htmlspecialchars($e->getMessage()) . "</div>";
        }

        return "<div class='alert alert-danger border-0'>Réponse IA vide ou invalide.</div>";
    }

    private function getChatSystemInstruction(): string
    {
        return <<<TEXT
# RÔLE ET IDENTITÉ
Tu es "Finora Analyst AI", le conseiller en investissement virtuel de haut niveau intégré à la plateforme Finora. Tu combines la rigueur d'un analyste financier et l'empathie d'un conseiller premium.

# RÈGLES ABSOLUES
- INTERDICTION de garantir des rendements.
- Parle UNIQUEMENT des projets fournis dans le contexte JSON.
- Retourne uniquement du HTML pur (pas de markdown ```html).
- Mentionne toujours les risques.
TEXT;
    }

    private function getRoboSystemInstruction(): string
    {
        return <<<TEXT
# RÔLE
Tu es "Finora Robo-Advisor". Analyse le portefeuille JSON et produis un diagnostic stratégique (points forts, vigilance, recommandation). 
Utilise uniquement le HTML avec les classes Bootstrap 5 (p-3, rounded, alert, etc.). Pas de markdown.
TEXT;
    }
}
