<?php

namespace App\Controller;

use App\Service\WalletBalanceService;
use App\Repository\TransactionWalletRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/api/user', name: 'api_user_')]
class ApiUserController extends AbstractController
{
    #[Route('/notifications', name: 'notifications', methods: ['GET'])]
    public function getNotifications(WalletBalanceService $balanceService): JsonResponse
    {
        // Currently hardcoded to userId 6 as per project convention
        $userId = 6; 
        $balance = $balanceService->calculateUserBalance($userId);

        $notifications = [];

        if ($balance < 0) {
            $notifications[] = [
                'id' => uniqid(),
                'type' => 'DANGER',
                'title' => 'Avertissement de solde négatif',
                'message' => 'Le solde de votre portefeuille est actuellement négatif (' . number_format($balance, 2) . ' DT). Veuillez vérifier vos transactions pour éviter un découvert.',
                'date' => (new \DateTime())->format('Y-m-d H:i:s'),
                'isRead' => false
            ];
        }

        return $this->json([
            'status' => 'success',
            'data' => $notifications,
            'summary' => [
                'unreadCount' => count($notifications)
            ]
        ]);
    }

    #[Route('/transactions', name: 'transactions', methods: ['GET'])]
    public function getUserTransactions(Request $request, TransactionWalletRepository $repo): JsonResponse
    {
        $userId = 6; 
        
        $page = max(1, $request->query->getInt('page', 1));
        $limit = max(1, min(100, $request->query->getInt('limit', 10)));
        $sortBy = $request->query->get('sortBy', 'dateTransaction');
        $sortOrder = strtoupper($request->query->get('sortOrder', 'DESC'));
        $sortOrder = in_array($sortOrder, ['ASC', 'DESC']) ? $sortOrder : 'DESC';

        $type = $request->query->get('type');
        $search = $request->query->get('search');

        $qb = $repo->createQueryBuilder('t');
        $qb->where('t.userId = :userId')
           ->setParameter('userId', $userId);

        if ($type) {
            $qb->andWhere('t.type = :type')->setParameter('type', $type);
        }
        if ($search) {
            $qb->andWhere('t.nomTransaction LIKE :search')->setParameter('search', '%' . $search . '%');
        }

        // Apply Sorting
        $allowedSorts = ['id', 'nomTransaction', 'montant', 'dateTransaction', 'type'];
        if (in_array($sortBy, $allowedSorts)) {
            $qb->orderBy('t.' . $sortBy, $sortOrder);
        }

        // Get total count
        $countQb = clone $qb;
        $total = $countQb->select('count(t.id)')->getQuery()->getSingleScalarResult();

        // Pagination
        $qb->setFirstResult(($page - 1) * $limit)->setMaxResults($limit);

        // Map data
        $data = array_map(function ($t) {
            return [
                'id' => $t->getId(),
                'transactionName' => $t->getNomTransaction(),
                'amount' => $t->getMontant(),
                'currency' => 'DT', // Dynamic later
                'categoryName' => $t->getCategory() ? $t->getCategory()->getNom() : null,
                'transactionType' => $t->getType(),
                'transactionDate' => $t->getDateTransaction()?->format('c'), // ISO8601 rich format
                'status' => 'COMPLETED',
                'description' => 'User transaction record'
            ];
        }, $qb->getQuery()->getResult());

        return $this->json([
            'status' => 'success',
            'message' => 'User transactions successfully fetched.',
            'meta' => [
                'user' => $userId,
                'page' => $page,
                'limit' => $limit,
                'totalItems' => $total,
                'totalPages' => ceil($total / $limit),
                'filtersApplied' => [
                    'type' => $type,
                    'search' => $search,
                    'sortBy' => $sortBy,
                    'sortOrder' => $sortOrder
                ]
            ],
            'data' => $data
        ]);
    }


}
