package com.example.finora.services;

import com.example.finora.entities.transaction;
import com.example.finora.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class servicetransaction implements Iservicetransaction {

    Connection cnx;

    public servicetransaction() {
        try {
            cnx = DBConnection.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ajouter(transaction t) {

        String sql = "INSERT INTO transaction_wallet (nom_transaction, type, montant, date_transaction, source, user_id, category_id) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, t.getNom_transaction());
            ps.setString(2, t.getType());
            ps.setDouble(3, t.getMontant());
            ps.setDate(4, t.getDate_transaction());
            ps.setString(5, t.getSource());
            ps.setInt(6, t.getUser_id());
            ps.setInt(7, t.getCategory_id());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(transaction t) {

        String sql = """
                    UPDATE transaction_wallet
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

        String sql = "DELETE FROM transaction_wallet WHERE id_transaction=?";

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
        SELECT t.*, c.nom AS categorie_nom, u.username
        FROM transaction_wallet t
        LEFT JOIN category c ON t.category_id = c.id_category
        LEFT JOIN users u ON t.user_id = u.id
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
                t.setCategorie(rs.getString("categorie_nom"));
                t.setUsername(rs.getString("username"));


                list.add(t);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public double getTotalIncome() {
        double total = 0;

        try {
            String req = "SELECT SUM(montant) FROM transaction_wallet WHERE type='income'";
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

    public double getTotalOutcome() {
        double total = 0;

        try {
            String req = "SELECT SUM(montant) FROM transaction_wallet WHERE type='outcome'";
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

    public List<transaction> afficherParUser(int userId) {

        List<transaction> list = new ArrayList<>();

        try {

            String query = """
                       SELECT t.*, c.nom AS categorie_nom, u.username
                       FROM transaction_wallet t
                       LEFT JOIN category c ON t.category_id = c.id_category
                       LEFT JOIN users u ON t.user_id = u.id
                       WHERE t.user_id = ?
                    """;

            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, userId);

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
                t.setUsername(rs.getString("username"));

                list.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

}
