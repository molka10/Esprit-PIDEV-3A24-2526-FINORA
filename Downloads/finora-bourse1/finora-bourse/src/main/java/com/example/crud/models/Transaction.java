package com.example.crud.models;

import java.sql.Timestamp;

/**
 * 📜 Modèle Transaction
 * Représente un achat ou une vente d'action
 */
public class Transaction {

    private int idTransaction;
    private int idAction;
    private String typeTransaction; // "ACHAT" ou "VENTE"
    private int quantite;
    private double prixUnitaire;
    private double montantTotal;
    private double commission;
    private Timestamp dateTransaction;

    // ✅ Nouveaux champs (table transaction)
    private String acteurRole;   // ex: INVESTISSEUR / ENTREPRISE
    private String acteurLabel;  // ex: Utilisateur (nom affiché)

    // Relation avec Action (pour affichage)
    private Action action;

    // Constructeurs
    public Transaction() {}

    public Transaction(int idAction, String typeTransaction,
                       int quantite, double prixUnitaire, double commission) {
        this.idAction        = idAction;
        this.typeTransaction = typeTransaction;
        this.quantite        = quantite;
        this.prixUnitaire    = prixUnitaire;
        this.montantTotal    = prixUnitaire * quantite;
        this.commission      = commission;
    }

    // ── Getters / Setters ──────────────────────────────────────

    public int getIdTransaction() { return idTransaction; }
    public void setIdTransaction(int v) { idTransaction = v; }

    public int getIdAction() { return idAction; }
    public void setIdAction(int v) { idAction = v; }

    public String getTypeTransaction() { return typeTransaction; }
    public void setTypeTransaction(String v) { typeTransaction = v; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int v) { quantite = v; }

    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double v) { prixUnitaire = v; }

    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double v) { montantTotal = v; }

    public double getCommission() { return commission; }
    public void setCommission(double v) { commission = v; }

    public Timestamp getDateTransaction() { return dateTransaction; }
    public void setDateTransaction(Timestamp v) { dateTransaction = v; }

    public Action getAction() { return action; }
    public void setAction(Action v) {
        action = v;
        idAction = (v != null) ? v.getIdAction() : 0;
    }

    // ✅ acteur_role / acteur_label
    public String getActeurRole() { return acteurRole; }
    public void setActeurRole(String acteurRole) { this.acteurRole = acteurRole; }

    public String getActeurLabel() { return acteurLabel; }
    public void setActeurLabel(String acteurLabel) { this.acteurLabel = acteurLabel; }

    // ── Helpers ───────────────────────────────────────────────

    /** Montant net = total + commission pour un achat, total - commission pour une vente */
    public double getMontantNet() {
        return "ACHAT".equals(typeTransaction)
                ? montantTotal + commission
                : montantTotal - commission;
    }

    /** Symbole de l'action ou "N/A" */
    public String getSymbole() {
        return action != null ? action.getSymbole() : "N/A";
    }

    /** Nom entreprise ou "N/A" */
    public String getNomEntreprise() {
        return action != null ? action.getNomEntreprise() : "N/A";
    }

    /** Devise de la bourse ou "TND" */
    public String getDevise() {
        if (action != null && action.getBourse() != null)
            return action.getBourse().getDevise();
        return "TND";
    }

    /** Texte acteur (utile pour affichage) */
    public String getActeurAffichage() {
        if (acteurLabel != null && !acteurLabel.isBlank()) return acteurLabel;
        if (acteurRole != null && !acteurRole.isBlank()) return acteurRole;
        return "N/A";
    }



    @Override
    public String toString() {
        return "Transaction{id=" + idTransaction +
                ", type=" + typeTransaction +
                ", symbole=" + getSymbole() +
                ", qte=" + quantite +
                ", total=" + montantTotal +
                ", acteur=" + getActeurAffichage() +
                "}";
    }
}
