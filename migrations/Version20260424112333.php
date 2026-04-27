<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260424112333 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE investment_comment (id INT AUTO_INCREMENT NOT NULL, content LONGTEXT NOT NULL, created_at DATETIME NOT NULL, user_id INT NOT NULL, investment_id INT NOT NULL, INDEX IDX_2B1E8C8FA76ED395 (user_id), INDEX IDX_2B1E8C8F6E1B4FD5 (investment_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE investment_comment ADD CONSTRAINT FK_2B1E8C8FA76ED395 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE investment_comment ADD CONSTRAINT FK_2B1E8C8F6E1B4FD5 FOREIGN KEY (investment_id) REFERENCES investment (investment_id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE investment ADD comments_json JSON DEFAULT NULL');
        $this->addSql('ALTER TABLE users CHANGE balance balance DOUBLE PRECISION DEFAULT 0 NOT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE investment_comment DROP FOREIGN KEY FK_2B1E8C8FA76ED395');
        $this->addSql('ALTER TABLE investment_comment DROP FOREIGN KEY FK_2B1E8C8F6E1B4FD5');
        $this->addSql('DROP TABLE investment_comment');
        $this->addSql('ALTER TABLE investment DROP comments_json');
        $this->addSql('ALTER TABLE users CHANGE balance balance DOUBLE PRECISION DEFAULT \'0\' NOT NULL');
    }
}
