<?php

namespace App\Controller;

use App\Entity\Categorie;
use App\Form\CategorieType;
use App\Repository\CategorieRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/categorie')]
#[IsGranted('ROLE_ADMIN')]
final class CategorieController extends AbstractController
{
    #[Route(name: 'app_categorie_index', methods: ['GET'])]
    public function index(CategorieRepository $categorieRepository, Request $request): Response
    {
        $role = $request->query->get('role');
        if ($role) {
            $request->getSession()->set('role', $role);
        } else {
            $role = $request->getSession()->get('role', 'visiteur');
            if ($role === 'admin') {
                $role = 'visiteur';
            }
        }
        return $this->render('categorie/index.html.twig', [
            'categories' => $categorieRepository->findAll(),
        ]);
    }

    #[Route('/new', name: 'app_categorie_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $categorie = new Categorie();
        $form = $this->createForm(CategorieType::class, $categorie);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($categorie);
            $entityManager->flush();

            return $this->redirectToRoute('app_categorie_index', ['role' => $request->query->get('role')], Response::HTTP_SEE_OTHER);
        }

        return $this->render('categorie/new.html.twig', [
            'categorie' => $categorie,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_categorie_show', methods: ['GET'])]
public function show(Categorie $categorie): Response
{
    $deleteForm = $this->createFormBuilder()
        ->setAction($this->generateUrl('app_categorie_delete', ['id' => $categorie->getId()]))
        ->setMethod('POST')
        ->getForm();

    return $this->render('categorie/show.html.twig', [
        'categorie' => $categorie,
        'form' => $deleteForm,
    ]);
}

#[Route('/{id}/edit', name: 'app_categorie_edit', methods: ['GET', 'POST'])]
public function edit(Request $request, Categorie $categorie, EntityManagerInterface $entityManager): Response
{
    $form = $this->createForm(CategorieType::class, $categorie);
    $form->handleRequest($request);

    if ($form->isSubmitted() && $form->isValid()) {
        $entityManager->flush();
        return $this->redirectToRoute('app_categorie_index', ['role' => $request->query->get('role')], Response::HTTP_SEE_OTHER);
    }

    $deleteForm = $this->createFormBuilder()
        ->setAction($this->generateUrl('app_categorie_delete', ['id' => $categorie->getId()]))
        ->setMethod('POST')
        ->getForm();

    return $this->render('categorie/edit.html.twig', [
        'categorie' => $categorie,
        'form' => $form,
        'delete_form' => $deleteForm,
    ]);
}

    #[Route('/{id}', name: 'app_categorie_delete', methods: ['POST'])]
public function delete(Request $request, Categorie $categorie, EntityManagerInterface $entityManager): Response
{
    if ($this->isCsrfTokenValid('delete'.$categorie->getId(), $request->getPayload()->getString('_token'))) {
        
        // Détacher les appels d'offre liés avant suppression
        foreach ($categorie->getAppelOffres() as $appelOffre) {
            $appelOffre->setCategorie(null);
            $entityManager->persist($appelOffre);
        }
        
        $entityManager->remove($categorie);
        $entityManager->flush();

        $this->addFlash('success', 'Catégorie supprimée avec succès !');
    }

    return $this->redirectToRoute('app_categorie_index', ['role' => $request->query->get('role')], Response::HTTP_SEE_OTHER);
}
}
