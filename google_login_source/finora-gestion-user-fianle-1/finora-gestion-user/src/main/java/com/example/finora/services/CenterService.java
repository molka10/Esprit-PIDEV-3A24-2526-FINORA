package com.example.finora.services;

import com.example.finora.entities.Center;
import com.example.finora.utils.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CenterService {

    private Connection cnx;

    public CenterService() {
        try {
            this.cnx = DBConnection.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Center> getAll() {
        List<Center> list = new ArrayList<>();
        String sql = "SELECT id, name, address, lat, lng FROM training_center";
        try (Statement st = cnx.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Center(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getDouble("lat"),
                        rs.getDouble("lng")));
            }
        } catch (Exception e) {
            System.err.println("CenterService.getAll: " + e.getMessage());
        }
        return list;
    }
    public int add(Center center) throws SQLException {
        String sql = "INSERT INTO training_center (name, address, lat, lng) VALUES (?, ?, ?, ?)";

        try (java.sql.PreparedStatement pst = cnx.prepareStatement(sql,
                java.sql.Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, center.name());
            pst.setString(2, center.address());
            pst.setDouble(3, center.lat());
            pst.setDouble(4, center.lng());

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        System.out.println("✅ Centre ajouté avec ID: " + id);
                        return id;
                    }
                }
            }

            throw new SQLException("Échec de l'ajout du centre");
        }
    }
}