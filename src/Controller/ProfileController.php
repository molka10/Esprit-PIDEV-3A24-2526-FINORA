<?php

namespace App\Controller;

use App\Entity\User;
use App\Form\UserType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use App\Entity\UserBiometrics;
use App\Service\FaceIdService;
use App\Service\SessionService;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

final class ProfileController extends AbstractController
{
    #[Route('/profile', name: 'app_profile')]
    public function index(\App\Service\SmartLearningService $smartLearningService): Response
    {
        /** @var User $user */
        $user = $this->getUser();

        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $averageScore = $smartLearningService->getAverageAiScore($user);

        // 🔥 USE SIMPLE ROLE (NO ARRAY CONFUSION)
        if ($user->getRole() === 'ADMIN') {
            return $this->render('admin/profile/index.html.twig', [
                'user' => $user,
                'averageAiScore' => $averageScore,
            ]);
        }

        return $this->render('profile/index.html.twig', [
            'user' => $user,
            'averageAiScore' => $averageScore,
        ]);
    }

    #[Route('/profile/edit', name: 'app_profile_edit')]
    public function edit(
        Request $request,
        EntityManagerInterface $em,
        UserPasswordHasherInterface $passwordHasher
    ): Response {
        /** @var User $user */
        $user = $this->getUser();

        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $oldImage = $user->getImage();

        $form = $this->createForm(UserType::class, $user);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {

            // 🔐 PASSWORD
            $plainPassword = $form->get('password')->getData();

            if (!empty($plainPassword)) {
                $user->setPassword(
                    $passwordHasher->hashPassword($user, $plainPassword)
                );
            }

            // 📸 IMAGE
            $imageFile = $form->get('image')->getData();

            if ($imageFile) {
                $extension = $imageFile->guessExtension();
                if (!$extension && $imageFile->getMimeType() === 'image/svg+xml') {
                    $extension = 'svg';
                }
                $newFilename = uniqid().'.'.($extension ?: 'png');

                $imageFile->move(
                    $this->getParameter('images_directory'),
                    $newFilename
                );

                // delete old image
                if ($oldImage && file_exists($this->getParameter('images_directory').'/'.$oldImage)) {
                    unlink($this->getParameter('images_directory').'/'.$oldImage);
                }

                $user->setImage($newFilename);
            }

            $em->flush();

            return $this->redirectToRoute('app_profile');
        }

        // 🔥 SAME FIX HERE
        if ($user->getRole() === 'ADMIN') {
            return $this->render('admin/profile/edit.html.twig', [
                'form' => $form->createView(),
            ]);
        }

        return $this->render('profile/edit.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/profile/face/enroll', name: 'app_profile_face_enroll', methods: ['POST'])]
    public function enrollFace(
        Request $request,
        FaceIdService $faceIdService,
        EntityManagerInterface $em
    ): JsonResponse {
        /** @var User $user */
        $user = $this->getUser();

        if (!$user) {
            return new JsonResponse(['success' => false, 'message' => 'Not authenticated'], 401);
        }

        $data = json_decode($request->getContent(), true);
        $base64Image = $data['image'] ?? null;

        if (!$base64Image) {
            return new JsonResponse(['success' => false, 'message' => 'No image provided'], 400);
        }

        // Get embedding from Hugging Face
        $embedding = $faceIdService->getFaceEmbedding($base64Image);

        if ($embedding === null) {
            return new JsonResponse(['success' => false, 'message' => 'Failed to process face embedding. Make sure there is a clear face.'], 400);
        }

        // Save into DB
        $userBiometrics = $user->getUserBiometrics();
        if (!$userBiometrics) {
            $userBiometrics = new UserBiometrics();
            $userBiometrics->setUser($user);
            $em->persist($userBiometrics);
        }
        
        $userBiometrics->setFaceEmbedding(json_encode($embedding));
        $em->flush();

        return new JsonResponse(['success' => true, 'message' => 'Face ID configured successfully.']);
    }


    #[Route('/profile/2fa/toggle', name: 'app_profile_2fa_toggle', methods: ['POST'])]
    public function toggle2FA(Request $request, EntityManagerInterface $em): JsonResponse
    {
        /** @var User $user */
        $user = $this->getUser();
        if (!$user) return new JsonResponse(['success' => false], 401);

        $data = json_decode($request->getContent(), true);
        $enabled = (bool)($data['enabled'] ?? false);

        $user->setTwoFaEnabled($enabled);
        $em->flush();

        return new JsonResponse([
            'success' => true, 
            'message' => $enabled ? '2FA activée.' : '2FA désactivée.'
        ]);
    }

    #[Route('/profile/digest/toggle', name: 'app_profile_digest_toggle', methods: ['POST'])]
    public function toggleDigest(Request $request, EntityManagerInterface $em): JsonResponse
    {
        /** @var User $user */
        $user = $this->getUser();
        if (!$user) return new JsonResponse(['success' => false], 401);

        $data = json_decode($request->getContent(), true);
        $enabled = (bool)($data['enabled'] ?? false);

        $user->setEmailDigestEnabled($enabled);
        $em->flush();

        return new JsonResponse([
            'success' => true, 
            'message' => $enabled ? 'Résumé hebdomadaire activé.' : 'Résumé hebdomadaire désactivé.'
        ]);
    }

    #[Route('/profile/session/revoke', name: 'app_profile_session_revoke', methods: ['POST'])]
    public function revokeSession(Request $request, SessionService $sessionService): JsonResponse
    {
        $data = json_decode($request->getContent(), true);
        $sessionId = (int)($data['id'] ?? 0);

        if ($sessionService->revokeSession($sessionId)) {
            return new JsonResponse(['success' => true]);
        }

        return new JsonResponse(['success' => false], 400);
    }
}