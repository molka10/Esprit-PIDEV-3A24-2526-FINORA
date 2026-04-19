<?php

namespace App\Controller;

use App\Entity\Investment;
use App\Entity\InvestmentWishlist;
use App\Repository\InvestmentRepository;
use App\Repository\InvestmentWishlistRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/api/investment-wishlist')]
#[IsGranted('ROLE_USER')]
class InvestmentWishlistController extends AbstractController
{
    #[Route('/toggle/{id}', name: 'app_investment_wishlist_toggle', methods: ['POST'])]
    public function toggle(int $id, InvestmentRepository $investmentRepo, EntityManagerInterface $em, InvestmentWishlistRepository $repository): JsonResponse
    {
        $investment = $investmentRepo->find($id);
        if (!$investment) {
            return new JsonResponse(['status' => 'error', 'message' => 'Investissement introuvable.'], 404);
        }
        
        // 1. Vérification du statut de l'investissement
        if ($investment->getStatus() !== 'ACTIVE') {
            return new JsonResponse(['status' => 'error', 'message' => 'Investissement non disponible.'], 403);
        }

        $user = $this->getUser();
        
        // 2. Vérification de l'existence
        $existingWishlist = $repository->findOneBy([
            'user' => $user,
            'investment' => $investment
        ]);

        if ($existingWishlist) {
            // S'il existe, on le retire (Toggle OFF)
            $em->remove($existingWishlist);
            $em->flush();
            return new JsonResponse(['status' => 'removed', 'message' => 'Retiré de votre wishlist.']);
        }

        // S'il n'existe pas, on l'ajoute (Toggle ON)
        $wishlist = new InvestmentWishlist();
        $wishlist->setUser($user);
        $wishlist->setInvestment($investment);

        $em->persist($wishlist);
        $em->flush();

        return new JsonResponse(['status' => 'added', 'message' => 'Ajouté à votre wishlist !']);
    }

    #[Route('/check/{id}', name: 'app_investment_wishlist_check', methods: ['GET'])]
    public function check(int $id, InvestmentRepository $investmentRepo, InvestmentWishlistRepository $repository): JsonResponse
    {
        $investment = $investmentRepo->find($id);
        if (!$investment) {
            return new JsonResponse(['in_wishlist' => false]);
        }

        $existing = $repository->findOneBy([
            'user' => $this->getUser(),
            'investment' => $investment
        ]);

        return new JsonResponse(['in_wishlist' => $existing !== null]);
    }

    #[Route('/my-ids', name: 'app_investment_wishlist_my_ids', methods: ['GET'])]
    public function myIds(InvestmentWishlistRepository $repository): JsonResponse
    {
        $wishlists = $repository->findBy(['user' => $this->getUser()]);
        $ids = array_map(fn($w) => $w->getInvestment()->getId(), $wishlists);
        
        return new JsonResponse(['ids' => $ids]);
    }

    #[Route('/admin/stats', name: 'app_admin_investment_wishlist_stats', methods: ['GET'])]
    #[IsGranted('ROLE_ADMIN')]
    public function stats(InvestmentWishlistRepository $repository): JsonResponse
    {
        $stats = $repository->getTopWishlistedInvestments(10);
        return new JsonResponse(['stats' => $stats]);
    }
}
