
package com.example.finora.entities;

import java.sql.Timestamp;

/**
 * 💰 Modèle Commission
 * Configuration des taux de commission appliqués aux transactions
 */
public class Commission {

    private int idCommission;
    private String nom;
    private String typeTransaction; // "ACHAT", "VENTE", "LES_DEUX"
    private double tauxPourcentage; // ex: 0.5 pour 0.5%
    private Timestamp dateCreation;
    private Timestamp dateModification;
    private boolean active;

    // Constructeurs
    public Commission() {}

    public Commission(String nom, String typeTransaction, double tauxPourcentage) {
        this.nom = nom;
        this.typeTransaction = typeTransaction;
        this.tauxPourcentage = tauxPourcentage;
        this.active = true;
    }

    // ── Getters / Setters ──────────────────────────────────────

    public int getIdCommission() { return idCommission; }
    public void setIdCommission(int v) { idCommission = v; }

    public String getNom() { return nom; }
    public void setNom(String v) { nom = v; }

    public String getTypeTransaction() { return typeTransaction; }
    public void setTypeTransaction(String v) { typeTransaction = v; }

    public double getTauxPourcentage() { return tauxPourcentage; }
    public void setTauxPourcentage(double v) { tauxPourcentage = v; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp v) { dateCreation = v; }

    public Timestamp getDateModification() { return dateModification; }
    public void setDateModification(Timestamp v) { dateModification = v; }

    public boolean isActive() { return active; }
    public void setActive(boolean v) { active = v; }

    // ── Helpers ───────────────────────────────────────────────

    /**
     * Calcule le montant de commission pour un montant donné
     * @param montant Montant de la transaction
     * @return Commission calculée
     */
    public double calculerCommission(double montant) {
        return Math.round(montant * (tauxPourcentage / 100.0) * 100.0) / 100.0;
    }

    /**
     * Vérifie si cette commission s'applique au type de transaction donné
     */
    public boolean sAppliquePour(String type) {
        return "LES_DEUX".equals(typeTransaction)
                || typeTransaction.equals(type);
    }

    @Override
    public String toString() {
        return nom + " (" + tauxPourcentage + "% - " + typeTransaction + ")";
    }
}