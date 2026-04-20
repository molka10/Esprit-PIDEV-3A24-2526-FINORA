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

    #[Route('/chart-statistics', name: 'chart_statistics', methods: ['GET'])]
    public function getChartData(TransactionWalletRepository $repo): JsonResponse
    {
        $userId = 6; 

        // Fetch all user transactions
        $transactions = $repo->createQueryBuilder('t')
            ->where('t.userId = :userId')
            ->setParameter('userId', $userId)
            ->orderBy('t.dateTransaction', 'ASC') // sort chronological for charts
            ->getQuery()
            ->getResult();

        $dailyAggregation = [];
        $totalIncome = 0;
        $totalOutcome = 0;

        foreach ($transactions as $t) {
            $dateString = $t->getDateTransaction()?->format('Y-m-d') ?? 'unknown';
            $type = $t->getType();
            $amount = $t->getMontant();

            if (!isset($dailyAggregation[$dateString])) {
                $dailyAggregation[$dateString] = [
                    'income' => 0,
                    'outcome' => 0,
                    'net' => 0
                ];
            }

            if ($type === 'INCOME' || strtolower($type) === 'revenu') {
                $dailyAggregation[$dateString]['income'] += $amount;
                $dailyAggregation[$dateString]['net'] += $amount;
                $totalIncome += $amount;
            } else {
                $amountAbs = abs($amount);
                $dailyAggregation[$dateString]['outcome'] += $amountAbs;
                $dailyAggregation[$dateString]['net'] -= $amountAbs;
                $totalOutcome += $amountAbs;
            }
        }

        // Format for straightforward use in libraries like Chart.js or ApexCharts
        $chartLabels = array_keys($dailyAggregation);
        $incomeData = array_column($dailyAggregation, 'income');
        $outcomeData = array_column($dailyAggregation, 'outcome');
        $netData = array_column($dailyAggregation, 'net');

        return $this->json([
            'status' => 'success',
            'data' => [
                'overview' => [
                    'totalIncome' => $totalIncome,
                    'totalOutcome' => $totalOutcome,
                    'balance' => $totalIncome - $totalOutcome
                ],
                'timeseries' => [
                    'labels' => $chartLabels, // e.g. ["2026-04-10", "2026-04-11"]
                    'datasets' => [
                        [
                            'label' => 'Income',
                            'data' => $incomeData
                        ],
                        [
                            'label' => 'Outcome',
                            'data' => $outcomeData
                        ],
                        [
                            'label' => 'Net Balance',
                            'data' => $netData
                        ]
                    ]
                ]
            ]
        ]);
    }

    #[Route('/category-statistics', name: 'category_statistics', methods: ['GET'])]
    public function getCategoryStatistics(TransactionWalletRepository $repo): JsonResponse
    {
        $userId = 6; 

        // Fetch all user transactions
        $transactions = $repo->createQueryBuilder('t')
            ->where('t.userId = :userId')
            ->setParameter('userId', $userId)
            ->getQuery()
            ->getResult();

        $categoryAggregation = [];

        foreach ($transactions as $t) {
            // Check if the transaction belongs to a category
            $categoryName = $t->getCategory() ? $t->getCategory()->getNom() : 'Uncategorized';
            $type = $t->getType();
            $amount = $t->getMontant();

            if (!isset($categoryAggregation[$categoryName])) {
                $categoryAggregation[$categoryName] = [
                    'income' => 0,
                    'outcome' => 0
                ];
            }

            if ($type === 'INCOME' || strtolower($type) === 'revenu') {
                $categoryAggregation[$categoryName]['income'] += $amount;
            } else {
                $categoryAggregation[$categoryName]['outcome'] += abs($amount);
            }
        }

        // Prepare chart-ready data structures
        $labels = array_keys($categoryAggregation);
        $incomeData = array_column($categoryAggregation, 'income');
        $outcomeData = array_column($categoryAggregation, 'outcome');

        return $this->json([
            'status' => 'success',
            'data' => [
                'labels' => $labels, // e.g. ["Salary", "Food", "Entertainment"]
                'datasets' => [
                    [
                        'label' => 'Revenus par Catégorie',
                        'data' => $incomeData
                    ],
                    [
                        'label' => 'Dépenses par Catégorie',
                        'data' => $outcomeData
                    ]
                ],
                // Raw grouped mapping for custom frontend tables
                'rawMapping' => $categoryAggregation
            ]
        ]);
    }

    #[Route('/calendar-events', name: 'calendar_events', methods: ['GET'])]
    public function getCalendarEvents(TransactionWalletRepository $repo): JsonResponse
    {
        $userId = 6; 

        // Fetch all user transactions
        $transactions = $repo->createQueryBuilder('t')
            ->where('t.userId = :userId')
            ->setParameter('userId', $userId)
            ->getQuery()
            ->getResult();

        $events = [];

        foreach ($transactions as $t) {
            $type = $t->getType();
            $isIncome = ($type === 'INCOME' || strtolower($type) === 'revenu');
            
            // Format styling based on type (Perfect for FullCalendar)
            $color = $isIncome ? '#22c55e' : '#ef4444'; // Green for Income, Red for Outcome
            $prefix = $isIncome ? '+' : '-';

            $events[] = [
                'id' => $t->getId(),
                'title' => $prefix . abs($t->getMontant()) . ' DT - ' . $t->getNomTransaction(),
                'start' => $t->getDateTransaction()?->format('Y-m-d\TH:i:s'), // ISO8601 formatting required by calendars
                'backgroundColor' => $color,
                'borderColor' => $color,
                'extendedProps' => [
                    'category' => $t->getCategory() ? $t->getCategory()->getNom() : 'Uncategorized',
                    'amount' => abs($t->getMontant()),
                    'type' => $type,
                    'isIncome' => $isIncome
                ]
            ];
        }

        return $this->json([
            'status' => 'success',
            'data' => $events
        ]);
    }

    #[Route('/calendar-events/by-user', name: 'calendar_events_by_user', methods: ['GET'])]
    public function getCalendarEventsByUser(Request $request, TransactionWalletRepository $repo): JsonResponse
    {
        $userId = $request->query->getInt('userId', 6);

        $transactions = $repo->createQueryBuilder('t')
            ->where('t.userId = :userId')
            ->setParameter('userId', $userId)
            ->getQuery()
            ->getResult();

        $events = [];

        foreach ($transactions as $t) {
            $type     = $t->getType();
            $isIncome = ($type === 'INCOME' || strtolower($type) === 'revenu');
            $color    = $isIncome ? '#22c55e' : '#ef4444';
            $prefix   = $isIncome ? '+' : '-';

            $events[] = [
                'id'              => $t->getId(),
                'title'           => $prefix . abs($t->getMontant()) . ' DT - ' . $t->getNomTransaction(),
                'start'           => $t->getDateTransaction()?->format('Y-m-d\TH:i:s'),
                'backgroundColor' => $color,
                'borderColor'     => $color,
                'extendedProps'   => [
                    'category' => $t->getCategory() ? $t->getCategory()->getNom() : 'Uncategorized',
                    'amount'   => abs($t->getMontant()),
                    'type'     => $type,
                    'isIncome' => $isIncome,
                    'userId'   => $userId,
                ]
            ];
        }

        return $this->json([
            'status' => 'success',
            'userId' => $userId,
            'data'   => $events
        ]);
    }
}
