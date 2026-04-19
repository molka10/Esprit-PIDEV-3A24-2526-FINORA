<?php

namespace App\Controller;

use App\Entity\InvestmentManagement;
use App\Service\InvestmentLifecycleService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/api/admin/investments')]
#[IsGranted('ROLE_ADMIN')]
class AdminInvestmentApiController extends AbstractController
{
    #[Route('/{id}/lifecycle', name: 'api_admin_investment_lifecycle', methods: ['GET'])]
    public function getLifecycle(InvestmentManagement $investment, InvestmentLifecycleService $lifecycleService): JsonResponse
    {
        $data = $lifecycleService->getLifecycleData($investment);

        return $this->json($data);
    }
}
