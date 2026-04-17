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
            5
        );

        // ── Métier: per-category statistics ──────────────────────────
        $ALERT_THRESHOLD = 500; // DT — trigger budget warning for high-outcome categories
        $thirtyDaysAgo   = new \DateTime('-30 days');

        $categoryStats = [];
        $totalBudgetRisk = 0;

        foreach ($categories as $cat) {
            $transactions = $em->getRepository(\App\Entity\TransactionWallet::class)
                ->createQueryBuilder('t')
                ->where('t.category = :cat')
                ->setParameter('cat', $cat)
                ->getQuery()
                ->getResult();

            $income  = 0;
            $outcome = 0;
            $recentCount = 0;

            foreach ($transactions as $t) {
                if ($t->getType() === 'INCOME') {
                    $income += $t->getMontant();
                } else {
                    $outcome += abs($t->getMontant());
                }
                if ($t->getDateTransaction() >= $thirtyDaysAgo) {
                    $recentCount++;
                }
            }

            // Budget alert: OUTCOME category that is HAUTE priority and spent > threshold
            $isAlert = ($cat->getType() === 'OUTCOME')
                    && ($cat->getPriorite() === 'HAUTE')
                    && ($outcome > $ALERT_THRESHOLD);

            if ($isAlert) {
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

        // Global health score: 100 = perfect, decreases for each risky category
        $alertCount   = count(array_filter($categoryStats, fn($s) => $s['isAlert']));
        $healthScore  = max(0, 100 - ($alertCount * 20));
        $healthLabel  = $healthScore >= 80 ? 'Bonne santé' : ($healthScore >= 50 ? 'Attention' : 'Critique');
        $healthColor  = $healthScore >= 80 ? 'success' : ($healthScore >= 50 ? 'warning' : 'danger');

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

