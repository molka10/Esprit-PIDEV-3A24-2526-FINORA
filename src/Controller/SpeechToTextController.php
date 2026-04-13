<?php

namespace App\Controller;

use App\Service\SpeechToTextService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

final class SpeechToTextController extends AbstractController
{
    #[Route('/api/speech-to-text', name: 'api_stt', methods: ['POST'])]
    public function transcribe(
        Request $request,
        SpeechToTextService $speechToTextService
    ): JsonResponse {
        try {
            $audio = $request->getContent();

            if (!$audio || strlen($audio) === 0) {
                return new JsonResponse([
                    'success' => false,
                    'error' => 'Aucun audio reçu.',
                ], 400);
            }

            $contentType = (string) ($request->headers->get('Content-Type') ?? 'audio/webm');
            $text = $speechToTextService->transcribe($audio, $contentType);

            return new JsonResponse([
                'success' => true,
                'text' => $text,
            ]);
        } catch (\Throwable $e) {
            return new JsonResponse([
                'success' => false,
                'error' => 'STT backend: ' . $e->getMessage(),
            ], 500);
        }
    }
}