<?php

namespace App\EventListener;

use Symfony\Component\HttpKernel\Event\RequestEvent;
use Symfony\Component\HttpKernel\HttpKernelInterface;

class RoleListener
{
    public function onKernelRequest(RequestEvent $event): void
    {
        if (!$event->isMainRequest()) {
            return;
        }

        $request = $event->getRequest();
        $session = $request->getSession();
        
        // On ne gère que les routes qui ne sont pas des assets ou des routes internes symfony
        $path = $request->getPathInfo();
        if (str_starts_with($path, '/_') || str_starts_with($path, '/assets')) {
            return;
        }

        $role = $request->query->get('role');
        if ($role) {
            $session->set('role', $role);
        } else {
            // Si on est sur une route admin, on force admin (sécurité par l'URL)
            if (str_contains($path, '/admin/')) {
                $session->set('role', 'admin');
            } else {
                // Sinon, si on vient de la session et que c'est admin, mais qu'on est sur une route front
                // On force le mode visiteur pour éviter le "bleeding"
                $currentSessionRole = $session->get('role', 'visiteur');
                if ($currentSessionRole === 'admin') {
                    $session->set('role', 'visiteur');
                }
            }
        }
    }
}
