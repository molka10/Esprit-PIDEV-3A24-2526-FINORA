<?php

namespace App\Controller;

use App\Entity\Formation;
use App\Entity\Lesson;
use App\Form\FormationType;
use Doctrine\ORM\EntityManagerInterface;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

final class FormationController extends AbstractController
{
    #[Route('/formation', name: 'formation_index')]
    public function index(Request $request, EntityManagerInterface $entityManager, PaginatorInterface $paginator): Response
    {
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

        $page = $request->query->getInt('page', 1);

        $formations = $paginator->paginate(
            $query,
            $page,
            5
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

        $lessonCounts = [];
        foreach ($formations as $formation) {
            $lessonCounts[$formation->getId()] = $entityManager->getRepository(Lesson::class)
                ->createQueryBuilder('l')
                ->select('COUNT(l.id)')
                ->leftJoin('l.formation', 'f')
                ->andWhere('f.id = :formationId')
                ->setParameter('formationId', $formation->getId())
                ->getQuery()
                ->getSingleScalarResult();
        }

        return $this->render('formation/index.html.twig', [
            'formations' => $formations,
            'titre' => $titre,
            'categorie' => $categorie,
            'niveau' => $niveau,
            'tri' => $tri,
            'ordre' => strtolower($ordre),
            'categories' => $categories,
            'lessonCounts' => $lessonCounts,
        ]);
    }

    #[Route('/formation/new', name: 'formation_new')]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $formation = new Formation();

        $form = $this->createForm(FormationType::class, $formation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($formation);
            $entityManager->flush();

            return $this->redirectToRoute('formation_index');
        }

        return $this->render('formation/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/formation/{id}/edit', name: 'formation_edit')]
    public function edit(int $id, Request $request, EntityManagerInterface $entityManager): Response
    {
        $formation = $entityManager->getRepository(Formation::class)->find($id);

        if (!$formation) {
            throw $this->createNotFoundException('Formation introuvable');
        }

        $form = $this->createForm(FormationType::class, $formation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            return $this->redirectToRoute('formation_index');
        }

        return $this->render('formation/edit.html.twig', [
            'form' => $form->createView(),
            'formation' => $formation,
        ]);
    }

    #[Route('/formation/{id}', name: 'formation_delete', methods: ['POST'])]
    public function delete(int $id, Request $request, EntityManagerInterface $entityManager): Response
    {
        $formation = $entityManager->getRepository(Formation::class)->find($id);

        if (!$formation) {
            throw $this->createNotFoundException('Formation introuvable');
        }

        if ($this->isCsrfTokenValid('delete' . $formation->getId(), $request->request->get('_token'))) {
            $entityManager->remove($formation);
            $entityManager->flush();
        }

        return $this->redirectToRoute('formation_index');
    }
}