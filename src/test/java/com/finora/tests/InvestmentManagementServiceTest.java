package com.finora.tests;

import org.junit.jupiter.api.Test;
import tn.finora.entities.InvestmentManagement;
import tn.finora.services.InvestmentManagementService;
import tn.finora.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InvestmentManagementServiceTest {

    private final int existingInvestmentId = 1;

    private int insertRow() throws Exception {
        Connection cnx = DBConnection.getInstance().getCnx();

        String insertSql = "INSERT INTO investment_management " +
                "(investment_id, investment_type, amount_invested, ownership_percentage, start_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, existingInvestmentId);
            ps.setString(2, "SHORT_TERM");
            ps.setBigDecimal(3, new BigDecimal("2000.00"));
            ps.setBigDecimal(4, new BigDecimal("20.00"));
            ps.setDate(5, Date.valueOf(LocalDate.now()));
            ps.setString(6, "ACTIVE");

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                assertTrue(keys.next(), " No generated key returned!");
                return keys.getInt(1);
            }
        }
    }

    private void deleteRow(int managementId) throws Exception {
        Connection cnx = DBConnection.getInstance().getCnx();
        try (PreparedStatement ps = cnx.prepareStatement(
                "DELETE FROM investment_management WHERE management_id=?")) {
            ps.setInt(1, managementId);
            ps.executeUpdate();
        }
    }

    @Test
    void testAdd() throws Exception {
        InvestmentManagementService service = new InvestmentManagementService();

        InvestmentManagement im = new InvestmentManagement(
                existingInvestmentId,
                "LONG_TERM",
                new BigDecimal("1000.00"),
                new BigDecimal("10.00"),
                LocalDate.now(),
                "ACTIVE"
        );

        service.add(im);

        Connection cnx = DBConnection.getInstance().getCnx();
        String sql = "SELECT * FROM investment_management ORDER BY management_id DESC LIMIT 1";

        int insertedId;

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            assertTrue(rs.next(), " No row found after add!");
            insertedId = rs.getInt("management_id");

            assertEquals(existingInvestmentId, rs.getInt("investment_id"));
            assertEquals("LONG_TERM", rs.getString("investment_type"));
            assertEquals("ACTIVE", rs.getString("status"));
            assertEquals(new BigDecimal("1000.00"), rs.getBigDecimal("amount_invested"));
        }

        deleteRow(insertedId);
    }

    @Test
    void testGetAll() throws Exception {
        InvestmentManagementService service = new InvestmentManagementService();
        int insertedId = insertRow();

        List<InvestmentManagement> list = service.getAll();
        assertNotNull(list);
        assertTrue(list.size() > 0);

        boolean found = list.stream().anyMatch(x -> x.getManagementId() == insertedId);
        assertTrue(found);

        deleteRow(insertedId);
    }

    @Test
    void testUpdate() throws Exception {
        InvestmentManagementService service = new InvestmentManagementService();
        int insertedId = insertRow();

        InvestmentManagement updated = new InvestmentManagement(
                insertedId,
                existingInvestmentId,
                "UPDATED_TYPE",
                new BigDecimal("9999.00"),
                new BigDecimal("55.50"),
                LocalDate.now(),
                "CLOSED"
        );

        service.update(updated);

        Connection cnx = DBConnection.getInstance().getCnx();
        String sql = "SELECT * FROM investment_management WHERE management_id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, insertedId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("UPDATED_TYPE", rs.getString("investment_type"));
                assertEquals("CLOSED", rs.getString("status"));
                assertEquals(new BigDecimal("9999.00"), rs.getBigDecimal("amount_invested"));
                assertEquals(new BigDecimal("55.50"), rs.getBigDecimal("ownership_percentage"));
            }
        }

        deleteRow(insertedId);
    }

    @Test
    void testDelete() throws Exception {
        InvestmentManagementService service = new InvestmentManagementService();
        int insertedId = insertRow();

        service.delete(insertedId);

        Connection cnx = DBConnection.getInstance().getCnx();
        String sql = "SELECT COUNT(*) FROM investment_management WHERE management_id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, insertedId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1));
            }
        }
    }
}
