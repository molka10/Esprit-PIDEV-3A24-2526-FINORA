<?php

namespace App\Service;

use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;

class TwoFactorService
{
    public function __construct(
        private EntityManagerInterface $entityManager,
        private MailerInterface $mailer
    ) {}

    public function generateOtp(User $user): string
    {
        $otp = (string) random_int(100000, 999999);
        $user->setOtpCode($otp);
        $user->setOtpExpiresAt(new \DateTime('+15 minutes'));
        
        $this->entityManager->flush();
        
        return $otp;
    }

    public function sendOtpEmail(User $user, string $otp): void
    {
        $email = (new Email())
            ->from('molkaomrani1412@gmail.com')
            ->to($user->getEmail())
            ->subject('Votre code de sécurité FINORA')
            ->html(sprintf('
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 10px;">
                    <h2 style="color: #7c3aed; text-align: center;">Sécurisation de votre compte</h2>
                    <p>Bonjour %s,</p>
                    <p>Une tentative de connexion à votre compte FINORA nécessite une vérification supplémentaire.</p>
                    <div style="background: #f8fafc; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #1e1b4b;">%s</span>
                    </div>
                    <p style="font-size: 14px; color: #64748b;">Ce code est valable pendant 15 minutes. Si vous n\'êtes pas à l\'origine de cette demande, veuillez ignorer cet email.</p>
                    <hr style="border: 0; border-top: 1px solid #e2e8f0; margin: 20px 0;">
                    <p style="text-align: center; font-size: 12px; color: #94a3b8;">&copy; %s FINORA - Plateforme d\'investissement sécurisée</p>
                </div>
            ', $user->getUsername(), $otp, date('Y')));

        $this->mailer->send($email);
    }
}
