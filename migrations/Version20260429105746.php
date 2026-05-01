<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260429105746 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE candidature CHANGE appel_offre_id appel_offre_id INT NOT NULL');
        $this->addSql('ALTER TABLE category ADD CONSTRAINT FK_64C19C1A76ED395 FOREIGN KEY (user_id) REFERENCES users (id)');
        $this->addSql('CREATE INDEX IDX_64C19C1A76ED395 ON category (user_id)');
        $this->addSql('ALTER TABLE formation CHANGE prix prix NUMERIC(10, 2) DEFAULT NULL');
        $this->addSql('ALTER TABLE recharge_requests CHANGE amount amount NUMERIC(10, 2) NOT NULL');
        $this->addSql('ALTER TABLE recharge_requests ADD CONSTRAINT FK_30C196BDA76ED395 FOREIGN KEY (user_id) REFERENCES users (id)');
        $this->addSql('CREATE INDEX IDX_30C196BDA76ED395 ON recharge_requests (user_id)');
        $this->addSql('ALTER TABLE transaction_bourse CHANGE montant_total montant_total NUMERIC(10, 2) NOT NULL, CHANGE commission commission NUMERIC(10, 2) NOT NULL');
        $this->addSql('ALTER TABLE transaction_wallet CHANGE user_id user_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE transaction_wallet ADD CONSTRAINT FK_A15E05FA76ED395 FOREIGN KEY (user_id) REFERENCES users (id)');
        $this->addSql('CREATE INDEX IDX_A15E05FA76ED395 ON transaction_wallet (user_id)');
        $this->addSql('ALTER TABLE users CHANGE balance balance NUMERIC(10, 2) DEFAULT 0 NOT NULL');
        $this->addSql('ALTER TABLE wishlist CHANGE price price NUMERIC(10, 2) NOT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE candidature CHANGE appel_offre_id appel_offre_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE category DROP FOREIGN KEY FK_64C19C1A76ED395');
        $this->addSql('DROP INDEX IDX_64C19C1A76ED395 ON category');
        $this->addSql('ALTER TABLE formation CHANGE prix prix DOUBLE PRECISION DEFAULT NULL');
        $this->addSql('ALTER TABLE recharge_requests DROP FOREIGN KEY FK_30C196BDA76ED395');
        $this->addSql('DROP INDEX IDX_30C196BDA76ED395 ON recharge_requests');
        $this->addSql('ALTER TABLE recharge_requests CHANGE amount amount DOUBLE PRECISION NOT NULL');
        $this->addSql('ALTER TABLE transaction_bourse CHANGE montant_total montant_total DOUBLE PRECISION NOT NULL, CHANGE commission commission DOUBLE PRECISION NOT NULL');
        $this->addSql('ALTER TABLE transaction_wallet DROP FOREIGN KEY FK_A15E05FA76ED395');
        $this->addSql('DROP INDEX IDX_A15E05FA76ED395 ON transaction_wallet');
        $this->addSql('ALTER TABLE transaction_wallet CHANGE user_id user_id INT NOT NULL');
        $this->addSql('ALTER TABLE users CHANGE balance balance NUMERIC(10, 2) DEFAULT \'0.00\' NOT NULL');
        $this->addSql('ALTER TABLE wishlist CHANGE price price DOUBLE PRECISION NOT NULL');
    }
}
