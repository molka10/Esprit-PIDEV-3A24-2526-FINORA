package com.example.crud.services;

import com.example.crud.interfaces.IServices;
import com.example.crud.models.Action;
import com.example.crud.models.Bourse;
import com.example.crud.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les actions
 *
 * IMPORTANT : Ce service charge TOUJOURS l'objet Bourse complet
 * grâce à une jointure SQL, comme demandé par le professeur
 */
public class ServiceAction implements IServices<Action> {

    private Connection connection;

    public ServiceAction() {
        this.connection = Database.getInstance().getConnection();
    }

    // ========================================
    // CREATE
    // ========================================
    @Override
    public void add(Action action) {
        String req = "INSERT INTO action (id_bourse, symbole, nom_entreprise, secteur, " +
                "prix_unitaire, quantite_disponible, statut) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, action.getIdBourse());
            pst.setString(2, action.getSymbole());
            pst.setString(3, action.getNomEntreprise());
            pst.setString(4, action.getSecteur());
            pst.setDouble(5, action.getPrixUnitaire());
            pst.setInt(6, action.getQuantiteDisponible());
            pst.setString(7, action.getStatut());

            pst.executeUpdate();
            System.out.println("✅ Action ajoutée : " + action.getSymbole());

            pst.close();
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========================================
    // READ ALL - AVEC JOINTURE POUR CHARGER LA BOURSE
    // ========================================
    @Override
    public List<Action> getAll() {
        List<Action> actions = new ArrayList<>();

        // JOINTURE pour récupérer TOUS les champs de la bourse
        String req = "SELECT a.*, " +
                "b.id_bourse, b.nom_bourse, b.pays, b.devise, b.statut AS statut_bourse, b.date_creation " +
                "FROM action a " +
                "JOIN bourse b ON a.id_bourse = b.id_bourse " +
                "ORDER BY a.symbole";

        try {
            Statement stm = connection.createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                // 1. Créer l'objet Bourse complet
                Bourse bourse = new Bourse();
                bourse.setIdBourse(rs.getInt("id_bourse"));
                bourse.setNomBourse(rs.getString("nom_bourse"));
                bourse.setPays(rs.getString("pays"));
                bourse.setDevise(rs.getString("devise"));
                bourse.setStatut(rs.getString("statut_bourse"));
                bourse.setDateCreation(rs.getTimestamp("date_creation"));

                // 2. Créer l'objet Action
                Action action = new Action();
                action.setIdAction(rs.getInt("id_action"));
                action.setIdBourse(rs.getInt("id_bourse"));
                action.setBourse(bourse);  // ← CLÉ ÉTRANGÈRE COMME INSTANCE
                action.setSymbole(rs.getString("symbole"));
                action.setNomEntreprise(rs.getString("nom_entreprise"));
                action.setSecteur(rs.getString("secteur"));
                action.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                action.setQuantiteDisponible(rs.getInt("quantite_disponible"));
                action.setDateAjout(rs.getTimestamp("date_ajout"));
                action.setStatut(rs.getString("statut"));

                actions.add(action);
            }

            rs.close();
            stm.close();
            System.out.println("✅ " + actions.size() + " action(s) chargée(s) avec leur bourse");
        } catch (SQLException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }

        return actions;
    }

    // ========================================
    // UPDATE
    // ========================================
    @Override
    public void update(Action action) {
        String req = "UPDATE action SET id_bourse=?, symbole=?, nom_entreprise=?, secteur=?, " +
                "prix_unitaire=?, quantite_disponible=?, statut=? WHERE id_action=?";

        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, action.getIdBourse());
            pst.setString(2, action.getSymbole());
            pst.setString(3, action.getNomEntreprise());
            pst.setString(4, action.getSecteur());
            pst.setDouble(5, action.getPrixUnitaire());
            pst.setInt(6, action.getQuantiteDisponible());
            pst.setString(7, action.getStatut());
            pst.setInt(8, action.getIdAction());

            pst.executeUpdate();
            System.out.println("✅ Action modifiée : " + action.getSymbole());

            pst.close();
        } catch (SQLException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========================================
    // DELETE
    // ========================================
    @Override
    public void delete(Action action) {
        String req = "DELETE FROM action WHERE id_action = ?";

        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, action.getIdAction());

            pst.executeUpdate();
            System.out.println("✅ Action supprimée : " + action.getSymbole());

            pst.close();
        } catch (SQLException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========================================
    // MÉTHODES SUPPLÉMENTAIRES
    // ========================================

    /**
     * Récupérer une action par ID (avec sa bourse)
     */
    public Action getById(int id) {
        Action action = null;

        String req = "SELECT a.*, " +
                "b.id_bourse, b.nom_bourse, b.pays, b.devise, b.statut AS statut_bourse, b.date_creation " +
                "FROM action a " +
                "JOIN bourse b ON a.id_bourse = b.id_bourse " +
                "WHERE a.id_action = ?";

        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Créer la bourse
                Bourse bourse = new Bourse();
                bourse.setIdBourse(rs.getInt("id_bourse"));
                bourse.setNomBourse(rs.getString("nom_bourse"));
                bourse.setPays(rs.getString("pays"));
                bourse.setDevise(rs.getString("devise"));
                bourse.setStatut(rs.getString("statut_bourse"));
                bourse.setDateCreation(rs.getTimestamp("date_creation"));

                // Créer l'action
                action = new Action();
                action.setIdAction(rs.getInt("id_action"));
                action.setIdBourse(rs.getInt("id_bourse"));
                action.setBourse(bourse);
                action.setSymbole(rs.getString("symbole"));
                action.setNomEntreprise(rs.getString("nom_entreprise"));
                action.setSecteur(rs.getString("secteur"));
                action.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                action.setQuantiteDisponible(rs.getInt("quantite_disponible"));
                action.setDateAjout(rs.getTimestamp("date_ajout"));
                action.setStatut(rs.getString("statut"));
            }

            rs.close();
            pst.close();
        } catch (SQLException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
        }

        return action;
    }

    /**
     * Récupérer toutes les actions d'une bourse
     */
    public List<Action> getByBourse(int idBourse) {
        List<Action> actions = new ArrayList<>();

        String req = "SELECT a.*, " +
                "b.id_bourse, b.nom_bourse, b.pays, b.devise, b.statut AS statut_bourse, b.date_creation " +
                "FROM action a " +
                "JOIN bourse b ON a.id_bourse = b.id_bourse " +
                "WHERE a.id_bourse = ? " +
                "ORDER BY a.symbole";

        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, idBourse);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                // Créer la bourse
                Bourse bourse = new Bourse();
                bourse.setIdBourse(rs.getInt("id_bourse"));
                bourse.setNomBourse(rs.getString("nom_bourse"));
                bourse.setPays(rs.getString("pays"));
                bourse.setDevise(rs.getString("devise"));
                bourse.setStatut(rs.getString("statut_bourse"));
                bourse.setDateCreation(rs.getTimestamp("date_creation"));

                // Créer l'action
                Action action = new Action();
                action.setIdAction(rs.getInt("id_action"));
                action.setIdBourse(rs.getInt("id_bourse"));
                action.setBourse(bourse);
                action.setSymbole(rs.getString("symbole"));
                action.setNomEntreprise(rs.getString("nom_entreprise"));
                action.setSecteur(rs.getString("secteur"));
                action.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                action.setQuantiteDisponible(rs.getInt("quantite_disponible"));
                action.setDateAjout(rs.getTimestamp("date_ajout"));
                action.setStatut(rs.getString("statut"));

                actions.add(action);
            }

            rs.close();
            pst.close();
        } catch (SQLException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
        }

        return actions;
    }
}