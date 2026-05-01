<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260429120350 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE cards ADD CONSTRAINT FK_4C258FDA76ED395 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('CREATE INDEX IDX_4C258FDA76ED395 ON cards (user_id)');
        $this->addSql('ALTER TABLE margin_loan ADD CONSTRAINT FK_B89DAA75A76ED395 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('CREATE INDEX IDX_B89DAA75A76ED395 ON margin_loan (user_id)');
        $this->addSql('ALTER TABLE quiz_result CHANGE lesson_id lesson_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE quiz_result ADD CONSTRAINT FK_FE2E314ACDF80196 FOREIGN KEY (lesson_id) REFERENCES lesson (id) ON DELETE SET NULL');
        $this->addSql('CREATE INDEX IDX_FE2E314ACDF80196 ON quiz_result (lesson_id)');
        $this->addSql('ALTER TABLE recharge_requests ADD CONSTRAINT FK_30C196BD4ACC9A20 FOREIGN KEY (card_id) REFERENCES cards (id) ON DELETE CASCADE');
        $this->addSql('CREATE INDEX IDX_30C196BD4ACC9A20 ON recharge_requests (card_id)');
        $this->addSql('ALTER TABLE user_api_key ADD CONSTRAINT FK_911FF397A76ED395 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('CREATE INDEX IDX_911FF397A76ED395 ON user_api_key (user_id)');
        $this->addSql('ALTER TABLE users CHANGE balance balance NUMERIC(10, 2) DEFAULT 0 NOT NULL');
        $this->addSql('ALTER TABLE wishlist ADD CONSTRAINT FK_9CE12A31A76ED395 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('CREATE INDEX IDX_9CE12A31A76ED395 ON wishlist (user_id)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE cards DROP FOREIGN KEY FK_4C258FDA76ED395');
        $this->addSql('DROP INDEX IDX_4C258FDA76ED395 ON cards');
        $this->addSql('ALTER TABLE margin_loan DROP FOREIGN KEY FK_B89DAA75A76ED395');
        $this->addSql('DROP INDEX IDX_B89DAA75A76ED395 ON margin_loan');
        $this->addSql('ALTER TABLE quiz_result DROP FOREIGN KEY FK_FE2E314ACDF80196');
        $this->addSql('DROP INDEX IDX_FE2E314ACDF80196 ON quiz_result');
        $this->addSql('ALTER TABLE quiz_result CHANGE lesson_id lesson_id INT NOT NULL');
        $this->addSql('ALTER TABLE recharge_requests DROP FOREIGN KEY FK_30C196BD4ACC9A20');
        $this->addSql('DROP INDEX IDX_30C196BD4ACC9A20 ON recharge_requests');
        $this->addSql('ALTER TABLE users CHANGE balance balance NUMERIC(10, 2) DEFAULT \'0.00\' NOT NULL');
        $this->addSql('ALTER TABLE user_api_key DROP FOREIGN KEY FK_911FF397A76ED395');
        $this->addSql('DROP INDEX IDX_911FF397A76ED395 ON user_api_key');
        $this->addSql('ALTER TABLE wishlist DROP FOREIGN KEY FK_9CE12A31A76ED395');
        $this->addSql('DROP INDEX IDX_9CE12A31A76ED395 ON wishlist');
    }
}
