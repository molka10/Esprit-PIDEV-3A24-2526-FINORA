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
            'merged_recommendations' => $this->recommendationsBuilder->getMergedTopForHome(),
            'investments' => $invRepo->findAll(),
            'managements' => $mgRepo->findAll(),
        ]);
    }

    /**
     * 📊 DASHBOARD ADMIN
     */
    #[Route('/admin', name: 'admin_dashboard', methods: ['GET'])]
    public function dashboard(
        InvestmentRepository $investmentRepo,
        InvestmentManagementRepository $managementRepo
    ): Response {

        $investments = $investmentRepo->findAll();

        $totalInvestments = count($investments);

        $activeManagement = count($managementRepo->findBy([
            'status' => 'ACTIVE'
        ]));

        $closedManagement = count($managementRepo->findBy([
            'status' => 'CLOSED'
        ]));

        $totalValue = 0;
        foreach ($investments as $inv) {
            $totalValue += $inv->getEstimatedValue();
        }

        return $this->render('admin/dashboard.html.twig', [
            'totalInvestments' => $totalInvestments,
            'activeManagement' => $activeManagement,
            'closedManagement' => $closedManagement,
            'totalValue' => $totalValue,
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