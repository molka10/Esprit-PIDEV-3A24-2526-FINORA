<?php

namespace App\Controller;

use App\Entity\Lesson;
use App\Service\TextToSpeechService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\BinaryFileResponse;
use Symfony\Component\Routing\Attribute\Route;

final class TextToSpeechController extends AbstractController
{
    #[Route('/tts/lesson/{id}', name: 'app_tts')]
    public function speakLesson(
        int $id,
        EntityManagerInterface $em,
        TextToSpeechService $tts
    ): BinaryFileResponse {
        $lesson = $em->getRepository(Lesson::class)->find($id);

        if (!$lesson) {
            throw $this->createNotFoundException();
        }

        $content = trim((string) $lesson->getContenu());

        if ($content === '') {
            throw new \RuntimeException('Contenu vide');
        }

        $dir = $this->getParameter('kernel.project_dir') . '/public/uploads/tts';

        if (!is_dir($dir)) {
            mkdir($dir, 0777, true);
        }

        $filePath = $dir . '/lesson_' . $lesson->getId() . '.wav';

        // generate only once
        $tts->generateAndSave($content, $filePath);

        return new BinaryFileResponse($filePath);
    }
}