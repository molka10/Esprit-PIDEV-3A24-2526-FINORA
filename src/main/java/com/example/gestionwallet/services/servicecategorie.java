package com.example.gestionwallet.services;

import com.example.gestionwallet.interfaces.services.Iservicecategorie;
import com.example.gestionwallet.models.categorie;
import com.example.gestionwallet.utils.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class servicecategorie implements Iservicecategorie {

    Connection cnx;

    public servicecategorie() {
        cnx = database.getInstance().getConnection();
    }

    //pour jointure
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

        if (existsByName(c.getNom(), c.getType())) {
            System.out.println(" Catégorie déjà existante ");
            return;
        }

        String sql = "INSERT INTO category (nom, priorite, type) VALUES (?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, c.getNom());
            ps.setString(2, c.getPriorite());
            ps.setString(3, c.getType());
            ps.executeUpdate();

            System.out.println("Catégorie ajoutée");

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
    public void supprimer(int id) {

        String sql = "DELETE FROM category WHERE id_category=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();

            System.out.println("Catégorie supprimée");

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
                        rs.getString("type")
                );

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
                        rs.getString("type")
                );
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
                        rs.getString("type")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
