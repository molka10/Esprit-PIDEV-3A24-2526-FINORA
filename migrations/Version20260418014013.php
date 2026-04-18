<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260418014013 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE user_formation_wishlist (user_id INT NOT NULL, formation_id INT NOT NULL, INDEX IDX_F7379B3BA76ED395 (user_id), INDEX IDX_F7379B3B5200282E (formation_id), PRIMARY KEY (user_id, formation_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE user_formation_wishlist ADD CONSTRAINT FK_F7379B3BA76ED395 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE user_formation_wishlist ADD CONSTRAINT FK_F7379B3B5200282E FOREIGN KEY (formation_id) REFERENCES formation (id) ON DELETE CASCADE');
        $this->addSql('DROP TABLE appel_offre');
        $this->addSql('DROP TABLE candidature');
        $this->addSql('DROP TABLE category');
        $this->addSql('DROP TABLE commission');
        $this->addSql('DROP TABLE formation_enrollment');
        $this->addSql('DROP TABLE investment');
        $this->addSql('DROP TABLE investment_management');
        $this->addSql('DROP TABLE password_resets');
        $this->addSql('DROP TABLE portefeuille');
        $this->addSql('DROP TABLE transaction_wallet');
        $this->addSql('DROP TABLE wallet_appel_offre_link');
        $this->addSql('DROP TABLE wallet_bourse_link');
        $this->addSql('DROP TABLE wallet_formation_link');
        $this->addSql('DROP TABLE wallet_investment_link');
        $this->addSql('DROP TABLE wishlist');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE appel_offre (appel_offre_id INT AUTO_INCREMENT NOT NULL, created_by_user_id INT DEFAULT NULL, titre VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, description TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, categorie VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, type ENUM(\'achat\', \'partenariat\', \'don\') CHARACTER SET utf8mb4 DEFAULT \'achat\' COLLATE `utf8mb4_unicode_ci`, budget_min DOUBLE PRECISION DEFAULT NULL, budget_max DOUBLE PRECISION DEFAULT NULL, devise VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, date_limite DATE DEFAULT NULL, statut ENUM(\'draft\', \'published\', \'closed\') CHARACTER SET utf8mb4 DEFAULT \'draft\' COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_ao_statut (statut), INDEX idx_ao_creator (created_by_user_id), PRIMARY KEY (appel_offre_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE candidature (candidature_id INT AUTO_INCREMENT NOT NULL, appel_offre_id INT NOT NULL, user_id INT DEFAULT NULL, nom_candidat VARCHAR(150) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, email_candidat VARCHAR(150) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, montant_propose DOUBLE PRECISION DEFAULT NULL, message TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, statut ENUM(\'submitted\', \'accepted\', \'rejected\') CHARACTER SET utf8mb4 DEFAULT \'submitted\' COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_cand_ao (appel_offre_id), INDEX idx_cand_user (user_id), PRIMARY KEY (candidature_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE category (id_category INT AUTO_INCREMENT NOT NULL, nom VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, priorite ENUM(\'HAUTE\', \'MOYENNE\', \'BASSE\') CHARACTER SET utf8mb4 DEFAULT \'MOYENNE\' COLLATE `utf8mb4_unicode_ci`, type ENUM(\'INCOME\', \'OUTCOME\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, user_id INT DEFAULT NULL, INDEX fk_category_user (user_id), PRIMARY KEY (id_category)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE commission (id_commission INT AUTO_INCREMENT NOT NULL, nom VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, symbole VARCHAR(10) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, type_transaction ENUM(\'ACHAT\', \'VENTE\', \'LES_DEUX\') CHARACTER SET utf8mb4 DEFAULT \'LES_DEUX\' COLLATE `utf8mb4_unicode_ci`, taux_pourcentage DOUBLE PRECISION NOT NULL, date_creation DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, date_modification DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, active TINYINT DEFAULT 1, INDEX idx_commission_symbole_active_type (symbole, active, type_transaction, date_modification), PRIMARY KEY (id_commission)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE formation_enrollment (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, formation_id INT NOT NULL, statut ENUM(\'PENDING\', \'ACTIVE\', \'COMPLETED\', \'CANCELLED\') CHARACTER SET utf8mb4 DEFAULT \'PENDING\' COLLATE `utf8mb4_unicode_ci`, enrolled_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, UNIQUE INDEX uq_enroll (user_id, formation_id), INDEX formation_id (formation_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE investment (investment_id BIGINT AUTO_INCREMENT NOT NULL, created_by_user_id INT DEFAULT NULL, name VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, category VARCHAR(30) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, location VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, estimated_value NUMERIC(12, 2) NOT NULL, risk_level ENUM(\'LOW\', \'MEDIUM\', \'HIGH\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, image_url VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, description TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, status ENUM(\'ACTIVE\', \'CLOSED\') CHARACTER SET utf8mb4 DEFAULT \'ACTIVE\' COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_inv_creator (created_by_user_id), PRIMARY KEY (investment_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE investment_management (management_id BIGINT AUTO_INCREMENT NOT NULL, investment_id BIGINT NOT NULL, investor_user_id INT NOT NULL, investment_type VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, amount_invested NUMERIC(12, 2) NOT NULL, ownership_percentage NUMERIC(5, 2) NOT NULL, start_date DATE NOT NULL, status ENUM(\'ACTIVE\', \'CLOSED\') CHARACTER SET utf8mb4 DEFAULT \'ACTIVE\' COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_mgmt_investment (investment_id), INDEX idx_mgmt_investor (investor_user_id), PRIMARY KEY (management_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE password_resets (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, code VARCHAR(10) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, expires_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, used TINYINT DEFAULT 0, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_user_id (user_id), INDEX idx_code (code), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE portefeuille (id_portefeuille INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, id_action INT NOT NULL, quantite INT DEFAULT 0, UNIQUE INDEX uq_user_action (user_id, id_action), INDEX id_action (id_action), PRIMARY KEY (id_portefeuille)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE transaction_wallet (id_transaction INT AUTO_INCREMENT NOT NULL, nom_transaction VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, type ENUM(\'INCOME\', \'OUTCOME\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, montant DOUBLE PRECISION NOT NULL, date_transaction DATE NOT NULL, source VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT \'MANUAL\' COLLATE `utf8mb4_unicode_ci`, user_id INT DEFAULT NULL, category_id INT DEFAULT NULL, INDEX idx_user_wallet (user_id), INDEX idx_category (category_id), INDEX idx_date_wallet (date_transaction), PRIMARY KEY (id_transaction)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wallet_appel_offre_link (id BIGINT AUTO_INCREMENT NOT NULL, wallet_tx_id INT NOT NULL, appel_offre_id INT NOT NULL, relation ENUM(\'DON\', \'PAIEMENT\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX wallet_tx_id (wallet_tx_id), INDEX idx_wallet_ao (appel_offre_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wallet_bourse_link (id BIGINT AUTO_INCREMENT NOT NULL, wallet_tx_id INT NOT NULL, bourse_tx_id INT NOT NULL, relation ENUM(\'DEBIT_ACHAT\', \'CREDIT_VENTE\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX bourse_tx_id (bourse_tx_id), UNIQUE INDEX uq_wallet_bourse (wallet_tx_id, bourse_tx_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wallet_formation_link (id BIGINT AUTO_INCREMENT NOT NULL, wallet_tx_id INT NOT NULL, enrollment_id INT NOT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, UNIQUE INDEX uq_wallet_form (wallet_tx_id, enrollment_id), INDEX enrollment_id (enrollment_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wallet_investment_link (id BIGINT AUTO_INCREMENT NOT NULL, wallet_tx_id INT NOT NULL, management_id BIGINT NOT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, UNIQUE INDEX uq_wallet_invest (wallet_tx_id, management_id), INDEX management_id (management_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wishlist (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, name VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, price DOUBLE PRECISION NOT NULL, INDEX idx_wishlist_user (user_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('ALTER TABLE user_formation_wishlist DROP FOREIGN KEY FK_F7379B3BA76ED395');
        $this->addSql('ALTER TABLE user_formation_wishlist DROP FOREIGN KEY FK_F7379B3B5200282E');
        $this->addSql('DROP TABLE user_formation_wishlist');
    }
}
