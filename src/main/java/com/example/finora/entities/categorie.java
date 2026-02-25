package com.example.finora.entities;

public class categorie {

    private int id_category;
    private String nom;
    private String priorite;
    private String type;


    public categorie() {}

    public categorie(String nom, String priorite, String type) {
        this.nom = nom;
        this.priorite = priorite;
        this.type = type;

    }

    public categorie(int id_category, String nom, String priorite, String type ) {
        this.id_category = id_category;
        this.nom = nom;
        this.priorite = priorite;
        this.type = type;


    }

    public int getId_category() { return id_category; }
    public void setId_category(int id_category) { this.id_category = id_category; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }



    @Override
    public String toString() {
        return "ID: " + id_category +
                " | Nom : " + nom +
                " | Priorité : " + priorite +
                " | Type : " + type ;

    }

}
