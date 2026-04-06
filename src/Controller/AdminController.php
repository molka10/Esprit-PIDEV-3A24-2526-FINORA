<?php

namespace App\Controller;

use App\Entity\Formation;
use App\Entity\Lesson;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

final class AdminController extends AbstractController
{
    #[Route('/admin', name: 'admin_dashboard')]
    public function index(EntityManagerInterface $entityManager): Response
    {
        $formationRepository = $entityManager->getRepository(Formation::class);
        $lessonRepository = $entityManager->getRepository(Lesson::class);

        $formations = $formationRepository->findAll();
        $lessons = $lessonRepository->findAll();

        $totalFormations = count($formations);
        $totalLessons = count($lessons);

        $publishedFormations = 0;
        foreach ($formations as $formation) {
            if ($formation->getIsPublished() === 1) {
                $publishedFormations++;
            }
        }

        $totalDuration = 0;
        foreach ($lessons as $lesson) {
            $totalDuration += $lesson->getDureeMinutes() ?? 0;
        }

        $averageLessonDuration = $totalLessons > 0
            ? round($totalDuration / $totalLessons, 1)
            : 0;

        return $this->render('admin/dashboard.html.twig', [
            'totalFormations' => $totalFormations,
            'totalLessons' => $totalLessons,
            'publishedFormations' => $publishedFormations,
            'averageLessonDuration' => $averageLessonDuration,
        ]);
    }
}