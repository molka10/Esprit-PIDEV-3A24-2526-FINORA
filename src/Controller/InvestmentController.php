<?php

namespace App\Controller;

use App\Entity\Investment;
use App\Form\InvestmentType;
use App\Repository\InvestmentRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/investment')]
class InvestmentController extends AbstractController
{
    // ================= INDEX =================
    #[Route('/', name: 'app_investment_index')]
    public function index(InvestmentRepository $repo): Response
    {
        return $this->render('investment/index.html.twig', [
            'investments' => $repo->findAll(),
        ]);
    }

    // ================= DASHBOARD 🔥 =================
    #[Route('/dashboard', name: 'app_investment_dashboard')]
    public function dashboard(InvestmentRepository $repo): Response
    {
        $investments = $repo->findAll();

        $total = count($investments);
        $totalValue = 0;
        $high = 0;
        $medium = 0;
        $low = 0;

        foreach ($investments as $inv) {
            $value = (float)$inv->getEstimatedValue();
            $totalValue += $value;

            // 🔥 stats risk
            if ($inv->getRiskLevel() === 'HIGH') {
                $high++;
            } elseif ($inv->getRiskLevel() === 'MEDIUM') {
                $medium++;
            } else {
                $low++;
            }
        }

        $average = $total > 0 ? $totalValue / $total : 0;

        return $this->render('investment/dashboard.html.twig', [
            'investments' => $investments,
            'total' => $total,
            'totalValue' => $totalValue,
            'average' => $average,
            'high' => $high,
            'medium' => $medium,
            'low' => $low,
        ]);
    }

    // ================= CARDS =================
    #[Route('/cards', name: 'app_investment_cards')]
    public function cards(InvestmentRepository $repo): Response
    {
        $investments = $repo->findAll();

        $totalValue = 0;
        foreach ($investments as $inv) {
            $totalValue += (float)$inv->getEstimatedValue();
        }

        return $this->render('investment/cards.html.twig', [
            'investments' => $investments,
            'totalValue' => $totalValue,
            'totalCount' => count($investments),
        ]);
    }

    // ================= CREATE =================
    #[Route('/new', name: 'app_investment_new')]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $investment = new Investment();

        $form = $this->createForm(InvestmentType::class, $investment);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {

            $value = (float)$investment->getEstimatedValue();

            if ($value > 1000000) {
                $investment->setRiskLevel('HIGH');
            } elseif ($value > 200000) {
                $investment->setRiskLevel('MEDIUM');
            } else {
                $investment->setRiskLevel('LOW');
            }

            $em->persist($investment);
            $em->flush();

            return $this->redirectToRoute('app_investment_dashboard'); // 🔥 redirect dashboard
        }

        return $this->render('investment/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    // ================= EDIT =================
    #[Route('/{investmentId}/edit', name: 'app_investment_edit')]
    public function edit(Request $request, Investment $investment, EntityManagerInterface $em): Response
    {
        $form = $this->createForm(InvestmentType::class, $investment);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {

            $value = (float)$investment->getEstimatedValue();

            if ($value > 1000000) {
                $investment->setRiskLevel('HIGH');
            } elseif ($value > 200000) {
                $investment->setRiskLevel('MEDIUM');
            } else {
                $investment->setRiskLevel('LOW');
            }

            $em->flush();

            return $this->redirectToRoute('app_investment_dashboard');
        }

        return $this->render('investment/edit.html.twig', [
            'form' => $form->createView(),
            'investment' => $investment,
        ]);
    }

    // ================= SHOW =================
    #[Route('/{investmentId}', name: 'app_investment_show')]
    public function show(Investment $investment): Response
    {
        return $this->render('investment/show.html.twig', [
            'investment' => $investment,
        ]);
    }

    // ================= DELETE =================
    #[Route('/{investmentId}', name: 'app_investment_delete', methods: ['POST'])]
    public function delete(Request $request, Investment $investment, EntityManagerInterface $em): Response
    {
        if ($this->isCsrfTokenValid('delete'.$investment->getInvestmentId(), $request->request->get('_token'))) {
            $em->remove($investment);
            $em->flush();
        }

        return $this->redirectToRoute('app_investment_dashboard');
    }
}