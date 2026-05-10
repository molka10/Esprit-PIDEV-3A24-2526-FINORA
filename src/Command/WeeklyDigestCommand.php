<?php

namespace App\Command;

use App\Entity\User;
use App\Entity\InvestmentNotification;
use App\Entity\NotificationBourse;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;

#[AsCommand(
    name: 'app:send-weekly-digest',
    description: 'Sends a weekly activity summary to users who opted in.',
)]
class WeeklyDigestCommand extends Command
{
    public function __construct(
        private EntityManagerInterface $entityManager,
        private MailerInterface $mailer
    ) {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);
        $users = $this->entityManager->getRepository(User::class)->findBy(['emailDigestEnabled' => true]);

        $io->progressStart(count($users));
        $oneWeekAgo = new \DateTime('-7 days');

        foreach ($users as $user) {
            /** @var User $user */
            
            // Gather activity
            $investNotifs = $this->entityManager->getRepository(InvestmentNotification::class)->createQueryBuilder('n')
                ->where('n.user = :user')
                ->andWhere('n.createdAt >= :date')
                ->setParameter('user', $user)
                ->setParameter('date', $oneWeekAgo)
                ->getQuery()->getResult();

            $bourseNotifs = $this->entityManager->getRepository(NotificationBourse::class)->createQueryBuilder('n')
                ->where('n.user = :user')
                ->andWhere('n.createdAt >= :date')
                ->setParameter('user', $user)
                ->setParameter('date', $oneWeekAgo)
                ->getQuery()->getResult();

            if (count($investNotifs) === 0 && count($bourseNotifs) === 0) {
                $io->progressAdvance();
                continue;
            }

            // Send Email
            $email = (new Email())
                ->from('no-reply@finora.com')
                ->to($user->getEmail())
                ->subject('Votre Digest Hebdomadaire FINORA 📊')
                ->html($this->generateDigestHtml($user, $investNotifs, $bourseNotifs));

            try {
                $this->mailer->send($email);
            } catch (\Exception $e) {
                $io->error('Failed to send email to ' . $user->getEmail());
            }

            $io->progressAdvance();
        }

        $io->progressFinish();
        $io->success('Weekly digests sent successfully.');

        return Command::SUCCESS;
    }

    private function generateDigestHtml(User $user, array $investNotifs, array $bourseNotifs): string
    {
        $html = "<h1>Bonjour " . $user->getUsername() . " !</h1>";
        $html .= "<p>Voici le résumé de votre activité sur FINORA cette semaine :</p>";
        
        if (count($investNotifs) > 0) {
            $html .= "<h3>Investissements & Offres</h3><ul>";
            foreach ($investNotifs as $n) {
                $html .= "<li><strong>" . $n->getTitle() . "</strong>: " . $n->getMessage() . "</li>";
            }
            $html .= "</ul>";
        }

        if (count($bourseNotifs) > 0) {
            $html .= "<h3>Bourse & Marchés</h3><ul>";
            foreach ($bourseNotifs as $n) {
                $html .= "<li><strong>" . $n->getTitre() . "</strong>: " . $n->getMessage() . "</li>";
            }
            $html .= "</ul>";
        }

        $html .= "<br><p>À bientôt sur <a href='https://finora.com'>FINORA</a> !</p>";
        
        return $html;
    }
}
