<?php

namespace App\Controller;

use App\Entity\Investment;
use App\Form\InvestmentType;
use App\Repository\InvestmentManagementRepository;
use App\Repository\InvestmentRepository;
use App\Service\InvestmentImageUploader;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bridge\Doctrine\Attribute\MapEntity;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Knp\Component\Pager\PaginatorInterface;

#[Route('/investment')]
final class InvestmentController extends AbstractController
{
    public function __construct(
        private readonly InvestmentImageUploader $imageUploader,
        private readonly \App\Service\RecommendationsBuilder $recommendationsBuilder,
    ) {}

    /**
     * ================= EXTERNAL SHOW =================
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
     * ================= EXTERNAL INVEST (BRIDGE) =================
     */
    #[Route('/external/{id}/invest', name: 'app_investment_external_invest', methods: ['GET', 'POST'])]
    public function investExternal(int $id, EntityManagerInterface $em, InvestmentRepository $repo): Response
    {
        if ($redirect = $this->checkAccess($this->container->get('request_stack')->getCurrentRequest())) return $redirect;

        $projectData = $this->recommendationsBuilder->findExternalById($id);
        if (!$projectData) {
            throw $this->createNotFoundException('Projet partenaire introuvable.');
        }

        // Logic: Check if we already imported this external project to the DB
        $investment = $repo->findOneBy(['name' => $projectData['name']]);

        if (!$investment) {
            // "Auto-Import" the project into the database
            $investment = new Investment();
            $investment->setName($projectData['name']);
            $investment->setCategory($projectData['category']);
            $investment->setLocation($projectData['location']);
            $investment->setEstimatedValue($projectData['estimated_value']);
            $investment->setRiskLevel($projectData['risk_level']);
            $investment->setStatus('ACTIVE');
            $investment->setDescription("Projet partenaire importé automatiquement depuis " . $projectData['location']);
            
            $em->persist($investment);
            $em->flush();
        }

        // Redirect to the regular management creation form with this new ID
        return $this->redirectToRoute('app_management_new', ['inv_id' => $investment->getId()]);
    }

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

    private function getUserId(Request $request): ?int
    {
        return $request->getSession()->get('user_id');
    }

    private function checkUser(Request $request)
    {
        if ($this->getRole($request) !== 'user') {
            return $this->redirectToRoute('app_investment_index');
        }
        return null;
    }

    /**
     * ================= INDEX =================
     */
    #[Route(name: 'app_investment_index', methods: ['GET'])]
    public function index(Request $request, InvestmentRepository $investmentRepository, PaginatorInterface $paginator): Response
    {
        if ($redirect = $this->checkAccess($request)) return $redirect;

        $params = $request->query->all();
        $search = $params['search'] ?? null;
        $category = $params['category'] ?? null;
        $risk = $params['risk'] ?? null;
        $sort = $params['sortBy'] ?? null;
        $price = $params['price'] ?? null;

        // Clean up array parameters (e.g. [''] to null)
        if (is_array($category)) {
            $category = array_filter($category);
            $category = empty($category) ? null : array_values($category);
        }

        // All roles see ALL investments (catalog)
        $queryBuilder = $investmentRepository->searchAndFilterQuery($search, $category, $risk, $sort, $price);

        $investments = $paginator->paginate(
            $queryBuilder,
            $request->query->getInt('page', 1),
            6 // Number of items per page
        );

        return $this->render('investment/index.html.twig', [
            'investments' => $investments,
        ]);
    }

    /**
     * ================= CREATE =================
     */
    #[Route('/new', name: 'app_investment_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        if ($redirect = $this->checkAccess($request)) return $redirect;

        // 🔐 Investisseur cannot create investments (only browse & invest)
        if ($this->getRole($request) === 'investisseur') {
            $this->addFlash('danger', 'Les investisseurs ne peuvent pas créer d\'investissements.');
            return $this->redirectToRoute('app_investment_index');
        }

        $investment = new Investment();
        $form = $this->createForm(InvestmentType::class, $investment);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {

            $this->applyUploadedImage(
                $form->get('imageFile')->getData(),
                $investment,
                null
            );

            // 🔐 Stamp the owner
            $userId = $this->getUserId($request);
            if ($userId) {
                $investment->setCreatedByUserId($userId);
            }

            $entityManager->persist($investment);
            $entityManager->flush();

            $this->addFlash('success', 'Investment créé avec succès.');

            return $this->redirectToRoute('app_investment_index');
        }

        return $this->render('investment/new.html.twig', [
            'form' => $form,
        ]);
    }

    /**
     * ================= MANAGEMENT =================
     */
    #[Route('/{id}/management', name: 'app_investment_management', methods: ['GET'])]
    public function management(
        Request $request,
        Investment $investment,
        InvestmentManagementRepository $managementRepository
    ): Response {
        if ($redirect = $this->checkAccess($request)) return $redirect;

        // 🔐 Ownership check
        $role = $this->getRole($request);
        $userId = $this->getUserId($request);
        if ($role !== 'admin' && $investment->getCreatedByUserId() !== $userId) {
            $this->addFlash('danger', 'Accès refusé.');
            return $this->redirectToRoute('app_investment_index');
        }

        return $this->render('investment/management.html.twig', [
            'investment' => $investment,
            'managements' => $managementRepository->findBy(['investment' => $investment]),
        ]);
    }

    /**
     * ================= EDIT =================
     */
    #[Route('/{id}/edit', name: 'app_investment_edit', methods: ['GET', 'POST'])]
    public function edit(
        Request $request,
        Investment $investment,
        EntityManagerInterface $entityManager
    ): Response {
        if ($redirect = $this->checkAccess($request)) return $redirect;

        // 🔐 Ownership check
        $role = $this->getRole($request);
        $userId = $this->getUserId($request);
        if ($role !== 'admin' && $investment->getCreatedByUserId() !== $userId) {
            $this->addFlash('danger', 'Accès refusé.');
            return $this->redirectToRoute('app_investment_index');
        }

        $previousImage = $investment->getImageUrl();

        $form = $this->createForm(InvestmentType::class, $investment);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {

            $this->applyUploadedImage(
                $form->get('imageFile')->getData(),
                $investment,
                $previousImage
            );

            $entityManager->flush();

            $this->addFlash('success', 'Investment mis à jour.');

            return $this->redirectToRoute('app_investment_index');
        }

        return $this->render('investment/edit.html.twig', [
            'form' => $form,
            'investment' => $investment,
        ]);
    }

    /**
     * ================= SHOW =================
     */
    #[Route('/{id}', name: 'app_investment_show', methods: ['GET'])]
    public function show(Request $request, Investment $investment): Response
    {
        if ($redirect = $this->checkAccess($request)) return $redirect;

        // All authenticated users can view investment details (public catalog)
        return $this->render('investment/show.html.twig', [
            'investment' => $investment,
        ]);
    }

    /**
     * ================= DELETE =================
     */
    #[Route('/{id}', name: 'app_investment_delete', methods: ['POST'])]
    public function delete(
        Request $request,
        Investment $investment,
        EntityManagerInterface $entityManager
    ): Response {
        if ($redirect = $this->checkAccess($request)) return $redirect;

        // 🔐 Ownership check
        $role = $this->getRole($request);
        $userId = $this->getUserId($request);
        if ($role !== 'admin' && $investment->getCreatedByUserId() !== $userId) {
            $this->addFlash('danger', 'Accès refusé.');
            return $this->redirectToRoute('app_investment_index');
        }

        if ($this->isCsrfTokenValid('delete'.$investment->getId(), $request->request->get('_token'))) {

            // 🔥 supprimer image aussi
            $this->imageUploader->remove($investment->getImageUrl());

            $entityManager->remove($investment);
            $entityManager->flush();

            $this->addFlash('success', 'Investment supprimé.');
        }

        return $this->redirectToRoute('app_investment_index');
    }

    /**
     * ================= IMAGE HANDLER =================
     */
    private function applyUploadedImage(
        mixed $file,
        Investment $investment,
        ?string $previousFilename
    ): void {
        if (!$file instanceof UploadedFile) {
            return;
        }

        $newFilename = $this->imageUploader->upload($file);
        $investment->setImageUrl($newFilename);

        if ($previousFilename && $previousFilename !== $newFilename) {
            $this->imageUploader->remove($previousFilename);
        }
    }
}