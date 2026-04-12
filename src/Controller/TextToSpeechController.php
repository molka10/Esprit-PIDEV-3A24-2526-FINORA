<?php

namespace App\Controller;

use App\Entity\Lesson;
use App\Service\TextToSpeechService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

final class TextToSpeechController extends AbstractController
{
    #[Route('/tts/lesson/{id}', name: 'app_tts', methods: ['GET'])]
    public function speakLesson(
        int $id,
        EntityManagerInterface $entityManager,
        TextToSpeechService $tts
    ): Response {
        $lesson = $entityManager->getRepository(Lesson::class)->find($id);

        if (!$lesson) {
            throw $this->createNotFoundException('Lesson introuvable.');
        }

        $content = trim((string) $lesson->getContenu());

        if ($content === '') {
            return new Response('Aucun contenu à lire.', Response::HTTP_BAD_REQUEST, [
                'Content-Type' => 'text/plain; charset=UTF-8',
            ]);
        }

        try {
            $audio = $tts->generateAudio($content);
        } catch (\Throwable $e) {
            return new Response('Erreur TTS: ' . $e->getMessage(), Response::HTTP_INTERNAL_SERVER_ERROR, [
                'Content-Type' => 'text/plain; charset=UTF-8',
            ]);
        }

        return new Response($audio, Response::HTTP_OK, [
            'Content-Type' => 'audio/wav',
            'Content-Disposition' => 'inline; filename="lesson-' . $lesson->getId() . '.wav"',
            'Cache-Control' => 'no-store, no-cache, must-revalidate, max-age=0',
        ]);
    }
}