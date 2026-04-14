<?php

namespace App\Security;

use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;
use Symfony\Component\Security\Http\Authenticator\AbstractLoginFormAuthenticator;
use Symfony\Component\Security\Http\Authenticator\Passport\Badge\UserBadge;
use Symfony\Component\Security\Http\Authenticator\Passport\Credentials\PasswordCredentials;
use Symfony\Component\Security\Http\Authenticator\Passport\Passport;
use Symfony\Component\Security\Http\Util\TargetPathTrait;
use Symfony\Component\Security\Http\Authenticator\Passport\Badge\CsrfTokenBadge;
use Symfony\Component\Security\Core\User\UserProviderInterface;


class LoginFormAuthenticator extends AbstractLoginFormAuthenticator
{
    use TargetPathTrait;

    public const LOGIN_ROUTE = 'app_login';

    public function __construct(private UrlGeneratorInterface $urlGenerator,
    private UserProviderInterface $userProvider)
    {
    }

   public function authenticate(Request $request): Passport
{
    $email = $request->request->get('_username', '');
    $password = $request->request->get('_password', '');

    // 🔥 CAPTCHA
    $captcha = $request->request->get('g-recaptcha-response');

    if (!$captcha) {
        throw new \Symfony\Component\Security\Core\Exception\AuthenticationException('Captcha is required.');
    }

    $secret = $_ENV['RECAPTCHA_SECRET'];

    $response = file_get_contents(
        "https://www.google.com/recaptcha/api/siteverify?secret="
        .$secret."&response=".$captcha
    );

    $responseKeys = json_decode($response, true);

    if (!$responseKeys["success"]) {
        throw new \Symfony\Component\Security\Core\Exception\AuthenticationException('Invalid captcha.');
    }

    return new Passport(
        new UserBadge($email, function ($userIdentifier) {
            /** @var \App\Entity\User $user */
            $user = $this->userProvider->loadUserByIdentifier($userIdentifier);

            // 🔥 BLOCK LOGIN IF NOT VERIFIED
            if (!$user->isVerified()) {
                throw new \Symfony\Component\Security\Core\Exception\AuthenticationException('Please verify your email first.');
            }

            return $user;
        }),
        new PasswordCredentials($password),
        [
            new CsrfTokenBadge('authenticate', $request->request->get('_csrf_token')),
        ]
    );
}

    public function onAuthenticationSuccess(Request $request, $token, string $firewallName): RedirectResponse
    {
        $user = $token->getUser();

        if (in_array('ROLE_ADMIN', $user->getRoles())) {
            return new RedirectResponse($this->urlGenerator->generate('app_admin'));
        }

        if (in_array('ROLE_ENTREPRISE', $user->getRoles())) {
            return new RedirectResponse($this->urlGenerator->generate('app_dashboard'));
        }

        return new RedirectResponse($this->urlGenerator->generate('app_profile'));
    }

    protected function getLoginUrl(Request $request): string
    {
        return $this->urlGenerator->generate(self::LOGIN_ROUTE);
    }
}