<?php

namespace App\Controller;

use App\Entity\InvestmentManagement;
use App\Form\InvestmentManagementType;
use App\Repository\InvestmentManagementRepository;
use App\Repository\InvestmentRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/management')]
class InvestmentManagementController extends AbstractController
{
    // ================= INDEX + SEARCH =================
    #[Route('/', name: 'app_management_index', methods: ['GET'])]
    public function index(Request $request, InvestmentManagementRepository $repo): Response
    {
        $search = $request->query->get('search');
        $status = $request->query->get('status');

        $qb = $repo->createQueryBuilder('i')
            ->join('i.investment', 'inv');

        if ($search) {
            $qb->andWhere('inv.name LIKE :search OR i.investmentType LIKE :search')
               ->setParameter('search', '%'.$search.'%');
        }

        if ($status && $status !== 'ALL') {
            $qb->andWhere('i.status = :status')
               ->setParameter('status', $status);
        }

        $items = $qb->getQuery()->getResult();

        return $this->render('investment_management/index.html.twig', [
            'items' => $items,
        ]);
    }

    // ================= DASHBOARD =================
    #[Route('/dashboard', name: 'app_management_dashboard')]
    public function dashboard(
        InvestmentManagementRepository $repo,
        InvestmentRepository $investmentRepo
    ): Response
    {
        // 🔹 Management
        $items = $repo->findAll();

        $total = count($items);
        $totalAmount = 0;
        $active = 0;
        $closed = 0;

        foreach ($items as $item) {
            $totalAmount += (float)$item->getAmountInvested();

            if ($item->getStatus() === 'ACTIVE') {
                $active++;
            } else {
                $closed++;
            }
        }

        $average = $total > 0 ? $totalAmount / $total : 0;

        // 🔹 Investments (pour calcul performance seulement)
        $investments = $investmentRepo->findAll();

        $totalValue = 0;
        foreach ($investments as $inv) {
            $totalValue += (float)$inv->getEstimatedValue();
        }

        // 🔥 MÉTIER PERFORMANCE
        $totalInvested = $totalAmount;
        $profit = $totalValue - $totalInvested;
        $roi = $totalInvested > 0 ? ($profit / $totalInvested) * 100 : 0;

        return $this->render('investment_management/dashboard.html.twig', [
            'total' => $total,
            'totalAmount' => $totalAmount,
            'active' => $active,
            'closed' => $closed,
            'average' => $average,
            'items' => $items,

            // 🔥 données métier
            'totalValue' => $totalValue,
            'totalInvested' => $totalInvested,
            'profit' => $profit,
            'roi' => $roi,
        ]);
    }

    // ================= CREATE =================
    #[Route('/new', name: 'app_management_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $item = new InvestmentManagement();

        $form = $this->createForm(InvestmentManagementType::class, $item);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {

            $percent = (float)$item->getOwnershipPercentage();
            $item->setStatus($percent == 100 ? 'CLOSED' : 'ACTIVE');

            $em->persist($item);
            $em->flush();

            $this->addFlash('success', 'Created successfully ✅');

            return $this->redirectToRoute('app_management_dashboard');
        }

        return $this->render('investment_management/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    // ================= SHOW =================
    #[Route('/{managementId}', name: 'app_management_show', methods: ['GET'])]
    public function show(InvestmentManagement $item): Response
    {
        return $this->render('investment_management/show.html.twig', [
            'item' => $item,
        ]);
    }

    // ================= EDIT =================
    #[Route('/{managementId}/edit', name: 'app_management_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, InvestmentManagement $item, EntityManagerInterface $em): Response
    {
        $form = $this->createForm(InvestmentManagementType::class, $item);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {

            $percent = (float)$item->getOwnershipPercentage();
            $item->setStatus($percent == 100 ? 'CLOSED' : 'ACTIVE');

            $em->flush();

            $this->addFlash('success', 'Updated successfully ✏️');

            return $this->redirectToRoute('app_management_dashboard');
        }

        return $this->render('investment_management/edit.html.twig', [
            'form' => $form->createView(),
            'item' => $item,
        ]);
    }

    // ================= DELETE =================
    #[Route('/{managementId}', name: 'app_management_delete', methods: ['POST'])]
    public function delete(Request $request, InvestmentManagement $item, EntityManagerInterface $em): Response
    {
        if ($this->isCsrfTokenValid('delete'.$item->getManagementId(), $request->request->get('_token'))) {

            $em->remove($item);
            $em->flush();

            $this->addFlash('danger', 'Deleted successfully ❌');
        }

        return $this->redirectToRoute('app_management_dashboard');
    }
}