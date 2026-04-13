<?php

namespace App\Controller;

use App\Entity\InvestmentManagement;
use App\Form\InvestmentManagementType;
use App\Repository\InvestmentManagementRepository;
use Doctrine\ORM\EntityManagerInterface;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/management')]
class InvestmentManagementController extends AbstractController
{
    // 🔐 ROLE
    private function getRole(Request $request)
    {
        return $request->getSession()->get('role');
    }

    private function checkAccess(Request $request)
    {
        if (!in_array($this->getRole($request), ['admin', 'investisseur', 'user'])) {
            return $this->redirectToRoute('choose_role');
        }
        return null;
    }

    private function checkInvestisseur(Request $request)
    {
        if ($this->getRole($request) !== 'investisseur') {
            return $this->redirectToRoute('app_management_index');
        }
        return null;
    }

    // ================= INDEX (COMME FORMATION) =================
    #[Route('/', name: 'app_management_index')]
    public function index(Request $request, InvestmentManagementRepository $repo, PaginatorInterface $paginator): Response
    {
        if ($redirect = $this->checkAccess($request)) return $redirect;

        $search = trim((string)$request->query->get('search', ''));
        $status = trim((string)$request->query->get('status', ''));
        $tri = (string)$request->query->get('tri', 'managementId');
        $ordre = strtolower((string)$request->query->get('ordre', 'desc'));

        $qb = $repo->createQueryBuilder('m')
                   ->leftJoin('m.investment', 'i')
                   ->addSelect('i');

        if ($search !== '') {
            $qb->andWhere('LOWER(m.investmentType) LIKE LOWER(:search)')
               ->setParameter('search', '%' . $search . '%');
        }

        if ($status !== '') {
            $qb->andWhere('m.status = :status')
               ->setParameter('status', $status);
        }

        $allowedSortFields = ['managementId', 'amountInvested', 'ownershipPercentage', 'status'];
        if (!in_array($tri, $allowedSortFields, true)) {
            $tri = 'managementId';
        }

        $ordre = $ordre === 'asc' ? 'ASC' : 'DESC';
        $qb->orderBy('m.' . $tri, $ordre);

        $query = $qb->getQuery();
        $page = $request->query->getInt('page', 1);

        $investment_managements = $paginator->paginate(
            $query,
            $page,
            6
        );

        return $this->render('investment_management/index.html.twig', [
            'investment_managements' => $investment_managements,
            'search' => $search,
            'status' => $status,
            'tri' => $tri,
            'ordre' => strtolower($ordre),
        ]);
    }

    // ================= CREATE =================
    #[Route('/new', name: 'app_management_new')]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        if ($redirect = $this->checkInvestisseur($request)) return $redirect;

        $item = new InvestmentManagement();
        
        $inv_id = $request->query->get('inv_id');
        if ($inv_id) {
            $investment = $em->getRepository(\App\Entity\Investment::class)->find($inv_id);
            if ($investment) {
                $item->setInvestment($investment);
            }
        }

        $form = $this->createForm(InvestmentManagementType::class, $item);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {

            $em->persist($item);
            $em->flush();

            return $this->redirectToRoute('app_management_index');
        }

        return $this->render('investment_management/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    // ================= SHOW =================
    #[Route('/{id}', name: 'app_management_show')]
    public function show(Request $request, InvestmentManagement $item): Response
    {
        if ($redirect = $this->checkAccess($request)) return $redirect;

        return $this->render('investment_management/show.html.twig', [
            'item' => $item,
        ]);
    }

    // ================= EDIT =================
    #[Route('/{id}/edit', name: 'app_management_edit')]
    public function edit(Request $request, InvestmentManagement $item, EntityManagerInterface $em): Response
    {
        if ($redirect = $this->checkInvestisseur($request)) return $redirect;

        $form = $this->createForm(InvestmentManagementType::class, $item);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {

            $em->flush();

            return $this->redirectToRoute('app_management_index');
        }

        return $this->render('investment_management/edit.html.twig', [
            'form' => $form->createView(),
            'item' => $item,
        ]);
    }

    // ================= DELETE =================
    #[Route('/{id}', name: 'app_management_delete', methods: ['POST'])]
    public function delete(Request $request, InvestmentManagement $item, EntityManagerInterface $em): Response
    {
        if ($redirect = $this->checkInvestisseur($request)) return $redirect;

        if ($this->isCsrfTokenValid('delete'.$item->getId(), $request->request->get('_token'))) {
            $em->remove($item);
            $em->flush();
        }

        return $this->redirectToRoute('app_management_index');
    }
}