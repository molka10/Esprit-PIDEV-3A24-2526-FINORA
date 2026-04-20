<?php

namespace App\Controller;

use App\Entity\Investment;
use App\Entity\InvestmentManagement;
use App\Form\InvestmentManagementType;
use App\Repository\InvestmentManagementRepository;
use App\Service\AiAssistantService;
use App\Service\PortfolioAnalyticsService;
use Doctrine\ORM\EntityManagerInterface;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use Dompdf\Dompdf;
use Dompdf\Options;

#[Route('/management')]
#[IsGranted('IS_AUTHENTICATED_FULLY')]
class InvestmentManagementController extends AbstractController
{
    /**
     * 📊 Suivi Dashboard (Management Index)
     */
    #[Route('', name: 'app_management_index', methods: ['GET'])]
    public function index(
        Request $request, 
        InvestmentManagementRepository $repo, 
        PaginatorInterface $paginator
    ): Response {
        $user = $this->getUser();
        $search = trim((string)$request->query->get('search', ''));
        $status = trim((string)$request->query->get('status', ''));
        $tri = (string)$request->query->get('tri', 'managementId');
        $ordre = strtolower((string)$request->query->get('ordre', 'desc'));

        $qb = $repo->createQueryBuilder('m')
                   ->leftJoin('m.investment', 'i')
                   ->addSelect('i');

        // Isolation: regular users see only their own participations
        if (!$this->isGranted('ROLE_ADMIN')) {
            $qb->andWhere('m.user = :user')
               ->setParameter('user', $user);
        }

        if ($search !== '') {
            $qb->andWhere('LOWER(m.investmentType) LIKE LOWER(:search) OR LOWER(i.name) LIKE LOWER(:search)')
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

        $ordre = ($ordre === 'asc') ? 'ASC' : 'DESC';
        $qb->orderBy('m.' . $tri, $ordre);

        $query = $qb->getQuery();
        $page = $request->query->getInt('page', 1);

        $managements = $paginator->paginate($query, $page, 10);

        if ($request->isXmlHttpRequest()) {
            // For the /management page, always use the card list partial
            return $this->render('investment_management/_management_list.html.twig', [
                'managements' => $managements,
            ]);
        }

        return $this->render('investment_management/index.html.twig', [
            'managements' => $managements,
            'search' => $search,
            'status' => $status,
            'tri' => $tri,
            'ordre' => strtolower($ordre),
        ]);
    }

    /**
     * 🧠 AI Robo-Advisor Diagnostic
     */
    #[Route('/ai/report', name: 'app_management_ai_report', methods: ['POST'])]
    public function generateAiReport(
        InvestmentManagementRepository $repo, 
        AiAssistantService $aiService,
        PortfolioAnalyticsService $analyticsService
    ): JsonResponse {
        $user = $this->getUser();
        
        // Fetch only user's data for analysis
        $managements = $repo->findBy(['user' => $user]);

        if (count($managements) === 0) {
            return new JsonResponse([
                'html' => "<div class='alert alert-warning border-0'><i class='bi bi-info-circle me-2'></i>Votre portefeuille est vide. L'IA n'a pas encore de données à analyser.</div>"
            ]);
        }

        $stats = [
            'totalInvested' => $analyticsService->calculateTotalValue($managements),
            'riskDistribution' => $analyticsService->getRiskExposure($managements),
            'categoryDistribution' => $analyticsService->getCategoryDistribution($managements)
        ];

        $htmlReport = $aiService->generatePortfolioAnalysis($stats);

        return new JsonResponse([
            'html' => $htmlReport,
            'stats' => $stats
        ]);
    }

    /**
     * ➕ New Participation
     */
    #[Route('/new', name: 'app_management_new', methods: ['GET', 'POST'])]
    public function new(
        Request $request, 
        EntityManagerInterface $em,
        \App\Service\WalletBalanceService $walletBalanceService
    ): Response
    {
        $invId = $request->query->get('inv_id');
        $item = new InvestmentManagement();
        
        if ($invId) {
            $investment = $em->getRepository(Investment::class)->find($invId);
            if ($investment) {
                $item->setInvestment($investment);
                $item->setInvestmentType($investment->getCategory());
            }
        }

        $form = $this->createForm(InvestmentManagementType::class, $item);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            /** @var \App\Entity\User $user */
            $user = $this->getUser();
            $amountInvested = $item->getAmountInvested() ?? 0.0;
            
            if ($amountInvested > 0) {
                $balance = $walletBalanceService->calculateUserBalance($user->getId());
                
                if ($balance < $amountInvested) {
                    $redirectUrl = $this->generateUrl('dashboard');
                    $this->addFlash('danger', sprintf(
                        'Solde insuffisant dans votre portefeuille. (Requis: %.2f DT, Actuel: %.2f DT). <a href="%s" class="btn btn-sm btn-light ms-2 text-dark fw-bold">Recharger mon portefeuille</a>', 
                        $amountInvested, 
                        $balance,
                        $redirectUrl
                    ));
                    
                    return $this->render('investment_management/new.html.twig', [
                        'form' => $form->createView(),
                        'investment' => $item->getInvestment()
                    ]);
                }
                
                // Fetch or Create Category "Investissement Projet"
                $categoryRepo = $em->getRepository(\App\Entity\Category::class);
                $categoryName = "Investissement Projet";
                $category = $categoryRepo->findOneBy(['nom' => $categoryName]);
                if (!$category) {
                    $category = new \App\Entity\Category();
                    $category->setNom($categoryName);
                    $category->setType('OUTCOME');
                    $category->setPriorite('HAUTE');
                    $category->setUserId($user->getId());
                    $em->persist($category);
                    $em->flush();
                }
                
                $walletTx = new \App\Entity\TransactionWallet();
                $walletTx->setMontant(-abs($amountInvested)); // Negative = expense
                $walletTx->setType('OUTCOME');
                $walletTx->setDateTransaction(new \DateTime());
                $walletTx->setCategory($category);
                $walletTx->setUserId($user->getId());
                
                $investmentName = $item->getInvestment() ? $item->getInvestment()->getName() : 'Projet (' . $item->getInvestmentType() . ')';
                $walletTx->setNomTransaction('Investissement: ' . $investmentName);
                
                $em->persist($walletTx);
            }

            $item->setUser($user);
            $item->setCreatedAt(new \DateTime());
            
            $em->persist($item);
            $em->flush();

            $this->addFlash('success', 'Votre participation a été enregistrée avec succès.');
            return $this->redirectToRoute('app_management_index');
        }

        return $this->render('investment_management/new.html.twig', [
            'form' => $form->createView(),
            'investment' => $item->getInvestment()
        ]);
    }

    /**
     * 👁️ Show Participation
     */
    #[Route('/{id}', name: 'app_management_show', requirements: ['id' => '\d+'], methods: ['GET'])]
    public function show(InvestmentManagement $item): Response
    {
        // Security: Ensure ownership
        if (!$this->isGranted('ROLE_ADMIN') && $item->getUser() !== $this->getUser()) {
            throw $this->createAccessDeniedException('Accès refusé.');
        }

        return $this->render('investment_management/show.html.twig', [
            'item' => $item,
        ]);
    }

    /**
     * ✏️ Edit Participation
     */
    #[Route('/{id}/edit', name: 'app_management_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, InvestmentManagement $item, EntityManagerInterface $em): Response
    {
        // Security: Owners or Admin
        if (!$this->isGranted('ROLE_ADMIN') && $item->getUser() !== $this->getUser()) {
            throw $this->createAccessDeniedException('Accès refusé.');
        }

        $form = $this->createForm(InvestmentManagementType::class, $item);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush();
            $this->addFlash('success', 'Mise à jour effectuée.');
            return $this->redirectToRoute('app_management_index');
        }

        return $this->render('investment_management/edit.html.twig', [
            'form' => $form->createView(),
            'item' => $item,
        ]);
    }

    /**
     * 🗑️ Delete Participation
     */
    #[Route('/{id}/delete', name: 'app_management_delete', methods: ['POST'])]
    public function delete(Request $request, InvestmentManagement $item, EntityManagerInterface $em): Response
    {
        if (!$this->isGranted('ROLE_ADMIN') && $item->getUser() !== $this->getUser()) {
            throw $this->createAccessDeniedException('Accès refusé.');
        }

        if ($this->isCsrfTokenValid('delete'.$item->getId(), $request->request->get('_token'))) {
            $em->remove($item);
            $em->flush();
            $this->addFlash('success', 'Participation supprimée.');
        }

        return $this->redirectToRoute('app_management_index');
    }

    /**
     * 📄 Export Full Portfolio PDF
     */
    #[Route('/export/portfolio', name: 'app_management_export_portfolio', methods: ['GET'])]
    public function exportPortfolioPdf(
        InvestmentManagementRepository $repo,
        PortfolioAnalyticsService $analyticsService
    ): Response {
        $user = $this->getUser();
        $managements = $repo->findBy(['user' => $user]);

        $stats = [
            'totalInvested' => $analyticsService->calculateTotalValue($managements),
        ];

        $html = $this->renderView('investment_management/portfolio_pdf.html.twig', [
            'items' => $managements,
            'user'  => $user,
            'stats' => $stats,
            'title' => 'Relevé Global de Portefeuille'
        ]);

        return $this->generatePdfResponse($html, 'Finora_Portfolio_Report.pdf');
    }

    /**
     * 📄 Export Single Participation PDF
     */
    #[Route('/{id}/export', name: 'app_management_export_single', methods: ['GET'])]
    public function exportPdf(InvestmentManagement $item): Response
    {
        if (!$this->isGranted('ROLE_ADMIN') && $item->getUser() !== $this->getUser()) {
            throw $this->createAccessDeniedException('Accès refusé.');
        }

        $stats = ['totalInvested' => $item->getAmountInvested()];

        $html = $this->renderView('investment_management/portfolio_pdf.html.twig', [
            'items' => [$item],
            'user'  => $item->getUser(),
            'stats' => $stats,
            'title' => 'Reçu d\'Investissement #' . $item->getId()
        ]);

        return $this->generatePdfResponse($html, 'Finora_Investment_Receipt_' . $item->getId() . '.pdf');
    }

    /**
     * Internal helper to generate PDF response
     */
    private function generatePdfResponse(string $html, string $filename): Response
    {
        try {
            $options = new Options();
            $options->set('defaultFont', 'Helvetica');
            $options->set('isRemoteEnabled', true);
            $options->set('isHtml5ParserEnabled', true);
            
            $dompdf = new Dompdf($options);
            $dompdf->setPaper('A4', 'portrait');
            $dompdf->set_option('chroot', realpath($this->getParameter('kernel.project_dir')));
            
            $dompdf->loadHtml($html);
            $dompdf->render();

            $output = $dompdf->output();

            return new Response($output, 200, [
                'Content-Type' => 'application/pdf',
                'Content-Disposition' => 'attachment; filename="' . $filename . '"',
            ]);
        } catch (\Exception $e) {
            return new Response("Erreur PDF: " . $e->getMessage() . " dans " . $e->getFile() . " à la ligne " . $e->getLine(), 500);
        }
    }
}
