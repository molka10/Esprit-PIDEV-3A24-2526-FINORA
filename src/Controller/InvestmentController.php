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
    #[Route('/', name: 'app_investment_index')]
    public function index(InvestmentRepository $repo): Response
    {
        return $this->render('investment/index.html.twig', [
            'investments' => $repo->findAll(),
        ]);
    }

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

            return $this->redirectToRoute('app_investment_cards');
        }

        return $this->render('investment/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

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

            return $this->redirectToRoute('app_investment_cards');
        }

        return $this->render('investment/edit.html.twig', [
            'form' => $form->createView(),
            'investment' => $investment,
        ]);
    }

    #[Route('/{investmentId}', name: 'app_investment_show')]
    public function show(Investment $investment): Response
    {
        return $this->render('investment/show.html.twig', [
            'investment' => $investment,
        ]);
    }

    #[Route('/{investmentId}', name: 'app_investment_delete', methods: ['POST'])]
    public function delete(Request $request, Investment $investment, EntityManagerInterface $em): Response
    {
        if ($this->isCsrfTokenValid('delete'.$investment->getInvestmentId(), $request->request->get('_token'))) {
            $em->remove($investment);
            $em->flush();
        }

        return $this->redirectToRoute('app_investment_cards');
    }
}