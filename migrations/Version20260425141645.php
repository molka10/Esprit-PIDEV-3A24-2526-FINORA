<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260425141645 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE rating_centre (id INT AUTO_INCREMENT NOT NULL, note INT NOT NULL, commentaire LONGTEXT DEFAULT NULL, created_at DATETIME NOT NULL, user_id INT NOT NULL, centre_id INT NOT NULL, INDEX IDX_E95280FDA76ED395 (user_id), INDEX IDX_E95280FD463CD7C3 (centre_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE rating_centre ADD CONSTRAINT FK_E95280FDA76ED395 FOREIGN KEY (user_id) REFERENCES users (id)');
        $this->addSql('ALTER TABLE rating_centre ADD CONSTRAINT FK_E95280FD463CD7C3 FOREIGN KEY (centre_id) REFERENCES centre_formation (id)');
        $this->addSql('ALTER TABLE users CHANGE balance balance DOUBLE PRECISION DEFAULT 0 NOT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE rating_centre DROP FOREIGN KEY FK_E95280FDA76ED395');
        $this->addSql('ALTER TABLE rating_centre DROP FOREIGN KEY FK_E95280FD463CD7C3');
        $this->addSql('DROP TABLE rating_centre');
        $this->addSql('ALTER TABLE users CHANGE balance balance DOUBLE PRECISION DEFAULT \'0\' NOT NULL');
    }
}
