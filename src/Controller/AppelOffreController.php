<?php

namespace App\Controller;

use App\Entity\AppelOffre;
use App\Form\AppelOffreType;
use App\Repository\AppelOffreRepository;
use App\Repository\CategorieRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\Form\FormError;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/appel/offre')]
final class AppelOffreController extends AbstractController
{
    #[Route(name: 'app_appel_offre_index', methods: ['GET'])]
    public function index(
        Request $request, 
        AppelOffreRepository $appelOffreRepository, 
        CategorieRepository $categorieRepository, 
        EntityManagerInterface $entityManager,
        \App\Service\SmartLearningService $smartLearningService
    ): Response
    {
        $user = $this->getUser();
        $recommendations = $user ? $smartLearningService->getRecommendations($user) : [];

        // Auto-clôture des appels d'offre expirés
        $appelsExpires = $appelOffreRepository->findAppelsExpires(new \DateTime());
        foreach ($appelsExpires as $appel) {
            $appel->setStatut('closed');
            $entityManager->persist($appel);
        }
        if (!empty($appelsExpires)) {
            $entityManager->flush();
            $this->addFlash('info', count($appelsExpires) . ' appel(s) d\'offre ont été clôturés automatiquement.');
        }

        $type = $request->query->get('type');
        $statut = $request->query->get('statut');
        $categorieId = $request->query->get('categorie');
        $search = $request->query->get('search');
        
        // Use security service to determine role for repository filtering
        $role = 'visiteur';
        if ($this->isGranted('ROLE_ADMIN')) {
            $role = 'admin';
        } elseif ($this->isGranted('ROLE_ENTREPRISE')) {
            $role = 'entreprise';
        }

        // Pagination settings
        $limit = 6;
        $page = (int)$request->query->get('page', 1);
        if ($page < 1) $page = 1;
        $offset = ($page - 1) * $limit;

        $totalItems = $appelOffreRepository->countByFilters(
            $type,
            $statut,
            $categorieId ? (int)$categorieId : null,
            $search,
            $role
        );
        $totalPages = (int)ceil($totalItems / $limit);

        $appel_offres = $appelOffreRepository->findByFilters(
            $type,
            $statut,
            $categorieId ? (int)$categorieId : null,
            $search,
            $role,
            $limit,
            $offset
        );

        $renderData = [
            'appel_offres' => $appel_offres,
            'categories' => $categorieRepository->findAll(),
            'current_page' => $page,
            'total_pages' => $totalPages,
            'total_items' => $totalItems,
            'filters' => [
                'type' => $type,
                'statut' => $statut,
                'categorie' => $categorieId,
                'search' => $search,
            ],
            'recommendations' => $recommendations
        ];

        if ($request->query->get('ajax')) {
            $template = ($request->query->get('role') === 'admin') 
                ? 'appel_offre/_admin_table.html.twig' 
                : 'appel_offre/_grid.html.twig';
            return $this->render($template, $renderData);
        }

        return $this->render('appel_offre/index.html.twig', $renderData);
    }

    #[Route('/new', name: 'app_appel_offre_new', methods: ['GET', 'POST'])]
    #[IsGranted('ROLE_ADMIN')]
    public function new(
        Request $request, 
        EntityManagerInterface $entityManager, 
        \App\Service\SmsService $smsService,
        \App\Repository\UserRepository $userRepository
    ): Response {
        $appelOffre = new AppelOffre();
        $form = $this->createForm(AppelOffreType::class, $appelOffre);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            // Vérification budgetMax >= budgetMin
            if ($appelOffre->getBudgetMin() && $appelOffre->getBudgetMax()) {
                if ($appelOffre->getBudgetMax() < $appelOffre->getBudgetMin()) {
                    $form->get('budgetMax')->addError(
                        new FormError('Le budget maximum doit être supérieur au budget minimum')
                    );
                } else {
                    $entityManager->persist($appelOffre);
                    $entityManager->flush();

                    // Notification SMS à l'admin si publié
                    if ($appelOffre->getStatut() === 'published') {
                        $admin = $userRepository->findOneAdmin();
                        if ($admin && $admin->getPhone()) {
                            $smsService->sendSms($admin->getPhone(), "Nouvel Appel d'Offre Publié : " . $appelOffre->getTitre());
                        }
                    }

                    $this->addFlash('success', 'Appel d\'offre créé avec succès !');
                    return $this->redirectToRoute('app_appel_offre_index', ['role' => $request->query->get('role')], Response::HTTP_SEE_OTHER);
                }
            } else {
                $entityManager->persist($appelOffre);
                $entityManager->flush();

                // Notification SMS à l'admin si publié
                if ($appelOffre->getStatut() === 'published') {
                    $admin = $userRepository->findOneAdmin();
                    if ($admin && $admin->getPhone()) {
                        $smsService->sendSms($admin->getPhone(), "Nouvel Appel d'Offre Publié : " . $appelOffre->getTitre());
                    }
                }

                $this->addFlash('success', 'Appel d\'offre créé avec succès !');
                return $this->redirectToRoute('app_appel_offre_index', ['role' => $request->query->get('role')], Response::HTTP_SEE_OTHER);
            }
        }

        return $this->render('appel_offre/new.html.twig', [
            'appel_offre' => $appelOffre,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_appel_offre_show', methods: ['GET'])]
    public function show(
        AppelOffre $appelOffre, 
        \App\Service\CurrencyService $currencyService,
        \App\Repository\CandidatureRepository $candidatureRepository,
        \App\Repository\RatingRepository $ratingRepository
    ): Response {
        $user = $this->getUser();
        $userCandidature = null;
        $userRating = null;

        if ($user) {
            $userCandidature = $candidatureRepository->findOneBy([
                'user' => $user,
                'appelOffre' => $appelOffre
            ]);
            $userRating = $ratingRepository->findOneBy([
                'user' => $user,
                'appelOffre' => $appelOffre
            ]);
        }

        $budgetMinEur = null;
        $budgetMaxEur = null;
        $budgetMinUsd = null;
        $budgetMaxUsd = null;

        if ($appelOffre->getBudgetMin()) {
            $budgetMinEur = $currencyService->convertTndTo($appelOffre->getBudgetMin(), 'EUR');
            $budgetMinUsd = $currencyService->convertTndTo($appelOffre->getBudgetMin(), 'USD');
        }
        if ($appelOffre->getBudgetMax()) {
            $budgetMaxEur = $currencyService->convertTndTo($appelOffre->getBudgetMax(), 'EUR');
            $budgetMaxUsd = $currencyService->convertTndTo($appelOffre->getBudgetMax(), 'USD');
        }

        return $this->render('appel_offre/show.html.twig', [
            'appel_offre' => $appelOffre,
            'userCandidature' => $userCandidature,
            'userRating' => $userRating,
            'budgetEur' => $budgetMinEur && $budgetMaxEur ? ['min' => $budgetMinEur, 'max' => $budgetMaxEur] : null,
            'budgetUsd' => $budgetMinUsd && $budgetMaxUsd ? ['min' => $budgetMinUsd, 'max' => $budgetMaxUsd] : null,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_appel_offre_edit', methods: ['GET', 'POST'])]
    #[IsGranted('ROLE_ADMIN')]
    public function edit(
        Request $request, 
        AppelOffre $appelOffre, 
        EntityManagerInterface $entityManager,
        \App\Service\SmsService $smsService,
        \App\Repository\UserRepository $userRepository
    ): Response {
        $form = $this->createForm(AppelOffreType::class, $appelOffre);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            // Vérification budgetMax >= budgetMin
            if ($appelOffre->getBudgetMin() && $appelOffre->getBudgetMax()) {
                if ($appelOffre->getBudgetMax() < $appelOffre->getBudgetMin()) {
                    $form->get('budgetMax')->addError(
                        new FormError('Le budget maximum doit être supérieur au budget minimum')
                    );
                } else {
                    $entityManager->flush();

                    // Notification SMS à l'admin si publié
                    if ($appelOffre->getStatut() === 'published') {
                        $admin = $userRepository->findOneAdmin();
                        if ($admin && $admin->getPhone()) {
                            $smsService->sendSms($admin->getPhone(), "Appel d'Offre Mis à jour & Publié : " . $appelOffre->getTitre());
                        }
                    }

                    $this->addFlash('success', 'Appel d\'offre modifié avec succès !');
                    return $this->redirectToRoute('app_appel_offre_index', ['role' => $request->query->get('role')], Response::HTTP_SEE_OTHER);
                }
            } else {
                $entityManager->flush();

                // Notification SMS à l'admin si publié
                if ($appelOffre->getStatut() === 'published') {
                    $admin = $userRepository->findOneAdmin();
                    if ($admin && $admin->getPhone()) {
                        $smsService->sendSms($admin->getPhone(), "Appel d'Offre Mis à jour & Publié : " . $appelOffre->getTitre());
                    }
                }

                $this->addFlash('success', 'Appel d\'offre modifié avec succès !');
                return $this->redirectToRoute('app_appel_offre_index', ['role' => $request->query->get('role')], Response::HTTP_SEE_OTHER);
            }
        }

        return $this->render('appel_offre/edit.html.twig', [
            'appel_offre' => $appelOffre,
            'form' => $form,
        ]);
    }

    #[Route('/{id}/delete', name: 'app_appel_offre_delete', methods: ['POST'])]
    #[IsGranted('ROLE_ADMIN')]
    public function delete(Request $request, AppelOffre $appelOffre, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$appelOffre->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($appelOffre);
            $entityManager->flush();
            $this->addFlash('success', 'Appel d\'offre supprimé avec succès !');
        }

        return $this->redirectToRoute('app_appel_offre_index', ['role' => $request->query->get('role')], Response::HTTP_SEE_OTHER);
    }

    #[Route('/ai/generate-description', name: 'app_appel_offre_ai_generate', methods: ['POST'])]
    public function generateAiDescription(Request $request, \App\Service\AiService $aiService): Response
    {
        $data = json_decode($request->getContent(), true);
        $title = $data['title'] ?? '';
        $type = $data['type'] ?? '';
        $category = $data['category'] ?? '';

        if (!$title) {
            return $this->json(['error' => 'Le titre est obligatoire'], 400);
        }

        $description = $aiService->generateTenderDescription($title, $type, $category);

        return $this->json(['description' => $description]);
    }

    #[Route('/ai/suggest-budget', name: 'app_appel_offre_ai_budget', methods: ['POST'])]
    public function suggestAiBudget(Request $request, \App\Service\AiService $aiService): Response
    {
        $data = json_decode($request->getContent(), true);
        $title = $data['title'] ?? '';
        $type = $data['type'] ?? '';
        $category = $data['category'] ?? '';

        if (!$title) {
            return $this->json(['error' => 'Le titre est obligatoire'], 400);
        }

        $budget = $aiService->suggestBudget($title, $type, $category);

        return $this->json($budget);
    }
}