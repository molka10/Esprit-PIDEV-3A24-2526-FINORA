<?php

namespace App\Controller;

use App\Entity\Bourse;
use App\Form\BourseType;
use App\Repository\BourseRepository;
use App\Service\BourseService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/bourse')]
class BourseController extends AbstractController
{
    public function __construct(
        private BourseService $bourseService,
        private EntityManagerInterface $em
    ) {}

    /**
     * 📋 Liste des bourses
     */
    #[Route('/', name: 'app_bourse_index', methods: ['GET'])]
    public function index(BourseRepository $bourseRepository): Response
    {
        return $this->render('bourse/index.html.twig', [
            'bourses' => $bourseRepository->findAll(),
        ]);
    }

    /**
     * ➕ Créer une bourse
     */
    #[Route('/new', name: 'app_bourse_new', methods: ['GET', 'POST'])]
    public function new(Request $request): Response
    {
        $bourse = new Bourse();

        $form = $this->createForm(BourseType::class, $bourse);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            try {
                $this->bourseService->create($bourse);

                $this->addFlash('success', '✅ Bourse créée avec succès !');
                return $this->redirectToRoute('app_bourse_index');

            } catch (\Exception $e) {
                $this->addFlash('danger', '❌ Erreur : ' . $e->getMessage());
            }
        }

        return $this->render('bourse/new.html.twig', [
            'bourse' => $bourse,
            'form' => $form->createView(), // ✅ IMPORTANT
        ]);
    }

    /**
     * 👁 Voir une bourse
     */
    #[Route('/{id}', name: 'app_bourse_show', methods: ['GET'])]
    public function show(Bourse $bourse): Response
    {
        return $this->render('bourse/show.html.twig', [
            'bourse' => $bourse,
        ]);
    }

    /**
     * ✏ Modifier une bourse
     */
    #[Route('/{id}/edit', name: 'app_bourse_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Bourse $bourse): Response
    {
        $form = $this->createForm(BourseType::class, $bourse);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            try {
                $this->bourseService->update($bourse);

                $this->addFlash('success', '✅ Bourse modifiée avec succès !');
                return $this->redirectToRoute('app_bourse_index');

            } catch (\Exception $e) {
                $this->addFlash('danger', '❌ Erreur : ' . $e->getMessage());
            }
        }

        return $this->render('bourse/edit.html.twig', [
            'bourse' => $bourse,
            'form' => $form->createView(), // ✅ IMPORTANT
        ]);
    }

    /**
     * 🗑 Supprimer une bourse
     */
    #[Route('/{id}', name: 'app_bourse_delete', methods: ['POST'])]
    public function delete(Request $request, Bourse $bourse): Response
    {
        if ($this->isCsrfTokenValid('delete' . $bourse->getId(), $request->request->get('_token'))) {
            try {
                $this->bourseService->delete($bourse);

                $this->addFlash('success', '✅ Bourse supprimée avec succès !');

            } catch (\Exception $e) {
                $this->addFlash('danger', '❌ Impossible de supprimer : ' . $e->getMessage());
            }
        }

        return $this->redirectToRoute('app_bourse_index');
    }

    /**
     * 🟢 Activer
     */
    #[Route('/{id}/activate', name: 'app_bourse_activate', methods: ['POST'])]
    public function activate(Bourse $bourse): Response
    {
        try {
            $this->bourseService->activate($bourse);
            $this->addFlash('success', '✅ Bourse activée !');
        } catch (\Exception $e) {
            $this->addFlash('danger', '❌ Erreur : ' . $e->getMessage());
        }

        return $this->redirectToRoute('app_bourse_index');
    }

    /**
     * 🔴 Désactiver
     */
    #[Route('/{id}/deactivate', name: 'app_bourse_deactivate', methods: ['POST'])]
    public function deactivate(Bourse $bourse): Response
    {
        try {
            $this->bourseService->deactivate($bourse);
            $this->addFlash('success', '✅ Bourse désactivée !');
        } catch (\Exception $e) {
            $this->addFlash('danger', '❌ Erreur : ' . $e->getMessage());
        }

        return $this->redirectToRoute('app_bourse_index');
    }
}