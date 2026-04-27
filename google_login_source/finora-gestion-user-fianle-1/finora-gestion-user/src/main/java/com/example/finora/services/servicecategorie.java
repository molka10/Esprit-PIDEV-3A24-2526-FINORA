package com.example.finora.services;

import com.example.finora.entities.categorie;
import com.example.finora.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class servicecategorie implements Iservicecategorie {

    Connection cnx;

    public servicecategorie() {
        try {
            cnx = DBConnection.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // pour jointure
    public int getIdByName(String nom) {
        String sql = "SELECT id_category FROM category WHERE nom = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, nom);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_category");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int getIdByNameAndType(String nom, String type) {
        String sql = "SELECT id_category FROM category WHERE LOWER(nom) = LOWER(?) AND LOWER(type) = LOWER(?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, nom);
            ps.setString(2, type);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_category");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public boolean existsByName(String nom, String type) {

        String sql = "SELECT COUNT(*) FROM category WHERE LOWER(nom)=LOWER(?) AND type=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, nom);
            ps.setString(2, type);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void ajouter(categorie c) {

        String sql = "INSERT INTO category (nom, priorite, type, user_id) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, c.getNom());
            ps.setString(2, c.getPriorite());
            ps.setString(3, c.getType());
            ps.setInt(4, c.getUserId());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(categorie c) {

        String sql = "UPDATE category SET nom=?, priorite=?, type=? WHERE id_category=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, c.getNom());
            ps.setString(2, c.getPriorite());
            ps.setString(3, c.getType());
            ps.setInt(4, c.getId_category());
            ps.executeUpdate();

            System.out.println("Catégorie modifiée");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(int id, int userId) {

        String sql = "DELETE FROM category WHERE id_category=? AND user_id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<categorie> afficher() {

        List<categorie> list = new ArrayList<>();
        String sql = "SELECT * FROM category";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

                categorie c = new categorie(
                        rs.getInt("id_category"),
                        rs.getString("nom"),
                        rs.getString("priorite"),
                        rs.getString("type"));

                list.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public categorie getById(int id) {

        String sql = "SELECT * FROM category WHERE id_category = ?";
        categorie c = null;

        try {
            PreparedStatement pst = cnx.prepareStatement(sql);
            pst.setInt(1, id);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                c = new categorie(
                        rs.getInt("id_category"),
                        rs.getString("nom"),
                        rs.getString("priorite"),
                        rs.getString("type"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return c;
    }

    public List<categorie> getByType(String type) {

        List<categorie> list = new ArrayList<>();
        String sql = "SELECT * FROM category WHERE UPPER(TRIM(type)) = UPPER(TRIM(?))";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, type);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new categorie(
                        rs.getInt("id_category"),
                        rs.getString("nom"),
                        rs.getString("priorite"),
                        rs.getString("type")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<categorie> getByRole(String role) {

        List<categorie> list = new ArrayList<>();
        String sql = "SELECT * FROM category WHERE role = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, role);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                categorie c = new categorie(
                        rs.getInt("id_category"),
                        rs.getString("nom"),
                        rs.getString("priorite"),
                        rs.getString("type"));

                list.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<categorie> getByRoleAndType(String role, String type) {

        List<categorie> list = new ArrayList<>();

        String sql = "SELECT * FROM category WHERE role = ? AND type = ?";

        try {

            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, role);
            ps.setString(2, type);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                categorie c = new categorie(
                        rs.getInt("id_category"),
                        rs.getString("nom"),
                        rs.getString("priorite"),
                        rs.getString("type"));

                list.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    public List<categorie> getByUserAndType(int userId, String type) {

        List<categorie> list = new ArrayList<>();

        String sql = "SELECT * FROM category WHERE user_id = ? AND UPPER(type)=UPPER(?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, type);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new categorie(
                        rs.getInt("id_category"),
                        rs.getString("nom"),
                        rs.getString("priorite"),
                        rs.getString("type"),
                        rs.getInt("user_id")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    public int getIdByNameTypeAndUser(String nom, String type, int userId) {

        String sql = "SELECT id_category FROM category WHERE LOWER(nom)=LOWER(?) AND LOWER(type)=LOWER(?) AND user_id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, nom);
            ps.setString(2, type);
            ps.setInt(3, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id_category");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
