<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260429120739 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE transaction_bourse DROP FOREIGN KEY `FK_ABBFCE5C61FB397F`');
        $this->addSql('ALTER TABLE transaction_bourse DROP FOREIGN KEY `FK_ABBFCE5C6B3CA4B`');
        $this->addSql('DROP INDEX IDX_ABBFCE5C61FB397F ON transaction_bourse');
        $this->addSql('DROP INDEX IDX_ABBFCE5C6B3CA4B ON transaction_bourse');
        $this->addSql('ALTER TABLE transaction_bourse CHANGE id_action action_id INT NOT NULL, CHANGE id_user user_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE transaction_bourse ADD CONSTRAINT FK_ABBFCE5C9D32F035 FOREIGN KEY (action_id) REFERENCES action (id_action)');
        $this->addSql('ALTER TABLE transaction_bourse ADD CONSTRAINT FK_ABBFCE5CA76ED395 FOREIGN KEY (user_id) REFERENCES users (id)');
        $this->addSql('CREATE INDEX IDX_ABBFCE5C9D32F035 ON transaction_bourse (action_id)');
        $this->addSql('CREATE INDEX IDX_ABBFCE5CA76ED395 ON transaction_bourse (user_id)');
        $this->addSql('ALTER TABLE users CHANGE balance balance NUMERIC(10, 2) DEFAULT 0 NOT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE transaction_bourse DROP FOREIGN KEY FK_ABBFCE5C9D32F035');
        $this->addSql('ALTER TABLE transaction_bourse DROP FOREIGN KEY FK_ABBFCE5CA76ED395');
        $this->addSql('DROP INDEX IDX_ABBFCE5C9D32F035 ON transaction_bourse');
        $this->addSql('DROP INDEX IDX_ABBFCE5CA76ED395 ON transaction_bourse');
        $this->addSql('ALTER TABLE transaction_bourse CHANGE action_id id_action INT NOT NULL, CHANGE user_id id_user INT DEFAULT NULL');
        $this->addSql('ALTER TABLE transaction_bourse ADD CONSTRAINT `FK_ABBFCE5C61FB397F` FOREIGN KEY (id_action) REFERENCES action (id_action)');
        $this->addSql('ALTER TABLE transaction_bourse ADD CONSTRAINT `FK_ABBFCE5C6B3CA4B` FOREIGN KEY (id_user) REFERENCES users (id)');
        $this->addSql('CREATE INDEX IDX_ABBFCE5C61FB397F ON transaction_bourse (id_action)');
        $this->addSql('CREATE INDEX IDX_ABBFCE5C6B3CA4B ON transaction_bourse (id_user)');
        $this->addSql('ALTER TABLE users CHANGE balance balance NUMERIC(10, 2) DEFAULT \'0.00\' NOT NULL');
    }
}
