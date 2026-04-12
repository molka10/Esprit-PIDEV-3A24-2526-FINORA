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

    /**
     * ================= INDEX =================
     */
    #[Route(name: 'app_investment_index', methods: ['GET'])]
    public function index(Request $request, InvestmentRepository $investmentRepository): Response
    {
        return $this->render('investment/index.html.twig', [
            'investments' => $investmentRepository->findAll(),
        ]);
    }

    /**
     * ================= CREATE =================
     */
    #[Route('/new', name: 'app_investment_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
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
        Investment $investment,
        InvestmentManagementRepository $managementRepository
    ): Response {
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
        $previousImage = $investment->getImageFilename();

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
    public function show(Investment $investment): Response
    {
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
        if ($this->isCsrfTokenValid('delete'.$investment->getId(), $request->request->get('_token'))) {

            // 🔥 supprimer image aussi
            $this->imageUploader->remove($investment->getImageFilename());

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
        $investment->setImageFilename($newFilename);

        if ($previousFilename && $previousFilename !== $newFilename) {
            $this->imageUploader->remove($previousFilename);
        }
    }
}