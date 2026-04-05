<?php

namespace App\Controller;

use App\Entity\Formation;
use App\Entity\Lesson;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

final class HomeController extends AbstractController
{
    #[Route('/home', name: 'app_home')]
    public function index(EntityManagerInterface $entityManager): Response
    {
        $formations = $entityManager->getRepository(Formation::class)->findAll();
        $lessons = $entityManager->getRepository(Lesson::class)->findAll();

        return $this->render('home/index.html.twig', [
            'formations' => $formations,
            'lessons' => $lessons,
        ]);
    }
}