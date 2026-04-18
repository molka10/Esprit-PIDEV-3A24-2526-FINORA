<?php

namespace App\Controller;

use App\Entity\Formation;
use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/wishlist')]
#[IsGranted('IS_AUTHENTICATED_FULLY')]
class WishlistController extends AbstractController
{
    #[Route('/', name: 'app_wishlist_index')]
    public function index(): Response
    {
        /** @var User $user */
        $user = $this->getUser();
        $wishlist = $user->getWishlist();

        return $this->render('wishlist/index.html.twig', [
            'wishlist' => $wishlist,
        ]);
    }

    #[Route('/toggle/{id}', name: 'app_wishlist_toggle', methods: ['POST'])]
    public function toggle(Formation $formation, EntityManagerInterface $entityManager): Response
    {
        /** @var User $user */
        $user = $this->getUser();

        if ($user->getWishlist()->contains($formation)) {
            $user->removeFromWishlist($formation);
            $status = 'removed';
            $message = 'Formation retirée de votre wishlist.';
        } else {
            $user->addToWishlist($formation);
            $status = 'added';
            $message = 'Formation ajoutée à votre wishlist !';
        }

        $entityManager->flush();

        return $this->json([
            'status' => $status,
            'message' => $message
        ]);
    }
}
