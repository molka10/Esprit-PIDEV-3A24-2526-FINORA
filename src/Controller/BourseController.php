<?php

namespace App\Controller;

use App\Entity\Bourse;
use App\Form\BourseType;
use App\Repository\BourseRepository;
use App\Service\BourseService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/bourse')]
class BourseController extends AbstractController
{
    public function __construct(
        private BourseService $bourseService
    ) {}

    /**
     * ✅ Liste optimisée (1 seule requête SQL)
     */
    #[Route('/', name: 'app_bourse_index', methods: ['GET'])]
    public function index(BourseRepository $bourseRepository): Response
    {
        // 🔥 Optimisé (au lieu de boucle + N requêtes)
        $boursesData = $bourseRepository->findAllWithActionsCount();

        return $this->render('bourse/index.html.twig', [
            'boursesData' => $boursesData,
        ]);
    }

    /**
     * ➕ Créer
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
                $this->addFlash('danger', '❌ ' . $e->getMessage());
            }
        }

        return $this->render('bourse/new.html.twig', [
            'form' => $form,
        ]);
    }

    /**
     * 👁️ Show
     */
    #[Route('/{id}', name: 'app_bourse_show', methods: ['GET'])]
    public function show(Bourse $bourse, BourseRepository $bourseRepository): Response
    {
        $nbActions = $bourseRepository->countActions($bourse);

        return $this->render('bourse/show.html.twig', [
            'bourse' => $bourse,
            'nbActions' => $nbActions,
        ]);
    }

    /**
     * ✏️ Edit
     */
    #[Route('/{id}/edit', name: 'app_bourse_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Bourse $bourse): Response
    {
        $form = $this->createForm(BourseType::class, $bourse);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            try {
                $this->bourseService->update($bourse);

                $this->addFlash('success', '✅ Bourse modifiée !');
                return $this->redirectToRoute('app_bourse_index');

            } catch (\Exception $e) {
                $this->addFlash('danger', '❌ ' . $e->getMessage());
            }
        }

        return $this->render('bourse/edit.html.twig', [
            'form' => $form,
            'bourse' => $bourse,
        ]);
    }

    /**
     * 🗑️ Delete sécurisé
     */
    #[Route('/{id}', name: 'app_bourse_delete', methods: ['POST'])]
    public function delete(Request $request, Bourse $bourse, BourseRepository $repo): Response
    {
        if ($this->isCsrfTokenValid('delete'.$bourse->getId(), $request->request->get('_token'))) {

            $nbActions = $repo->countActions($bourse);

            if ($nbActions > 0) {
                $this->addFlash('danger', "❌ Impossible de supprimer (contient $nbActions actions)");
                return $this->redirectToRoute('app_bourse_index');
            }

            try {
                $this->bourseService->delete($bourse);
                $this->addFlash('success', '✅ Supprimée avec succès');

            } catch (\Exception $e) {
                $this->addFlash('danger', '❌ ' . $e->getMessage());
            }
        }

        return $this->redirectToRoute('app_bourse_index');
    }

    /**
     * ✅ Activate
     */
    #[Route('/{id}/activate', name: 'app_bourse_activate', methods: ['POST'])]
    public function activate(Bourse $bourse): Response
    {
        $this->bourseService->activate($bourse);
        $this->addFlash('success', '✅ Activée');

        return $this->redirectToRoute('app_bourse_index');
    }

    /**
     * ⛔ Deactivate
     */
    #[Route('/{id}/deactivate', name: 'app_bourse_deactivate', methods: ['POST'])]
    public function deactivate(Bourse $bourse): Response
    {
        $this->bourseService->deactivate($bourse);
        $this->addFlash('success', '⛔ Désactivée');

        return $this->redirectToRoute('app_bourse_index');
    }

    /**
     * 📊 (OPTIONNEL) Dashboard stats
     */
    #[Route('/stats', name: 'app_bourse_stats')]
    public function stats(BourseRepository $repo): Response
    {
        $stats = $repo->getStatistics(); // ⚠️ nécessite la méthode

        return $this->render('bourse/stats.html.twig', [
            'stats' => $stats
        ]);
    }
}