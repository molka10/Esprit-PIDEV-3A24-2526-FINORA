<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260416120044 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE centre_formation (id INT AUTO_INCREMENT NOT NULL, nom VARCHAR(255) NOT NULL, adresse VARCHAR(500) NOT NULL, ville VARCHAR(50) NOT NULL, latitude NUMERIC(10, 7) NOT NULL, longitude NUMERIC(10, 7) NOT NULL, description LONGTEXT DEFAULT NULL, telephone VARCHAR(30) DEFAULT NULL, email VARCHAR(255) DEFAULT NULL, site_web VARCHAR(255) DEFAULT NULL, is_active TINYINT NOT NULL, created_at DATETIME NOT NULL, PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE messenger_messages (id BIGINT AUTO_INCREMENT NOT NULL, body LONGTEXT NOT NULL, headers LONGTEXT NOT NULL, queue_name VARCHAR(190) NOT NULL, created_at DATETIME NOT NULL, available_at DATETIME NOT NULL, delivered_at DATETIME DEFAULT NULL, INDEX IDX_75EA56E0FB7336F0E3BD61CE16BA31DBBF396750 (queue_name, available_at, delivered_at, id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE appel_offre DROP FOREIGN KEY `appel_offre_ibfk_1`');
        $this->addSql('ALTER TABLE candidature DROP FOREIGN KEY `candidature_ibfk_1`');
        $this->addSql('ALTER TABLE candidature DROP FOREIGN KEY `candidature_ibfk_2`');
        $this->addSql('ALTER TABLE category DROP FOREIGN KEY `fk_category_user`');
        $this->addSql('ALTER TABLE formation_enrollment DROP FOREIGN KEY `formation_enrollment_ibfk_1`');
        $this->addSql('ALTER TABLE formation_enrollment DROP FOREIGN KEY `formation_enrollment_ibfk_2`');
        $this->addSql('ALTER TABLE investment DROP FOREIGN KEY `investment_ibfk_1`');
        $this->addSql('ALTER TABLE investment_management DROP FOREIGN KEY `investment_management_ibfk_1`');
        $this->addSql('ALTER TABLE investment_management DROP FOREIGN KEY `investment_management_ibfk_2`');
        $this->addSql('ALTER TABLE password_resets DROP FOREIGN KEY `password_resets_ibfk_1`');
        $this->addSql('ALTER TABLE portefeuille DROP FOREIGN KEY `portefeuille_ibfk_1`');
        $this->addSql('ALTER TABLE portefeuille DROP FOREIGN KEY `portefeuille_ibfk_2`');
        $this->addSql('ALTER TABLE transaction_wallet DROP FOREIGN KEY `transaction_wallet_ibfk_1`');
        $this->addSql('ALTER TABLE transaction_wallet DROP FOREIGN KEY `transaction_wallet_ibfk_2`');
        $this->addSql('ALTER TABLE wallet_appel_offre_link DROP FOREIGN KEY `wallet_appel_offre_link_ibfk_1`');
        $this->addSql('ALTER TABLE wallet_appel_offre_link DROP FOREIGN KEY `wallet_appel_offre_link_ibfk_2`');
        $this->addSql('ALTER TABLE wallet_bourse_link DROP FOREIGN KEY `wallet_bourse_link_ibfk_1`');
        $this->addSql('ALTER TABLE wallet_bourse_link DROP FOREIGN KEY `wallet_bourse_link_ibfk_2`');
        $this->addSql('ALTER TABLE wallet_formation_link DROP FOREIGN KEY `wallet_formation_link_ibfk_1`');
        $this->addSql('ALTER TABLE wallet_formation_link DROP FOREIGN KEY `wallet_formation_link_ibfk_2`');
        $this->addSql('ALTER TABLE wallet_investment_link DROP FOREIGN KEY `wallet_investment_link_ibfk_1`');
        $this->addSql('ALTER TABLE wallet_investment_link DROP FOREIGN KEY `wallet_investment_link_ibfk_2`');
        $this->addSql('ALTER TABLE wishlist DROP FOREIGN KEY `wishlist_ibfk_1`');
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
        $this->addSql('ALTER TABLE action DROP FOREIGN KEY `action_ibfk_1`');
        $this->addSql('DROP INDEX idx_symbole ON action');
        $this->addSql('ALTER TABLE action DROP FOREIGN KEY `action_ibfk_1`');
        $this->addSql('ALTER TABLE action CHANGE statut statut VARCHAR(20) NOT NULL, CHANGE date_ajout date_ajout DATETIME NOT NULL');
        $this->addSql('ALTER TABLE action ADD CONSTRAINT FK_47CC8C92FBF509F1 FOREIGN KEY (id_bourse) REFERENCES bourse (id_bourse)');
        $this->addSql('DROP INDEX symbole ON action');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_47CC8C922B57F8D4 ON action (symbole)');
        $this->addSql('DROP INDEX idx_bourse ON action');
        $this->addSql('CREATE INDEX IDX_47CC8C92FBF509F1 ON action (id_bourse)');
        $this->addSql('ALTER TABLE action ADD CONSTRAINT `action_ibfk_1` FOREIGN KEY (id_bourse) REFERENCES bourse (id_bourse) ON DELETE CASCADE');
        $this->addSql('DROP INDEX nom_bourse ON bourse');
        $this->addSql('DROP INDEX idx_nom_bourse ON bourse');
        $this->addSql('ALTER TABLE bourse CHANGE devise devise VARCHAR(3) NOT NULL, CHANGE statut statut VARCHAR(20) NOT NULL, CHANGE date_creation date_creation DATETIME NOT NULL');
        $this->addSql('ALTER TABLE formation DROP FOREIGN KEY `formation_ibfk_1`');
        $this->addSql('DROP INDEX idx_form_creator ON formation');
        $this->addSql('DROP INDEX idx_form_published ON formation');
        $this->addSql('ALTER TABLE formation ADD pourquoi_acheter LONGTEXT DEFAULT NULL, ADD prix DOUBLE PRECISION DEFAULT NULL, ADD rating DOUBLE PRECISION DEFAULT NULL, ADD updated_at DATETIME DEFAULT NULL, DROP created_by_user_id, DROP created_at, CHANGE titre titre VARCHAR(255) NOT NULL, CHANGE description description LONGTEXT DEFAULT NULL, CHANGE categorie categorie VARCHAR(255) NOT NULL, CHANGE niveau niveau VARCHAR(255) NOT NULL, CHANGE is_published is_published INT NOT NULL');
        $this->addSql('ALTER TABLE lesson DROP FOREIGN KEY `lesson_ibfk_1`');
        $this->addSql('ALTER TABLE lesson DROP FOREIGN KEY `lesson_ibfk_1`');
        $this->addSql('ALTER TABLE lesson ADD updated_at DATETIME DEFAULT NULL, CHANGE titre titre VARCHAR(255) NOT NULL, CHANGE contenu contenu LONGTEXT DEFAULT NULL, CHANGE ordre ordre INT NOT NULL, CHANGE duree_minutes duree_minutes INT NOT NULL');
        $this->addSql('ALTER TABLE lesson ADD CONSTRAINT FK_F87474F35200282E FOREIGN KEY (formation_id) REFERENCES formation (id)');
        $this->addSql('DROP INDEX idx_lesson_form ON lesson');
        $this->addSql('CREATE INDEX IDX_F87474F35200282E ON lesson (formation_id)');
        $this->addSql('ALTER TABLE lesson ADD CONSTRAINT `lesson_ibfk_1` FOREIGN KEY (formation_id) REFERENCES formation (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE quiz_result DROP FOREIGN KEY `quiz_result_ibfk_1`');
        $this->addSql('ALTER TABLE quiz_result DROP FOREIGN KEY `quiz_result_ibfk_2`');
        $this->addSql('DROP INDEX idx_quiz_lesson ON quiz_result');
        $this->addSql('DROP INDEX idx_quiz_user ON quiz_result');
        $this->addSql('ALTER TABLE quiz_result ADD fraud_suspected INT DEFAULT 0 NOT NULL, ADD fraud_explanation LONGTEXT DEFAULT NULL, DROP user_id, CHANGE student_name student_name VARCHAR(255) NOT NULL, CHANGE passed passed INT NOT NULL, CHANGE taken_at taken_at DATETIME DEFAULT NULL');
        $this->addSql('ALTER TABLE training_center CHANGE name name VARCHAR(255) NOT NULL, CHANGE lat lat VARCHAR(255) NOT NULL, CHANGE lng lng VARCHAR(255) NOT NULL');
        $this->addSql('ALTER TABLE transaction_bourse DROP FOREIGN KEY `transaction_bourse_ibfk_1`');
        $this->addSql('ALTER TABLE transaction_bourse DROP FOREIGN KEY `transaction_bourse_ibfk_2`');
        $this->addSql('DROP INDEX idx_date ON transaction_bourse');
        $this->addSql('ALTER TABLE transaction_bourse DROP FOREIGN KEY `transaction_bourse_ibfk_1`');
        $this->addSql('ALTER TABLE transaction_bourse DROP FOREIGN KEY `transaction_bourse_ibfk_2`');
        $this->addSql('ALTER TABLE transaction_bourse CHANGE type_transaction type_transaction VARCHAR(20) NOT NULL, CHANGE commission commission DOUBLE PRECISION NOT NULL, CHANGE acteur_role acteur_role VARCHAR(50) DEFAULT NULL, CHANGE acteur_label acteur_label VARCHAR(100) DEFAULT NULL, CHANGE date_transaction date_transaction DATETIME NOT NULL');
        $this->addSql('ALTER TABLE transaction_bourse ADD CONSTRAINT FK_ABBFCE5C61FB397F FOREIGN KEY (id_action) REFERENCES action (id_action)');
        $this->addSql('ALTER TABLE transaction_bourse ADD CONSTRAINT FK_ABBFCE5C6B3CA4B FOREIGN KEY (id_user) REFERENCES users (id)');
        $this->addSql('DROP INDEX idx_action ON transaction_bourse');
        $this->addSql('CREATE INDEX IDX_ABBFCE5C61FB397F ON transaction_bourse (id_action)');
        $this->addSql('DROP INDEX idx_user ON transaction_bourse');
        $this->addSql('CREATE INDEX IDX_ABBFCE5C6B3CA4B ON transaction_bourse (id_user)');
        $this->addSql('ALTER TABLE transaction_bourse ADD CONSTRAINT `transaction_bourse_ibfk_1` FOREIGN KEY (id_action) REFERENCES action (id_action) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE transaction_bourse ADD CONSTRAINT `transaction_bourse_ibfk_2` FOREIGN KEY (id_user) REFERENCES users (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE user_biometrics DROP INDEX user_id, ADD UNIQUE INDEX UNIQ_D7128AA6A76ED395 (user_id)');
        $this->addSql('ALTER TABLE user_biometrics DROP FOREIGN KEY `user_biometrics_ibfk_1`');
        $this->addSql('ALTER TABLE user_biometrics CHANGE face_embedding face_embedding LONGTEXT DEFAULT NULL, CHANGE created_at created_at DATETIME NOT NULL, CHANGE is_active is_active TINYINT NOT NULL');
        $this->addSql('ALTER TABLE user_biometrics ADD CONSTRAINT FK_D7128AA6A76ED395 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('DROP INDEX idx_email ON users');
        $this->addSql('DROP INDEX idx_username ON users');
        $this->addSql('ALTER TABLE users CHANGE username username VARCHAR(100) NOT NULL, CHANGE email email VARCHAR(180) NOT NULL, CHANGE mot_de_passe mot_de_passe VARCHAR(255) DEFAULT NULL, CHANGE role role VARCHAR(20) NOT NULL, CHANGE address address VARCHAR(100) DEFAULT NULL, CHANGE created_at created_at DATETIME NOT NULL, CHANGE is_verified is_verified TINYINT NOT NULL');
        $this->addSql('DROP INDEX username ON users');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_1483A5E9F85E0677 ON users (username)');
        $this->addSql('DROP INDEX email ON users');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_1483A5E9E7927C74 ON users (email)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE appel_offre (appel_offre_id INT AUTO_INCREMENT NOT NULL, created_by_user_id INT DEFAULT NULL, titre VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, description TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, categorie VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, type ENUM(\'achat\', \'partenariat\', \'don\') CHARACTER SET utf8mb4 DEFAULT \'achat\' COLLATE `utf8mb4_unicode_ci`, budget_min DOUBLE PRECISION DEFAULT NULL, budget_max DOUBLE PRECISION DEFAULT NULL, devise VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, date_limite DATE DEFAULT NULL, statut ENUM(\'draft\', \'published\', \'closed\') CHARACTER SET utf8mb4 DEFAULT \'draft\' COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_ao_creator (created_by_user_id), INDEX idx_ao_statut (statut), PRIMARY KEY (appel_offre_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE candidature (candidature_id INT AUTO_INCREMENT NOT NULL, appel_offre_id INT NOT NULL, user_id INT DEFAULT NULL, nom_candidat VARCHAR(150) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, email_candidat VARCHAR(150) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, montant_propose DOUBLE PRECISION DEFAULT NULL, message TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, statut ENUM(\'submitted\', \'accepted\', \'rejected\') CHARACTER SET utf8mb4 DEFAULT \'submitted\' COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_cand_ao (appel_offre_id), INDEX idx_cand_user (user_id), PRIMARY KEY (candidature_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE category (id_category INT AUTO_INCREMENT NOT NULL, nom VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, priorite ENUM(\'HAUTE\', \'MOYENNE\', \'BASSE\') CHARACTER SET utf8mb4 DEFAULT \'MOYENNE\' COLLATE `utf8mb4_unicode_ci`, type ENUM(\'INCOME\', \'OUTCOME\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, user_id INT DEFAULT NULL, INDEX fk_category_user (user_id), PRIMARY KEY (id_category)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE commission (id_commission INT AUTO_INCREMENT NOT NULL, nom VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, symbole VARCHAR(10) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, type_transaction ENUM(\'ACHAT\', \'VENTE\', \'LES_DEUX\') CHARACTER SET utf8mb4 DEFAULT \'LES_DEUX\' COLLATE `utf8mb4_unicode_ci`, taux_pourcentage DOUBLE PRECISION NOT NULL, date_creation DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, date_modification DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, active TINYINT DEFAULT 1, INDEX idx_commission_symbole_active_type (symbole, active, type_transaction, date_modification), PRIMARY KEY (id_commission)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE formation_enrollment (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, formation_id INT NOT NULL, statut ENUM(\'PENDING\', \'ACTIVE\', \'COMPLETED\', \'CANCELLED\') CHARACTER SET utf8mb4 DEFAULT \'PENDING\' COLLATE `utf8mb4_unicode_ci`, enrolled_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, UNIQUE INDEX uq_enroll (user_id, formation_id), INDEX formation_id (formation_id), INDEX IDX_237404D8A76ED395 (user_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE investment (investment_id BIGINT AUTO_INCREMENT NOT NULL, created_by_user_id INT DEFAULT NULL, name VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, category VARCHAR(30) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, location VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, estimated_value NUMERIC(12, 2) NOT NULL, risk_level ENUM(\'LOW\', \'MEDIUM\', \'HIGH\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, image_url VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, description TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, status ENUM(\'ACTIVE\', \'CLOSED\') CHARACTER SET utf8mb4 DEFAULT \'ACTIVE\' COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_inv_creator (created_by_user_id), PRIMARY KEY (investment_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE investment_management (management_id BIGINT AUTO_INCREMENT NOT NULL, investment_id BIGINT NOT NULL, investor_user_id INT NOT NULL, investment_type VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, amount_invested NUMERIC(12, 2) NOT NULL, ownership_percentage NUMERIC(5, 2) NOT NULL, start_date DATE NOT NULL, status ENUM(\'ACTIVE\', \'CLOSED\') CHARACTER SET utf8mb4 DEFAULT \'ACTIVE\' COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_mgmt_investment (investment_id), INDEX idx_mgmt_investor (investor_user_id), PRIMARY KEY (management_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE password_resets (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, code VARCHAR(10) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, expires_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, used TINYINT DEFAULT 0, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_user_id (user_id), INDEX idx_code (code), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE portefeuille (id_portefeuille INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, id_action INT NOT NULL, quantite INT DEFAULT 0, INDEX id_action (id_action), UNIQUE INDEX uq_user_action (user_id, id_action), INDEX IDX_2955FFFEA76ED395 (user_id), PRIMARY KEY (id_portefeuille)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE transaction_wallet (id_transaction INT AUTO_INCREMENT NOT NULL, nom_transaction VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, type ENUM(\'INCOME\', \'OUTCOME\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, montant DOUBLE PRECISION NOT NULL, date_transaction DATE NOT NULL, source VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT \'MANUAL\' COLLATE `utf8mb4_unicode_ci`, user_id INT DEFAULT NULL, category_id INT DEFAULT NULL, INDEX idx_user_wallet (user_id), INDEX idx_category (category_id), INDEX idx_date_wallet (date_transaction), PRIMARY KEY (id_transaction)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wallet_appel_offre_link (id BIGINT AUTO_INCREMENT NOT NULL, wallet_tx_id INT NOT NULL, appel_offre_id INT NOT NULL, relation ENUM(\'DON\', \'PAIEMENT\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_wallet_ao (appel_offre_id), INDEX wallet_tx_id (wallet_tx_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wallet_bourse_link (id BIGINT AUTO_INCREMENT NOT NULL, wallet_tx_id INT NOT NULL, bourse_tx_id INT NOT NULL, relation ENUM(\'DEBIT_ACHAT\', \'CREDIT_VENTE\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX bourse_tx_id (bourse_tx_id), UNIQUE INDEX uq_wallet_bourse (wallet_tx_id, bourse_tx_id), INDEX IDX_4A6D67948DFC4339 (wallet_tx_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wallet_formation_link (id BIGINT AUTO_INCREMENT NOT NULL, wallet_tx_id INT NOT NULL, enrollment_id INT NOT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX enrollment_id (enrollment_id), UNIQUE INDEX uq_wallet_form (wallet_tx_id, enrollment_id), INDEX IDX_DCFFAF898DFC4339 (wallet_tx_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wallet_investment_link (id BIGINT AUTO_INCREMENT NOT NULL, wallet_tx_id INT NOT NULL, management_id BIGINT NOT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, UNIQUE INDEX uq_wallet_invest (wallet_tx_id, management_id), INDEX management_id (management_id), INDEX IDX_6D52B86A8DFC4339 (wallet_tx_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wishlist (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, name VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, price DOUBLE PRECISION NOT NULL, INDEX idx_wishlist_user (user_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('ALTER TABLE appel_offre ADD CONSTRAINT `appel_offre_ibfk_1` FOREIGN KEY (created_by_user_id) REFERENCES users (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE candidature ADD CONSTRAINT `candidature_ibfk_1` FOREIGN KEY (appel_offre_id) REFERENCES appel_offre (appel_offre_id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE candidature ADD CONSTRAINT `candidature_ibfk_2` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE category ADD CONSTRAINT `fk_category_user` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE formation_enrollment ADD CONSTRAINT `formation_enrollment_ibfk_1` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE formation_enrollment ADD CONSTRAINT `formation_enrollment_ibfk_2` FOREIGN KEY (formation_id) REFERENCES formation (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE investment ADD CONSTRAINT `investment_ibfk_1` FOREIGN KEY (created_by_user_id) REFERENCES users (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE investment_management ADD CONSTRAINT `investment_management_ibfk_1` FOREIGN KEY (investment_id) REFERENCES investment (investment_id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE investment_management ADD CONSTRAINT `investment_management_ibfk_2` FOREIGN KEY (investor_user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE password_resets ADD CONSTRAINT `password_resets_ibfk_1` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE portefeuille ADD CONSTRAINT `portefeuille_ibfk_1` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE portefeuille ADD CONSTRAINT `portefeuille_ibfk_2` FOREIGN KEY (id_action) REFERENCES action (id_action) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE transaction_wallet ADD CONSTRAINT `transaction_wallet_ibfk_1` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE transaction_wallet ADD CONSTRAINT `transaction_wallet_ibfk_2` FOREIGN KEY (category_id) REFERENCES category (id_category) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE wallet_appel_offre_link ADD CONSTRAINT `wallet_appel_offre_link_ibfk_1` FOREIGN KEY (wallet_tx_id) REFERENCES transaction_wallet (id_transaction) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE wallet_appel_offre_link ADD CONSTRAINT `wallet_appel_offre_link_ibfk_2` FOREIGN KEY (appel_offre_id) REFERENCES appel_offre (appel_offre_id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE wallet_bourse_link ADD CONSTRAINT `wallet_bourse_link_ibfk_1` FOREIGN KEY (wallet_tx_id) REFERENCES transaction_wallet (id_transaction) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE wallet_bourse_link ADD CONSTRAINT `wallet_bourse_link_ibfk_2` FOREIGN KEY (bourse_tx_id) REFERENCES transaction_bourse (id_transaction) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE wallet_formation_link ADD CONSTRAINT `wallet_formation_link_ibfk_1` FOREIGN KEY (wallet_tx_id) REFERENCES transaction_wallet (id_transaction) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE wallet_formation_link ADD CONSTRAINT `wallet_formation_link_ibfk_2` FOREIGN KEY (enrollment_id) REFERENCES formation_enrollment (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE wallet_investment_link ADD CONSTRAINT `wallet_investment_link_ibfk_1` FOREIGN KEY (wallet_tx_id) REFERENCES transaction_wallet (id_transaction) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE wallet_investment_link ADD CONSTRAINT `wallet_investment_link_ibfk_2` FOREIGN KEY (management_id) REFERENCES investment_management (management_id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE wishlist ADD CONSTRAINT `wishlist_ibfk_1` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('DROP TABLE centre_formation');
        $this->addSql('DROP TABLE messenger_messages');
        $this->addSql('ALTER TABLE action DROP FOREIGN KEY FK_47CC8C92FBF509F1');
        $this->addSql('ALTER TABLE action DROP FOREIGN KEY FK_47CC8C92FBF509F1');
        $this->addSql('ALTER TABLE action CHANGE statut statut ENUM(\'DISPONIBLE\', \'INDISPONIBLE\') DEFAULT \'DISPONIBLE\', CHANGE date_ajout date_ajout DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL');
        $this->addSql('ALTER TABLE action ADD CONSTRAINT `action_ibfk_1` FOREIGN KEY (id_bourse) REFERENCES bourse (id_bourse) ON DELETE CASCADE');
        $this->addSql('CREATE INDEX idx_symbole ON action (symbole)');
        $this->addSql('DROP INDEX idx_47cc8c92fbf509f1 ON action');
        $this->addSql('CREATE INDEX idx_bourse ON action (id_bourse)');
        $this->addSql('DROP INDEX uniq_47cc8c922b57f8d4 ON action');
        $this->addSql('CREATE UNIQUE INDEX symbole ON action (symbole)');
        $this->addSql('ALTER TABLE action ADD CONSTRAINT FK_47CC8C92FBF509F1 FOREIGN KEY (id_bourse) REFERENCES bourse (id_bourse)');
        $this->addSql('ALTER TABLE bourse CHANGE devise devise VARCHAR(10) NOT NULL, CHANGE statut statut ENUM(\'ACTIVE\', \'INACTIVE\') DEFAULT \'ACTIVE\', CHANGE date_creation date_creation DATETIME DEFAULT CURRENT_TIMESTAMP');
        $this->addSql('CREATE UNIQUE INDEX nom_bourse ON bourse (nom_bourse)');
        $this->addSql('CREATE INDEX idx_nom_bourse ON bourse (nom_bourse)');
        $this->addSql('ALTER TABLE formation ADD created_by_user_id INT DEFAULT NULL, ADD created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, DROP pourquoi_acheter, DROP prix, DROP rating, DROP updated_at, CHANGE titre titre VARCHAR(120) NOT NULL, CHANGE description description TEXT DEFAULT NULL, CHANGE categorie categorie VARCHAR(80) NOT NULL, CHANGE niveau niveau ENUM(\'Débutant\', \'Intermédiaire\', \'Avancé\') NOT NULL, CHANGE is_published is_published TINYINT DEFAULT 0');
        $this->addSql('ALTER TABLE formation ADD CONSTRAINT `formation_ibfk_1` FOREIGN KEY (created_by_user_id) REFERENCES users (id) ON DELETE SET NULL');
        $this->addSql('CREATE INDEX idx_form_creator ON formation (created_by_user_id)');
        $this->addSql('CREATE INDEX idx_form_published ON formation (is_published)');
        $this->addSql('ALTER TABLE lesson DROP FOREIGN KEY FK_F87474F35200282E');
        $this->addSql('ALTER TABLE lesson DROP FOREIGN KEY FK_F87474F35200282E');
        $this->addSql('ALTER TABLE lesson DROP updated_at, CHANGE titre titre VARCHAR(120) NOT NULL, CHANGE contenu contenu TEXT DEFAULT NULL, CHANGE ordre ordre INT DEFAULT 1, CHANGE duree_minutes duree_minutes INT DEFAULT 0');
        $this->addSql('ALTER TABLE lesson ADD CONSTRAINT `lesson_ibfk_1` FOREIGN KEY (formation_id) REFERENCES formation (id) ON DELETE CASCADE');
        $this->addSql('DROP INDEX idx_f87474f35200282e ON lesson');
        $this->addSql('CREATE INDEX idx_lesson_form ON lesson (formation_id)');
        $this->addSql('ALTER TABLE lesson ADD CONSTRAINT FK_F87474F35200282E FOREIGN KEY (formation_id) REFERENCES formation (id)');
        $this->addSql('ALTER TABLE quiz_result ADD user_id INT DEFAULT NULL, DROP fraud_suspected, DROP fraud_explanation, CHANGE student_name student_name VARCHAR(100) NOT NULL, CHANGE passed passed TINYINT NOT NULL, CHANGE taken_at taken_at DATETIME DEFAULT CURRENT_TIMESTAMP');
        $this->addSql('ALTER TABLE quiz_result ADD CONSTRAINT `quiz_result_ibfk_1` FOREIGN KEY (lesson_id) REFERENCES lesson (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE quiz_result ADD CONSTRAINT `quiz_result_ibfk_2` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL');
        $this->addSql('CREATE INDEX idx_quiz_lesson ON quiz_result (lesson_id)');
        $this->addSql('CREATE INDEX idx_quiz_user ON quiz_result (user_id)');
        $this->addSql('ALTER TABLE training_center CHANGE name name VARCHAR(120) NOT NULL, CHANGE lat lat DOUBLE PRECISION NOT NULL, CHANGE lng lng DOUBLE PRECISION NOT NULL');
        $this->addSql('ALTER TABLE transaction_bourse DROP FOREIGN KEY FK_ABBFCE5C61FB397F');
        $this->addSql('ALTER TABLE transaction_bourse DROP FOREIGN KEY FK_ABBFCE5C6B3CA4B');
        $this->addSql('ALTER TABLE transaction_bourse DROP FOREIGN KEY FK_ABBFCE5C61FB397F');
        $this->addSql('ALTER TABLE transaction_bourse DROP FOREIGN KEY FK_ABBFCE5C6B3CA4B');
        $this->addSql('ALTER TABLE transaction_bourse CHANGE type_transaction type_transaction ENUM(\'ACHAT\', \'VENTE\') NOT NULL, CHANGE commission commission DOUBLE PRECISION DEFAULT \'0\', CHANGE date_transaction date_transaction DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, CHANGE acteur_role acteur_role ENUM(\'INVESTISSEUR\', \'ENTREPRISE\', \'ADMIN\') DEFAULT \'INVESTISSEUR\', CHANGE acteur_label acteur_label VARCHAR(120) DEFAULT NULL');
        $this->addSql('ALTER TABLE transaction_bourse ADD CONSTRAINT `transaction_bourse_ibfk_1` FOREIGN KEY (id_action) REFERENCES action (id_action) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE transaction_bourse ADD CONSTRAINT `transaction_bourse_ibfk_2` FOREIGN KEY (id_user) REFERENCES users (id) ON DELETE SET NULL');
        $this->addSql('CREATE INDEX idx_date ON transaction_bourse (date_transaction)');
        $this->addSql('DROP INDEX idx_abbfce5c6b3ca4b ON transaction_bourse');
        $this->addSql('CREATE INDEX idx_user ON transaction_bourse (id_user)');
        $this->addSql('DROP INDEX idx_abbfce5c61fb397f ON transaction_bourse');
        $this->addSql('CREATE INDEX idx_action ON transaction_bourse (id_action)');
        $this->addSql('ALTER TABLE transaction_bourse ADD CONSTRAINT FK_ABBFCE5C61FB397F FOREIGN KEY (id_action) REFERENCES action (id_action)');
        $this->addSql('ALTER TABLE transaction_bourse ADD CONSTRAINT FK_ABBFCE5C6B3CA4B FOREIGN KEY (id_user) REFERENCES users (id)');
        $this->addSql('ALTER TABLE users CHANGE username username VARCHAR(50) NOT NULL, CHANGE email email VARCHAR(150) NOT NULL, CHANGE mot_de_passe mot_de_passe VARCHAR(255) NOT NULL, CHANGE role role ENUM(\'ADMIN\', \'ENTREPRISE\', \'USER\') DEFAULT \'USER\', CHANGE address address VARCHAR(255) DEFAULT NULL, CHANGE created_at created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, CHANGE is_verified is_verified TINYINT DEFAULT 0 NOT NULL');
        $this->addSql('CREATE INDEX idx_email ON users (email)');
        $this->addSql('CREATE INDEX idx_username ON users (username)');
        $this->addSql('DROP INDEX uniq_1483a5e9f85e0677 ON users');
        $this->addSql('CREATE UNIQUE INDEX username ON users (username)');
        $this->addSql('DROP INDEX uniq_1483a5e9e7927c74 ON users');
        $this->addSql('CREATE UNIQUE INDEX email ON users (email)');
        $this->addSql('ALTER TABLE user_biometrics DROP INDEX UNIQ_D7128AA6A76ED395, ADD INDEX user_id (user_id)');
        $this->addSql('ALTER TABLE user_biometrics DROP FOREIGN KEY FK_D7128AA6A76ED395');
        $this->addSql('ALTER TABLE user_biometrics CHANGE face_embedding face_embedding TEXT NOT NULL, CHANGE is_active is_active TINYINT DEFAULT 1, CHANGE created_at created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL');
        $this->addSql('ALTER TABLE user_biometrics ADD CONSTRAINT `user_biometrics_ibfk_1` FOREIGN KEY (user_id) REFERENCES users (id)');
    }
}
