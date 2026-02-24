package com.example.crud.models;

import java.time.LocalDateTime;

public class Transaction {

    private int idTransaction;
    private String typeTransaction;
    private int quantite;
    private double prixUnitaire;
    private double montantTotal;
    private LocalDateTime dateTransaction;
    private int idAction;
    private String acteurRole;
    private String acteurLabel;

    public Transaction() {}

    public Transaction(String typeTransaction, int quantite, double prixUnitaire,
                       int idAction, String acteurRole, String acteurLabel) {
        this.typeTransaction = typeTransaction;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.montantTotal = quantite * prixUnitaire;
        this.idAction = idAction;
        this.acteurRole = acteurRole;
        this.acteurLabel = acteurLabel;
    }

    public int getIdTransaction() { return idTransaction; }
    public void setIdTransaction(int idTransaction) { this.idTransaction = idTransaction; }

    public String getTypeTransaction() { return typeTransaction; }
    public void setTypeTransaction(String typeTransaction) { this.typeTransaction = typeTransaction; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }

    public LocalDateTime getDateTransaction() { return dateTransaction; }
    public void setDateTransaction(LocalDateTime dateTransaction) { this.dateTransaction = dateTransaction; }

    public int getIdAction() { return idAction; }
    public void setIdAction(int idAction) { this.idAction = idAction; }

    public String getActeurRole() { return acteurRole; }
    public void setActeurRole(String acteurRole) { this.acteurRole = acteurRole; }

    public String getActeurLabel() { return acteurLabel; }
    public void setActeurLabel(String acteurLabel) { this.acteurLabel = acteurLabel; }
}
