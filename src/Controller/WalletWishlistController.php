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

class WalletWishlistController extends AbstractController
{
    #[Route('/wallet/wishlist', name: 'wishlist')]
    public function index(Request $request, EntityManagerInterface $em, SessionInterface $session, WalletBalanceService $balanceService, WishlistPredictorService $predictor): Response
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }
        $userId = $user->getId();

        // Ã¢Å¾â€¢ ADD ITEM
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

        // Ã°Å¸â€™Â° SAVE GOAL (stored in session Ã¢â‚¬â€ no DB change needed)
        if ($request->isMethod('POST') && $request->request->get('_action') === 'set_goal') {
            $goal = (float) $request->request->get('goal');
            if ($goal > 0) {
                $session->set('wishlist_goal_' . $userId, $goal);
            }
            return $this->redirectToRoute('wishlist');
        }

        // Ã°Å¸â€œâ€¹ GET LIST
        $wishlist = $em->getRepository(Wishlist::class)
            ->findBy(['userId' => $userId]);

        // Ã°Å¸Â§Â® Compute totals
        $total = array_reduce($wishlist, fn($carry, $item) => $carry + $item->getPrice(), 0.0);
        $goal  = $session->get('wishlist_goal_' . $userId, 0);
        usort($wishlist, function ($a, $b) {
            return $b->getPrice() <=> $a->getPrice();
        });

        // Ã°Å¸Å½Â¯ Predictions processing
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

    // Ã°Å¸â€”â€˜ DELETE
    #[Route('/wallet/wishlist/delete/{id}', name: 'wishlist_delete')]
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
