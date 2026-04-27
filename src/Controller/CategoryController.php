<?php

namespace App\Controller;

use App\Entity\Category;
use App\Entity\TransactionWallet;
use App\Form\CategoryType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Form\FormInterface;
use Knp\Component\Pager\PaginatorInterface;

class CategoryController extends AbstractController
{
    #[Route('/category/add', name: 'category_add')]
    public function add(Request $req, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $category = new Category();
        $form = $this->createForm(CategoryType::class, $category);
        $form->handleRequest($req);

        if ($form->isSubmitted() && $form->isValid()) {
            $category->setUser($user);
            $em->persist($category);
            $em->flush();

            if ($req->isXmlHttpRequest() || $req->headers->get('Accept') === 'application/json') {
                return new JsonResponse(['status' => 'success', 'message' => 'Catégorie créée !']);
            }

            return $this->redirectToRoute('category_list');
        }

        if ($form->isSubmitted() && !$form->isValid()) {
            if ($req->isXmlHttpRequest() || $req->headers->get('Accept') === 'application/json') {
                return new JsonResponse(['status' => 'error', 'errors' => $this->getFormErrors($form)], 400);
            }
        }

        return $this->render('category/addC.html.twig', [
            'form' => $form->createView()
        ]);
    }

    private function getFormErrors(FormInterface $form): array
    {
        $errors = [];
        foreach ($form->getErrors(true) as $error) {
            $errors[] = $error->getMessage();
        }
        return $errors;
    }

    #[Route('/category/edit/{id}', name: 'category_edit')]
    public function edit(int $id, Request $req, EntityManagerInterface $em): Response
    {
        $category = $em->getRepository(Category::class)->find($id);
        if (!$category) {
            throw $this->createNotFoundException('Catégorie non trouvée');
        }

        // Security Check
        if ($category->getUserId() !== $this->getUser()->getId()) {
            throw $this->createAccessDeniedException("Vous n'êtes pas autorisé à modifier cette catégorie.");
        }

        $form = $this->createForm(CategoryType::class, $category);
        $form->handleRequest($req);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush();
            if ($req->isXmlHttpRequest() || $req->headers->get('Accept') === 'application/json') {
                return new JsonResponse(['status' => 'success', 'message' => 'Catégorie modifiée !']);
            }
            return $this->redirectToRoute('category_list');
        }

        if ($form->isSubmitted() && !$form->isValid()) {
            if ($req->isXmlHttpRequest() || $req->headers->get('Accept') === 'application/json') {
                return new JsonResponse(['status' => 'error', 'errors' => $this->getFormErrors($form)], 400);
            }
        }

        return $this->render('category/editC.html.twig', [
            'form' => $form->createView()
        ]);
    }

    #[Route('/category/delete/{id}', name: 'category_delete')]
    public function delete(int $id, EntityManagerInterface $em): Response
    {
        $category = $em->getRepository(Category::class)->find($id);
        if (!$category) {
            throw $this->createNotFoundException('Catégorie non trouvée');
        }

        // Security Check
        if ($category->getUserId() !== $this->getUser()->getId()) {
            throw $this->createAccessDeniedException("Vous n'êtes pas autorisé à supprimer cette catégorie.");
        }

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
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $qb = $em->getRepository(Category::class)->createQueryBuilder('c')
                 ->andWhere('c.user = :uid')
                 ->setParameter('uid', $user->getId());

        $search     = $request->query->get('search');
        $filterType = $request->query->get('filter_type');
        $sortBy     = $request->query->get('sortBy', 'nom');

        if ($search) {
            $qb->andWhere('c.nom LIKE :search')->setParameter('search', '%' . $search . '%');
        }
        if ($filterType) {
            $qb->andWhere('c.type = :type')->setParameter('type', $filterType);
        }

        $allowedSorts = ['nom', 'priorite'];
        $qb->orderBy('c.' . (in_array($sortBy, $allowedSorts) ? $sortBy : 'nom'), 'ASC');

        $categories = $paginator->paginate(
            $qb->getQuery(),
            $request->query->getInt('page', 1),
            8
        );

        $thirtyDaysAgo = new \DateTime('-30 days');
        $categoryStats = [];
        $totalBudgetRisk = 0;
        $alertCount = 0;
        $ALERT_THRESHOLD = 500;

        foreach ($categories as $cat) {
            $transactions = $em->getRepository(TransactionWallet::class)->findBy(['category' => $cat]);

            $income  = 0;
            $outcome = 0;
            $recentCount = 0;

            foreach ($transactions as $t) {
                $amt = abs($t->getMontant());
                if ($t->getType() === 'INCOME') {
                    $income += $amt;
                } else {
                    $outcome += $amt;
                }
                if ($t->getDateTransaction() >= $thirtyDaysAgo) {
                    $recentCount++;
                }
            }

            $isAlert = ($cat->getType() === 'OUTCOME')
                    && ($cat->getPriorite() === 'HAUTE')
                    && ($outcome > $ALERT_THRESHOLD);

            if ($isAlert) {
                $alertCount++;
                $totalBudgetRisk += $outcome;
            }

            $categoryStats[$cat->getId()] = [
                'count'       => count($transactions),
                'income'      => $income,
                'outcome'     => $outcome,
                'net'         => $income - $outcome,
                'isActive'    => $recentCount > 0,
                'recentCount' => $recentCount,
                'isAlert'     => $isAlert,
            ];
        }

        $healthScore  = max(0, 100 - ($alertCount * 25));
        $healthLabel  = $healthScore >= 75 ? 'Bonne santé' : ($healthScore >= 50 ? 'Attention' : 'Critique');
        $healthColor  = $healthScore >= 75 ? 'success' : ($healthScore >= 50 ? 'warning' : 'danger');

        return $this->render('category/listC.html.twig', [
            'categories'     => $categories,
            'categoryStats'  => $categoryStats,
            'alertCount'     => $alertCount,
            'totalBudgetRisk'=> $totalBudgetRisk,
            'healthScore'    => $healthScore,
            'healthLabel'    => $healthLabel,
            'healthColor'    => $healthColor,
            'alertThreshold' => $ALERT_THRESHOLD,
        ]);
    }
}
