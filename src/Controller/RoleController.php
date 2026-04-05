<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class RoleController extends AbstractController
{
    #[Route('/switch-role', name: 'app_switch_role', methods: ['POST'])]
    public function switchRole(Request $request): Response
    {
        $role = $request->request->get('role', 'user');
        $request->getSession()->set('role', $role);

        return $this->redirect($request->headers->get('referer', '/'));
    }
}