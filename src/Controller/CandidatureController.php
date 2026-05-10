<?php

namespace App\Controller;

use App\Entity\Candidature;
use App\Entity\User;
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
#[IsGranted('ROLE_USER')]
final class CandidatureController extends AbstractController
{
    #[Route(name: 'app_candidature_index', methods: ['GET'])]
    public function index(CandidatureRepository $candidatureRepository, Request $request): Response
    {
        $user = $this->getUser();
        if (!$user instanceof User) {
            return $this->redirectToRoute('app_login');
        }
        
        // Use security service to determine role for repository filtering
        $role = 'user';
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
        $user = $this->getUser();
        if ($user instanceof User) {
            $candidature->setUser($user);
        }

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
            $action = $request->request->get('action', 'submit');
            
            // Handle CV Upload for both Draft and Submit
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
                    // Fail silently or log
                }
            }

            if ($action === 'draft') {
                $candidature->setStatut('draft');
                $entityManager->persist($candidature);
                $entityManager->flush();
                $this->addFlash('info', 'Brouillon enregistré avec succès (Document inclus).');
                return $this->redirectToRoute('app_candidature_index');
            }

            // AI Analysis (Only for final submission)
            $tender = $candidature->getAppelOffre();
            $criteria = $tender->getRequiredCriteria() ?? $tender->getDescription();
            
            $candidateProfile = "MESSAGE DE MOTIVATION:\n" . $candidature->getMessage() . 
                               "\n\nCONTENU DU CV:\n" . $cvText;

            $aiResult = $aiService->analyzeCandidature(
                $tender->getTitre(),
                $criteria,
                $candidateProfile
            );

            $candidature->setAiScore($aiResult['score']);
            
            // Store both analysis and axes in the aiAnalysis field as JSON
            $analysisData = [
                'text' => $aiResult['analysis'],
                'axes' => $aiResult['axes']
            ];
            $candidature->setAiAnalysis(json_encode($analysisData));
            
            $candidature->setStatut('submitted');

            $entityManager->persist($candidature);
            $entityManager->flush();

            // Notification SMS
            $admin = $userRepository->findOneAdmin();
            if ($admin && $admin->getPhone()) {
                $smsService->sendSms($admin->getPhone(), "Nouvelle candidature reçue (" . $aiResult['score'] . "%) pour: " . $tender->getTitre());
            }

            $this->addFlash('success', 'Candidature soumise avec succès ! Score IA : ' . $aiResult['score'] . '%.');
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
        $analysisData = $candidature->getAiAnalysis();
        $axes = [];
        $text = $analysisData;

        // Try to decode if it's JSON
        if ($analysisData && str_starts_with($analysisData, '{')) {
            $decoded = json_decode($analysisData, true);
            if ($decoded) {
                $text = $decoded['text'] ?? '';
                $axes = $decoded['axes'] ?? [];
            }
        }

        return $this->render('candidature/show.html.twig', [
            'candidature' => $candidature,
            'matchingScore' => $candidature->getAiScore(),
            'aiAnalysis' => $text,
            'aiAxes' => $axes
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
            $action = $request->request->get('action', 'submit');
            
            if ($action === 'draft') {
                $candidature->setStatut('draft');
                $this->addFlash('info', 'Modifications enregistrées comme brouillon.');
            } else {
                $candidature->setStatut('submitted');
                $this->addFlash('success', 'Candidature soumise avec succès !');
            }

            $entityManager->flush();
            return $this->redirectToRoute('app_candidature_index', ['role' => $request->query->get('role')], Response::HTTP_SEE_OTHER);
        }

        return $this->render('candidature/edit.html.twig', [
            'candidature' => $candidature,
            'form' => $form,
        ]);
    }

    #[Route('/{id}/accepter', name: 'app_candidature_accepter', methods: ['POST'])]
    #[IsGranted('ROLE_ADMIN')]
    public function accepter(Request $request, Candidature $candidature, EntityManagerInterface $entityManager, SmsService $smsService, \App\Service\NotificationService $notificationService): Response
    {
        if ($this->isCsrfTokenValid('accepter'.$candidature->getId(), $request->getPayload()->getString('_token'))) {
            $candidature->setStatut('accepted');
            $entityManager->flush();

            // Notification SMS à l'étudiant
            $user = $candidature->getUser();
            if ($user && $user->getPhone()) {
                $smsService->sendSms($user->getPhone(), "Félicitations ! Votre candidature pour '" . $candidature->getAppelOffre()->getTitre() . "' a été ACCEPTEE.");
            }

            // Notification Interne
            if ($user) {
                $notificationService->send(
                    $user,
                    'APPEL_OFFRE',
                    '✅ Candidature Acceptée',
                    "Félicitations ! Votre candidature pour l'appel d'offre '" . $candidature->getAppelOffre()->getTitre() . "' a été acceptée par l'administrateur."
                );
            }

            $this->addFlash('success', 'Candidature acceptée avec succès !');
        }

        return $this->redirectToRoute('app_candidature_show', ['id' => $candidature->getId(), 'role' => $request->query->get('role')]);
    }

    #[Route('/{id}/rejeter', name: 'app_candidature_rejeter', methods: ['POST'])]
    #[IsGranted('ROLE_ADMIN')]
    public function rejeter(Request $request, Candidature $candidature, EntityManagerInterface $entityManager, SmsService $smsService, \App\Service\NotificationService $notificationService): Response
    {
        if ($this->isCsrfTokenValid('rejeter'.$candidature->getId(), $request->getPayload()->getString('_token'))) {
            $candidature->setStatut('rejected');
            $entityManager->flush();

            // Notification SMS à l'étudiant
            $user = $candidature->getUser();
            if ($user && $user->getPhone()) {
                $smsService->sendSms($user->getPhone(), "Désolé, votre candidature pour '" . $candidature->getAppelOffre()->getTitre() . "' a été refusée.");
            }

            // Notification Interne
            if ($user) {
                $notificationService->send(
                    $user,
                    'APPEL_OFFRE',
                    '❌ Candidature Refusée',
                    "Nous sommes désolés, mais votre candidature pour l'appel d'offre '" . $candidature->getAppelOffre()->getTitre() . "' n'a pas été retenue."
                );
            }

            $this->addFlash('success', 'Candidature rejetée avec succès !');
        }

        return $this->redirectToRoute('app_candidature_show', ['id' => $candidature->getId(), 'role' => $request->query->get('role')]);
    }

    #[Route('/{id}/withdraw', name: 'app_candidature_withdraw', methods: ['POST'])]
    public function withdraw(Request $request, Candidature $candidature, EntityManagerInterface $entityManager): Response
    {
        if ($candidature->getUser() !== $this->getUser()) {
             throw new AccessDeniedException('Action non autorisée.');
        }
        
        $deadline = $candidature->getAppelOffre()->getDateLimite();
        if ($deadline && $deadline < new \DateTime()) {
            $this->addFlash('error', 'Impossible de retirer votre candidature après la date limite.');
            return $this->redirectToRoute('app_candidature_index');
        }

        if ($this->isCsrfTokenValid('withdraw'.$candidature->getId(), $request->request->get('_token'))) {
            $candidature->setStatut('withdrawn');
            $entityManager->flush();
            $this->addFlash('success', 'Candidature retirée avec succès.');
        }

        return $this->redirectToRoute('app_candidature_index');
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