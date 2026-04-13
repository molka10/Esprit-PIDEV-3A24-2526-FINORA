<?php

namespace App\Controller;

use App\Repository\InvestmentRepository;
use App\Repository\InvestmentManagementRepository;
use App\Service\RecommendationsBuilder;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class HomeController extends AbstractController
{
    private RecommendationsBuilder $recommendationsBuilder;

    public function __construct(RecommendationsBuilder $recommendationsBuilder)
    {
        $this->recommendationsBuilder = $recommendationsBuilder;
    }

    /**
     * 🏠 PAGE HOME
     */
    #[Route('/', name: 'app_home', methods: ['GET'])]
    public function index(
        InvestmentRepository $invRepo,
        InvestmentManagementRepository $mgRepo
    ): Response {

        return $this->render('home/index.html.twig', [
            'top3_interne' => $this->recommendationsBuilder->getTop3Interne(),
            'top3_externe' => $this->recommendationsBuilder->getTop3Externe(),
            'investments' => $invRepo->findAll(),
            'managements' => $mgRepo->findAll(),
        ]);
    }

    /**
     * 📊 DASHBOARD ADMIN
     */
    #[Route('/admin', name: 'admin_dashboard', methods: ['GET'])]
    public function dashboard(
        \Symfony\Component\HttpFoundation\Request $request,
        InvestmentRepository $investmentRepo,
        InvestmentManagementRepository $managementRepo
    ): Response {
        if ($request->getSession()->get('role') !== 'admin') {
            return $this->redirectToRoute('choose_role');
        }

        $investments = $investmentRepo->findAll();
        $totalInvestments = count($investments);

        $activeManagement = count($managementRepo->findBy(['status' => 'ACTIVE']));
        $closedManagement = count($managementRepo->findBy(['status' => 'CLOSED']));

        $totalValue = 0;
        $categoryDistribution = [];
        $riskDistribution = ['LOW' => 0, 'MEDIUM' => 0, 'HIGH' => 0];

        foreach ($investments as $inv) {
            $totalValue += (float)$inv->getEstimatedValue();

            $cat = ltrim(trim($inv->getCategory() ?? 'Autre'));
            $categoryDistribution[$cat] = ($categoryDistribution[$cat] ?? 0) + 1;

            $risk = $inv->getRiskLevel() ?? 'MEDIUM';
            if (isset($riskDistribution[$risk])) {
                $riskDistribution[$risk]++;
            }
        }

        return $this->render('admin/dashboard.html.twig', [
            'totalInvestments' => $totalInvestments,
            'activeManagement' => $activeManagement,
            'closedManagement' => $closedManagement,
            'totalValue' => $totalValue,
            'chartCategories' => json_encode(array_keys($categoryDistribution)),
            'chartCategoryData' => json_encode(array_values($categoryDistribution)),
            'chartRisks' => json_encode(array_values($riskDistribution)),
        ]);
    }

    /**
     * 📁 LISTE INVESTMENTS
     */
    #[Route('/investments', name: 'app_investment_index', methods: ['GET'])]
    public function investments(InvestmentRepository $repo): Response
    {
        return $this->render('investment/index.html.twig', [
            'investments' => $repo->findAll(),
        ]);
    }

    /**
     * 📁 LISTE MANAGEMENT
     */
    #[Route('/management', name: 'app_management_index', methods: ['GET'])]
    public function management(InvestmentManagementRepository $repo): Response
    {
        return $this->render('investment_management/index.html.twig', [
            'investment_managements' => $repo->findAll(),
        ]);
    }
}