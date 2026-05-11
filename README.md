# 🚀 FINORA - Plateforme Financière Intelligente & Gamifiée

![Finora Banner](https://via.placeholder.com/1200x400/1e0a3c/ffffff?text=FINORA+-+L%27Avenir+de+la+Finance+Intelligente)

**FINORA** est une application web innovante conçue pour démocratiser l'investissement boursier, la gestion de portefeuille et l'éducation financière. En combinant l'Intelligence Artificielle, la Gamification et une interface utilisateur premium (Glassmorphism), Finora offre une expérience immersive de bout en bout pour les investisseurs débutants et experts.

---

## ✨ Fonctionnalités Clés (Effet WOW)

### 📈 1. Module Bourse & Marché en Temps Réel
*   **Analyse Technique Avancée :** Graphiques en chandeliers japonais (Candlesticks) dynamiques alimentés par ApexCharts.
*   **Heatmap du Marché :** Visualisation thermique en temps réel des variations boursières.
*   **Simulateur "Monte-Carlo" & Univers 3D :** Outils d'analyse de risques et modélisations immersives pour les investisseurs.
*   **Social Trading :** Possibilité de suivre et "copier" les stratégies des meilleurs traders de la communauté.

### 🤖 2. Intelligence Artificielle (AI Assistant)
*   **Chatbot Financier Intégré :** Un assistant IA persistant accessible depuis le marché pour donner des recommandations d'achat, des analyses de risques et des résumés de marché.
*   **Smart Suggestions :** L'IA analyse le portefeuille et propose des actions ciblées (ex: "Achetez AAPL pour diversifier votre portefeuille").

### 🪴 3. Gamification & Wallet Intelligent
*   **L'Arbre de Richesse (Tamagotchi) :** Une plante virtuelle dynamique qui évolue en fonction de la santé de votre solde (de la jeune pousse 🌱 à l'arbre d'abondance 🌳).
*   **ESG & Empreinte Carbone :** Suivi de l'impact écologique de vos transactions avec un système de compensation carbone (planter des arbres).
*   **Abonnements & Sécurité (Sub-Manager) :** Un pare-feu financier permettant de détecter et de bloquer d'un simple clic les abonnements fantômes. L'algorithme rejette automatiquement toute transaction bloquée.
*   **Système de Trophées (Badges) :** Récompenses débloquées en fonction de votre comportement d'investissement (ex: *Loup de Wall Street*, *Main de Diamant*).

### 🎓 4. Formation & Investissement
*   **Quiz Interactifs Anti-Triche :** Modules de formation avec suivi en temps réel et détection de fraude.
*   **Dashboard d'Investissement :** Suivi du cycle de vie des projets, calcul de rentabilité (ROI), et timeline visuelle des levées de fonds.

---

## 🛠️ Stack Technique

L'architecture de FINORA repose sur une séparation claire entre des technologies robustes et modernes :

*   **Backend (Logique Métier & Web) :** Symfony 6 / PHP 8
*   **Backend (Microservices & Auth) :** Java / Spring Boot (Synchronisation des mots de passe BCrypt et des sessions)
*   **Frontend & UI :** Twig, Bootstrap 5, CSS3 (Custom Glassmorphism & Gradients), JavaScript Vanilla (AJAX)
*   **Base de Données :** MySQL (Doctrine ORM)
*   **Bibliothèques tierces :** 
    *   `ApexCharts.js` (Graphiques boursiers)
    *   `Dompdf` (Génération sécurisée de RIB et relevés de compte)
    *   `ExchangeRate API` (Conversion de devises en temps réel)

---

## 🚀 Installation & Lancement (Environnement de Développement)

### Prérequis
* PHP 8.1+ & Composer
* MySQL / MariaDB
* Symfony CLI
* Java 17+ (pour le module Spring Boot lié)

### Étapes d'installation

1. **Cloner le projet**
   ```bash
   git clone https://github.com/votre-compte/finora.git
   cd finora

