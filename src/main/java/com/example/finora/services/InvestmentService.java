package com.example.finora.services;

import com.example.finora.entities.Investment;
import com.example.finora.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvestmentService {

    private final Connection cnx;

    public InvestmentService() {
        cnx = DBConnection.getInstance().getCnx();
    }

    // =====================================
    // ADD
    // =====================================

    public void add(Investment inv) {

        String sql = """
                INSERT INTO investment
                (name, category, location, estimated_value,
                 risk_level, image_url, description, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, inv.getName());
            ps.setString(2, inv.getCategory());
            ps.setString(3, inv.getLocation());
            ps.setBigDecimal(4, inv.getEstimatedValue());
            ps.setString(5, inv.getRiskLevel());
            ps.setString(6, inv.getImageUrl());
            ps.setString(7, inv.getDescription());
            ps.setTimestamp(8, Timestamp.valueOf(inv.getCreatedAt()));

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Add investment error: " + e.getMessage(), e);
        }
    }

    // =====================================
    // UPDATE
    // =====================================

    public void update(Investment inv) {

        String sql = """
                UPDATE investment
                SET name = ?,
                    category = ?,
                    location = ?,
                    estimated_value = ?,
                    risk_level = ?,
                    image_url = ?,
                    description = ?
                WHERE investment_id = ?
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, inv.getName());
            ps.setString(2, inv.getCategory());
            ps.setString(3, inv.getLocation());
            ps.setBigDecimal(4, inv.getEstimatedValue());
            ps.setString(5, inv.getRiskLevel());
            ps.setString(6, inv.getImageUrl());
            ps.setString(7, inv.getDescription());
            ps.setInt(8, inv.getInvestmentId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Update investment error: " + e.getMessage(), e);
        }
    }

    // =====================================
    // DELETE
    // =====================================

    public void delete(int id) {

        String sql = "DELETE FROM investment WHERE investment_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Delete investment error: " + e.getMessage(), e);
        }
    }

    // =====================================
    // GET ALL
    // =====================================

    public List<Investment> getAll() {

        List<Investment> list = new ArrayList<>();

        String sql = "SELECT * FROM investment ORDER BY investment_id DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Investment inv = new Investment(
                        rs.getInt("investment_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("location"),
                        rs.getBigDecimal("estimated_value"),
                        rs.getString("risk_level"),
                        rs.getString("image_url"),
                        rs.getString("description"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );

                list.add(inv);
            }

        } catch (SQLException e) {
            throw new RuntimeException("GetAll investment error: " + e.getMessage(), e);
        }

        return list;
    }
}