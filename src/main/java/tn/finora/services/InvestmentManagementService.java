package tn.finora.services;

import tn.finora.entities.InvestmentManagement;
import tn.finora.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class    InvestmentManagementService {

    private final Connection cnx;

    public InvestmentManagementService() {
        cnx = DBConnection.getInstance().getCnx();
    }

    public int add(InvestmentManagement im) {
        String sql = "INSERT INTO investment_management (investment_id, investment_type, amount_invested, ownership_percentage, start_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, im.getInvestmentId());
            ps.setString(2, im.getInvestmentType());
            ps.setBigDecimal(3, im.getAmountInvested());
            ps.setBigDecimal(4, im.getOwnershipPercentage());
            ps.setDate(5, Date.valueOf(im.getStartDate()));
            ps.setString(6, im.getStatus());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("❌ add management: " + e.getMessage(), e);
        }
    }
    public List<InvestmentManagement> getAll() {
        List<InvestmentManagement> list = new ArrayList<>();
        String sql = "SELECT * FROM investment_management ORDER BY management_id DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InvestmentManagement im = new InvestmentManagement();
                im.setManagementId(rs.getInt("management_id"));
                im.setInvestmentId(rs.getInt("investment_id"));
                im.setInvestmentType(rs.getString("investment_type"));
                im.setAmountInvested(rs.getBigDecimal("amount_invested"));
                im.setOwnershipPercentage(rs.getBigDecimal("ownership_percentage"));

                Date d = rs.getDate("start_date");
                if (d != null) im.setStartDate(d.toLocalDate());

                im.setStatus(rs.getString("status"));
                list.add(im);
            }

        } catch (SQLException e) {
            throw new RuntimeException("❌ getAll: " + e.getMessage(), e);
        }

        return list;
    }

    public List<InvestmentManagement> getAllByInvestment(int investmentId) {
        List<InvestmentManagement> list = new ArrayList<>();
        String sql = "SELECT * FROM investment_management WHERE investment_id=? ORDER BY management_id DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, investmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvestmentManagement im = new InvestmentManagement();
                    im.setManagementId(rs.getInt("management_id"));
                    im.setInvestmentId(rs.getInt("investment_id"));
                    im.setInvestmentType(rs.getString("investment_type"));
                    im.setAmountInvested(rs.getBigDecimal("amount_invested"));
                    im.setOwnershipPercentage(rs.getBigDecimal("ownership_percentage"));
                    Date d = rs.getDate("start_date");
                    if (d != null) im.setStartDate(d.toLocalDate());
                    im.setStatus(rs.getString("status"));
                    list.add(im);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("❌ getAllByInvestment: " + e.getMessage(), e);
        }
        return list;
    }

    public void update(InvestmentManagement im) {
        String sql = "UPDATE investment_management SET investment_id=?, investment_type=?, amount_invested=?, ownership_percentage=?, start_date=?, status=? " +
                "WHERE management_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, im.getInvestmentId());
            ps.setString(2, im.getInvestmentType());
            ps.setBigDecimal(3, im.getAmountInvested());
            ps.setBigDecimal(4, im.getOwnershipPercentage());
            ps.setDate(5, Date.valueOf(im.getStartDate()));
            ps.setString(6, im.getStatus());
            ps.setInt(7, im.getManagementId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("❌ update management: " + e.getMessage(), e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM investment_management WHERE management_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("❌ delete management: " + e.getMessage(), e);
        }
    }
}
