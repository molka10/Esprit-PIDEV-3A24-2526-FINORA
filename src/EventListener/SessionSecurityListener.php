<?php

namespace App\EventListener;

use Symfony\Component\HttpKernel\Event\RequestEvent;
use Symfony\Bundle\SecurityBundle\Security;
use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;

class SessionSecurityListener
{
    public function __construct(
        private Security $security,
        private UrlGeneratorInterface $urlGenerator
    ) {}

    public function onKernelRequest(RequestEvent $event): void
    {
        if (!$event->isMainRequest()) {
            return;
        }

        $request = $event->getRequest();
        $session = $request->getSession();
        $user = $this->security->getUser();

        if ($user instanceof \App\Entity\User) {
            $storedSessionId = $user->getCurrentSessionId();

            // 1. 🔥 CONCURRENT LOGIN PROTECTION
            // If the session ID in the database is different from the current one, 
            // it means the user logged in from somewhere else.
            if ($storedSessionId && $session->getId() !== $storedSessionId) {
                $session->getFlashBag()->add('danger', 'Déconnexion : Une autre session a été ouverte pour ce compte.');
                $event->setResponse(new RedirectResponse($this->urlGenerator->generate('app_logout')));
                return;
            }

            // 2. 🔥 ROLE ISOLATION
            // Prevent an Admin from being "connected" as a User and vice-versa in the same session.
            $isAdminPath = str_starts_with($request->getPathInfo(), '/admin');
            $hasAdminRole = in_array('ROLE_ADMIN', $user->getRoles());

            if ($isAdminPath && !$hasAdminRole) {
                $event->setResponse(new RedirectResponse($this->urlGenerator->generate('app_dashboard')));
            } elseif (!$isAdminPath && $hasAdminRole && !str_starts_with($request->getPathInfo(), '/logout')) {
                // If an Admin tries to browse the front-end user pages, redirect them back to Admin
                // Unless they are trying to logout.
                if ($request->getPathInfo() !== '/admin/dashboard' && !str_starts_with($request->getPathInfo(), '/_')) {
                     // Uncomment the next line if you want to FORCE admins to stay in admin area
                     // $event->setResponse(new RedirectResponse($this->urlGenerator->generate('app_admin_dashboard')));
                }
            }
        }
    }
}

