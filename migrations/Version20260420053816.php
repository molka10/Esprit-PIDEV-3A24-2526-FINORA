<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260420053816 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE user_favorite_appels (user_id INT NOT NULL, appel_offre_id INT NOT NULL, INDEX IDX_80B6309CA76ED395 (user_id), INDEX IDX_80B6309C308E35F8 (appel_offre_id), PRIMARY KEY (user_id, appel_offre_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE user_favorite_appels ADD CONSTRAINT FK_80B6309CA76ED395 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE user_favorite_appels ADD CONSTRAINT FK_80B6309C308E35F8 FOREIGN KEY (appel_offre_id) REFERENCES appel_offre (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE users CHANGE balance balance DOUBLE PRECISION DEFAULT 0 NOT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE user_favorite_appels DROP FOREIGN KEY FK_80B6309CA76ED395');
        $this->addSql('ALTER TABLE user_favorite_appels DROP FOREIGN KEY FK_80B6309C308E35F8');
        $this->addSql('DROP TABLE user_favorite_appels');
        $this->addSql('ALTER TABLE users CHANGE balance balance DOUBLE PRECISION DEFAULT \'0\' NOT NULL');
    }
}
