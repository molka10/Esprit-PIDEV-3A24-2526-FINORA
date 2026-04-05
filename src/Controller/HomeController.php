<?php

namespace App\Controller;

use App\Entity\Formation;
use App\Entity\Lesson;
use Doctrine\ORM\EntityManagerInterface;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
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

    #[Route('/formations', name: 'app_formations')]
    public function formations(
        Request $request,
        EntityManagerInterface $entityManager,
        PaginatorInterface $paginator
    ): Response {
        $titre = trim((string) $request->query->get('titre', ''));
        $categorie = trim((string) $request->query->get('categorie', ''));
        $niveau = trim((string) $request->query->get('niveau', ''));
        $tri = (string) $request->query->get('tri', 'id');
        $ordre = strtolower((string) $request->query->get('ordre', 'desc'));

        $qb = $entityManager->getRepository(Formation::class)->createQueryBuilder('f');

        if ($titre !== '') {
            $qb->andWhere('LOWER(f.titre) LIKE LOWER(:titre)')
                ->setParameter('titre', '%' . $titre . '%');
        }

        if ($categorie !== '') {
            $qb->andWhere('LOWER(f.categorie) LIKE LOWER(:categorie)')
                ->setParameter('categorie', '%' . $categorie . '%');
        }

        if ($niveau !== '') {
            $qb->andWhere('f.niveau = :niveau')
                ->setParameter('niveau', $niveau);
        }

        $allowedSortFields = ['id', 'titre', 'categorie', 'niveau'];
        if (!in_array($tri, $allowedSortFields, true)) {
            $tri = 'id';
        }

        $ordre = $ordre === 'asc' ? 'ASC' : 'DESC';
        $qb->orderBy('f.' . $tri, $ordre);

        $query = $qb->getQuery();

        $formations = $paginator->paginate(
            $query,
            $request->query->getInt('page', 1),
            6
        );

        $categories = [
            'Bourse',
            'Investissement',
            'Trading',
            'Actions',
            'ETF',
            'Obligations',
            'Crypto',
            'Forex',
            'Analyse technique',
            'Analyse fondamentale',
            'Gestion des risques',
            'Portefeuille',
            'Dividendes',
            'Marchés financiers',
            'Psychologie du trader',
            'Économie',
            'Inflation',
            'Taux d’intérêt',
            'Fiscalité',
        ];

        return $this->render('home/formations.html.twig', [
            'formations' => $formations,
            'titre' => $titre,
            'categorie' => $categorie,
            'niveau' => $niveau,
            'tri' => $tri,
            'ordre' => strtolower($ordre),
            'categories' => $categories,
        ]);
    }
}