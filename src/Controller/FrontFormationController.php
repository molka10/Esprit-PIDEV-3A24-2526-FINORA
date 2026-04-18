<?php

namespace App\Controller;

use App\Entity\Formation;
use App\Entity\Lesson;
use App\Entity\QuizResult;
use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

final class FrontFormationController extends AbstractController
{
    #[Route('/formation-home', name: 'app_formation_home')]
    public function index(EntityManagerInterface $entityManager): Response
    {
        $formations = $entityManager->getRepository(Formation::class)->findBy(['is_published' => 1]);
        $lessons = $entityManager->getRepository(Lesson::class)->findAll();

        return $this->render('home/index.html.twig', [
            'formations' => $formations,
            'lessons' => $lessons,
            'userBadges' => $this->getUserBadges($entityManager),
        ]);
    }

    #[Route('/formations', name: 'app_formations')]
    public function formations(
        Request $request,
        EntityManagerInterface $entityManager,
        PaginatorInterface $paginator
    ): Response {
        $titre = trim((string) $request->query->get('titre', ''));
        $categorie = trim((string) $request->query->get('categorie', ''));
        $niveau = trim((string) $request->query->get('niveau', ''));
        $tri = (string) $request->query->get('tri', 'id');
        $ordre = strtolower((string) $request->query->get('ordre', 'desc'));
        $mesFormations = $request->query->get('mes_formations') === '1';

        $user = $this->getUser();
        $purchasedIds = [];
        if ($user instanceof User) {
            foreach ($user->getPurchasedFormations() as $f) {
                $purchasedIds[] = $f->getId();
            }
        }

        $qb = $entityManager->getRepository(Formation::class)->createQueryBuilder('f')
            ->where('f.is_published = 1');

        if ($titre !== '') {
            $qb->andWhere('LOWER(f.titre) LIKE LOWER(:titre)')
                ->setParameter('titre', '%' . $titre . '%');
        }

        if ($categorie !== '') {
            $qb->andWhere('LOWER(f.categorie) LIKE LOWER(:categorie)')
                ->setParameter('categorie', '%' . $categorie . '%');
        }

        if ($niveau !== '') {
            $qb->andWhere('f.niveau = :niveau')
                ->setParameter('niveau', $niveau);
        }

        if ($mesFormations && $user instanceof User) {
            if (!empty($purchasedIds)) {
                $qb->andWhere('f.id IN (:pids)')
                    ->setParameter('pids', $purchasedIds);
            } else {
                $qb->andWhere('1 = 0'); // Return empty if no purchases
            }
        }

        $allowedSortFields = ['id', 'titre', 'categorie', 'niveau'];
        if (!in_array($tri, $allowedSortFields, true)) {
            $tri = 'id';
        }

        $ordre = $ordre === 'asc' ? 'ASC' : 'DESC';
        $qb->orderBy('f.' . $tri, $ordre);

        $query = $qb->getQuery();

        $formations = $paginator->paginate(
            $query,
            $request->query->getInt('page', 1),
            6
        );

        $categories = [
            'Bourse', 'Investissement', 'Trading', 'Actions', 'ETF', 'Obligations',
            'Crypto', 'Forex', 'Analyse technique', 'Analyse fondamentale',
            'Gestion des risques', 'Portefeuille', 'Dividendes', 'Marchés financiers',
            'Psychologie du trader', 'Économie', 'Inflation', 'Taux d’intérêt', 'Fiscalité',
        ];

        // 🏆 Bestsellers Logic: Order by number of purchases
        $bestsellers = $entityManager->getRepository(Formation::class)->createQueryBuilder('f')
            ->leftJoin('f.purchasedBy', 'p')
            ->select('f, COUNT(p.id) as HIDDEN pCount')
            ->where('f.is_published = 1')
            ->groupBy('f.id')
            ->orderBy('pCount', 'DESC')
            ->setMaxResults(3)
            ->getQuery()
            ->getResult();

        // 📈 Progress Logic
        $progressMap = [];
        if ($user instanceof User) {
            $quizResults = $entityManager->getRepository(QuizResult::class)->findBy([
                'user' => $user,
                'passed' => 1
            ]);
            
            $passedLessonIds = array_map(fn($r) => $r->getLessonId(), $quizResults);

            foreach ($formations as $formation) {
                $totalLessons = count($formation->getLessons());
                if ($totalLessons > 0) {
                    $passedCount = 0;
                    foreach ($formation->getLessons() as $lesson) {
                        if (in_array($lesson->getId(), $passedLessonIds)) {
                            $passedCount++;
                        }
                    }
                    $percent = round(($passedCount / $totalLessons) * 100);
                    $progressMap[$formation->getId()] = [
                        'passed' => $passedCount,
                        'total' => $totalLessons,
                        'percent' => $percent,
                        'isCompleted' => ($percent === 100)
                    ];
                }
            }
        }

        return $this->render('home/formations.html.twig', [
            'formations' => $formations,
            'titre' => $titre,
            'categorie' => $categorie,
            'niveau' => $niveau,
            'tri' => $tri,
            'ordre' => strtolower($ordre),
            'categories' => $categories,
            'mesFormations' => $mesFormations,
            'purchasedIds' => $purchasedIds,
            'bestsellers' => $bestsellers,
            'progressMap' => $progressMap,
            'userBadges' => $this->getUserBadges($entityManager),
        ]);
    }

    #[Route('/formations/{id}', name: 'app_formation_show')]
    public function formationShow(int $id, EntityManagerInterface $entityManager, Request $request): Response
    {
        $formation = $entityManager->getRepository(Formation::class)->find($id);

        if (!$formation) {
            throw $this->createNotFoundException('Formation introuvable');
        }

        $user = $this->getUser();
        $isPurchased = false;
        if ($user instanceof User) {
            $isPurchased = $user->getPurchasedFormations()->contains($formation);
        }

        $lessons = $entityManager->getRepository(Lesson::class)
            ->createQueryBuilder('l')
            ->leftJoin('l.formation', 'f')
            ->addSelect('f')
            ->andWhere('f.id = :formationId')
            ->setParameter('formationId', $id)
            ->orderBy('l.ordre', 'ASC')
            ->getQuery()
            ->getResult();

        $quizResultsMap = [];
        if ($user instanceof User) {
            $results = $entityManager->getRepository(QuizResult::class)->findBy([
                'user' => $user,
                'passed' => 1
            ]);
            foreach ($results as $res) {
                if (!isset($quizResultsMap[$res->getLessonId()]) || $res->getScore() > $quizResultsMap[$res->getLessonId()]->getScore()) {
                    $quizResultsMap[$res->getLessonId()] = $res;
                }
            }

            // Map fraud counts per lesson
            $fraudResults = $entityManager->getRepository(QuizResult::class)->findBy([
                'user' => $user,
                'fraudSuspected' => 1
            ]);
            $fraudMap = [];
            foreach ($fraudResults as $fr) {
                $lid = $fr->getLessonId();
                $fraudMap[$lid] = ($fraudMap[$lid] ?? 0) + 1;
            }
        } else {
            $fraudMap = [];
        }

        return $this->render('home/formation_show.html.twig', [
            'formation' => $formation,
            'lessons' => $lessons,
            'isPurchased' => $isPurchased,
            'quizResultsMap' => $quizResultsMap,
            'fraudMap' => $fraudMap,
            'userBadges' => $this->getUserBadges($entityManager),
        ]);
    }

    #[Route('/formations/{id}/inscription', name: 'app_formation_inscription')]
    public function formationInscription(int $id, EntityManagerInterface $entityManager, Request $request): Response
    {
        $formation = $entityManager->getRepository(Formation::class)->find($id);

        if (!$formation) {
            throw $this->createNotFoundException('Formation introuvable');
        }

        /** @var User $user */
        $user = $this->getUser();
        if (!$user) {
            $this->addFlash('danger', 'Veuillez vous connecter pour acheter cette formation.');
            return $this->redirectToRoute('app_login');
        }
        
        if (!$user->getPurchasedFormations()->contains($formation)) {
            $user->addPurchasedFormation($formation);
            $entityManager->flush();
        }

        $this->addFlash('success', 'Achat de la formation "' . $formation->getTitre() . '" validé avec succès ! Les leçons sont désormais débloquées.');

        return $this->redirectToRoute('app_formation_show', ['id' => $id]);
    }

    #[Route('/lessons', name: 'app_lessons')]
    public function lessons(
        Request $request,
        EntityManagerInterface $entityManager,
        PaginatorInterface $paginator
    ): Response {
        $titre = trim((string) $request->query->get('titre', ''));
        $formationId = trim((string) $request->query->get('formation', ''));
        $tri = (string) $request->query->get('tri', 'ordre');
        $ordre = strtolower((string) $request->query->get('ordre', 'asc'));

        $qb = $entityManager->getRepository(Lesson::class)
            ->createQueryBuilder('l')
            ->leftJoin('l.formation', 'f')
            ->addSelect('f')
            ->where('f.is_published = 1');

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

        $ordre = $ordre === 'desc' ? 'DESC' : 'ASC';
        $qb->orderBy($allowedSortFields[$tri], $ordre);

        $query = $qb->getQuery();

        $lessons = $paginator->paginate(
            $query,
            $request->query->getInt('page', 1),
            6
        );

        $formations = $entityManager->getRepository(Formation::class)->findBy(['is_published' => 1]);

        // Get purchased formation IDs from Database
        $user = $this->getUser();
        $purchasedFormationIds = [];
        if ($user instanceof User) {
            foreach ($user->getPurchasedFormations() as $f) {
                $purchasedFormationIds[] = $f->getId();
            }
        }

        return $this->render('home/lessons.html.twig', [
            'lessons' => $lessons,
            'formations' => $formations,
            'titre' => $titre,
            'formationSelected' => $formationId,
            'tri' => $tri,
            'ordre' => strtolower($ordre),
            'purchasedFormationIds' => $purchasedFormationIds,
            'userBadges' => $this->getUserBadges($entityManager),
        ]);
    }

    private function getUserBadges(EntityManagerInterface $em): array
    {
        $user = $this->getUser();
        if (!$user) {
            return [];
        }

        $passedQuizzes = $em->getRepository(\App\Entity\QuizResult::class)->createQueryBuilder('q')
            ->where('q.user = :user')
            ->andWhere('q.passed = 1')
            ->setParameter('user', $user)
            ->orderBy('q.takenAt', 'DESC')
            ->getQuery()
            ->getResult();

        $userBadges = [];
        foreach ($passedQuizzes as $quiz) {
            $formTitle = $quiz->getFormationTitle();
            if (!isset($userBadges[$formTitle])) {
                // Try to find the formation level
                $formation = $em->getRepository(Formation::class)->findOneBy(['titre' => $formTitle]);
                $level = $formation ? $formation->getNiveau() : 'Débutant';

                $userBadges[$formTitle] = [
                    'formationTitle' => $formTitle,
                    'level' => $level,
                    'lessonTitle' => $quiz->getLessonTitle(),
                    'score' => $quiz->getScore(),
                    'takenAt' => $quiz->getTakenAt(),
                ];
            }
        }

        return $userBadges;
    }

    #[Route('/formations/{id}/rate/{score}', name: 'app_formation_rate', methods: ['POST'])]
    public function rateFormation(int $id, int $score, EntityManagerInterface $em): JsonResponse
    {
        $formation = $em->getRepository(Formation::class)->find($id);
        if (!$formation) {
            return new JsonResponse(['error' => 'Formation non trouvée'], 404);
        }

        $user = $this->getUser();
        if (!$user) {
            return new JsonResponse(['error' => 'Non authentifié'], 401);
        }

        // Option A: Single update logic
        $currentRating = $formation->getRating() ?? 0.0;
        $currentCount = $formation->getRatingCount();

        $newCount = $currentCount + 1;
        $newRating = (($currentRating * $currentCount) + $score) / $newCount;

        $formation->setRating($newRating);
        $formation->setRatingCount($newCount);

        $em->flush();

        return new JsonResponse([
            'newRating' => round($newRating, 1),
            'newCount' => $newCount
        ]);
    }
}