-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : sam. 25 avr. 2026 à 23:35
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;
SET time_zone = "+00:00";

--
-- Base de données : `finora`
--

-- --------------------------------------------------------

--
-- Structure de la table `action`
--

CREATE TABLE `action` (
  `id_action` int(11) NOT NULL,
  `symbole` varchar(20) NOT NULL,
  `nom_entreprise` varchar(150) NOT NULL,
  `secteur` varchar(100) NOT NULL,
  `prix_unitaire` double NOT NULL,
  `quantite_disponible` int(11) NOT NULL,
  `statut` varchar(20) NOT NULL,
  `date_ajout` datetime NOT NULL,
  `bourse_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `action`
--

INSERT INTO `action` (`id_action`, `symbole`, `nom_entreprise`, `secteur`, `prix_unitaire`, `quantite_disponible`, `statut`, `date_ajout`, `bourse_id`) VALUES
(1, 'AAPL', 'apple', 'Technologie', 272.95, 8, 'DISPONIBLE', '2026-02-25 05:22:43', 1),
(3, 'GOOGL', 'google', 'Télécommunications', 298.4, 0, 'DISPONIBLE', '2026-02-27 01:45:47', 1),
(4, 'MIC', 'MICROSOFT', 'Immobilier', 12.83, 0, 'INDISPONIBLE', '2026-04-16 15:04:44', 2);

-- --------------------------------------------------------

--
-- Structure de la table `action_news`
--

CREATE TABLE `action_news` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `impact_percent` double NOT NULL,
  `date_ajout` datetime NOT NULL,
  `action_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `action_news`
--

INSERT INTO `action_news` (`id`, `titre`, `impact_percent`, `date_ajout`, `action_id`) VALUES
(1, 'Fusion surprise annoncée avec un concurrent. (google)', 30, '2026-04-21 00:14:26', 3),
(2, 'Problème de chaîne d\'approvisionnement. (MICROSOFT)', -15, '2026-04-21 00:16:39', 4),
(3, 'Hausse record des bénéfices trimestriels. (MICROSOFT)', 15, '2026-04-21 00:18:04', 4),
(4, 'Lancement d\'un nouveau produit révolutionnaire. (MICROSOFT)', 25, '2026-04-21 14:52:41', 4),
(5, 'Scandale financier révélé ! L\'entreprise plonge. (MICROSOFT)', -30, '2026-04-22 17:07:07', 4),
(6, 'Lancement d\'un nouveau produit révolutionnaire. (google)', 25, '2026-04-25 02:01:23', 3),
(7, 'Fusion surprise annoncée avec un concurrent. (google)', 30, '2026-04-25 21:58:53', 3);

-- --------------------------------------------------------

--
-- Structure de la table `appel_offre`
--

CREATE TABLE `appel_offre` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `description` longtext DEFAULT NULL,
  `type` varchar(50) NOT NULL,
  `budget_min` decimal(10,2) DEFAULT NULL,
  `budget_max` decimal(10,2) DEFAULT NULL,
  `devise` varchar(20) DEFAULT NULL,
  `date_limite` date DEFAULT NULL,
  `statut` varchar(50) NOT NULL,
  `created_at` datetime NOT NULL,
  `categorie_id` int(11) DEFAULT NULL,
  `created_by_id` int(11) DEFAULT NULL,
  `required_criteria` longtext DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `appel_offre` (`id`, `titre`, `description`, `type`, `budget_min`, `budget_max`, `devise`, `date_limite`, `statut`, `created_at`, `categorie_id`, `created_by_id`, `required_criteria`) VALUES
(3, 'Stage PFE', 'des etudiant parfait', 'partenariat', 100.00, 300.00, 'TND', '2026-04-16', 'closed', '2026-04-04 07:29:05', 3, NULL, NULL),
(4, 'Appel d\'offre test', NULL, 'achat', 1000.00, 5000.00, 'TND', '2026-12-31', 'published', '2026-04-04 07:31:09', 3, NULL, NULL),
(6, 'slm', 'slm ya ayouha al chaa3b', 'partenariat', 10.00, 100.00, 'TND', '2026-04-03', 'draft', '2026-04-04 20:42:39', 3, NULL, NULL),
(8, 'test', 'zetstststststststststst', 'achat', 500.00, 1000.00, 'EUR', '2026-04-05', 'closed', '2026-04-06 12:56:54', 3, NULL, NULL),
(9, 'slm', 'haaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 'partenariat', 10.00, 40.00, 'TND', '2026-04-12', 'closed', '2026-04-13 02:48:05', 3, NULL, NULL),
(11, 'Offre Dev Web', 'Cr├®ation site web moderne', 'achat', 800.00, 2000.00, 'TND', '2026-05-01', 'draft', '2026-04-13 04:09:04', 3, NULL, NULL),
(12, 'Stage IA', 'Recherche ├®tudiant en intelligence artificielle', 'partenariat', 0.00, 0.00, 'TND', '2026-06-10', 'published', '2026-04-13 04:09:04', 3, NULL, NULL),
(13, 'Projet Mobile', 'D├®veloppement application mobile Android/iOS', 'achat', 1500.00, 4000.00, 'EUR', '2026-05-20', 'draft', '2026-04-13 04:09:04', 3, NULL, NULL),
(14, 'Analyse Réseau', 'Audit et sécurisation du réseau informatique', 'partenariat', 200.00, 800.00, 'TND', '2026-06-01', 'published', '2026-04-13 04:09:47', 3, NULL, NULL),
(15, 'Design UI/UX', 'Cr├®ation interface utilisateur moderne et intuitive', 'achat', 500.00, 1500.00, 'EUR', '2026-05-25', 'published', '2026-04-13 04:09:47', 3, NULL, NULL),
(21, 'D├®veloppement Plateforme E-Learning', 'Offre d\'achat : D├®veloppement d\'une plateforme E-Learning multim├®dia. Nous recherchons un partenaire exp├®riment├® pour concevoir et r├®aliser notre projet. Solution innovante et performante souhait├®e.', 'achat', 20000.00, 50000.00, 'TND', '2026-04-21', 'closed', '2026-04-18 21:21:32', 5, NULL, NULL),
(22, 'stage platforme de santé', 'Offre de partenariat : Stage sur plateforme de santé informatisée. Améliorez vos compétences dans le domaine de l\'informatique santé. Collaborons pour innovations révolutionnaires.', 'partenariat', 5000.00, 10000.00, 'TND', '2026-04-21', 'closed', '2026-04-20 04:46:08', 3, NULL, NULL),
(23, 'platforme de gaming', 'Partenariat innovant dans le domaine de la jeu vidéo : nous offrons une plateforme avancée pour un développement mutuel. (Informatique, Gaming)', 'partenariat', 20000.00, 50000.00, 'TND', '2026-04-21', 'closed', '2026-04-20 05:41:37', 3, NULL, NULL),
(24, 'platforme de education', 'Partenariat en education numérique : développer une plateforme innovante dans le domaine de l\'informatique.', 'partenariat', 20000.00, 50000.00, 'TND', '2026-04-21', 'closed', '2026-04-20 05:50:51', 3, NULL, NULL),
(25, 'devlopment web', 'Partenariat en développement web innovant : création de sites web performants et responsive, intégration de fonctionnalités avancées, collaboration pour améliorer votre présence numérique.', 'partenariat', 10000.00, 20000.00, 'TND', '2026-04-21', 'closed', '2026-04-20 12:55:07', 3, NULL, NULL),
(26, 'devlopment mobile', 'Partenariat en développement mobile innovant : création d\'applications mobiles performantes et intuitives (Informatique).', 'partenariat', 10000.00, 30000.00, 'TND', '2026-04-21', 'closed', '2026-04-20 13:19:37', 3, NULL, NULL),
(27, 'Devlopment site pour cars', 'Partenariat : Développement de site web innovant pour le marché automobile, intégrant multimedia.', 'partenariat', 20000.00, 50000.00, 'TND', '2026-04-25', 'closed', '2026-04-24 19:08:27', 5, NULL, 'Expérience en développement web, Maîtrise de HTML, CSS et JavaScript, Connaissance de frameworks multimédia tels que Three.js ou WebGL, Familiarité avec bases de données SQL ou NoSQL, Bonne compréhension de l\'anglais écrit (pour interagir avec l\'équipe internationale).');

-- --------------------------------------------------------

--
-- Structure de la table `bourse`
--

CREATE TABLE `bourse` (
  `id_bourse` int(11) NOT NULL,
  `nom_bourse` varchar(100) NOT NULL,
  `pays` varchar(50) NOT NULL,
  `devise` varchar(3) NOT NULL,
  `statut` varchar(20) NOT NULL,
  `date_creation` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `bourse` (`id_bourse`, `nom_bourse`, `pays`, `devise`, `statut`, `date_creation`) VALUES
(1, 'llll', 'Albanie', 'TND', 'ACTIVE', '2026-02-24 23:01:35'),
(2, 'paris', 'france', 'EUR', 'ACTIVE', '2026-04-16 15:03:41'),
(3, 'berlin', 'Albanie', 'TND', 'ACTIVE', '2026-04-20 12:16:12');

-- --------------------------------------------------------

--
-- Structure de la table `bourse_wishlist`
--

CREATE TABLE `bourse_wishlist` (
  `id` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `action_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `bourse_wishlist` (`id`, `created_at`, `user_id`, `action_id`) VALUES
(1, '2026-04-19 20:51:11', 15, 1),
(2, '2026-04-19 21:30:03', 15, 4),
(3, '2026-04-20 01:34:25', 21, 1);

-- --------------------------------------------------------

--
-- Structure de la table `candidature`
--

CREATE TABLE `candidature` (
  `id` int(11) NOT NULL,
  `montant_propose` decimal(10,2) DEFAULT NULL,
  `message` longtext DEFAULT NULL,
  `statut` varchar(50) NOT NULL,
  `created_at` datetime NOT NULL,
  `appel_offre_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `cv_path` varchar(255) DEFAULT NULL,
  `ai_score` int(11) DEFAULT NULL,
  `ai_analysis` longtext DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `candidature` (`id`, `montant_propose`, `message`, `statut`, `created_at`, `appel_offre_id`, `user_id`, `cv_path`, `ai_score`, `ai_analysis`) VALUES
(19, 10000.00, 'je a beacoup d\'experience dans ce domaine et je pense que je suis un membre serieux pour ce project donc je suis fiere detre dans ce project', 'accepted', '2026-04-20 06:08:30', 23, 21, NULL, NULL, NULL),
(20, 30000.00, 'accepte moi je suis compitant', 'rejected', '2026-04-20 07:11:41', 24, 26, NULL, NULL, NULL),
(21, 1400.00, 'lskdmlskfmlskfmslfkmlqskfml', 'submitted', '2026-04-20 13:06:28', 25, 21, NULL, NULL, NULL),
(22, 10000.00, 'je suis motiver et  je veux bien travailler', 'accepted', '2026-04-20 13:22:09', 26, 21, NULL, NULL, NULL),
(23, 100.00, 'aaaaaaaaaaaaaaaaaaaa', 'rejected', '2026-04-24 17:49:21', 14, 21, 'CV-Analyste-Reseau-69eb9100494c3.pdf', 0, 'Le profil du candidat n\'est pas renseigné (aaaaaaaaaaaaaaaaaaaa), il est donc impossible d\'évaluer sa candidature par rapport aux critères de l\'appel d\'offre. Des informations concrètes sur l\'expérience en audit et sécurisation réseau sont nécessaires.'),
(25, 300.00, '\"Je suis très intéressé par l\'audit réseau', 'accepted', '2026-04-24 18:01:32', 14, 21, 'CV-Securite-Reseau-69eb93d8ed35a.pdf', 85, 'Le candidat possède une solide expérience en audit et sécurisation de réseaux, avec des compétences techniques pertinentes et une formation adéquate. Son expérience actuelle chez TechSafe correspond directement aux exigences de l\'appel d\'offres, bien que sa lettre de motivation soit très succincte.'),
(33, 300.00, 'aaaaaaaaaaaaaaaaaaaaaaaaaa', 'rejected', '2026-04-24 18:36:29', 14, 21, 'CV-Securite-Reseau-69eb9c0ae4237.pdf', 85, 'Le candidat possède une solide expertise en audit et sécurisation réseau, avec des compétences techniques pertinentes et des certifications reconnues. Son expérience récente en tant que Consultant Sécurité Réseau pour des clients grands comptes est un atout majeur, bien que le message de motivation soit absent et donc non évalué.'),
(34, 300.00, 'je uis motiver et j\'ai une pu d\'experience', 'submitted', '2026-04-25 22:07:19', 15, 21, 'CV-Securite-Reseau-69ed1ef45af1e.pdf', 10, 'Le candidat n\'a aucune expérience pertinente en design UI/UX, ses compétences et expériences étant centrées sur la sécurité réseau et les systèmes. Le message de motivation est trop vague et ne démontre pas une compréhension du poste.');

-- --------------------------------------------------------

--
-- Structure de la table `cards`
--

CREATE TABLE `cards` (
  `id` int(11) NOT NULL,
  `card_holder_name` varchar(255) NOT NULL,
  `expiry_date` varchar(5) NOT NULL,
  `last4` varchar(4) NOT NULL,
  `user_id` int(11) NOT NULL,
  `is_default` tinyint(4) NOT NULL,
  `created_at` datetime NOT NULL,
  `brand` varchar(50) NOT NULL,
  `stripe_payment_method_id` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `cards` (`id`, `card_holder_name`, `expiry_date`, `last4`, `user_id`, `is_default`, `created_at`, `brand`, `stripe_payment_method_id`) VALUES
(1, 'imen neifar', '02/27', '689', 6, 1, '2026-04-18 17:58:26', '', ''),
(2, 'imen neifar', '02/27', '689', 1, 1, '2026-04-18 19:23:51', '', ''),
(3, 'lays', '08/28', '123', 6, 0, '2026-04-19 19:25:26', '', ''),
(4, 'amine', '0527', '532', 15, 1, '2026-04-20 00:33:10', '', ''),
(5, 'entreprise', '02/20', '123', 21, 1, '2026-04-20 10:38:24', '', ''),
(6, 'amine', '02/27', '4347', 21, 0, '2026-04-25 00:55:55', 'mastercard', 'pm_1TPsdb2eZvKYlo2ClLMqjjNp');

-- --------------------------------------------------------

--
-- Structure de la table `categorie`
--

CREATE TABLE `categorie` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `categorie` (`id`, `nom`) VALUES
(3, 'Informatique'),
(5, 'multimedia'),
(6, 'youssef');

-- --------------------------------------------------------

--
-- Structure de la table `category`
--

CREATE TABLE `category` (
  `id_category` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `priorite` varchar(255) NOT NULL,
  `user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `category` (`id_category`, `nom`, `type`, `priorite`, `user_id`) VALUES
(45, 'page test', 'OUTCOME', 'MOYENNE', 6),
(48, 'LOL', 'INCOME', 'HAUTE', 15),
(49, 'all', 'OUTCOME', 'BASSE', 15),
(50, 'hello', 'INCOME', 'HAUTE', 21),
(51, 'Paiement Formation', 'OUTCOME', 'HAUTE', 21),
(52, 'Investissement Projet', 'OUTCOME', 'HAUTE', 21),
(53, 'Trading Bourse', 'OUTCOME', 'MOYENNE', 21),
(54, 'bourse', 'OUTCOME', 'HAUTE', 21),
(55, 'income', 'INCOME', 'HAUTE', 21),
(56, 'all', 'OUTCOME', 'HAUTE', 21);

-- --------------------------------------------------------

--
-- Structure de la table `centre_formation`
--

CREATE TABLE `centre_formation` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `adresse` varchar(500) NOT NULL,
  `ville` varchar(50) NOT NULL,
  `latitude` decimal(10,7) NOT NULL,
  `longitude` decimal(10,7) NOT NULL,
  `description` longtext DEFAULT NULL,
  `telephone` varchar(30) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `site_web` varchar(255) DEFAULT NULL,
  `is_active` tinyint(4) NOT NULL,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `centre_formation` (`id`, `nom`, `adresse`, `ville`, `latitude`, `longitude`, `description`, `telephone`, `email`, `site_web`, `is_active`, `created_at`) VALUES
(1, 'FINORA Sfax', 'Route Tunis Km 5', 'Sfax', 34.7406000, 10.7603000, 'Centre spécialisé en finance', '+21690000000', 'sfax@finora.com', NULL, 1, '2026-04-16 16:28:51'),
(2, 'FINORA Nabeul', 'Centre ville', 'Nabeul', 36.4510000, 10.7350000, 'Formation en trading et investissement', '+21691111111', 'nabeul@finora.com', NULL, 1, '2026-04-16 16:28:51'),
(3, 'centre finora tunis', 'sabkha', 'Tunis', 37.7781503, 10.2596289, NULL, '+21694211133', 'hym@gmail.com', NULL, 1, '2026-04-17 20:15:32'),
(4, 'centre monastir', 'ksarhellal', 'Monastir', 35.6393434, 10.8828726, NULL, '+21694211133', 'hym@gmail.com', NULL, 1, '2026-04-18 02:29:50'),
(5, 'centre la marsa', 'corniche', 'Tunis', 36.8771680, 10.3291805, 'wow', '+21694321511', 'molkaomrani1412@gmail.com', NULL, 1, '2026-04-20 11:00:25'),
(6, 'centre esprit', 'corniche', 'Ariana', 36.8328593, 10.1532480, NULL, '+21694321511', 'molkaomrani1412@gmail.com', NULL, 1, '2026-04-20 11:59:20');

-- --------------------------------------------------------

--
-- Structure de la table `formation`
--

CREATE TABLE `formation` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `description` longtext DEFAULT NULL,
  `categorie` varchar(255) NOT NULL,
  `niveau` varchar(255) NOT NULL,
  `is_published` int(11) NOT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `pourquoi_acheter` longtext DEFAULT NULL,
  `prix` double DEFAULT NULL,
  `rating` double DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `rating_count` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `formation` (`id`, `titre`, `description`, `categorie`, `niveau`, `is_published`, `image_url`, `pourquoi_acheter`, `prix`, `rating`, `updated_at`, `rating_count`) VALUES
(1, 'Analyse technique', 'Apprendre les indicateurs techniques', 'Trading', 'Intermédiaire', 1, 'pexels-69e277b0986fd.jpg', NULL, 120, 4.5, NULL, 0),
(2, 'Investissement long terme', 'Stratégies d’investissement durable', 'Investissement', 'Avancé', 1, 'pexels-69e27781a99ae.jpg', NULL, 80, 4.2, NULL, 0),
(3, 'Crypto avancé', 'Gestion avancée des cryptomonnaies', 'Crypto', 'Avancé', 1, 'pexels-69e26f2f6c16f.jpg', NULL, 150, 3, NULL, 1),
(4, 'Bourseeeee', NULL, 'Trading, Obligations', 'Débutant', 1, 'pexels-69e254bd46976.jpg', 'because', 800, 4, NULL, 2),
(5, 'testtttt', NULL, 'Obligations', 'Intermédiaire', 1, 'pexels-69e5f838d3a17.jpg', 'you will need it', 500, 0, NULL, 0),
(6, 'validation', NULL, 'Marchés financiers, Psychologie du trader, Économie, Inflation', 'Débutant', 1, 'pexels-69e5f9795f23c.jpg', NULL, 200, 0, NULL, 0);

-- --------------------------------------------------------

--
-- Structure de la table `investment`
--

CREATE TABLE `investment` (
  `investment_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `category` varchar(50) NOT NULL,
  `location` varchar(255) NOT NULL,
  `estimated_value` decimal(10,2) NOT NULL,
  `risk_level` varchar(50) NOT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `description` longtext DEFAULT NULL,
  `status` varchar(50) NOT NULL,
  `created_at` datetime NOT NULL,
  `comments_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`comments_json`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `investment` (`investment_id`, `user_id`, `name`, `category`, `location`, `estimated_value`, `risk_level`, `image_url`, `description`, `status`, `created_at`, `comments_json`) VALUES
(1, 1, 'Immeuble Centre Tunis', 'IMMOBILIER', 'Tunis, Tunisie', 850000.00, 'MEDIUM', NULL, 'Immeuble de 6 étages en plein centre-ville avec un taux d\'occupation de 90%.', 'INACTIVE', '2026-02-20 07:00:00', NULL),
(2, 1, 'Startup FinTech Maghreb', 'STARTUP', 'Tunis, Tunisie', 200000.00, 'HIGH', NULL, 'Application de paiement mobile ciblant le marché nord-africain. Levée de fonds série A.', 'ACTIVE', '2026-02-21 09:00:00', NULL),
(3, 1, 'Ferme agricole bio', 'AGRICULTURE', 'Béja, Tunisie', 120000.00, 'LOW', NULL, 'Exploitation agricole bio de 15 hectares produisant olives et céréales.', 'ACTIVE', '2026-02-22 08:00:00', NULL),
(4, 1, 'Parc solaire 50kW', 'ENERGIE', 'Tozeur, Tunisie', 350000.00, 'LOW', NULL, 'Installation de panneaux solaires avec contrat de rachat d\'électricité sur 20 ans.', 'ACTIVE', '2026-02-23 10:00:00', NULL),
(5, 1, 'mimi', 'MAISON', 'tunis', 2000.00, 'MEDIUM', 'https://maisons-moyse.fr/wp-content/uploads/2024/03/constructeur-maison-sur-mesure.jpg', 'mmmmmmmmmm', 'ACTIVE', '2026-04-04 17:32:00', NULL),
(27, 1, 'momo', 'MAISON', '12', 2000.00, 'LOW', 'http://m', 'm', 'ACTIVE', '2026-04-05 20:18:57', NULL),
(28, 1, 'mimi12', 'HOTEL', 'ariana', 30000.00, 'LOW', 'https://picsum.photos/300', 'mmmmm', 'ACTIVE', '2026-04-05 20:24:37', NULL),
(29, 1, 'maryem', 'MAISON', 'tunis', 20000.00, 'LOW', 'https://picsum.photos/300', 'kkkkkk', 'ACTIVE', '2026-04-06 11:41:20', NULL),
(30, 1, 'maison ons', 'MAISON', 'korba', 29999999.98, 'LOW', 'petal-69dc3df6ef1a01.06260108.png', 'mmmmmmmmmmmmmm', 'ACTIVE', '2026-04-13 01:51:03', NULL),
(31, 1, 'Complexe Résidentiel (simulé)', 'IMMOBILIER', 'Quartier Nouveau', 4500000.00, 'LOW', NULL, 'Projet partenaire importé automatiquement depuis Quartier Nouveau', 'ACTIVE', '2026-04-16 00:33:15', NULL),
(32, 1, 'Boutique-hôtel (simulé)', 'HOTEL', 'Centre-ville', 2100000.00, 'MEDIUM', NULL, 'Projet partenaire importé automatiquement depuis Centre-ville', 'ACTIVE', '2026-04-16 01:07:25', NULL),
(33, 1, 'llouka', 'MAISON', 'korba', 20000.00, 'MEDIUM', 'b33beb50930c208ad87848eadf25df1c-69e26f75eb4856.97369040.png', 'mmmmmmmmmm', 'ACTIVE', '2026-04-17 18:35:49', NULL),
(34, 21, 'residence najla', 'IMMOBILIER', 'jaafer', 100000.00, 'MEDIUM', NULL, 'bonne etat', 'INACTIVE', '2026-04-18 20:34:15', NULL),
(35, 21, 'aaaa', 'STARTUP', 'beja', 4050.00, 'LOW', NULL, 'bonbon bon bon', 'ACTIVE', '2026-04-19 02:05:54', NULL),
(36, 21, 'dar \"AMAR\"', 'ENERGIE', 'jandouba', 4000.00, 'MEDIUM', 'capture-d-ecran-2026-02-06-170423-69e41f4e1f8712.98032214.png', 'oiuuiioiooi', 'ACTIVE', '2026-04-19 02:18:22', NULL),
(37, 21, 'villa blue', 'HOTEL', 'sidi bou', 4000.00, 'MEDIUM', 'capture-d-ecran-2026-02-09-181121-69e421ab3c7b41.42256727.png', 'mmmmmmmmmmmmmm', 'ACTIVE', '2026-04-19 02:28:27', NULL),
(38, 21, 'villa didon', 'MAISON', 'carthage', 900000.00, 'HIGH', 'capture-d-ecran-2026-02-06-165943-69e4226f9cdd64.42581958.png', 'hhhhhhhhhhhhhh', 'ACTIVE', '2026-04-19 02:31:43', NULL),
(39, NULL, 'Tech Hub Gafsa', 'STARTUP', 'Gafsa, TN', 450000.00, 'MEDIUM', NULL, 'Opportunité partenaire certifiée par FINORA. Origine: TechNodes', 'ACTIVE', '2026-04-19 03:07:05', NULL),
(40, NULL, 'AgriTech Valley', 'AGRICULTURE', 'Béja, TN', 650000.00, 'HIGH', NULL, 'Opportunité partenaire certifiée par FINORA. Origine: AgriNetwork', 'ACTIVE', '2026-04-19 03:21:38', NULL),
(41, 21, 'dar houyem', 'MAISON', 'Monastir', 550.00, 'MEDIUM', '582148227-1714532043308054-6531325058063789908-n-69e5eda4953216.76398761.jpg', 'homhom', 'ACTIVE', '2026-04-20 11:11:00', '[{\"username\":\"amin@gmail.com\",\"content\":\"c&#039;est un tres bon plan !\",\"userId\":15,\"userAvatar\":null,\"id\":\"69eb53ac3244d\",\"createdAt\":\"2026-04-24 13:27:40\"}]'),
(42, 21, 'aziz', 'MAISON', 'Monastir', 550.00, 'MEDIUM', '0e0eb889-4911-4934-a46b-6ad09a0a5f6c-69e6002e05b267.70440214.jpg', 'mmmmmmmmm', 'PENDING', '2026-04-20 12:30:06', NULL),
(43, NULL, 'Boutique-Hôtel Azure', 'HOTEL', 'Hammamet, TN', 2100000.00, 'MEDIUM', NULL, 'Opportunité partenaire certifiée par FINORA. Origine: Tourism Fund', 'ACTIVE', '2026-04-24 16:11:44', '[]');

-- --------------------------------------------------------

--
-- Structure de la table `investment_management`
--

CREATE TABLE `investment_management` (
  `management_id` int(11) NOT NULL,
  `investment_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `investment_type` varchar(255) NOT NULL,
  `amount_invested` decimal(10,2) NOT NULL,
  `ownership_percentage` decimal(5,2) NOT NULL,
  `start_date` date NOT NULL,
  `status` varchar(50) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `rating` int(11) DEFAULT NULL,
  `priority` varchar(20) DEFAULT NULL,
  `notes` longtext DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `investment_management` (`management_id`, `investment_id`, `user_id`, `investment_type`, `amount_invested`, `ownership_percentage`, `start_date`, `status`, `created_at`, `rating`, `priority`, `notes`) VALUES
(1, 1, 1, 'Equity', 50000.00, 5.88, '2026-02-20', 'CLOSED', '2026-02-20 08:00:00', NULL, 'MEDIUM', NULL),
(2, 1, 1, 'Equity', 85000.00, 10.00, '2026-02-21', 'ACTIVE', '2026-02-21 09:00:00', NULL, 'MEDIUM', NULL),
(3, 2, 1, 'Equity', 20000.00, 10.00, '2026-02-22', 'ACTIVE', '2026-02-22 10:00:00', NULL, 'MEDIUM', NULL),
(4, 2, 1, 'Equity', 15000.00, 7.50, '2026-02-23', 'ACTIVE', '2026-02-23 11:00:00', NULL, 'MEDIUM', NULL),
(5, 3, 1, 'Equity', 30000.00, 25.00, '2026-02-23', 'ACTIVE', '2026-02-23 12:00:00', NULL, 'MEDIUM', NULL),
(6, 4, 1, 'Equity', 70000.00, 20.00, '2026-02-24', 'ACTIVE', '2026-02-24 08:00:00', NULL, 'MEDIUM', NULL),
(9, 1, 1, 'startup', 2000.00, 30.00, '2026-04-23', 'ACTIVE', '2026-04-05 22:34:39', NULL, 'MEDIUM', NULL),
(11, 2, 1, 'startup', 2000.00, 30.00, '2026-04-09', 'ACTIVE', '2026-04-06 00:42:06', NULL, 'MEDIUM', NULL),
(13, 1, 1, 'startup', 5000.00, 20.00, '2026-04-07', 'CLOSED', '2026-04-06 10:42:38', NULL, 'MEDIUM', NULL),
(14, 30, 1, 'maison', 500000.00, 40.00, '2026-04-15', 'ACTIVE', '2026-04-14 00:16:58', NULL, 'MEDIUM', NULL),
(15, 29, 1, 'startup', 5000.00, 50.00, '2026-04-15', 'ACTIVE', '2026-04-14 00:34:09', NULL, 'MEDIUM', NULL),
(16, 30, 21, 'startup', 1400.00, 10.00, '2026-04-18', 'ACTIVE', NULL, NULL, 'MEDIUM', NULL),
(17, 40, 21, 'AGRICULTURE', 600000.00, 20.00, '2026-04-20', 'ACTIVE', '2026-04-19 04:12:24', NULL, 'MEDIUM', NULL),
(18, 40, 21, 'AGRICULTURE', 65000000.00, 100.00, '2026-04-19', 'CLOSED', '2026-04-19 06:16:55', NULL, 'MEDIUM', NULL),
(19, 39, 21, 'STARTUP', 1000.00, 2.00, '2026-04-20', 'ACTIVE', '2026-04-20 01:53:50', NULL, 'MEDIUM', NULL),
(20, 38, 21, 'MAISON', 900.00, 40.00, '2026-04-20', 'ACTIVE', '2026-04-20 11:06:56', NULL, 'MEDIUM', NULL),
(21, 41, 21, 'MAISON', 500.00, 10.00, '2026-04-21', 'ACTIVE', '2026-04-21 02:22:34', NULL, 'MEDIUM', NULL),
(22, 41, 15, 'MAISON', 500.00, 60.00, '2026-04-24', 'ACTIVE', '2026-04-24 13:08:19', 3, 'MEDIUM', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `investment_notification`
--

CREATE TABLE `investment_notification` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `message` longtext NOT NULL,
  `type` varchar(50) NOT NULL,
  `is_read` tinyint(4) NOT NULL,
  `created_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `investment_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `investment_wishlist`
--

CREATE TABLE `investment_wishlist` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `investment_id` int(11) NOT NULL,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `investment_wishlist` (`id`, `user_id`, `investment_id`, `created_at`) VALUES
(3, 21, 39, '2026-04-19 06:15:11'),
(4, 21, 40, '2026-04-19 06:18:52'),
(5, 21, 38, '2026-04-19 06:18:58'),
(6, 15, 40, '2026-04-19 20:55:15'),
(7, 15, 38, '2026-04-19 20:55:18'),
(8, 15, 36, '2026-04-19 21:30:38');

-- --------------------------------------------------------

--
-- Structure de la table `lesson`
--

CREATE TABLE `lesson` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `contenu` longtext DEFAULT NULL,
  `video_url` varchar(500) DEFAULT NULL,
  `ordre` int(11) NOT NULL,
  `duree_minutes` int(11) NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `formation_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `lesson` (`id`, `titre`, `contenu`, `video_url`, `ordre`, `duree_minutes`, `updated_at`, `formation_id`) VALUES
(1, 'Introduction Analyse Technique', 'Bases des graphiques et tendances', 'https://www.youtube.com/watch?v=CT051UOLul0', 1, 20, NULL, 1),
(2, 'Les indicateurs', 'RSI, MACD, Moyennes mobiles', NULL, 2, 30, NULL, 1),
(3, 'Pourquoi investir ?', 'Objectifs et stratégies', NULL, 1, 15, NULL, 2),
(4, 'Diversification', 'Réduire les risques', 'https://www.youtube.com/watch?v=_B_24GUWdSM', 2, 20, NULL, 2),
(5, 'Introduction crypto avancé', 'Wallets et sécurité', NULL, 1, 25, NULL, 3),
(6, 'aaaaaaa', 'jjjjj', NULL, 5, 70, NULL, 4),
(7, 'teeest', 'nnnn', NULL, 6, 25, NULL, 1),
(8, 'teeest', 'nnnn', NULL, 6, 25, NULL, 1),
(9, 'teeest', 'nnnn', NULL, 6, 25, NULL, 1),
(10, 'teeeeeeeeeest', NULL, NULL, 20, 20, NULL, 4),
(11, 'aaaaaaa', 'test test test test', 'https://www.youtube.com/watch?v=QDi_lWvi-1Y', 5, 40, NULL, 4),
(12, 'aaaaaaa', NULL, NULL, 5, 40, NULL, 6),
(13, 'aaaaaaa', NULL, NULL, 5, 40, NULL, 4);

-- --------------------------------------------------------

--
-- Structure de la table `margin_loan`
--

CREATE TABLE `margin_loan` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `montant_emprunte` double NOT NULL,
  `statut` varchar(50) NOT NULL,
  `date_emprunt` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `margin_loan` (`id`, `user_id`, `montant_emprunte`, `statut`, `date_emprunt`) VALUES
(1, 21, 500, 'ACTIF', '2026-04-21 00:06:36');

-- --------------------------------------------------------

--
-- Structure de la table `notification_bourse`
--

CREATE TABLE `notification_bourse` (
  `id` int(11) NOT NULL,
  `type` varchar(30) NOT NULL,
  `titre` varchar(200) NOT NULL,
  `message` longtext NOT NULL,
  `is_read` tinyint(4) NOT NULL,
  `created_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `action_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `notification_bourse` (`id`, `type`, `titre`, `message`, `is_read`, `created_at`, `user_id`, `action_id`) VALUES
(1, 'vente', '💰 Vente confirmée', 'Vous avez vendu 1 action(s) apple (AAPL). +267.95 TND crédités dans votre wallet.', 1, '2026-04-20 21:26:49', 21, 1),
(2, 'vente', '💰 Vente confirmée', 'Vous avez vendu 1 action(s) MICROSOFT (MIC). +10.00 TND crédités dans votre wallet.', 1, '2026-04-20 21:32:57', 21, 4),
(3, 'achat', '✅ Achat confirmé', 'Vous avez acheté 1 action(s) MICROSOFT (MIC) pour 20.00 TND.', 1, '2026-04-20 21:44:14', 21, 4),
(4, 'vente', '💰 Vente confirmée', 'Vous avez vendu 1 action(s) apple (AAPL). +267.95 TND crédités dans votre wallet.', 1, '2026-04-20 21:49:20', 21, 1),
(5, 'achat', '✅ Achat confirmé', 'Vous avez acheté 1 action(s) apple (AAPL) pour 277.95 TND.', 1, '2026-04-25 01:38:13', 15, 1),
(6, 'vente', '💰 Vente confirmée', 'Vous avez vendu 1 action(s) apple (AAPL). +267.95 TND crédités dans votre wallet.', 0, '2026-04-25 22:02:51', 21, 1);

-- --------------------------------------------------------

--
-- Structure de la table `quiz_result`
--

CREATE TABLE `quiz_result` (
  `id` int(11) NOT NULL,
  `student_name` varchar(255) NOT NULL,
  `lesson_id` int(11) NOT NULL,
  `lesson_title` varchar(255) NOT NULL,
  `formation_title` varchar(255) NOT NULL,
  `score` int(11) NOT NULL,
  `passed` int(11) NOT NULL,
  `taken_at` datetime DEFAULT NULL,
  `fraud_suspected` int(11) NOT NULL DEFAULT 0,
  `fraud_explanation` longtext DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `quiz_result` (`id`, `student_name`, `lesson_id`, `lesson_title`, `formation_title`, `score`, `passed`, `taken_at`, `fraud_suspected`, `fraud_explanation`, `user_id`) VALUES
(1, 'Ali', 1, 'Introduction Analyse Technique', 'Analyse technique', 90, 1, '2026-04-16 16:28:51', 0, NULL, NULL),
(2, 'Sami', 2, 'Les indicateurs', 'Analyse technique', 60, 1, '2026-04-16 16:28:51', 0, NULL, NULL),
(3, 'Mouna', 3, 'Pourquoi investir ?', 'Investissement long terme', 50, 0, '2026-04-16 16:28:51', 0, NULL, NULL),
(4, 'layes', 1, 'Introduction Analyse Technique', 'Analyse technique', 80, 1, '2026-04-17 22:00:58', 0, NULL, 21),
(5, 'layes', 2, 'Les indicateurs', 'Analyse technique', 0, 0, '2026-04-18 03:03:19', 1, 'Nous avons détecté un comportement suspect à l\'occasion de ce quiz, en raison d\'une sortie du mode plein écran, qui suggère que vous avez pu accéder à d\'autres ressources extérieures pendant l\'examen. De plus, les réponses anormalement rapides, soit 3 fois en moins de 3 secondes, soulèvent des inquiétudes quant à l\'utilisation de méthodes de triche.', 21),
(6, 'layes', 2, 'Les indicateurs', 'Analyse technique', 20, 0, '2026-04-18 03:03:49', 1, 'Nous avons détecté un comportement suspect lors de votre participation au quiz, en particulier le fait que vous avez répondu à 3 questions en moins de 3 secondes. Cette rapidité anormale de réponse soulève des doutes quant à votre capacité à réfléchir et à répondre de manière réfléchie aux questions, ce qui est incompatible avec les principes de l\'examen.', 21),
(7, 'layes', 2, 'Les indicateurs', 'Analyse technique', 20, 0, '2026-04-18 03:07:38', 1, 'Nous avons détecté un comportement suspect lors de votre participation au quiz, qui pourrait indiquer une tentative de triche. Les statistiques indiquent que vous avez effectué une sortie du mode plein écran, ce qui pourrait être une tentative de consultation d\'une autre ressource pendant l\'examen. De plus, les réponses anormalement rapides que vous avez fournies (< 3 secondes) pour 3 questions suggèrent également une utilisation d\'une méthode non autorisée pour obtenir des réponses.', 21),
(8, 'layes', 1, 'Introduction Analyse Technique', 'Analyse technique', 20, 0, '2026-04-18 04:52:00', 1, 'Nous avons détecté un comportement suspect lors de votre participation au quiz, qui pourrait être lié à une tentative de triche. Les pertes de focus fréquentes (2 changements d\'onglets) et les sorties du mode plein écran (2 fois) suggèrent que vous avez pu consulter des ressources externes ou discuter avec d\'autres étudiants. De plus, les réponses anormalement rapides (3 fois en moins de 3 secondes) laissent présager un accès à des informations non autorisées.', 21),
(9, 'amine', 2, 'Les indicateurs', 'Analyse technique', 20, 0, '2026-04-19 18:04:00', 0, NULL, 15),
(10, 'amine', 2, 'Les indicateurs', 'Analyse technique', 0, 0, '2026-04-19 18:05:28', 1, 'Nous avons détecté un comportement suspect lors de votre participation au quiz. Les statistiques indiquent que vous avez quitté le mode plein écran une seule fois, ce qui pourrait suggérer que vous avez tenté de consulter d\'autres sources pendant l\'examen. De plus, vous avez répondu à une question en moins de 3 secondes, ce qui est considéré comme un temps de réponse anormalement rapide.', 15),
(11, 'amine', 2, 'Les indicateurs', 'Analyse technique', 40, 0, '2026-04-19 18:06:29', 1, 'Nous avons détecté un comportement suspect lors de votre participation au quiz, en particulier les 4 réponses anormalement rapides (inférieures à 3 secondes) qui soulèvent des inquiétudes quant à l\'utilisation de ressources externes ou à une aide non autorisée. Cette fréquence de réponses rapides suggère un comportement inhabituel qui pourrait être lié à une forme de triche.', 15),
(12, 'amine', 2, 'Les indicateurs', 'Analyse technique', 20, 0, '2026-04-19 18:07:28', 0, NULL, 15),
(13, 'Invité', 1, 'Introduction Analyse Technique', 'Analyse technique', 0, 0, '2026-04-20 00:18:09', 1, 'Nous avons détecté des réponses anormalement rapides, avec 4 réponses fournies en moins de 3 secondes. Cette fréquence élevée de réponses rapides suggère que vous auriez pu avoir accès à des informations ou à des ressources non autorisées, ce qui constitue une violation des règles de l\'examen. Cette suspicion de triche sera examinée plus en détail.', NULL),
(14, 'layes', 7, 'teeest', 'Analyse technique', 20, 0, '2026-04-20 10:50:09', 1, 'Nous avons détecté un comportement suspect au cours de votre participation au quiz, notamment une fréquence anormale de changements d\'onglets (2 fois), une sortie du mode plein écran (1 fois) et une rapidité de réponse inusitée (3 réponses en moins de 3 secondes). Ces anomalies suggèrent potentiellement une utilisation de ressources externes non autorisées, telles que des notes ou des outils de recherche en ligne, qui pourraient compromettre l\'intégrité du quiz.', 21),
(15, 'layes', 7, 'teeest', 'Analyse technique', 20, 0, '2026-04-20 10:50:53', 1, 'Nous avons détecté des réponses anormalement rapides sur votre compte, à savoir 4 réponses en moins de 3 secondes. Cette fréquence élevée de réponses rapides suggère que vous auriez pu avoir accès à des informations non autorisées, telles que des notes ou des réponses prévues. Cette activité est considérée comme une forme de triche et sera prise en compte dans l\'évaluation finale de votre performance.', 21),
(16, 'layes', 7, 'teeest', 'Analyse technique', 0, 0, '2026-04-20 10:51:28', 1, 'Nous avons détecté des réponses anormalement rapides, avec 3 réponses fournies en moins de 3 secondes, ce qui soulève des suspicions quant à l\'utilisation de méthodes non autorisées pour répondre aux questions. Cette fréquence de réponses rapides est considérée comme anormale et peut indiquer que vous avez pu avoir accès à des informations non autorisées ou utiliser des outils de recherche pendant le quiz.', 21),
(17, 'layes', 8, 'teeest', 'Analyse technique', 20, 0, '2026-04-20 12:04:43', 1, 'Nous avons détecté des comportements inhabituels lors de votre participation au quiz, notamment 1 changement d\'onglet, 1 sortie du mode plein écran et 4 réponses anormalement rapides en moins de 3 secondes. Ces actions suggèrent que vous pourriez avoir utilisé des ressources externes ou des méthodes de triche pour répondre aux questions, ce qui est interdit dans notre environnement de quiz.', 21),
(18, 'layes', 8, 'teeest', 'Analyse technique', 0, 0, '2026-04-20 12:05:15', 0, NULL, 21);

-- --------------------------------------------------------

--
-- Structure de la table `rating`
--

CREATE TABLE `rating` (
  `id` int(11) NOT NULL,
  `note` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  `appel_offre_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `rating` (`id`, `note`, `created_at`, `appel_offre_id`, `user_id`) VALUES
(1, 4, '2026-04-04 23:56:01', 4, NULL),
(2, 2, '2026-04-04 23:56:40', 3, NULL),
(3, 5, '2026-04-04 23:56:55', 4, NULL),
(4, 5, '2026-04-04 23:57:27', 3, NULL),
(5, 1, '2026-04-04 23:57:40', 3, NULL),
(6, 2, '2026-04-05 00:31:02', 4, NULL),
(7, 3, '2026-04-05 00:31:19', 3, NULL),
(8, 4, '2026-04-06 13:01:23', 8, NULL),
(9, 5, '2026-04-13 02:50:03', 9, NULL),
(10, 4, '2026-04-18 00:54:44', 14, NULL),
(11, 4, '2026-04-18 04:22:07', 12, NULL),
(12, 4, '2026-04-18 18:25:33', 14, NULL),
(13, 4, '2026-04-19 19:16:05', 14, NULL),
(14, 4, '2026-04-20 04:56:48', 15, NULL),
(15, 4, '2026-04-20 05:09:50', 22, NULL),
(16, 4, '2026-04-20 05:31:47', 22, 21),
(17, 3, '2026-04-20 06:40:59', 23, 21),
(18, 5, '2026-04-20 07:10:44', 22, 26),
(19, 4, '2026-04-20 13:07:39', 25, 21),
(20, 4, '2026-04-20 13:28:19', 26, 21),
(21, 1, '2026-04-20 13:29:15', 26, 26);

-- --------------------------------------------------------

--
-- Structure de la table `rating_centre`
--

CREATE TABLE `rating_centre` (
  `id` int(11) NOT NULL,
  `note` int(11) NOT NULL,
  `commentaire` longtext DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `centre_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `rating_centre` (`id`, `note`, `commentaire`, `created_at`, `user_id`, `centre_id`) VALUES
(1, 3, 'cool', '2026-04-25 16:25:57', 21, 1);

-- --------------------------------------------------------

--
-- Structure de la table `recharge_requests`
--

CREATE TABLE `recharge_requests` (
  `id` int(11) NOT NULL,
  `amount` double NOT NULL,
  `status` varchar(20) NOT NULL,
  `otp` varchar(10) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `card_id` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  `confirmed_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `recharge_requests` (`id`, `amount`, `status`, `otp`, `user_id`, `card_id`, `created_at`, `confirmed_at`) VALUES
(14, 400, 'COMPLETED', NULL, 6, 1, '2026-04-18 18:14:48', '2026-04-18 18:14:48'),
(15, 200, 'PENDING', NULL, 6, 1, '2026-04-18 18:17:50', NULL),
(16, 200, 'COMPLETED', NULL, 6, 1, '2026-04-18 18:19:33', '2026-04-18 18:19:33'),
(17, 400, 'COMPLETED', NULL, 6, 1, '2026-04-18 18:20:08', '2026-04-18 18:20:08'),
(18, 300, 'COMPLETED', NULL, 6, 1, '2026-04-18 18:33:20', '2026-04-18 18:33:22'),
(19, 300, 'COMPLETED', NULL, 6, 1, '2026-04-18 19:05:17', '2026-04-18 19:05:19'),
(20, 120, 'COMPLETED', NULL, 6, 1, '2026-04-18 19:17:54', '2026-04-18 19:17:56'),
(21, 180, 'COMPLETED', NULL, 6, 1, '2026-04-18 19:23:32', '2026-04-18 19:23:34'),
(22, 460, 'COMPLETED', NULL, 1, 2, '2026-04-18 19:24:05', '2026-04-18 19:24:06'),
(23, 1000, 'FAILED', NULL, 6, 1, '2026-04-19 14:42:18', NULL),
(24, 1000, 'FAILED', NULL, 6, 1, '2026-04-19 14:42:20', NULL),
(25, 1000, 'FAILED', NULL, 6, 1, '2026-04-19 14:42:25', NULL),
(26, 1000, 'FAILED', NULL, 6, 1, '2026-04-19 14:42:27', NULL),
(27, 1000, 'FAILED', NULL, 6, 1, '2026-04-19 14:45:56', NULL),
(28, 1000, 'FAILED', NULL, 6, 1, '2026-04-19 14:46:01', NULL),
(29, 1000, 'FAILED', NULL, 6, 1, '2026-04-19 14:46:03', NULL),
(30, 1000, 'FAILED', NULL, 6, 1, '2026-04-19 14:46:05', NULL),
(31, 100, 'FAILED', NULL, 6, 3, '2026-04-19 19:26:03', NULL),
(32, 1000, 'FAILED', NULL, 15, 4, '2026-04-20 00:33:28', NULL),
(33, 1000, 'FAILED', NULL, 15, 4, '2026-04-20 00:44:26', NULL),
(34, 1000, 'FAILED', NULL, 15, 4, '2026-04-20 00:46:09', NULL),
(35, 1000, 'COMPLETED', NULL, 15, 4, '2026-04-20 00:46:12', '2026-04-20 00:47:18'),
(36, 1000, 'COMPLETED', NULL, 15, 4, '2026-04-20 00:47:19', '2026-04-20 00:47:24'),
(37, 1040, 'PENDING', '729057', 15, 4, '2026-04-20 00:50:11', NULL),
(38, 50, 'COMPLETED', NULL, 15, 4, '2026-04-20 00:55:04', '2026-04-20 00:55:05'),
(39, 145, 'COMPLETED', NULL, 21, 5, '2026-04-20 10:38:35', '2026-04-20 10:38:37'),
(40, 500, 'COMPLETED', NULL, 21, 6, '2026-04-25 19:46:06', '2026-04-25 19:46:08'),
(41, 100, 'PENDING', NULL, 21, 6, '2026-04-25 21:39:44', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `transaction_bourse`
--

CREATE TABLE `transaction_bourse` (
  `id_transaction` int(11) NOT NULL,
  `type_transaction` varchar(20) NOT NULL,
  `quantite` int(11) NOT NULL,
  `prix_unitaire` double NOT NULL,
  `montant_total` double NOT NULL,
  `commission` double NOT NULL,
  `date_transaction` datetime NOT NULL,
  `acteur_role` varchar(50) DEFAULT NULL,
  `acteur_label` varchar(100) DEFAULT NULL,
  `action_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `transaction_bourse` (`id_transaction`, `type_transaction`, `quantite`, `prix_unitaire`, `montant_total`, `commission`, `date_transaction`, `acteur_role`, `acteur_label`, `action_id`, `user_id`) VALUES
(1, 'ACHAT', 1, 272.95, 272.95, 1.36, '2026-02-27 01:11:17', 'INVESTISSEUR', 'USER_STATIC', 1, 1),
(2, 'VENTE', 1, 272.95, 272.95, 1.36, '2026-02-27 01:11:21', 'INVESTISSEUR', 'USER_STATIC', 1, 1),
(3, 'ACHAT', 1, 141.25, 141.25, 2.19, '2026-02-27 01:46:37', 'INVESTISSEUR', 'USER_STATIC', 3, 1),
(4, 'ACHAT', 2, 272.95, 545.9, 8.46, '2026-02-27 02:30:42', 'INVESTISSEUR', 'USER_STATIC', 1, 1),
(5, 'VENTE', 1, 272.95, 272.95, 4.23, '2026-02-27 02:30:48', 'INVESTISSEUR', 'USER_STATIC', 1, 1),
(6, 'ACHAT', 1, 272.95, 272.95, 4.23, '2026-02-27 02:41:42', 'INVESTISSEUR', 'USER_STATIC', 1, 1),
(7, 'VENTE', 1, 272.95, 272.95, 4.23, '2026-02-27 02:41:55', 'INVESTISSEUR', 'USER_STATIC', 1, 1),
(8, 'ACHAT', 2, 141.25, 282.5, 4.38, '2026-02-27 02:53:32', 'INVESTISSEUR', 'USER_STATIC', 3, 1),
(9, 'ACHAT', 1, 272.95, 272.95, 4.23, '2026-02-28 00:08:30', 'INVESTISSEUR', 'aziz', 1, 1),
(10, 'ACHAT', 2, 272.95, 545.9, 8.46, '2026-02-28 23:34:39', 'INVESTISSEUR', 'aziz', 1, 1),
(11, 'VENTE', 2, 272.95, 545.9, 8.46, '2026-02-28 23:34:48', 'INVESTISSEUR', 'aziz', 1, 1),
(12, 'ACHAT', 7, 272.95, 1910.6499999999999, 29.62, '2026-03-01 04:59:31', 'INVESTISSEUR', 'aziz', 1, 1),
(13, 'ACHAT', 1, 272.95, 272.95, 4.23, '2026-03-01 05:02:02', 'INVESTISSEUR', 'aziz', 1, 1),
(14, 'VENTE', 1, 272.95, 272.95, 4.23, '2026-03-01 05:02:15', 'INVESTISSEUR', 'aziz', 1, 1),
(15, 'ACHAT', 1, 272.95, 272.95, 0, '2026-04-16 14:58:12', NULL, NULL, 1, 15),
(16, 'ACHAT', 1, 141.25, 141.25, 0, '2026-04-18 18:07:56', NULL, NULL, 3, NULL),
(17, 'ACHAT', 2, 15, 30, 0, '2026-04-18 18:09:25', NULL, NULL, 4, 15),
(18, 'ACHAT', 1, 15, 15, 0, '2026-04-19 02:11:57', NULL, NULL, 4, 16),
(19, 'ACHAT', 2, 15, 30, 0, '2026-04-20 01:17:28', NULL, NULL, 4, 21),
(20, 'ACHAT', 4, 15, 60, 0, '2026-04-20 01:26:45', NULL, NULL, 4, 21),
(21, 'ACHAT', 1, 15, 15, 0, '2026-04-20 01:34:47', NULL, NULL, 4, 21),
(22, 'ACHAT', 2, 272.95, 545.9, 0, '2026-04-20 01:48:19', NULL, NULL, 1, 21),
(23, 'ACHAT', 2, 272.95, 550.9, 5, '2026-04-20 02:14:58', NULL, NULL, 1, 21),
(24, 'ACHAT', 1, 272.95, 277.95, 5, '2026-04-20 11:15:32', NULL, NULL, 1, 21),
(25, 'ACHAT', 1, 272.95, 277.95, 5, '2026-04-20 21:12:42', NULL, NULL, 1, 21),
(26, 'ACHAT', 1, 272.95, 277.95, 5, '2026-04-20 21:16:05', NULL, NULL, 1, 21),
(27, 'VENTE', 1, 272.95, 267.95, 5, '2026-04-20 21:26:49', NULL, NULL, 1, 21),
(28, 'VENTE', 1, 15, 10, 5, '2026-04-20 21:32:57', NULL, NULL, 4, 21),
(29, 'ACHAT', 1, 15, 20, 5, '2026-04-20 21:44:14', NULL, NULL, 4, 21),
(30, 'VENTE', 1, 272.95, 267.95, 5, '2026-04-20 21:49:20', NULL, NULL, 1, 21),
(31, 'ACHAT', 1, 272.95, 277.95, 5, '2026-04-25 01:38:13', NULL, NULL, 1, 15),
(32, 'VENTE', 1, 272.95, 267.95, 5, '2026-04-25 22:02:50', NULL, NULL, 1, 21);

-- --------------------------------------------------------

--
-- Structure de la table `transaction_wallet`
--

CREATE TABLE `transaction_wallet` (
  `id_transaction` int(11) NOT NULL,
  `nom_transaction` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `montant` double NOT NULL,
  `date_transaction` datetime NOT NULL,
  `source` varchar(255) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `is_active` tinyint(4) NOT NULL DEFAULT 1,
  `category_id` int(11) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACCEPTED'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `transaction_wallet` (`id_transaction`, `nom_transaction`, `type`, `montant`, `date_transaction`, `source`, `user_id`, `is_active`, `category_id`, `status`) VALUES
(107, 'outcome1', 'OUTCOME', -200, '2026-04-18 00:00:00', 'manual', 6, 1, 45, 'ACCEPTED'),
(120, 'dd', 'INCOME', 1500, '2026-04-19 14:40:00', 'manual', 15, 1, 48, 'ACCEPTED'),
(121, 'ddf', 'OUTCOME', -700, '2026-04-19 15:04:00', 'manual', 15, 1, 48, 'ACCEPTED'),
(122, 'salaire', 'OUTCOME', -7000, '2026-04-19 23:34:00', 'manual', 15, 1, 49, 'ACCEPTED'),
(123, 'courses', 'INCOME', 150, '2026-04-25 23:36:00', 'manual', 15, 1, 48, 'ACCEPTED'),
(126, 'indomi', 'INCOME', 7000, '2026-04-19 23:52:00', 'manual', 15, 1, 48, 'ACCEPTED'),
(128, 'indomi', 'INCOME', 7000, '2026-04-20 00:50:00', 'manual', 21, 1, 50, 'ACCEPTED'),
(129, 'Achat Formation: Crypto avancé', 'OUTCOME', -150, '2026-04-20 01:51:00', NULL, 21, 1, 51, 'ACCEPTED'),
(130, 'Investissement: Tech Hub Gafsa', 'OUTCOME', -1000, '2026-04-20 01:53:00', NULL, 21, 1, 52, 'ACCEPTED'),
(131, 'Achat Formation: Investissement long terme', 'OUTCOME', -80, '2026-04-20 02:14:28', NULL, 21, 1, 51, 'ACCEPTED'),
(132, 'Achat de 2 actions AAPL', 'OUTCOME', -550.9, '2026-04-20 02:14:58', NULL, 21, 1, 53, 'ACCEPTED'),
(134, 'Investissement: villa didon', 'OUTCOME', -900, '2026-04-20 11:06:56', NULL, 21, 1, 52, 'ACCEPTED'),
(135, 'Achat de 1 actions AAPL', 'OUTCOME', -277.95, '2026-04-20 11:15:32', NULL, 21, 1, 53, 'ACCEPTED'),
(136, 'Achat Formation: validation', 'OUTCOME', -200, '2026-04-20 12:09:57', NULL, 21, 1, 51, 'ACCEPTED'),
(137, 'Achat de 1 actions AAPL', 'OUTCOME', -277.95, '2026-04-20 21:12:42', NULL, 21, 1, 53, 'ACCEPTED'),
(138, 'Achat de 1 actions AAPL', 'OUTCOME', -277.95, '2026-04-20 21:16:05', NULL, 21, 1, 53, 'ACCEPTED'),
(139, 'Vente de 1 actions AAPL', 'INCOME', 267.95, '2026-04-20 21:26:49', NULL, 21, 1, 53, 'ACCEPTED'),
(140, 'Vente de 1 actions MIC', 'INCOME', 10, '2026-04-20 21:32:57', NULL, 21, 1, 53, 'ACCEPTED'),
(141, 'Achat de 1 actions MIC', 'OUTCOME', -20, '2026-04-20 21:44:14', NULL, 21, 1, 53, 'ACCEPTED'),
(142, 'Vente de 1 actions AAPL', 'INCOME', 267.95, '2026-04-20 21:49:20', NULL, 21, 1, 53, 'ACCEPTED'),
(143, 'Investissement: dar houyem', 'OUTCOME', -500, '2026-04-21 02:22:34', NULL, 21, 1, 52, 'ACCEPTED'),
(144, 'Investissement: dar houyem', 'OUTCOME', -500, '2026-04-24 13:08:19', NULL, 15, 1, 52, 'ACCEPTED'),
(147, 'dd', 'INCOME', 100, '2026-04-24 21:58:00', 'manual', 21, 1, 50, 'ACCEPTED'),
(148, 'ddt', 'OUTCOME', -100, '2026-03-19 21:59:00', 'manual', 21, 1, 52, 'ACCEPTED'),
(155, 'Achat de 1 actions AAPL', 'OUTCOME', -277.95, '2026-04-25 01:38:13', NULL, 15, 1, 53, 'ACCEPTED'),
(156, 'income 5100', 'INCOME', 5100, '2026-05-03 17:04:00', 'manual', 21, 1, 55, 'ACCEPTED'),
(157, 'income 6000', 'OUTCOME', -6000, '2026-04-25 17:21:00', 'manual', 21, 1, 54, 'REJECTED'),
(158, 'out', 'OUTCOME', -6000, '2026-04-11 17:52:00', 'manual', 21, 1, 51, 'PENDING'),
(159, 'Recharge Portfolio (API Stripe)', 'INCOME', 500, '2026-04-25 19:46:08', 'STRIPE_GW', 21, 1, 45, 'ACCEPTED'),
(160, 'Finora Pay - Envoi à amin@gmail.com', 'OUTCOME', -200, '2026-04-25 21:48:25', NULL, 21, 1, 45, 'ACCEPTED'),
(161, 'Finora Pay - Reçu de molkaomrani1412@gmail.com', 'INCOME', 200, '2026-04-25 21:48:25', NULL, 15, 1, 48, 'ACCEPTED'),
(162, 'testi', 'INCOME', 10, '2026-04-25 20:54:00', 'manual', 21, 1, 55, 'ACCEPTED'),
(163, 'Vente de 1 actions AAPL', 'INCOME', 267.95, '2026-04-25 22:02:50', NULL, 21, 1, 53, 'ACCEPTED'),
(164, 'Achat Formation: testtttt', 'OUTCOME', -500, '2026-04-25 22:04:44', NULL, 21, 1, 51, 'ACCEPTED');

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `email` varchar(180) NOT NULL,
  `mot_de_passe` varchar(255) DEFAULT NULL,
  `role` varchar(20) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` varchar(100) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `is_verified` tinyint(4) NOT NULL,
  `balance` decimal(10,2) NOT NULL DEFAULT 0.00,
  `current_session_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `users` (`id`, `username`, `email`, `mot_de_passe`, `role`, `phone`, `address`, `date_of_birth`, `created_at`, `image`, `is_verified`, `balance`, `current_session_id`) VALUES
(1, '', '', '', 'USER', NULL, NULL, NULL, '2026-04-05 12:06:07', NULL, 1, 0.00, NULL),
(6, 'Imen Neifar', 'naiferimen9@gmail.com', '$2a$12$XhOmLJl03tZFs.ZnAbYYOOMZbS6Ko5aBJoDs955d/VivhfkfrKGim', 'USER', '28093904', 'ariana', '2026-03-13', '2026-03-01 16:40:05', NULL, 0, 0.00, NULL),
(10, 'imenadmin', 'imen.neifar@esprit.tn', '$2y$13$VhLV/7Ai4hNYBJXRtQbyEOzvH5B2h0UNohS9j3dS0X/T2OGi.4bZm', 'ADMIN', '95955878', NULL, NULL, '2026-03-01 16:42:26', NULL, 1, 0.00, NULL),
(11, 'louka', 'louka@gmail.com', '$2a$12$xSRXr9FCsbxnCFhkF.26ZeGymhrdg9Umg9Qa6Su9LNKW426Ga2cHK', 'ENTREPRISE', '28093904', 'ariana', '2005-05-10', '2026-03-01 17:38:00', NULL, 1, 0.00, NULL),
(12, 'imenee', 'imen2@gmail.com', '$2y$13$zN81tOjoInLK7JECbCK9veiU86Eo2OMK3Kum8SOM4btTbmxC5GJxC', 'USER', '98115773', 'ariana', '2004-05-10', '2026-04-15 01:32:49', NULL, 1, 0.00, 'met68m4lnj5dos6ahrt5ajdev9'),
(13, 'molka', 'molka1234@gmail.com', '$2y$13$MgDy5ctJcKqDuWrNez.VOu.F7U2RjPi5Q7Vrzh8phQMQZdHtdB5PK', 'ENTREPRISE', '94321511', 'ariana', '2004-05-10', '2026-04-15 01:58:31', NULL, 0, 0.00, NULL),
(14, 'loka', 'lok1412@gmail.com', '$2y$13$fFygEvSn/8Q792GTJ3FJn.WnSBpyIS0NAg1tzn/zpp1wrDUbMTmoe', 'USER', '98745632', 'Résidence najla, Jaafer 1', '2004-05-11', '2026-04-15 22:27:21', NULL, 0, 0.00, NULL),
(15, 'amine', 'amin@gmail.com', '$2y$13$IGp0vV8IrWYGsErUsIViWesLGodOM2Rnp8kRm09E8ICAO30whfM86', 'USER', '98741257', 'Résidence najla, Jaafer 1', '1997-05-10', '2026-04-15 22:32:59', NULL, 1, 0.00, ''),
(16, 'Admin', 'admin@finora.com', '$2y$10$EnUiD4SjiC/rX58/52PM0ehMlZRHrz3mM8s7VTOso2okLaKys4e/m', 'ADMIN', '95955878', 'ariana', NULL, '2026-04-16 01:35:00', '69e5b0f526b60.png', 1, 0.00, ''),
(19, 'JeanD', 'jean@gmail.com', '$2y$13$s3bp7KPvCACdLXKpfZCl9uGF/4wpPoryH3QfLFu0c1bRwwlS7hq5u', 'ENTREPRISE', '98445566', 'tunisie', '2001-06-10', '2026-04-16 12:46:57', NULL, 0, 0.00, NULL),
(20, 'test', 'test@gmail.com', '$2y$13$MF0U8SNz.DKCVqDQUc./beQBWZoGnqb4hHCqsQHdA1FB1GA0vQrXO', 'ENTREPRISE', '98445566', 'tunisie', '1988-11-29', '2026-04-16 12:49:18', NULL, 0, 0.00, NULL),
(21, 'layes', 'molkaomrani1412@gmail.com', '$2y$13$wPQgR5JQWZo529R1/Zgrs.dlxzys9QTfJaN55ZLc7xOzIYwqXNUy6', 'ENTREPRISE', '95955878', 'tunisie', '2001-08-10', '2026-04-16 12:54:05', NULL, 1, 0.00, ''),
(22, 'islem', 'islemnaifer99@gmail.com', '$2y$13$GtgKwlZXyZI8pRx4KleeG.zn8tWQk4uFom9ozowWFFkGxHmOgu.BW', 'ENTREPRISE', '98445566', 'tunisie', '2001-02-01', '2026-04-16 13:18:34', NULL, 0, 0.00, NULL),
(23, 'polo', 'molkaomrani200@gmail.com', '$2y$13$GCLRiI5gT1sfybDOqZ9DIeNEZi0FU/mgxvnCrJis1mTDuOi.xO67.', 'USER', '98745632', 'ariana', '2001-05-05', '2026-04-16 13:20:37', NULL, 0, 0.00, NULL),
(24, 'houyem', 'houyemkhalifa8@gmail.com', '$2y$13$rXYi56E/XiYBzE6iGAHcO.wa18IUibIK.iNEI7gLZ75OO5RB6RwGW', 'USER', '98745632', 'tunisie', '2003-08-08', '2026-04-16 16:52:39', NULL, 0, 0.00, NULL),
(26, 'youssef', 'jalloulyoussef5@gmail.com', '$2y$13$Kx/AYokBL3UJfN2nBNNuSugf9R80tt7oWJwxo3GOeywhssUnEYZcm', 'ENTREPRISE', '95955878', 'Ariana', '2012-01-04', '2026-04-20 07:00:34', NULL, 1, 0.00, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `user_api_key`
--

CREATE TABLE `user_api_key` (
  `id` int(11) NOT NULL,
  `token` varchar(255) NOT NULL,
  `user_id` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  `expires_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `user_biometrics`
--

CREATE TABLE `user_biometrics` (
  `id` int(11) NOT NULL,
  `face_embedding` longtext DEFAULT NULL,
  `is_active` tinyint(4) NOT NULL,
  `created_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `user_favorite_appels`
--

CREATE TABLE `user_favorite_appels` (
  `user_id` int(11) NOT NULL,
  `appel_offre_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `user_favorite_appels` (`user_id`, `appel_offre_id`) VALUES
(21, 22),
(26, 24);

-- --------------------------------------------------------

--
-- Structure de la table `user_formation_purchased`
--

CREATE TABLE `user_formation_purchased` (
  `user_id` int(11) NOT NULL,
  `formation_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `user_formation_purchased` (`user_id`, `formation_id`) VALUES
(15, 1),
(15, 3),
(21, 1),
(21, 2),
(21, 3),
(21, 4),
(21, 5),
(21, 6);

-- --------------------------------------------------------

--
-- Structure de la table `user_formation_wishlist`
--

CREATE TABLE `user_formation_wishlist` (
  `user_id` int(11) NOT NULL,
  `formation_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `user_formation_wishlist` (`user_id`, `formation_id`) VALUES
(15, 4),
(21, 1),
(21, 3);

-- --------------------------------------------------------

--
-- Structure de la table `wishlist`
--

CREATE TABLE `wishlist` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `price` double NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `wishlist` (`id`, `name`, `price`, `user_id`) VALUES
(12, 'hhh', 100, 1),
(16, 'mmm', 200, 1),
(17, 'hihih', 120, 1),
(20, 'car', 50000, 6),
(22, 'kia picanto', 500000, 15),
(24, 'ALIMENTATION', 100, 15),
(25, 'car', 10000, 21);

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `action`
--
ALTER TABLE `action`
  ADD PRIMARY KEY (`id_action`),
  ADD UNIQUE KEY `UNIQ_47CC8C922B57F8D4` (`symbole`),
  ADD KEY `IDX_47CC8C92FBF509F1` (`bourse_id`);

--
-- Index pour la table `action_news`
--
ALTER TABLE `action_news`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_6B32225E61FB397F` (`action_id`);

--
-- Index pour la table `appel_offre`
--
ALTER TABLE `appel_offre`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_BC56FD47BCF5E72D` (`categorie_id`),
  ADD KEY `IDX_BC56FD47B03A8386` (`created_by_id`);

--
-- Index pour la table `bourse`
--
ALTER TABLE `bourse`
  ADD PRIMARY KEY (`id_bourse`);

--
-- Index pour la table `bourse_wishlist`
--
ALTER TABLE `bourse_wishlist`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_action_unique` (`user_id`,`action_id`),
  ADD KEY `IDX_8CAE9C8FA76ED395` (`user_id`),
  ADD KEY `IDX_8CAE9C8F9D32F035` (`action_id`);

--
-- Index pour la table `candidature`
--
ALTER TABLE `candidature`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_E33BD3B8308E35F8` (`appel_offre_id`),
  ADD KEY `IDX_E33BD3B8A76ED395` (`user_id`);

--
-- Index pour la table `cards`
--
ALTER TABLE `cards`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `categorie`
--
ALTER TABLE `categorie`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `category`
--
ALTER TABLE `category`
  ADD PRIMARY KEY (`id_category`);

--
-- Index pour la table `centre_formation`
--
ALTER TABLE `centre_formation`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `doctrine_migration_versions`
--
ALTER TABLE `doctrine_migration_versions`
  ADD PRIMARY KEY (`version`);

--
-- Index pour la table `formation`
--
ALTER TABLE `formation`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `investment`
--
ALTER TABLE `investment`
  ADD PRIMARY KEY (`investment_id`),
  ADD KEY `IDX_43CA0AD6A76ED395` (`user_id`);

--
-- Index pour la table `investment_management`
--
ALTER TABLE `investment_management`
  ADD PRIMARY KEY (`management_id`),
  ADD KEY `IDX_12369BDE6E1B4FD5` (`investment_id`),
  ADD KEY `IDX_12369BDEA76ED395` (`user_id`);

--
-- Index pour la table `investment_notification`
--
ALTER TABLE `investment_notification`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_30B6F9EEA76ED395` (`user_id`),
  ADD KEY `IDX_30B6F9EE6E1B4FD5` (`investment_id`);

--
-- Index pour la table `investment_wishlist`
--
ALTER TABLE `investment_wishlist`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_investment_unique` (`user_id`,`investment_id`),
  ADD KEY `IDX_A55DF32DA76ED395` (`user_id`),
  ADD KEY `IDX_A55DF32D6E1B4FD5` (`investment_id`);

--
-- Index pour la table `lesson`
--
ALTER TABLE `lesson`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_F87474F35200282E` (`formation_id`);

--
-- Index pour la table `margin_loan`
--
ALTER TABLE `margin_loan`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `notification_bourse`
--
ALTER TABLE `notification_bourse`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_19654241A76ED395` (`user_id`),
  ADD KEY `IDX_1965424161FB397F` (`action_id`);

--
-- Index pour la table `quiz_result`
--
ALTER TABLE `quiz_result`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_FE2E314AA76ED395` (`user_id`);

--
-- Index pour la table `rating`
--
ALTER TABLE `rating`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_D8892622308E35F8` (`appel_offre_id`),
  ADD KEY `IDX_D8892622A76ED395` (`user_id`);

--
-- Index pour la table `rating_centre`
--
ALTER TABLE `rating_centre`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_E95280FDA76ED395` (`user_id`),
  ADD KEY `IDX_E95280FD463CD7C3` (`centre_id`);

--
-- Index pour la table `recharge_requests`
--
ALTER TABLE `recharge_requests`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `transaction_bourse`
--
ALTER TABLE `transaction_bourse`
  ADD PRIMARY KEY (`id_transaction`),
  ADD KEY `IDX_ABBFCE5C61FB397F` (`action_id`),
  ADD KEY `IDX_ABBFCE5C6B3CA4B` (`user_id`);

--
-- Index pour la table `transaction_wallet`
--
ALTER TABLE `transaction_wallet`
  ADD PRIMARY KEY (`id_transaction`),
  ADD KEY `IDX_A15E05F12469DE2` (`category_id`);

--
-- Index pour la table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_1483A5E9F85E0677` (`username`),
  ADD UNIQUE KEY `UNIQ_1483A5E9E7927C74` (`email`);

--
-- Index pour la table `user_api_key`
--
ALTER TABLE `user_api_key`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_911FF3975F37A13B` (`token`);

--
-- Index pour la table `user_biometrics`
--
ALTER TABLE `user_biometrics`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_D7128AA6A76ED395` (`user_id`);

--
-- Index pour la table `user_favorite_appels`
--
ALTER TABLE `user_favorite_appels`
  ADD PRIMARY KEY (`user_id`,`appel_offre_id`),
  ADD KEY `IDX_80B6309CA76ED395` (`user_id`),
  ADD KEY `IDX_80B6309C308E35F8` (`appel_offre_id`);

--
-- Index pour la table `user_formation_purchased`
--
ALTER TABLE `user_formation_purchased`
  ADD PRIMARY KEY (`user_id`,`formation_id`),
  ADD KEY `IDX_C9098B96A76ED395` (`user_id`),
  ADD KEY `IDX_C9098B965200282E` (`formation_id`);

--
-- Index pour la table `user_formation_wishlist`
--
ALTER TABLE `user_formation_wishlist`
  ADD PRIMARY KEY (`user_id`,`formation_id`),
  ADD KEY `IDX_F7379B3BA76ED395` (`user_id`),
  ADD KEY `IDX_F7379B3B5200282E` (`formation_id`);

--
-- Index pour la table `wishlist`
--
ALTER TABLE `wishlist`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `action`
--
ALTER TABLE `action`
  MODIFY `id_action` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `action_news`
--
ALTER TABLE `action_news`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT pour la table `appel_offre`
--
ALTER TABLE `appel_offre`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT pour la table `bourse`
--
ALTER TABLE `bourse`
  MODIFY `id_bourse` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `bourse_wishlist`
--
ALTER TABLE `bourse_wishlist`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `candidature`
--
ALTER TABLE `candidature`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=35;

--
-- AUTO_INCREMENT pour la table `cards`
--
ALTER TABLE `cards`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT pour la table `categorie`
--
ALTER TABLE `categorie`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT pour la table `category`
--
ALTER TABLE `category`
  MODIFY `id_category` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=57;

--
-- AUTO_INCREMENT pour la table `centre_formation`
--
ALTER TABLE `centre_formation`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT pour la table `formation`
--
ALTER TABLE `formation`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT pour la table `investment`
--
ALTER TABLE `investment`
  MODIFY `investment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=44;

--
-- AUTO_INCREMENT pour la table `investment_management`
--
ALTER TABLE `investment_management`
  MODIFY `management_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT pour la table `investment_wishlist`
--
ALTER TABLE `investment_wishlist`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT pour la table `lesson`
--
ALTER TABLE `lesson`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT pour la table `margin_loan`
--
ALTER TABLE `margin_loan`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `notification_bourse`
--
ALTER TABLE `notification_bourse`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT pour la table `quiz_result`
--
ALTER TABLE `quiz_result`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT pour la table `rating`
--
ALTER TABLE `rating`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT pour la table `rating_centre`
--
ALTER TABLE `rating_centre`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `recharge_requests`
--
ALTER TABLE `recharge_requests`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=42;

--
-- AUTO_INCREMENT pour la table `transaction_bourse`
--
ALTER TABLE `transaction_bourse`
  MODIFY `id_transaction` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT pour la table `transaction_wallet`
--
ALTER TABLE `transaction_wallet`
  MODIFY `id_transaction` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=165;

--
-- AUTO_INCREMENT pour la table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT pour la table `user_api_key`
--
ALTER TABLE `user_api_key`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `user_biometrics`
--
ALTER TABLE `user_biometrics`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `wishlist`
--
ALTER TABLE `wishlist`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `action`
--
ALTER TABLE `action`
  ADD CONSTRAINT `FK_47CC8C92FBF509F1` FOREIGN KEY (`bourse_id`) REFERENCES `bourse` (`id_bourse`);

--
-- Contraintes pour la table `action_news`
--
ALTER TABLE `action_news`
  ADD CONSTRAINT `FK_6B32225E61FB397F` FOREIGN KEY (`action_id`) REFERENCES `action` (`id_action`);

--
-- Contraintes pour la table `appel_offre`
--
ALTER TABLE `appel_offre`
  ADD CONSTRAINT `FK_BC56FD47B03A8386` FOREIGN KEY (`created_by_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FK_BC56FD47BCF5E72D` FOREIGN KEY (`categorie_id`) REFERENCES `categorie` (`id`);

--
-- Contraintes pour la table `bourse_wishlist`
--
ALTER TABLE `bourse_wishlist`
  ADD CONSTRAINT `FK_8CAE9C8F9D32F035` FOREIGN KEY (`action_id`) REFERENCES `action` (`id_action`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_8CAE9C8FA76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `candidature`
--
ALTER TABLE `candidature`
  ADD CONSTRAINT `FK_E33BD3B8308E35F8` FOREIGN KEY (`appel_offre_id`) REFERENCES `appel_offre` (`id`),
  ADD CONSTRAINT `FK_E33BD3B8A76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Contraintes pour la table `investment`
--
ALTER TABLE `investment`
  ADD CONSTRAINT `FK_INVESTMENT_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

--
-- Contraintes pour la table `investment_management`
--
ALTER TABLE `investment_management`
  ADD CONSTRAINT `FK_MANAGEMENT_INVESTMENT` FOREIGN KEY (`investment_id`) REFERENCES `investment` (`investment_id`),
  ADD CONSTRAINT `FK_MANAGEMENT_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

--
-- Contraintes pour la table `investment_notification`
--
ALTER TABLE `investment_notification`
  ADD CONSTRAINT `FK_30B6F9EE6E1B4FD5` FOREIGN KEY (`investment_id`) REFERENCES `investment` (`investment_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_30B6F9EEA76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Contraintes pour la table `investment_wishlist`
--
ALTER TABLE `investment_wishlist`
  ADD CONSTRAINT `FK_FAVORITE_INVEST` FOREIGN KEY (`investment_id`) REFERENCES `investment` (`investment_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_FAVORITE_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `lesson`
--
ALTER TABLE `lesson`
  ADD CONSTRAINT `FK_F87474F35200282E` FOREIGN KEY (`formation_id`) REFERENCES `formation` (`id`);

--
-- Contraintes pour la table `notification_bourse`
--
ALTER TABLE `notification_bourse`
  ADD CONSTRAINT `FK_1965424161FB397F` FOREIGN KEY (`action_id`) REFERENCES `action` (`id_action`) ON DELETE SET NULL,
  ADD CONSTRAINT `FK_19654241A76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `quiz_result`
--
ALTER TABLE `quiz_result`
  ADD CONSTRAINT `FK_FE2E314AA76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

--
-- Contraintes pour la table `rating`
--
ALTER TABLE `rating`
  ADD CONSTRAINT `FK_D8892622308E35F8` FOREIGN KEY (`appel_offre_id`) REFERENCES `appel_offre` (`id`),
  ADD CONSTRAINT `FK_D8892622A76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Contraintes pour la table `rating_centre`
--
ALTER TABLE `rating_centre`
  ADD CONSTRAINT `FK_E95280FD463CD7C3` FOREIGN KEY (`centre_id`) REFERENCES `centre_formation` (`id`),
  ADD CONSTRAINT `FK_E95280FDA76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Contraintes pour la table `transaction_bourse`
--
ALTER TABLE `transaction_bourse`
  ADD CONSTRAINT `FK_ABBFCE5C61FB397F` FOREIGN KEY (`action_id`) REFERENCES `action` (`id_action`),
  ADD CONSTRAINT `FK_ABBFCE5C6B3CA4B` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Contraintes pour la table `transaction_wallet`
--
ALTER TABLE `transaction_wallet`
  ADD CONSTRAINT `FK_A15E05F12469DE2` FOREIGN KEY (`category_id`) REFERENCES `category` (`id_category`);

--
-- Contraintes pour la table `user_biometrics`
--
ALTER TABLE `user_biometrics`
  ADD CONSTRAINT `FK_D7128AA6A76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `user_favorite_appels`
--
ALTER TABLE `user_favorite_appels`
  ADD CONSTRAINT `FK_80B6309C308E35F8` FOREIGN KEY (`appel_offre_id`) REFERENCES `appel_offre` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_80B6309CA76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `user_formation_purchased`
--
ALTER TABLE `user_formation_purchased`
  ADD CONSTRAINT `FK_C9098B965200282E` FOREIGN KEY (`formation_id`) REFERENCES `formation` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_C9098B96A76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `user_formation_wishlist`
--
ALTER TABLE `user_formation_wishlist`
  ADD CONSTRAINT `FK_F7379B3B5200282E` FOREIGN KEY (`formation_id`) REFERENCES `formation` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_F7379B3BA76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;
