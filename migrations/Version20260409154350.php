<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260409154350 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE messenger_messages (id BIGINT AUTO_INCREMENT NOT NULL, body LONGTEXT NOT NULL, headers LONGTEXT NOT NULL, queue_name VARCHAR(190) NOT NULL, created_at DATETIME NOT NULL, available_at DATETIME NOT NULL, delivered_at DATETIME DEFAULT NULL, INDEX IDX_75EA56E0FB7336F0E3BD61CE16BA31DBBF396750 (queue_name, available_at, delivered_at, id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE action DROP FOREIGN KEY `action_ibfk_1`');
        $this->addSql('ALTER TABLE appel_offre DROP FOREIGN KEY `appel_offre_ibfk_1`');
        $this->addSql('ALTER TABLE candidature DROP FOREIGN KEY `candidature_ibfk_1`');
        $this->addSql('ALTER TABLE candidature DROP FOREIGN KEY `candidature_ibfk_2`');
        $this->addSql('ALTER TABLE formation DROP FOREIGN KEY `formation_ibfk_1`');
        $this->addSql('ALTER TABLE formation_enrollment DROP FOREIGN KEY `formation_enrollment_ibfk_1`');
        $this->addSql('ALTER TABLE formation_enrollment DROP FOREIGN KEY `formation_enrollment_ibfk_2`');
        $this->addSql('ALTER TABLE investment DROP FOREIGN KEY `investment_ibfk_1`');
        $this->addSql('ALTER TABLE investment_management DROP FOREIGN KEY `investment_management_ibfk_1`');
        $this->addSql('ALTER TABLE lesson DROP FOREIGN KEY `lesson_ibfk_1`');
        $this->addSql('ALTER TABLE password_resets DROP FOREIGN KEY `password_resets_ibfk_1`');
        $this->addSql('ALTER TABLE portefeuille DROP FOREIGN KEY `portefeuille_ibfk_1`');
        $this->addSql('ALTER TABLE portefeuille DROP FOREIGN KEY `portefeuille_ibfk_2`');
        $this->addSql('ALTER TABLE quiz_result DROP FOREIGN KEY `quiz_result_ibfk_1`');
        $this->addSql('ALTER TABLE quiz_result DROP FOREIGN KEY `quiz_result_ibfk_2`');
        $this->addSql('ALTER TABLE transaction_bourse DROP FOREIGN KEY `transaction_bourse_ibfk_1`');
        $this->addSql('ALTER TABLE transaction_bourse DROP FOREIGN KEY `transaction_bourse_ibfk_2`');
        $this->addSql('ALTER TABLE transaction_wallet DROP FOREIGN KEY `transaction_wallet_ibfk_1`');
        $this->addSql('ALTER TABLE transaction_wallet DROP FOREIGN KEY `transaction_wallet_ibfk_2`');
        $this->addSql('ALTER TABLE user_biometrics DROP FOREIGN KEY `user_biometrics_ibfk_1`');
        $this->addSql('ALTER TABLE wallet_appel_offre_link DROP FOREIGN KEY `wallet_appel_offre_link_ibfk_1`');
        $this->addSql('ALTER TABLE wallet_appel_offre_link DROP FOREIGN KEY `wallet_appel_offre_link_ibfk_2`');
        $this->addSql('ALTER TABLE wallet_bourse_link DROP FOREIGN KEY `wallet_bourse_link_ibfk_1`');
        $this->addSql('ALTER TABLE wallet_bourse_link DROP FOREIGN KEY `wallet_bourse_link_ibfk_2`');
        $this->addSql('ALTER TABLE wallet_formation_link DROP FOREIGN KEY `wallet_formation_link_ibfk_1`');
        $this->addSql('ALTER TABLE wallet_formation_link DROP FOREIGN KEY `wallet_formation_link_ibfk_2`');
        $this->addSql('ALTER TABLE wallet_investment_link DROP FOREIGN KEY `wallet_investment_link_ibfk_1`');
        $this->addSql('ALTER TABLE wallet_investment_link DROP FOREIGN KEY `wallet_investment_link_ibfk_2`');
        $this->addSql('ALTER TABLE wishlist DROP FOREIGN KEY `wishlist_ibfk_1`');
        $this->addSql('DROP TABLE action');
        $this->addSql('DROP TABLE appel_offre');
        $this->addSql('DROP TABLE bourse');
        $this->addSql('DROP TABLE candidature');
        $this->addSql('DROP TABLE category');
        $this->addSql('DROP TABLE commission');
        $this->addSql('DROP TABLE formation');
        $this->addSql('DROP TABLE formation_enrollment');
        $this->addSql('DROP TABLE investment');
        $this->addSql('DROP TABLE investment_management');
        $this->addSql('DROP TABLE lesson');
        $this->addSql('DROP TABLE password_resets');
        $this->addSql('DROP TABLE portefeuille');
        $this->addSql('DROP TABLE quiz_result');
        $this->addSql('DROP TABLE training_center');
        $this->addSql('DROP TABLE transaction_bourse');
        $this->addSql('DROP TABLE transaction_wallet');
        $this->addSql('DROP TABLE user_biometrics');
        $this->addSql('DROP TABLE wallet_appel_offre_link');
        $this->addSql('DROP TABLE wallet_bourse_link');
        $this->addSql('DROP TABLE wallet_formation_link');
        $this->addSql('DROP TABLE wallet_investment_link');
        $this->addSql('DROP TABLE wishlist');
        $this->addSql('DROP INDEX username ON users');
        $this->addSql('DROP INDEX idx_email ON users');
        $this->addSql('DROP INDEX idx_username ON users');
        $this->addSql('ALTER TABLE users ADD is_verified TINYINT DEFAULT 0 NOT NULL, CHANGE username username VARCHAR(100) NOT NULL, CHANGE email email VARCHAR(180) NOT NULL, CHANGE role role VARCHAR(20) NOT NULL, CHANGE address address VARCHAR(100) DEFAULT NULL, CHANGE created_at created_at DATETIME NOT NULL');
        $this->addSql('DROP INDEX email ON users');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_1483A5E9E7927C74 ON users (email)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE action (id_action INT AUTO_INCREMENT NOT NULL, id_bourse INT NOT NULL, symbole VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, nom_entreprise VARCHAR(150) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, secteur VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, prix_unitaire DOUBLE PRECISION NOT NULL, quantite_disponible INT NOT NULL, statut ENUM(\'DISPONIBLE\', \'INDISPONIBLE\') CHARACTER SET utf8mb4 DEFAULT \'DISPONIBLE\' COLLATE `utf8mb4_unicode_ci`, date_ajout DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, UNIQUE INDEX symbole (symbole), INDEX idx_symbole (symbole), INDEX idx_bourse (id_bourse), PRIMARY KEY (id_action)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE appel_offre (appel_offre_id INT AUTO_INCREMENT NOT NULL, created_by_user_id INT DEFAULT NULL, titre VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, description TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, categorie VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, type ENUM(\'achat\', \'partenariat\', \'don\') CHARACTER SET utf8mb4 DEFAULT \'achat\' COLLATE `utf8mb4_unicode_ci`, budget_min DOUBLE PRECISION DEFAULT NULL, budget_max DOUBLE PRECISION DEFAULT NULL, devise VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, date_limite DATE DEFAULT NULL, statut ENUM(\'draft\', \'published\', \'closed\') CHARACTER SET utf8mb4 DEFAULT \'draft\' COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_ao_creator (created_by_user_id), INDEX idx_ao_statut (statut), PRIMARY KEY (appel_offre_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE bourse (id_bourse INT AUTO_INCREMENT NOT NULL, nom_bourse VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, pays VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, devise VARCHAR(10) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, statut ENUM(\'ACTIVE\', \'INACTIVE\') CHARACTER SET utf8mb4 DEFAULT \'ACTIVE\' COLLATE `utf8mb4_unicode_ci`, date_creation DATETIME DEFAULT CURRENT_TIMESTAMP, UNIQUE INDEX nom_bourse (nom_bourse), INDEX idx_nom_bourse (nom_bourse), PRIMARY KEY (id_bourse)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE candidature (candidature_id INT AUTO_INCREMENT NOT NULL, appel_offre_id INT NOT NULL, user_id INT DEFAULT NULL, nom_candidat VARCHAR(150) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, email_candidat VARCHAR(150) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, montant_propose DOUBLE PRECISION DEFAULT NULL, message TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, statut ENUM(\'submitted\', \'accepted\', \'rejected\') CHARACTER SET utf8mb4 DEFAULT \'submitted\' COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_cand_ao (appel_offre_id), INDEX idx_cand_user (user_id), PRIMARY KEY (candidature_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE category (id_category INT AUTO_INCREMENT NOT NULL, nom VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, priorite ENUM(\'HAUTE\', \'MOYENNE\', \'BASSE\') CHARACTER SET utf8mb4 DEFAULT \'MOYENNE\' COLLATE `utf8mb4_unicode_ci`, type ENUM(\'INCOME\', \'OUTCOME\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, PRIMARY KEY (id_category)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE commission (id_commission INT AUTO_INCREMENT NOT NULL, nom VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, symbole VARCHAR(10) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, type_transaction ENUM(\'ACHAT\', \'VENTE\', \'LES_DEUX\') CHARACTER SET utf8mb4 DEFAULT \'LES_DEUX\' COLLATE `utf8mb4_unicode_ci`, taux_pourcentage DOUBLE PRECISION NOT NULL, date_creation DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, date_modification DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, active TINYINT DEFAULT 1, INDEX idx_commission_symbole_active_type (symbole, active, type_transaction, date_modification), PRIMARY KEY (id_commission)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE formation (id INT AUTO_INCREMENT NOT NULL, created_by_user_id INT DEFAULT NULL, titre VARCHAR(120) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, description TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, categorie VARCHAR(80) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, niveau ENUM(\'Débutant\', \'Intermédiaire\', \'Avancé\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, is_published TINYINT DEFAULT 0, image_url VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_form_creator (created_by_user_id), INDEX idx_form_published (is_published), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE formation_enrollment (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, formation_id INT NOT NULL, statut ENUM(\'PENDING\', \'ACTIVE\', \'COMPLETED\', \'CANCELLED\') CHARACTER SET utf8mb4 DEFAULT \'PENDING\' COLLATE `utf8mb4_unicode_ci`, enrolled_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX formation_id (formation_id), UNIQUE INDEX uq_enroll (user_id, formation_id), INDEX IDX_237404D8A76ED395 (user_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE investment (investment_id BIGINT AUTO_INCREMENT NOT NULL, created_by_user_id INT DEFAULT NULL, name VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, category VARCHAR(30) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, location VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, estimated_value NUMERIC(12, 2) NOT NULL, risk_level ENUM(\'LOW\', \'MEDIUM\', \'HIGH\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, image_url VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, description TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, status ENUM(\'ACTIVE\', \'CLOSED\') CHARACTER SET utf8mb4 DEFAULT \'ACTIVE\' COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_inv_creator (created_by_user_id), PRIMARY KEY (investment_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE investment_management (management_id BIGINT AUTO_INCREMENT NOT NULL, investment_id BIGINT NOT NULL, investment_type VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, amount_invested NUMERIC(12, 2) NOT NULL, ownership_percentage NUMERIC(5, 2) NOT NULL, start_date DATE NOT NULL, status ENUM(\'ACTIVE\', \'CLOSED\') CHARACTER SET utf8mb4 DEFAULT \'ACTIVE\' COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_mgmt_investment (investment_id), PRIMARY KEY (management_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE lesson (id INT AUTO_INCREMENT NOT NULL, formation_id INT NOT NULL, titre VARCHAR(120) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, contenu TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, ordre INT DEFAULT 1, duree_minutes INT DEFAULT 0, video_url VARCHAR(500) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, INDEX idx_lesson_form (formation_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE password_resets (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, code VARCHAR(10) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, expires_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, used TINYINT DEFAULT 0, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_user_id (user_id), INDEX idx_code (code), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE portefeuille (id_portefeuille INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, id_action INT NOT NULL, quantite INT DEFAULT 0, UNIQUE INDEX uq_user_action (user_id, id_action), INDEX id_action (id_action), INDEX IDX_2955FFFEA76ED395 (user_id), PRIMARY KEY (id_portefeuille)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE quiz_result (id INT AUTO_INCREMENT NOT NULL, user_id INT DEFAULT NULL, student_name VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, lesson_id INT NOT NULL, lesson_title VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, formation_title VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, score INT NOT NULL, passed TINYINT NOT NULL, taken_at DATETIME DEFAULT CURRENT_TIMESTAMP, INDEX idx_quiz_user (user_id), INDEX idx_quiz_lesson (lesson_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE training_center (id INT AUTO_INCREMENT NOT NULL, name VARCHAR(120) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, address VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, lat DOUBLE PRECISION NOT NULL, lng DOUBLE PRECISION NOT NULL, PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE transaction_bourse (id_transaction INT AUTO_INCREMENT NOT NULL, id_action INT NOT NULL, id_user INT DEFAULT NULL, type_transaction ENUM(\'ACHAT\', \'VENTE\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, quantite INT NOT NULL, prix_unitaire DOUBLE PRECISION NOT NULL, montant_total DOUBLE PRECISION NOT NULL, commission DOUBLE PRECISION DEFAULT \'0\', acteur_role ENUM(\'INVESTISSEUR\', \'ENTREPRISE\', \'ADMIN\') CHARACTER SET utf8mb4 DEFAULT \'INVESTISSEUR\' COLLATE `utf8mb4_unicode_ci`, acteur_label VARCHAR(120) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, date_transaction DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_date (date_transaction), INDEX idx_user (id_user), INDEX idx_action (id_action), PRIMARY KEY (id_transaction)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE transaction_wallet (id_transaction INT AUTO_INCREMENT NOT NULL, nom_transaction VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, type ENUM(\'INCOME\', \'OUTCOME\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, montant DOUBLE PRECISION NOT NULL, date_transaction DATE NOT NULL, source VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT \'MANUAL\' COLLATE `utf8mb4_unicode_ci`, user_id INT DEFAULT NULL, category_id INT DEFAULT NULL, INDEX idx_category (category_id), INDEX idx_date_wallet (date_transaction), INDEX idx_user_wallet (user_id), PRIMARY KEY (id_transaction)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE user_biometrics (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, face_embedding TEXT CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, is_active TINYINT DEFAULT 1, INDEX user_id (user_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wallet_appel_offre_link (id BIGINT AUTO_INCREMENT NOT NULL, wallet_tx_id INT NOT NULL, appel_offre_id INT NOT NULL, relation ENUM(\'DON\', \'PAIEMENT\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX wallet_tx_id (wallet_tx_id), INDEX idx_wallet_ao (appel_offre_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wallet_bourse_link (id BIGINT AUTO_INCREMENT NOT NULL, wallet_tx_id INT NOT NULL, bourse_tx_id INT NOT NULL, relation ENUM(\'DEBIT_ACHAT\', \'CREDIT_VENTE\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, UNIQUE INDEX uq_wallet_bourse (wallet_tx_id, bourse_tx_id), INDEX bourse_tx_id (bourse_tx_id), INDEX IDX_4A6D67948DFC4339 (wallet_tx_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wallet_formation_link (id BIGINT AUTO_INCREMENT NOT NULL, wallet_tx_id INT NOT NULL, enrollment_id INT NOT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX enrollment_id (enrollment_id), UNIQUE INDEX uq_wallet_form (wallet_tx_id, enrollment_id), INDEX IDX_DCFFAF898DFC4339 (wallet_tx_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wallet_investment_link (id BIGINT AUTO_INCREMENT NOT NULL, wallet_tx_id INT NOT NULL, management_id BIGINT NOT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX management_id (management_id), UNIQUE INDEX uq_wallet_invest (wallet_tx_id, management_id), INDEX IDX_6D52B86A8DFC4339 (wallet_tx_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE wishlist (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, name VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, price DOUBLE PRECISION NOT NULL, INDEX idx_wishlist_user (user_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('ALTER TABLE action ADD CONSTRAINT `action_ibfk_1` FOREIGN KEY (id_bourse) REFERENCES bourse (id_bourse) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE appel_offre ADD CONSTRAINT `appel_offre_ibfk_1` FOREIGN KEY (created_by_user_id) REFERENCES users (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE candidature ADD CONSTRAINT `candidature_ibfk_1` FOREIGN KEY (appel_offre_id) REFERENCES appel_offre (appel_offre_id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE candidature ADD CONSTRAINT `candidature_ibfk_2` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE formation ADD CONSTRAINT `formation_ibfk_1` FOREIGN KEY (created_by_user_id) REFERENCES users (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE formation_enrollment ADD CONSTRAINT `formation_enrollment_ibfk_1` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE formation_enrollment ADD CONSTRAINT `formation_enrollment_ibfk_2` FOREIGN KEY (formation_id) REFERENCES formation (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE investment ADD CONSTRAINT `investment_ibfk_1` FOREIGN KEY (created_by_user_id) REFERENCES users (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE investment_management ADD CONSTRAINT `investment_management_ibfk_1` FOREIGN KEY (investment_id) REFERENCES investment (investment_id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE lesson ADD CONSTRAINT `lesson_ibfk_1` FOREIGN KEY (formation_id) REFERENCES formation (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE password_resets ADD CONSTRAINT `password_resets_ibfk_1` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE portefeuille ADD CONSTRAINT `portefeuille_ibfk_1` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE portefeuille ADD CONSTRAINT `portefeuille_ibfk_2` FOREIGN KEY (id_action) REFERENCES action (id_action) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE quiz_result ADD CONSTRAINT `quiz_result_ibfk_1` FOREIGN KEY (lesson_id) REFERENCES lesson (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE quiz_result ADD CONSTRAINT `quiz_result_ibfk_2` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE transaction_bourse ADD CONSTRAINT `transaction_bourse_ibfk_1` FOREIGN KEY (id_action) REFERENCES action (id_action) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE transaction_bourse ADD CONSTRAINT `transaction_bourse_ibfk_2` FOREIGN KEY (id_user) REFERENCES users (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE transaction_wallet ADD CONSTRAINT `transaction_wallet_ibfk_1` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE transaction_wallet ADD CONSTRAINT `transaction_wallet_ibfk_2` FOREIGN KEY (category_id) REFERENCES category (id_category) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE user_biometrics ADD CONSTRAINT `user_biometrics_ibfk_1` FOREIGN KEY (user_id) REFERENCES users (id)');
        $this->addSql('ALTER TABLE wallet_appel_offre_link ADD CONSTRAINT `wallet_appel_offre_link_ibfk_1` FOREIGN KEY (wallet_tx_id) REFERENCES transaction_wallet (id_transaction) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE wallet_appel_offre_link ADD CONSTRAINT `wallet_appel_offre_link_ibfk_2` FOREIGN KEY (appel_offre_id) REFERENCES appel_offre (appel_offre_id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE wallet_bourse_link ADD CONSTRAINT `wallet_bourse_link_ibfk_1` FOREIGN KEY (wallet_tx_id) REFERENCES transaction_wallet (id_transaction) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE wallet_bourse_link ADD CONSTRAINT `wallet_bourse_link_ibfk_2` FOREIGN KEY (bourse_tx_id) REFERENCES transaction_bourse (id_transaction) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE wallet_formation_link ADD CONSTRAINT `wallet_formation_link_ibfk_1` FOREIGN KEY (wallet_tx_id) REFERENCES transaction_wallet (id_transaction) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE wallet_formation_link ADD CONSTRAINT `wallet_formation_link_ibfk_2` FOREIGN KEY (enrollment_id) REFERENCES formation_enrollment (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE wallet_investment_link ADD CONSTRAINT `wallet_investment_link_ibfk_1` FOREIGN KEY (wallet_tx_id) REFERENCES transaction_wallet (id_transaction) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE wallet_investment_link ADD CONSTRAINT `wallet_investment_link_ibfk_2` FOREIGN KEY (management_id) REFERENCES investment_management (management_id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE wishlist ADD CONSTRAINT `wishlist_ibfk_1` FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE');
        $this->addSql('DROP TABLE messenger_messages');
        $this->addSql('ALTER TABLE users DROP is_verified, CHANGE username username VARCHAR(50) NOT NULL, CHANGE email email VARCHAR(150) NOT NULL, CHANGE role role ENUM(\'ADMIN\', \'ENTREPRISE\', \'USER\') DEFAULT \'USER\', CHANGE address address VARCHAR(255) DEFAULT NULL, CHANGE created_at created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL');
        $this->addSql('CREATE UNIQUE INDEX username ON users (username)');
        $this->addSql('CREATE INDEX idx_email ON users (email)');
        $this->addSql('CREATE INDEX idx_username ON users (username)');
        $this->addSql('DROP INDEX uniq_1483a5e9e7927c74 ON users');
        $this->addSql('CREATE UNIQUE INDEX email ON users (email)');
    }
}
