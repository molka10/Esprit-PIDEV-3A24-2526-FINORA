<?php

namespace App\Controller;

use App\Service\ChatbotApiService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Session\SessionInterface;
use Symfony\Component\Routing\Attribute\Route;

/**
 * 🤖 ChatbotApiController - Chatbot avec API externe (sans base de données)
 */
#[Route('/chatbot')]
class ChatbotApiController extends AbstractController
{
    public function __construct(
        private ChatbotApiService $chatbotService
    ) {}

    /**
     * 💬 Interface du chatbot
     */
    #[Route('/', name: 'app_chatbot')]
    public function index(SessionInterface $session): Response
    {
        // Récupérer l'historique de la session (optionnel)
        $historique = $session->get('chatbot_historique', []);

        return $this->render('chatbot/index.html.twig', [
            'historique' => $historique,
        ]);
    }

    /**
     * 📤 Envoyer un message (AJAX)
     */
    #[Route('/message', name: 'app_chatbot_message', methods: ['POST'])]
    public function envoyerMessage(
        Request $request,
        SessionInterface $session
    ): JsonResponse {
        
        $message = $request->request->get('message');

        if (empty($message)) {
            return $this->json(['error' => 'Message vide'], 400);
        }

        try {
            // Récupérer l'historique de conversation
            $historique = $session->get('chatbot_historique', []);

            // Envoyer le message à l'API
            $reponse = $this->chatbotService->envoyerMessage($message, $historique);

            // Sauvegarder dans l'historique de session
            $historique[] = [
                'role' => 'user',
                'content' => $message,
                'timestamp' => time()
            ];
            $historique[] = [
                'role' => 'assistant',
                'content' => $reponse['content'] ?? '...',
                'timestamp' => time()
            ];

            // Limiter l'historique à 20 messages
            if (count($historique) > 20) {
                $historique = array_slice($historique, -20);
            }

            $session->set('chatbot_historique', $historique);

            return $this->json([
                'success' => true,
                'message' => $reponse['content'],
                'suggestions' => $reponse['suggestions'] ?? [],
                'actions' => $reponse['actions'] ?? [],
                'timestamp' => date('H:i')
            ]);

        } catch (\Exception $e) {
            return $this->json([
                'error' => 'Erreur lors de l\'envoi du message',
                'details' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * 🔄 Nouvelle conversation (vide l'historique)
     */
    #[Route('/nouvelle', name: 'app_chatbot_nouvelle', methods: ['POST'])]
    public function nouvelleConversation(SessionInterface $session): JsonResponse
    {
        $session->remove('chatbot_historique');

        return $this->json([
            'success' => true,
            'message' => 'Nouvelle conversation démarrée'
        ]);
    }

    /**
     * 📋 Récupérer l'historique
     */
    #[Route('/historique', name: 'app_chatbot_historique', methods: ['GET'])]
    public function historique(SessionInterface $session): JsonResponse
    {
        $historique = $session->get('chatbot_historique', []);

        return $this->json([
            'historique' => $historique
        ]);
    }

    /**
     * 🗑️ Effacer l'historique
     */
    #[Route('/effacer', name: 'app_chatbot_effacer', methods: ['POST'])]
    public function effacer(SessionInterface $session): JsonResponse
    {
        $session->remove('chatbot_historique');

        return $this->json([
            'success' => true,
            'message' => 'Historique effacé'
        ]);
    }
}