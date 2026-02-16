package com.example.finora_user.services;

import com.example.finora_user.entities.User;
import com.example.finora_user.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private Connection cn;

    public UserService() throws SQLException {
        cn = DBConnection.getInstance().getConnection();
    }

    // CREATE
    public void addUser(User u) throws SQLException {
        String sql = "INSERT INTO users (username, email, mot_de_passe, role) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setString(1, u.getUsername());
        ps.setString(2, u.getEmail());
        ps.setString(3, u.getMotDePasse());
        ps.setString(4, u.getRole());
        ps.executeUpdate();
    }

    // READ ALL
    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setUsername(rs.getString("username"));
            u.setEmail(rs.getString("email"));
            u.setMotDePasse(rs.getString("mot_de_passe"));
            u.setRole(rs.getString("role"));
            list.add(u);
        }
        return list;
    }

    // READ BY ID
    public User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id=?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setUsername(rs.getString("username"));
            u.setEmail(rs.getString("email"));
            u.setMotDePasse(rs.getString("mot_de_passe"));
            u.setRole(rs.getString("role"));
            return u;
        }
        return null;

    }


    // UPDATE
    public boolean updateUser(User u) throws SQLException {
        String sql = "UPDATE users SET username=?, email=?, role=? WHERE id=?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setString(1, u.getUsername());
        ps.setString(2, u.getEmail());
        ps.setString(3, u.getRole());
        ps.setInt(4, u.getId());
        return ps.executeUpdate() > 0;
    }



    // DELETE
    public boolean deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id=?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setInt(1, id);
        return ps.executeUpdate() > 0;
    }

    // CREATE and return generated ID (for testing)
    public int addUserReturnId(User u) throws SQLException {
        String sql = "INSERT INTO users (username, email, mot_de_passe, role) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, u.getUsername());
        ps.setString(2, u.getEmail());
        ps.setString(3, u.getMotDePasse());
        ps.setString(4, u.getRole());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }
    public User login(String email, String motDePasse) throws SQLException {
        String sql = "SELECT * FROM users WHERE email=? AND mot_de_passe=?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setString(1, email);
        ps.setString(2, motDePasse);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setUsername(rs.getString("username"));
            u.setEmail(rs.getString("email"));
            u.setMotDePasse(rs.getString("mot_de_passe"));
            u.setRole(rs.getString("role"));
            return u;
        }
        return null;
    }
    // Search by username/email
    public java.util.List<User> searchText(String q) throws java.sql.SQLException {
        java.util.List<User> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM users WHERE username LIKE ? OR email LIKE ? ORDER BY id DESC";
        java.sql.PreparedStatement ps = cn.prepareStatement(sql);
        String like = "%" + q + "%";
        ps.setString(1, like);
        ps.setString(2, like);

        java.sql.ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    // Search by role (ADMIN/ENTREPRISE/USER)
    public java.util.List<User> searchByRole(String role) throws java.sql.SQLException {
        java.util.List<User> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM users WHERE role=? ORDER BY id DESC";
        java.sql.PreparedStatement ps = cn.prepareStatement(sql);
        ps.setString(1, role);

        java.sql.ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(map(rs));
        return list;
    }
    // Convert ResultSet → User object
    private User map(ResultSet rs) throws SQLException {

        User u = new User();

        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setRole(rs.getString("role"));

        return u;
    }




}
