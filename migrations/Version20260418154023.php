<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260418154023 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('DROP TABLE user_biometrics');
        $this->addSql('DROP TABLE wallet_appel_offre_link');
        $this->addSql('DROP TABLE wallet_bourse_link');
        $this->addSql('DROP TABLE wallet_formation_link');
        $this->addSql('DROP TABLE wallet_investment_link');
        $this->addSql('ALTER TABLE category DROP FOREIGN KEY `fk_category_user`');
        $this->addSql('DROP INDEX fk_category_user ON category');
        $this->addSql('ALTER TABLE category CHANGE nom nom VARCHAR(255) NOT NULL, CHANGE priorite priorite VARCHAR(255) NOT NULL, CHANGE type type VARCHAR(255) NOT NULL');
        $this->addSql('ALTER TABLE transaction_wallet DROP FOREIGN KEY `transaction_wallet_ibfk_1`');
        $this->addSql('ALTER TABLE transaction_wallet DROP FOREIGN KEY `transaction_wallet_ibfk_2`');
        $this->addSql('DROP INDEX idx_date_wallet ON transaction_wallet');
        $this->addSql('DROP INDEX idx_user_wallet ON transaction_wallet');
        $this->addSql('ALTER TABLE transaction_wallet DROP FOREIGN KEY `transaction_wallet_ibfk_2`');
        $this->addSql('ALTER TABLE transaction_wallet CHANGE nom_transaction nom_transaction VARCHAR(255) NOT NULL, CHANGE type type VARCHAR(255) NOT NULL, CHANGE date_transaction date_transaction DATETIME NOT NULL, CHANGE source source VARCHAR(255) DEFAULT NULL, CHANGE user_id user_id INT NOT NULL, CHANGE category_id category_id INT NOT NULL');
        $this->addSql('ALTER TABLE transaction_wallet ADD CONSTRAINT FK_A15E05F12469DE2 FOREIGN KEY (category_id) REFERENCES category (id_category)');
        $this->addSql('DROP INDEX idx_category ON transaction_wallet');
        $this->addSql('CREATE INDEX IDX_A15E05F12469DE2 ON transaction_wallet (category_id)');
        $this->addSql('ALTER TABLE transaction_wallet ADD CONSTRAINT `transaction_wallet_ibfk_2` FOREIGN KEY (category_id) REFERENCES category (id_category) ON DELETE SET NULL');
        $this->addSql('DROP INDEX idx_email ON users');
        $this->addSql('DROP INDEX idx_username ON users');
        $this->addSql('ALTER TABLE users CHANGE role role ENUM(\'ADMIN\', \'ENTREPRISE\', \'USER\')');
        $this->addSql('DROP INDEX username ON users');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_1483A5E9F85E0677 ON users (username)');
        $this->addSql('DROP INDEX email ON users');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_1483A5E9E7927C74 ON users (email)');
        $this->addSql('DROP INDEX idx_wishlist_user ON wishlist');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE user_biometrics (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, face_embedding TEXT CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, is_active TINYINT DEFAULT 1, INDEX user_id (user_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wallet_appel_offre_link (id BIGINT AUTO_INCREMENT NOT NULL, wallet_tx_id INT NOT NULL, appel_offre_id INT NOT NULL, relation ENUM(\'DON\', \'PAIEMENT\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_wallet_ao (appel_offre_id), INDEX wallet_tx_id (wallet_tx_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wallet_bourse_link (id BIGINT AUTO_INCREMENT NOT NULL, wallet_tx_id INT NOT NULL, bourse_tx_id INT NOT NULL, relation ENUM(\'DEBIT_ACHAT\', \'CREDIT_VENTE\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX bourse_tx_id (bourse_tx_id), UNIQUE INDEX uq_wallet_bourse (wallet_tx_id, bourse_tx_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wallet_formation_link (id BIGINT AUTO_INCREMENT NOT NULL, wallet_tx_id INT NOT NULL, enrollment_id INT NOT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX enrollment_id (enrollment_id), UNIQUE INDEX uq_wallet_form (wallet_tx_id, enrollment_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wallet_investment_link (id BIGINT AUTO_INCREMENT NOT NULL, wallet_tx_id INT NOT NULL, management_id BIGINT NOT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, UNIQUE INDEX uq_wallet_invest (wallet_tx_id, management_id), INDEX management_id (management_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('ALTER TABLE category CHANGE nom nom VARCHAR(100) NOT NULL, CHANGE type type ENUM(\'INCOME\', \'OUTCOME\') NOT NULL, CHANGE priorite priorite ENUM(\'HAUTE\', \'MOYENNE\', \'BASSE\') DEFAULT \'MOYENNE\'');
        $this->addSql('ALTER TABLE category ADD CONSTRAINT `fk_category_user` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('CREATE INDEX fk_category_user ON category (user_id)');
        $this->addSql('ALTER TABLE transaction_wallet DROP FOREIGN KEY FK_A15E05F12469DE2');
        $this->addSql('ALTER TABLE transaction_wallet DROP FOREIGN KEY FK_A15E05F12469DE2');
        $this->addSql('ALTER TABLE transaction_wallet CHANGE nom_transaction nom_transaction VARCHAR(100) DEFAULT NULL, CHANGE type type ENUM(\'INCOME\', \'OUTCOME\') NOT NULL, CHANGE date_transaction date_transaction DATE NOT NULL, CHANGE source source VARCHAR(100) DEFAULT \'MANUAL\', CHANGE user_id user_id INT DEFAULT NULL, CHANGE category_id category_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE transaction_wallet ADD CONSTRAINT `transaction_wallet_ibfk_1` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE transaction_wallet ADD CONSTRAINT `transaction_wallet_ibfk_2` FOREIGN KEY (category_id) REFERENCES category (id_category) ON DELETE SET NULL');
        $this->addSql('CREATE INDEX idx_date_wallet ON transaction_wallet (date_transaction)');
        $this->addSql('CREATE INDEX idx_user_wallet ON transaction_wallet (user_id)');
        $this->addSql('DROP INDEX idx_a15e05f12469de2 ON transaction_wallet');
        $this->addSql('CREATE INDEX idx_category ON transaction_wallet (category_id)');
        $this->addSql('ALTER TABLE transaction_wallet ADD CONSTRAINT FK_A15E05F12469DE2 FOREIGN KEY (category_id) REFERENCES category (id_category)');
        $this->addSql('ALTER TABLE users CHANGE role role ENUM(\'ADMIN\', \'ENTREPRISE\', \'USER\') DEFAULT \'USER\'');
        $this->addSql('CREATE INDEX idx_email ON users (email)');
        $this->addSql('CREATE INDEX idx_username ON users (username)');
        $this->addSql('DROP INDEX uniq_1483a5e9e7927c74 ON users');
        $this->addSql('CREATE UNIQUE INDEX email ON users (email)');
        $this->addSql('DROP INDEX uniq_1483a5e9f85e0677 ON users');
        $this->addSql('CREATE UNIQUE INDEX username ON users (username)');
        $this->addSql('CREATE INDEX idx_wishlist_user ON wishlist (user_id)');
    }
}
