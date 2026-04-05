<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260404044945 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE appel_offre (id INT AUTO_INCREMENT NOT NULL, titre VARCHAR(255) NOT NULL, description LONGTEXT DEFAULT NULL, type VARCHAR(50) NOT NULL, budget_min NUMERIC(10, 2) DEFAULT NULL, budget_max NUMERIC(10, 2) DEFAULT NULL, devise VARCHAR(20) DEFAULT NULL, date_limite DATE DEFAULT NULL, statut VARCHAR(50) NOT NULL, created_at DATETIME NOT NULL, categorie_id INT DEFAULT NULL, created_by_id INT DEFAULT NULL, INDEX IDX_BC56FD47BCF5E72D (categorie_id), INDEX IDX_BC56FD47B03A8386 (created_by_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE candidature (id INT AUTO_INCREMENT NOT NULL, montant_propose NUMERIC(10, 2) DEFAULT NULL, message LONGTEXT DEFAULT NULL, statut VARCHAR(50) NOT NULL, created_at DATETIME NOT NULL, appel_offre_id INT NOT NULL, user_id INT DEFAULT NULL, INDEX IDX_E33BD3B8308E35F8 (appel_offre_id), INDEX IDX_E33BD3B8A76ED395 (user_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE categorie (id INT AUTO_INCREMENT NOT NULL, nom VARCHAR(100) NOT NULL, PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE user (id INT AUTO_INCREMENT NOT NULL, email VARCHAR(180) NOT NULL, roles JSON NOT NULL, password VARCHAR(255) NOT NULL, nom VARCHAR(150) NOT NULL, telephone VARCHAR(20) DEFAULT NULL, UNIQUE INDEX UNIQ_IDENTIFIER_EMAIL (email), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE messenger_messages (id BIGINT AUTO_INCREMENT NOT NULL, body LONGTEXT NOT NULL, headers LONGTEXT NOT NULL, queue_name VARCHAR(190) NOT NULL, created_at DATETIME NOT NULL, available_at DATETIME NOT NULL, delivered_at DATETIME DEFAULT NULL, INDEX IDX_75EA56E0FB7336F0E3BD61CE16BA31DBBF396750 (queue_name, available_at, delivered_at, id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE appel_offre ADD CONSTRAINT FK_BC56FD47BCF5E72D FOREIGN KEY (categorie_id) REFERENCES categorie (id)');
        $this->addSql('ALTER TABLE appel_offre ADD CONSTRAINT FK_BC56FD47B03A8386 FOREIGN KEY (created_by_id) REFERENCES user (id)');
        $this->addSql('ALTER TABLE candidature ADD CONSTRAINT FK_E33BD3B8308E35F8 FOREIGN KEY (appel_offre_id) REFERENCES appel_offre (id)');
        $this->addSql('ALTER TABLE candidature ADD CONSTRAINT FK_E33BD3B8A76ED395 FOREIGN KEY (user_id) REFERENCES user (id)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE appel_offre DROP FOREIGN KEY FK_BC56FD47BCF5E72D');
        $this->addSql('ALTER TABLE appel_offre DROP FOREIGN KEY FK_BC56FD47B03A8386');
        $this->addSql('ALTER TABLE candidature DROP FOREIGN KEY FK_E33BD3B8308E35F8');
        $this->addSql('ALTER TABLE candidature DROP FOREIGN KEY FK_E33BD3B8A76ED395');
        $this->addSql('DROP TABLE appel_offre');
        $this->addSql('DROP TABLE candidature');
        $this->addSql('DROP TABLE categorie');
        $this->addSql('DROP TABLE user');
        $this->addSql('DROP TABLE messenger_messages');
    }
}
