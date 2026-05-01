<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260429114702 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE action DROP FOREIGN KEY `FK_47CC8C92FBF509F1`');
        $this->addSql('DROP INDEX IDX_47CC8C92FBF509F1 ON action');
        $this->addSql('ALTER TABLE action CHANGE id_bourse bourse_id INT NOT NULL');
        $this->addSql('ALTER TABLE action ADD CONSTRAINT FK_47CC8C924E67DDD1 FOREIGN KEY (bourse_id) REFERENCES bourse (id_bourse)');
        $this->addSql('CREATE INDEX IDX_47CC8C924E67DDD1 ON action (bourse_id)');
        $this->addSql('ALTER TABLE action_news DROP FOREIGN KEY `FK_6B32225E61FB397F`');
        $this->addSql('DROP INDEX IDX_6B32225E61FB397F ON action_news');
        $this->addSql('ALTER TABLE action_news CHANGE id_action action_id INT NOT NULL');
        $this->addSql('ALTER TABLE action_news ADD CONSTRAINT FK_6B32225E9D32F035 FOREIGN KEY (action_id) REFERENCES action (id_action)');
        $this->addSql('CREATE INDEX IDX_6B32225E9D32F035 ON action_news (action_id)');
        $this->addSql('ALTER TABLE appel_offre CHANGE created_by_id created_by_id INT NOT NULL');
        $this->addSql('ALTER TABLE notification_bourse DROP FOREIGN KEY `FK_1965424161FB397F`');
        $this->addSql('DROP INDEX IDX_1965424161FB397F ON notification_bourse');
        $this->addSql('ALTER TABLE notification_bourse CHANGE id_action action_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE notification_bourse ADD CONSTRAINT FK_196542419D32F035 FOREIGN KEY (action_id) REFERENCES action (id_action) ON DELETE SET NULL');
        $this->addSql('CREATE INDEX IDX_196542419D32F035 ON notification_bourse (action_id)');
        $this->addSql('ALTER TABLE users CHANGE balance balance NUMERIC(10, 2) DEFAULT 0 NOT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE action DROP FOREIGN KEY FK_47CC8C924E67DDD1');
        $this->addSql('DROP INDEX IDX_47CC8C924E67DDD1 ON action');
        $this->addSql('ALTER TABLE action CHANGE bourse_id id_bourse INT NOT NULL');
        $this->addSql('ALTER TABLE action ADD CONSTRAINT `FK_47CC8C92FBF509F1` FOREIGN KEY (id_bourse) REFERENCES bourse (id_bourse)');
        $this->addSql('CREATE INDEX IDX_47CC8C92FBF509F1 ON action (id_bourse)');
        $this->addSql('ALTER TABLE action_news DROP FOREIGN KEY FK_6B32225E9D32F035');
        $this->addSql('DROP INDEX IDX_6B32225E9D32F035 ON action_news');
        $this->addSql('ALTER TABLE action_news CHANGE action_id id_action INT NOT NULL');
        $this->addSql('ALTER TABLE action_news ADD CONSTRAINT `FK_6B32225E61FB397F` FOREIGN KEY (id_action) REFERENCES action (id_action)');
        $this->addSql('CREATE INDEX IDX_6B32225E61FB397F ON action_news (id_action)');
        $this->addSql('ALTER TABLE appel_offre CHANGE created_by_id created_by_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE notification_bourse DROP FOREIGN KEY FK_196542419D32F035');
        $this->addSql('DROP INDEX IDX_196542419D32F035 ON notification_bourse');
        $this->addSql('ALTER TABLE notification_bourse CHANGE action_id id_action INT DEFAULT NULL');
        $this->addSql('ALTER TABLE notification_bourse ADD CONSTRAINT `FK_1965424161FB397F` FOREIGN KEY (id_action) REFERENCES action (id_action) ON DELETE SET NULL');
        $this->addSql('CREATE INDEX IDX_1965424161FB397F ON notification_bourse (id_action)');
        $this->addSql('ALTER TABLE users CHANGE balance balance NUMERIC(10, 2) DEFAULT \'0.00\' NOT NULL');
    }
}
