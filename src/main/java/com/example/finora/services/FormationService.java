package com.example.finora.services;

import com.example.finora.entities.Formation;
import com.example.finora.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormationService {

    private final Connection cnx = DBConnection.getInstance().getCnx();

    public void add(Formation f) throws SQLException {
        String sql = "INSERT INTO formation(titre, description, categorie, niveau, is_published, image_url) VALUES (?,?,?,?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, f.getTitre());
        ps.setString(2, f.getDescription());
        ps.setString(3, f.getCategorie());
        ps.setString(4, f.getNiveau());
        ps.setBoolean(5, f.isPublished());
        ps.setString(6, f.getImageUrl());
        ps.executeUpdate();
    }

    public List<Formation> getAll() throws SQLException {
        List<Formation> list = new ArrayList<>();
        String sql = "SELECT * FROM formation ORDER BY id DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Formation f = new Formation();
            f.setId(rs.getInt("id"));
            f.setTitre(rs.getString("titre"));
            f.setDescription(rs.getString("description"));
            f.setCategorie(rs.getString("categorie"));
            f.setNiveau(rs.getString("niveau"));
            f.setPublished(rs.getBoolean("is_published"));
            f.setImageUrl(rs.getString("image_url"));
            list.add(f);
        }
        return list;
    }

    public void update(Formation f) throws SQLException {
        String sql = "UPDATE formation SET titre=?, description=?, categorie=?, niveau=?, is_published=?, image_url=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, f.getTitre());
        ps.setString(2, f.getDescription());
        ps.setString(3, f.getCategorie());
        ps.setString(4, f.getNiveau());
        ps.setBoolean(5, f.isPublished());
        ps.setString(6, f.getImageUrl());
        ps.setInt(7, f.getId());

        int rows = ps.executeUpdate();
        if (rows == 0) {
            System.out.println("⚠️ Aucun enregistrement trouvé avec id=" + f.getId());
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM formation WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);

        int rows = ps.executeUpdate();
        if (rows == 0) {
            System.out.println("⚠️ Aucun enregistrement trouvé avec id=" + id);
        }
    }
}
