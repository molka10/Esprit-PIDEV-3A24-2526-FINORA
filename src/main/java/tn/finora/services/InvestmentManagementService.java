package tn.finora.services;

import tn.finora.entities.InvestmentManagement;
import tn.finora.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvestmentManagementService {

    private final Connection cnx;

    public InvestmentManagementService() {
        cnx = DBConnection.getInstance().getCnx();
    }

    // =====================================================
    // ADD
    // =====================================================
    public int add(InvestmentManagement im) {

        validateBusinessRules(im);

        String sql = """
                INSERT INTO investment_management
                (investment_id, investment_type, amount_invested,
                 ownership_percentage, start_date, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps =
                     cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, im.getInvestmentId());
            ps.setString(2, im.getInvestmentType());
            ps.setBigDecimal(3, im.getAmountInvested());
            ps.setBigDecimal(4, im.getOwnershipPercentage());
            ps.setDate(5, Date.valueOf(im.getStartDate()));
            ps.setString(6, im.getStatus());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    closeInvestmentIfFullyOwned(im.getInvestmentId());
                    return keys.getInt(1);
                }
            }

            return -1;

        } catch (SQLException e) {
            throw new RuntimeException("Add management error: "
                    + e.getMessage(), e);
        }
    }

    // =====================================================
    // UPDATE
    // =====================================================
    public void update(InvestmentManagement im) {

        validateBusinessRules(im);

        String sql = """
                UPDATE investment_management SET
                investment_id=?,
                investment_type=?,
                amount_invested=?,
                ownership_percentage=?,
                start_date=?,
                status=?
                WHERE management_id=?
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, im.getInvestmentId());
            ps.setString(2, im.getInvestmentType());
            ps.setBigDecimal(3, im.getAmountInvested());
            ps.setBigDecimal(4, im.getOwnershipPercentage());
            ps.setDate(5, Date.valueOf(im.getStartDate()));
            ps.setString(6, im.getStatus());
            ps.setInt(7, im.getManagementId());

            ps.executeUpdate();

            closeInvestmentIfFullyOwned(im.getInvestmentId());

        } catch (SQLException e) {
            throw new RuntimeException("Update management error: "
                    + e.getMessage(), e);
        }
    }

    // =====================================================
    // DELETE
    // =====================================================
    public void delete(int id) {

        String sql =
                "DELETE FROM investment_management WHERE management_id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Delete management error: "
                    + e.getMessage(), e);
        }
    }

    // =====================================================
    // GET ALL (JOIN)
    // =====================================================
    public List<InvestmentManagement> getAll() {

        List<InvestmentManagement> list = new ArrayList<>();

        String sql = """
                SELECT im.*, i.name AS investment_name
                FROM investment_management im
                JOIN investment i
                ON im.investment_id = i.investment_id
                ORDER BY im.management_id DESC
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs, true));
            }

        } catch (SQLException e) {
            throw new RuntimeException("getAll JOIN error: "
                    + e.getMessage(), e);
        }

        return list;
    }

    // =====================================================
    // 🔥 BUSINESS METHODS
    // =====================================================

    public BigDecimal getTotalOwnershipForInvestment(int investmentId) {

        String sql = """
                SELECT COALESCE(SUM(ownership_percentage),0)
                FROM investment_management
                WHERE investment_id=?
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, investmentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ownership calculation error: "
                    + e.getMessage(), e);
        }

        return BigDecimal.ZERO;
    }

    public void closeInvestmentIfFullyOwned(int investmentId) {

        BigDecimal total = getTotalOwnershipForInvestment(investmentId);

        if (total.compareTo(new BigDecimal("100")) >= 0) {

            String updateSql = """
                    UPDATE investment_management
                    SET status='CLOSED'
                    WHERE investment_id=?
                    """;

            try (PreparedStatement ps = cnx.prepareStatement(updateSql)) {

                ps.setInt(1, investmentId);
                ps.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException("Auto-close error: "
                        + e.getMessage(), e);
            }
        }
    }

    // =====================================================
    // VALIDATION BACKEND (SECURITY)
    // =====================================================
    private void validateBusinessRules(InvestmentManagement im) {

        if (im.getAmountInvested()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be > 0.");
        }

        if (im.getOwnershipPercentage()
                .compareTo(BigDecimal.ZERO) < 0 ||
                im.getOwnershipPercentage()
                        .compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("Ownership must be between 0 and 100.");
        }
    }

    // =====================================================
    // CLEAN MAPPER
    // =====================================================
    private InvestmentManagement mapResultSet(ResultSet rs,
                                              boolean includeInvestmentName)
            throws SQLException {

        InvestmentManagement im = new InvestmentManagement();

        im.setManagementId(rs.getInt("management_id"));
        im.setInvestmentId(rs.getInt("investment_id"));
        im.setInvestmentType(rs.getString("investment_type"));
        im.setAmountInvested(rs.getBigDecimal("amount_invested"));
        im.setOwnershipPercentage(rs.getBigDecimal("ownership_percentage"));

        Date d = rs.getDate("start_date");
        if (d != null)
            im.setStartDate(d.toLocalDate());

        im.setStatus(rs.getString("status"));

        if (includeInvestmentName) {
            im.setInvestmentName(rs.getString("investment_name"));
        }

        return im;
    }
}