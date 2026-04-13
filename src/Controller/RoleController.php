<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Session\SessionInterface;
use Symfony\Component\Routing\Annotation\Route;

class RoleController extends AbstractController
{
    // ================= CHOOSE ROLE =================
    #[Route('/choose-role', name: 'choose_role')]
    public function chooseRole()
    {
        return $this->render('role/choose.html.twig');
    }

    // ================= SET ROLE =================
    #[Route('/set-role/{role}', name: 'set_role')]
    public function setRole(string $role, SessionInterface $session)
    {
        // 🔐 sécuriser les rôles autorisés
        $allowedRoles = ['admin', 'investisseur', 'user'];

        if (!in_array($role, $allowedRoles)) {
            return $this->redirectToRoute('choose_role');
        }

        // stocker rôle en session
        $session->set('role', $role);

        // ✅ REDIRECTION CORRECTE SOUS FORME DE SIGN-IN
        if ($role === 'admin') {
            return $this->redirectToRoute('admin_dashboard');
        }

        if ($role === 'investisseur') {
            return $this->redirectToRoute('app_management_index');
        }
        
        if ($role === 'user') {
            return $this->redirectToRoute('app_investment_index');
        }

        return $this->redirectToRoute('choose_role');
    }

    // ================= LOGOUT ROLE =================
    #[Route('/logout-role', name: 'logout_role')]
    public function logout(SessionInterface $session)
    {
        $session->remove('role');

        return $this->redirectToRoute('choose_role');
    }
}