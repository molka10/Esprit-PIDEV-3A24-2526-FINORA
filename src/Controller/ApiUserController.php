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
        $user = $this->getUser();
        if (!$user) { return $this->json(['status' => 'error', 'data' => []], 401); }
        $userId = $user->getId();
        $balance = $balanceService->calculateUserBalance($userId);

        $notifications = [];

        if ($balance < 0) {
            $notifications[] = [
                'id'      => uniqid(),
                'type'    => 'DANGER',
                'title'   => 'Avertissement de solde négatif',
                'message' => 'Le solde de votre portefeuille est actuellement négatif (' . number_format($balance, 2) . ' DT). Veuillez vérifier vos transactions pour éviter un découvert.',
                'date'    => (new \DateTime())->format('Y-m-d H:i:s'),
                'isRead'  => false
            ];
        }

        return $this->json([
            'status' => 'success',
            'data'   => $notifications,
            'summary' => ['unreadCount' => count($notifications)]
        ]);
    }

    #[Route('/transactions', name: 'transactions', methods: ['GET'])]
    public function getUserTransactions(Request $request, TransactionWalletRepository $repo): JsonResponse
    {
        $user = $this->getUser();
        if (!$user) { return $this->json(['status' => 'error'], 401); }
        $userId = $user->getId();

        $page      = max(1, $request->query->getInt('page', 1));
        $limit     = max(1, min(100, $request->query->getInt('limit', 10)));
        $sortBy    = $request->query->get('sortBy', 'dateTransaction');
        $sortOrder = strtoupper($request->query->get('sortOrder', 'DESC'));
        $sortOrder = in_array($sortOrder, ['ASC', 'DESC']) ? $sortOrder : 'DESC';
        $type      = $request->query->get('type');
        $search    = $request->query->get('search');

        $qb = $repo->createQueryBuilder('t');
        $qb->where('t.user = :userId')->setParameter('userId', $userId);

        if ($type)   { $qb->andWhere('t.type = :type')->setParameter('type', $type); }
        if ($search) { $qb->andWhere('t.nomTransaction LIKE :search')->setParameter('search', '%' . $search . '%'); }

        $allowedSorts = ['id', 'nomTransaction', 'montant', 'dateTransaction', 'type'];
        if (in_array($sortBy, $allowedSorts)) { $qb->orderBy('t.' . $sortBy, $sortOrder); }

        $countQb = clone $qb;
        $total = $countQb->select('count(t.id)')->getQuery()->getSingleScalarResult();
        $qb->setFirstResult(($page - 1) * $limit)->setMaxResults($limit);

        $data = array_map(function ($t) {
            return [
                'id'              => $t->getId(),
                'transactionName' => $t->getNomTransaction(),
                'amount'          => $t->getMontant(),
                'currency'        => 'DT',
                'categoryName'    => $t->getCategory() ? $t->getCategory()->getNom() : null,
                'transactionType' => $t->getType(),
                'transactionDate' => $t->getDateTransaction()?->format('c'),
                'status'          => 'COMPLETED',
                'description'     => 'User transaction record'
            ];
        }, $qb->getQuery()->getResult());

        return $this->json([
            'status' => 'success',
            'message' => 'User transactions successfully fetched.',
            'meta' => [
                'user' => $userId, 'page' => $page, 'limit' => $limit,
                'totalItems' => $total, 'totalPages' => ceil($total / $limit),
                'filtersApplied' => ['type' => $type, 'search' => $search, 'sortBy' => $sortBy, 'sortOrder' => $sortOrder]
            ],
            'data' => $data
        ]);
    }

    #[Route('/chart-statistics', name: 'chart_statistics', methods: ['GET'])]
    public function getChartData(TransactionWalletRepository $repo): JsonResponse
    {
        $user = $this->getUser();
        if (!$user) { return $this->json(['status' => 'error'], 401); }
        $userId = $user->getId();

        $transactions = $repo->createQueryBuilder('t')
            ->where('t.user = :userId')->setParameter('userId', $userId)
            ->orderBy('t.dateTransaction', 'ASC')
            ->getQuery()->getResult();

        $dailyAggregation = [];
        $totalIncome = 0;
        $totalOutcome = 0;

        foreach ($transactions as $t) {
            $dateString = $t->getDateTransaction()?->format('Y-m-d') ?? 'unknown';
            $type   = $t->getType();
            $amount = $t->getMontant();

            if (!isset($dailyAggregation[$dateString])) {
                $dailyAggregation[$dateString] = ['income' => 0, 'outcome' => 0, 'net' => 0];
            }

            if ($type === 'INCOME' || strtolower($type) === 'revenu') {
                $dailyAggregation[$dateString]['income'] += $amount;
                $dailyAggregation[$dateString]['net']    += $amount;
                $totalIncome += $amount;
            } else {
                $amountAbs = abs($amount);
                $dailyAggregation[$dateString]['outcome'] += $amountAbs;
                $dailyAggregation[$dateString]['net']     -= $amountAbs;
                $totalOutcome += $amountAbs;
            }
        }

        return $this->json([
            'status' => 'success',
            'data' => [
                'overview'   => ['totalIncome' => $totalIncome, 'totalOutcome' => $totalOutcome, 'balance' => $totalIncome - $totalOutcome],
                'timeseries' => [
                    'labels'   => array_keys($dailyAggregation),
                    'datasets' => [
                        ['label' => 'Income',      'data' => array_column($dailyAggregation, 'income')],
                        ['label' => 'Outcome',     'data' => array_column($dailyAggregation, 'outcome')],
                        ['label' => 'Net Balance', 'data' => array_column($dailyAggregation, 'net')],
                    ]
                ]
            ]
        ]);
    }

    #[Route('/category-statistics', name: 'category_statistics', methods: ['GET'])]
    public function getCategoryStatistics(TransactionWalletRepository $repo): JsonResponse
    {
        $user = $this->getUser();
        if (!$user) { return $this->json(['status' => 'error'], 401); }
        $userId = $user->getId();

        $transactions = $repo->createQueryBuilder('t')
            ->where('t.user = :userId')->setParameter('userId', $userId)
            ->getQuery()->getResult();

        $categoryAggregation = [];

        foreach ($transactions as $t) {
            $categoryName = $t->getCategory() ? $t->getCategory()->getNom() : 'Uncategorized';
            $type   = $t->getType();
            $amount = $t->getMontant();

            if (!isset($categoryAggregation[$categoryName])) {
                $categoryAggregation[$categoryName] = ['income' => 0, 'outcome' => 0];
            }

            if ($type === 'INCOME' || strtolower($type) === 'revenu') {
                $categoryAggregation[$categoryName]['income'] += $amount;
            } else {
                $categoryAggregation[$categoryName]['outcome'] += abs($amount);
            }
        }

        return $this->json([
            'status' => 'success',
            'data' => [
                'labels'     => array_keys($categoryAggregation),
                'datasets'   => [
                    ['label' => 'Revenus par Catégorie',  'data' => array_column($categoryAggregation, 'income')],
                    ['label' => 'Dépenses par Catégorie', 'data' => array_column($categoryAggregation, 'outcome')],
                ],
                'rawMapping' => $categoryAggregation
            ]
        ]);
    }

    #[Route('/calendar-events', name: 'calendar_events', methods: ['GET'])]
    public function getCalendarEvents(TransactionWalletRepository $repo): JsonResponse
    {
        $user = $this->getUser();
        if (!$user) { return $this->json(['status' => 'error', 'data' => []], 401); }
        $userId = $user->getId();

        $transactions = $repo->createQueryBuilder('t')
            ->where('t.user = :userId')->setParameter('userId', $userId)
            ->getQuery()->getResult();

        $events = [];

        foreach ($transactions as $t) {
            $type     = $t->getType();
            $isIncome = ($type === 'INCOME' || strtolower($type) === 'revenu');
            $color    = $isIncome ? '#22c55e' : '#ef4444';
            $prefix   = $isIncome ? '+' : '-';

            $events[] = [
                'id'              => $t->getId(),
                'title'           => $prefix . abs($t->getMontant()) . ' DT - ' . $t->getNomTransaction(),
                'start'           => $t->getDateTransaction()?->format('Y-m-d'),
                'backgroundColor' => $color,
                'borderColor'     => $color,
                'extendedProps'   => [
                    'category' => $t->getCategory() ? $t->getCategory()->getNom() : 'Uncategorized',
                    'amount'   => abs($t->getMontant()),
                    'type'     => $type,
                    'isIncome' => $isIncome
                ]
            ];
        }

        return $this->json(['status' => 'success', 'data' => $events]);
    }

    #[Route('/calendar-events/by-user', name: 'calendar_events_by_user', methods: ['GET'])]
    public function getCalendarEventsByUser(Request $request, TransactionWalletRepository $repo): JsonResponse
    {
        $user = $this->getUser();
        if (!$user) { return $this->json(['status' => 'error', 'data' => []], 401); }
        $userId = $request->query->getInt('userId', $user->getId());

        $transactions = $repo->createQueryBuilder('t')
            ->where('t.user = :userId')->setParameter('userId', $userId)
            ->getQuery()->getResult();

        $events = [];

        foreach ($transactions as $t) {
            $type     = $t->getType();
            $isIncome = ($type === 'INCOME' || strtolower($type) === 'revenu');
            $color    = $isIncome ? '#22c55e' : '#ef4444';
            $prefix   = $isIncome ? '+' : '-';

            $events[] = [
                'id'              => $t->getId(),
                'title'           => $prefix . abs($t->getMontant()) . ' DT - ' . $t->getNomTransaction(),
                'start'           => $t->getDateTransaction()?->format('Y-m-d'),
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

        return $this->json(['status' => 'success', 'userId' => $userId, 'data' => $events]);
    }
}
