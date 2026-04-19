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
use App\Repository\UserRepository;

#[Route('/candidature')]
final class CandidatureController extends AbstractController
{
    #[Route(name: 'app_candidature_index', methods: ['GET'])]
    public function index(CandidatureRepository $candidatureRepository, Request $request): Response
    {
        $role = $request->getSession()->get('role', 'visiteur');
        $user = $this->getUser();

        // Pagination settings
        $limit = 6;
        $page = (int)$request->query->get('page', 1);
        if ($page < 1) $page = 1;
        $offset = ($page - 1) * $limit;

        $totalItems = $candidatureRepository->countByUserRole($user, $role);
        $totalPages = (int)ceil($totalItems / $limit);

        $candidatures = $candidatureRepository->findByUserRole($user, $role, $limit, $offset);

        return $this->render('candidature/index.html.twig', [
            'candidatures' => $candidatures,
            'current_page' => $page,
            'total_pages' => $totalPages,
            'total_items' => $totalItems
        ]);
    }

    #[Route('/new', name: 'app_candidature_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager, SmsService $smsService, UserRepository $userRepository): Response
    {
        $candidature = new Candidature();
        $form = $this->createForm(CandidatureType::class, $candidature);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($candidature);
            $entityManager->flush();

            // Notification SMS à l'admin
            $admin = $userRepository->findOneAdmin();
            if ($admin) {
                if ($admin->getTelephone()) {
                    $smsService->sendSms($admin->getTelephone(), "Nouvelle candidature reçue pour l'appel d'offre: " . $candidature->getAppelOffre()->getTitre());
                } else {
                    $this->addFlash('warning', "L'admin n'a pas de numéro de téléphone.");
                }
            } else {
                $this->addFlash('warning', "Aucun administrateur trouvé pour la notification SMS.");
            }

            $this->addFlash('success', 'Candidature soumise avec succès !');
            return $this->redirectToRoute('app_candidature_index', ['role' => $request->query->get('role')], Response::HTTP_SEE_OTHER);
        }

        return $this->render('candidature/new.html.twig', [
            'candidature' => $candidature,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_candidature_show', methods: ['GET'])]
    public function show(Candidature $candidature, \App\Service\AiService $aiService): Response
    {
        $matchingScore = $aiService->calculateMatchingScore(
            (string)$candidature->getAppelOffre()->getTitre(),
            (string)$candidature->getAppelOffre()->getDescription(),
            (string)$candidature->getMessage()
        );

        return $this->render('candidature/show.html.twig', [
            'candidature' => $candidature,
            'matchingScore' => $matchingScore,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_candidature_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Candidature $candidature, EntityManagerInterface $entityManager): Response
    {
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
    public function accepter(Request $request, Candidature $candidature, EntityManagerInterface $entityManager, SmsService $smsService): Response
    {
        if ($this->isCsrfTokenValid('accepter'.$candidature->getId(), $request->getPayload()->getString('_token'))) {
            $candidature->setStatut('accepted');
            $entityManager->flush();

            // Notification SMS à l'entreprise
            $user = $candidature->getUser();
            if ($user && $user->getTelephone()) {
                $smsService->sendSms($user->getTelephone(), "Félicitations ! Votre candidature pour '" . $candidature->getAppelOffre()->getTitre() . "' a été ACCEPTEE.");
            }

            $this->addFlash('success', 'Candidature acceptée avec succès !');
        }

        return $this->redirectToRoute('app_candidature_show', ['id' => $candidature->getId(), 'role' => $request->query->get('role')]);
    }

    #[Route('/{id}/rejeter', name: 'app_candidature_rejeter', methods: ['POST'])]
    public function rejeter(Request $request, Candidature $candidature, EntityManagerInterface $entityManager, SmsService $smsService): Response
    {
        if ($this->isCsrfTokenValid('rejeter'.$candidature->getId(), $request->getPayload()->getString('_token'))) {
            $candidature->setStatut('rejected');
            $entityManager->flush();

            // Notification SMS à l'entreprise
            $user = $candidature->getUser();
            if ($user && $user->getTelephone()) {
                $smsService->sendSms($user->getTelephone(), "Désolé, votre candidature pour '" . $candidature->getAppelOffre()->getTitre() . "' a été refusée.");
            }

            $this->addFlash('success', 'Candidature rejetée avec succès !');
        }

        return $this->redirectToRoute('app_candidature_show', ['id' => $candidature->getId(), 'role' => $request->query->get('role')]);
    }

    #[Route('/{id}', name: 'app_candidature_delete', methods: ['POST'])]
    public function delete(Request $request, Candidature $candidature, EntityManagerInterface $entityManager): Response
    {
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