<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260420192544 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE notification_bourse (id INT AUTO_INCREMENT NOT NULL, type VARCHAR(30) NOT NULL, titre VARCHAR(200) NOT NULL, message LONGTEXT NOT NULL, is_read TINYINT NOT NULL, created_at DATETIME NOT NULL, user_id INT NOT NULL, id_action INT DEFAULT NULL, INDEX IDX_19654241A76ED395 (user_id), INDEX IDX_1965424161FB397F (id_action), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE notification_bourse ADD CONSTRAINT FK_19654241A76ED395 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE notification_bourse ADD CONSTRAINT FK_1965424161FB397F FOREIGN KEY (id_action) REFERENCES action (id_action) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE users CHANGE balance balance DOUBLE PRECISION DEFAULT 0 NOT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE notification_bourse DROP FOREIGN KEY FK_19654241A76ED395');
        $this->addSql('ALTER TABLE notification_bourse DROP FOREIGN KEY FK_1965424161FB397F');
        $this->addSql('DROP TABLE notification_bourse');
        $this->addSql('ALTER TABLE users CHANGE balance balance DOUBLE PRECISION DEFAULT \'0\' NOT NULL');
    }
}
