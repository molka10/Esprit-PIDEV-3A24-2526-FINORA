<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260418020315 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE user_formation_purchased (user_id INT NOT NULL, formation_id INT NOT NULL, INDEX IDX_C9098B96A76ED395 (user_id), INDEX IDX_C9098B965200282E (formation_id), PRIMARY KEY (user_id, formation_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE user_formation_purchased ADD CONSTRAINT FK_C9098B96A76ED395 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE user_formation_purchased ADD CONSTRAINT FK_C9098B965200282E FOREIGN KEY (formation_id) REFERENCES formation (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE formation ADD rating_count INT DEFAULT 0 NOT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE user_formation_purchased DROP FOREIGN KEY FK_C9098B96A76ED395');
        $this->addSql('ALTER TABLE user_formation_purchased DROP FOREIGN KEY FK_C9098B965200282E');
        $this->addSql('DROP TABLE user_formation_purchased');
        $this->addSql('ALTER TABLE formation DROP rating_count');
    }
}
