<?php

namespace App\Controller;

use App\Entity\Rating;
use App\Repository\AppelOffreRepository;
use App\Repository\RatingRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/rating')]
class RatingController extends AbstractController
{
    #[Route('/noter/{id}', name: 'app_rating_noter', methods: ['POST'])]
    public function noter(
        Request $request,
        int $id,
        AppelOffreRepository $appelOffreRepository,
        RatingRepository $ratingRepository,
        EntityManagerInterface $entityManager
    ): JsonResponse {
        $appelOffre = $appelOffreRepository->find($id);

        if (!$appelOffre) {
            return new JsonResponse(['error' => 'Appel d\'offre non trouvé'], 404);
        }

        $note = (int) $request->request->get('note');
        $user = $this->getUser();

        if ($note < 1 || $note > 5) {
            return new JsonResponse(['error' => 'La note doit être entre 1 et 5'], 400);
        }

        // 1. Vérifier en base de données si l'utilisateur est connecté
        if ($user) {
            $existingRating = $ratingRepository->findOneBy([
                'user' => $user,
                'appelOffre' => $appelOffre
            ]);

            if ($existingRating) {
                return new JsonResponse([
                    'error' => 'Vous avez déjà évalué cet appel d\'offre avec votre compte.',
                    'already_rated' => true
                ], 400);
            }
        }

        // 2. Vérifier via session (pour les non-connectés ou sécurité sup)
        $session = $request->getSession();
        $notesSession = $session->get('notes_donnees', []);

        if (in_array($id, $notesSession)) {
            return new JsonResponse([
                'error' => 'Vous avez déjà noté cet appel d\'offre.',
                'already_rated' => true
            ], 400);
        }

        // Créer le rating
        $rating = new Rating();
        $rating->setNote($note);
        $rating->setAppelOffre($appelOffre);
        
        if ($user) {
            $rating->setUser($user);
        }

        $entityManager->persist($rating);
        $entityManager->flush();

        // Sauvegarder dans la session
        $notesSession[] = $id;
        $session->set('notes_donnees', $notesSession);

        // Calculer la moyenne
        $moyenne = $ratingRepository->getMoyenne($appelOffre->getId());
        $totalVotes = $ratingRepository->getTotalVotes($appelOffre->getId());

        return new JsonResponse([
            'success' => true,
            'moyenne' => round($moyenne, 1),
            'total_votes' => $totalVotes,
            'note' => $note,
        ]);
    }

    #[Route('/stats/{id}', name: 'app_rating_stats', methods: ['GET'])]
    public function stats(
        Request $request,
        int $id,
        RatingRepository $ratingRepository
    ): JsonResponse {
        $moyenne = $ratingRepository->getMoyenne($id);
        $totalVotes = $ratingRepository->getTotalVotes($id);

        // Vérifier si déjà noté dans la session
        $session = $request->getSession();
        $notesSession = $session->get('notes_donnees', []);
        $dejaNote = in_array($id, $notesSession);

        return new JsonResponse([
            'moyenne' => round($moyenne, 1),
            'total_votes' => $totalVotes,
            'deja_note' => $dejaNote,
        ]);
    }
}