<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260419184551 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE bourse_wishlist (id INT AUTO_INCREMENT NOT NULL, created_at DATETIME NOT NULL, user_id INT NOT NULL, action_id INT NOT NULL, INDEX IDX_8CAE9C8FA76ED395 (user_id), INDEX IDX_8CAE9C8F9D32F035 (action_id), UNIQUE INDEX user_action_unique (user_id, action_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE bourse_wishlist ADD CONSTRAINT FK_8CAE9C8FA76ED395 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE bourse_wishlist ADD CONSTRAINT FK_8CAE9C8F9D32F035 FOREIGN KEY (action_id) REFERENCES action (id_action) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE users CHANGE balance balance DOUBLE PRECISION DEFAULT 0 NOT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE bourse_wishlist DROP FOREIGN KEY FK_8CAE9C8FA76ED395');
        $this->addSql('ALTER TABLE bourse_wishlist DROP FOREIGN KEY FK_8CAE9C8F9D32F035');
        $this->addSql('DROP TABLE bourse_wishlist');
        $this->addSql('ALTER TABLE users CHANGE balance balance DOUBLE PRECISION DEFAULT \'0\' NOT NULL');
    }
}
