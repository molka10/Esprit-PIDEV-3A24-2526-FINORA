<?php

namespace App\Security;

use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;
use SymfonyCasts\Bundle\VerifyEmail\Exception\VerifyEmailExceptionInterface;
use SymfonyCasts\Bundle\VerifyEmail\VerifyEmailHelperInterface;

class EmailVerifier
{
    public function __construct(
        private VerifyEmailHelperInterface $verifyEmailHelper,
        private MailerInterface $mailer,
        private EntityManagerInterface $entityManager,
    ) {
    }

    public function sendEmailConfirmation(string $verifyEmailRouteName, User $user): void
    {
        
        // 🔗 Generate signed URL
        $signatureComponents = $this->verifyEmailHelper->generateSignature(
            $verifyEmailRouteName,
            (string) $user->getId(),
            (string) $user->getEmail(),
            ['id' => $user->getId()]
        );

        $signedUrl = $signatureComponents->getSignedUrl();

        // 📧 Create REAL email (no Twig issues)
        $email = (new \Symfony\Component\Mime\Email())
    ->from(new \Symfony\Component\Mime\Address('jalloulaziz6@gmail.com', 'Finora'))
    ->to($user->getEmail())
    ->subject('Verify your email - Finora')
            ->html("
                <h1>Welcome to Finora 🎉</h1>
                <p>Click the button below to verify your account:</p>
                <p>
                    <a href='$signedUrl' style='padding:10px 20px; background:#6f42c1; color:white; text-decoration:none;'>
                        Verify Email
                    </a>
                </p>
                <p>This link will expire soon.</p>
            ");

$this->mailer->send($email);

  }

    /**
     * @throws VerifyEmailExceptionInterface
     */
    public function handleEmailConfirmation(Request $request, User $user): void
    {
        $this->verifyEmailHelper->validateEmailConfirmationFromRequest(
            $request,
            (string) $user->getId(),
            (string) $user->getEmail()
        );

        $user->setIsVerified(true);

        $this->entityManager->persist($user);
        $this->entityManager->flush();
    }
}