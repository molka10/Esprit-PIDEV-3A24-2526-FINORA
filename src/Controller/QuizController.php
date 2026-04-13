<?php

namespace App\Controller;

use App\Entity\Lesson;
use App\Entity\QuizResult;
use App\Service\GroqQuizService;
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
        EntityManagerInterface $entityManager
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

        foreach ($questions as $index => $question) {
            $given = $request->request->get('answer_' . $index);
            $expected = $question['correct'] ?? null;

            if ($given !== null && is_numeric($given) && $expected !== null && (int) $given === (int) $expected) {
                $correctAnswers++;
            }
        }

        $score = (int) round(($correctAnswers / max($totalQuestions, 1)) * 100);
        $passed = $score >= 80 ? 1 : 0;

        $result = new QuizResult();
        $result
            ->setStudentName($studentName)
            ->setLessonId((int) $lesson->getId())
            ->setLessonTitle((string) $lesson->getTitre())
            ->setFormationTitle($lesson->getFormation() ? (string) $lesson->getFormation()->getTitre() : 'Formation inconnue')
            ->setScore($score)
            ->setPassed($passed)
            ->setTakenAt(new \DateTime());

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
}