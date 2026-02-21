package com.example.project_pi.entities;

import java.sql.Timestamp;

public class Candidature {

    private int candidatureId;
    private int appelOffreId;
    private String nomCandidat;
    private String emailCandidat;
    private double montantPropose;
    private String message;
    private String statut;
    private Timestamp createdAt;

    // Empty constructor
    public Candidature() {}

    // Constructor for INSERT (without ID and createdAt)
    public Candidature(int appelOffreId,
                       String nomCandidat,
                       String emailCandidat,
                       double montantPropose,
                       String message,
                       String statut) {

        this.appelOffreId = appelOffreId;
        this.nomCandidat = nomCandidat;
        this.emailCandidat = emailCandidat;
        this.montantPropose = montantPropose;
        this.message = message;
        this.statut = statut;
    }

    // Full constructor (useful for mapping from DB)
    public Candidature(int candidatureId,
                       int appelOffreId,
                       String nomCandidat,
                       String emailCandidat,
                       double montantPropose,
                       String message,
                       String statut,
                       Timestamp createdAt) {

        this.candidatureId = candidatureId;
        this.appelOffreId = appelOffreId;
        this.nomCandidat = nomCandidat;
        this.emailCandidat = emailCandidat;
        this.montantPropose = montantPropose;
        this.message = message;
        this.statut = statut;
        this.createdAt = createdAt;
    }

    // Getters & Setters

    public int getCandidatureId() {
        return candidatureId;
    }

    public void setCandidatureId(int candidatureId) {
        this.candidatureId = candidatureId;
    }

    public int getAppelOffreId() {
        return appelOffreId;
    }

    public void setAppelOffreId(int appelOffreId) {
        this.appelOffreId = appelOffreId;
    }

    public String getNomCandidat() {
        return nomCandidat;
    }

    public void setNomCandidat(String nomCandidat) {
        this.nomCandidat = nomCandidat;
    }

    public String getEmailCandidat() {
        return emailCandidat;
    }

    public void setEmailCandidat(String emailCandidat) {
        this.emailCandidat = emailCandidat;
    }

    public double getMontantPropose() {
        return montantPropose;
    }

    public void setMontantPropose(double montantPropose) {
        this.montantPropose = montantPropose;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return nomCandidat + " - " + emailCandidat;
    }
}