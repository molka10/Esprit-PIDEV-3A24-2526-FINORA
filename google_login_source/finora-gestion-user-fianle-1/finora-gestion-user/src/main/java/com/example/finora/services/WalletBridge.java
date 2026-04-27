package com.example.finora.services;

import com.example.finora.entities.transaction;

import java.sql.Date;
import java.time.LocalDate;
import com.example.finora.entities.categorie;
/**
 * 🔗 WalletBridge — Central integration layer
 * Records financial movements from Bourse, Investment, and Appel d'Offre
 * as income/outcome entries in the Wallet (transaction_wallet table).
 */
public class WalletBridge {

    private static final servicetransaction walletService = new servicetransaction();
    private static final servicecategorie categorieService = new servicecategorie();

    private WalletBridge() {
    }

    // ─────────────────────────────────────────────────────────
    // BOURSE → WALLET
    // ─────────────────────────────────────────────────────────

    /**
     * Stock purchase (ACHAT) → outcome in wallet
     */
    public static void recordBourseAchat(int userId, String actionSymbol, double montantTotal) {
        record(
                userId,
                "Achat action " + actionSymbol,
                "outcome",
                montantTotal,
                "Bourse",
                getOrCreateCategoryId(userId, "Bourse", "outcome"));
    }

    /**
     * Stock sale (VENTE) → income in wallet
     */
    public static void recordBourseVente(int userId, String actionSymbol, double montantTotal) {
        record(
                userId,
                "Vente action " + actionSymbol,
                "income",
                montantTotal,
                "Bourse",
                getOrCreateCategoryId(userId, "Bourse", "income"));  // 🔥 هنا التعديل
    }

    // ─────────────────────────────────────────────────────────
    // INVESTMENT → WALLET
    // ─────────────────────────────────────────────────────────

    /**
     * New investment created → outcome in wallet
     */
    public static void recordInvestment(int userId, String investmentName, double amount) {
        record(
                userId,
                "Investissement: " + investmentName,
                "outcome",
                amount,
                "Investissement",
                getOrCreateCategoryId(userId,"Investissement", "outcome"));
    }

    // ─────────────────────────────────────────────────────────
    // APPEL D'OFFRE → WALLET
    // ─────────────────────────────────────────────────────────

    /**
     * Candidature submitted → outcome in wallet (money committed)
     */
    public static void recordAppelOffreSubmitted(int userId, String titre, double montant) {
        if (montant <= 0)
            return; // skip if no montant proposed
        record(
                userId,
                "Candidature: " + titre,
                "outcome",
                montant,
                "Appel d'Offre",
                getOrCreateCategoryId(userId,"Appel d'Offre", "outcome"));
    }

    /**
     * Candidature accepted → income in wallet (money received)
     */
    public static void recordAppelOffreAccepted(int userId, String titre, double montant) {
        if (montant <= 0)
            return;
        record(
                userId,
                "Appel d'Offre accepté: " + titre,
                "income",
                montant,
                "Appel d'Offre",
                getOrCreateCategoryId(userId,"Appel d'Offre", "income"));
    }

    // ─────────────────────────────────────────────────────────
    // INTERNAL HELPERS
    // ─────────────────────────────────────────────────────────

    private static void record(int userId, String nom, String type,
            double montant, String source, int categoryId) {

        if (type != null && type.equalsIgnoreCase("outcome")) {
            montant = -Math.abs(montant);
        }

        try {
            transaction t = new transaction(
                    nom,
                    type,
                    montant,
                    Date.valueOf(LocalDate.now()),
                    source,
                    userId,
                    categoryId);
            walletService.ajouter(t);
            System.out.println("✅ WalletBridge: " + type + " | " + nom + " | " + montant);
        } catch (Exception e) {
            System.err.println("⚠️ WalletBridge error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int getOrCreateCategoryId(int userId, String nom, String type) {

        int id = categorieService.getIdByNameTypeAndUser(nom, type, userId);
        if (id > 0)
            return id;

        categorie cat = new categorie(nom, "HAUTE", type, userId);
        categorieService.ajouter(cat);

        return categorieService.getIdByNameTypeAndUser(nom, type, userId);
    }
}
