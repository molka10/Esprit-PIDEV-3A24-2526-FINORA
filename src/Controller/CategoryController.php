<?php


namespace App\Controller;

use App\Entity\Category;
use App\Form\CategoryType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Knp\Component\Pager\PaginatorInterface;


class CategoryController extends AbstractController
{
    

#[Route('/category/add', name: 'category_add')]
    public function add(Request $req, EntityManagerInterface $em): Response
    {
        $category = new Category();
        $form = $this->createForm(CategoryType::class, $category);

        $form->handleRequest($req);

        if ($form->isSubmitted() && $form->isValid()) {

            $category->setUserId(6);

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
    public function list(
        Request $request,
        EntityManagerInterface $em,
        PaginatorInterface $paginator
    ): Response {
        $qb = $em->getRepository(Category::class)->createQueryBuilder('c');

        $search = $request->query->get('search');
        $filterType = $request->query->get('filter_type');
        $sortBy = $request->query->get('sortBy', 'nom');

        if ($search) {
            $qb->andWhere('c.nom LIKE :search')->setParameter('search', '%' . $search . '%');
        }
        if ($filterType) {
            $qb->andWhere('c.type = :type')->setParameter('type', $filterType);
        }
        
        $allowedSorts = ['nom', 'priorite'];
        if (in_array($sortBy, $allowedSorts)) {
            $qb->orderBy('c.' . $sortBy, 'ASC');
        } else {
            $qb->orderBy('c.nom', 'ASC');
        }

        $query = $qb->getQuery();

        $categories = $paginator->paginate(
            $query,
            $request->query->getInt('page', 1),
            5
        );

        return $this->render('category/listC.html.twig', [
            'categories' => $categories
        ]);
    }
}

