<?php

namespace App\Controller;

use App\Entity\CentreFormation;
use App\Form\CentreFormationType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

final class CentreFormationController extends AbstractController
{
    // ────────────────────────────────────────────────────────────────
    //  ADMIN — List
    // ────────────────────────────────────────────────────────────────
    #[Route('/admin/centres', name: 'admin_centres_index', methods: ['GET'])]
    public function index(EntityManagerInterface $em): Response
    {
        $centres = $em->getRepository(CentreFormation::class)->findBy([], ['createdAt' => 'DESC']);

        return $this->render('admin/centres/index.html.twig', [
            'centres' => $centres,
        ]);
    }

    // ────────────────────────────────────────────────────────────────
    //  ADMIN — New
    // ────────────────────────────────────────────────────────────────
    #[Route('/admin/centres/nouveau', name: 'admin_centres_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $centre = new CentreFormation();
        $form   = $this->createForm(CentreFormationType::class, $centre);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($centre);
            $em->flush();

            $this->addFlash('success', 'Centre de formation ajouté avec succès.');
            return $this->redirectToRoute('admin_centres_index');
        }

        return $this->render('admin/centres/form.html.twig', [
            'form'   => $form->createView(),
            'centre' => $centre,
            'edit'   => false,
        ]);
    }

    // ────────────────────────────────────────────────────────────────
    //  ADMIN — Edit
    // ────────────────────────────────────────────────────────────────
    #[Route('/admin/centres/{id}/modifier', name: 'admin_centres_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, CentreFormation $centre, EntityManagerInterface $em): Response
    {
        $form = $this->createForm(CentreFormationType::class, $centre);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush();

            $this->addFlash('success', 'Centre de formation mis à jour.');
            return $this->redirectToRoute('admin_centres_index');
        }

        return $this->render('admin/centres/form.html.twig', [
            'form'   => $form->createView(),
            'centre' => $centre,
            'edit'   => true,
        ]);
    }

    // ────────────────────────────────────────────────────────────────
    //  ADMIN — Delete
    // ────────────────────────────────────────────────────────────────
    #[Route('/admin/centres/{id}/supprimer', name: 'admin_centres_delete', methods: ['POST'])]
    public function delete(Request $request, CentreFormation $centre, EntityManagerInterface $em): Response
    {
        if ($this->isCsrfTokenValid('delete_centre_' . $centre->getId(), $request->request->get('_token'))) {
            $em->remove($centre);
            $em->flush();
            $this->addFlash('success', 'Centre supprimé.');
        }

        return $this->redirectToRoute('admin_centres_index');
    }

    // ────────────────────────────────────────────────────────────────
    //  PUBLIC JSON API — all active centres (used by front map)
    // ────────────────────────────────────────────────────────────────
    #[Route('/api/centres', name: 'api_centres_json', methods: ['GET'])]
    public function apiCentres(EntityManagerInterface $em): JsonResponse
    {
        try {
            $centres = $em->getRepository(CentreFormation::class)->findAll();
            
            // Manual filter as a safety against missing columns in old database schemas
            $activeCentres = array_filter($centres, fn($c) => $c->isActive());

            $data = [];
            foreach ($activeCentres as $c) {
                $data[] = [
                    'id'          => $c->getId(),
                    'nom'         => $c->getNom(),
                    'adresse'     => $c->getAdresse(),
                    'ville'       => $c->getVille(),
                    'latitude'    => $c->getLatitude(),
                    'longitude'   => $c->getLongitude(),
                    'description' => $c->getDescription(),
                    'telephone'   => $c->getTelephone(),
                    'email'       => $c->getEmail(),
                    'siteWeb'     => $c->getSiteWeb(),
                    'avgRating'   => $c->getAverageRating(),
                    'reviewCount' => count($c->getRatings()),
                ];
            }

            return $this->json($data);
        } catch (\Exception $e) {
            return $this->json(['error' => $e->getMessage()], 500);
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  PUBLIC — Map page (user-facing)
    // ────────────────────────────────────────────────────────────────
    #[Route('/carte', name: 'public_centres_map', methods: ['GET'])]
    public function publicMap(EntityManagerInterface $entityManager): Response
    {
        $centres = $entityManager->getRepository(CentreFormation::class)->findAll();
        
        return $this->render('centres/map.html.twig', [
            'centres' => $centres
        ]);
    }

    // ────────────────────────────────────────────────────────────────
    //  PUBLIC — Detail page with Reviews
    // ────────────────────────────────────────────────────────────────
    #[Route('/centres/{id}', name: 'public_centres_show', methods: ['GET'])]
    public function show(CentreFormation $centre): Response
    {
        return $this->render('centres/show.html.twig', [
            'centre' => $centre,
        ]);
    }

    // ────────────────────────────────────────────────────────────────
    //  PUBLIC — Submit Rating (POST)
    // ────────────────────────────────────────────────────────────────
    #[Route('/centres/{id}/avis', name: 'public_centres_rate', methods: ['POST'])]
    public function rate(Request $request, CentreFormation $centre, EntityManagerInterface $em): Response
    {
        /** @var \App\Entity\User $user */
        $user = $this->getUser();
        if (!$user) {
            $this->addFlash('error', 'Vous devez être connecté pour laisser un avis.');
            return $this->redirectToRoute('app_login');
        }

        $note = (int) $request->request->get('note');
        $commentaire = $request->request->get('commentaire');

        if ($note < 1 || $note > 5) {
            $this->addFlash('error', 'Note invalide.');
            return $this->redirectToRoute('public_centres_show', ['id' => $centre->getId()]);
        }

        $rating = new \App\Entity\RatingCentre();
        $rating->setUser($user)
               ->setCentre($centre)
               ->setNote($note)
               ->setCommentaire($commentaire);

        $em->persist($rating);
        $em->flush();

        $this->addFlash('success', 'Merci ! Votre avis a été enregistré.');
        return $this->redirectToRoute('public_centres_show', ['id' => $centre->getId()]);
    }
}
