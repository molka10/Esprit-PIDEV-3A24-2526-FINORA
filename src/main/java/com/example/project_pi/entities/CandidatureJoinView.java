package com.example.project_pi.entities;

public class CandidatureJoinView {
    private int candidatureId;
    private int appelOffreId;
    private String titreAppelOffre;

    private String nomCandidat;
    private String emailCandidat;
    private double montantPropose;
    private String statut;

    public CandidatureJoinView() {}

    public CandidatureJoinView(int candidatureId, int appelOffreId, String titreAppelOffre,
                               String nomCandidat, String emailCandidat, double montantPropose, String statut) {
        this.candidatureId = candidatureId;
        this.appelOffreId = appelOffreId;
        this.titreAppelOffre = titreAppelOffre;
        this.nomCandidat = nomCandidat;
        this.emailCandidat = emailCandidat;
        this.montantPropose = montantPropose;
        this.statut = statut;
    }

    public int getCandidatureId() { return candidatureId; }
    public int getAppelOffreId() { return appelOffreId; }
    public String getTitreAppelOffre() { return titreAppelOffre; }
    public String getNomCandidat() { return nomCandidat; }
    public String getEmailCandidat() { return emailCandidat; }
    public double getMontantPropose() { return montantPropose; }
    public String getStatut() { return statut; }

    @Override
    public String toString() {
        return "CandidatureJoinView{" +
                "candidatureId=" + candidatureId +
                ", appelOffreId=" + appelOffreId +
                ", titreAppelOffre='" + titreAppelOffre + '\'' +
                ", nomCandidat='" + nomCandidat + '\'' +
                ", emailCandidat='" + emailCandidat + '\'' +
                ", montantPropose=" + montantPropose +
                ", statut='" + statut + '\'' +
                '}';
    }
}
