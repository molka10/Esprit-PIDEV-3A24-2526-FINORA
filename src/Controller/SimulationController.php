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
        $assets = [
            ['symbol' => 'AAPL', 'name' => 'Apple Inc.', 'price' => 182.63, 'change' => 1.24, 'sector' => 'Tech', 'icon' => 'bi-apple'],
            ['symbol' => 'TSLA', 'name' => 'Tesla Inc.', 'price' => 177.90, 'change' => -2.34, 'sector' => 'Auto', 'icon' => 'bi-lightning-charge'],
            ['symbol' => 'NVDA', 'name' => 'NVIDIA Corp.', 'price' => 875.40, 'change' => 4.12, 'sector' => 'Semi', 'icon' => 'bi-cpu'],
            ['symbol' => 'MSFT', 'name' => 'Microsoft', 'price' => 415.50, 'change' => 0.87, 'sector' => 'Tech', 'icon' => 'bi-windows'],
            ['symbol' => 'AMZN', 'name' => 'Amazon', 'price' => 188.40, 'change' => -0.55, 'sector' => 'E-Com', 'icon' => 'bi-shop'],
            ['symbol' => 'GOOGL', 'name' => 'Alphabet', 'price' => 173.52, 'change' => 1.08, 'sector' => 'Tech', 'icon' => 'bi-search'],
            ['symbol' => 'GOLD', 'name' => 'Gold (XAU)', 'price' => 2342.00, 'change' => 0.45, 'sector' => 'Commodity', 'icon' => 'bi-gem'],
            ['symbol' => 'BTC', 'name' => 'Bitcoin', 'price' => 62400.00, 'change' => 3.22, 'sector' => 'Crypto', 'icon' => 'bi-currency-bitcoin'],
        ];

        $scenarios = [
            ['id'=>'crash','title'=>'Krach Boursier','emoji'=>'💥','description'=>'Les marchés chutent de 35% suite à une crise bancaire.','difficulty'=>'Difficile','badge_class'=>'bg-danger','impact'=>-35],
            ['id'=>'bull','title'=>'Bull Run Tech','emoji'=>'🚀','description'=>'Le secteur tech explose grâce à l\'IA.','difficulty'=>'Moyen','badge_class'=>'bg-success','impact'=>48],
            ['id'=>'inflation','title'=>'Pic d\'Inflation','emoji'=>'📈','description'=>'Inflation à 9%, taux en hausse.','difficulty'=>'Moyen','badge_class'=>'bg-warning','impact'=>-12],
            ['id'=>'ipo','title'=>'IPO Explosive','emoji'=>'⚡','description'=>'Une startup révolutionnaire entre en bourse.','difficulty'=>'Facile','badge_class'=>'bg-info','impact'=>120],
        ];

        $investments = [
            ['id'=>1,'name'=>'Résidence Solaire Tunis','category'=>'MAISON','yield'=>8.5,'risk'=>'LOW','amount'=>50000,'duration'=>'24 mois','icon'=>'bi-house'],
            ['id'=>2,'name'=>'Startup FinTech AI','category'=>'STARTUP','yield'=>22.0,'risk'=>'HIGH','amount'=>15000,'duration'=>'18 mois','icon'=>'bi-rocket-takeoff'],
            ['id'=>3,'name'=>'Ferme Bio Nabeul','category'=>'AGRICULTURE','yield'=>12.0,'risk'=>'MEDIUM','amount'=>30000,'duration'=>'36 mois','icon'=>'bi-tree'],
            ['id'=>4,'name'=>'Parc Éolien Bizerte','category'=>'ENERGIE','yield'=>10.5,'risk'=>'LOW','amount'=>75000,'duration'=>'48 mois','icon'=>'bi-wind'],
            ['id'=>5,'name'=>'Hôtel Djerba Premium','category'=>'HOTEL','yield'=>15.0,'risk'=>'MEDIUM','amount'=>120000,'duration'=>'60 mois','icon'=>'bi-building'],
        ];

        $appels = [
            ['id'=>1,'title'=>'Développement App Mobile Banking','budget'=>'25,000 TND','deadline'=>'30 jours','category'=>'Tech','skills'=>['React Native','Node.js','API REST'],'difficulty'=>'Senior'],
            ['id'=>2,'title'=>'Audit Financier PME','budget'=>'8,000 TND','deadline'=>'15 jours','category'=>'Finance','skills'=>['Comptabilité','Excel','Audit'],'difficulty'=>'Intermédiaire'],
            ['id'=>3,'title'=>'Campagne Marketing Digital','budget'=>'12,000 TND','deadline'=>'20 jours','category'=>'Marketing','skills'=>['SEO','Google Ads','Analytics'],'difficulty'=>'Junior'],
            ['id'=>4,'title'=>'Conseil Juridique Startup','budget'=>'5,000 TND','deadline'=>'10 jours','category'=>'Juridique','skills'=>['Droit des affaires','Contrats','RGPD'],'difficulty'=>'Senior'],
        ];

        return $this->render('simulation/index.html.twig', [
            'assets' => $assets,
            'scenarios' => $scenarios,
            'investments' => $investments,
            'appels' => $appels,
        ]);
    }
}
