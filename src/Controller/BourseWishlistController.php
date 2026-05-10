<?php

namespace App\Controller;

use App\Entity\User;
use App\Entity\Action;
use App\Entity\BourseWishlist;
use App\Repository\ActionRepository;
use App\Repository\BourseWishlistRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/api/bourse-wishlist')]
#[IsGranted('ROLE_USER')]
class BourseWishlistController extends AbstractController
{
    #[Route('/', name: 'app_bourse_wishlist_index')]
    public function index(BourseWishlistRepository $repository): Response
    {
        $wishlist = $repository->findBy(['user' => $this->getUser()]);

        return $this->render('bourse/wishlist.html.twig', [
            'wishlist' => $wishlist,
        ]);
    }

    #[Route('/toggle/{id}', name: 'app_bourse_wishlist_toggle', methods: ['POST'])]
    public function toggle(int $id, ActionRepository $actionRepo, EntityManagerInterface $em, BourseWishlistRepository $repository): JsonResponse
    {
        $action = $actionRepo->find($id);
        if (!$action) {
            return new JsonResponse(['status' => 'error', 'message' => 'Action introuvable.'], 404);
        }

        $user = $this->getUser();
        
        $existingWishlist = $repository->findOneBy([
            'user' => $user,
            'action' => $action
        ]);

        if ($existingWishlist) {
            $em->remove($existingWishlist);
            $em->flush();
            return new JsonResponse(['status' => 'removed', 'message' => 'Action retirée de votre wishlist.']);
        }

        $wishlist = new BourseWishlist();
        if ($user instanceof User) {
            $wishlist->setUser($user);
        }
        $wishlist->setAction($action);

        $em->persist($wishlist);
        $em->flush();

        return new JsonResponse(['status' => 'added', 'message' => 'Action ajoutée à votre wishlist !']);
    }

    #[Route('/my-ids', name: 'app_bourse_wishlist_my_ids', methods: ['GET'])]
    public function myIds(BourseWishlistRepository $repository): JsonResponse
    {
        $wishlists = $repository->findBy(['user' => $this->getUser()]);
        $ids = array_map(fn($w) => $w->getAction()->getId(), $wishlists);
        
        return new JsonResponse(['ids' => $ids]);
    }

    #[Route('/set-target-price/{id}', name: 'app_bourse_wishlist_set_target', methods: ['POST'])]
    public function setTargetPrice(int $id, Request $request, BourseWishlistRepository $repository, EntityManagerInterface $em): JsonResponse
    {
        $wishlist = $repository->findOneBy([
            'user' => $this->getUser(),
            'action' => $id
        ]);

        if (!$wishlist) {
            return new JsonResponse(['status' => 'error', 'message' => 'Cette action n\'est pas dans votre wishlist.'], 404);
        }

        $data = json_decode($request->getContent(), true);
        $price = $data['price'] ?? null;

        if ($price === null) {
            return new JsonResponse(['status' => 'error', 'message' => 'Prix invalide.'], 400);
        }

        $wishlist->setTargetPrice((string)$price);
        $em->flush();

        return new JsonResponse(['status' => 'success', 'message' => 'Alerte de prix configurée !']);
    }
}
