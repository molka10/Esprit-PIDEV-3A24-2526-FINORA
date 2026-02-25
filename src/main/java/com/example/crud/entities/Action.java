package com.example.crud.entities;

import java.sql.Timestamp;

/**
 * Classe Action - Représente une action boursière

 * CLÉ ÉTRANGÈRE COMME INSTANCE :
 * - En MySQL : id_bourse (INT) - clé étrangère
 * - En Java : bourse (Bourse) - instance de l'objet Bourse

 * Cette approche permet d'accéder directement aux informations
 * de la bourse sans faire de requête supplémentaire
 */
public class Action {

    // ========================================
    // ATTRIBUTS
    // ========================================

    private int idAction;

    // CLÉ ÉTRANGÈRE - Pour MySQL
    private int idBourse;

    // INSTANCE DE BOURSE - Pour Java (comme demandé par le professeur)
    private Bourse bourse;

    private String symbole;
    private String nomEntreprise;
    private String secteur;
    private double prixUnitaire;
    private int quantiteDisponible;
    private Timestamp dateAjout;
    private String statut;

    // ========================================
    // CONSTRUCTEURS
    // ========================================

    /**
     * Constructeur vide
     */
    public Action() {
    }

    /**
     * Constructeur avec ID de bourse (pour insertion)
     */
    public Action(int idBourse, String symbole, String nomEntreprise,
                  String secteur, double prixUnitaire, int quantiteDisponible, String statut) {
        this.idBourse = idBourse;
        this.symbole = symbole;
        this.nomEntreprise = nomEntreprise;
        this.secteur = secteur;
        this.prixUnitaire = prixUnitaire;
        this.quantiteDisponible = quantiteDisponible;
        this.statut = statut;
    }

    /**
     * Constructeur avec OBJET Bourse
     */
    public Action(Bourse bourse, String symbole, String nomEntreprise,
                  String secteur, double prixUnitaire, int quantiteDisponible, String statut) {
        this.bourse = bourse;
        this.idBourse = bourse != null ? bourse.getIdBourse() : 0;
        this.symbole = symbole;
        this.nomEntreprise = nomEntreprise;
        this.secteur = secteur;
        this.prixUnitaire = prixUnitaire;
        this.quantiteDisponible = quantiteDisponible;
        this.statut = statut;
    }

    /**
     * Constructeur complet
     */
    public Action(int idAction, int idBourse, String symbole, String nomEntreprise,
                  String secteur, double prixUnitaire, int quantiteDisponible,
                  Timestamp dateAjout, String statut) {
        this.idAction = idAction;
        this.idBourse = idBourse;
        this.symbole = symbole;
        this.nomEntreprise = nomEntreprise;
        this.secteur = secteur;
        this.prixUnitaire = prixUnitaire;
        this.quantiteDisponible = quantiteDisponible;
        this.dateAjout = dateAjout;
        this.statut = statut;
    }

    // ========================================
    // GETTERS ET SETTERS
    // ========================================

    public int getIdAction() {
        return idAction;
    }

    public void setIdAction(int idAction) {
        this.idAction = idAction;
    }

    public int getIdBourse() {
        return idBourse;
    }

    public void setIdBourse(int idBourse) {
        this.idBourse = idBourse;
    }

    /**
     * Getter pour l'objet Bourse
     * Permet d'accéder directement aux informations de la bourse
     */
    public Bourse getBourse() {
        return bourse;
    }

    /**
     * Setter pour l'objet Bourse
     * IMPORTANT : Synchronise automatiquement l'ID
     */
    public void setBourse(Bourse bourse) {
        this.bourse = bourse;
        if (bourse != null) {
            this.idBourse = bourse.getIdBourse();
        }
    }

    public String getSymbole() {
        return symbole;
    }

    public void setSymbole(String symbole) {
        this.symbole = symbole;
    }

    public String getNomEntreprise() {
        return nomEntreprise;
    }

    public void setNomEntreprise(String nomEntreprise) {
        this.nomEntreprise = nomEntreprise;
    }

    public String getSecteur() {
        return secteur;
    }

    public void setSecteur(String secteur) {
        this.secteur = secteur;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public int getQuantiteDisponible() {
        return quantiteDisponible;
    }

    public void setQuantiteDisponible(int quantiteDisponible) {
        this.quantiteDisponible = quantiteDisponible;
    }

    public Timestamp getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(Timestamp dateAjout) {
        this.dateAjout = dateAjout;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    // ========================================
    // MÉTHODES UTILES
    // ========================================

    /**
     * Retourne le nom de la bourse
     * Utilise l'objet Bourse si disponible
     */
    public String getNomBourse() {
        if (bourse != null) {
            return bourse.getNomBourse();
        }
        return "N/A";
    }

    /**
     * Retourne la devise de la bourse
     */
    public String getDevise() {
        if (bourse != null) {
            return bourse.getDevise();
        }
        return "N/A";
    }

    /**
     * Retourne le pays de la bourse
     */
    public String getPays() {
        if (bourse != null) {
            return bourse.getPays();
        }
        return "N/A";
    }

    @Override
    public String toString() {
        String bourseInfo = (bourse != null) ? bourse.getNomBourse() : "ID: " + idBourse;
        return "Action{" +
                "idAction=" + idAction +
                ", symbole='" + symbole + '\'' +
                ", nomEntreprise='" + nomEntreprise + '\'' +
                ", bourse=" + bourseInfo +
                ", prix=" + prixUnitaire +
                ", quantite=" + quantiteDisponible +
                ", statut='" + statut + '\'' +
                '}';
    }

    public boolean isDisponible() {
        return "DISPONIBLE".equalsIgnoreCase(this.statut) && this.quantiteDisponible > 0;
    }

    /**
     * Calcule la valeur totale de cette action en stock
     */
    public double getValeurTotale() {
        return this.prixUnitaire * this.quantiteDisponible;
    }
}