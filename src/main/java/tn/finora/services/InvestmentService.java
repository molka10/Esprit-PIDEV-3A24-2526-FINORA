package tn.finora.services;

import tn.finora.entities.Investment;
import tn.finora.utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InvestmentService {

    private final Connection cnx;

    public InvestmentService() {
        cnx = DBConnection.getInstance().getCnx();
    }

    public int add(Investment inv) {
        String sql = "INSERT INTO investment (name, category, location, estimated_value, risk_level, image_url, description, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, inv.getName());
            ps.setString(2, inv.getCategory());
            ps.setString(3, inv.getLocation());
            ps.setBigDecimal(4, inv.getEstimatedValue());
            ps.setString(5, inv.getRiskLevel());
            ps.setString(6, inv.getImageUrl());
            ps.setString(7, inv.getDescription());

            LocalDateTime dt = inv.getCreatedAt() != null ? inv.getCreatedAt() : LocalDateTime.now();
            ps.setTimestamp(8, Timestamp.valueOf(dt));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("❌ add investment: " + e.getMessage(), e);
        }
    }

    public List<Investment> getAll() {
        List<Investment> list = new ArrayList<>();
        String sql = "SELECT * FROM investment ORDER BY investment_id DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Investment inv = new Investment();
                inv.setInvestmentId(rs.getInt("investment_id"));
                inv.setName(rs.getString("name"));
                inv.setCategory(rs.getString("category"));
                inv.setLocation(rs.getString("location"));
                inv.setEstimatedValue(rs.getBigDecimal("estimated_value"));
                inv.setRiskLevel(rs.getString("risk_level"));
                inv.setImageUrl(rs.getString("image_url"));
                inv.setDescription(rs.getString("description"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) inv.setCreatedAt(ts.toLocalDateTime());
                list.add(inv);
            }
        } catch (SQLException e) {
            throw new RuntimeException("❌ getAll investment: " + e.getMessage(), e);
        }
        return list;
    }

    public void update(Investment inv) {
        String sql = "UPDATE investment SET name=?, category=?, location=?, estimated_value=?, risk_level=?, image_url=?, description=? WHERE investment_id=?";
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
            throw new RuntimeException("❌ update investment: " + e.getMessage(), e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM investment WHERE investment_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("❌ delete investment: " + e.getMessage(), e);
        }
    }
}
