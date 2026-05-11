<?php

namespace App\Controller;

use App\Entity\User;
use App\Entity\Lesson;
use App\Entity\QuizResult;
use App\Service\WalletBalanceService;
use App\Service\CurrencyConverterService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\RequestStack;
use Symfony\Component\Routing\Attribute\Route;

/**
 * Internal API controller for real-time UI updates.
 * – GET  /api/balance          → returns current user wallet balance (JSON)
 * – POST /api/quiz/fraud-report → called by the Java app to record a fraud strike
 */
#[Route('/api')]
class ApiController extends AbstractController
{
    public function __construct(
        private WalletBalanceService      $walletBalanceService,
        private CurrencyConverterService  $currencyConverter,
        private RequestStack              $requestStack,
        private EntityManagerInterface    $em,
    ) {}

    // ─────────────────────────────────────────────────────────────
    // 💰 LIVE BALANCE (called after buy/sell via AJAX)
    // ─────────────────────────────────────────────────────────────
    #[Route('/balance', name: 'api_balance', methods: ['GET'])]
    public function balance(): JsonResponse
    {
        $user = $this->getUser();
        if (!$user instanceof User) {
            return $this->json(['balance' => 0, 'formatted' => '0.00 TND'], 401);
        }

        $rawBalance = $this->walletBalanceService->calculateUserBalance($user->getId());
        $currency   = $this->requestStack->getSession()->get('app_currency', 'TND');
        $converted  = $this->currencyConverter->convert($rawBalance, 'TND', $currency);

        $symbol = match($currency) {
            'EUR'   => '€',
            'USD'   => '$',
            default => 'TND',
        };

        $formatted = number_format($converted, 2, '.', ' ') . ' ' . $symbol;

        return $this->json([
            'balance'   => round($converted, 2),
            'raw'       => round($rawBalance, 2),
            'currency'  => $currency,
            'formatted' => $formatted,
        ]);
    }

    // ─────────────────────────────────────────────────────────────
    // 🚨 FRAUD REPORT  (called by Java after 3 fraud events)
    // Java sends: { "userId": 42, "lessonId": 7, "reason": "..." }
    // ─────────────────────────────────────────────────────────────
    #[Route('/quiz/fraud-report', name: 'api_quiz_fraud_report', methods: ['POST'])]
    public function fraudReport(Request $request): JsonResponse
    {
        // Optional: add an API secret header check for security
        // $apiKey = $request->headers->get('X-Api-Key');
        // if ($apiKey !== $_ENV['JAVA_API_KEY']) return $this->json(['error' => 'Unauthorized'], 401);

        $data     = json_decode($request->getContent(), true) ?? [];
        $userId   = (int) ($data['userId']   ?? 0);
        $lessonId = (int) ($data['lessonId'] ?? 0);
        $reason   = (string) ($data['reason'] ?? 'Reported by Java module');

        if (!$userId || !$lessonId) {
            return $this->json(['error' => 'userId and lessonId are required'], 400);
        }

        $user   = $this->em->getRepository(User::class)->find($userId);
        $lesson = $this->em->getRepository(Lesson::class)->find($lessonId);

        if (!$user || !$lesson) {
            return $this->json(['error' => 'User or Lesson not found'], 404);
        }

        // Count existing Symfony fraud strikes for this user+lesson
        $existingStrikes = $this->em->getRepository(QuizResult::class)->count([
            'user'           => $user,
            'lesson'         => $lesson,
            'fraudSuspected' => 1,
        ]);

        // Create a fraud QuizResult entry so Symfony's 3-strike check triggers
        $result = new QuizResult();
        $result
            ->setUser($user)
            ->setStudentName($user->getUsername())
            ->setLesson($lesson)
            ->setLessonTitle((string) $lesson->getTitre())
            ->setFormationTitle($lesson->getFormation() ? (string) $lesson->getFormation()->getTitre() : 'N/A')
            ->setScore(0)
            ->setPassed(0)
            ->setTakenAt(new \DateTime())
            ->setFraudSuspected(1)
            ->setFraudExplanation('[Java Report] ' . $reason);

        $this->em->persist($result);
        $this->em->flush();

        $newTotal = $existingStrikes + 1;
        $blocked  = $newTotal >= 3;

        return $this->json([
            'success'         => true,
            'fraudStrikes'    => $newTotal,
            'quizBlocked'     => $blocked,
            'message'         => $blocked
                ? "User {$user->getUsername()} is now BLOCKED from quiz (lesson #{$lessonId})"
                : "Fraud strike #{$newTotal} recorded for user {$user->getUsername()} (lesson #{$lessonId})",
        ]);
    }
}
