<?php

namespace App\Controller;

use App\Entity\Candidature;
use App\Form\CandidatureType;
use App\Repository\CandidatureRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use App\Service\SmsService;
use App\Repository\AppelOffreRepository;
use App\Repository\UserRepository;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use Symfony\Component\Security\Core\Exception\AccessDeniedException;

#[Route('/candidature')]
final class CandidatureController extends AbstractController
{
    #[Route(name: 'app_candidature_index', methods: ['GET'])]
    public function index(CandidatureRepository $candidatureRepository, Request $request): Response
    {
        $user = $this->getUser();
        
        // Use security service to determine role for repository filtering
        $role = 'visiteur';
        if ($this->isGranted('ROLE_ADMIN')) {
            $role = 'admin';
        } elseif ($this->isGranted('ROLE_ENTREPRISE')) {
            $role = 'entreprise';
        }

        // Pagination settings
        $limit = 6;
        $page = (int)$request->query->get('page', 1);
        if ($page < 1) $page = 1;
        $offset = ($page - 1) * $limit;

        $totalItems = $candidatureRepository->countByUserRole($user, $role);
        $totalPages = (int)ceil($totalItems / $limit);

        $candidatures = $candidatureRepository->findByUserRole($user, $role, $limit, $offset);

        $renderData = [
            'candidatures' => $candidatures,
            'current_page' => $page,
            'total_pages' => $totalPages,
            'total_items' => $totalItems
        ];

        if ($request->query->get('ajax')) {
            $template = ($request->query->get('role') === 'admin') 
                ? 'candidature/_admin_table.html.twig' 
                : 'candidature/_grid.html.twig';
            return $this->render($template, $renderData);
        }

        return $this->render('candidature/index.html.twig', $renderData);
    }

    #[Route('/new', name: 'app_candidature_new', methods: ['GET', 'POST'])]
    public function new(
        Request $request, 
        EntityManagerInterface $entityManager, 
        SmsService $smsService, 
        UserRepository $userRepository, 
        AppelOffreRepository $appelOffreRepository,
        \App\Service\CvUploader $cvUploader,
        \App\Service\AiService $aiService
    ): Response
    {
        $candidature = new Candidature();
        
        // Pre-fill user
        $candidature->setUser($this->getUser());

        // Pre-fill AppelOffre if ID is provided in query
        $appelOffreId = $request->query->get('appelOffre');
        if ($appelOffreId) {
            $appelOffre = $appelOffreRepository->find($appelOffreId);
            if ($appelOffre) {
                $candidature->setAppelOffre($appelOffre);
            }
        }

        $form = $this->createForm(CandidatureType::class, $candidature);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            // Handle CV Upload & Text Extraction
            $cvFile = $form->get('cvFile')->getData();
            $cvText = "";
            if ($cvFile) {
                $cvFileName = $cvUploader->upload($cvFile);
                $candidature->setCvPath($cvFileName);

                try {
                    $pdfParser = new \Smalot\PdfParser\Parser();
                    $pdf = $pdfParser->parseFile($cvUploader->getTargetDirectory() . '/' . $cvFileName);
                    $cvText = $pdf->getText();
                } catch (\Exception $e) {
                    // Silently fail or log if PDF parsing fails
                }
            }

            // AI Analysis
            $tender = $candidature->getAppelOffre();
            $criteria = $tender->getRequiredCriteria() ?? $tender->getDescription();
            
            // Combine Message + CV Text for a full profile analysis
            $candidateProfile = "MESSAGE DE MOTIVATION:\n" . $candidature->getMessage() . 
                               "\n\nCONTENU DU CV:\n" . $cvText;

            $aiResult = $aiService->analyzeCandidature(
                $tender->getTitre(),
                $criteria,
                $candidateProfile
            );

            $candidature->setAiScore($aiResult['score']);
            $candidature->setAiAnalysis($aiResult['analysis']);

            $entityManager->persist($candidature);
            $entityManager->flush();

            // Notification SMS à l'admin
            $admin = $userRepository->findOneAdmin();
            if ($admin) {
                if ($admin->getPhone()) {
                    $success = $smsService->sendSms($admin->getPhone(), "Nouvelle candidature reçue (" . $aiResult['score'] . "%) pour: " . $tender->getTitre());
                    if (!$success) {
                        $this->addFlash('warning', 'Échec SMS : ' . $smsService->getLastError());
                    }
                } else {
                    $this->addFlash('warning', 'L\'administrateur n\'a pas de numéro de téléphone configuré.');
                }
            } else {
                $this->addFlash('warning', 'Aucun administrateur trouvé pour recevoir la notification SMS.');
            }

            $this->addFlash('success', 'Candidature soumise avec succès ! L\'IA a évalué votre profil à ' . $aiResult['score'] . '%.');
            return $this->redirectToRoute('app_candidature_index', ['role' => $request->query->get('role')], Response::HTTP_SEE_OTHER);
        }

        return $this->render('candidature/new.html.twig', [
            'candidature' => $candidature,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_candidature_show', methods: ['GET'])]
    public function show(Candidature $candidature): Response
    {
        return $this->render('candidature/show.html.twig', [
            'candidature' => $candidature,
            'matchingScore' => $candidature->getAiScore(),
            'aiAnalysis' => $candidature->getAiAnalysis(),
        ]);
    }

    #[Route('/{id}/edit', name: 'app_candidature_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Candidature $candidature, EntityManagerInterface $entityManager): Response
    {
        // Ownership check
        if ($candidature->getUser() !== $this->getUser() && !in_array('ROLE_ADMIN', $this->getUser()->getRoles())) {
            throw new AccessDeniedException('Vous ne pouvez pas modifier cette candidature.');
        }

        $form = $this->createForm(CandidatureType::class, $candidature);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();
            $this->addFlash('success', 'Candidature modifiée avec succès !');
            return $this->redirectToRoute('app_candidature_index', ['role' => $request->query->get('role')], Response::HTTP_SEE_OTHER);
        }

        return $this->render('candidature/edit.html.twig', [
            'candidature' => $candidature,
            'form' => $form,
        ]);
    }

    #[Route('/{id}/accepter', name: 'app_candidature_accepter', methods: ['POST'])]
    #[IsGranted('ROLE_ADMIN')]
    public function accepter(Request $request, Candidature $candidature, EntityManagerInterface $entityManager, SmsService $smsService): Response
    {
        if ($this->isCsrfTokenValid('accepter'.$candidature->getId(), $request->getPayload()->getString('_token'))) {
            $candidature->setStatut('accepted');
            $entityManager->flush();

            // Notification SMS à l'entreprise
            $user = $candidature->getUser();
            if ($user && $user->getPhone()) {
                $smsService->sendSms($user->getPhone(), "Félicitations ! Votre candidature pour '" . $candidature->getAppelOffre()->getTitre() . "' a été ACCEPTEE.");
            }

            $this->addFlash('success', 'Candidature acceptée avec succès !');
        }

        return $this->redirectToRoute('app_candidature_show', ['id' => $candidature->getId(), 'role' => $request->query->get('role')]);
    }

    #[Route('/{id}/rejeter', name: 'app_candidature_rejeter', methods: ['POST'])]
    #[IsGranted('ROLE_ADMIN')]
    public function rejeter(Request $request, Candidature $candidature, EntityManagerInterface $entityManager, SmsService $smsService): Response
    {
        if ($this->isCsrfTokenValid('rejeter'.$candidature->getId(), $request->getPayload()->getString('_token'))) {
            $candidature->setStatut('rejected');
            $entityManager->flush();

            // Notification SMS à l'entreprise
            $user = $candidature->getUser();
            if ($user && $user->getPhone()) {
                $smsService->sendSms($user->getPhone(), "Désolé, votre candidature pour '" . $candidature->getAppelOffre()->getTitre() . "' a été refusée.");
            }

            $this->addFlash('success', 'Candidature rejetée avec succès !');
        }

        return $this->redirectToRoute('app_candidature_show', ['id' => $candidature->getId(), 'role' => $request->query->get('role')]);
    }

    #[Route('/{id}', name: 'app_candidature_delete', methods: ['POST'])]
    public function delete(Request $request, Candidature $candidature, EntityManagerInterface $entityManager): Response
    {
        // Ownership check
        if ($candidature->getUser() !== $this->getUser() && !in_array('ROLE_ADMIN', $this->getUser()->getRoles())) {
            throw new AccessDeniedException('Vous ne pouvez pas supprimer cette candidature.');
        }

        if ($this->isCsrfTokenValid('delete'.$candidature->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($candidature);
            $entityManager->flush();
            $this->addFlash('success', 'Candidature supprimée avec succès !');
        }

        return $this->redirectToRoute('app_candidature_index', ['role' => $request->query->get('role')], Response::HTTP_SEE_OTHER);
    }

    #[Route('/{id}/export', name: 'app_candidature_export', methods: ['GET'])]
    public function export(Candidature $candidature): Response
    {
        return $this->render('candidature/export.html.twig', [
            'candidature' => $candidature,
        ]);
    }
}