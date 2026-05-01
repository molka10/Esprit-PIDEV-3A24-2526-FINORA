<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260429121716 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Final architectural stabilization: Recreate missing tables and harmonize schema.';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('SET FOREIGN_KEY_CHECKS = 0;');

        // Cleanup any partial states
        $this->addSql('DROP TABLE IF EXISTS card;');
        $this->addSql('DROP TABLE IF EXISTS recharge_request;');
        $this->addSql('DROP TABLE IF EXISTS `user`;');
        $this->addSql('DROP TABLE IF EXISTS user_biometric;');
        $this->addSql('DROP TABLE IF EXISTS user_biometrics;');

        // Recreate essential tables
        $this->addSql('CREATE TABLE `user` (id INT AUTO_INCREMENT NOT NULL, image VARCHAR(255) DEFAULT NULL, username VARCHAR(100) NOT NULL, email VARCHAR(180) NOT NULL, mot_de_passe VARCHAR(255) DEFAULT NULL, role VARCHAR(20) NOT NULL, phone VARCHAR(20) DEFAULT NULL, address VARCHAR(100) DEFAULT NULL, date_of_birth DATE DEFAULT NULL, created_at DATETIME NOT NULL, is_verified TINYINT NOT NULL, current_session_id VARCHAR(255) DEFAULT NULL, balance NUMERIC(10, 2) DEFAULT 0 NOT NULL, UNIQUE INDEX UNIQ_8D93D649F85E0677 (username), UNIQUE INDEX UNIQ_8D93D649E7927C74 (email), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`;');
        $this->addSql('CREATE TABLE card (id INT AUTO_INCREMENT NOT NULL, card_holder_name VARCHAR(255) NOT NULL, last4 VARCHAR(4) NOT NULL, brand VARCHAR(50) NOT NULL, stripe_payment_method_id VARCHAR(255) NOT NULL, expiry_date VARCHAR(5) NOT NULL, is_default TINYINT NOT NULL, created_at DATETIME NOT NULL, user_id INT NOT NULL, INDEX IDX_161498D3A76ED395 (user_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`;');
        $this->addSql('CREATE TABLE recharge_request (id INT AUTO_INCREMENT NOT NULL, amount NUMERIC(10, 2) NOT NULL, status VARCHAR(20) NOT NULL, otp VARCHAR(10) DEFAULT NULL, created_at DATETIME NOT NULL, confirmed_at DATETIME DEFAULT NULL, user_id INT NOT NULL, card_id INT NOT NULL, INDEX IDX_7B0324BAA76ED395 (user_id), INDEX IDX_7B0324BA4ACC9A20 (card_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`;');
        $this->addSql('CREATE TABLE user_biometric (id INT AUTO_INCREMENT NOT NULL, face_embedding LONGTEXT DEFAULT NULL, is_active TINYINT NOT NULL, created_at DATETIME NOT NULL, user_id INT NOT NULL, UNIQUE INDEX UNIQ_103238B4A76ED395 (user_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`;');

        // Restore FKs for newly created tables
        $this->addSql('ALTER TABLE card ADD CONSTRAINT FK_161498D3A76ED395 FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE;');
        $this->addSql('ALTER TABLE recharge_request ADD CONSTRAINT FK_7B0324BAA76ED395 FOREIGN KEY (user_id) REFERENCES `user` (id);');
        $this->addSql('ALTER TABLE recharge_request ADD CONSTRAINT FK_7B0324BA4ACC9A20 FOREIGN KEY (card_id) REFERENCES card (id) ON DELETE CASCADE;');
        $this->addSql('ALTER TABLE user_biometric ADD CONSTRAINT FK_103238B4A76ED395 FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE;');

        // Harmonize existing tables
        $this->addSql('ALTER TABLE action DROP FOREIGN KEY IF EXISTS FK_47CC8C924E67DDD1;');
        $this->addSql('ALTER TABLE action CHANGE id_action id INT AUTO_INCREMENT NOT NULL, DROP PRIMARY KEY, ADD PRIMARY KEY (id);');
        $this->addSql('ALTER TABLE action ADD CONSTRAINT FK_47CC8C924E67DDD1 FOREIGN KEY (bourse_id) REFERENCES bourse (id);');

        $this->addSql('ALTER TABLE investment_management DROP FOREIGN KEY IF EXISTS FK_MANAGEMENT_INVESTMENT;');
        $this->addSql('ALTER TABLE investment_management CHANGE created_at created_at DATETIME NOT NULL;');
        $this->addSql('ALTER TABLE investment_management ADD CONSTRAINT FK_12369BDE6E1B4FD5 FOREIGN KEY (investment_id) REFERENCES investment (id) ON DELETE CASCADE;');

        $this->addSql('ALTER TABLE investment CHANGE investment_id id INT AUTO_INCREMENT NOT NULL, DROP PRIMARY KEY, ADD PRIMARY KEY (id);');
        $this->addSql('ALTER TABLE bourse CHANGE id_bourse id INT AUTO_INCREMENT NOT NULL, DROP PRIMARY KEY, ADD PRIMARY KEY (id);');

        $this->addSql('ALTER TABLE candidature DROP FOREIGN KEY IF EXISTS FK_E33BD3B8308E35F8;');
        $this->addSql('ALTER TABLE candidature ADD CONSTRAINT FK_E33BD3B8308E35F8 FOREIGN KEY (appel_offre_id) REFERENCES appel_offre (id) ON DELETE CASCADE;');

        $this->addSql('ALTER TABLE transaction_bourse DROP FOREIGN KEY IF EXISTS FK_ABBFCE5C9D32F035;');
        $this->addSql('ALTER TABLE transaction_bourse CHANGE id_transaction id INT AUTO_INCREMENT NOT NULL, DROP PRIMARY KEY, ADD PRIMARY KEY (id);');
        $this->addSql('ALTER TABLE transaction_bourse ADD CONSTRAINT FK_ABBFCE5C9D32F035 FOREIGN KEY (action_id) REFERENCES action (id);');

        $this->addSql('ALTER TABLE action_news DROP FOREIGN KEY IF EXISTS FK_6B32225E9D32F035;');
        $this->addSql('ALTER TABLE action_news CHANGE impact_percent impact_percent NUMERIC(10, 2) NOT NULL;');
        $this->addSql('ALTER TABLE action_news ADD CONSTRAINT FK_6B32225E9D32F035 FOREIGN KEY (action_id) REFERENCES action (id) ON DELETE CASCADE;');

        $this->addSql('ALTER TABLE bourse_wishlist DROP FOREIGN KEY IF EXISTS FK_8CAE9C8F9D32F035;');
        $this->addSql('ALTER TABLE bourse_wishlist ADD CONSTRAINT FK_8CAE9C8F9D32F035 FOREIGN KEY (action_id) REFERENCES action (id) ON DELETE CASCADE;');

        $this->addSql('ALTER TABLE category CHANGE id_category id INT AUTO_INCREMENT NOT NULL, DROP PRIMARY KEY, ADD PRIMARY KEY (id);');

        $this->addSql('ALTER TABLE investment_notification DROP FOREIGN KEY IF EXISTS FK_30B6F9EE6E1B4FD5;');
        $this->addSql('ALTER TABLE investment_notification ADD CONSTRAINT FK_30B6F9EE6E1B4FD5 FOREIGN KEY (investment_id) REFERENCES investment (id) ON DELETE CASCADE;');

        $this->addSql('ALTER TABLE investment_wishlist DROP FOREIGN KEY IF EXISTS FK_FAVORITE_INVEST;');
        $this->addSql('ALTER TABLE investment_wishlist ADD CONSTRAINT FK_A55DF32D6E1B4FD5 FOREIGN KEY (investment_id) REFERENCES investment (id) ON DELETE CASCADE;');

        $this->addSql('ALTER TABLE notification_bourse DROP FOREIGN KEY IF EXISTS FK_196542419D32F035;');
        $this->addSql('ALTER TABLE notification_bourse ADD CONSTRAINT FK_196542419D32F035 FOREIGN KEY (action_id) REFERENCES action (id) ON DELETE SET NULL;');

        $this->addSql('ALTER TABLE rating DROP FOREIGN KEY IF EXISTS FK_D8892622308E35F8;');
        $this->addSql('ALTER TABLE rating ADD CONSTRAINT FK_D8892622308E35F8 FOREIGN KEY (appel_offre_id) REFERENCES appel_offre (id) ON DELETE CASCADE;');

        $this->addSql('ALTER TABLE rating_centre DROP FOREIGN KEY IF EXISTS FK_E95280FD463CD7C3;');
        $this->addSql('ALTER TABLE rating_centre DROP FOREIGN KEY IF EXISTS FK_E95280FDA76ED395;');
        $this->addSql('ALTER TABLE rating_centre ADD CONSTRAINT FK_E95280FD463CD7C3 FOREIGN KEY (centre_id) REFERENCES centre_formation (id) ON DELETE CASCADE;');
        $this->addSql('ALTER TABLE rating_centre ADD CONSTRAINT FK_E95280FDA76ED395 FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE;');

        $this->addSql('ALTER TABLE transaction_wallet DROP FOREIGN KEY IF EXISTS FK_A15E05F12469DE2;');
        $this->addSql('ALTER TABLE transaction_wallet ADD CONSTRAINT FK_A15E05F12469DE2 FOREIGN KEY (category_id) REFERENCES category (id);');

        $this->addSql('SET FOREIGN_KEY_CHECKS = 1;');
    }

    public function down(Schema $schema): void
    {
    }
}
