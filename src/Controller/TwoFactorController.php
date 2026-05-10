<?php

namespace App\Controller;

use App\Service\TwoFactorService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class TwoFactorController extends AbstractController
{
    #[Route('/2fa/verify', name: 'app_2fa_verify')]
    public function verify(Request $request, TwoFactorService $twoFactorService, EntityManagerInterface $em): Response
    {
        /** @var \App\Entity\User $user */
        $user = $this->getUser();

        if (!$user || !$request->getSession()->get('2fa_required')) {
            return $this->redirectToRoute('app_home');
        }

        if ($request->isMethod('POST')) {
            $otp = $request->request->get('otp');
            
            if ($user->getOtpCode() === $otp && $user->isOtpValid()) {
                $user->setOtpCode(null);
                $user->setOtpExpiresAt(null);
                $em->flush();

                $request->getSession()->remove('2fa_required');
                $this->addFlash('success', 'Authentification réussie.');

                return $this->redirectToRoute('app_home');
            }

            $this->addFlash('danger', 'Code invalide ou expiré.');
        }

        return $this->render('security/2fa_verify.html.twig');
    }

    #[Route('/2fa/resend', name: 'app_2fa_resend')]
    public function resend(Request $request, TwoFactorService $twoFactorService): Response
    {
        /** @var \App\Entity\User $user */
        $user = $this->getUser();

        if (!$user || !$request->getSession()->get('2fa_required')) {
            return $this->redirectToRoute('app_login');
        }

        $otp = $twoFactorService->generateOtp($user);
        $twoFactorService->sendOtpEmail($user, $otp);

        $this->addFlash('success', 'Un nouveau code a été envoyé à votre adresse email.');

        return $this->redirectToRoute('app_2fa_verify');
    }
}
