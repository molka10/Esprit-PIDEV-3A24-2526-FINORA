<?php

namespace App\Controller;

use App\Entity\InvestmentManagement;
use App\Form\InvestmentManagementType;
use App\Repository\InvestmentManagementRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/management')]
class InvestmentManagementController extends AbstractController
{
    #[Route('/', name: 'app_management_index', methods: ['GET'])]
    public function index(InvestmentManagementRepository $repo): Response
    {
        return $this->render('investment_management/index.html.twig', [
            'items' => $repo->findAll(),
        ]);
    }

    #[Route('/new', name: 'app_management_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $item = new InvestmentManagement();

        $form = $this->createForm(InvestmentManagementType::class, $item);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {

            // 🔥 BUSINESS LOGIC
            $percent = (float)$item->getOwnershipPercentage();
            $item->setStatus($percent == 100 ? 'CLOSED' : 'ACTIVE');

            $em->persist($item);
            $em->flush();

            $this->addFlash('success', 'Created successfully ✅');

            return $this->redirectToRoute('app_management_index');
        }

        return $this->render('investment_management/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{managementId}', name: 'app_management_show', methods: ['GET'])]
    public function show(InvestmentManagement $item): Response
    {
        return $this->render('investment_management/show.html.twig', [
            'item' => $item,
        ]);
    }

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

            return $this->redirectToRoute('app_management_index');
        }

        return $this->render('investment_management/edit.html.twig', [
            'form' => $form->createView(),
            'item' => $item,
        ]);
    }

    #[Route('/{managementId}', name: 'app_management_delete', methods: ['POST'])]
    public function delete(Request $request, InvestmentManagement $item, EntityManagerInterface $em): Response
    {
        if ($this->isCsrfTokenValid('delete'.$item->getManagementId(), $request->request->get('_token'))) {

            $em->remove($item);
            $em->flush();

            $this->addFlash('danger', 'Deleted successfully ❌');
        }

        return $this->redirectToRoute('app_management_index');
    }
}