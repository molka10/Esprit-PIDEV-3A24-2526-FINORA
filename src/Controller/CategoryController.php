<?php

namespace App\Controller;

use App\Entity\Category;
use App\Form\CategoryType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class CategoryController extends AbstractController
{
    

    #[Route('/category/add', name: 'category_add')]
    public function add(Request $req, EntityManagerInterface $em): Response
    {
        $category = new Category();
        $form = $this->createForm(CategoryType::class, $category);

        $form->handleRequest($req);

        if ($form->isSubmitted() && $form->isValid()) {

            $category->setUserId(rand(1, 5));

            $em->persist($category);
            $em->flush();

            return $this->redirectToRoute('category_list');
        }

        return $this->render('category/addC.html.twig', [
            'form' => $form->createView()
        ]);
    }

    #[Route('/category/edit/{id}', name: 'category_edit')]
    public function edit($id, Request $req, EntityManagerInterface $em): Response
    {
        $category = $em->getRepository(Category::class)->find($id);

        $form = $this->createForm(CategoryType::class, $category);
        $form->handleRequest($req);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush();
            return $this->redirectToRoute('category_list');
        }

        return $this->render('category/editC.html.twig', [
            'form' => $form->createView()
        ]);
    }

    #[Route('/category/delete/{id}', name: 'category_delete')]
    public function delete($id, EntityManagerInterface $em): Response
    {
        $category = $em->getRepository(Category::class)->find($id);

        $em->remove($category);
        $em->flush();

        return $this->redirectToRoute('category_list');
    }


    #[Route('/category', name: 'category_list')]
public function list(Request $request, EntityManagerInterface $em)
{
    $category = new Category();
    $form = $this->createForm(CategoryType::class, $category);

    $form->handleRequest($request);

    if ($form->isSubmitted() && $form->isValid()) {

        $category->setUserId(1);

        $em->persist($category);
        $em->flush();

        return $this->redirectToRoute('category_list');
    }

    $categories = $em->getRepository(Category::class)->findAll();

    return $this->render('category/listC.html.twig', [
        'form' => $form->createView(),
        'categories' => $categories
    ]);
}
}