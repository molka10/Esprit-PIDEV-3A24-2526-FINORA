<?php

namespace App\Controller;

use App\Entity\AppelOffre;
use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/appel/offre/wishlist')]
#[IsGranted('IS_AUTHENTICATED_FULLY')]
class AppelOffreWishlistController extends AbstractController
{
    #[Route('/toggle/{id}', name: 'app_appel_offre_wishlist_toggle', methods: ['POST'])]
    public function toggle(AppelOffre $appelOffre, EntityManagerInterface $em, Request $request): JsonResponse|Response
    {
        /** @var User $user */
        $user = $this->getUser();
        
        if (!$user) {
            return $this->json(['message' => 'Utilisateur non connecté'], 403);
        }

        if ($user->getFavoriteAppels()->contains($appelOffre)) {
            $user->removeFavoriteAppel($appelOffre);
            $favorite = false;
            $message = 'Appel d\'offre retiré de vos favoris.';
        } else {
            $user->addFavoriteAppel($appelOffre);
            $favorite = true;
            $message = 'Appel d\'offre ajouté à vos favoris !';
        }

        $em->flush();

        if ($request->headers->get('X-Requested-With') === 'XMLHttpRequest') {
            return $this->json([
                'favorite' => $favorite,
                'message' => $message
            ]);
        }

        $this->addFlash('success', $message);
        return $this->redirect($request->headers->get('referer', $this->generateUrl('app_appel_offre_index')));
    }

    #[Route('/my-favorites', name: 'app_appel_offre_wishlist_index')]
    public function index(): Response
    {
        /** @var User $user */
        $user = $this->getUser();
        
        return $this->render('appel_offre/favorites.html.twig', [
            'appel_offres' => $user->getFavoriteAppels(),
            'total_pages' => 1, // Pas de pagination nécessaire pour les favoris personnels en général
            'current_page' => 1
        ]);
    }
}
