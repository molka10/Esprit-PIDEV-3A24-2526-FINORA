<?php

namespace App\Controller;

use App\Entity\Formation;
use App\Entity\Lesson;
use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Contracts\Cache\ItemInterface;

class HomeController extends AbstractController
{
    #[Route('/', name: 'app_home')]
    public function index(EntityManagerInterface $entityManager, \Symfony\Contracts\Cache\CacheInterface $cache): Response
    {
        // Temporarily disabled cache for debugging
        $data = $cache->get('home_stats', function (ItemInterface $item) use ($entityManager) {
            $item->expiresAfter(3600);
            
            $formationCount = $entityManager->getRepository(Formation::class)->count(['is_published' => 1]);
            $lessonCount = $entityManager->getRepository(Lesson::class)->count([]);
            $userCount = $entityManager->getRepository(User::class)->count([]);

            $bestsellers = $entityManager->getRepository(Formation::class)->createQueryBuilder('f')
                ->leftJoin('f.purchasedBy', 'p')
                ->select('f')
                ->where('f.is_published = 1')
                ->groupBy('f.id')
                ->orderBy('COUNT(p.id)', 'DESC')
                ->setMaxResults(3)
                ->getQuery()
                ->getResult();

            return [
                'formationCount' => $formationCount,
                'lessonCount' => $lessonCount,
                'userCount' => $userCount,
                'bestsellers' => $bestsellers,
            ];
        });

        return $this->render('front/index.html.twig', $data);
    }
}