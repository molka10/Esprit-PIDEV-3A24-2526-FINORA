<?php

namespace App\Controller;

use App\Entity\Investment;
use App\Form\InvestmentType;
use App\Repository\InvestmentRepository;
use App\Service\InvestmentImageUploader;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use Dompdf\Dompdf;
use Dompdf\Options;
use Knp\Component\Pager\PaginatorInterface;

#[Route('/investment')]
class InvestmentController extends AbstractController
{
    public function __construct(
        private readonly InvestmentImageUploader $imageUploader,
        private readonly \App\Repository\InvestmentManagementRepository $managementRepository,
        private readonly \App\Service\RecommendationsBuilder $recommendationsBuilder
    ) {}

    /**
     * 🏠 Investment Module Home Page (Landing Page with APIs)
     */
    #[Route('/home', name: 'app_investment_home', methods: ['GET'])]
    public function home(): Response
    {
        return $this->render('investment/home.html.twig', [
            'internal_recs' => $this->recommendationsBuilder->getInternalRecommendations(6),
            'external_recs' => $this->recommendationsBuilder->getExternalRecommendations(6),
            'managements' => $this->managementRepository->findAll(),
        ]);
    }

    #[Route('/wishlist', name: 'app_investment_wishlist_list', methods: ['GET'])]
    #[\Symfony\Component\Security\Http\Attribute\IsGranted('ROLE_USER')]
    public function wishlist(\App\Repository\InvestmentWishlistRepository $repository): Response
    {
        $wishlists = $repository->findBy(
            ['user' => $this->getUser()],
            ['createdAt' => 'DESC']
        );

        return $this->render('investment/wishlist.html.twig', [
            'wishlists' => $wishlists
        ]);
    }

    /**
     * 🤖 AI Chat Assistant
     */
    #[Route('/ai/chat', name: 'app_investment_ai_chat', methods: ['POST'])]
    public function aiChat(Request $request, \App\Service\AiAssistantService $aiService): \Symfony\Component\HttpFoundation\JsonResponse
    {
        // Increase time limit for slow AI APIs (Groq/Gemini)
        set_time_limit(120);
        
        $data = json_decode($request->getContent(), true);
        $message = $data['message'] ?? '';
        $language = $data['language'] ?? 'fr';

        if (empty(trim($message))) {
            return new \Symfony\Component\HttpFoundation\JsonResponse(['error' => 'Message vide'], 400);
        }

        $response = $aiService->processUserMessage($message, $language);
        return new \Symfony\Component\HttpFoundation\JsonResponse($response);
    }

    /**
     * 🔍 View details of an External Project (Partner API)
     */
    #[Route('/external/{id}', name: 'app_investment_external_show', methods: ['GET'])]
    public function externalShow(int $id): Response
    {
        $project = $this->recommendationsBuilder->findExternalById($id);
        if (!$project) {
            throw $this->createNotFoundException('Projet partenaire introuvable.');
        }

        return $this->render('investment/external_show.html.twig', [
            'project' => $project,
        ]);
    }

    /**
     * 📄 Export External Project Factsheet PDF
     */
    #[Route('/external/{id}/factsheet', name: 'app_investment_external_factsheet', methods: ['GET'])]
    public function exportExternalFactsheet(int $id): Response
    {
        $projectData = $this->recommendationsBuilder->findExternalById($id);
        if (!$projectData) {
            throw $this->createNotFoundException('Projet partenaire introuvable.');
        }

        // Create a temporary Investment object for the template compatibility
        $investment = new Investment();
        $investment->setName($projectData['name']);
        $investment->setCategory($projectData['category']);
        $investment->setLocation($projectData['location']);
        $investment->setEstimatedValue((float)$projectData['estimated_value']);
        $investment->setRiskLevel($projectData['risk_level']);
        $investment->setDescription("Opportunité partenaire certifiée. Origine: " . $projectData['published_by']);
        $investment->setStatus('ACTIVE');

        $stats = [
            'fundingGoal' => $projectData['estimated_value'],
            'fundingCurrent' => 0,
            'fundingPercentage' => 0,
            'totalInvested' => $projectData['estimated_value'],
        ];

        // Generate QR code pointing back to the real site detail page
        $appUrl = $_ENV['APP_URL'] ?? null;
        if ($appUrl && !str_contains($appUrl, 'localhost')) {
            $publicUrl = rtrim($appUrl, '/') . $this->generateUrl('app_investment_external_show', ['id' => $id]);
        } else {
            $publicUrl = $this->generateUrl('app_investment_external_show', ['id' => $id], \Symfony\Component\Routing\Generator\UrlGeneratorInterface::ABSOLUTE_URL);
        }
        
        $qrCodeUrl = sprintf('https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=%s&format=png', urlencode($publicUrl));

        // Get Base64 of QR Code (Now working thanks to GD)
        $qrCodeBase64 = null;
        try {
            $qrData = @file_get_contents($qrCodeUrl);
            if ($qrData) {
                $qrCodeBase64 = 'data:image/png;base64,' . base64_encode($qrData);
            }
        } catch (\Exception $e) {}

        $html = $this->renderView('investment_management/portfolio_pdf.html.twig', [
            'items' => [],
            'investment' => $investment,
            'user'  => $this->getUser(),
            'stats' => $stats,
            'qrCodeUrl' => $qrCodeBase64,
            'title' => 'Fiche Partenaire - ' . $investment->getName()
        ]);

        return $this->generatePdfResponse($html, 'Finora_Partner_Factsheet_' . $id . '.pdf');
    }

    /**
     * 🌉 Bridge: Auto-import external project and redirect to invest
     */
    #[Route('/external/{id}/invest', name: 'app_investment_external_invest', methods: ['GET'])]
    public function investExternal(int $id, EntityManagerInterface $em, InvestmentRepository $repo): Response
    {
        $projectData = $this->recommendationsBuilder->findExternalById($id);
        if (!$projectData) {
            throw $this->createNotFoundException('Projet partenaire introuvable.');
        }

        // Check if already imported
        $investment = $repo->findOneBy(['name' => $projectData['name']]);

        if (!$investment) {
            $investment = new Investment();
            $investment->setName($projectData['name']);
            $investment->setCategory($projectData['category']);
            $investment->setLocation($projectData['location']);
            $investment->setEstimatedValue((float)$projectData['estimated_value']);
            $investment->setRiskLevel($projectData['risk_level']);
            $investment->setStatus('ACTIVE'); // External projects are verified
            $investment->setDescription("Opportunité partenaire certifiée par FINORA. Origine: " . $projectData['source']);
            
            $em->persist($investment);
            $em->flush();
        }

        return $this->redirectToRoute('app_management_new', ['inv_id' => $investment->getId()]);
    }

    /**
     * Browsing all investment opportunities (Public to Investors/Entreprises)
     */
    #[Route('', name: 'app_investment_index', methods: ['GET'])]
    public function index(
        Request $request, 
        InvestmentRepository $investmentRepository, 
        PaginatorInterface $paginator,
        \App\Service\SmartLearningService $smartLearningService
    ): Response
    {
        $user = $this->getUser();
        $recommendations = $user ? $smartLearningService->getRecommendations($user) : [];

        $search = $request->query->get('search');
        $category = $request->query->all()['category'] ?? null;
        $risk = $request->query->get('risk');
        $sort = $request->query->get('sortBy');
        $price = $request->query->get('price');

        $queryBuilder = $investmentRepository->searchAndFilterQuery($search, $category, $risk, $sort, $price);

        $investments = $paginator->paginate(
            $queryBuilder,
            $request->query->getInt('page', 1),
            6
        );

        if ($request->isXmlHttpRequest()) {
            return $this->render('investment/_project_list.html.twig', [
                'investments' => $investments,
            ]);
        }

        return $this->render('investment/index.html.twig', [
            'investments' => $investments,
            'recommendations' => $recommendations
        ]);
    }

    /**
     * Create a new investment (Restricted to ROLE_ENTREPRISE or ROLE_ADMIN)
     */
    #[Route('/new', name: 'app_investment_new', methods: ['GET', 'POST'])]
    #[IsGranted('ROLE_ENTREPRISE')]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $investment = new Investment();
        $form = $this->createForm(InvestmentType::class, $investment);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $imageFile = $form->get('imageFile')->getData();
            if ($imageFile instanceof UploadedFile) {
                $investment->setImageUrl($this->imageUploader->upload($imageFile));
            }

            $investment->setUser($this->getUser());
            $entityManager->persist($investment);
            $entityManager->flush();

            $this->addFlash('success', 'Votre opportunité d\'investissement a été enregistrée.');
            return $this->redirectToRoute('app_investment_index');
        }

        return $this->render('investment/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    /**
     * View investment details (Public to all logged in users)
     */
    #[Route('/{id}', name: 'app_investment_show', methods: ['GET'], requirements: ['id' => '\d+'])]
    public function show(Investment $investment): Response
    {
        return $this->render('investment/show.html.twig', [
            'investment' => $investment,
        ]);
    }

    /**
     * Edit an investment (Only for the owner or Admin)
     */
    #[Route('/{id}/edit', name: 'app_investment_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Investment $investment, EntityManagerInterface $entityManager): Response
    {
        if ($investment->getUser() !== $this->getUser() && !$this->isGranted('ROLE_ADMIN')) {
            throw $this->createAccessDeniedException('Vous n\'êtes pas le propriétaire de cet investissement.');
        }

        $form = $this->createForm(InvestmentType::class, $investment);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $imageFile = $form->get('imageFile')->getData();
            if ($imageFile instanceof UploadedFile) {
                if ($investment->getImageUrl()) {
                    $this->imageUploader->remove($investment->getImageUrl());
                }
                $investment->setImageUrl($this->imageUploader->upload($imageFile));
            }

            $entityManager->flush();

            $this->addFlash('success', 'L\'investissement a été mis à jour.');
            return $this->redirectToRoute('app_investment_index');
        }

            return $this->render('investment/edit.html.twig', [
            'form' => $form->createView(),
            'investment' => $investment,
        ]);
    }

    /**
     * 📄 Export Project Factsheet PDF (Public)
     */
    #[Route('/{id}/factsheet', name: 'app_investment_export_factsheet', methods: ['GET'])]
    public function exportFactsheet(Investment $investment): Response
    {
        if ($investment->getStatus() !== 'ACTIVE' && !$this->isGranted('ROLE_ADMIN')) {
            throw $this->createAccessDeniedException('Cet investissement n\'est pas encore public.');
        }

        $stats = [
            'fundingGoal' => $investment->getFundingGoal(),
            'fundingCurrent' => $investment->getFundingCurrent(),
            'fundingPercentage' => $investment->getFundingPercentage(),
            'totalInvested' => $investment->getEstimatedValue(), // Use total as primary metric
        ];

        $appUrl = $_ENV['APP_URL'] ?? null;
        if ($appUrl && !str_contains($appUrl, 'localhost')) {
            $publicUrl = rtrim($appUrl, '/') . $this->generateUrl('app_investment_show', ['id' => $investment->getId()]);
        } else {
            $publicUrl = $this->generateUrl('app_investment_show', ['id' => $investment->getId()], \Symfony\Component\Routing\Generator\UrlGeneratorInterface::ABSOLUTE_URL);
        }
        
        $qrCodeUrl = sprintf('https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=%s&format=png', urlencode($publicUrl));

        // Get Base64 of QR Code
        $qrCodeBase64 = null;
        try {
            $qrData = @file_get_contents($qrCodeUrl);
            if ($qrData) {
                $qrCodeBase64 = 'data:image/png;base64,' . base64_encode($qrData);
            }
        } catch (\Exception $e) {}

        $html = $this->renderView('investment_management/portfolio_pdf.html.twig', [
            'items' => [], // Empty for factsheet mode
            'investment' => $investment,
            'user'  => $this->getUser(),
            'stats' => $stats,
            'qrCodeUrl' => $qrCodeBase64,
            'title' => 'Fiche Signature - ' . $investment->getName()
        ]);

        return $this->generatePdfResponse($html, 'Finora_Factsheet_' . $investment->getId() . '.pdf');
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

            return new Response($dompdf->output(), 200, [
                'Content-Type' => 'application/pdf',
                'Content-Disposition' => 'attachment; filename="' . $filename . '"',
            ]);
        } catch (\Exception $e) {
            return new Response("Erreur PDF: " . $e->getMessage(), 500);
        }
    }
}
