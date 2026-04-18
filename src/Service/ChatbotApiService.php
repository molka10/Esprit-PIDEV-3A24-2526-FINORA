<?php

namespace App\Service;

use App\Repository\BourseRepository;
use App\Repository\ActionRepository;
use App\Repository\TransactionBourseRepository;

use Symfony\Component\DependencyInjection\Attribute\Autowire;

/**
 * 🤖 ChatbotService Amélioré - Intégré avec FINORA
 */
class ChatbotApiService
{
    public function __construct(
        private BourseRepository $bourseRepo,
        private ActionRepository $actionRepo,
        private TransactionBourseRepository $transactionRepo,
        private \Symfony\Contracts\HttpClient\HttpClientInterface $client,
        #[Autowire(param: 'ai_api_key')]
        private string $aiApiKey
    ) {}

    /**
     * Génère une réponse intelligente basée sur le contexte FINORA
     */
    public function genererReponse(string $message, array $historique = []): array
    {
        $messageLower = strtolower($message);

        // Détection d'intention et génération de réponse structurée
        if ($this->contient($messageLower, ['liste', 'voir', 'toutes', 'bourses'])) {
            return $this->listerBourses();
        }

        if ($this->contient($messageLower, ['actions', 'liste actions', 'voir actions'])) {
            return $this->listerActions();
        }

        if ($this->contient($messageLower, ['prix', 'cotation', 'cours']) && $this->contient($messageLower, ['aapl', 'apple'])) {
            return $this->afficherPrixAction('AAPL');
        }

        if ($this->contient($messageLower, ['prix', 'cotation', 'cours']) && $this->contient($messageLower, ['googl', 'google'])) {
            return $this->afficherPrixAction('GOOGL');
        }

        if ($this->contient($messageLower, ['prix', 'cotation', 'cours']) && $this->contient($messageLower, ['msi'])) {
            return $this->afficherPrixAction('MSI');
        }

        if ($this->contient($messageLower, ['statistiques', 'stats', 'résumé', 'bilan'])) {
            return $this->afficherStatistiques();
        }

        if ($this->contient($messageLower, ['transactions', 'historique', 'opérations'])) {
            return $this->afficherTransactionsRecentes();
        }

        if ($this->contient($messageLower, ['comment', 'acheter'])) {
            return $this->guideAchat();
        }

        if ($this->contient($messageLower, ['comment', 'vendre'])) {
            return $this->guideVente();
        }

        if ($this->contient($messageLower, ['commission', 'frais'])) {
            return $this->expliquerCommissions();
        }

        if ($this->contient($messageLower, ['aide', 'help', 'commandes'])) {
            return $this->afficherAide();
        }

        // Salutations
        if ($this->contient($messageLower, ['bonjour', 'salut', 'hello', 'hey'])) {
            return [
                'type' => 'text',
                'content' => "👋 Bonjour ! Je suis votre assistant FINORA. Je peux vous aider avec :\n\n• Les bourses et actions\n• Les prix en temps réel\n• L'historique de vos transactions\n• Les commissions\n\nQue souhaitez-vous savoir ?",
                'suggestions' => ['Voir les actions', 'Mes statistiques', 'Comment acheter ?']
            ];
        }

        // Réponse par défaut -> Appel API LLM (Gemini)
        return $this->appelerLLM($message, $historique);
    }

    /**
     * Appel à l'API Gemini pour une réponse intelligente
     */
    private function appelerLLM(string $message, array $historique = []): array
    {
        if (empty($this->aiApiKey) || $this->aiApiKey === 'your_gemini_api_key_here') {
            return [
                'type' => 'text',
                'content' => "🤖 **Assistant en mode local.**\n\nPour activer l'IA complète, veuillez configurer la clé API dans le fichier `.env`. En attendant, je peux vous aider avec les bourses, les prix et vos transactions.",
                'suggestions' => ['Liste des bourses', 'Voir les actions']
            ];
        }

        try {
            // Préparation du contexte (Données réelles du marché)
            $actions = $this->actionRepo->findAvailable();
            $contexteMarche = "Voici les actions actuellement disponibles sur FINORA :\n";
            foreach (array_slice($actions, 0, 5) as $a) {
                $contexteMarche .= "- {$a->getSymbole()} ({$a->getNomEntreprise()}): {$a->getPrixUnitaire()} TND\n";
            }

            $systemPrompt = "Tu es l'assistant intelligent de FINORA, une plateforme de gestion boursière. 
            Tes réponses doivent être professionnelles, concises et en français.
            Utilise les données du marché suivantes si besoin : {$contexteMarche}
            Si on te demande d'acheter ou vendre, explique la procédure (Trading -> Acheter/Vendre).";

            $contents = [
                ['role' => 'user', 'parts' => [['text' => $systemPrompt]]]
            ];

            // Ajout de l'historique
            foreach (array_slice($historique, -5) as $h) {
                $contents[] = [
                    'role' => ($h['role'] === 'assistant' ? 'model' : 'user'),
                    'parts' => [['text' => $h['content']]]
                ];
            }

            // Message actuel
            $contents[] = [
                'role' => 'user',
                'parts' => [['text' => $message]]
            ];

            $response = $this->client->request('POST', 'https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=' . $this->aiApiKey, [
                'json' => ['contents' => $contents]
            ]);

            $data = $response->toArray();
            $rawText = $data['candidates'][0]['content']['parts'][0]['text'] ?? "Désolé, je ne peux pas répondre pour le moment.";

            return [
                'type' => 'text',
                'content' => $rawText,
                'suggestions' => ['Mes statistiques', 'Comment acheter ?']
            ];

        } catch (\Exception $e) {
            return [
                'type' => 'text',
                'content' => "Désolé, une erreur est survenue lors de la communication avec l'IA. " . $e->getMessage(),
                'suggestions' => ['Réessayer', 'Aide']
            ];
        }
    }

    /**
     * Liste toutes les bourses
     */
    private function listerBourses(): array
    {
        $bourses = $this->bourseRepo->findAll();

        if (empty($bourses)) {
            return [
                'type' => 'text',
                'content' => "❌ Aucune bourse n'est disponible pour le moment.",
            ];
        }

        $content = "📊 **Liste des Bourses** (" . count($bourses) . ")\n\n";

        foreach ($bourses as $bourse) {
            $statut = $bourse->getStatut() === 'ACTIVE' ? '✅' : '❌';
            $content .= "• {$statut} **{$bourse->getNomBourse()}** - {$bourse->getPays()} ({$bourse->getDevise()})\n";
        }

        return [
            'type' => 'text',
            'content' => $content,
            'suggestions' => ['Voir les actions', 'Mes statistiques']
        ];
    }

    /**
     * Liste toutes les actions disponibles
     */
    private function listerActions(): array
    {
        $actions = $this->actionRepo->findAvailable();

        if (empty($actions)) {
            return [
                'type' => 'text',
                'content' => "❌ Aucune action disponible pour le moment.",
            ];
        }

        $content = "📈 **Actions Disponibles** (" . count($actions) . ")\n\n";

        foreach (array_slice($actions, 0, 10) as $action) {
            $content .= "• **{$action->getSymbole()}** - {$action->getNomEntreprise()}\n";
            $content .= "  💰 Prix: {$action->getPrixUnitaire()} TND | Stock: {$action->getQuantiteDisponible()}\n\n";
        }

        if (count($actions) > 10) {
            $content .= "\n_... et " . (count($actions) - 10) . " autres actions_";
        }

        return [
            'type' => 'text',
            'content' => $content,
            'suggestions' => ['Prix de AAPL', 'Comment acheter ?']
        ];
    }

    /**
     * Affiche le prix d'une action spécifique
     */
    private function afficherPrixAction(string $symbole): array
    {
        $action = $this->actionRepo->findOneBy(['symbole' => $symbole]);

        if (!$action) {
            return [
                'type' => 'text',
                'content' => "❌ L'action **{$symbole}** n'existe pas dans notre base.",
                'suggestions' => ['Voir les actions disponibles']
            ];
        }

        $statut = $action->getStatut() === 'DISPONIBLE' ? '✅ Disponible' : '❌ Indisponible';

        $content = "📊 **{$action->getSymbole()}** - {$action->getNomEntreprise()}\n\n";
        $content .= "💰 **Prix:** {$action->getPrixUnitaire()} TND\n";
        $content .= "📦 **Stock:** {$action->getQuantiteDisponible()} actions\n";
        $content .= "🏢 **Secteur:** {$action->getSecteur()}\n";
        $content .= "📍 **Statut:** {$statut}\n";

        return [
            'type' => 'text',
            'content' => $content,
            'actions' => [
                ['label' => '🛒 Acheter', 'url' => '/trading/acheter?action=' . $action->getId()],
                ['label' => '💰 Vendre', 'url' => '/trading/vendre?action=' . $action->getId()]
            ]
        ];
    }

    /**
     * Affiche les statistiques globales
     */
    private function afficherStatistiques(): array
    {
        $nbBourses = $this->bourseRepo->count([]);
        $nbActions = $this->actionRepo->count([]);
        $nbTransactions = $this->transactionRepo->count([]);

        $statsActions = $this->actionRepo->getStatistics();
        $statsTransactions = $this->transactionRepo->getStatistics();

        $content = "📊 **Statistiques FINORA**\n\n";
        $content .= "🏛️ Bourses: **{$nbBourses}**\n";
        $content .= "📈 Actions: **{$nbActions}** ({$statsActions['disponibles']} disponibles)\n";
        $content .= "💼 Transactions: **{$nbTransactions}**\n";
        $content .= "💰 Volume total: **" . number_format($statsTransactions['volume_total'] ?? 0, 2) . " TND**\n";

        return [
            'type' => 'text',
            'content' => $content,
            'suggestions' => ['Voir les actions', 'Mes transactions']
        ];
    }

    /**
     * Affiche les dernières transactions
     */
    private function afficherTransactionsRecentes(): array
    {
        $transactions = $this->transactionRepo->findRecent(5);

        if (empty($transactions)) {
            return [
                'type' => 'text',
                'content' => "❌ Aucune transaction enregistrée.",
            ];
        }

        $content = "📜 **Dernières Transactions**\n\n";

        foreach ($transactions as $transaction) {
            $type = $transaction->getTypeTransaction();
            $icon = $type === 'ACHAT' ? '🟢' : '🔴';
            
            $content .= "{$icon} **{$type}** - {$transaction->getAction()->getSymbole()}\n";
            $content .= "  Qté: {$transaction->getQuantite()} | Montant: {$transaction->getMontantTotal()} TND\n";
            $content .= "  _" . $transaction->getDateTransaction()->format('d/m/Y H:i') . "_\n\n";
        }

        return [
            'type' => 'text',
            'content' => $content,
            'actions' => [
                ['label' => '📜 Voir tout l\'historique', 'url' => '/trading/historique']
            ]
        ];
    }
    public function envoyerMessage(string $message, array $historique = []): array
    {
        return $this->genererReponse($message, $historique);
    }

    /**
     * Guide d'achat
     */
    private function guideAchat(): array
    {
        return [
            'type' => 'text',
            'content' => "🛒 **Comment Acheter des Actions ?**\n\n1. Allez sur **Trading → Acheter**\n2. Sélectionnez l'action souhaitée\n3. Entrez la quantité (vérifiez le stock disponible)\n4. Consultez le montant total + commission\n5. Confirmez l'achat\n\n💡 _La commission est de 1% (min 5 TND, max 100 TND)_",
            'actions' => [
                ['label' => '🛒 Aller à l\'achat', 'url' => '/trading/acheter']
            ]
        ];
    }

    /**
     * Guide de vente
     */
    private function guideVente(): array
    {
        return [
            'type' => 'text',
            'content' => "💰 **Comment Vendre des Actions ?**\n\n1. Allez sur **Trading → Vendre**\n2. Sélectionnez l'action à vendre\n3. Entrez la quantité (maximum = votre portefeuille)\n4. Consultez le montant que vous recevrez (après commission)\n5. Confirmez la vente\n\n💡 _La commission est déduite du montant de vente_",
            'actions' => [
                ['label' => '💰 Aller à la vente', 'url' => '/trading/vendre']
            ]
        ];
    }

    /**
     * Explique le système de commissions
     */
    private function expliquerCommissions(): array
    {
        return [
            'type' => 'text',
            'content' => "💰 **Système de Commissions**\n\n📌 **Taux standard:** 1% du montant\n📌 **Minimum:** 5 TND\n📌 **Maximum:** 100 TND\n\n**Taux préférentiels:**\n• AAPL, GOOGL: 0.5%\n• TSLA: 1.5%\n\n**Exemple:**\nAchat de 10 actions à 100 TND\n• Montant: 1000 TND\n• Commission: 10 TND (1%)\n• **Total: 1010 TND**",
        ];
    }

    /**
     * Affiche l'aide
     */
    private function afficherAide(): array
    {
        return [
            'type' => 'text',
            'content' => "🤖 **Commandes disponibles:**\n\n📊 **Données**\n• 'Liste des bourses'\n• 'Voir les actions'\n• 'Prix de [SYMBOLE]'\n• 'Mes statistiques'\n\n💼 **Trading**\n• 'Comment acheter ?'\n• 'Comment vendre ?'\n• 'Mes transactions'\n\n💡 **Infos**\n• 'Commissions'\n• 'Aide'",
            'suggestions' => ['Voir les actions', 'Mes statistiques', 'Prix de AAPL']
        ];
    }

    /**
     * Vérifie si un message contient des mots-clés
     */
    private function contient(string $haystack, array $needles): bool
    {
        foreach ($needles as $needle) {
            if (str_contains($haystack, $needle)) {
                return true;
            }
        }
        return false;
    }
}