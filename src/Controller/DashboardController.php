<?php

namespace App\Controller;

use App\Repository\AppelOffreRepository;
use App\Repository\CandidatureRepository;
use App\Repository\CategorieRepository;
use App\Repository\UserRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class DashboardController extends AbstractController
{
    #[Route('/', name: 'app_dashboard')]
    public function index(
        AppelOffreRepository $appelOffreRepository,
        CandidatureRepository $candidatureRepository,
        CategorieRepository $categorieRepository,
        UserRepository $userRepository
    ): Response {
        // Stats globales
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

        return $this->render('dashboard/index.html.twig', [
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
}