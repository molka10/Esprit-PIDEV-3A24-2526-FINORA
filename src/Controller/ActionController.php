<?php

namespace App\Controller;

use App\Entity\Action;
use App\Form\ActionType;
use App\Repository\ActionRepository;
use App\Service\ActionService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/action')]
class ActionController extends AbstractController
{
    public function __construct(
        private ActionService $actionService,
        private EntityManagerInterface $em
    ) {}

    /**
     * Liste de toutes les actions
     */
    #[Route('/', name: 'app_action_index', methods: ['GET'])]
    public function index(ActionRepository $actionRepository): Response
    {
        return $this->render('action/index.html.twig', [
            'actions' => $actionRepository->findAll(),
        ]);
    }

    /**
     * Créer une nouvelle action
     */
    #[Route('/new', name: 'app_action_new', methods: ['GET', 'POST'])]
    public function new(Request $request): Response
    {
        $action = new Action();
        $form = $this->createForm(ActionType::class, $action);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            try {
                // Utiliser le service pour la logique métier
                $this->actionService->create($action);
                
                $this->addFlash('success', '✅ Action créée avec succès !');
                return $this->redirectToRoute('app_action_index');
                
            } catch (\Exception $e) {
                $this->addFlash('danger', '❌ Erreur : ' . $e->getMessage());
            }
        }

        return $this->render('action/new.html.twig', [
            'action' => $action,
            'form' => $form,
        ]);
    }

    /**
     * Afficher les détails d'une action
     */
    #[Route('/{id}', name: 'app_action_show', methods: ['GET'])]
    public function show(Action $action): Response
    {
        return $this->render('action/show.html.twig', [
            'action' => $action,
        ]);
    }

    /**
     * Modifier une action existante
     */
    #[Route('/{id}/edit', name: 'app_action_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Action $action): Response
    {
        $form = $this->createForm(ActionType::class, $action);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            try {
                // Utiliser le service pour la logique métier
                $this->actionService->update($action);
                
                $this->addFlash('success', '✅ Action modifiée avec succès !');
                return $this->redirectToRoute('app_action_index');
                
            } catch (\Exception $e) {
                $this->addFlash('danger', '❌ Erreur : ' . $e->getMessage());
            }
        }

        return $this->render('action/edit.html.twig', [
            'action' => $action,
            'form' => $form,
        ]);
    }

    /**
     * Supprimer une action
     */
    #[Route('/{id}', name: 'app_action_delete', methods: ['POST'])]
    public function delete(Request $request, Action $action): Response
    {
        if ($this->isCsrfTokenValid('delete' . $action->getId(), $request->request->get('_token'))) {
            try {
                // Utiliser le service pour la logique métier
                $this->actionService->delete($action);
                
                $this->addFlash('success', '✅ Action supprimée avec succès !');
                
            } catch (\Exception $e) {
                $this->addFlash('danger', '❌ Impossible de supprimer : ' . $e->getMessage());
            }
        }

        return $this->redirectToRoute('app_action_index');
    }

    /**
     * Mettre à jour le stock d'une action
     */
    #[Route('/{id}/update-stock', name: 'app_action_update_stock', methods: ['POST'])]
    public function updateStock(Request $request, Action $action): Response
    {
        $quantite = (int) $request->request->get('quantite');

        try {
            $this->actionService->updateStock($action, $quantite);
            $this->addFlash('success', '✅ Stock mis à jour avec succès !');
        } catch (\Exception $e) {
            $this->addFlash('danger', '❌ Erreur : ' . $e->getMessage());
        }

        return $this->redirectToRoute('app_action_show', ['id' => $action->getId()]);
    }

    /**
     * Rechercher des actions
     */
    #[Route('/search', name: 'app_action_search', methods: ['GET'])]
    public function search(Request $request): Response
    {
        $query = $request->query->get('q', '');
        
        if (empty($query)) {
            return $this->redirectToRoute('app_action_index');
        }

        $actions = $this->actionService->search($query);

        return $this->render('action/index.html.twig', [
            'actions' => $actions,
            'search_query' => $query,
        ]);
    }

    /**
     * Filtrer par secteur
     */
    #[Route('/secteur/{secteur}', name: 'app_action_by_secteur', methods: ['GET'])]
    public function bySecteur(string $secteur): Response
    {
        $actions = $this->actionService->findBySecteur($secteur);

        return $this->render('action/index.html.twig', [
            'actions' => $actions,
            'secteur_filter' => $secteur,
        ]);
}}