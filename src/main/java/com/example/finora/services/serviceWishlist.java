package com.example.finora.services;

import com.example.finora.entities.Wishlist;
import com.example.finora.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class serviceWishlist {

    private Connection cnx;

    public serviceWishlist() {
        cnx = DBConnection.getInstance().getConnection();
    }

    public void ajouter(Wishlist w) {
        try {
            String sql = "INSERT INTO wishlist (user_id, name, price) VALUES (?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, w.getUserId());
            ps.setString(2, w.getName());
            ps.setDouble(3, w.getPrice());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Wishlist> afficher(int userId) {

        List<Wishlist> list = new ArrayList<>();

        try {
            String sql = "SELECT * FROM wishlist WHERE user_id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Wishlist(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getDouble("price")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void supprimer(int id) {
        try {
            String sql = "DELETE FROM wishlist WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}