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

#[Route('/investment')]
final class InvestmentController extends AbstractController
{
    public function __construct(
        private readonly InvestmentImageUploader $imageUploader,
    ) {}

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
    public function index(Request $request, InvestmentRepository $investmentRepository): Response
    {
        if ($redirect = $this->checkAccess($request)) return $redirect;

        $search = $request->query->get('search');
        $category = $request->query->get('category');
        $risk = $request->query->get('risk');
        $sort = $request->query->get('sort');

        return $this->render('investment/index.html.twig', [
            'investments' => $investmentRepository->searchAndFilter($search, $category, $risk, $sort),
        ]);
    }

    /**
     * ================= CREATE =================
     */
    #[Route('/new', name: 'app_investment_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        if ($redirect = $this->checkAccess($request)) return $redirect;

        $investment = new Investment();
        $form = $this->createForm(InvestmentType::class, $investment);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {

            $this->applyUploadedImage(
                $form->get('imageFile')->getData(),
                $investment,
                null
            );

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