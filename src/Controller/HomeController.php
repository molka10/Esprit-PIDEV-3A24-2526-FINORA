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

        $activeManagements = $managementRepo->findBy(['status' => 'ACTIVE']);
        $closedManagement = count($managementRepo->findBy(['status' => 'CLOSED']));
        $activeManagement = count($activeManagements);

        $totalValue = 0;
        $categoryDistribution = [];
        $riskDistribution = ['LOW' => 0, 'MEDIUM' => 0, 'HIGH' => 0];

        // Les statistiques doivent se baser sur le PORTFOLIO ACTIF et non sur tout le catalogue
        foreach ($activeManagements as $mng) {
            $inv = $mng->getInvestment();
            
            // Calcul de la valeur totale investie
            $totalValue += (float)$mng->getAmountInvested();

            if ($inv) {
                // Catégories
                $cat = ltrim(trim($inv->getCategory() ?? 'Autre'));
                $categoryDistribution[$cat] = ($categoryDistribution[$cat] ?? 0) + 1;

                // Risques
                $risk = $inv->getRiskLevel() ?? 'MEDIUM';
                if (isset($riskDistribution[$risk])) {
                    $riskDistribution[$risk]++;
                }
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
            'invList' => $investments,
            'mngList' => $managementRepo->findAll(),
        ]);
    }
}