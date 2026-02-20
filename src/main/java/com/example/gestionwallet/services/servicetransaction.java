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

        String sql = """
            INSERT INTO transaction
            (nom_transaction, type, montant, date_transaction, source, user_id, category_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

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
            UPDATE transaction
            SET nom_transaction=?, type=?, montant=?, date_transaction=?, category_id=?
            WHERE id_transaction=?
        """;

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, t.getNom_transaction());
            ps.setString(2, t.getType());
            ps.setDouble(3, t.getMontant());
            ps.setDate(4, t.getDate_transaction());
            ps.setInt(5, t.getCategory_id());
            ps.setInt(6, t.getId_transaction());
            ps.executeUpdate();

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

    @Override
    public List<transaction> afficher() {

        List<transaction> list = new ArrayList<>();

        String sql = "SELECT * FROM transaction";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

                transaction t = new transaction(
                        rs.getInt("id_transaction"),
                        rs.getString("nom_transaction"),
                        rs.getString("type"),
                        rs.getDouble("montant"),
                        rs.getDate("date_transaction"),
                        rs.getString("source"),
                        rs.getInt("user_id"),
                        rs.getInt("category_id")
                );

                list.add(t);
            }

        } catch (SQLException e) {
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
}
