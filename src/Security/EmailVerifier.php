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
                <div style='font-family: Roboto, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 12px;'>
                    <div style='text-align: center; margin-bottom: 30px;'>
                        <h1 style='color: #7c3aed; margin-bottom: 10px;'>Bienvenue chez Finora 🎉</h1>
                        <p style='color: #666;'>Merci de nous avoir rejoint ! Plus qu'une étape pour activer votre compte.</p>
                    </div>
                    <div style='background: #f8f9fa; padding: 30px; border-radius: 12px; text-align: center;'>
                        <p style='font-size: 16px; color: #333; margin-bottom: 25px;'>Cliquez sur le bouton ci-dessous pour vérifier votre adresse email et accéder à votre dashboard :</p>
                        <a href='$signedUrl' style='display: inline-block; padding: 14px 30px; background: linear-gradient(135deg, #7c3aed 0%, #3b1278 100%); color: white; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px; box-shadow: 0 4px 6px rgba(124, 58, 237, 0.2);'>
                            Vérifier mon compte
                        </a>
                        <p style='margin-top: 25px; color: #999; font-size: 12px;'>Ce lien expirera dans 60 minutes.</p>
                    </div>
                    <div style='text-align: center; margin-top: 30px; color: #aaa; font-size: 11px;'>
                        &copy; 2026 Finora Platform. Tous droits réservés.
                    </div>
                </div>
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