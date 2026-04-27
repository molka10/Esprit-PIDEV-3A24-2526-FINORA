<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Authentication\AuthenticationUtils;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Contracts\HttpClient\HttpClientInterface;
use Symfony\Bundle\SecurityBundle\Security;
use Symfony\Component\HttpFoundation\JsonResponse;
use App\Service\FaceIdService;
use App\Repository\UserRepository;
use App\Security\LoginFormAuthenticator;
use Doctrine\ORM\EntityManagerInterface;

class SecurityController extends AbstractController
{
    #[Route('/login', name: 'app_login')]
    public function login(
        Request $request,
        AuthenticationUtils $authenticationUtils,
        HttpClientInterface $client
    ): Response {

        // Symfony login error
        $error = $authenticationUtils->getLastAuthenticationError();
        $lastUsername = $authenticationUtils->getLastUsername();

        $formErrors = [];

        if ($request->isMethod('POST')) {

            $email = $request->request->get('_username'); // ⚠️ FIXED
            $password = $request->request->get('_password'); // ⚠️ FIXED

            // ✅ EMAIL VALIDATION
            if (empty($email)) {
                $formErrors[] = "Email is required";
            } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
                $formErrors[] = "Invalid email format";
            }

            // ✅ PASSWORD VALIDATION
            if (empty($password)) {
                $formErrors[] = "Password is required";
            }


            // ❌ If errors → stop login
            if (!empty($formErrors)) {
                return $this->render('security/login.html.twig', [
                    'last_username' => $lastUsername,
                    'error' => $error,
                    'formErrors' => $formErrors,
                    'recaptcha_site_key' => $_ENV['GOOGLE_RECAPTCHA_SITE_KEY'],
                ]);
            }
        }

        return $this->render('security/login.html.twig', [
            'last_username' => $lastUsername,
            'error' => $error,
            'formErrors' => $formErrors,
            'recaptcha_site_key' => $_ENV['GOOGLE_RECAPTCHA_SITE_KEY'],
        ]);
    }

    #[Route('/logout', name: 'app_logout')]
    public function logout(): void
    {
        throw new \LogicException('Logout handled by Symfony.');
    }

    #[Route('/login/face', name: 'app_login_face', methods: ['POST'])]
    public function loginFace(
        Request $request,
        FaceIdService $faceIdService,
        UserRepository $userRepository,
        Security $security,
        EntityManagerInterface $entityManager
    ): JsonResponse {
        $data = json_decode($request->getContent(), true);
        $base64Image = $data['image'] ?? null;
        $email = $data['email'] ?? null;

        if (!$base64Image || !$email) {
            return new JsonResponse(['success' => false, 'message' => 'Image and Email are required'], 400);
        }

        $user = $userRepository->findOneBy(['email' => $email]);
        if (!$user) {
            return new JsonResponse(['success' => false, 'message' => 'No account found with this email'], 404);
        }

        $biometrics = $user->getUserBiometrics();
        if (!$biometrics || !$biometrics->isActive() || !$biometrics->getFaceEmbedding()) {
            return new JsonResponse(['success' => false, 'message' => 'Face ID is not set up for this account'], 400);
        }

        $currentEmbedding = $faceIdService->getFaceEmbedding($base64Image);

        if ($currentEmbedding === null) {
            return new JsonResponse(['success' => false, 'message' => 'Could not extract features from the image.'], 400);
        }

        $storedVector = json_decode($biometrics->getFaceEmbedding(), true);
        
        if (is_array($storedVector)) {
            $similarity = $faceIdService->calculateSimilarity($currentEmbedding, $storedVector);
            
            // Require 85% similarity threshold to avoid false positives with image backgrounds
            if ($similarity >= 0.85) {
                // 🔥 SESSION SECURITY: Track session ID for Face ID login too
                $user->setCurrentSessionId($request->getSession()->getId());
                $entityManager->flush();
                
                // Log the user in programmatically
                $security->login($user, LoginFormAuthenticator::class, 'main');
                return new JsonResponse(['success' => true, 'message' => 'Face ID Login successful!']);
            }
        }

        return new JsonResponse(['success' => false, 'message' => 'Face not recognized for this account.'], 401);
    }
}