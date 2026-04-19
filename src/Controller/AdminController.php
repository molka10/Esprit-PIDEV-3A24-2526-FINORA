<?php

namespace App\Controller;

use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[IsGranted('ROLE_ADMIN')]
class AdminController extends AbstractController
{
    #[Route('/admin', name: 'app_admin')]
    public function index(EntityManagerInterface $em): Response
    {
        // 📊 Stats
        $verified = $em->getRepository(User::class)->count(['isVerified' => true]);
        $notVerified = $em->getRepository(User::class)->count(['isVerified' => false]);
        $totalUsers = $em->getRepository(User::class)->count([]);
        
        $pendingInvestments = $em->getRepository(\App\Entity\Investment::class)->count(['status' => 'PENDING']);

        return $this->render('admin/dashboard.html.twig', [
            'verified' => $verified,
            'notVerified' => $notVerified,
            'totalUsers' => $totalUsers,
            'pendingInvestments' => $pendingInvestments,
        ]);
    }
}