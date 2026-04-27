package com.example.finora.entities;

public class Lesson {
    private int id;
    private int formationId;
    private String titre;
    private String contenu;
    private String videoUrl;     // ✅ NEW
    private int ordre;
    private int dureeMinutes;

    public Lesson() {}

    public Lesson(int formationId, String titre, String contenu, String videoUrl, int ordre, int dureeMinutes) {
        this.formationId = formationId;
        this.titre = titre;
        this.contenu = contenu;
        this.videoUrl = videoUrl;
        this.ordre = ordre;
        this.dureeMinutes = dureeMinutes;
    }
    public Lesson(int formationId, String titre, String contenu, int ordre, int dureeMinutes) {
        this.formationId = formationId;
        this.titre = titre;
        this.contenu = contenu;
        this.videoUrl = null; // default
        this.ordre = ordre;
        this.dureeMinutes = dureeMinutes;
    }
    public String getYouTubeVideoId() {
        if (videoUrl == null) return null;
        String url = videoUrl.trim();

        // watch?v=xxxx
        int v = url.indexOf("v=");
        if (v >= 0) {
            String id = url.substring(v + 2);
            int amp = id.indexOf('&');
            if (amp >= 0) id = id.substring(0, amp);
            return id.isBlank() ? null : id;
        }

        // youtu.be/xxxx
        int shortIdx = url.indexOf("youtu.be/");
        if (shortIdx >= 0) {
            String id = url.substring(shortIdx + "youtu.be/".length());
            int q = id.indexOf('?');
            if (q >= 0) id = id.substring(0, q);
            return id.isBlank() ? null : id;
        }

        return null;
    }

    public String getYouTubeThumbnailUrl() {
        String id = getYouTubeVideoId();
        if (id == null) return null;
        return "https://img.youtube.com/vi/" + id + "/hqdefault.jpg";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFormationId() { return formationId; }
    public void setFormationId(int formationId) { this.formationId = formationId; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    public int getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(int dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    @Override
    public String toString() {
        return "Lesson{" +
                "id=" + id +
                ", formationId=" + formationId +
                ", titre='" + titre + '\'' +
                ", ordre=" + ordre +
                ", dureeMinutes=" + dureeMinutes +
                '}';
    }
}