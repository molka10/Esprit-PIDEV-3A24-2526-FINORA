<?php

namespace App\Controller;

use App\Entity\Formation;
use App\Form\FormationType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

final class FormationController extends AbstractController
{
    #[Route('/formation', name: 'formation_index')]
    public function index(Request $request, EntityManagerInterface $entityManager): Response
    {
        $titre = $request->query->get('titre');
        $categorie = $request->query->get('categorie');
        $niveau = $request->query->get('niveau');
        $tri = $request->query->get('tri', 'id');
        $ordre = $request->query->get('ordre', 'asc');

        $qb = $entityManager->getRepository(Formation::class)->createQueryBuilder('f');

        if (!empty($titre)) {
            $qb->andWhere('f.titre LIKE :titre')
                ->setParameter('titre', '%' . $titre . '%');
        }

        if (!empty($categorie)) {
            $qb->andWhere('f.categorie LIKE :categorie')
                ->setParameter('categorie', '%' . $categorie . '%');
        }

        if (!empty($niveau)) {
            $qb->andWhere('f.niveau = :niveau')
                ->setParameter('niveau', $niveau);
        }

        $allowedSortFields = ['id', 'titre', 'categorie', 'niveau', 'created_at'];
        if (!in_array($tri, $allowedSortFields, true)) {
            $tri = 'id';
        }

        $ordre = strtolower($ordre) === 'desc' ? 'DESC' : 'ASC';

        $qb->orderBy('f.' . $tri, $ordre);

        $formations = $qb->getQuery()->getResult();

        return $this->render('formation/index.html.twig', [
            'formations' => $formations,
            'titre' => $titre,
            'categorie' => $categorie,
            'niveau' => $niveau,
            'tri' => $tri,
            'ordre' => strtolower($ordre),
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