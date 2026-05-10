php bin/console dbal:run-sql "CREATE INDEX IDX_4C258FDA76ED395 ON cards (user_id)"
php bin/console dbal:run-sql "ALTER TABLE cards ADD CONSTRAINT FK_4C258FDA76ED395 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE"

php bin/console dbal:run-sql "CREATE INDEX IDX_B89DAA75A76ED395 ON margin_loan (user_id)"
php bin/console dbal:run-sql "ALTER TABLE margin_loan ADD CONSTRAINT FK_B89DAA75A76ED395 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE"

php bin/console dbal:run-sql "CREATE INDEX IDX_30C196BDA76ED395 ON recharge_requests (user_id)"
php bin/console dbal:run-sql "CREATE INDEX IDX_30C196BD4ACC9A20 ON recharge_requests (card_id)"
php bin/console dbal:run-sql "ALTER TABLE recharge_requests ADD CONSTRAINT FK_30C196BDA76ED395 FOREIGN KEY (user_id) REFERENCES users (id)"
php bin/console dbal:run-sql "ALTER TABLE recharge_requests ADD CONSTRAINT FK_30C196BD4ACC9A20 FOREIGN KEY (card_id) REFERENCES cards (id) ON DELETE CASCADE"

php bin/console dbal:run-sql "CREATE INDEX IDX_911FF397A76ED395 ON user_api_key (user_id)"
php bin/console dbal:run-sql "ALTER TABLE user_api_key ADD CONSTRAINT FK_911FF397A76ED395 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE"

php bin/console dbal:run-sql "CREATE INDEX IDX_9CE12A31A76ED395 ON wishlist (user_id)"
php bin/console dbal:run-sql "ALTER TABLE wishlist ADD CONSTRAINT FK_9CE12A31A76ED395 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE"
