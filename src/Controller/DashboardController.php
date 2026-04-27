<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class DashboardController extends AbstractController
{
    #[Route('/dashboard', name: 'app_dashboard')]
    public function index(
        \App\Service\GamificationService $gamificationService,
        \App\Service\SmartLearningService $smartLearningService,
        \App\Repository\TransactionBourseRepository $transactionBourseRepository,
        \App\Repository\TransactionWalletRepository $transactionWalletRepository,
        \App\Repository\InvestmentManagementRepository $investmentManagementRepository,
        \App\Repository\CandidatureRepository $candidatureRepository
    ): Response
    {
        $user = $this->getUser();

        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        if (in_array('ROLE_ADMIN', $user->getRoles())) {
            return $this->redirectToRoute('app_admin_dashboard');
        }

        if (in_array('ROLE_ENTREPRISE', $user->getRoles())) {
            return $this->redirectToRoute('app_entreprise_dashboard');
        }

        // 🧠 SMART RECOMMENDATIONS
        $recommendations = $smartLearningService->getRecommendations($user);

        // Stats Bourse
        $bourseTransactions = $transactionBourseRepository->findBy(['user' => $user], ['dateTransaction' => 'DESC'], 5);
        $totalBourseVolume = 0;
        foreach ($transactionBourseRepository->findBy(['user' => $user]) as $t) {
            $totalBourseVolume += $t->getMontantTotal();
        }

        // Stats Wallet
        $walletTransactionsCount = count($transactionWalletRepository->findBy(['user' => $user->getId()]));
        
        // Stats Invest
        $totalInvestments = count($investmentManagementRepository->findBy(['user' => $user]));
        
        // Stats Appels
        $totalCandidatures = count($candidatureRepository->findBy(['user' => $user]));

        $badges = $gamificationService->getUserBadges($user);

        return $this->render('dashboard/user.html.twig', [
            'badges' => $badges,
            'recommendations' => $recommendations,
            'stats' => [
                'bourseVolume' => $totalBourseVolume,
                'walletTransactions' => $walletTransactionsCount,
                'investmentsCount' => $totalInvestments,
                'candidaturesCount' => $totalCandidatures,
            ],
            'recentTransactions' => $bourseTransactions
        ]);
    }

    #[Route('/admin/dashboard', name: 'app_admin_dashboard')]
    public function admin(
        \App\Repository\AppelOffreRepository $appelOffreRepository,
        \App\Repository\CandidatureRepository $candidatureRepository,
        \App\Repository\CategorieRepository $categorieRepository,
        \App\Repository\UserRepository $userRepository
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        return $this->render('dashboard/admin.html.twig', [
            // Appels
            'totalAppels' => count($appelOffreRepository->findAll()),
            'appelPublies' => count($appelOffreRepository->findBy(['statut' => 'published'])),
            'appelClotures' => count($appelOffreRepository->findBy(['statut' => 'closed'])),
            'appelBrouillons' => count($appelOffreRepository->findBy(['statut' => 'draft'])),
            
            // Candidatures
            'totalCandidatures' => count($candidatureRepository->findAll()),
            'candidaturesAcceptees' => count($candidatureRepository->findBy(['statut' => 'accepted'])),
            'candidaturesRejetes' => count($candidatureRepository->findBy(['statut' => 'rejected'])),
            'candidaturesEnAttente' => count($candidatureRepository->findBy(['statut' => 'submitted'])),
            
            // Autres
            'totalCategories' => count($categorieRepository->findAll()),
            'totalUsers' => count($userRepository->findAll()),
            
            // Listes
            'derniersAppels' => $appelOffreRepository->findBy([], ['createdAt' => 'DESC'], 5),
            'dernieresCandidatures' => $candidatureRepository->findBy([], ['createdAt' => 'DESC'], 5),

            // --- Market Stats (Requested Pies) ---
            'categoryStats' => $this->getCategoryRepartition($appelOffreRepository, $categorieRepository),
        ]);
    }

    private function getCategoryRepartition($appelOffreRepository, $categorieRepository): array
    {
        $categories = $categorieRepository->findAll();
        $data = [];
        foreach ($categories as $cat) {
            $count = count($appelOffreRepository->findBy(['categorie' => $cat]));
            if ($count > 0) {
                $data[] = [
                    'name' => $cat->getNom(),
                    'count' => $count
                ];
            }
        }
        return $data;
    }

    #[Route('/entreprise/dashboard', name: 'app_entreprise_dashboard')]
    public function entreprise(
        \App\Repository\AppelOffreRepository $appelOffreRepository,
        \App\Repository\CandidatureRepository $candidatureRepository,
        \App\Service\GamificationService $gamificationService
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_ENTREPRISE');
        $user = $this->getUser();

        $badges = $gamificationService->getEntrepriseBadges($user);

        return $this->render('dashboard/entreprise.html.twig', [
            'appelPublies' => count($appelOffreRepository->findBy(['createdBy' => $user, 'statut' => 'published'])), // Fixed query slightly just in case it wasn't counting correctly
            'totalCandidatures' => count($candidatureRepository->findBy(['user' => $user])),
            'candidaturesEnAttente' => count($candidatureRepository->findBy(['statut' => 'submitted', 'user' => $user])),
            'badges_entreprise' => $badges,
        ]);
    }

    #[Route('/gamification', name: 'app_gamification')]
    public function gamification(\App\Service\GamificationService $gamificationService): Response
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        if (in_array('ROLE_ENTREPRISE', $user->getRoles())) {
            $badges = $gamificationService->getEntrepriseBadges($user);
            $type = 'entreprise';
        } else {
            $badges = $gamificationService->getUserBadges($user);
            $type = 'user';
        }

        return $this->render('dashboard/gamification.html.twig', [
            'badges' => $badges,
            'type' => $type
        ]);
    }
}