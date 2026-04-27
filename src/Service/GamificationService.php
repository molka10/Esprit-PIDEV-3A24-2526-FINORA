<?php

namespace App\Service;

use App\Entity\User;
use App\Entity\MarginLoan;
use App\Repository\TransactionBourseRepository;
use Doctrine\ORM\EntityManagerInterface;

class GamificationService
{
    private $em;
    private $transactionRepo;

    public function __construct(EntityManagerInterface $em, TransactionBourseRepository $transactionRepo)
    {
        $this->em = $em;
        $this->transactionRepo = $transactionRepo;
    }

    public function getUserBadges(User $user): array
    {
        $badges = [];

        // 1. Badge Loup de Wall Street : Si PNL positif ou grosse activité (> 5 transactions)
        $transactions = $this->transactionRepo->findBy(['user' => $user]);
        $totalAchat = 0;
        $totalVente = 0;
        foreach ($transactions as $t) {
            if ($t->getTypeTransaction() === 'ACHAT') {
                $totalAchat += $t->getMontantTotal();
            } else {
                $totalVente += $t->getMontantTotal();
            }
        }
        $isLoup = ($totalVente > $totalAchat && $totalVente > 0) || (count($transactions) >= 3);

        $badges[] = [
            'id' => 'loup',
            'name' => 'Loup de Bourse',
            'icon' => '🐺',
            'color' => '#f59e0b', // Or/Ambre
            'description' => 'Trader aguerri : bénéfices validés ou volume de transactions élevé.',
            'unlocked' => $isLoup
        ];

        // 2. Badge Spéculateur (Margin Loan)
        $loans = $this->em->getRepository(MarginLoan::class)->findBy(['userId' => $user->getId()]);
        $isSpeculateur = count($loans) > 0;

        $badges[] = [
            'id' => 'speculateur',
            'name' => 'Spéculateur Marge',
            'icon' => '💸',
            'color' => '#3b82f6', // Bleu
            'description' => 'Maîtrise de l\'effet de levier : a emprunté sur marge.',
            'unlocked' => $isSpeculateur
        ];

        // 3. Badge Erudit (Formation) : Vérifie si la relation purchasedFormations existe et > 0
        $isErudit = method_exists($user, 'getPurchasedFormations') && $user->getPurchasedFormations() && $user->getPurchasedFormations()->count() > 0;
        $badges[] = [
            'id' => 'erudit',
            'name' => 'Érudit Financier',
            'icon' => '🎓',
            'color' => '#a855f7', // Violet
            'description' => 'A investi dans son éducation via le module E-learning.',
            'unlocked' => $isErudit
        ];
        
        // 4. Badge Investisseur (Premier achat Bourse)
        $isActive = count($transactions) > 0;
        $badges[] = [
            'id' => 'investisseur',
            'name' => 'Premier Sang',
            'icon' => '🎯',
            'color' => '#ef4444', // Rouge
            'description' => 'A exécuté son tout premier ordre en bourse.',
            'unlocked' => $isActive
        ];

        // 5. Badge Main de Diamant
        $isDiamond = false;
        foreach ($transactions as $t) {
            if ($t->getDateTransaction() < new \DateTime('-30 days')) {
                $isDiamond = true;
                break;
            }
        }
        $badges[] = [
            'id' => 'diamond',
            'name' => 'Main de Diamant',
            'icon' => '💎',
            'color' => '#60a5fa',
            'description' => 'Investisseur long terme : a conservé des actifs pendant plus de 30 jours.',
            'unlocked' => $isDiamond
        ];

        // 6. Badge Diversificateur
        $secteurs = [];
        foreach ($transactions as $t) {
            if ($t->getAction() && $t->getAction()->getSecteur()) {
                $secteurs[$t->getAction()->getSecteur()] = true;
            }
        }
        $isDiversified = count($secteurs) >= 2;
        $badges[] = [
            'id' => 'diversificateur',
            'name' => 'Diversificateur',
            'icon' => '🌈',
            'color' => '#10b981',
            'description' => 'Maître du risque : a investi dans plusieurs secteurs d\'activité.',
            'unlocked' => $isDiversified
        ];

        return $badges;
    }

    public function getEntrepriseBadges(User $user): array
    {
        $badges = [];

        // 1. Badge "Leader B2B" : A publié au moins un Appel d'Offre
        $appelsOffres = $this->em->getRepository(\App\Entity\AppelOffre::class)->findBy(['createdBy' => $user]);
        $isLeader = count($appelsOffres) > 0;
        
        $badges[] = [
            'id' => 'leader',
            'name' => 'Leader B2B',
            'icon' => '🤝',
            'color' => '#f59e0b', // Or
            'description' => 'Créateur d\'opportunités : a publié un ou plusieurs Appels d\'Offres.',
            'unlocked' => $isLeader
        ];

        // 2. Badge "Chasseur de Têtes" : A reçu au moins une candidature
        // On récupère les candidatures sur ses appels d'offres
        $hasCandidatures = false;
        foreach ($appelsOffres as $ao) {
            if ($ao->getCandidatures() && $ao->getCandidatures()->count() > 0) {
                $hasCandidatures = true;
                break;
            }
        }
        $badges[] = [
            'id' => 'recruteur',
            'name' => 'Chasseur de Têtes',
            'icon' => '🕵️‍♂️',
            'color' => '#3b82f6', // Bleu
            'description' => 'A attiré l\'attention de candidats sur le marché du travail.',
            'unlocked' => $hasCandidatures
        ];

        // 3. Badge "Investisseur Corporate" : L'entreprise investit en bourse (Trésorerie)
        $transactions = $this->transactionRepo->findBy(['user' => $user]);
        $isInvestisseur = count($transactions) > 0;
        
        $badges[] = [
            'id' => 'corporate_invest',
            'name' => 'Capital Corporate',
            'icon' => '📈',
            'color' => '#10b981', // Vert
            'description' => 'Fait fructifier la trésorerie de l\'entreprise sur les marchés boursiers.',
            'unlocked' => $isInvestisseur
        ];

        return $badges;
    }
}
