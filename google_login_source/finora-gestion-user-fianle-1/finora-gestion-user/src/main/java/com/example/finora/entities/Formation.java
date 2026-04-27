package com.example.finora.entities;

public class Formation {
    private int id;
    private String titre;
    private String description;
    private String categorie;
    private String niveau;
    private boolean published;
    private String imageUrl;

    public Formation() {}

    public Formation(String titre, String description, String categorie, String niveau, boolean published, String imageUrl) {
        this.titre = titre;
        this.description = description;
        this.categorie = categorie;
        this.niveau = niveau;
        this.published = published;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        // ✅ Important: ComboBox falls back to toString() sometimes
        // We only want the visible title, not "Formation{id=...}"
        return (titre == null || titre.isBlank()) ? "Formation" : titre;
    }
}