<?php
require_once __DIR__ . '/../vendor/autoload.php';
use App\Kernel;
use Symfony\Component\Dotenv\Dotenv;
use App\Entity\InvestmentNotification;
use App\Entity\InvestmentManagement;

(new Dotenv())->bootEnv(__DIR__ . '/../.env');
$kernel = new Kernel($_SERVER['APP_ENV'], (bool) $_SERVER['APP_DEBUG']);
$kernel->boot();
$container = $kernel->getContainer();
$em = $container->get('doctrine')->getManager();
$router = $container->get('router');

$notifications = $em->getRepository(InvestmentNotification::class)->findAll();
$count = 0;

foreach ($notifications as $notif) {
    if ($notif->getLink() === null) {
        $msg = $notif->getMessage();
        $title = $notif->getTitle();
        
        // Search for project name in quotes
        if (preg_match('/"([^"]+)"/', $msg, $matches)) {
            $projectName = $matches[1];
            $user = $notif->getUser();
            
            // Find management record for this user and project name
            $qb = $em->createQueryBuilder();
            $management = $qb->select('m')
                ->from(InvestmentManagement::class, 'm')
                ->join('m.investment', 'i')
                ->where('i.name = :name')
                ->andWhere('m.user = :user OR m.coInvestor = :user')
                ->setParameter('name', $projectName)
                ->setParameter('user', $user)
                ->setMaxResults(1)
                ->getQuery()
                ->getOneOrNullResult();

            if ($management) {
                $link = $router->generate('app_management_show', ['id' => $management->getId()]);
                $notif->setLink($link);
                $count++;
            }
        }
    }
}

$em->flush();
echo "Updated $count notifications with links.\n";
