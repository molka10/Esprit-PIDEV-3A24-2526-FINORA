<?php

namespace App\Controller;

use App\Entity\QuizResult;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[IsGranted('ROLE_ADMIN')]
class AdminQuizController extends AbstractController
{
    #[Route('/admin/quiz/results', name: 'app_admin_quiz_results')]
    public function results(EntityManagerInterface $em): Response
    {
        // 📈 Top 5 Quizzes (Most Passed)
        $topQuizzes = $em->createQuery(
            'SELECT q.lessonTitle, COUNT(q.id) as totalPassed 
             FROM App\Entity\QuizResult q 
             WHERE q.passed = 1 
             GROUP BY q.lessonTitle 
             ORDER BY totalPassed DESC'
        )->setMaxResults(5)->getResult();

        // 📋 Latest Results & Fraud
        $latestResults = $em->getRepository(QuizResult::class)->findBy([], ['takenAt' => 'DESC'], 20);
        $fraudAttempts = $em->getRepository(QuizResult::class)->findBy(['fraudSuspected' => 1], ['takenAt' => 'DESC'], 10);

        // 📊 General Stats
        $totalAttempts = $em->getRepository(QuizResult::class)->count([]);
        $totalPassed = $em->getRepository(QuizResult::class)->count(['passed' => 1]);
        $passRate = $totalAttempts > 0 ? round(($totalPassed / $totalAttempts) * 100, 1) : 0;

        // 🔄 Growth Analysis (This month vs Last month)
        $thisMonthStart = new \DateTime('first day of this month 00:00:00');
        $lastMonthStart = (clone $thisMonthStart)->modify('-1 month');
        
        $attemptsThisMonth = $em->createQuery('SELECT COUNT(q.id) FROM App\Entity\QuizResult q WHERE q.takenAt >= :start')
            ->setParameter('start', $thisMonthStart)->getSingleScalarResult();
        $attemptsLastMonth = $em->createQuery('SELECT COUNT(q.id) FROM App\Entity\QuizResult q WHERE q.takenAt >= :start AND q.takenAt < :end')
            ->setParameter('start', $lastMonthStart)->setParameter('end', $thisMonthStart)->getSingleScalarResult();
        
        $growth = $attemptsLastMonth > 0 ? round((($attemptsThisMonth - $attemptsLastMonth) / $attemptsLastMonth) * 100, 1) : 100;

        // 👥 User Segmentation
        $userStats = $em->createQuery(
            'SELECT q.studentName, COUNT(q.id) as certs 
             FROM App\Entity\QuizResult q 
             WHERE q.passed = 1 
             GROUP BY q.studentName'
        )->getResult();

        $segments = ['beginner' => 0, 'intermediate' => 0, 'expert' => 0];
        foreach ($userStats as $stat) {
            if ($stat['certs'] >= 5) $segments['expert']++;
            elseif ($stat['certs'] >= 2) $segments['intermediate']++;
            else $segments['beginner']++;
        }

        return $this->render('admin/quiz/results.html.twig', [
            'topQuizzes' => $topQuizzes,
            'latestResults' => $latestResults,
            'fraudAttempts' => $fraudAttempts,
            'totalAttempts' => $totalAttempts,
            'passRate' => $passRate,
            'growth' => $growth,
            'segments' => $segments,
        ]);
    }
}
