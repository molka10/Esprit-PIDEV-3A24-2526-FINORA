<?php

namespace App\Controller;

use App\Repository\InvestmentManagementRepository;
use App\Repository\InvestmentRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class InvestorProfileController extends AbstractController
{
    #[Route('/investor/profile', name: 'app_investor_profile', methods: ['GET'])]
    public function profile(
        Request $request,
        InvestmentManagementRepository $managementRepo,
        InvestmentRepository $investmentRepo
    ): Response {
        $role = $request->getSession()->get('role');
        if ($role !== 'investisseur') {
            return $this->redirectToRoute('choose_role');
        }

        $userId = $request->getSession()->get('user_id');

        // Fetch management records owned by this investor
        $managements = $managementRepo->findBy(['createdByUserId' => $userId]);

        // Fetch investments owned by this investor
        $investments = $investmentRepo->findBy(['createdByUserId' => $userId]);

        // Stats
        $totalInvested = 0;
        $activeCount = 0;
        $closedCount = 0;
        foreach ($managements as $m) {
            $totalInvested += (float) $m->getAmountInvested();
            if ($m->getStatus() === 'ACTIVE') {
                $activeCount++;
            } elseif ($m->getStatus() === 'CLOSED') {
                $closedCount++;
            }
        }

        return $this->render('investor/profile.html.twig', [
            'managements'   => $managements,
            'investments'   => $investments,
            'totalInvested' => $totalInvested,
            'activeCount'   => $activeCount,
            'closedCount'   => $closedCount,
            'totalMgmt'     => count($managements),
        ]);
    }
}
