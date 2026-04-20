<?php

namespace App\Controller;

use App\Entity\TransactionWallet;
use App\Repository\TransactionWalletRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\HttpFoundation\Response;

#[Route('/api/admin', name: 'api_admin_')]
class ApiAdminController extends AbstractController
{
    // Simple simulated middleware for API key validation (Admin Role Simulation).
    private function checkAccess(Request $request): ?JsonResponse
    {
        $token = $request->headers->get('X-API-KEY');
        // A hardcoded key for this demonstration to show secure endpoints
        if ($token !== 'finora-admin-secret-777') {
            return $this->json(['error' => 'Unauthorized Access. Invalid API Key.'], 403);
        }
        return null;
    }

    #[Route('/transactions', name: 'transactions', methods: ['GET'])]
    public function getTransactions(Request $request, TransactionWalletRepository $repo): JsonResponse
    {
        if ($authError = $this->checkAccess($request)) return $authError;

        $page = max(1, $request->query->getInt('page', 1));
        $limit = max(1, min(100, $request->query->getInt('limit', 10)));
        $sortBy = $request->query->get('sortBy', 'dateTransaction');
        $sortOrder = strtoupper($request->query->get('sortOrder', 'DESC'));
        $sortOrder = in_array($sortOrder, ['ASC', 'DESC']) ? $sortOrder : 'DESC';

        // Advanced Filters
        $type = $request->query->get('type');
        $minAmount = $request->query->get('minAmount');
        $maxAmount = $request->query->get('maxAmount');
        $search = $request->query->get('search');
        $isActive = $request->query->get('isActive');

        $qb = $repo->createQueryBuilder('t');

        if ($type) {
            $qb->andWhere('t.type = :type')->setParameter('type', $type);
        }
        if ($minAmount !== null && $minAmount !== '') {
            $qb->andWhere('t.montant >= :minAmount')->setParameter('minAmount', $minAmount);
        }
        if ($maxAmount !== null && $maxAmount !== '') {
            $qb->andWhere('t.montant <= :maxAmount')->setParameter('maxAmount', $maxAmount);
        }
        if ($search) {
            $qb->andWhere('t.nomTransaction LIKE :search')->setParameter('search', '%' . $search . '%');
        }
        if ($isActive !== null && $isActive !== '') {
            $qb->andWhere('t.isActive = :isActive')->setParameter('isActive', filter_var($isActive, FILTER_VALIDATE_BOOLEAN));
        }

        // Count for pagination metadata
        $countQb = clone $qb;
        $total = $countQb->select('count(t.id)')->getQuery()->getSingleScalarResult();

        // Apply Ordering
        $allowedSorts = ['id', 'nomTransaction', 'montant', 'dateTransaction', 'type', 'isActive'];
        if (in_array($sortBy, $allowedSorts)) {
            $qb->orderBy('t.' . $sortBy, $sortOrder);
        }

        // Limit & offset
        $qb->setFirstResult(($page - 1) * $limit)
           ->setMaxResults($limit);

        $results = $qb->getQuery()->getResult();
        $data = [];
        foreach ($results as $t) {
            $data[] = [
                'id' => $t->getId(),
                'name' => $t->getNomTransaction(),
                'type' => $t->getType(),
                'amount' => $t->getMontant(),
                'date' => $t->getDateTransaction()?->format('Y-m-d H:i:s'),
                'userId' => $t->getUserId(),
                'userName' => 'User ' . $t->getUserId(),
                'isActive' => $t->getIsActive()
            ];
        }

        return $this->json([
            'status' => 'success',
            'data' => $data,
            'meta' => [
                'page' => $page,
                'limit' => $limit,
                'total' => $total,
                'totalPages' => ceil($total / $limit)
            ]
        ]);
    }

    #[Route('/statistics', name: 'statistics', methods: ['GET'])]
    public function getStatistics(Request $request, TransactionWalletRepository $repo): JsonResponse
    {
        if ($authError = $this->checkAccess($request)) return $authError;

        $qb = $repo->createQueryBuilder('t');
        $totalTransactions = $qb->select('count(t.id)')->getQuery()->getSingleScalarResult();

        $incomeSum = $repo->createQueryBuilder('t')
            ->select('SUM(t.montant)')
            ->where('t.type = :type')
            ->setParameter('type', 'INCOME')
            ->getQuery()->getSingleScalarResult() ?? 0;

        $outcomeSum = $repo->createQueryBuilder('t')
            ->select('SUM(t.montant)')
            ->where('t.type = :type')
            ->setParameter('type', 'OUTCOME')
            ->getQuery()->getSingleScalarResult() ?? 0;

        $activeCounts = $repo->createQueryBuilder('t')
            ->select('count(t.id)')
            ->where('t.isActive = true')
            ->getQuery()->getSingleScalarResult();

        return $this->json([
            'status' => 'success',
            'data' => [
                'counts' => [
                    'total' => (int)$totalTransactions,
                    'active' => (int)$activeCounts,
                    'inactive' => (int)($totalTransactions - $activeCounts),
                ],
                'aggregations' => [
                    'totalIncome' => (float)$incomeSum,
                    'totalOutcome' => (float)$outcomeSum,
                    'netBalance' => (float)($incomeSum - $outcomeSum),
                ]
            ]
        ]);
    }

    #[Route('/transactions/{id}/status', name: 'toggle_status', methods: ['PATCH'])]
    public function toggleStatus(int $id, Request $request, TransactionWalletRepository $repo, EntityManagerInterface $em): JsonResponse
    {
        if ($authError = $this->checkAccess($request)) return $authError;

        $transaction = $repo->find($id);
        if (!$transaction) {
            return $this->json(['error' => 'Transaction not found'], 404);
        }

        $data = json_decode($request->getContent(), true);
        if (!isset($data['isActive'])) {
            return $this->json(['error' => 'Validation error: Missing "isActive" field'], 400);
        }

        $transaction->setIsActive((bool)$data['isActive']);
        $em->flush();

        return $this->json([
            'status' => 'success',
            'message' => 'Status updated successfully',
            'data' => [
                'id' => $transaction->getId(),
                'isActive' => $transaction->getIsActive()
            ]
        ]);
    }

    #[Route('/transactions/bulk', name: 'bulk_operations', methods: ['POST'])]
    public function bulkOperations(Request $request, TransactionWalletRepository $repo, EntityManagerInterface $em): JsonResponse
    {
        if ($authError = $this->checkAccess($request)) return $authError;

        $data = json_decode($request->getContent(), true);
        $action = $data['action'] ?? null;
        $ids = $data['ids'] ?? [];

        if (empty($ids) || !is_array($ids)) {
            return $this->json(['error' => 'Validation error: No valid IDs array provided'], 400);
        }
        if (!in_array($action, ['delete', 'activate', 'deactivate'])) {
            return $this->json(['error' => 'Validation error: Invalid action. Use delete, activate, or deactivate.'], 400);
        }

        $transactions = $repo->findBy(['id' => $ids]);
        if (count($transactions) === 0) {
            return $this->json(['error' => 'No matching transactions found for given IDs'], 404);
        }

        $count = 0;
        foreach ($transactions as $t) {
            if ($action === 'delete') {
                $em->remove($t);
            } elseif ($action === 'activate') {
                $t->setIsActive(true);
            } elseif ($action === 'deactivate') {
                $t->setIsActive(false);
            }
            $count++;
        }
        $em->flush();

        return $this->json([
            'status' => 'success',
            'message' => 'Bulk operation executed successfully',
            'meta' => [
                'action' => $action,
                'affectedRows' => $count
            ]
        ]);
    }

    // A Twig Dashboard documenting and interacting with the APIs
    #[Route('/docs', name: 'docs', methods: ['GET'])]
    public function docs(): Response
    {
        return $this->render('admin/api_docs.html.twig');
    }
}
