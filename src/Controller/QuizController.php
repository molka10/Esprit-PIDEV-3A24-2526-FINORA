<?php

namespace App\Controller;

use App\Entity\Lesson;
use App\Entity\Formation;
use App\Entity\QuizResult;
use App\Service\GroqQuizService;
use App\Service\QuizFraudService;
use App\Service\QuizAiCommentService;
use Doctrine\ORM\EntityManagerInterface;
use Nucleos\DompdfBundle\Wrapper\DompdfWrapperInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Session\SessionInterface;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/quiz')]
final class QuizController extends AbstractController
{
    #[Route('/lesson/{id}', name: 'app_quiz_take', methods: ['GET'])]
    public function takeQuiz(
        Lesson $lesson,
        Request $request,
        GroqQuizService $groqQuizService,
        SessionInterface $session,
        EntityManagerInterface $entityManager
    ): Response {
        $user = $this->getUser();
        $studentName = $user ? $user->getUsername() : 'Invité';

        if ($user && !$user->getPurchasedFormations()->contains($lesson->getFormation())) {
            $this->addFlash('danger', '🚫 Vous devez acheter cette formation pour accéder au quiz.');
            return $this->redirectToRoute('app_formation_show', ['id' => $lesson->getFormation()->getId()]);
        }

        // Check for fraud strikes (3 strikes = blocked) using User ID if possible
        $criteria = ['lessonId' => $lesson->getId(), 'fraudSuspected' => 1];
        if ($user) {
            $criteria['user'] = $user;
        } else {
            $criteria['studentName'] = $studentName;
        }
        
        $fraudCount = $entityManager->getRepository(QuizResult::class)->count($criteria);

        if ($fraudCount >= 3) {
            $this->addFlash('danger', '🚫 Vous êtes interdit de passer ce quiz.');
            return $this->redirectToRoute('app_lesson_show', ['id' => $lesson->getId()]);
        }

        $lang = $request->query->get('lang');
        $questions = null;

        if ($lang) {
            $questions = $groqQuizService->generateQuiz(
                (string) $lesson->getTitre(),
                (string) $lesson->getContenu(),
                $lang
            );
            $session->set('quiz_questions_' . $lesson->getId(), $questions);
            $session->set('quiz_lang_' . $lesson->getId(), $lang);
        } else {
            // Clear old session quiz if we're at selection screen
            $session->remove('quiz_questions_' . $lesson->getId());
            $session->remove('quiz_lang_' . $lesson->getId());
        }

        return $this->render('quiz/take.html.twig', [
            'lesson' => $lesson,
            'questions' => $questions,
            'fraudCount' => $fraudCount,
            'lang' => $lang
        ]);
    }

    #[Route('/lesson/{id}/submit', name: 'app_quiz_submit', methods: ['POST'])]
    public function submitQuiz(
        Lesson $lesson,
        Request $request,
        SessionInterface $session,
        EntityManagerInterface $entityManager,
        QuizFraudService $quizFraudService,
        QuizAiCommentService $quizAiCommentService
    ): Response {
        $questions = $session->get('quiz_questions_' . $lesson->getId(), []);

        if (!is_array($questions) || count($questions) === 0) {
            $this->addFlash('danger', 'Le quiz a expiré. Veuillez le régénérer.');
            return $this->redirectToRoute('app_quiz_take', ['id' => $lesson->getId()]);
        }

        $user = $this->getUser();
        $studentName = $user ? $user->getUsername() : 'Invité';

        $correctAnswers = 0;
        $totalQuestions = count($questions);
        $reviewData = [];

        foreach ($questions as $index => $question) {
            $given = $request->request->get('answer_' . $index);
            $expected = $question['correct'] ?? null;
            $isCorrect = ($given !== null && is_numeric($given) && $expected !== null && (int) $given === (int) $expected);

            if ($isCorrect) {
                $correctAnswers++;
            }

            $reviewData[] = [
                'question' => $question['question'] ?? 'Question invalide',
                'options' => $question['options'] ?? [],
                'correct' => (int) ($expected ?? 0),
                'given' => $given !== null ? (int) $given : -1,
                'isCorrect' => $isCorrect,
            ];
        }

        $score = (int) round(($correctAnswers / max($totalQuestions, 1)) * 100);
        $passed = $score >= 80 ? 1 : 0;

        $focusLossCount = (int) $request->request->get('focusLossCount', 0);
        $exitFullscreenCount = (int) $request->request->get('exitFullscreenCount', 0);
        $fastAnswers = (int) $request->request->get('fastAnswers', 0);

        $fraudAnalysis = $quizFraudService->analyze($focusLossCount, $exitFullscreenCount, $fastAnswers);
        
        $fraudExplanation = null;
        if ($fraudAnalysis['isFraud']) {
            $passed = 0;
            $fraudExplanation = $quizAiCommentService->generateFraudComment($fraudAnalysis['details']);
            $this->addFlash('fraud_alert', 'Un comportement suspect a été détecté durant votre quiz. Toute récidive entraînera un blocage.');
        }

        if ($user && !$user->getPurchasedFormations()->contains($lesson->getFormation())) {
            return new JsonResponse(['error' => 'Enrollment required'], 403);
        }

        $result = new QuizResult();
        $result
            ->setUser($user)
            ->setStudentName($studentName)
            ->setLessonId((int) $lesson->getId())
            ->setLessonTitle((string) $lesson->getTitre())
            ->setFormationTitle($lesson->getFormation() ? (string) $lesson->getFormation()->getTitre() : 'Formation inconnue')
            ->setScore($score)
            ->setPassed($passed)
            ->setTakenAt(new \DateTime())
            ->setFraudSuspected($fraudAnalysis['isFraud'] ? 1 : 0)
            ->setFraudExplanation($fraudExplanation);

        $entityManager->persist($result);
        $entityManager->flush();

        $session->remove('quiz_questions_' . $lesson->getId());

        return $this->render('quiz/result.html.twig', [
            'lesson' => $lesson,
            'score' => $score,
            'correctAnswers' => $correctAnswers,
            'totalQuestions' => $totalQuestions,
            'passed' => $passed === 1,
            'studentName' => $studentName,
            'quizResult' => $result,
            'fraudSuspected' => $fraudAnalysis['isFraud'],
            'fraudExplanation' => $fraudExplanation,
            'reviewData' => $reviewData,
        ]);
    }

    #[Route('/certificate/{id}', name: 'app_quiz_certificate', methods: ['GET'])]
    public function certificate(
        QuizResult $quizResult,
        DompdfWrapperInterface $dompdfWrapper
    ): Response {
        if (!$quizResult->isPassed()) {
            $this->addFlash('danger', 'Le certificat n’est disponible que pour un quiz réussi.');
            return $this->redirectToRoute('app_home');
        }

        $html = $this->renderView('quiz/certificate_pdf.html.twig', [
            'quizResult' => $quizResult,
        ]);

        return $dompdfWrapper->getStreamResponse($html, sprintf('certificat_%s.pdf', $quizResult->getStudentName()));
    }

    #[Route('/my-certificates', name: 'app_my_certificates', methods: ['GET'])]
    public function myCertificates(
        EntityManagerInterface $entityManager
    ): Response {
        $user = $this->getUser();
        $studentName = $user ? $user->getUsername() : null;
        
        $certificates = [];
        $suggestedQuizzes = [];

        if ($user) {
            $allPassed = $entityManager->getRepository(QuizResult::class)->findBy(
                ['user' => $user, 'passed' => 1],
                ['score' => 'DESC']
            );
            
            $seenLessons = [];
            foreach ($allPassed as $res) {
                if (!isset($seenLessons[$res->getLessonId()])) {
                    $certificates[] = $res;
                    $seenLessons[$res->getLessonId()] = true;
                }
            }

            $purchasedIds = [];
            foreach ($user->getPurchasedFormations() as $f) {
                $purchasedIds[] = $f->getId();
            }

            if (!empty($purchasedIds)) {
                $lessons = $entityManager->getRepository(Lesson::class)->createQueryBuilder('l')
                    ->join('l.formation', 'f')
                    ->where('f.id IN (:ids)')
                    ->setParameter('ids', $purchasedIds)
                    ->orderBy('l.ordre', 'ASC')
                    ->getQuery()
                    ->getResult();

                $passedLessonIds = array_map(fn($c) => $c->getLessonId(), $certificates);

                foreach ($lessons as $lesson) {
                    if (!in_array($lesson->getId(), $passedLessonIds)) {
                        $suggestedQuizzes[] = $lesson;
                    }
                }
            }
        }

        return $this->render('quiz/my_certificates.html.twig', [
            'certificates' => $certificates,
            'studentName' => $studentName,
            'suggestedQuizzes' => array_slice($suggestedQuizzes, 0, 6),
        ]);
    }

    #[Route('/formation/{id}/master-certificate', name: 'app_master_certificate', methods: ['GET'])]
    public function masterCertificate(
        Formation $formation,
        EntityManagerInterface $em,
        DompdfWrapperInterface $dompdfWrapper
    ): Response {
        $user = $this->getUser();
        if (!$user || !$user->getPurchasedFormations()->contains($formation)) {
            $this->addFlash('danger', 'Accès refusé.');
            return $this->redirectToRoute('app_formations');
        }

        $lessons = $formation->getLessons();
        $totalLessons = count($lessons);
        
        if ($totalLessons === 0) {
            $this->addFlash('warning', 'Cette formation ne contient aucune leçon.');
            return $this->redirectToRoute('app_formation_show', ['id' => $formation->getId()]);
        }

        $passedLessonsCount = $em->getRepository(QuizResult::class)->createQueryBuilder('q')
            ->select('COUNT(DISTINCT q.lessonId)')
            ->where('q.user = :user')
            ->andWhere('q.passed = 1')
            ->andWhere('q.lessonId IN (:ids)')
            ->setParameter('user', $user)
            ->setParameter('ids', $lessons->map(fn($l) => $l->getId())->toArray())
            ->getQuery()
            ->getSingleScalarResult();

        if ($passedLessonsCount < $totalLessons) {
            $this->addFlash('warning', sprintf('Vous devez réussir tous les quiz de cette formation (%d/%d) pour obtenir le Master Certificate.', $passedLessonsCount, $totalLessons));
            return $this->redirectToRoute('app_formation_show', ['id' => $formation->getId()]);
        }

        $html = $this->renderView('quiz/master_certificate_pdf.html.twig', [
            'user' => $user,
            'formation' => $formation,
            'date' => new \DateTime(),
            'totalLessons' => $totalLessons
        ]);

        return $dompdfWrapper->getStreamResponse($html, sprintf('master_certificat_%s.pdf', $formation->getTitre()));
    }

    #[Route('/lesson/{id}/explain', name: 'app_quiz_explain', methods: ['POST'])]
    public function explainQuestion(
        Lesson $lesson,
        Request $request,
        GroqQuizService $groqQuizService
    ): JsonResponse {
        $data = json_decode($request->getContent(), true);
        
        $question = $data['question'] ?? '';
        $correctAnswer = $data['correct'] ?? '';
        $userAnswer = $data['given'] ?? '';

        if (!$question || !$correctAnswer) {
            return $this->json(['error' => 'Données invalides'], Response::HTTP_BAD_REQUEST);
        }

        $lang = $session->get('quiz_lang_' . $lesson->getId(), 'fr');
        
        $explanation = $groqQuizService->explainQuestion(
            (string) $lesson->getContenu(),
            $question,
            $correctAnswer,
            $userAnswer,
            $lang
        );

        return $this->json(['explanation' => $explanation]);
    }
}