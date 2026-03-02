package com.example.finora.entities;

public class categorie {

    private int id_category;
    private String nom;
    private String priorite;
    private String type;
    private int userId;

    public categorie() {}

    // 1️⃣ Pour INSERT
    public categorie(String nom, String priorite, String type, int userId) {
        this.nom = nom;
        this.priorite = priorite;
        this.type = type;
        this.userId = userId;
    }

    // 2️⃣ Pour SELECT sans userId
    public categorie(int id_category, String nom, String priorite, String type) {
        this.id_category = id_category;
        this.nom = nom;
        this.priorite = priorite;
        this.type = type;
    }

    // 3️⃣ Pour SELECT avec userId
    public categorie(int id_category, String nom, String priorite, String type, int userId) {
        this.id_category = id_category;
        this.nom = nom;
        this.priorite = priorite;
        this.type = type;
        this.userId = userId;
    }

    public int getId_category() { return id_category; }
    public String getNom() { return nom; }
    public String getPriorite() { return priorite; }
    public String getType() { return type; }
    public int getUserId() { return userId; }

    public void setId_category(int id_category) { this.id_category = id_category; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPriorite(String priorite) { this.priorite = priorite; }
    public void setType(String type) { this.type = type; }
    public void setUserId(int userId) { this.userId = userId; }
}