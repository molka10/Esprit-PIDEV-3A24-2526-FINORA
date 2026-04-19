<?php

namespace App\Controller;

use App\Repository\AppelOffreRepository;
use App\Repository\CandidatureRepository;
use App\Repository\CategorieRepository;
use App\Repository\UserRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class DashboardController extends AbstractController
{
    #[Route('/', name: 'app_dashboard')]
    public function index(Request $request): Response
    {
        $roleParam = $request->query->get('role');
        if ($roleParam) {
            $request->getSession()->set('role', $roleParam);
        }
        $role = $request->getSession()->get('role', 'visiteur');

        if ($role === 'admin') {
            return $this->redirectToRoute('app_dashboard_admin', ['role' => 'admin']);
        } elseif ($role === 'entreprise') {
            return $this->redirectToRoute('app_dashboard_entreprise');
        }

        return $this->redirectToRoute('app_dashboard_visiteur');
    }

    #[Route('/admin/dashboard', name: 'app_dashboard_admin')]
    public function admin(
        AppelOffreRepository $appelOffreRepository,
        CandidatureRepository $candidatureRepository,
        CategorieRepository $categorieRepository,
        UserRepository $userRepository,
        Request $request
    ): Response {
        // Force session role to admin for this mock role system
        $request->getSession()->set('role', 'admin');

        // Stats globales pour Admin
        $totalAppels = count($appelOffreRepository->findAll());
        $appelPublies = count($appelOffreRepository->findBy(['statut' => 'published']));
        $appelClotures = count($appelOffreRepository->findBy(['statut' => 'closed']));
        $appelBrouillons = count($appelOffreRepository->findBy(['statut' => 'draft']));

        $totalCandidatures = count($candidatureRepository->findAll());
        $candidaturesAcceptees = count($candidatureRepository->findBy(['statut' => 'accepted']));
        $candidaturesRejetes = count($candidatureRepository->findBy(['statut' => 'rejected']));
        $candidaturesEnAttente = count($candidatureRepository->findBy(['statut' => 'submitted']));

        $totalCategories = count($categorieRepository->findAll());
        $totalUsers = count($userRepository->findAll());

        // Derniers appels d'offre
        $derniersAppels = $appelOffreRepository->findBy(
            [], ['createdAt' => 'DESC'], 5
        );

        // Dernières candidatures
        $dernieresCandidatures = $candidatureRepository->findBy(
            [], ['createdAt' => 'DESC'], 5
        );

        return $this->render('dashboard/admin.html.twig', [
            'totalAppels' => $totalAppels,
            'appelPublies' => $appelPublies,
            'appelClotures' => $appelClotures,
            'appelBrouillons' => $appelBrouillons,
            'totalCandidatures' => $totalCandidatures,
            'candidaturesAcceptees' => $candidaturesAcceptees,
            'candidaturesRejetes' => $candidaturesRejetes,
            'candidaturesEnAttente' => $candidaturesEnAttente,
            'totalCategories' => $totalCategories,
            'totalUsers' => $totalUsers,
            'derniersAppels' => $derniersAppels,
            'dernieresCandidatures' => $dernieresCandidatures,
        ]);
    }

    #[Route('/entreprise/dashboard', name: 'app_dashboard_entreprise')]
    public function entreprise(
        AppelOffreRepository $appelOffreRepository,
        CandidatureRepository $candidatureRepository,
        Request $request
    ): Response {
        // Force session role to entreprise for this mock role system
        $request->getSession()->set('role', 'entreprise');

        // Pour l'entreprise, idéalement on filtre par l'utilisateur connecté
        $user = $this->getUser();

        // Statistiques globales ou filtrées
        $appelPublies = count($appelOffreRepository->findBy(['statut' => 'published']));
        
        if ($user) {
            $totalCandidatures = count($candidatureRepository->findBy(['user' => $user]));
            $candidaturesEnAttente = count($candidatureRepository->findBy(['statut' => 'submitted', 'user' => $user]));
        } else {
            $totalCandidatures = count($candidatureRepository->findAll());
            $candidaturesEnAttente = count($candidatureRepository->findBy(['statut' => 'submitted']));
        }

        return $this->render('dashboard/entreprise.html.twig', [
            'appelPublies' => $appelPublies,
            'totalCandidatures' => $totalCandidatures,
            'candidaturesEnAttente' => $candidaturesEnAttente,
        ]);
    }

    #[Route('/visiteur/dashboard', name: 'app_dashboard_visiteur')]
    public function visiteur(Request $request): Response
    {
        // Force session role to visiteur for this mock role system
        $request->getSession()->set('role', 'visiteur');
        
        return $this->render('dashboard/visiteur.html.twig');
    }
}