package com.example.gestionwallet.services;

import com.example.gestionwallet.interfaces.services.Iservicetransaction;
import com.example.gestionwallet.models.transaction;
import com.example.gestionwallet.utils.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class servicetransaction implements Iservicetransaction {

    Connection cnx;

    public servicetransaction() {
        cnx = database.getInstance().getConnection();
    }

    @Override
    public void ajouter(transaction t) {

        String sql = "INSERT INTO transaction (nom_transaction, type, montant, date_transaction, source, user_id, category_id, role) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, t.getNom_transaction());
            ps.setString(2, t.getType());
            ps.setDouble(3, t.getMontant());
            ps.setDate(4, t.getDate_transaction());
            ps.setString(5, t.getSource());
            ps.setInt(6, t.getUser_id());
            ps.setInt(7, t.getCategory_id());
            ps.setString(8, t.getRole());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(transaction t) {

        String sql = """
        UPDATE transaction
        SET nom_transaction=?, montant=?, date_transaction=?
        WHERE id_transaction=?
    """;

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, t.getNom_transaction());
            ps.setDouble(2, t.getMontant());
            ps.setDate(3, t.getDate_transaction());
            ps.setInt(4, t.getId_transaction());

            int rows = ps.executeUpdate();
            System.out.println("Rows updated = " + rows);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(int id) {

        String sql = "DELETE FROM transaction WHERE id_transaction=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<transaction> afficher() {

        List<transaction> list = new ArrayList<>();

        try {

            String query = """
                SELECT t.*, c.nom AS categorie_nom
                FROM transaction t
                LEFT JOIN category c
                ON t.category_id = c.id_category
                """;

            Statement stt = cnx.createStatement();
            ResultSet rs = stt.executeQuery(query);

            while (rs.next()) {

                transaction t = new transaction();

                t.setId_transaction(rs.getInt("id_transaction"));
                t.setNom_transaction(rs.getString("nom_transaction"));
                t.setType(rs.getString("type"));
                t.setMontant(rs.getDouble("montant"));
                t.setDate_transaction(rs.getDate("date_transaction"));
                t.setSource(rs.getString("source"));
                t.setUser_id(rs.getInt("user_id"));
                t.setCategory_id(rs.getInt("category_id"));
                t.setRole(rs.getString("role"));

                // 🔥 HEDHI ELLI KENET NA9SA
                t.setCategorie(rs.getString("categorie_nom"));

                list.add(t);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // 🔵 TOTAL INCOME
    public double getTotalIncome() {
        double total = 0;

        try {
            String req = "SELECT SUM(montant) FROM transaction WHERE type='income'";
            PreparedStatement ps = cnx.prepareStatement(req);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                total = rs.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return total;
    }

    // 🔴 TOTAL OUTCOME
    public double getTotalOutcome() {
        double total = 0;

        try {
            String req = "SELECT SUM(montant) FROM transaction WHERE type='outcome'";
            PreparedStatement ps = cnx.prepareStatement(req);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                total = rs.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return total;
    }
    public List<transaction> afficherParRole(String role) {

        List<transaction> list = new ArrayList<>();

        try {

            String query = """
            SELECT t.*, c.nom AS categorie_nom
            FROM transaction t
            JOIN category c ON t.category_id = c.id_category
            WHERE c.role = ?
        """;

            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setString(1, role);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                transaction t = new transaction();

                t.setId_transaction(rs.getInt("id_transaction"));
                t.setNom_transaction(rs.getString("nom_transaction"));
                t.setType(rs.getString("type"));
                t.setMontant(rs.getDouble("montant"));
                t.setDate_transaction(rs.getDate("date_transaction"));
                t.setSource(rs.getString("source"));
                t.setUser_id(rs.getInt("user_id"));
                t.setCategory_id(rs.getInt("category_id"));
                t.setCategorie(rs.getString("categorie_nom"));

                list.add(t);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
