package com.example.finora_user.services;

import com.example.finora_user.entities.User;
import com.example.finora_user.utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private Connection cn;

    public UserService() throws SQLException {
        cn = DBConnection.getInstance().getConnection();
    }

    // ================= CREATE =================
    public int addUserReturnId(User u) throws SQLException {

        String sql = """
                INSERT INTO users 
                (username, email, mot_de_passe, role, phone, address, date_of_birth)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, u.getUsername());
        ps.setString(2, u.getEmail());
        ps.setString(3, u.getMotDePasse());
        ps.setString(4, u.getRole());
        ps.setString(5, u.getPhone());
        ps.setString(6, u.getAddress());

        if (u.getDateOfBirth() != null) {
            ps.setDate(7, Date.valueOf(u.getDateOfBirth()));
        } else {
            ps.setNull(7, Types.DATE);
        }

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }

        return -1;
    }

    // ================= READ ALL =================
    public List<User> getAllUsers() throws SQLException {

        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id DESC";

        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(map(rs));
        }

        return list;
    }

    // ================= READ BY ID =================
    public User getUserById(int id) throws SQLException {

        String sql = "SELECT * FROM users WHERE id=?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return map(rs);
        }

        return null;
    }

    // ================= UPDATE =================
    public boolean updateUser(User u) throws SQLException {

        String sql = """
                UPDATE users 
                SET username=?, email=?, role=?, phone=?, address=?, date_of_birth=?
                WHERE id=?
                """;

        PreparedStatement ps = cn.prepareStatement(sql);

        ps.setString(1, u.getUsername());
        ps.setString(2, u.getEmail());
        ps.setString(3, u.getRole());
        ps.setString(4, u.getPhone());
        ps.setString(5, u.getAddress());

        if (u.getDateOfBirth() != null) {
            ps.setDate(6, Date.valueOf(u.getDateOfBirth()));
        } else {
            ps.setNull(6, Types.DATE);
        }

        ps.setInt(7, u.getId());

        return ps.executeUpdate() > 0;
    }

    // ================= DELETE =================
    public boolean deleteUser(int id) throws SQLException {

        String sql = "DELETE FROM users WHERE id=?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setInt(1, id);

        return ps.executeUpdate() > 0;
    }

    // ================= LOGIN =================
    public User login(String email, String motDePasse) throws SQLException {

        String sql = "SELECT * FROM users WHERE email=? AND mot_de_passe=?";
        PreparedStatement ps = cn.prepareStatement(sql);

        ps.setString(1, email);
        ps.setString(2, motDePasse);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return map(rs);
        }

        return null;
    }

    // ================= SEARCH TEXT =================
    public List<User> searchText(String q) throws SQLException {

        List<User> list = new ArrayList<>();

        String sql = """
                SELECT * FROM users
                WHERE username LIKE ? OR email LIKE ?
                ORDER BY id DESC
                """;

        PreparedStatement ps = cn.prepareStatement(sql);

        String like = "%" + q + "%";
        ps.setString(1, like);
        ps.setString(2, like);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(map(rs));
        }

        return list;
    }

    // ================= SEARCH BY ROLE =================
    public List<User> searchByRole(String role) throws SQLException {

        List<User> list = new ArrayList<>();

        String sql = "SELECT * FROM users WHERE role=? ORDER BY id DESC";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setString(1, role);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(map(rs));
        }

        return list;
    }

    // ================= MAP RESULTSET =================
    private User map(ResultSet rs) throws SQLException {

        User u = new User();

        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setRole(rs.getString("role"));
        u.setPhone(rs.getString("phone"));
        u.setAddress(rs.getString("address"));

        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            u.setDateOfBirth(dob.toLocalDate());
        }

        return u;
    }
}
