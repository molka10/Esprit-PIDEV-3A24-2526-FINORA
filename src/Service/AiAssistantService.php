<?php

namespace App\Service;

use App\Repository\InvestmentRepository;
use Symfony\Contracts\HttpClient\HttpClientInterface;
use Symfony\Component\DependencyInjection\Attribute\Autowire;

class AiAssistantService
{
    public function __construct(
        private readonly InvestmentRepository $investmentRepository,
        private readonly \App\Repository\InvestmentWishlistRepository $wishlistRepository,
        private readonly RecommendationsBuilder $recommendationsBuilder,
        private readonly HttpClientInterface $httpClient,
        private readonly \Symfony\Bundle\SecurityBundle\Security $security,
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
                'match_reason' => $match['reason'],
                'confidence' => $match['confidence'],
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
        if (preg_match('/(maison|villa|logement|house)/i', $message)) return 'MAISON';
        if (preg_match('/(immobilier|appartement|real estate|apartment|property)/i', $message)) return 'IMMOBILIER';
        if (preg_match('/(startup|entreprise|tech|innovation|ia|company|business)/i', $message)) return 'STARTUP';
        if (preg_match('/(hôtel|hotel|tourisme|voyage|tourism|travel)/i', $message)) return 'HOTEL';
        if (preg_match('/(terrain|agricole|terre|agriculture|land|farm)/i', $message)) return 'AGRICULTURE';
        if (preg_match('/(energie|énerg|solar|solaire|energy|wind)/i', $message)) return 'ENERGIE';
        return null;
    }

    private function findBestMatches(?float $budget, ?string $risk, ?string $category): array
    {
        $user = $this->security->getUser();
        
        // 1. Fallback budget to user balance if not specified
        if ($budget === null && $user instanceof \App\Entity\User) {
            $budget = $user->getBalance();
        }

        // 2. Fetch Active & non-Critical investments
        $allInvestments = $this->investmentRepository->findAll();
        $processableItems = [];
        foreach ($allInvestments as $inv) {
            // Requirement: exclude status != ACTIVE and exclude CRITICAL
            if ($inv->getStatus() !== 'ACTIVE' || $inv->getStatus() === 'CRITICAL') {
                continue;
            }
            
            $processableItems[] = $inv;
        }

        // 3. Pre-fetch wishlist IDs for boost logic
        $wishlistIds = [];
        if ($user instanceof \App\Entity\User) {
            $wishlists = $this->wishlistRepository->findBy(['user' => $user]);
            foreach ($wishlists as $w) {
                if ($w->getInvestment()) {
                    $wishlistIds[] = $w->getInvestment()->getId();
                }
            }
        }

        $scored = [];
        foreach ($processableItems as $inv) {
            $score = 0;
            $reasons = [];
            
            // --- A. RISK COMPATIBILITY (40%) ---
            $riskPoints = 0;
            if ($risk !== null) {
                if ($inv->getRiskLevel() === $risk) {
                    $riskPoints = 40;
                    $reasons[] = "Correspond parfaitement à votre profil de risque " . strtolower($risk);
                } else {
                    // Partial points for proximity
                    $levels = ['LOW' => 1, 'MEDIUM' => 2, 'HIGH' => 3];
                    $diff = abs($levels[$inv->getRiskLevel()] - $levels[$risk]);
                    if ($diff === 1) {
                        $riskPoints = 20;
                        $reasons[] = "Niveau de risque proche de vos attentes";
                    }
                }
            } else {
                $riskPoints = 20; // Neutral if unspecified
            }
            $score += $riskPoints;

            // --- B. CATEGORY MATCH (Priorité Absolue) ---
            $catPoints = 0;
            if ($category !== null) {
                if (str_contains(strtolower($inv->getCategory() ?? ''), strtolower($category))) {
                    $catPoints = 200; // Massive bonus to FORCE category match
                    $reasons[] = "Secteur " . $inv->getCategory() . " ciblé";
                } else {
                    $catPoints = -50; // Penalty for wrong category
                }
            } else {
                $catPoints = 15; // Neutral
            }
            $score += $catPoints;

            // --- C. YIELD / RENDEMENT (20%) ---
            // Based on annualReturn: LOW=7.5%, MEDIUM=9.5%, HIGH=12%
            $yield = $inv->getAnnualReturn();
            $yieldPoints = ($yield / 12) * 20; // 12% max -> 20pts
            $score += $yieldPoints;
            if ($yield >= 10) $reasons[] = "Rendement annuel attractif (" . $yield . "%)";

            // --- D. GOAL / DURATION (10%) ---
            $duration = $inv->getDurationMonths();
            $durationPoints = ($duration / 36) * 10; // 36 months max -> 10pts
            $score += $durationPoints;

            // --- E. WISHLIST BOOST (Bonus) ---
            if (in_array($inv->getId(), $wishlistIds)) {
                $score += 10;
                $reasons[] = "Fait partie de vos favoris";
            }
            
            // --- F. BUDGET FILTER (Flexible scoring instead of strict skip) ---
            $val = (float) $inv->getEstimatedValue();
            if ($budget !== null) {
                if ($val > $budget * 1.5) {
                    $score -= 30; // Penalty if too far from budget, but do NOT skip it if it's the right category
                    if ($category !== null && str_contains(strtolower($inv->getCategory() ?? ''), strtolower($category))) {
                        $reasons[] = "Dépasse votre budget (" . number_format($val, 0, '.', ' ') . " TND) mais correspond au secteur";
                    }
                } elseif ($val <= $budget) {
                    $score += 30; // Big bonus for respecting budget
                    $reasons[] = "Respecte votre budget de " . number_format($budget, 0, '.', ' ') . " TND";
                }
            }

            // Final formatting
            $finalScore = (int) min(100, round($score));
            $confidence = $finalScore > 80 ? 'Élevé' : ($finalScore > 50 ? 'Moyen' : 'Faible');

            $scored[] = [
                'investment' => [
                    'id' => $inv->getId(),
                    'name' => $inv->getName(),
                    'category' => $inv->getCategory(),
                    'estimated_value' => $inv->getEstimatedValue(),
                    'risk_level' => $inv->getRiskLevel(),
                    'image_url' => $inv->getImageUrl(),
                    'location' => $inv->getLocation(),
                ],
                'score' => $finalScore,
                'reason' => implode(". ", array_slice($reasons, 0, 2)) . ".",
                'confidence' => $confidence
            ];
        }

        // Sort by score
        usort($scored, fn($a, $b) => $b['score'] <=> $a['score']);
        
        // Handle no results
        if (empty($scored)) {
             if ($budget !== null || $risk !== null || $category !== null) {
                 // Absolute fallback: return the top 3 active investments anyway
                 $fallback = [];
                 foreach ($processableItems as $inv) {
                     $fallback[] = [
                         'investment' => [
                             'id' => $inv->getId(),
                             'name' => $inv->getName(),
                             'category' => $inv->getCategory(),
                             'estimated_value' => $inv->getEstimatedValue(),
                             'risk_level' => $inv->getRiskLevel(),
                             'image_url' => $inv->getImageUrl(),
                             'location' => $inv->getLocation(),
                             'is_external' => false
                         ],
                         'score' => 50,
                         'reason' => "Alternative disponible sur notre plateforme.",
                         'confidence' => 'Faible'
                     ];
                 }
                 return array_slice($fallback, 0, 3);
             }
        }

        return array_slice($scored, 0, 5); // Return Top 5 as requested
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

        $prompt = "Tu es 'Finora Analyst AI', l'assistant financier virtuel d'élite de la plateforme d'investissement Finora.
        Message utilisateur: " . $userMessage . "
        Contexte système ou opportunités trouvées: " . $jsonContext . "
        
        INSTRUCTIONS DE COMPORTEMENT :
        1. Ton ton doit être extrêmement professionnel, poli, rassurant et expert.
        2. Sois concis. Ne fais pas de longues introductions.
        3. Ne renvoie JAMAIS de blocs de code markdown (comme ```html). Formate discrètement en gras (*texte*).
        4. Si le contexte contient des recommandations, mets-les en valeur. Si les recommandations trouvées ne correspondent pas exactement à la demande de l'utilisateur (par exemple, un budget différent ou un autre secteur), explique poliment qu'il s'agit des meilleures alternatives actuellement disponibles sur Finora.
        
        RÈGLE ABSOLUE DE LANGUE : Tu DOIS IMPÉRATIVEMENT générer ta réponse finale UNIQUEMENT en " . strtoupper($selectedLang) . ".";

        try {
            return $this->sendRequest($prompt);
        } catch (\Exception $e) {
            $fallbackMsgs = [
                'fr' => "Je vous recommande particulièrement ces projets qui pourraient correspondre à votre profil.",
                'en' => "I highly recommend these projects that might fit your profile.",
                'ar' => "أوصي بشدة بهذه المشاريع التي قد تناسب ملفك الشخصي."
            ];
            return $fallbackMsgs[$selectedLang] ?? $fallbackMsgs['fr'];
        }
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
                    ],
                    'timeout' => 45 // Groq is fast, so fail fast if down
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
            $response = $this->httpClient->request('POST', $url, [
                'json' => $payload,
                'timeout' => 60 // Allow 60s for Gemini to respond
            ]);
            $data = $response->toArray();
            $text = $data['candidates'][0]['content']['parts'][0]['text'] ?? "Désolé, l'IA est indisponible.";
            return trim(str_replace(['```html', '```'], '', $text));
        } catch (\Exception $e) {
            return "Erreur globale de l'assistant IA: " . $e->getMessage();
        }
    }
}
