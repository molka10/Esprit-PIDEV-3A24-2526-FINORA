<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260429110352 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE action CHANGE prix_unitaire prix_unitaire NUMERIC(10, 2) NOT NULL');
        $this->addSql('ALTER TABLE action_news CHANGE impact_percent impact_percent NUMERIC(5, 2) NOT NULL');
        $this->addSql('ALTER TABLE margin_loan CHANGE montant_emprunte montant_emprunte NUMERIC(10, 2) NOT NULL');
        $this->addSql('ALTER TABLE transaction_bourse CHANGE prix_unitaire prix_unitaire NUMERIC(10, 2) NOT NULL');
        $this->addSql('ALTER TABLE transaction_wallet CHANGE montant montant NUMERIC(10, 2) NOT NULL');
        $this->addSql('ALTER TABLE users CHANGE balance balance NUMERIC(10, 2) DEFAULT 0 NOT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE action CHANGE prix_unitaire prix_unitaire DOUBLE PRECISION NOT NULL');
        $this->addSql('ALTER TABLE action_news CHANGE impact_percent impact_percent DOUBLE PRECISION NOT NULL');
        $this->addSql('ALTER TABLE margin_loan CHANGE montant_emprunte montant_emprunte DOUBLE PRECISION NOT NULL');
        $this->addSql('ALTER TABLE transaction_bourse CHANGE prix_unitaire prix_unitaire DOUBLE PRECISION NOT NULL');
        $this->addSql('ALTER TABLE transaction_wallet CHANGE montant montant DOUBLE PRECISION NOT NULL');
        $this->addSql('ALTER TABLE users CHANGE balance balance NUMERIC(10, 2) DEFAULT \'0.00\' NOT NULL');
    }
}
