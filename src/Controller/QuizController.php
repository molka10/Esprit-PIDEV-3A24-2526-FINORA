<?php

namespace App\Controller;

use App\Entity\Lesson;
use App\Entity\QuizResult;
use App\Service\GroqQuizService;
use App\Service\QuizFraudService;
use App\Service\QuizAiCommentService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Session\SessionInterface;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/quiz')]
final class QuizController extends AbstractController
{
    #[Route('/lesson/{id}', name: 'app_quiz_take', methods: ['GET'])]
    public function takeQuiz(
        Lesson $lesson,
        GroqQuizService $groqQuizService,
        SessionInterface $session
    ): Response {
        $questions = $groqQuizService->generateQuiz(
            (string) $lesson->getTitre(),
            (string) $lesson->getContenu()
        );

        $session->set('quiz_questions_' . $lesson->getId(), $questions);

        return $this->render('quiz/take.html.twig', [
            'lesson' => $lesson,
            'questions' => $questions,
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

        $studentName = trim((string) $request->request->get('student_name', 'Invité'));
        if ($studentName === '') {
            $studentName = 'Invité';
        }

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

        // --- Fraud Analysis Logic ---
        $focusLossCount = (int) $request->request->get('focusLossCount', 0);
        $exitFullscreenCount = (int) $request->request->get('exitFullscreenCount', 0);
        $fastAnswers = (int) $request->request->get('fastAnswers', 0);

        $fraudAnalysis = $quizFraudService->analyze($focusLossCount, $exitFullscreenCount, $fastAnswers);
        
        $fraudExplanation = null;
        if ($fraudAnalysis['isFraud']) {
            $passed = 0; // Invalidate the run
            $fraudExplanation = $quizAiCommentService->generateFraudComment($fraudAnalysis['details']);
        }
        // -----------------------------

        $result = new QuizResult();
        $result
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
        QuizResult $quizResult
    ): Response {
        if (!$quizResult->isPassed()) {
            $this->addFlash('danger', 'Le certificat n’est disponible que pour un quiz réussi.');
            return $this->redirectToRoute('app_home');
        }

        return $this->render('quiz/certificate.html.twig', [
            'quizResult' => $quizResult,
        ]);
    }

    #[Route('/lesson/{id}/explain', name: 'app_quiz_explain', methods: ['POST'])]
    public function explainQuestion(
        Lesson $lesson,
        Request $request,
        GroqQuizService $groqQuizService
    ): \Symfony\Component\HttpFoundation\JsonResponse {
        $data = json_decode($request->getContent(), true);
        
        $question = $data['question'] ?? '';
        $correctAnswer = $data['correct'] ?? '';
        $userAnswer = $data['given'] ?? '';

        if (!$question || !$correctAnswer) {
            return $this->json(['error' => 'Données invalides'], Response::HTTP_BAD_REQUEST);
        }

        $explanation = $groqQuizService->explainQuestion(
            (string) $lesson->getContenu(),
            $question,
            $correctAnswer,
            $userAnswer
        );

        return $this->json(['explanation' => $explanation]);
    }
}