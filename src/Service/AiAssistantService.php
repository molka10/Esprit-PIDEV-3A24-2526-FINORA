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
        $systemInstruction = <<<TEXT
# RÔLE ET IDENTITÉ
Tu es "Finora Analyst AI", le conseiller en investissement virtuel de haut niveau intégré à la plateforme Finora. Tu combines la rigueur d'un analyste financier, la logique d'un algorithme de recommandation (style Airbnb/Netflix) et l'empathie d'un conseiller client premium. 

# CONTEXTE DE LA PLATEFORME
Finora est une plateforme de financement participatif et de marché d'investissement (Immobilier, Startups, Hôtellerie, Terrains). Les utilisateurs viennent te voir pour découvrir des opportunités, analyser des projets, ou comparer des options selon leur budget et leur tolérance au risque.

# TA MISSION
Tu reçois deux choses :
1. Le message brut de l'utilisateur.
2. Un contexte JSON contenant les critères extraits (budget, risque, catégorie) et une liste d'opportunités correspondantes issues de notre base de données.

Tu dois analyser ce contexte et formuler une réponse ultra-personnalisée qui met en valeur les meilleures opportunités, sans jamais inventer de projet qui n'est pas dans la liste fournie.

# RÈGLES ABSOLUES (SAFETY & COMPLIANCE)
- INTERDICTION TOTALE de garantir des rendements ou de promettre des gains.
- Tu dois toujours mentionner que les investissements comportent des risques.
- Tu ne dois JAMAIS utiliser de balises markdown de type ```html. Tu dois retourner du HTML pur et brut.
- Tu ne dois parler QUE des projets présents dans le array `available_investments`. Si la liste est vide, tu le dis honnêtement.
- Si l'utilisateur est agressif ou vulgarise, reste stoïque, professionnel et ramène le sujet sur l'investissement.

# COMPORTEMENT ET ADAPTATION DU TON
- Détecte le niveau de l'utilisateur :
  * Débutant (vocabulaire simple, questions basiques) -> Utilise des métaphores simples, explique les termes. Ton : Paternel/Pédagogique.
  * Intermédiaire (demande d'analyse) -> Ton : Analytique, explique les Pour/Contre.
  * Expert (demande de comparaison précise, Leviers) -> Ton : Direct, chiffres précis, langage technique.
- Sois proactif : si l'utilisateur donne un budget mais pas de risque, suggère le scénario le plus probable tout en proposant l'alternative.

# FORMAT DE SORTIE OBLIGATOIRE (HTML)
Tu DOIS structurer ta réponse exactement avec ce HTML (utilise les classes Bootstrap 5 existantes). Ne mets aucun texte avant ou après ce bloc HTML.

1. L'en-tête d'identité :
<div class="text-white-50 mb-3 small"><i class="bi bi-robot me-1"></i> <b>Finora Analyst AI</b> — [Titre contextuel, ex: Analyse de votre profil]</div>

2. Les critères détectés (sous forme de badges) :
<div class="d-flex flex-wrap gap-2 mb-3">
  <span class="badge bg-light text-dark bg-opacity-75">[Critère 1]</span>
  <span class="badge bg-light text-dark bg-opacity-75">[Critère 2]</span>
</div>

3. Une phrase d'introduction humaine (2-3 lignes max) expliquant ta démarche.

4. La recommandation principale (si dispo) :
<div class="p-3 rounded-3 mb-3" style="background: rgba(255,255,255,0.05); border-left: 4px solid var(--primary-color);">
  <h6 class="text-white mb-2">🎯 [Nom du projet] [Badge Interne/Externe]</h6>
  <div class="d-flex gap-3 text-sm text-white-50 mb-3">
    <span><i class="bi bi-graph-up-arrow text-success me-1"></i>[ROI estimé: 5-8% si faible, 8-12% si moyen, 12-20% si élevé]</span>
    <span><i class="bi bi-shield-check text-warning me-1"></i>Risque [Badge LOW/MEDIUM/HIGH]</span>
  </div>
  <h6 class="text-white-50 text-uppercase" style="font-size: 0.75rem; letter-spacing: 1px;">Pourquoi c'est un bon choix :</h6>
  <ul class="mb-0 ps-3 text-white" style="font-size: 0.9rem;">
    <li class="mb-1"><b class="text-success">[Argument 1]</b></li>
    <li><b class="text-info">[Argument 2]</b></li>
  </ul>
</div>

5. L'alternative (si dispo) :
<p class="mb-2 text-white-50 small">Option alternative à considérer :</p>
<div class="d-flex align-items-center p-2 rounded-3" style="background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.1); font-size: 0.85rem;">
  <i class="bi bi-arrow-right-circle text-secondary me-2"></i>
  <span class="text-white me-auto"><b>[Nom Alt]</b> <span class="text-muted">[Source]</span></span>
  <span>[Badge Risque]</span>
</div>

6. Le disclaimer de sécurité :
<div class="mt-3 small text-muted font-monospace" style="font-size: 0.75rem;">Avertissement : Les performances passées ne préjugent pas des performances futures. FINORA ne fournit aucune garantie financière.</div>

# EXEMPLE DE BADGES À UTILISER
- Risque Faible : <span class="badge bg-success bg-opacity-25 text-success border border-success">Faible</span>
- Risque Moyen : <span class="badge bg-warning bg-opacity-25 text-warning border border-warning">Moyen</span>
- Risque Élevé : <span class="badge bg-danger bg-opacity-25 text-danger border border-danger">Élevé</span>
- Source Interne (si is_external=false) : <span class="badge bg-primary bg-opacity-25 text-primary border border-primary ms-2"><i class="bi bi-house me-1"></i>Catalogue Interne</span>
- Source Externe (si is_external=true) : <span class="badge bg-info bg-opacity-25 text-info border border-info ms-2"><i class="bi bi-globe me-1"></i>Partenaire Externe</span>
TEXT;

        $response = $this->httpClient->request('POST', 'https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent', [
            'query' => [
                'key' => $this->geminiApiKey
            ],
            'json' => [
                'system_instruction' => [
                    'parts' => [
                        ['text' => $systemInstruction]
                    ]
                ],
                'contents' => [
                    ['role' => 'user', 'parts' => [['text' => "Message utilisateur: " . $userMessage . "\n\nContexte JSON: " . $jsonContext]]]
                ],
                'generationConfig' => [
                    'temperature' => 0.5,
                ]
            ]
        ]);

        $data = $response->toArray();
        if (isset($data['candidates'][0]['content']['parts'][0]['text'])) {
            $text = $data['candidates'][0]['content']['parts'][0]['text'];
            
            // Nettoyage de balises Markdown si le modèle en renvoie quand même.
            $text = str_replace('```html', '', $text);
            $text = str_replace('```', '', $text);
            
            return trim($text);
        }

        throw new \Exception("Invalid API response format");
    }

    private function generateLocalResponse(?float $budget, ?string $risk, ?string $category, array $investments): string
    {
        if (count($investments) === 0) {
            return "<div class='alert alert-warning mb-0 border-0' style='background: rgba(255, 193, 7, 0.1); color: #ffc107;'><i class='bi bi-exclamation-triangle me-2'></i>D'après votre profil, je n'ai malheureusement trouvé aucune opportunité correspondant exactement à vos critères en ce moment. Vous pouvez essayer d'élargir votre budget ou d'ajuster votre tolérance au risque.</div>";
        }

        $bestMatch = $investments[0]['investment'];
        $bestScore = $investments[0]['score'];

        $intro = "<div class='text-white-50 mb-3 small'><i class='bi bi-robot me-1'></i> <b>Finora Analyst AI</b> — Analyse de votre profil</div>";
        $criteres = [];
        if ($budget) $criteres[] = "<span class='badge bg-light text-dark bg-opacity-75'>Budget: <i class='bi bi-cash-coin text-success'></i> " . number_format($budget, 0, ',', ' ') . " $</span>";
        if ($risk) $criteres[] = "<span class='badge bg-light text-dark bg-opacity-75'>Risque cible: " . $risk . "</span>";
        if ($category) $criteres[] = "<span class='badge bg-light text-dark bg-opacity-75'>Secteur: " . $category . "</span>";
        
        if (count($criteres) > 0) {
            $intro .= "<div class='d-flex flex-wrap gap-2 mb-3'>" . implode('', $criteres) . "</div>";
        }

        $intro .= "<p>Voici ma meilleure recommandation basée sur vos critères stricts :</p>";

        $html = $intro;
        
        $roi = "5–8%";
        if ($bestMatch['risk_level'] === 'MEDIUM') $roi = "8–12%";
        if ($bestMatch['risk_level'] === 'HIGH') $roi = "12–20%";

        $riskLabels = [
            'LOW' => '<span class="badge bg-success bg-opacity-25 text-success border border-success">Faible</span>',
            'MEDIUM' => '<span class="badge bg-warning bg-opacity-25 text-warning border border-warning">Moyen</span>',
            'HIGH' => '<span class="badge bg-danger bg-opacity-25 text-danger border border-danger">Élevé</span>'
        ];
        $riskBadge = $riskLabels[$bestMatch['risk_level']] ?? $bestMatch['risk_level'];

        $html .= "<div class='p-3 rounded-3 mb-3' style='background: rgba(255,255,255,0.05); border-left: 4px solid var(--primary-color);'>";
        $html .= "<h6 class='text-white mb-2'>🎯 " . htmlspecialchars($bestMatch['name']) . "</h6>";
        $html .= "<div class='d-flex gap-3 text-sm text-white-50 mb-3'>";
        $html .= "<span><i class='bi bi-graph-up-arrow text-success me-1'></i>" . $roi . " de ROI estimé</span>";
        $html .= "<span><i class='bi bi-shield-check text-warning me-1'></i>Risque " . $riskBadge . "</span>";
        $html .= "</div>";

        $html .= "<h6 class='text-white-50 text-uppercase' style='font-size: 0.75rem; letter-spacing: 1px;'>Pourquoi c'est un bon choix :</h6>";
        $html .= "<ul class='mb-0 ps-3 text-white' style='font-size: 0.9rem;'>";
        
        if ($category && str_contains(strtolower($bestMatch['category'] ?? ''), strtolower($category))) {
            $html .= "<li class='mb-1'><b class='text-success'>Alignement Fort :</b> Ce projet s'inscrit parfaitement dans la catégorie " . ($bestMatch['category'] ?? '') . ".</li>";
        } else {
            $html .= "<li class='mb-1'><b class='text-info'>Choix Alternatif :</b> L'investissement s'est porté sur " . ($bestMatch['category'] ?? '') . " car c'est la seule opportunité s'approchant de vos contraintes financières.</li>";
        }
        
        if ($budget && $bestMatch['estimated_value'] <= $budget) {
            $html .= "<li><b class='text-success'>Budget Optimisé :</b> La valorisation requise (" . number_format($bestMatch['estimated_value'], 0, ',', ' ') . " $) respecte votre limite.</li>";
        }
        
        $html .= "</ul>";
        $html .= "</div>";

        // Alternative
        if (count($investments) > 1) {
            $alt = $investments[1]['investment'];
            $altRiskBadge = $riskLabels[$alt['risk_level']] ?? $alt['risk_level'];
            
            $html .= "<p class='mb-2 text-white-50 small'>Option alternative à considérer :</p>";
            $html .= "<div class='d-flex align-items-center p-2 rounded-3' style='background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.1); font-size: 0.85rem;'>";
            $html .= "<i class='bi bi-arrow-right-circle text-secondary me-2'></i>";
            $html .= "<span class='text-white me-auto'><b>" . htmlspecialchars($alt['name']) . "</b></span>";
            $html .= "<span>" . $altRiskBadge . "</span>";
            $html .= "</div>";
        }

        $html .= "<div class='mt-3 small text-muted font-monospace' style='font-size: 0.75rem;'>Avertissement : Les performances passées ne préjugent pas des performances futures. FINORA ne fournit aucune garantie financière.</div>";

        return $html;
    }
}
