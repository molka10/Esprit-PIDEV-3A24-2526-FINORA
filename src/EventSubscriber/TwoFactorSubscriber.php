<?php

namespace App\EventSubscriber;

use Symfony\Component\EventDispatcher\EventSubscriberInterface;
use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\HttpKernel\Event\RequestEvent;
use Symfony\Component\HttpKernel\KernelEvents;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;

class TwoFactorSubscriber implements EventSubscriberInterface
{
    public function __construct(
        private UrlGeneratorInterface $urlGenerator
    ) {}

    public function onKernelRequest(RequestEvent $event): void
    {
        if (!$event->isMainRequest()) {
            return;
        }

        $request = $event->getRequest();
        $session = $request->getSession();

        if ($session->get('2fa_required')) {
            $route = $request->attributes->get('_route');
            
            // Allow 2FA verification, resend, and logout routes
            $allowedRoutes = ['app_2fa_verify', 'app_2fa_resend', 'app_logout'];
            
            if (!in_array($route, $allowedRoutes)) {
                $event->setResponse(new RedirectResponse($this->urlGenerator->generate('app_2fa_verify')));
            }
        }
    }

    public static function getSubscribedEvents(): array
    {
        return [
            KernelEvents::REQUEST => ['onKernelRequest', 0],
        ];
    }
}
