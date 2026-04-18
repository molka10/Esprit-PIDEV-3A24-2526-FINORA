<?php

namespace App\Controller;

use App\Entity\User;
use App\Form\RegistrationFormType;
use App\Security\EmailVerifier;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Contracts\Translation\TranslatorInterface;
use SymfonyCasts\Bundle\VerifyEmail\Exception\VerifyEmailExceptionInterface;

class RegistrationController extends AbstractController
{
    public function __construct(private EmailVerifier $emailVerifier)
    {
    }

    #[Route('/register', name: 'app_register')]
    public function register(
        Request $request,
        UserPasswordHasherInterface $userPasswordHasher,
        EntityManagerInterface $entityManager
    ): Response {

        $user = new User();

        // 🔥 Default values
        $user->setCreatedAt(new \DateTime());
        $user->setRole('USER');
        $user->setIsVerified(false); // 📧 REQUIRES EMAIL VERIFICATION

        $form = $this->createForm(RegistrationFormType::class, $user);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {

            // 🔐 HASH PASSWORD
            $plainPassword = $form->get('plainPassword')->getData();

            $user->setPassword(
                $userPasswordHasher->hashPassword($user, $plainPassword)
            );

            // 🔒 Security: allow only USER / ENTREPRISE
            if (!in_array($user->getRole(), ['USER', 'ENTREPRISE'])) {
                $user->setRole('USER');
            }

            $entityManager->persist($user);
            $entityManager->flush();

            // 📧 SEND VERIFICATION EMAIL ✅ FIXED
            $this->emailVerifier->sendEmailConfirmation(
                'app_verify_email',
                $user
            );

            // ✅ Message instead of auto login
            $this->addFlash('success', 'Check your email to verify your account.');

            return $this->redirectToRoute('app_login');
        }

        return $this->render('registration/register.html.twig', [
            'registrationForm' => $form,
        ]);
    }

    #[Route('/verify/email', name: 'app_verify_email')]
    public function verifyUserEmail(
        Request $request,
        TranslatorInterface $translator,
        EntityManagerInterface $manager
    ): Response {

        $id = $request->query->get('id');

        if (!$id) {
            return $this->redirectToRoute('app_register');
        }

        $user = $manager->getRepository(User::class)->find($id);

        if (!$user) {
            return $this->redirectToRoute('app_register');
        }

        try {
            $this->emailVerifier->handleEmailConfirmation($request, $user);
        } catch (VerifyEmailExceptionInterface $exception) {

            $this->addFlash(
                'verify_email_error',
                $translator->trans($exception->getReason(), [], 'VerifyEmailBundle')
            );

            return $this->redirectToRoute('app_register');
        }

        $this->addFlash('success', 'Your email has been verified.');

        return $this->redirectToRoute('app_login');
    }
}