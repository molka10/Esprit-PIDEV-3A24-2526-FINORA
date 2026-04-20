<?php

namespace App\Command;

use App\Repository\AppelOffreRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;

#[AsCommand(
    name: 'app:cloturer-appels-offre',
    description: 'Clôture automatiquement les appels d\'offre dont la date limite est dépassée',
)]
class CloturerAppelsOffreCommand extends Command
{
    public function __construct(
        private AppelOffreRepository $appelOffreRepository,
        private EntityManagerInterface $entityManager
    ) {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);
        $io->title('Vérification des appels d\'offre expirés');

        $today = new \DateTime();
        $appelsExpires = $this->appelOffreRepository->findAppelsExpires($today);

        if (empty($appelsExpires)) {
            $io->success('Aucun appel d\'offre à clôturer.');
            return Command::SUCCESS;
        }

        $count = 0;
        foreach ($appelsExpires as $appel) {
            $appel->setStatut('closed');
            $this->entityManager->persist($appel);
            $io->text(sprintf(
                '✅ Clôturé : "%s" (date limite : %s)',
                $appel->getTitre(),
                $appel->getDateLimite()->format('d/m/Y')
            ));
            $count++;
        }

        $this->entityManager->flush();
        $io->success(sprintf('%d appel(s) d\'offre clôturé(s) avec succès.', $count));

        return Command::SUCCESS;
    }
}