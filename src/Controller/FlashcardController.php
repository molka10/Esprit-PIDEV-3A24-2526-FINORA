<?php

namespace App\Controller;

use App\Entity\Lesson;
use App\Entity\Flashcard;
use App\Service\FlashcardGeneratorService;
use App\Repository\FlashcardRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Annotation\Route;

class FlashcardController extends AbstractController
{
    #[Route('/api/flashcards/lesson/{id}', name: 'api_flashcards_get', methods: ['GET'])]
    public function getFlashcards(Lesson $lesson, FlashcardRepository $repo, FlashcardGeneratorService $generator): JsonResponse
    {
        $flashcards = $repo->findBy(['lesson' => $lesson]);

        if (empty($flashcards)) {
            $flashcards = $generator->generateForLesson($lesson);
        }

        $data = array_map(fn($f) => [
            'id' => $f->getId(),
            'q' => $f->getQuestion(),
            'a' => $f->getAnswer()
        ], $flashcards);

        return $this->json($data);
    }

    #[Route('/api/flashcards/lesson/{id}/regenerate', name: 'api_flashcards_regenerate', methods: ['POST'])]
    public function regenerate(Lesson $lesson, FlashcardRepository $repo, FlashcardGeneratorService $generator, EntityManagerInterface $em): JsonResponse
    {
        $existing = $repo->findBy(['lesson' => $lesson]);
        foreach ($existing as $f) {
            $em->remove($f);
        }
        $em->flush();

        $flashcards = $generator->generateForLesson($lesson);
        $data = array_map(fn($f) => [
            'id' => $f->getId(),
            'q' => $f->getQuestion(),
            'a' => $f->getAnswer()
        ], $flashcards);

        return $this->json($data);
    }
}
