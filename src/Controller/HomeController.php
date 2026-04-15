<?php

namespace App\Controller;

use App\Repository\InvestmentRepository;
use App\Repository\InvestmentManagementRepository;
use App\Service\RecommendationsBuilder;
use App\Service\PortfolioAnalyticsService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class HomeController extends AbstractController
{
    private RecommendationsBuilder $recommendationsBuilder;
    private PortfolioAnalyticsService $analyticsService;

    public function __construct(
        RecommendationsBuilder $recommendationsBuilder,
        PortfolioAnalyticsService $analyticsService
    ) {
        $this->recommendationsBuilder = $recommendationsBuilder;
        $this->analyticsService = $analyticsService;
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
        Request $request,
        InvestmentRepository $investmentRepo,
        InvestmentManagementRepository $managementRepo
    ): Response {
        if ($request->getSession()->get('role') !== 'admin') {
            return $this->redirectToRoute('choose_role');
        }

        $investments = $investmentRepo->findAll();
        $managements = $managementRepo->findAll();

        $stats = $this->analyticsService->getDashboardStats($investments, $managements);

        return $this->render('admin/dashboard.html.twig', array_merge($stats, [
            'invList' => $investments,
            'mngList' => $managements,
        ]));
    }
}