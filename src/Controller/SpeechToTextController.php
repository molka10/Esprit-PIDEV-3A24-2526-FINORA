<?php

namespace App\Controller;

use App\Service\SpeechToTextService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

class SpeechToTextController extends AbstractController
{
    #[Route('/api/speech-to-text', name: 'api_stt', methods: ['POST'])]
    public function transcribe(Request $request, SpeechToTextService $stt): JsonResponse
    {
        $audio = $request->getContent();

        if (!$audio) {
            return new JsonResponse(['error' => 'No audio'], 400);
        }

        $text = $stt->transcribe($audio);

        return new JsonResponse([
            'text' => $text
        ]);
    }
}