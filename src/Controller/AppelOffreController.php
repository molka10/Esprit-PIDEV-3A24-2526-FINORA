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

#[Route('/appel/offre')]
final class AppelOffreController extends AbstractController
{
    #[Route(name: 'app_appel_offre_index', methods: ['GET'])]
public function index(Request $request, AppelOffreRepository $appelOffreRepository, CategorieRepository $categorieRepository, EntityManagerInterface $entityManager): Response
{
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
    $role = $request->getSession()->get('role');

    $appel_offres = $appelOffreRepository->findByFilters(
        $type,
        $statut,
        $categorieId ? (int)$categorieId : null,
        $search,
        $role
    );
    $categories = $categorieRepository->findAll();

    return $this->render('appel_offre/index.html.twig', [
        'appel_offres' => $appel_offres,
        'categories' => $categories,
        'filters' => [
            'type' => $type,
            'statut' => $statut,
            'categorie' => $categorieId,
            'search' => $search,
        ],
    ]);
}

    #[Route('/new', name: 'app_appel_offre_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
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
                    $this->addFlash('success', 'Appel d\'offre créé avec succès !');
                    return $this->redirectToRoute('app_appel_offre_index', [], Response::HTTP_SEE_OTHER);
                }
            } else {
                $entityManager->persist($appelOffre);
                $entityManager->flush();
                $this->addFlash('success', 'Appel d\'offre créé avec succès !');
                return $this->redirectToRoute('app_appel_offre_index', [], Response::HTTP_SEE_OTHER);
            }
        }

        return $this->render('appel_offre/new.html.twig', [
            'appel_offre' => $appelOffre,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_appel_offre_show', methods: ['GET'])]
    public function show(AppelOffre $appelOffre): Response
    {
        return $this->render('appel_offre/show.html.twig', [
            'appel_offre' => $appelOffre,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_appel_offre_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, AppelOffre $appelOffre, EntityManagerInterface $entityManager): Response
    {
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
                    $this->addFlash('success', 'Appel d\'offre modifié avec succès !');
                    return $this->redirectToRoute('app_appel_offre_index', [], Response::HTTP_SEE_OTHER);
                }
            } else {
                $entityManager->flush();
                $this->addFlash('success', 'Appel d\'offre modifié avec succès !');
                return $this->redirectToRoute('app_appel_offre_index', [], Response::HTTP_SEE_OTHER);
            }
        }

        return $this->render('appel_offre/edit.html.twig', [
            'appel_offre' => $appelOffre,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_appel_offre_delete', methods: ['POST'])]
    public function delete(Request $request, AppelOffre $appelOffre, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$appelOffre->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($appelOffre);
            $entityManager->flush();
            $this->addFlash('success', 'Appel d\'offre supprimé avec succès !');
        }

        return $this->redirectToRoute('app_appel_offre_index', [], Response::HTTP_SEE_OTHER);
    }
}