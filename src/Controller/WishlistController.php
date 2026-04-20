<?php

namespace App\Controller;

use App\Entity\BourseWishlist;
use App\Entity\InvestmentWishlist;
use App\Entity\Wishlist as WalletWishlist;
use App\Entity\Action;
use App\Entity\Investment;
use App\Repository\BourseWishlistRepository;
use App\Repository\InvestmentWishlistRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/wishlist')]
#[IsGranted('IS_AUTHENTICATED_FULLY')]
class WishlistController extends AbstractController
{
    #[Route('/my-wishlist', name: 'app_unified_wishlist')]
    public function index(EntityManagerInterface $em): Response
    {
        /** @var User $user */
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        // 1. Formations (Many-to-Many)
        $formations = $user->getWishlist();

        // 2. Bourse (BourseWishlist Entity)
        $bourseItems = $em->getRepository(BourseWishlist::class)->findBy(['user' => $user]);

        // 3. Investment (InvestmentWishlist Entity)
        $investmentItems = $em->getRepository(InvestmentWishlist::class)->findBy(['user' => $user]);

        // 4. Wallet (WalletWishlist Entity)
        $walletItems = $em->getRepository(WalletWishlist::class)->findBy(['userId' => $user->getId()]);

        return $this->render('wishlist/unified.html.twig', [
            'formations'  => $formations,
            'bourseItems' => $bourseItems,
            'investmentItems' => $investmentItems,
            'walletItems' => $walletItems,
        ]);
    }

    #[Route('/formations', name: 'app_wishlist_index')]
    public function formationsWishlist(): Response
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
