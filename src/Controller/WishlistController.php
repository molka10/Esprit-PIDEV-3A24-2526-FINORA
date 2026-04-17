<?php

namespace App\Controller;

use App\Entity\Wishlist;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Session\SessionInterface;
use Symfony\Component\Routing\Annotation\Route;
use App\Service\WalletBalanceService;
use App\Service\WishlistPredictorService;

class WishlistController extends AbstractController
{
    #[Route('/wishlist', name: 'wishlist')]
    public function index(Request $request, EntityManagerInterface $em, SessionInterface $session, WalletBalanceService $balanceService, WishlistPredictorService $predictor): Response
    {
        $userId = 6; // To match the active transaction user ID

        // ➕ ADD ITEM
        if ($request->isMethod('POST') && $request->request->get('_action') !== 'set_goal') {

            $name  = $request->request->get('name');
            $price = $request->request->get('price');

            if ($name && $price) {
                $item = new Wishlist();
                $item->setName($name);
                $item->setPrice((float) $price);
                $item->setUserId($userId);

                $em->persist($item);
                $em->flush();
            }

            return $this->redirectToRoute('wishlist');
        }

        // 💰 SAVE GOAL (stored in session — no DB change needed)
        if ($request->isMethod('POST') && $request->request->get('_action') === 'set_goal') {
            $goal = (float) $request->request->get('goal');
            if ($goal > 0) {
                $session->set('wishlist_goal_' . $userId, $goal);
            }
            return $this->redirectToRoute('wishlist');
        }

        // 📋 GET LIST
        $wishlist = $em->getRepository(Wishlist::class)
            ->findBy(['userId' => $userId]);

        // 🧮 Compute totals
        $total = array_reduce($wishlist, fn($carry, $item) => $carry + $item->getPrice(), 0.0);
        $goal  = $session->get('wishlist_goal_' . $userId, 0);
        usort($wishlist, function ($a, $b) {
            return $b->getPrice() <=> $a->getPrice();
        });

        // 🎯 Predictions processing
        $predictions = [];
        $currentBalance = 0;
        foreach ($wishlist as $item) {
            $pred = $predictor->canAffordWishlistItem($userId, $item->getPrice());
            $currentBalance = $pred['current_balance'];
            $predictions[$item->getId()] = $pred;
        }

        // Just in case wishlist is empty
        if (empty($wishlist)) {
            $currentBalance = $balanceService->calculateUserBalance($userId);
        }

        return $this->render('wallet/wishlist.html.twig', [
            'wishlist' => $wishlist,
            'total'    => $total,
            'goal'     => $goal,
            'balance'  => $currentBalance,
            'predictions' => $predictions
        ]);
    }

    // 🗑 DELETE
    #[Route('/wishlist/delete/{id}', name: 'wishlist_delete')]
    public function delete(int $id, EntityManagerInterface $em): Response
    {
        $item = $em->getRepository(Wishlist::class)->find($id);

        if ($item) {
            $em->remove($item);
            $em->flush();
        }

        return $this->redirectToRoute('wishlist');
    }
}