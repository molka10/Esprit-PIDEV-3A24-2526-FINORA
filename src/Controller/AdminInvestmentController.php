<?php

namespace App\Controller;

use App\Entity\Investment;
use App\Entity\InvestmentManagement;
use App\Form\InvestmentManagementType;
use App\Form\InvestmentType;
use App\Repository\InvestmentManagementRepository;
use App\Repository\InvestmentRepository;
use App\Service\InvestmentImageUploader;
use App\Service\PortfolioAnalyticsService;
use Doctrine\ORM\EntityManagerInterface;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/admin/investment')]
#[IsGranted('ROLE_ADMIN')]
class AdminInvestmentController extends AbstractController
{
    public function __construct(
        private readonly InvestmentImageUploader $imageUploader,
        private readonly PortfolioAnalyticsService $analyticsService,
        private readonly \Knp\Component\Pager\PaginatorInterface $paginator
    ) {}

    #[Route('', name: 'admin_investment_index', methods: ['GET'])]
    public function index(Request $request, InvestmentRepository $investmentRepository, InvestmentManagementRepository $mgmtRepo, PaginatorInterface $paginator): Response
    {
        // Admins see everything, so showOnlyActive = false
        $queryBuilder = $investmentRepository->searchAndFilterQuery(
            $request->query->get('search'),
            null,
            null,
            'new',
            null,
            false 
        );

        $investments = $paginator->paginate(
            $queryBuilder,
            $request->query->getInt('page', 1),
            10
        );

        // Re-calculate stats for the dashboard grid
        $allInvestments = $investmentRepository->findAll();
        $allManagements = $mgmtRepo->findAll();
        $stats = $this->analyticsService->getDashboardStats($allInvestments, $allManagements);

        return $this->render('admin/investment/index.html.twig', [
            'investments' => $investments,
            'stats' => $stats,
        ]);
    }

    #[Route('/moderation', name: 'admin_investment_moderation', methods: ['GET'])]
    public function moderation(InvestmentRepository $investmentRepository): Response
    {
        $pendingProjects = $investmentRepository->findBy(['status' => 'PENDING'], ['createdAt' => 'DESC']);

        return $this->render('admin/investment/moderation.html.twig', [
            'investments' => $pendingProjects,
        ]);
    }

    #[Route('/{id}/approve', name: 'admin_investment_approve', methods: ['POST'])]
    public function approve(Request $request, Investment $investment, EntityManagerInterface $entityManager): Response
    {
        if (!$this->isCsrfTokenValid('approve' . $investment->getId(), $request->request->get('_token'))) {
            $this->addFlash('error', 'Token CSRF invalide.');
            return $this->redirectToRoute('admin_investment_moderation');
        }

        $investment->setStatus('ACTIVE');

        // Notification logic
        $notification = new \App\Entity\InvestmentNotification();
        $notification->setUser($investment->getUser());
        $notification->setInvestment($investment);
        $notification->setType('INVESTMENT_ACCEPTED');
        $notification->setTitle('Investissement Accepté');
        $notification->setMessage('Votre investissement "' . $investment->getName() . '" a été accepté par l\'administrateur.');
        $entityManager->persist($notification);

        $entityManager->flush();

        $this->addFlash('success', 'Le projet "' . $investment->getName() . '" a été approuvé et est désormais public.');
        return $this->redirectToRoute('admin_investment_moderation');
    }

    #[Route('/{id}/reject', name: 'admin_investment_reject', methods: ['POST'])]
    public function reject(Request $request, Investment $investment, EntityManagerInterface $entityManager): Response
    {
        if (!$this->isCsrfTokenValid('reject' . $investment->getId(), $request->request->get('_token'))) {
            $this->addFlash('error', 'Token CSRF invalide.');
            return $this->redirectToRoute('admin_investment_moderation');
        }

        $investment->setStatus('REJECTED');

        // Notification logic
        $notification = new \App\Entity\InvestmentNotification();
        $notification->setUser($investment->getUser());
        $notification->setInvestment($investment);
        $notification->setType('INVESTMENT_REJECTED');
        $notification->setTitle('Investissement Rejeté');
        $notification->setMessage('Votre investissement "' . $investment->getName() . '" a été rejeté par l\'administrateur.');
        $entityManager->persist($notification);

        $entityManager->flush();

        $this->addFlash('warning', 'Le projet "' . $investment->getName() . '" a été rejeté.');
        return $this->redirectToRoute('admin_investment_moderation');
    }

    #[Route('/managements', name: 'admin_investment_management_list', methods: ['GET'])]
    public function managementList(Request $request, InvestmentManagementRepository $mgmtRepo): Response
    {
        $qb = $mgmtRepo->createQueryBuilder('m')
            ->leftJoin('m.investment', 'i')
            ->leftJoin('m.user', 'u')
            ->addSelect('i', 'u');

        $pagination = $this->paginator->paginate($qb, $request->query->getInt('page', 1), 10);

        return $this->render('admin/investment/management_list.html.twig', [
            'managements' => $pagination,
        ]);
    }

    #[Route('/new', name: 'admin_investment_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $investment = new Investment();
        $form = $this->createForm(InvestmentType::class, $investment, ['is_admin' => true]);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $imageFile = $form->get('imageFile')->getData();
            if ($imageFile instanceof UploadedFile) {
                $investment->setImageUrl($this->imageUploader->upload($imageFile));
            }
            $investment->setUser($this->getUser());
            $em->persist($investment);
            $em->flush();

            $this->addFlash('success', 'Investissement créé.');
            return $this->redirectToRoute('admin_investment_index');
        }

        return $this->render('admin/investment/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}/edit', name: 'admin_investment_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Investment $investment, EntityManagerInterface $em): Response
    {
        $form = $this->createForm(InvestmentType::class, $investment, ['is_admin' => true]);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $imageFile = $form->get('imageFile')->getData();
            if ($imageFile instanceof UploadedFile) {
                if ($investment->getImageUrl()) {
                    $this->imageUploader->remove($investment->getImageUrl());
                }
                $investment->setImageUrl($this->imageUploader->upload($imageFile));
            }
            $em->flush();
            $this->addFlash('success', 'Investissement mis à jour.');
            return $this->redirectToRoute('admin_investment_index');
        }

        return $this->render('admin/investment/edit.html.twig', [
            'form' => $form->createView(),
            'investment' => $investment,
        ]);
    }

    #[Route('/management/new', name: 'admin_investment_management_new', methods: ['GET', 'POST'])]
    public function newManagement(Request $request, EntityManagerInterface $em): Response
    {
        $mgmt = new InvestmentManagement();
        $form = $this->createForm(InvestmentManagementType::class, $mgmt);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($mgmt);
            $em->flush();
            $this->addFlash('success', 'Lien d\'investissement créé.');
            return $this->redirectToRoute('admin_investment_management_list');
        }

        return $this->render('admin/investment/management_new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}/delete', name: 'admin_investment_delete', methods: ['POST'])]
    public function delete(Request $request, Investment $investment, EntityManagerInterface $em): Response
    {
        if ($this->isCsrfTokenValid('delete'.$investment->getId(), $request->request->get('_token'))) {
            if ($investment->getImageUrl()) {
                $this->imageUploader->remove($investment->getImageUrl());
            }
            $em->remove($investment);
            $em->flush();
        }

        return $this->redirectToRoute('admin_investment_index');
    }
}
