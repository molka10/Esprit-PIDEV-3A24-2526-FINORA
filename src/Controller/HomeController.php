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

    #[Route('/formations/{id}', name: 'app_formation_show')]
    public function formationShow(int $id, EntityManagerInterface $entityManager, Request $request): Response
    {
        $formation = $entityManager->getRepository(Formation::class)->find($id);

        if (!$formation) {
            throw $this->createNotFoundException('Formation introuvable');
        }

        $session = $request->getSession();
        $purchasedFormations = $session->get('purchased_formations', []);
        $isPurchased = in_array($id, $purchasedFormations, true);

        $lessons = $entityManager->getRepository(Lesson::class)
            ->createQueryBuilder('l')
            ->leftJoin('l.formation', 'f')
            ->addSelect('f')
            ->andWhere('f.id = :formationId')
            ->setParameter('formationId', $id)
            ->orderBy('l.ordre', 'ASC')
            ->getQuery()
            ->getResult();

        return $this->render('home/formation_show.html.twig', [
            'formation' => $formation,
            'lessons' => $lessons,
            'isPurchased' => $isPurchased,
        ]);
    }

    #[Route('/formations/{id}/inscription', name: 'app_formation_inscription')]
    public function formationInscription(int $id, EntityManagerInterface $entityManager, Request $request): Response
    {
        $formation = $entityManager->getRepository(Formation::class)->find($id);

        if (!$formation) {
            throw $this->createNotFoundException('Formation introuvable');
        }

        $session = $request->getSession();
        $purchasedFormations = $session->get('purchased_formations', []);
        
        if (!in_array($id, $purchasedFormations, true)) {
            $purchasedFormations[] = $id;
            $session->set('purchased_formations', $purchasedFormations);
        }

        $this->addFlash('success', 'Achat de la formation "' . $formation->getTitre() . '" validé avec succès ! Les leçons sont désormais débloquées.');

        return $this->redirectToRoute('app_formation_show', ['id' => $id]);
    }

    #[Route('/lessons', name: 'app_lessons')]
    public function lessons(
        Request $request,
        EntityManagerInterface $entityManager,
        PaginatorInterface $paginator
    ): Response {
        $titre = trim((string) $request->query->get('titre', ''));
        $formationId = trim((string) $request->query->get('formation', ''));
        $tri = (string) $request->query->get('tri', 'ordre');
        $ordre = strtolower((string) $request->query->get('ordre', 'asc'));

        $qb = $entityManager->getRepository(Lesson::class)
            ->createQueryBuilder('l')
            ->leftJoin('l.formation', 'f')
            ->addSelect('f');

        if ($titre !== '') {
            $qb->andWhere('LOWER(l.titre) LIKE LOWER(:titre) OR LOWER(l.contenu) LIKE LOWER(:titre)')
                ->setParameter('titre', '%' . $titre . '%');
        }

        if ($formationId !== '') {
            $qb->andWhere('f.id = :formationId')
                ->setParameter('formationId', $formationId);
        }

        $allowedSortFields = [
            'id' => 'l.id',
            'titre' => 'l.titre',
            'ordre' => 'l.ordre',
            'dureeMinutes' => 'l.dureeMinutes',
        ];

        if (!array_key_exists($tri, $allowedSortFields)) {
            $tri = 'ordre';
        }

        $ordre = $ordre === 'desc' ? 'DESC' : 'ASC';
        $qb->orderBy($allowedSortFields[$tri], $ordre);

        $query = $qb->getQuery();

        $lessons = $paginator->paginate(
            $query,
            $request->query->getInt('page', 1),
            6
        );

        $formations = $entityManager->getRepository(Formation::class)->findAll();

        return $this->render('home/lessons.html.twig', [
            'lessons' => $lessons,
            'formations' => $formations,
            'titre' => $titre,
            'formationSelected' => $formationId,
            'tri' => $tri,
            'ordre' => strtolower($ordre),
        ]);
    }
}