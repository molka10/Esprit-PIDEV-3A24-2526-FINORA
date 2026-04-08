<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260408203117 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE formation ADD updated_at DATETIME DEFAULT NULL');
        $this->addSql('DROP INDEX fk_lesson_formation ON lesson');
        $this->addSql('ALTER TABLE quiz_result DROP FOREIGN KEY `fk_quiz_lesson`');
        $this->addSql('DROP INDEX fk_quiz_lesson ON quiz_result');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE formation DROP updated_at');
        $this->addSql('CREATE INDEX fk_lesson_formation ON lesson (formation_id)');
        $this->addSql('ALTER TABLE quiz_result ADD CONSTRAINT `fk_quiz_lesson` FOREIGN KEY (lesson_id) REFERENCES lesson (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('CREATE INDEX fk_quiz_lesson ON quiz_result (lesson_id)');
    }
}
