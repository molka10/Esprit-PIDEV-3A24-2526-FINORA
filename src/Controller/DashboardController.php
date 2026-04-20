<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class DashboardController extends AbstractController
{
    #[Route('/dashboard', name: 'app_dashboard')]
    public function index(): Response
    {
        $user = $this->getUser();

        // 🔒 Not logged → login
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        // 🟣 ADMIN -> Redirect to dedicated admin route
        if (in_array('ROLE_ADMIN', $user->getRoles())) {
            return $this->redirectToRoute('app_admin_dashboard');
        }

        // 🟣 ENTREPRISE
        if (in_array('ROLE_ENTREPRISE', $user->getRoles())) {
            // This could also be a separate method if it grows
            return $this->redirectToRoute('app_entreprise_dashboard');
        }

        // 🔵 USER / VISITEUR
        return $this->render('dashboard/user.html.twig');
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
        ]);
    }

    #[Route('/entreprise/dashboard', name: 'app_entreprise_dashboard')]
    public function entreprise(
        \App\Repository\AppelOffreRepository $appelOffreRepository,
        \App\Repository\CandidatureRepository $candidatureRepository
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_ENTREPRISE');
        $user = $this->getUser();

        return $this->render('dashboard/entreprise.html.twig', [
            'appelPublies' => count($appelOffreRepository->findBy(['statut' => 'published'])),
            'totalCandidatures' => count($candidatureRepository->findBy(['user' => $user])),
            'candidaturesEnAttente' => count($candidatureRepository->findBy(['statut' => 'submitted', 'user' => $user])),
        ]);
    }
}