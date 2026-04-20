SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

INSERT IGNORE INTO `cards` (`id`, `card_holder_name`, `card_number`, `expiry_date`, `cvv`, `user_id`, `is_default`, `created_at`) VALUES
(1, 'imen neifar', '5399959907294347', '02/27', '689', 6, 1, '2026-04-18 17:58:26'),
(2, 'imen neifar', '5399959907294347', '02/27', '689', 1, 1, '2026-04-18 19:23:51');

INSERT IGNORE INTO `category` (`id_category`, `nom`, `priorite`, `type`, `user_id`) VALUES
(31, 'income', 'HAUTE', 'INCOME', 1),
(32, 'outcome', 'MOYENNE', 'OUTCOME', 1),
(33, 'testacategory', 'HAUTE', 'INCOME', 1),
(34, 'testacategory', 'MOYENNE', 'OUTCOME', 1),
(35, 'test1', 'HAUTE', 'INCOME', 1),
(36, 'test2', 'MOYENNE', 'OUTCOME', 1),
(38, 'category', 'MOYENNE', 'OUTCOME', 1),
(45, 'page test', 'MOYENNE', 'OUTCOME', 6),
(46, 'test', 'BASSE', 'INCOME', 6);

INSERT IGNORE INTO `recharge_requests` (`id`, `amount`, `status`, `otp`, `user_id`, `card_id`, `created_at`, `confirmed_at`) VALUES
(14, 400, 'COMPLETED', NULL, 6, 1, '2026-04-18 18:14:48', '2026-04-18 18:14:48'),
(15, 200, 'PENDING', NULL, 6, 1, '2026-04-18 18:17:50', NULL),
(16, 200, 'COMPLETED', NULL, 6, 1, '2026-04-18 18:19:33', '2026-04-18 18:19:33'),
(17, 400, 'COMPLETED', NULL, 6, 1, '2026-04-18 18:20:08', '2026-04-18 18:20:08'),
(18, 300, 'COMPLETED', NULL, 6, 1, '2026-04-18 18:33:20', '2026-04-18 18:33:22'),
(19, 300, 'COMPLETED', NULL, 6, 1, '2026-04-18 19:05:17', '2026-04-18 19:05:19'),
(20, 120, 'COMPLETED', NULL, 6, 1, '2026-04-18 19:17:54', '2026-04-18 19:17:56'),
(21, 180, 'COMPLETED', NULL, 6, 1, '2026-04-18 19:23:32', '2026-04-18 19:23:34'),
(22, 460, 'COMPLETED', NULL, 1, 2, '2026-04-18 19:24:05', '2026-04-18 19:24:06');

INSERT IGNORE INTO `transaction_wallet` (`id_transaction`, `nom_transaction`, `type`, `montant`, `date_transaction`, `source`, `user_id`, `category_id`, `is_active`) VALUES
(63, 'test income', 'INCOME', 500, '2026-04-11 00:00:00', 'manual', 1, 31, 1),
(65, 'income', 'INCOME', 1000, '2026-04-07 00:00:00', 'manual', 1, 31, 1),
(68, 'testtest', 'OUTCOME', -1410, '2026-04-09 00:00:00', 'manual', 1, 34, 1),
(69, 'salaire', 'OUTCOME', -150, '2026-04-18 00:00:00', 'manual', 1, 32, 1),
(71, 'jjj', 'INCOME', 3000, '2026-04-01 00:00:00', 'manual', 6, 31, 1),
(72, 'fff', 'INCOME', 2500, '2026-04-17 00:00:00', 'manual', 1, 33, 1),
(106, 'out', 'OUTCOME', -200, '2026-04-16 00:00:00', 'manual', 6, 32, 1),
(107, 'outcome1', 'OUTCOME', -200, '2026-04-18 00:00:00', 'manual', 6, 45, 1),
(109, 'testout', 'INCOME', 700, '2026-05-23 00:00:00', 'manual', 6, 31, 1),
(110, 'testout', 'OUTCOME', -700, '2026-03-06 00:00:00', 'manual', 6, 38, 1),
(112, 'Recharge Portfolio (External API)', 'INCOME', 200, '2026-04-18 18:19:33', 'EXTERNAL_API', 6, 31, 1),
(113, 'Recharge Portfolio (External API)', 'INCOME', 400, '2026-04-18 18:20:08', 'EXTERNAL_API', 6, 31, 1),
(114, 'Recharge Portfolio (API Stripe)', 'INCOME', 300, '2026-04-18 18:33:22', 'STRIPE_GW', 6, 31, 1),
(115, 'Recharge Portfolio (API Stripe)', 'INCOME', 300, '2026-04-18 19:05:19', 'STRIPE_GW', 6, 31, 1),
(116, 'testout', 'OUTCOME', -700, '2026-04-23 18:10:00', 'manual', 6, 32, 1),
(117, 'Recharge Portfolio (API Stripe)', 'INCOME', 120, '2026-04-18 19:17:56', 'STRIPE_GW', 6, 31, 1),
(118, 'Recharge Portfolio (API Stripe)', 'INCOME', 180, '2026-04-18 19:23:34', 'STRIPE_GW', 6, 31, 1),
(119, 'Recharge Portfolio (API Stripe)', 'INCOME', 460, '2026-04-18 19:24:06', 'STRIPE_GW', 1, 31, 1);

INSERT IGNORE INTO `wishlist` (`id`, `user_id`, `name`, `price`) VALUES
(12, 1, 'hhh', 100),
(16, 1, 'mmm', 200),
(17, 1, 'hihih', 120),
(20, 6, 'car', 50000);
