<?php

namespace App\Controller;

use App\Entity\Formation;
use App\Entity\Lesson;
use App\Form\LessonType;
use App\Service\YouTubeSearchService;
use Doctrine\ORM\EntityManagerInterface;
use Nucleos\DompdfBundle\Wrapper\DompdfWrapperInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/lesson')]
final class LessonController extends AbstractController
{
    #[Route(name: 'app_lesson_index', methods: ['GET'])]
    public function index(Request $request, EntityManagerInterface $entityManager): Response
    {
        $titre = trim((string) $request->query->get('titre', ''));
        $formationId = trim((string) $request->query->get('formation', ''));
        $tri = (string) $request->query->get('tri', 'ordre');
        $ordreTri = strtolower((string) $request->query->get('ordre', 'asc'));

        $qb = $entityManager->getRepository(Lesson::class)
            ->createQueryBuilder('l')
            ->leftJoin('l.formation', 'f')
            ->addSelect('f');

        if ($titre !== '') {
            $qb->andWhere('LOWER(l.titre) LIKE LOWER(:titre) OR LOWER(l.contenu) LIKE LOWER(:titre)')
                ->setParameter('titre', '%' . $titre . '%');
        }

        if ($formationId !== '') {
            $qb->andWhere('f.id = :formationId')
                ->setParameter('formationId', $formationId);
        }

        $allowedSortFields = [
            'id' => 'l.id',
            'titre' => 'l.titre',
            'ordre' => 'l.ordre',
            'dureeMinutes' => 'l.dureeMinutes',
        ];

        if (!array_key_exists($tri, $allowedSortFields)) {
            $tri = 'ordre';
        }

        $ordreTri = $ordreTri === 'desc' ? 'DESC' : 'ASC';
        $qb->orderBy($allowedSortFields[$tri], $ordreTri);

        $lessons = $qb->getQuery()->getResult();
        $formations = $entityManager->getRepository(Formation::class)->findAll();

        return $this->render('lesson/index.html.twig', [
            'lessons' => $lessons,
            'formations' => $formations,
            'titre' => $titre,
            'formationSelected' => $formationId,
            'tri' => $tri,
            'ordre' => strtolower($ordreTri),
        ]);
    }

    #[Route('/new', name: 'app_lesson_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $lesson = new Lesson();
        $form = $this->createForm(LessonType::class, $lesson);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($lesson);
            $entityManager->flush();

            $this->addFlash('success', 'La lesson a été ajoutée avec succès.');

            return $this->redirectToRoute('app_lesson_index');
        }

        return $this->render('lesson/new.html.twig', [
            'lesson' => $lesson,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_lesson_show', methods: ['GET'])]
    public function show(Lesson $lesson): Response
    {
        return $this->render('lesson/show.html.twig', [
            'lesson' => $lesson,
        ]);
    }

    #[Route('/{id}/youtube/manage', name: 'app_lesson_youtube_manage', methods: ['GET'])]
    public function youtubeManage(
        Lesson $lesson,
        Request $request,
        YouTubeSearchService $youTubeSearchService
    ): Response {
        $query = trim((string) $request->query->get('q', (string) $lesson->getTitre()));
        $sort = (string) $request->query->get('sort', 'relevance');

        $results = [];
        $error = null;

        if ($query !== '') {
            try {
                $results = $youTubeSearchService->search($query, 8, $sort);
            } catch (\Throwable $e) {
                $error = $e->getMessage();
            }
        }

        return $this->render('lesson/youtube_manage.html.twig', [
            'lesson' => $lesson,
            'query' => $query,
            'sort' => $sort,
            'results' => $results,
            'error' => $error,
        ]);
    }

    #[Route('/{id}/youtube/link', name: 'app_lesson_youtube_link', methods: ['POST'])]
    public function linkYoutubeVideo(
        Lesson $lesson,
        Request $request,
        EntityManagerInterface $entityManager
    ): Response {
        if (!$this->isCsrfTokenValid('link_youtube_' . $lesson->getId(), (string) $request->request->get('_token'))) {
            throw $this->createAccessDeniedException('Jeton CSRF invalide.');
        }

        $videoUrl = trim((string) $request->request->get('video_url', ''));

        if ($videoUrl === '') {
            $this->addFlash('danger', 'Aucune vidéo YouTube sélectionnée.');
            return $this->redirectToRoute('app_lesson_youtube_manage', ['id' => $lesson->getId()]);
        }

        $lesson->setVideoUrl($videoUrl);
        $entityManager->flush();

        $this->addFlash('success', 'La vidéo YouTube a été associée à la lesson.');

        return $this->redirectToRoute('app_lesson_edit', ['id' => $lesson->getId()]);
    }

    #[Route('/{id}/youtube/remove', name: 'app_lesson_youtube_remove', methods: ['POST'])]
    public function removeYoutubeVideo(
        Lesson $lesson,
        Request $request,
        EntityManagerInterface $entityManager
    ): Response {
        if (!$this->isCsrfTokenValid('remove_youtube_' . $lesson->getId(), (string) $request->request->get('_token'))) {
            throw $this->createAccessDeniedException('Jeton CSRF invalide.');
        }

        $lesson->setVideoUrl(null);
        $entityManager->flush();

        $this->addFlash('success', 'La vidéo associée a été supprimée.');

        return $this->redirectToRoute('app_lesson_edit', ['id' => $lesson->getId()]);
    }

    #[Route('/{id}/pdf', name: 'app_lesson_pdf', methods: ['GET'])]
    public function exportPdf(Lesson $lesson, DompdfWrapperInterface $dompdfWrapper): Response
    {
        $html = $this->renderView('lesson/pdf.html.twig', [
            'lesson' => $lesson,
        ]);

        return $dompdfWrapper->getStreamResponse($html, 'lesson-' . $lesson->getId() . '.pdf');
    }

    #[Route('/{id}/edit', name: 'app_lesson_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Lesson $lesson, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(LessonType::class, $lesson);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            $this->addFlash('success', 'La lesson a été mise à jour avec succès.');

            return $this->redirectToRoute('app_lesson_index');
        }

        return $this->render('lesson/edit.html.twig', [
            'lesson' => $lesson,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_lesson_delete', methods: ['POST'])]
    public function delete(Request $request, Lesson $lesson, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete' . $lesson->getId(), (string) $request->request->get('_token'))) {
            $entityManager->remove($lesson);
            $entityManager->flush();

            $this->addFlash('success', 'La lesson a été supprimée.');
        }

        return $this->redirectToRoute('app_lesson_index');
    }
    #[Route('/{id}/videos', name: 'app_lesson_related_videos', methods: ['GET'])]
public function relatedVideos(
    Lesson $lesson,
    YouTubeSearchService $youTubeSearchService
): Response {
    $results = [];
    $error = null;

    try {
        $results = $youTubeSearchService->search($lesson->getTitre(), 6);
    } catch (\Throwable $e) {
        $error = $e->getMessage();
    }

    return $this->render('lesson/related_videos.html.twig', [
        'lesson' => $lesson,
        'results' => $results,
        'error' => $error,
    ]);
}
}