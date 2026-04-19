<?php

namespace App\Controller;

use App\Repository\InvestmentManagementRepository;
use App\Repository\InvestmentRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

class InvestorProfileController extends AbstractController
{
    #[Route('/front/profile', name: 'app_investor_profile', methods: ['GET'])]
    #[IsGranted('IS_AUTHENTICATED_FULLY')]
    public function profile(
        InvestmentManagementRepository $managementRepo,
        InvestmentRepository $investmentRepo
    ): Response {
        $user = $this->getUser();

        // Fetch management records owned by this investor
        $managements = $managementRepo->findBy(['user' => $user]);

        // Fetch investments owned by THIS user (if they are an entreprise/investor creator)
        $investments = $investmentRepo->findBy(['user' => $user]);

        // Stats calculation
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
