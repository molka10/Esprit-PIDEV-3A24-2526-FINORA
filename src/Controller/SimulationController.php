<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[IsGranted('ROLE_USER')]
final class SimulationController extends AbstractController
{
    #[Route('/simulation', name: 'app_simulation')]
    public function index(): Response
    {
        // Realistic market assets for the simulator
        $assets = [
            ['symbol' => 'AAPL', 'name' => 'Apple Inc.', 'price' => 182.63, 'change' => 1.24, 'sector' => 'Technology', 'icon' => 'bi-apple'],
            ['symbol' => 'TSLA', 'name' => 'Tesla Inc.', 'price' => 177.90, 'change' => -2.34, 'sector' => 'Auto/EV', 'icon' => 'bi-lightning-charge'],
            ['symbol' => 'NVDA', 'name' => 'NVIDIA Corp.', 'price' => 875.40, 'change' => 4.12, 'sector' => 'Semiconductors', 'icon' => 'bi-cpu'],
            ['symbol' => 'MSFT', 'name' => 'Microsoft Corp.', 'price' => 415.50, 'change' => 0.87, 'sector' => 'Technology', 'icon' => 'bi-windows'],
            ['symbol' => 'AMZN', 'name' => 'Amazon.com Inc.', 'price' => 188.40, 'change' => -0.55, 'sector' => 'E-Commerce', 'icon' => 'bi-shop'],
            ['symbol' => 'GOOGL', 'name' => 'Alphabet Inc.', 'price' => 173.52, 'change' => 1.08, 'sector' => 'Technology', 'icon' => 'bi-search'],
            ['symbol' => 'META', 'name' => 'Meta Platforms', 'price' => 492.10, 'change' => 2.10, 'sector' => 'Social Media', 'icon' => 'bi-meta'],
            ['symbol' => 'GOLD', 'name' => 'Gold (XAU/USD)', 'price' => 2342.00, 'change' => 0.45, 'sector' => 'Commodities', 'icon' => 'bi-gem'],
            ['symbol' => 'BTC', 'name' => 'Bitcoin', 'price' => 62400.00, 'change' => 3.22, 'sector' => 'Crypto', 'icon' => 'bi-currency-bitcoin'],
        ];

        $scenarios = [
            [
                'id' => 'crash',
                'title' => 'Krach Boursier 2.0',
                'emoji' => '💥',
                'description' => 'Les marchés mondiaux chutent de 35% suite à une crise bancaire majeure. Que faites-vous avec votre portefeuille ?',
                'difficulty' => 'Difficile',
                'badge_class' => 'bg-danger',
                'impact' => -35,
            ],
            [
                'id' => 'bull',
                'title' => 'Bull Run Tech',
                'emoji' => '🚀',
                'description' => 'Le secteur technologique explose suite à une révolution en IA. Vous avez 1 000 $ — comment maximisez-vous vos gains ?',
                'difficulty' => 'Moyen',
                'badge_class' => 'bg-success',
                'impact' => +48,
            ],
            [
                'id' => 'inflation',
                'title' => 'Pic d\'Inflation',
                'emoji' => '📈',
                'description' => 'L\'inflation atteint 9%. Les taux montent en flèche. Quelle stratégie adoptez-vous pour protéger votre capital ?',
                'difficulty' => 'Moyen',
                'badge_class' => 'bg-warning',
                'impact' => -12,
            ],
            [
                'id' => 'ipo',
                'title' => 'IPO Explosive',
                'emoji' => '⚡',
                'description' => 'Une startup révolutionnaire entre en bourse. Investissez-vous tôt pour un gain potentiel ou attendez-vous la stabilisation ?',
                'difficulty' => 'Facile',
                'badge_class' => 'bg-info',
                'impact' => +120,
            ],
        ];

        return $this->render('simulation/index.html.twig', [
            'assets' => $assets,
            'scenarios' => $scenarios,
        ]);
    }
}
