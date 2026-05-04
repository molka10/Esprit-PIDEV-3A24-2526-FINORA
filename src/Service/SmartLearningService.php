<?php

namespace App\Service;

use App\Entity\Formation;
use App\Entity\InvestmentManagement;
use App\Entity\TransactionBourse;
use App\Entity\TransactionWallet;
use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Contracts\Cache\CacheInterface;
use Symfony\Contracts\Cache\ItemInterface;

class SmartLearningService
{
    public function __construct(
        private EntityManagerInterface $entityManager,
        private CacheInterface $cache
    ) {}

    /**
     * Analyzes user behavior and suggests relevant formations.
     * Returns an array of recommendations.
     * Optimized: single Formation query + per-user cache (5 min TTL).
     */
    public function getRecommendations(User $user): array
    {
        $userId = $user->getId();

        return $this->cache->get('smart_recs_user_' . $userId, function (ItemInterface $item) use ($user, $userId) {
            $item->expiresAfter(300); // 5 minutes

            $recommendations = [];
            $balance = (float) $user->getBalance();

            // ── 1. Spending Behavior (Last 30 days) ──────────────────────────
            $oneMonthAgo = new \DateTime('-30 days');
            $totalOutcome = (float) $this->entityManager
                ->getRepository(TransactionWallet::class)
                ->createQueryBuilder('t')
                ->select('SUM(ABS(t.montant))')
                ->where('t.user = :uid')
                ->andWhere('t.dateTransaction >= :date')
                ->andWhere('t.status = :status')
                ->andWhere('t.type = :type')
                ->setParameter('uid', $userId)
                ->setParameter('date', $oneMonthAgo)
                ->setParameter('status', 'ACCEPTED')
                ->setParameter('type', 'OUTCOME')
                ->getQuery()
                ->getSingleScalarResult();

            // ── 2. Trading count ──────────────────────────────────────────────
            $tradeCount = (int) $this->entityManager
                ->getRepository(TransactionBourse::class)
                ->createQueryBuilder('t')
                ->select('COUNT(t.id)')
                ->where('t.user = :uid')
                ->setParameter('uid', $userId)
                ->getQuery()
                ->getSingleScalarResult();

            // ── 3. Investment count ───────────────────────────────────────────
            $investCount = (int) $this->entityManager
                ->getRepository(InvestmentManagement::class)
                ->createQueryBuilder('m')
                ->select('COUNT(m.managementId)')
                ->where('m.user = :uid')
                ->setParameter('uid', $userId)
                ->getQuery()
                ->getSingleScalarResult();

            // ── Build raw recommendation list ────────────────────────────────
            $recs = [];

            if ($totalOutcome > 1200) {
                $recs[] = ['title' => '📉 Optimisez votre budget', 'reason' => 'Vos dépenses récentes (' . number_format($totalOutcome, 0) . ' DT) sont élevées. Apprenez à dégager de l\'épargne.', 'keyword' => 'Budget', 'cat' => 'Gestion des risques'];
            }

            if ($tradeCount > 0) {
                if ($balance < 300) {
                    $recs[] = ['title' => '🛡️ Stratégie de Survie', 'reason' => 'Le trading sans gestion des risques est un pari. Sécurisez votre capital restant.', 'keyword' => 'Risque', 'cat' => 'Gestion des risques'];
                }
                if ($tradeCount > 15) {
                    $recs[] = ['title' => '📊 Maîtrisez le Marché', 'reason' => 'Vous êtes très actif. L\'analyse fondamentale vous donnera l\'avantage.', 'keyword' => 'Analyse', 'cat' => 'Analyse fondamentale'];
                }
            }

            if ($investCount === 0 && $balance > 1000) {
                $recs[] = ['title' => '🏢 Devenez Actionnaire', 'reason' => 'Découvrez l\'investissement direct dans des projets réels.', 'keyword' => 'Investissement', 'cat' => 'Investissement'];
            }

            if (in_array('ROLE_ENTREPRISE', $user->getRoles())) {
                $recs[] = ['title' => '🤝 Développez votre réseau', 'reason' => 'Optimisez vos coûts en lançant des appels d\'offres stratégiques.', 'keyword' => 'Partenariat', 'cat' => 'Investissement'];
            }

            if (count($recs) < 2) {
                if ($balance > 2500) {
                    $recs[] = ['title' => '📈 Diversification', 'reason' => 'Construisez un portefeuille solide et équilibré.', 'keyword' => 'Portefeuille', 'cat' => 'Marchés financiers'];
                } elseif ($balance < 150) {
                    $recs[] = ['title' => '🌱 Fondations Financières', 'reason' => 'Apprenez les bases pour faire fructifier vos premiers deniers.', 'keyword' => 'Éducation', 'cat' => 'Marchés financiers'];
                }
            }

            if (empty($recs)) {
                $recs[] = ['title' => '🚀 Cap sur l\'Indépendance', 'reason' => 'Découvrez comment générer des revenus passifs grâce à des stratégies éprouvées.', 'keyword' => 'Revenus', 'cat' => 'Marchés financiers'];
            }

            // ── Single Formation query for ALL recommendations ────────────────
            $keywords  = array_unique(array_column($recs, 'keyword'));
            $cats      = array_unique(array_column($recs, 'cat'));
            $allTerms  = array_unique(array_merge($keywords, $cats));

            // Fetch formations matching any keyword/cat in ONE query
            $qb = $this->entityManager->getRepository(Formation::class)->createQueryBuilder('f');
            $orClauses = [];
            foreach ($allTerms as $i => $term) {
                $orClauses[] = "LOWER(f.titre) LIKE LOWER(:t{$i}) OR LOWER(f.description) LIKE LOWER(:t{$i}) OR LOWER(f.categorie) LIKE LOWER(:t{$i})";
                $qb->setParameter("t{$i}", '%' . $term . '%');
            }
            $qb->where(implode(' OR ', $orClauses));
            $formations = $qb->getQuery()->getResult();

            // Index formations by matched term for O(1) lookup
            $formationIndex = [];
            foreach ($formations as $f) {
                $titleLower = strtolower($f->getTitre() . ' ' . $f->getDescription() . ' ' . $f->getCategorie());
                foreach ($allTerms as $term) {
                    if (str_contains($titleLower, strtolower($term))) {
                        $formationIndex[$term] = $formationIndex[$term] ?? $f;
                    }
                }
            }

            // ── Map formations to recommendations ─────────────────────────────
            $recommendations = [];
            foreach ($recs as $rec) {
                $matched = $formationIndex[$rec['keyword']] ?? $formationIndex[$rec['cat']] ?? null;
                $recommendations[] = [
                    'title'          => $rec['title'],
                    'reason'         => $rec['reason'],
                    'formation'      => $matched,
                    'category_slug'  => $rec['cat'],
                    'icon'           => $this->getIconForCategory($rec['cat']),
                ];
            }

            return $recommendations;
        });
    }

    private function getIconForCategory(string $cat): string
    {
        $cat = strtolower($cat);
        if (str_contains($cat, 'risqu'))  return 'bi-shield-check';
        if (str_contains($cat, 'analys')) return 'bi-graph-up-arrow';
        if (str_contains($cat, 'marché')) return 'bi-bank';
        if (str_contains($cat, 'budget')) return 'bi-piggy-bank';
        return 'bi-book';
    }
}
