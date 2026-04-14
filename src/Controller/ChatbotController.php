<?php

namespace App\Controller;

use App\Entity\Lesson;
use App\Service\LessonChatbotService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

final class ChatbotController extends AbstractController
{
    #[Route('/api/chatbot/lesson/{id}', name: 'api_chatbot_lesson', methods: ['POST'])]
    public function chat(
        int $id,
        Request $request,
        EntityManagerInterface $em,
        LessonChatbotService $chatbot
    ): JsonResponse {
        $lesson = $em->getRepository(Lesson::class)->find($id);

        if (!$lesson) {
            return new JsonResponse(['error' => 'Leçon introuvable.'], 404);
        }

        $data = json_decode($request->getContent(), true);

        if (!isset($data['message']) || trim($data['message']) === '') {
            return new JsonResponse(['error' => 'Le message est vide.'], 400);
        }

        $userMessage = trim($data['message']);
        $history = $data['history'] ?? [];
        $lang = $data['lang'] ?? 'fr';

        // Whitelist allowed languages
        $allowedLangs = ['fr', 'en', 'ar', 'es', 'de', 'it', 'zh', 'tr', 'pt', 'ru', 'ja'];
        if (!in_array($lang, $allowedLangs, true)) {
            $lang = 'fr';
        }

        $reply = $chatbot->chat(
            $lesson->getTitre() ?? '',
            $lesson->getContenu() ?? '',
            $history,
            $userMessage,
            $lang
        );

        return new JsonResponse([
            'reply' => $reply,
        ]);
    }
}
