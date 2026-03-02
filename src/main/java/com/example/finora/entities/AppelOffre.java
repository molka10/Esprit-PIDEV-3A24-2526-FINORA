package com.example.finora.entities;

import java.time.LocalDate;

public class AppelOffre {

    private int appelOffreId;
    private String titre;
    private String description;
    private String categorie;
    private String type;
    private double budgetMin;
    private double budgetMax;
    private String devise;
    private LocalDate dateLimite;
    private String statut;

    public AppelOffre() {}

    public AppelOffre(String titre, String description, String categorie, String type,
                      double budgetMin, double budgetMax, String devise,
                      LocalDate dateLimite, String statut) {
        this.titre = titre;
        this.description = description;
        this.categorie = categorie;
        this.type = type;
        this.budgetMin = budgetMin;
        this.budgetMax = budgetMax;
        this.devise = devise;
        this.dateLimite = dateLimite;
        this.statut = statut;
    }

    public int getAppelOffreId() { return appelOffreId; }
    public void setAppelOffreId(int appelOffreId) { this.appelOffreId = appelOffreId; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getBudgetMin() { return budgetMin; }
    public void setBudgetMin(double budgetMin) { this.budgetMin = budgetMin; }

    public double getBudgetMax() { return budgetMax; }
    public void setBudgetMax(double budgetMax) { this.budgetMax = budgetMax; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public LocalDate getDateLimite() { return dateLimite; }
    public void setDateLimite(LocalDate dateLimite) { this.dateLimite = dateLimite; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "AppelOffre{" +
                "appelOffreId=" + appelOffreId +
                ", titre='" + titre + '\'' +
                ", categorie='" + categorie + '\'' +
                ", type='" + type + '\'' +
                ", budgetMin=" + budgetMin +
                ", budgetMax=" + budgetMax +
                ", devise='" + devise + '\'' +
                ", dateLimite=" + dateLimite +
                ", statut='" + statut + '\'' +
                '}';
    }
}
