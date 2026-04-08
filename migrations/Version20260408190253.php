<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260408190253 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Update formation lesson schema';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('ALTER TABLE formation CHANGE titre titre VARCHAR(255) NOT NULL, CHANGE description description LONGTEXT DEFAULT NULL, CHANGE categorie categorie VARCHAR(255) NOT NULL, CHANGE niveau niveau VARCHAR(255) NOT NULL, CHANGE is_published is_published INT NOT NULL');

        $this->addSql('ALTER TABLE lesson DROP latitude, DROP longitude, CHANGE titre titre VARCHAR(255) NOT NULL, CHANGE contenu contenu LONGTEXT DEFAULT NULL, CHANGE ordre ordre INT NOT NULL, CHANGE duree_minutes duree_minutes INT NOT NULL');

        $this->addSql('CREATE INDEX IDX_F87474F35200282E ON lesson (formation_id)');
        $this->addSql('ALTER TABLE lesson ADD CONSTRAINT FK_F87474F35200282E FOREIGN KEY (formation_id) REFERENCES formation (id)');

        $this->addSql('ALTER TABLE quiz_result CHANGE student_name student_name VARCHAR(255) NOT NULL, CHANGE passed passed INT NOT NULL, CHANGE taken_at taken_at DATETIME DEFAULT NULL');

        $this->addSql('ALTER TABLE training_center CHANGE name name VARCHAR(255) NOT NULL, CHANGE lat lat VARCHAR(255) NOT NULL, CHANGE lng lng VARCHAR(255) NOT NULL');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE formation CHANGE titre titre VARCHAR(120) NOT NULL, CHANGE description description TEXT DEFAULT NULL, CHANGE categorie categorie VARCHAR(80) NOT NULL, CHANGE niveau niveau VARCHAR(30) NOT NULL, CHANGE is_published is_published TINYINT DEFAULT 0 NOT NULL');

        $this->addSql('ALTER TABLE lesson DROP FOREIGN KEY FK_F87474F35200282E');
        $this->addSql('DROP INDEX IDX_F87474F35200282E ON lesson');

        $this->addSql('ALTER TABLE lesson ADD latitude DOUBLE PRECISION DEFAULT NULL, ADD longitude DOUBLE PRECISION DEFAULT NULL, CHANGE titre titre VARCHAR(120) NOT NULL, CHANGE contenu contenu TEXT DEFAULT NULL, CHANGE ordre ordre INT DEFAULT 1 NOT NULL, CHANGE duree_minutes duree_minutes INT DEFAULT 0');

        $this->addSql('ALTER TABLE quiz_result CHANGE student_name student_name VARCHAR(100) NOT NULL, CHANGE passed passed TINYINT NOT NULL, CHANGE taken_at taken_at DATETIME DEFAULT CURRENT_TIMESTAMP');

        $this->addSql('ALTER TABLE training_center CHANGE name name VARCHAR(120) NOT NULL, CHANGE lat lat DOUBLE PRECISION NOT NULL, CHANGE lng lng DOUBLE PRECISION NOT NULL');
    }
}