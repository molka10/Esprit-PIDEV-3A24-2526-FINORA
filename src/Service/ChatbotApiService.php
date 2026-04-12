<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

/**
 * 🤖 ChatbotApiService - Service d'appel à une API de chatbot externe
 * 
 * Supporte : OpenAI GPT, Claude API, ou toute autre API REST
 */
class ChatbotApiService
{
    private string $apiKey;
    private string $apiUrl;
    private string $model;

    public function __construct(
        private HttpClientInterface $httpClient
    ) {
        // Configuration API (à mettre dans .env)
        $this->apiKey = $_ENV['CHATBOT_API_KEY'] ?? '';
        $this->apiUrl = $_ENV['CHATBOT_API_URL'] ?? 'https://api.openai.com/v1/chat/completions';
        $this->model = $_ENV['CHATBOT_MODEL'] ?? 'gpt-3.5-turbo';
    }

    /**
     * Envoie un message à l'API et récupère la réponse
     * 
     * @param string $message Message de l'utilisateur
     * @param array $historique Historique de conversation (optionnel)
     * @return string Réponse du chatbot
     */
    public function envoyerMessage(string $message, array $historique = []): string
    {
        try {
            // Construction des messages pour l'API
            $messages = $this->construireMessages($message, $historique);

            // Appel à l'API
            $response = $this->httpClient->request('POST', $this->apiUrl, [
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                    'Content-Type' => 'application/json',
                ],
                'json' => [
                    'model' => $this->model,
                    'messages' => $messages,
                    'temperature' => 0.7,
                    'max_tokens' => 500,
                ]
            ]);

            $data = $response->toArray();

            // Extraire la réponse
            return $data['choices'][0]['message']['content'] ?? 'Désolé, je n\'ai pas pu générer de réponse.';

        } catch (\Exception $e) {
            // En cas d'erreur, utiliser réponses prédéfinies
            return $this->getReponseParDefaut($message);
        }
    }

    /**
     * Construit le tableau de messages pour l'API
     */
    private function construireMessages(string $message, array $historique): array
    {
        $messages = [
            [
                'role' => 'system',
                'content' => 'Tu es un assistant virtuel pour FINORA, une plateforme de gestion boursière et de devises. Tu aides les utilisateurs avec leurs questions sur les actions, les transactions, les conversions de devises et l\'utilisation de la plateforme.'
            ]
        ];

        // Ajouter l'historique (optionnel)
        foreach ($historique as $msg) {
            $messages[] = [
                'role' => $msg['role'], // 'user' ou 'assistant'
                'content' => $msg['content']
            ];
        }

        // Ajouter le message actuel
        $messages[] = [
            'role' => 'user',
            'content' => $message
        ];

        return $messages;
    }

    /**
     * Réponses prédéfinies (fallback si API ne répond pas)
     */
    private function getReponseParDefaut(string $message): string
    {
        $messageLower = strtolower($message);

        $reponses = [
            'bourse' => "Je peux vous aider avec les actions boursières ! Consultez le tableau de bord pour voir le marché en temps réel.",
            'action' => "Pour acheter ou vendre des actions, rendez-vous dans l'interface de trading.",
            'prix' => "Les prix sont mis à jour en temps réel. Consultez le marché pour voir les dernières cotations.",
            'acheter' => "Pour acheter : Trading → Sélectionnez l'action → Entrez la quantité → Confirmez.",
            'vendre' => "Pour vendre : Trading → Vendre → Sélectionnez vos actions → Confirmez.",
            'commission' => "Les commissions sont de 1% (min 5 TND, max 100 TND). Certaines actions ont des taux préférentiels.",
            'devise' => "Je peux vous aider avec les conversions de devises. Accédez à la section Exchange.",
            'taux' => "Les taux de change sont mis à jour régulièrement dans la section Exchange.",
            'historique' => "Consultez votre historique dans la section Transactions pour voir tous vos achats et ventes.",
            'bonjour' => "Bonjour ! Je suis votre assistant FINORA. Comment puis-je vous aider ?",
            'merci' => "De rien ! N'hésitez pas si vous avez d'autres questions.",
            'aide' => "Je peux vous aider avec : la bourse, les actions, les devises, vos transactions et les commissions. Que souhaitez-vous savoir ?",
        ];

        foreach ($reponses as $mot => $reponse) {
            if (str_contains($messageLower, $mot)) {
                return $reponse;
            }
        }

        return "Je suis là pour vous aider avec FINORA ! Posez-moi des questions sur la bourse, les actions, les devises ou vos transactions.";
    }

    /**
     * Version simplifiée : réponses prédéfinies uniquement (sans API)
     * Utile pour développement ou si pas d'API key
     */
    public function getReponseSimple(string $message): string
    {
        return $this->getReponseParDefaut($message);
    }
}